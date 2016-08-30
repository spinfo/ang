package ang.analyzer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AnalysisProfile {

	private String id;
	private Map<String, Integer> terms;
	private List<String> sources;
	private Set<String> stopWords;
	private Map<String, Map<String, Integer>> coOccurences;
	private Map<String, Map<String, Integer>> compounds;
	private int yearFrom;
	private int yearTo;
	private int contextSize;
	private boolean useCompounds;
	private boolean useStopwords;
	private boolean useOnlyContexts;
	
	private List<File> corpus;
	private StringBuilder results;
	
	
	public AnalysisProfile(){
		super();
		this.terms = new HashMap<String, Integer>();
		this.sources = new ArrayList<String>();
		this.stopWords = new HashSet<String>();
		this.yearFrom = -1;
		this.yearTo = -1;
		this.contextSize = 5;
		this.useCompounds = false;
		this.useStopwords = true;
		this.useOnlyContexts = true;
		this.results = new StringBuilder();
		this.corpus = new ArrayList<File>();
	}

	public AnalysisProfile(
			String[] terms,
			String[] sources,
			int yearFrom,
			int yearTo,
			int contextSize,
			boolean useCompounds,
			boolean useStopwords,
			boolean useOnlyContexts) {
		this();
		setTerms(terms);
		setSources(sources);
		this.yearFrom = yearFrom;
		this.yearTo = yearTo;
		this.contextSize = contextSize;
		this.useCompounds = useCompounds;
		this.useStopwords = useStopwords;
		this.useOnlyContexts = useOnlyContexts;
	}
	
	private void generateID(){
		StringBuilder sb = new StringBuilder();
		for (String t : terms.keySet())
			sb.append(t.replaceAll("\\W", "")
					.substring(0, Math.min(4, t.length())) + "-");
		for (String s : sources)
			sb.append(s.replaceAll("\\W", "")
					.substring(0, Math.min(4, s.length())) + "-");
		sb.deleteCharAt(sb.length()-1);
		if (yearFrom > -1) sb.append("_" + yearFrom);
		if (yearTo > -1) sb.append("_" + yearTo);
		if (useCompounds) sb.append("_COMP");
		if (useStopwords) sb.append("_STOP");
		sb.append("_CNT" + contextSize);
		sb.append("_" + System.currentTimeMillis());
		this.id = sb.toString();
	}
	
	public String addToResults(String resultsData){
		results.append(resultsData);
		return resultsData;
	}
	
	public String getResults(){
		return results.toString();
	}
	
	public String getID(){
		generateID();
		return id;
	}
	
	public void addCorpusFile(File corpusFile){
		corpus.add(corpusFile);
	}
	
	public List<File> getCorpusFiles(){
		return corpus;
	}
	
	public void addStopWord(String stopWord){
		stopWords.add(stopWord);
	}
	
	public void addStopWords(List<String> stopWords){
		for (String s : stopWords) this.stopWords.add(s);
	}
	
	public Set<String> getStopWords(){
		return stopWords;
	}
	
	public String getCorpusFileIDFor(String term){
		if (terms.get(term.toUpperCase()) == null){
			System.err.println("[ERROR]\tterm \"" + term + "\" not in profiles terms list!");
			return null;
		} else {
			String src = "";
			for (String s : sources) src += "_" + s;
			return term.toUpperCase()
					.replaceAll("Ü", "UE")
					.replaceAll("Ä", "AE")
					.replaceAll("Ö", "OE")
					.replaceAll("ß", "SS") + src
					+ (yearFrom != -1 ? "_" + yearFrom : "")
					+ (yearTo != -1 ? "_" + yearTo : "")
					+ "_" + contextSize;
		}
	}

	public Map<String, Integer> getTermsMap() {
		return terms;
	}
	
	public Set<String> getTermsSet() {
		return terms.keySet();
	}

	public void setTerms(String[] terms) {
		this.terms.clear();
		if (terms == null) return;
		for (int i = 0; i < terms.length; i++)
			this.terms.put(terms[i].toUpperCase(), 0);
	}
	
	public void increaseTermCount(String term, int amount){
		terms.put(term, terms.get(term) + amount);
	}

	public List<String> getSources() {
		return sources;
	}

	public void setSources(String[] sources) {
		this.sources.clear();
		if (sources == null) return;
		for (int i = 0; i < sources.length; i++)
			this.sources.add(sources[i].toLowerCase());
	}

	public int getYearFrom() {
		return yearFrom;
	}

	public void setYearFrom(int yearFrom) {
		this.yearFrom = yearFrom;
	}

	public int getYearTo() {
		return yearTo;
	}

	public void setYearTo(int yearTo) {
		this.yearTo = yearTo;
	}

	public int getContextSize() {
		return contextSize;
	}

	public void setContextSize(int contextSize) {
		this.contextSize = contextSize;
	}

	public boolean usesCompounds() {
		return useCompounds;
	}

	public void setUseCompounds(boolean useCompounds) {
		this.useCompounds = useCompounds;
	}

	public boolean usesStopwords() {
		return useStopwords;
	}

	public void setUseStopwords(boolean useStopwords) {
		this.useStopwords = useStopwords;
	}

	public boolean usesOnlyContexts() {
		return useOnlyContexts;
	}

	public void setUseOnlyContexts(boolean useOnlyContexts) {
		this.useOnlyContexts = useOnlyContexts;
	}
	
	public void addCoOccurrences(String forTerm, Map<String, Integer> coOccurrences){
		if (this.coOccurences == null)
			this.coOccurences = new HashMap<String, Map<String, Integer>>();
		this.coOccurences.put(forTerm, coOccurrences);
	}
	
	public Map<String, Integer> getCoOccurrences(String forTerm){
		return coOccurences.get(forTerm);
	}
	
	public void addCompounds(String forTerm, Map<String, Integer> Compounds) {
		if (this.compounds == null)
			this.compounds = new HashMap<String, Map<String, Integer>>();
		this.compounds.put(forTerm, Compounds);
	}

	public Map<String, Integer> getCompounds(String forTerm) {
		return compounds.get(forTerm);
	}

}
