package ang.suite;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.bson.Document;

import com.mongodb.client.FindIterable;

import de.uni_koeln.spinfo.ang.utils.AngStringUtils;
import de.uni_koeln.spinfo.ang.utils.IO;
import de.uni_koeln.spinfo.ang.utils.JarExec;
import de.uni_koeln.spinfo.ang.utils.MongoWrapper;
import de.uni_koeln.spinfo.ang.utils.ProgressFeedback;



public class DISCOWrapper {

	private static final String DISCO_JAR_PATH = "DISCO" + File.separator + "disco-2.1.jar";
	private static final String BUILDER_JAR_PATH = "DISCO" + File.separator + "DISCOBuilder-1.0.jar";
	private static final String BUILDER_CONFIG_PATH = "DISCO" + File.separator + "disco.config";
	private static final String TEMP_WORDSPACE_PATH = "DISCO" + File.separator + "wordspace_";
	private static final String TEMP_CORPUS_PATH = "DISCO" + File.separator + "corpus_";
	private static final String RESULTS_DIR_PATH = "results";
	private static final String STOPWORD_PATH = "DISCO" + File.separator + "stopword-lists" + File.separator
			+ "stopword-list_de_utf8.txt";
	private static final File DEFAULT_WORKING_DIR = new File("temp").getParentFile();
	private static final long MAX_CORPUS_SIZE_FOR_ANALYSIS = 32000000;
	private static final int JVM_MEMORY_DISCO_BUILDER_MB = 2048;
	private static final int JVM_MEMORY_DISCO_MB = 512;
	private static final String OUTPUT_SECTION_SEPARATOR = "====================================";

	private MongoWrapper mongo;

	private String word1;
	private String word2;
	private String dbQuery;
	private String source;
	private int yearFrom;
	private int yearTo;
	private int contextWordsLeftRight;
	private int numberFeatureWords;
	private String runID;
	private boolean substrings;
	private Map<String, Integer> compounds;
	private Set<String> stopWords;
	private boolean useStopWords;
	
	

	private void init() {
		this.mongo = new MongoWrapper();
		Properties props = IO.loadProperties("db.properties", this.getClass());

		mongo.init(props.getProperty("user"), // USER
				props.getProperty("pw"), // PASS
				props.getProperty("db"), // DB
				props.getProperty("host"), // HOST
				props.getProperty("port"), // PORT
				props.getProperty("collection"));// COLLECTION
	}

	public File runAnalysis(String word1, String word2, String dbQuery, String source, int yearFrom, int yearTo,
			boolean substrings, int contextWordsLeftRight, int numberFeatureWords,
			boolean keepWordSpace, boolean useStopWords) throws IOException {
		// init fields
		this.word1 = word1.toUpperCase();
		this.word2 = word2 != null ? word2.toUpperCase() : null;
		this.dbQuery = (dbQuery != null ? dbQuery.toUpperCase() : null);
		this.source = source;
		this.useStopWords = useStopWords;
		this.yearFrom = yearFrom;
		this.yearTo = yearTo;
		this.contextWordsLeftRight = contextWordsLeftRight;
		this.numberFeatureWords = numberFeatureWords;
		this.substrings = substrings;
		this.compounds = new HashMap<String, Integer>();
		this.runID = buildRunID();
		
		System.out.println(buildResultsHeader() + "\n");

		// init monoDB connection
		System.out.println("Initializing database connection...");
		if (this.mongo == null)
			init();

		System.out.println("RunID for this analysis is " + runID);

		// look for existing word spaces with same criteria
		String wordSpacePath = null;
		String corpusPath = null;
		
		// build corpus
		System.out.println("Building corpus...");
		corpusPath = buildCorpus();

		if (IO.folderSize(new File(corpusPath).toPath()) <= MAX_CORPUS_SIZE_FOR_ANALYSIS) {
			// build word-space with DISCOBuilder
			System.out.println("Building word-space...");
			wordSpacePath = buildWordSpace(corpusPath);
		} else {
			System.out.println("[WARNING] corpus size exceeds maximum for vector analysis!");
		}

		System.out.println("Running naive analysis...");
		// run naive analysis
		String analysisResults = runNaiveAnalysis(corpusPath);

		if (wordSpacePath != null && new File(wordSpacePath).exists()) {
			System.out.println("Running DISCO analysis...");
			// run vector analysis via DISCO
			analysisResults += runVectorAnalysis(wordSpacePath);
		} else {
			System.out.println("Skipping DISCO analysis - wordspace could not be created!");
		}

		System.out.println("done.\n");
		if (this.mongo != null)
			this.mongo.close();
		this.mongo = null;

		// delete wordspace and corpus
		File wordSpace = null;
		if (wordSpacePath != null)
			wordSpace = new File(wordSpacePath);
		if (!keepWordSpace && wordSpace != null && wordSpace.exists())
			IO.deleteFolder(wordSpace.getParentFile());
		IO.deleteFolder(new File(corpusPath));

		// write results to file
		File resultsFile = new File(RESULTS_DIR_PATH + File.separator + runID + ".txt");
		IO.writeStringToFile(analysisResults, resultsFile.getAbsolutePath());
		return resultsFile;
	}

