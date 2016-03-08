package de.uni_koeln.spinfo.ang.preprocess;

import de.uni_koeln.spinfo.ang.utils.Patterns;

public class PreProcessorApp {

	public static void main(String[] args) {
		if (args.length < 1){
			System.err.println("[USAGE]\tthis program expects 1 parameter (path to a directory)");
			return;
		}
		
		AbstractPreProcessor pp = new HamburgDTKorpusPreProcessor();
		pp.process(args[0],
				Patterns.FILE_NAME_ADD_EXTENSION + "conll", //file name pattern
				"", //db user
				"", //db pass
				"ang", //db name
				"localhost", //db host
				"27017", //db port
				"angdata"); //db collection name
	}

}
