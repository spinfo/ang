package ang.analyzer;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
import de.uni_koeln.spinfo.ang.utils.MongoWrapper;
import de.uni_koeln.spinfo.ang.utils.ProgressFeedback;


public class Analyzer {

	private static final String SEP = File.separator;
	private static final String RESULTS_DIR_PATH = "data" + SEP + "results";
	private static final String STOPWORDS_PATH = "data" + SEP + "stopwords" + SEP + "stopwords-de.txt";
	private static final String CACHE_DIR = "data" + SEP + "cache";
	private static final String OUTPUT_SECTION_SEPARATOR = "====================================";

	private MongoWrapper mongo;
	private File cacheDir = new File(CACHE_DIR);
	

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

	public String runAnalysis(AnalysisProfile profile) throws IOException {
		//print analysis params
		System.out.println(profile.addToResults(buildResultsHeader(profile)));

		// init monoDB connection
		System.out.println("[INFO]\tInitializing database connection...");
		if (this.mongo == null) init();

		System.out.println("[INFO]\tRunID for this analysis is " + profile.getID());

		// build corpus
		System.out.println("[INFO]\tBuilding corpus...");
		buildCorpus(profile);
		
		// run analysis
		System.out.println("[INFO]\tRunning analysis...");
		for (String t : profile.getTerms())
			profile.addToResults(runNaiveAnalysis(profile, t));
		
		//cleanup
		System.out.println("[INFO]\tdone.\n");
		if (this.mongo != null)
			this.mongo.close();
		this.mongo = null;

		// write results to file
		String resultsPath = RESULTS_DIR_PATH + SEP + profile.getID() + ".txt";
		IO.writeStringToFile(profile.getResults(), resultsPath);
		//return path to results file
		return resultsPath;
	}

	private void buildCorpus(AnalysisProfile profile) throws IOException {
		// prepare stopwords
		if (profile.usesStopwords()){
			System.out.print("[INFO] building stopword list... ");
			profile.addStopWords(Arrays.asList(IO.readFile(STOPWORDS_PATH)
					.toUpperCase().split("\\P{L}+")));
			System.out.println("(" + profile.getStopWords().size() + " words)");
		}
		if (profile.getSources() != null){
			for (String s : profile.getSources())
				profile.addStopWord(s.toUpperCase());
		}
		
		//build corpus
		int countCache = 0;
		int countFresh = 0;
		for (String t : profile.getTerms()){
			System.out.print("[INFO] generating corpus for \"" + t + "\"... ");
			File corpusFile;
			//check cache directory for existing corpus file
			if ((corpusFile = checkForExistingCorpusFile(profile.getCorpusFileIDFor(t))) != null){
				System.out.println("[INFO]\tfound corpus file in cache: " + corpusFile.getName());
				countCache++;
				continue;
			} else {
				corpusFile = new File(CACHE_DIR + SEP + profile.getCorpusFileIDFor(t) + ".txt");
				IO.writeStringToFile(buildCorpus(t, profile), corpusFile.getAbsolutePath());
				countFresh++;
			}
			profile.addCorpusFile(corpusFile);
		}

		System.out.println("[INFO] corpora complete. reused form cache: "
				+ countCache + ". generated: " + countFresh + ".");
	}
	
	private String buildCorpus(String term, AnalysisProfile profile) throws IOException{
		// query db for data
		FindIterable<Document> results;
		if (profile.usesStopwords()){
			results = mongo.getSearchResults(
					parseRegexQuery(term.toLowerCase()), profile.getSources(), false, true,
					(profile.getYearFrom() > -1 && profile.getYearTo() > -1), profile.getYearFrom(), profile.getYearTo(), 25000);
		} else {
			results = mongo.getSearchResults(
					term, profile.getSources(), profile.getYearFrom(), profile.getYearTo(), false);
		}
		
		//create compounds map
		Map<String, Integer> compounds = new HashMap<String, Integer>();
		
		//create corpus content
		StringBuilder sb = new StringBuilder();
		int occCount = 0;
		for (Document doc : results) {
			String text = doc.getString("text").toUpperCase();
			text = text.replaceAll("\\-", " "); //remove hyphens
			if (!profile.usesCompounds() && !text.matches(".*\\b" + term + "\\b.*")) continue; //ignore if only compounds were found
			text = seperateQuery(text, term, compounds); //separate composites
			text = text.replaceAll("\\P{L}+", " ").trim();
			text = removeTokens(profile.getStopWords(), text); //remove stopwords
			
			//trim text to context windows
			List<String> texts = AngStringUtils.trimTextMulti(
					text, term, profile.getContextSize() + 1);
			
			for (String t : texts){
				if (t.length() < 5) continue;
				sb.append(t + "\n");
				occCount++;
			}
		}
		
		//sort and add compounds map to profile
		compounds = sortByValue(compounds, false);
		profile.addCompounds(term, compounds);

		System.out.println("[INFO]\tterm '" + term + "' found " + occCount + " times.");
		return sb.toString();
	}
	
