package de.uni_koeln.spinfo.ang.langdetect;

import org.apache.tika.language.LanguageIdentifier;

public class TikaGermanDetector implements IGermanDetector{
	
	@Override
	public boolean isGerman(String text) {
		LanguageIdentifier identifier = new LanguageIdentifier(text);
		return identifier.getLanguage().equals("de");
	}

}
