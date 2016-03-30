package de.uni_koeln.spinfo.ang.preprocess;

import java.util.Properties;

import de.uni_koeln.spinfo.ang.utils.IO;

public class PreProcessorApp {

	public static void main(String[] args) {
		if (args.length < 1){
			System.err.println("[USAGE]\tthis program expects 1 parameter (path to a directory)");
			return;
		}
		
		AbstractPreProcessor pp = new WackyPreProcessor();
		Properties props = IO.loadProperties("db.properties", pp.getClass());
		System.out.println(props);
		
		pp.process(args[0],
				"dewac_preproc", //file name pattern
				props.getProperty("user"),	//USER
				props.getProperty("pw"),		//PASS
				props.getProperty("db"),		//DB
				props.getProperty("host"),		//HOST
				props.getProperty("port"),		//PORT
				props.getProperty("collection"));//COLLECTION
		
	}

}