	private String buildCorpus() throws IOException {
		// create temporary corpus output directory
		File outputDir = new File(TEMP_CORPUS_PATH + runID);
		outputDir.mkdirs();
		
		// prep stopwords
		stopWords = new HashSet<String>();
		if (useStopWords){
			System.out.print("[INFO] building stopword list... ");
			stopWords.addAll(Arrays.asList(IO.readFile(STOPWORD_PATH)
					.toUpperCase().split("\n")));
			System.out.println("(" + stopWords.size() + " words)");
		}
		if (source != null) stopWords.add(source.toUpperCase());

		//build corpus
		buildCorpus(word1, outputDir);
		if (word2 != null && word2.length() > 1) buildCorpus(word2, outputDir);

		System.out.println("[INFO] total corpus size: "
				+ AngStringUtils.humanReadableByteCount(IO.folderSize(outputDir.toPath())));
		return outputDir.getAbsolutePath();
		// return new File("DISCO/test-corpus-dewac").getAbsolutePath();
	}
	
	private void buildCorpus(String word, File outputDir) throws IOException{
		System.out.print("[INFO] generating corpus for \"" + word + "\"... ");
		// query db for data
		FindIterable<Document> results;
		if (substrings){
			results = mongo.getSearchResults(
					parseRegexQuery(word), source, false, true,
					(yearFrom > -1 && yearTo > -1), yearFrom, yearTo, 25000);
		} else {
			results = mongo.getSearchResults(
					word, source, yearFrom, yearTo, false);
		}
		
		//create temporary corpus
		StringBuilder sb = new StringBuilder();
		int occCount = 0;
		for (Document doc : results) {
			String text = doc.getString("text").toUpperCase();
			text = text.replaceAll("\\-", " "); //remove hyphens
			if (!substrings && !text.matches(".*\\b" + word + "\\b.*")) continue; //ignore if only compounds were found
			text = seperateQuery(text, word); //separate composites
			text = text.replaceAll("\\P{L}+", " ").trim();
			text = removeTokens(stopWords, text); //remove stopwords
			
			//trim text to context windows
			List<String> texts = AngStringUtils.trimTextMulti(
					text, word, contextWordsLeftRight + 1);
			
			//System.out.print("\rWord found in text: " + texts.size() + " times.");
			
			//write text(s) to file
			for (String t : texts){
				//System.out.print("\r" + t);
				if (t.length() < 5) continue;
				sb.append(t + "\n");
				occCount++;
			}
		}
		//write file
		FileWriter fw = new FileWriter(new File(
				outputDir.getAbsolutePath() + File.separator
				+ word.replaceAll("\\W", "") + ".txt"));
		fw.write(sb.toString());
		fw.close();
		System.out.println("(found: " + occCount + " times)");
	}

	private String runNaiveAnalysis(String corpusPath) {
		// create output file
		File resultsDir = new File(RESULTS_DIR_PATH);
		if (!resultsDir.exists())
			resultsDir.mkdirs();

		StringBuilder sb = new StringBuilder();
		// results file header
		sb.append(buildResultsHeader());
		sb.append("\n\n\nKollokationen: Analyse durch naive Methode\n"
				+ "Wert = Anteil der Fundstellen, in deren Kontext das Wort vorkommt\n"
				+ OUTPUT_SECTION_SEPARATOR + "\n");

		// create results map
		Map<String, Integer> results = new HashMap<String, Integer>();

		// analyze
		List<File> corpusFiles = IO.getAllFiles(corpusPath, null);
		ProgressFeedback pf = new ProgressFeedback("Naive Analysis", corpusFiles.size());
		int occCount = 0;
		for (File f : corpusFiles) {
			String content = IO.readFile(f.getAbsolutePath());
			//occCount += content.split("\n").length;
			for (String text : content.split("\n")){
				if (!text.contains(word1)) continue;
				for (String token : text.split("\\P{L}+")) {
					if (token.length() < 3) continue;
					if (token.contains(word1)){
						occCount++;
						continue;
					}
					addToCountMap(results, token);
				}
			}
			pf.step();
		}
		pf.end();
		
		results = sortByValue(results, false);
		// delete all but 20 most frequent
		int count = 0;
		for (Entry<String, Integer> e : results.entrySet()) {
			if (count <= 20) {
				sb.append(e.getKey() + "\t" + ((double) e.getValue() / (double) occCount) + "\n");
			}
			count++;
		}
		
		//compunds
		if (substrings){
			sb.append("\n\nGefundene (und berücksichtigte)\nKomposita und deren Häufigkeit\n" 
					+ OUTPUT_SECTION_SEPARATOR + "\n");
			compounds = sortByValue(compounds, false);
			for (Entry<String, Integer> e : compounds.entrySet()) {
				sb.append(e.getKey() + "\t" + e.getValue() + "\n");
			} 
		}

		return sb.toString();
	}

