package de.uni_koeln.spinfo.ang.utils;

import java.text.Normalizer;
import java.text.Normalizer.Form;

public class AngStringUtils {
	
	
	public static String normalize(String input){
		return Normalizer.normalize(input, Form.NFC);
	}
	
	
	public static String cleanStringFromInvalidChars(String string){
		return string.replaceAll(Patterns.CONTROL_CHARS, "")
			      	 .replaceAll(Patterns.INCOMPLETE_UNICODE, "")
					 .replaceAll("\n", " ");
	}

	
}
