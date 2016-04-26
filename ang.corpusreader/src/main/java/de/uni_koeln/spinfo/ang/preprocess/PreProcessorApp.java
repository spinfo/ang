package de.uni_koeln.spinfo.ang.preprocess;

import de.uni_koeln.spinfo.ang.utils.Patterns;

public class PreProcessorApp {

	public static void main(String[] args) {
		if (args.length < 1){
			System.err.println("[USAGE]\tthis program expects 1 parameter (path to a directory)");
			return;
		}
		
		AbstractPreProcessor pp = new DSAStrukturPreProcessor();
		//Properties props = IO.loadProperties("db.properties", pp.getClass());
		//System.out.println(props);
		
		pp.process(args[0],
				Patterns.FILE_NAME_ADD_EXTENSION + "txt", //file name pattern
				"",	//USER
				"",		//PASS
				"ang",		//DB
				"localhost",		//HOST
				"27017",		//PORT
				"angdata");//COLLECTION
		
	}

}