	private String runVectorAnalysis(String wordSpacePath) {
		File resultsDir = new File(RESULTS_DIR_PATH);
		if (!resultsDir.exists())
			resultsDir.mkdirs();
		StringBuilder sb = new StringBuilder();

		// results file header
		// sb.append("Analyse-Parameter:\nWort: " + word1 + "\nDB-Query: " +
		// dbQuery + "\nQuelle: " + source
		// + "\nJahr von: " + (yearFrom == -1 ? "alle" : yearFrom) + "\nJahr
		// bis: "
		// + (yearTo == -1 ? "alle" : yearTo) + "\nWortkontext: " +
		// contextWordsLeftRight * 2 + " Wörter"
		// + "\nn häufigsten Wörter verwenden: " + numberFeatureWords);

		// run DISCO -bn
		// sb.append("\n\n\nAnalyse durch DISCO\n============"
		// + "\n\nSemantisch ähnlich zu " + word1 + ":\n");
		// sb.append(
		// JarExec.runJar(DISCO_JAR_PATH, DEFAULT_WORKING_DIR,
		// new String[] { wordSpacePath, "-bn", word1, "20" },
		// JVM_MEMORY_DISCO_MB, false));

		// run DISCO -bc
		sb.append("\n\nSignifikanteste Kollokationen zu " + word1 + ":\n" + OUTPUT_SECTION_SEPARATOR + "\n");
		sb.append(JarExec.runJar(DISCO_JAR_PATH, DEFAULT_WORKING_DIR,
				new String[] { wordSpacePath, "-bc", word1, "20" }, JVM_MEMORY_DISCO_MB, false));

		// run DISCO -f
		sb.append("\n\nKorpus-Häufigkeit von " + word1 + ":\n");
		sb.append(JarExec.runJar(DISCO_JAR_PATH, DEFAULT_WORKING_DIR, new String[] { wordSpacePath, "-f", word1 },
				JVM_MEMORY_DISCO_MB, false));
		
		//stop here if word2 is null
		if (word2 == null) return sb.toString();
		
		// run DISCO -s
		sb.append("\n\nSemantische Ähnlichkeit erster Ordnung von \""
		+ word1 + "\" und \"" + word2 + "\":\n");
		sb.append(
		JarExec.runJar(DISCO_JAR_PATH, DEFAULT_WORKING_DIR,
		new String[] { wordSpacePath, "-s", word1, word2, "COSINE"},
		JVM_MEMORY_DISCO_MB, false));
		
		// run DISCO -s2
//		sb.append("\n\nSemantische Ähnlichkeit zweiter Ordnung von \""
//		+ word1 + "\" und \"" + word2 + "\":\n");
//		sb.append(
//		JarExec.runJar(DISCO_JAR_PATH, DEFAULT_WORKING_DIR,
//		new String[] { wordSpacePath, "-s2", word1, word2},
//		JVM_MEMORY_DISCO_MB, false));
		
		// run DISCO -cc
		sb.append("\n\nGemeinsamer Kontext von \""
		+ word1 + "\" und \"" + word2 + "\":\n");
		sb.append(
		JarExec.runJar(DISCO_JAR_PATH, DEFAULT_WORKING_DIR,
		new String[] { wordSpacePath, "-cc", word1, word2},
		JVM_MEMORY_DISCO_MB, false));

		// run DISCO -f
		// sb.append("\n\nAnzahl der abgefragten Wörter:\n");
		// sb.append(
		// JarExec.runJar(DISCO_JAR_PATH, DEFAULT_WORKING_DIR,
		// new String[] { wordSpacePath, "-n" },
		// JVM_MEMORY_DISCO_MB, false));

		return sb.toString();
	}

	private String buildWordSpace(String corpusPath) throws IOException {
		// create temp dir
		File outputDir = new File(TEMP_WORDSPACE_PATH + runID);
		if (!outputDir.exists())
			outputDir.mkdirs();

		// prepare config file
		prepareConfig(corpusPath, outputDir.getAbsolutePath());

		// run DISCOBuilder
		JarExec.runJar(BUILDER_JAR_PATH, DEFAULT_WORKING_DIR, new String[] { BUILDER_CONFIG_PATH },
				JVM_MEMORY_DISCO_BUILDER_MB, false);

		// return path to word-space index directory
		System.out.println("Generated word-space '" + outputDir.getName() + "'. Size: "
				+ AngStringUtils.humanReadableByteCount(IO.folderSize(outputDir.toPath())));
		return outputDir.getAbsolutePath() + File.separator + "DISCO-idx";
	}

