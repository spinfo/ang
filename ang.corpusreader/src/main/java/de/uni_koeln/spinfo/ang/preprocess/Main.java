package de.uni_koeln.spinfo.ang.preprocess;

import de.uni_koeln.spinfo.ang.utils.Patterns;


public class Main {

	public static void main(String[] args) {
		if (args.length < 1){
			System.err.println("[USAGE]\tthis program expects 1 parameter (path to a directory)");
			return;
		}
		
		AbstractPreProcessor tpp = new TestPreProcessor();
		tpp.process(args[0],
				Patterns.EVERYTHING,
				"", //db user
				"", //db pass
				"ang",
				"localhost",
				"27017",
				"angdata");
	}

}
