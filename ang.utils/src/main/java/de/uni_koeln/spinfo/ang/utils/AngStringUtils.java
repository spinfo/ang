package de.uni_koeln.spinfo.ang.utils;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AngStringUtils {
	
	
	public static String normalize(String input){
		return Normalizer.normalize(input, Form.NFC);
	}
	
	
	public static String cleanStringFromInvalidChars(String string){
		return string.replaceAll(Patterns.CONTROL_CHARS, "")
			      	 .replaceAll(Patterns.INCOMPLETE_UNICODE, "")
					 .replaceAll("\n", " ");
	}
	
	
	public static int generateID(Object seed){
		int idHash = seed.hashCode();
		return idHash > 0 ? idHash : idHash*-1;
	}
	
	
	public static String trimText(String text, String around, int contextNrOfWords) {
		String[] tokens = text.split(" ");
		int min = -1;
		int max = -1;

		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].contains(around)) {
				if (min == -1)
					min = i;
				max = i;
			}
		}

		min = min - contextNrOfWords < 0 ? 0 : min - contextNrOfWords;
		max = max + contextNrOfWords > tokens.length ? tokens.length : max + contextNrOfWords;

		StringBuilder sb = new StringBuilder();
		for (int i = min; i < max; i++) {
			sb.append(tokens[i]);
			sb.append(" ");
		}

		return sb.toString();
	}
	
	
	public static String humanReadableByteCount(long bytes) {
	    int unit = 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = "KMGTPE".charAt(exp-1) + "i";
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
	
	
	public static String[] findIn(String findPattern, String inString){
		Pattern pattern = Pattern.compile(findPattern);
		Matcher matcher = pattern.matcher(inString);
		String[] collect = new String[0];
		
		while (matcher.find()){
			collect = Arrays.copyOf(collect, collect.length + 1);
			collect[collect.length - 1] = matcher.group(0);
		}
		
		return collect;
	}

	
}
