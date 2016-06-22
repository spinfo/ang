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
	private static final int JVM_MEMORY_DISCO_BUILDER_MB = 1024;
	private static final int JVM_MEMORY_DISCO_MB = 256;

	private MongoWrapper mongo;

	private String word;
	private String dbQuery;
	private String source;
	private int yearFrom;
	private int yearTo;
	private int contextWordsLeftRight;
	private int numberFeatureWords;
	private String runID;

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
			int contextWordsLeftRight, int numberFeatureWords, boolean keepWordSpace) throws IOException {

		// init fields
		this.word = word.toUpperCase();
		this.dbQuery = (dbQuery != null ? dbQuery.toUpperCase() : null);
		this.source = source;
		this.yearFrom = yearFrom;
		this.yearTo = yearTo;
		this.contextWordsLeftRight = contextWordsLeftRight;
		this.numberFeatureWords = numberFeatureWords;
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
			corpusPath = buildCorpus(runID, word, contextWordsLeftRight, dbQuery, source, yearFrom, yearTo);

			if (IO.folderSize(new File(corpusPath).toPath()) <= MAX_CORPUS_SIZE_FOR_ANALYSIS) {
				// build word-space with DISCOBuilder
				System.out.println("Building word-space...");
				wordSpacePath = buildWordSpace(runID, corpusPath, contextWordsLeftRight, numberFeatureWords);
			}
		}

		System.out.println("Running naive analysis...");
		// run naive analysis
		String analysisResults = runNaiveAnalysis(runID, word, dbQuery, source, yearFrom, yearTo, corpusPath,
				contextWordsLeftRight, STOPWORD_PATH);

		if (wordSpacePath != null && new File(wordSpacePath).exists()) {
			System.out.println("Running DISCO analysis...");
			// run vector analysis via DISCO
			analysisResults += runVectorAnalysis(runID, word, dbQuery, source, yearFrom, yearTo, wordSpacePath,
					contextWordsLeftRight, numberFeatureWords);
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

	private String buildCorpus(String runID, String word1, int context, String dbQuery, String source, int yearFrom,
			int yearTo) throws IOException {

		// create temporary corpus output directory
		File outputDir = new File(TEMP_CORPUS_PATH + runID);
		outputDir.mkdirs();

		// query db for data
		FindIterable<Document> results = mongo.getSearchResults(dbQuery, source, yearFrom, yearTo, false);

		// write temporary corpus

		for (Document doc : results) {
			String text = doc.getString("text");
			text = text.replaceAll("\\P{L}+", " ").toUpperCase();
			if (dbQuery != null)
				text = AngStringUtils.trimText(text, word1, context + 5);
			if (text.length() < 5)
				continue;
			// write file
			FileWriter fw = new FileWriter(new File(
					outputDir.getAbsolutePath() + File.separator + System.currentTimeMillis() + text.hashCode()));
			fw.write(text);
			fw.close();
		}

		System.out.println("Generated corpus '" + outputDir.getName() + "'. Size: "
				+ AngStringUtils.humanReadableByteCount(IO.folderSize(outputDir.toPath())));
		return outputDir.getAbsolutePath();
		// return new File("DISCO/test-corpus-dewac").getAbsolutePath();
	}

	private String runNaiveAnalysis(String runID, String word1, String dbQuery, String source, int yearFrom, int yearTo,
			String corpusPath, int contextWordsLeftRight, String stopWordsFile) {

		// create output file
		File resultsDir = new File(RESULTS_DIR_PATH);
		if (!resultsDir.exists())
			resultsDir.mkdirs();

		StringBuilder sb = new StringBuilder();
		// results file header
		sb.append("Analyse-Parameter:\nWort: " + word1 + "\nDB-Query: " + (dbQuery != null ? dbQuery : "-")
				+ "\nQuelle: " + (source != null ? source : "alle") + "\nJahr von: "
				+ (yearFrom == -1 ? "alle" : yearFrom) + "\nJahr bis: " + (yearTo == -1 ? "alle" : yearTo)
				+ "\nWortkontext: " + contextWordsLeftRight * 2 + " Wörter");

		sb.append("\n\n\nAnalyse durch naive Methode\n" + "(häufigste Wörter im Umfeld, Stopwörter gefiltert)\n"
				+ "===========================\n");

		// get stopwords
		Set<String> stopWords = new HashSet<String>(
				Arrays.asList(IO.readFile(stopWordsFile).toUpperCase().split("\\P{L}+")));

		// create results map
		Map<String, Integer> results = new HashMap<String, Integer>();

		// analyze
		List<File> corpusFiles = IO.getAllFiles(corpusPath, null);
		for (File f : corpusFiles) {
			for (String token : IO.readFile(f.getAbsolutePath()).split("\\P{L}+")) {
				String t = token.toUpperCase();
				if (t.length() < 3 || t.contains(word1) || stopWords.contains(t))
					continue;
				if (results.containsKey(t)) {
					results.put(t, results.get(t) + 1);
				} else {
					results.put(t, 1);
				}
			}
		}
		results = sortByValue(results, false);
		// delete all but 20 most frequent
		int count = 0;
		for (Entry<String, Integer> e : results.entrySet()) {
			if (count <= 20) {
				sb.append(e.getKey() + "\t" + ((double) e.getValue() / (double) results.size()) + "\n");
			}
			count++;
		}

		return sb.toString();
	}

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

	private String runVectorAnalysis(String runID, String word1, String dbQuery, String source, int yearFrom,
			int yearTo, String wordSpacePath, int contextWordsLeftRight, int numberFeatureWords) {

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
		sb.append("\n\nSignifikanteste Kollokationen zu " + word1 + ":\n");
		sb.append(JarExec.runJar(DISCO_JAR_PATH, DEFAULT_WORKING_DIR,
				new String[] { wordSpacePath, "-bc", word1, "20" }, JVM_MEMORY_DISCO_MB, false));

		// run DISCO -f
		sb.append("\n\nKorpus-Häufigkeit von " + word1 + ":\n");
		sb.append(JarExec.runJar(DISCO_JAR_PATH, DEFAULT_WORKING_DIR, new String[] { wordSpacePath, "-f", word1 },
				JVM_MEMORY_DISCO_MB, false));

		// run DISCO -f
		// sb.append("\n\nAnzahl der abgefragten Wörter:\n");
		// sb.append(
		// JarExec.runJar(DISCO_JAR_PATH, DEFAULT_WORKING_DIR,
		// new String[] { wordSpacePath, "-n" },
		// JVM_MEMORY_DISCO_MB, false));

		return sb.toString();
	}

	private String buildWordSpace(String runID, String corpusPath, int contextWordsLeftRight, int numberFeatureWords)
			throws IOException {

		// create temp dir
		File outputDir = new File(TEMP_WORDSPACE_PATH + runID);
		if (!outputDir.exists())
			outputDir.mkdirs();

		// prepare config file
		prepareConfig(corpusPath, outputDir.getAbsolutePath(), contextWordsLeftRight, numberFeatureWords);

		// run DISCOBuilder
		JarExec.runJar(BUILDER_JAR_PATH, DEFAULT_WORKING_DIR, new String[] { BUILDER_CONFIG_PATH },
				JVM_MEMORY_DISCO_BUILDER_MB, false);

		// return path to word-space index directory
		System.out.println("Generated word-space '" + outputDir.getName() + "'. Size: "
				+ AngStringUtils.humanReadableByteCount(IO.folderSize(outputDir.toPath())));
		return outputDir.getAbsolutePath() + File.separator + "DISCO-idx";
	}

	private void prepareConfig(String inputDir, String outputDir, int contextWordsLeftRight, int numberFeatureWords)
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

	private String buildRunID() {
		return word + (dbQuery == null ? "" : "_Q-" + dbQuery) + (source == null ? "" : "_" + source)
				+ (yearFrom + yearTo == -2 ? "" : "_[" + yearFrom + "-" + yearTo + "]") + "_" + contextWordsLeftRight
				+ "_" + numberFeatureWords;
	}

}
