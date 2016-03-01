package de.uni_koeln.spinfo.ang.langdetect;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import de.uni_koeln.spinfo.ang.utils.IO;


public class StopWordsGermanDetector implements IGermanDetector {

	private Set<String> stopwords;
	
	public StopWordsGermanDetector() {
		stopwords = new HashSet<String>(
				Arrays.asList(
						IO.readFile("stopgerman.txt").split("\n")));
		
	}

	@Override
	public boolean isGerman(String text) {
		for (String s : stopwords){
			if (text.toLowerCase().matches(".*\\P{L}" + s + "\\P{L}.*")){
				return true;
			}
		}
		return false;
	}

}