	/*
	 * construct config file for DISCOBuilder
	 */
	private void prepareConfig(String inputDir, String outputDir)
			throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = IO.getFileReader(BUILDER_CONFIG_PATH, StandardCharsets.UTF_8);

		// read and modify config
		String line;
		while ((line = br.readLine()) != null) {
			if (line.startsWith("inputDir=")) {
				line = "inputDir=" + inputDir;
			} else if (line.startsWith("outputDir=")) {
				line = "outputDir=" + outputDir;
			} else if (line.startsWith("leftContext=")) {
				line = "leftContext=" + contextWordsLeftRight;
			} else if (line.startsWith("rightContext=")) {
				line = "rightContext=" + contextWordsLeftRight;
			} else if (line.startsWith("numberFeatureWords=")) {
				line = "numberFeatureWords=" + numberFeatureWords;
			} else if (line.startsWith("stopwordFile=")) {
				line = "stopwordFile=" + new File(STOPWORD_PATH).getAbsolutePath();
			} else if (line.startsWith("dontCompute2ndOrder=")) {
				line = "dontCompute2ndOrder=true";
			}
			
			sb.append(line);
			sb.append("\n");
		}
		br.close();

		// write config
		File configFile = new File(BUILDER_CONFIG_PATH);
		configFile.delete();
		FileWriter fw = new FileWriter(configFile);
		fw.write(sb.toString());
		fw.close();
	}

	/*
	 * run id for file names and temporary directory names
	 */
	private String buildRunID() {
		return word1.replaceAll("\\W", "")
				+ (word2 != null ? "_" + word2.replaceAll("\\W", "") : "")
				//+ (dbQuery == null ? "" : "_Q-" + dbQuery)
				+ (source == null ? "" : "_" + source)
				+ (yearFrom + yearTo == -2 ? "" : "_" + yearFrom + "-" + yearTo + "") + "_" + contextWordsLeftRight
				+ "_" + numberFeatureWords + (substrings ? "_subs" : "");
	}
	
	/*
	 * construct simple regex query
	 */
	private String parseRegexQuery(String query){
		return "\\p{L}*" + query + "\\p{L}*";
	}
	
	/*
	 * turns "see this queryword here" into "see this query word here"
	 */
	private String seperateQuery(String text, String query){
		String out = text.replaceAll(query, " " + query + " ")
				.replaceAll("  ", " ").trim();
		if (!text.equals(out)){
			String[] comps = AngStringUtils.findIn("\\p{L}*" + query + "\\p{L}*", text);
			if (comps.length > 0 && !comps[0].equals(query))
				addToCountMap(compounds, comps[0]);
		}
		return out;
	}
	
	/*
	 * sort a map by value
	 */
	private <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map, final boolean ascending) {
		List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			@Override
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				if (ascending)
					return (o1.getValue()).compareTo(o2.getValue());
				else
					return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
	
	/*
	 * add to Map<String, Integer>, only keep and count unique entries
	 */
	private void addToCountMap(Map<String, Integer> map, String s){
		if (map.containsKey(s)) {
			map.put(s, map.get(s) + 1);
		} else {
			map.put(s, 1);
		}
	}
	
	/*
	 * remove these tokens
	 */
	private String removeTokens(Set<String> tokens, String from){
		for (String t : tokens){
			from = from.replaceAll("\\b" + t + "\\b", "");
		}
		return from.replaceAll("\\P{L}+", " ").trim();
	}
	
	
	private String buildResultsHeader(){
		return "Analyse-Parameter:"
				+ "\nWort1: " + word1
				+ "\nWort2: " + (word2 == null ? "-" : word2)
				+ "\nDB-Query1: " + (word1 != null ? word1 : "-")
				+ "\nDB-Query2: " + (word2 != null ? word2 : "-")
				+ "\nQuelle: " + (source != null ? source : "alle")
				+ "\nWort auch in Komposita suchen: " + (substrings ? "Ja" : "Nein")
				+ "\nJahr von: " + (yearFrom == -1 ? "alle" : yearFrom)
				+ "\nJahr bis: " + (yearTo == -1 ? "alle" : yearTo)
				+ "\nWortkontext: " + contextWordsLeftRight * 2 + " Wörter"
				+ "\nStopwörter: " + (useStopWords ? "ja" : "nein");
	}

}