	private File checkForExistingCorpusFile(String corpusFileID){
		for (File f : cacheDir.listFiles()){
			if (f.getName().equals(corpusFileID + ".txt")) return f;
		}
		return null;
	}

	private String runNaiveAnalysis(AnalysisProfile profile, String term) {
		StringBuilder sb = new StringBuilder();
		sb.append("\n\nKookkurenzen von " + term + "\n"
				+ OUTPUT_SECTION_SEPARATOR + "\n");

		// create co-occurrences map
		Map<String, Integer> coOccurrences = new HashMap<String, Integer>();

		// analyze
		ProgressFeedback pf = new ProgressFeedback("Naive Analysis", profile.getCorpusFiles().size());
		int occCount = 0;
		for (File f : profile.getCorpusFiles()) {
			String content = IO.readFile(f.getAbsolutePath());
			//occCount += content.split("\n").length;
			for (String text : content.split("\n")){
				if (!text.contains(term)) continue;
				for (String token : text.split("\\P{L}+")) {
					if (token.length() < 3) continue;
					if (token.contains(term)){
						occCount++;
						continue;
					}
					addToCountMap(coOccurrences, token);
				}
			}
			pf.step();
		}
		pf.end();
		
		//sort co-occurrence map
		coOccurrences = sortByValue(coOccurrences, false);
		
		// delete all but 20 most frequent
		int count = 0;
		for (Entry<String, Integer> e : coOccurrences.entrySet()) {
			if (count <= 20) {
				sb.append(e.getKey() + "\t" + ((double) e.getValue() / (double) occCount) + "\n");
				count++;
			} else {
				coOccurrences.remove(e.getKey());
			}
		}
		
		//add co-occurrences to profile
		profile.addCoOccurrences(term, coOccurrences);
		
		//compunds
		if (profile.usesCompounds()){
			sb.append("\n\nGefundene Komposita von " + term + " und deren Häufigkeit:\n" 
					+ OUTPUT_SECTION_SEPARATOR + "\n");
			for (Entry<String, Integer> e : profile.getCompounds(term).entrySet()) {
				sb.append(e.getKey() + "\t" + e.getValue() + "\n");
			}
		}
		
		//term occurences
		sb.append("\nKorpushäufigkeit von " + term + ": " + occCount
				+ OUTPUT_SECTION_SEPARATOR + "\n");

		return sb.toString();
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
	private String seperateQuery(String text, String query, Map<String, Integer> compounds){
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
	
	private String buildResultsHeader(AnalysisProfile profile){
		StringBuilder sb = new StringBuilder();
		sb.append(OUTPUT_SECTION_SEPARATOR);
		sb.append("\nAnalyse-Parameter:\nTerme: ");
		for (String t : profile.getTerms()) sb.append(t + " ");
		sb.append("\nQuellen: ");
		for (String s : profile.getSources()) sb.append(s + " ");
		sb.append("\nWort auch in Komposita suchen: " + (profile.usesCompounds() ? "Ja" : "Nein"));
		sb.append("\nJahr von: " + (profile.getYearFrom() == -1 ? "alle" : profile.getYearFrom()));
		sb.append("\nJahr bis: " + (profile.getYearTo() == -1 ? "alle" : profile.getYearTo()));
		sb.append("\nWortkontext: " + profile.getContextSize() + " Wörter");
		sb.append("\nStopwörter: " + (profile.usesStopwords() ? "ja" : "nein"));
		return sb.toString() + "\n" + OUTPUT_SECTION_SEPARATOR + "\n";
	}

}
