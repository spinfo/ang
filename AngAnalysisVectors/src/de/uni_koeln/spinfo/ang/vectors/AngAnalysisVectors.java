package de.uni_koeln.spinfo.ang.vectors;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AngAnalysisVectors {
	
	
	public void analyze(File matrixCSV, String delimiter, boolean header){
		StringBuilder sb = new StringBuilder();
		Map<String, Double[]> vectors = null;
		//Map<String, Integer> absoluteCount = null;
		
		//read matrix csv	
		System.out.println("Reading matrix CSV");
		try {
			vectors = extractVectorsFromCSV(readFile(matrixCSV), delimiter, header);
			//absoluteCount = readAbsoluteCount(absoluteCountFile);
			//vectors = normalizeVectors(vectors, absoluteCount);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Extracted " + vectors.size() + " vectors");
		System.out.println("Checking vector length consistency");
		checkVectorData(vectors);
		
		//TEMP
//		int c;
//		List<Integer> inds = new ArrayList<Integer>();
//		for (int i = 0; i < 1574; i++) {
//			c = 0;
//			for (String k : vectors.keySet())
//				if (vectors.get(k)[i] > 0)
//					c++;
//			if (c <= 1)
//				inds.add(i);
//		}
//		for (String k : vectors.keySet())
//			for (int j = inds.size() - 1; j >= 0; j--)
//				vectors.put(k, removeElement(vectors.get(k), j));
//		System.out.println("REMOVED " + inds.size() + " COOCS!!!");
		
		//write distance matrix to file
		System.out.println("generating distance matrix");
		Map<String, Map<String, Double>> distances = null;
		try {
			distances = generateDistanceMatrix(vectors, delimiter);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//write results meta data
		System.out.println("Writing analysis meta data");
		sb.append("ANG DATA VECTOR ANALYSIS\n\n[INPUT FILE]\n");
		sb.append("Name: " + matrixCSV.getName() + "\n");
		sb.append("Size: " + humanReadableByteCount(matrixCSV.length(), true) + "\n");
		sb.append("Modified: " + new Date(matrixCSV.lastModified()) + "\n");
		sb.append("Vectors: " + vectors.size() + "\n");
		sb.append("Dimensions: " + vectors.getOrDefault(vectors.keySet().toArray()[0], new Double[0]).length + "\n\n");
		writeToFile(sb.toString(), new File("results-meta.txt"), false);
		
		//write 5 most similar file
		writeMostSimilarData(distances, 5);
	}
	
	
	public static Double[] removeElement(Double[] original, int element){
		Double[] n = new Double[original.length - 1];
	    System.arraycopy(original, 0, n, 0, element );
	    System.arraycopy(original, element+1, n, element, original.length - element-1);
	    return n;
	}
	
	
	private Map<String, Double[]> normalizeVectors(Map<String, Double[]> vectors, Map<String, Integer> absoluteCount) {
		for (String term : vectors.keySet()){
			Double[] v = vectors.get(term);
			for (int i = 0; i < v.length; i++) {
				v[i] = v[i]/(double)absoluteCount.get(term);
			}
			vectors.replace(term, v);
		}
		return vectors;
	}


	private Map<String, Integer> readAbsoluteCount(File absoluteCountFile) {
		Map<String, Integer> absoluteCount = new HashMap<String, Integer>();
		String s = null;
		
		try {
			s = readFile(absoluteCountFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for (String line : s.split("\n")){
			if (line.length() == 0) continue;
			absoluteCount.put(line.split("\t")[0], Integer.valueOf(line.split("\t")[1]));
		}
		
		return absoluteCount;
	}
	
	
	private void writeMostSimilarData(Map<String, Map<String, Double>> distances, int nMostSimilar){
		StringBuilder sb = new StringBuilder();
		int count;
		
		for (String key1 : distances.keySet()){
			sb.append("[" + key1 + "]\n");
			count = 0;
			for (String key2 : distances.get(key1).keySet()){
				sb.append(key2 + " (" + distances.get(key1).get(key2) + ")\n");
				count++;
				if (count == nMostSimilar) break;
			}
			sb.append("\n");
		}
		
		writeToFile(sb.toString(), new File("results-most-similar.txt"), false);
	}
	
	
	private Map<String, Map<String, Double>> generateDistanceMatrix(Map<String, Double[]> vectors, String delimiter) throws IOException{
		//StringBuilder sb = new StringBuilder();
		File f = new File("results-distances.csv");
		if (f.exists()){
			f.delete();
			f.createNewFile();
		}
		BufferedWriter bw = getWriter(f, true);
		Map<String, Map<String, Double>> distances = new HashMap<String, Map<String, Double>>();
		
		//header
		for (String key : vectors.keySet()){
			bw.append(delimiter + key);
		}
		bw.append("\n");
		
		int count = 0;
		
		//calc dist
		for (String key1 : vectors.keySet()){
			bw.append(key1);
			distances.put(key1, new HashMap<String, Double>());
			for (String key2 : vectors.keySet()){
				distances.get(key1).put(key2, VectorMath.cosineSimilarity(vectors.get(key1), vectors.get(key2)));
				bw.append(delimiter);
				bw.append(distances.get(key1).get(key2) + "");
			}
			distances.put(key1, sortMapByValue(distances.get(key1), false));
			bw.append("\n");
			count++;
			if (count % 1000 == 0) System.out.println(count + " / " + vectors.size());
		}
		
		System.out.println("writing distance matrix to file");
		//write file
		//writeToFile(sb.toString(), );
		bw.close();
		return distances;
	}
	
	
	private boolean checkVectorData(Map<String, Double[]> vectors){
		//check vector length
		int last = -1;
		for (String key : vectors.keySet()){
			if (last == -1){
				last = vectors.get(key).length;
			}
			if (vectors.get(key).length != last){
				System.err.println("WARNING: inconsistent vector length!");
				return false;
			}
			last = vectors.get(key).length;
		}
		System.out.println("OK: All vectors of same length: " + last);
		return true;
	}
	
	
	private Map<String, Double[]> extractVectorsFromCSV(String csv, String delimiter, boolean header){
		Map<String, Double[]> vectors = new HashMap<String, Double[]>();
		
		String[] lines = csv.split("\n");
		for (String line : lines){
			//skip header
			if (header){
				header = false;
				continue;
			}
			//extract vector values
			String[] values = line.split(delimiter);
			Double[] vector = new Double[values.length - 1];
			String term = values[0];
			for (int i = 1; i < values.length; i++) {
				vector[i-1] = Double.valueOf(values[i]);
			}
			//add vector to map
			vectors.put(term, vector);
		}
		
		return vectors;
	}
	
	
	private String readFile(File file) throws IOException{
		BufferedReader br = getReader(file);
		StringBuilder sb = new StringBuilder();
		String line;
		
		while ((line = br.readLine()) != null){
			sb.append(line);
			sb.append("\n");
		}
		br.close();
		
		return sb.toString();
	}
	
	
	private BufferedReader getReader(File f) {
		try {
			return new BufferedReader(new FileReader(f));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	
	private BufferedWriter getWriter(File f, boolean append) {
		try {
			return new BufferedWriter(new FileWriter(f, append));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public void writeToFile(String content, File file, boolean append){
		BufferedWriter bw = getWriter(file, append);
		try {
			bw.write(content);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		bw = null;
	}
	
	
	private String humanReadableByteCount(long bytes, boolean si) {
	    int unit = si ? 1000 : 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
	
	
	private <K, V extends Comparable<? super V>> Map<K, V> sortMapByValue(Map<K, V> map, final boolean ascending) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			@Override
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				if (ascending)
					return (o1.getValue()).compareTo(o2.getValue());
				else
					return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
	
}
