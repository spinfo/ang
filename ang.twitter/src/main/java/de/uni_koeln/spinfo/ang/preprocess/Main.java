package de.uni_koeln.spinfo.ang.preprocess;

import java.io.File;
import java.util.Set;

import de.uni_koeln.spinfo.ang.benchmark.BenchmarkData;
import de.uni_koeln.spinfo.ang.benchmark.SimpleBenchmark;
import de.uni_koeln.spinfo.ang.util.IO;
import de.uni_koeln.spinfo.ang.util.MongoWrapper;

public class Main {

	public static void main(String[] args) {
		if (args.length < 1){
			System.err.println("[USAGE]\tthis program expects 1 parameter (path to a directory)");
			return;
		}
		
		SimpleBenchmark bMark = new SimpleBenchmark();
		bMark.startNewBenchmark("processing " + args[0]);
		Set<File> files = IO.getAllFiles(args[0], Patterns.TWITTER_JSON_FILES);
		
		MongoWrapper mongo = new MongoWrapper();
		mongo.init("", "", "ang", "localhost", "27017", "angdata");
		TwitterPreProcessor proc = new TwitterPreProcessor(mongo);
		int resultsCount = 0;
		
		for (File f : files){
			System.out.println("\n===== " + f.getName() + " =====");
			BenchmarkData subBMarkData = proc.process(f);
			System.out.println(subBMarkData != null ? subBMarkData : "[ERROR]\t" + f.getAbsolutePath());
			resultsCount += subBMarkData.getMarkerCount();
			bMark.newMarker();
		}
		
		mongo.close();
		
		BenchmarkData fullBMarkData = bMark.stopBenchMark();
		System.out.println("\n\n===== RESULTS =====\n"
				+ "processed files:\t" + fullBMarkData.getMarkerCount() + "\n"
				+ "extracted objects:\t" + resultsCount + "\n"
				+ "processing time:\t" + fullBMarkData.getRecordedTimeAsString() + "\n");
	}

}
