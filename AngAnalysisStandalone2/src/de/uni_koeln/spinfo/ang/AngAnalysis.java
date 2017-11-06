package de.uni_koeln.spinfo.ang;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class AngAnalysis {
	
	private int nMostFreqDimensions;
	private int nMostFreqTerms;
	private Timer timer;
	
	
	public void analyze(String termsPath,
						String typesPath,
						String corpusPath,
						String matrixFilePath,
						String nMostFreqDimensions,
						String nMostFreqTerms) throws IOException{
		
		System.out.println("[ ANG fixed vector space analysis ]");
		this.nMostFreqDimensions = Integer.valueOf(nMostFreqDimensions);
		this.nMostFreqTerms = Integer.valueOf(nMostFreqTerms);
		this.timer = new Timer();
		
		
		//read types
		System.out.print("reading types");
		timer.start();
		List<String> types = readTerms(typesPath, Integer.MAX_VALUE);
		System.out.println(" [" + timer.stopAndFormat() + "]");
		
		//read terms
		System.out.print("reading additional terms");
		timer.start();
		List<String> terms = readTerms(termsPath, Integer.MAX_VALUE);
		System.out.println(" [" + timer.stopAndFormat() + "]");
		
		//build vector-space dummy map
		System.out.print("building dummy vector");
		timer.start();
		Map<String, Integer> vectorDummy = new HashMap<String, Integer>();
		for (int i = 0; i < Math.min(types.size(), this.nMostFreqDimensions); i++){
			vectorDummy.put(types.get(i), 0);
		}
		System.out.println(" [" + timer.stopAndFormat() + "]");
		
		//feed terms and types into matrix map
		HashMap<String, Map<String, Integer>> matrixMap 
			= new HashMap<String, Map<String, Integer>>();
		System.out.print("adding to matrix map: " + this.nMostFreqTerms + " most frequent terms");
		timer.start();
		for (int i = 0; i < Math.min(types.size(), this.nMostFreqTerms); i++){
			matrixMap.put(types.get(i), new HashMap<String, Integer>(vectorDummy));
		}
		System.out.println(" [" + timer.stopAndFormat() + "]");
		
		System.out.print("adding to matrix map: " + terms.size() + " additional terms");
		timer.start();
		for (String t : terms){
			matrixMap.put(t, new HashMap<String, Integer>(vectorDummy));
		}
		System.out.println(" [" + timer.stopAndFormat() + "]");
		
		//cleanup
		System.out.print("performing cleanup");
		timer.start();
		terms = null;
		types = null;
		System.out.println(" [" + timer.stopAndFormat() + "]");

		//process corpus
		System.out.println("starting corpus processing...");
		timer.start();
		collectCoOccurrences(matrixMap, corpusPath);
		System.out.println("done processing corpus [" + timer.stopAndFormat() + "]");
		
		//convert sub-maps to TreeMaps for natural order
		System.out.print("converting HashMap vectors to TreeMaps for natural order");
		timer.start();
		for (String key : matrixMap.keySet()){
			matrixMap.put(key, new TreeMap<String, Integer>(matrixMap.get(key)));
		}
		System.out.println(" [" + timer.stopAndFormat() + "]");
		
		//generate CSV data
		System.out.print("generating CSV data");
		timer.start();
		String csvMatrix = coOccurrenceMapToCSV(matrixMap, ";");
		System.out.println(" [" + timer.stopAndFormat() + "]");
		
		//cleanup
		matrixMap = null;
		
		System.out.print("writing output files");
		timer.start();
		BufferedWriter bw = getWriter(new File(matrixFilePath), false);
		bw.write(csvMatrix);
		bw.close();
		System.out.println(" [" + timer.stopAndFormat() + "]");
		
		System.out.println("done.");
	}
	
//	private HashMap<String, HashMap<String, Integer>> constructCoOccurrenceMap(int nMostFreq) throws IOException{
//		HashMap<String, HashMap<String, Integer>> map = new HashMap<String, HashMap<String, Integer>>();
//		List<File> files = getFiles(new File(outputDirPath), null);
//		
//		for (File f : files){
//			BufferedReader br = getReader(f);
//			String line;
//			HashMap<String, Integer> singleTermMap = new HashMap<String, Integer>();
//			
//			while ((line = br.readLine()) != null){
//				if (line.length() < 3 || !line.contains("\t"))
//					continue;
//				String[] s = line.split("\t");
//				singleTermMap.put(s[0], Integer.valueOf(s[1]));
//			}
//			br.close();
//			
//			singleTermMap = trimMap(singleTermMap, nMostFreq);
//			map.put(f.getName(), singleTermMap);
//		}
//		
//		return map;
//	}
	
//	private void writeCoOccs(String t, Map<String, Integer> map) throws IOException {
//		File tempFile = new File(outputDirPath + "/" + t);
//		if (tempFile.exists()){
//			tempFile.delete();
//			tempFile.createNewFile();
//		}
//		BufferedWriter bw = getWriter(new File(outputDirPath + "/" + t));
//		
//		for (String key : map.keySet()){
//			bw.append(key + "\t" + map.get(key) + "\n");
//		}
//		bw.close();
//	}

	private void collectCoOccurrences(HashMap<String, Map<String, Integer>> matrixMap, String corpusPath) throws IOException{
		List<File> files = getFiles(new File(corpusPath), null);
		
		//files
		for (int i = 0; i < files.size(); i++){
			System.out.print("PROCESSING: " + files.get(i).getName() + "\t(" + (i+1) + "/" + files.size() + ")");
			timer.start();
			BufferedReader br = getReader(files.get(i));
			String line;
			//documents
			while ((line = br.readLine()) != null){
				//tokens
				Set<String> tokens = new HashSet<String>(Arrays.asList(line.split(" ")));
				for (String token : tokens){
					if (matrixMap.containsKey(token)){
						List<String[]> contexts = trimTextMulti(line, token, 5, false); //get context coOccs
						//add co-occs to matrix map
						for (String[] context : contexts){
							for (String coOcc : context){
								if (matrixMap.get(token).containsKey(coOcc)){
									matrixMap.get(token).put(coOcc, matrixMap.get(token).get(coOcc) + 1);
								}
							}
						}
					}
				}
			}
			br.close();
			System.out.println(" [" + timer.stopAndFormat() + "]");
		}
	}
	
	private List<String> readTerms(String termsPath, int nMostFreq) throws IOException{
		BufferedReader br = getReader(new File(termsPath));
		List<String> terms = new ArrayList<String>();
		
		int count = 0;
		String line;
		while ((line = br.readLine()) != null){
			terms.add(line.toUpperCase().split("\\s")[0]);
			count++;
			if (count == nMostFreq)
				break;
		}
		br.close();
		return terms;
	}
	

	private BufferedReader getReader(File f) {
		try {
			return new BufferedReader(new FileReader(f));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	private List<File> getFiles(File dir, String regex) {
		List<File> files = new ArrayList<File>(Arrays.asList(dir.listFiles()));
		Iterator<File> iter = files.iterator();

		while (iter.hasNext()) {
			File f = iter.next();
			if (regex != null && !f.getName().matches(regex)) {
				iter.remove();
			}
		}

		return files;
	}

	private BufferedWriter getWriter(File f, boolean append) {
		try {
			return new BufferedWriter(new FileWriter(f, append));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
//	private void addToSimpleCoOccMap(String term, String[] coOccs, Map<String, Integer> coOccMap){
//		for (String c : coOccs){
//			if (c.equals(term))
//				continue;
//			if (coOccMap.get(c) == null)
//				coOccMap.put(c, 0);
//			//add
//			coOccMap.put(c, coOccMap.get(c) + 1);
//		}
//	}
	
	
	private static List<String[]> trimTextMulti(String text, String around, int contextNrOfWords, boolean useSubstrings) {
		List<String[]> out = new ArrayList<String[]>();
		String[] tokens = text.replaceAll("\\P{L}", " ").replaceAll("\\s+", " ").split(" ");

		around = around.toUpperCase();
		int min;
		int max;
		int ind = -1;

		for (int i = ind + 1; i < tokens.length; i++) {
			if (useSubstrings && !tokens[i].toUpperCase().contains(around))
				continue;
			if (!useSubstrings && !tokens[i].equalsIgnoreCase(around))
				continue;
			ind = i;
			min = Math.max(ind - contextNrOfWords, 0);
			max = Math.min(ind + contextNrOfWords + 1, tokens.length);
			out.add(Arrays.copyOfRange(tokens, min, max));
		}

		return out;
	}
	
	
	private String coOccurrenceMapToCSV(HashMap<String, Map<String, Integer>> matrixMap, String delimiter){
		////construct CSV
		StringBuilder sb = new StringBuilder();
		
		//header
		for (String field : matrixMap.keySet())
			sb.append(delimiter + field);
		sb.append("\n");
		
		//data
		for (String term : matrixMap.keySet()){
			sb.append(term);
			for (String dimension : matrixMap.get(term).keySet()){
				sb.append(delimiter + matrixMap.get(term).get(dimension));
			}
			sb.append("\n");
		}
		sb.deleteCharAt(sb.length() - 1);
		//return CSV data
		return sb.toString();
	}
	
	
//	private <K, V extends Comparable<? super V>> Map<K, V> sortMapByValue(Map<K, V> map, final boolean ascending) {
//		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
//		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
//			@Override
//			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
//				if (ascending)
//					return (o1.getValue()).compareTo(o2.getValue());
//				else
//					return (o2.getValue()).compareTo(o1.getValue());
//			}
//		});
//
//		Map<K, V> result = new HashMap<K, V>();
//		for (Map.Entry<K, V> entry : list) {
//			result.put(entry.getKey(), entry.getValue());
//		}
//		return result;
//	}
	
	
//	private HashMap<String, Integer> trimMap(HashMap<String, Integer> map, int n){
//		int count = 0;
//		for (Iterator<Map.Entry<String, Integer>> it = map.entrySet().iterator(); it.hasNext(); ) {
//			it.next();
//			if (count < n) {
//				count++;
//			} else {
//				it.remove();
//			}
//		}
//		return map;
//	}
	
	
}
