package de.uni_koeln.spinfo.ang.utils;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
	
	
	public static List<String> trimTextMulti(String text, String around, int contextNrOfWords) {
		List<String> out = new ArrayList<String>();
		
		String[] tokens = text.split(" ");
		int min;
		int max;
		int ind = -1;
		int count = countContains(text, around);

		while (count > 0){
			for (int i = ind+1; i < tokens.length; i++) {
				if (tokens[i].contains(around)) {
					ind = i;
					min = Math.max(ind - contextNrOfWords, 0);
					max = Math.min(ind + contextNrOfWords, tokens.length);
					
					StringBuilder sb = new StringBuilder();
					for (int j = min; j < max; j++) {
						sb.append(tokens[j]);
						sb.append(" ");
					}
					out.add(sb.toString());
				}
			}
			count--;
		}

		return out;
	}
	
	
	private static int countContains(String string, String substring){
		int count = 0;
		String[] tokens = string.split(" ");
		
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].equals(substring)) count++;
		}
		
		return count;
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
