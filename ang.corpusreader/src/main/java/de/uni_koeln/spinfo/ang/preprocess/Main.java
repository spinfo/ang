package de.uni_koeln.spinfo.ang.preprocess;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.uni_koeln.spinfo.ang.benchmark.BenchmarkData;
import de.uni_koeln.spinfo.ang.benchmark.SimpleBenchmark;
import de.uni_koeln.spinfo.ang.utils.FormatConvert;
import de.uni_koeln.spinfo.ang.utils.IO;
import de.uni_koeln.spinfo.ang.utils.MongoWrapper;
import de.uni_koeln.spinfo.ang.utils.Patterns;


public class Main {

	public static void main(String[] args) {
		if (args.length < 1){
			System.err.println("[USAGE]\tthis program expects 1 parameter (path to a directory)");
			return;
		}
		
		TwitterPreProcessor tpp = new TwitterPreProcessor();
		tpp.process(args[0],
				Patterns.TWITTER_JSON_FILES,
				"", //db user
				"", //db pass
				"ang",
				"localhost",
				"27017",
				"angdata");
	}

}
