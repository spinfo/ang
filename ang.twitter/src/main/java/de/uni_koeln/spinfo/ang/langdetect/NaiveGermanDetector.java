package de.uni_koeln.spinfo.ang.langdetect;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import org.tartarus.snowball.ext.germanStemmer;

public class NaiveGermanDetector implements IGermanDetector{
	
	private static final double THRESHOLD = 0.45;
	
	private Set<String> stems;
	private germanStemmer stemmer;
	
	public NaiveGermanDetector() {
		stemmer = new germanStemmer();
		stems = new HashSet<String>();
		try {
			Scanner scan = new Scanner(new File("german_stems.txt"));
			while (scan.hasNextLine()){
				stems.add(scan.nextLine());
			}
			scan.close();
			System.out.println("[NAIVE] " + stems.size());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isGerman(String text) {
		String[] tokens = text.split("\\P{L}+");
		double count = 0;
		
		for (String t : tokens){
			stemmer.setCurrent(t.toLowerCase());
			stemmer.stem();
			String s = stemmer.getCurrent();
			if (stems.contains(s)){
				count++;
			}
		}
		
		return (count / (double)tokens.length) > THRESHOLD;
	}

}
