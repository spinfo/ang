package de.uni_koeln.spinfo.ang.langdetect;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.cybozu.labs.langdetect.Language;

public class LangDetectGermanDetector implements IGermanDetector{
	
	public LangDetectGermanDetector() {
		super();
		
		try {
			DetectorFactory.loadProfile("profiles.sm");
		} catch (LangDetectException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public boolean isGerman(String text) {
		try {
        	Detector detector = DetectorFactory.create();
            detector.append(text);
            if (detector.detect().equals("de")){
            	for (Language lang : detector.getProbabilities()){
            		if (lang.lang.equals("de") && lang.prob > 0.99999){
            			return true;
            		}
            	}
            }
			return false;
		} catch (LangDetectException e) {
			return false;
		}
	}

}
