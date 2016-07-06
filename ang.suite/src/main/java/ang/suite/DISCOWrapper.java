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
	private static final int JVM_MEMORY_DISCO_BUILDER_MB = 2024;
	private static final int JVM_MEMORY_DISCO_MB = 256;
	private static final String OUTPUT_SECTION_SEPARATOR = "====================================";

	private MongoWrapper mongo;

	private String word;
	private String dbQuery;
	private String source;
	private int yearFrom;
	private int yearTo;
	private int contextWordsLeftRight;
	private int numberFeatureWords;
	private String runID;
	private boolean substrings;
	private Map<String, Integer> composita;
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

	public File runAnalysis(String word, String dbQuery, String source, int yearFrom, int yearTo,
			boolean substrings, int contextWordsLeftRight, int numberFeatureWords,
			boolean keepWordSpace, boolean useStopWords) throws IOException {
		// init fields
		this.word = word.toUpperCase();
		this.dbQuery = (dbQuery != null ? dbQuery.toUpperCase() : null);
		this.source = source;
		this.useStopWords = useStopWords;
		this.yearFrom = yearFrom;
		this.yearTo = yearTo;
		this.contextWordsLeftRight = contextWordsLeftRight;
		this.numberFeatureWords = numberFeatureWords;
		this.substrings = substrings;
		this.composita = new HashMap<String, Integer>();
		this.runID = buildRunID();

		System.out.println("Starting analysis for: " + word);

		// init monoDB connection
		System.out.println("Initializing database connection...");
		if (this.mongo == null)
			init();

		System.out.println("RunID for this analysis is " + runID);

		// look for existing word spaces with same criteria
		String wordSpacePath = null;
		String corpusPath = null;
		File ws = new File(TEMP_WORDSPACE_PATH + File.separator + runID);
		if (ws.exists() && ws.isDirectory()) {
			wordSpacePath = ws.getAbsolutePath();
		} else {
			// build corpus
			System.out.println("Building corpus...");
			corpusPath = buildCorpus();

			if (IO.folderSize(new File(corpusPath).toPath()) <= MAX_CORPUS_SIZE_FOR_ANALYSIS) {
				// build word-space with DISCOBuilder
				System.out.println("Building word-space...");
				wordSpacePath = buildWordSpace(corpusPath);
			}
		}

		System.out.println("Running naive analysis...");
		// run naive analysis
		String analysisResults = runNaiveAnalysis(corpusPath);

		if (wordSpacePath != null && new File(wordSpacePath).exists()) {
			System.out.println("Running DISCO analysis...");
			// run vector analysis via DISCO
			analysisResults += runVectorAnalysis(wordSpacePath);
		} else {
			System.out.println("Skipping DISCO analysis because of corpus size: "
					+ AngStringUtils.humanReadableByteCount(IO.folderSize(new File(corpusPath).toPath()))
					+ " (maximum set to " + AngStringUtils.humanReadableByteCount(MAX_CORPUS_SIZE_FOR_ANALYSIS) + ")");
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
			Arrays.asList(IO.readFile(STOPWORD_PATH)
					.toUpperCase().split("\\P{L}+"));
		}
		stopWords.add(source.toUpperCase());

		// query db for data
		FindIterable<Document> results;
		if (substrings){
			results = mongo.getSearchResults(
					parseRegexQuery(dbQuery), source, false, true,
					(yearFrom > -1 && yearTo > -1), yearFrom, yearTo, 25000);
		} else {
			results = mongo.getSearchResults(
					dbQuery, source, yearFrom, yearTo, false);
		}
		
		// write temporary corpus
		for (Document doc : results) {
			String text = doc.getString("text");
			text = text.toUpperCase();
			text = text.replaceAll("\\-", ""); //remove hyphens
			text = removeTokens(stopWords, text); //remove stopwords
			text = seperateQuery(text, dbQuery); //separate composites
			
			//trim text to context windows
			List<String> texts = AngStringUtils.trimTextMulti(
					text, word, contextWordsLeftRight + 2);
			
			//write text(s) to file
			FileWriter fw = new FileWriter(new File(
					outputDir.getAbsolutePath() + File.separator
					+ System.currentTimeMillis() + text.hashCode() + ".txt"));
			for (String t : texts){
				if (t.length() < 5) continue;
				fw.write(t + "\n");
			}
			fw.close();
		}

		System.out.println("Generated corpus '" + outputDir.getName() + "'. Size: "
				+ AngStringUtils.humanReadableByteCount(IO.folderSize(outputDir.toPath())));
		return outputDir.getAbsolutePath();
		// return new File("DISCO/test-corpus-dewac").getAbsolutePath();
	}

	private String runNaiveAnalysis(String corpusPath) {
		// create output file
		File resultsDir = new File(RESULTS_DIR_PATH);
		if (!resultsDir.exists())
			resultsDir.mkdirs();

		StringBuilder sb = new StringBuilder();
		// results file header
		sb.append(buildResultsHeader());
		sb.append("\n\n\nAnalyse durch naive Methode\n"
				+ "(häufigste Wörter im Umfeld, Stopwörter gefiltert)\n"
				+ "Wert = Anteil der Fundstellen, in deren Kontext das Wort vorkommt\n"
				+ OUTPUT_SECTION_SEPARATOR + "\n");

		// create results map
		Map<String, Integer> results = new HashMap<String, Integer>();

		// analyze
		List<File> corpusFiles = IO.getAllFiles(corpusPath, null);
		int occCount = 0;
		for (File f : corpusFiles) {
			String content = IO.readFile(f.getAbsolutePath());
			occCount += content.split("\n").length;
			for (String token : content.split("\\P{L}+")) {
				token = token.toUpperCase();
				if (token.length() < 2 || token.contains(word)
						|| (useStopWords && stopWords.contains(token)))
					continue;
				addToCountMap(results, token);
			}
		}
		results = sortByValue(results, false);
		// delete all but 20 most frequent
		int count = 0;
		for (Entry<String, Integer> e : results.entrySet()) {
			if (count <= 20) {
				sb.append(e.getKey() + "\t" + ((double) e.getValue() / (double) occCount) + "\n");
			}
			count++;
		}
		
		//composita
		if (substrings){
			sb.append("\n\nGefundene (und berücksichtigte)\nKomposita und deren Häufigkeit\n" 
					+ OUTPUT_SECTION_SEPARATOR + "\n");
			composita = sortByValue(composita, false);
			for (Entry<String, Integer> e : composita.entrySet()) {
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
		sb.append("\n\nSignifikanteste Kollokationen zu " + word + ":\n" + OUTPUT_SECTION_SEPARATOR + "\n");
		sb.append(JarExec.runJar(DISCO_JAR_PATH, DEFAULT_WORKING_DIR,
				new String[] { wordSpacePath, "-bc", word, "20" }, JVM_MEMORY_DISCO_MB, false));

		// run DISCO -f
		sb.append("\n\nKorpus-Häufigkeit von " + word + ":\n");
		sb.append(JarExec.runJar(DISCO_JAR_PATH, DEFAULT_WORKING_DIR, new String[] { wordSpacePath, "-f", word },
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
		return word
				//+ (dbQuery == null ? "" : "_Q-" + dbQuery)
				+ (source == null ? "" : "_" + source)
				+ (yearFrom + yearTo == -2 ? "" : "_[" + yearFrom + "-" + yearTo + "]") + "_" + contextWordsLeftRight
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
				addToCountMap(composita, comps[0]);
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
			from = from.replaceAll("\\b" + Pattern.quote(t) + "\\b", "");
		}
		return from.replaceAll("\\P{L}+", " ");
	}
	
	
	private String buildResultsHeader(){
		return "Analyse-Parameter:\nWort: " + word + "\nDB-Query: " + (dbQuery != null ? dbQuery : "-")
				+ "\nQuelle: " + (source != null ? source : "alle")
				+ "\nWort auch in Komposita suchen: " + (substrings ? "Ja" : "Nein")
				+ "\nJahr von: " + (yearFrom == -1 ? "alle" : yearFrom)
				+ "\nJahr bis: " + (yearTo == -1 ? "alle" : yearTo)
				+ "\nWortkontext: " + contextWordsLeftRight * 2 + " Wörter"
				+ "\nStopwörter: " + (useStopWords ? "ja" : "nein");
	}

}
