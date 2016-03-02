package de.uni_koeln.spinfo.ang.preprocess;

import de.uni_koeln.spinfo.ang.utils.Patterns;

public class TwitterPreProcessorApp {

	public static void main(String[] args) {
		if (args.length < 1){
			System.err.println("[USAGE]\tthis program expects 1 parameter (path to a directory)");
			return;
		}
		
		AbstractPreProcessor pp = new WackyPreProcessor();
		pp.process(args[0],
				Patterns.TWITTER_JSON_FILES,
				"", //db user
				"", //db pass
				"ang",
				"localhost",
				"27017",
				"angdata");
	}

}
