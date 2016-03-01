package de.uni_koeln.spinfo.ang.preprocess;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.uni_koeln.spinfo.ang.benchmark.BenchmarkData;
import de.uni_koeln.spinfo.ang.benchmark.SimpleBenchmark;
import de.uni_koeln.spinfo.ang.util.FormatConvert;
import de.uni_koeln.spinfo.ang.util.IO;
import de.uni_koeln.spinfo.ang.util.MongoWrapper;
import de.uni_koeln.spinfo.ang.util.Patterns;


public class Main {

	public static void main(String[] args) {
		if (args.length < 1){
			System.err.println("[USAGE]\tthis program expects 1 parameter (path to a directory)");
			return;
		}
		
		SimpleBenchmark bMark = new SimpleBenchmark();
		bMark.startNewBenchmark("processing of files in " + args[0]);
		List<File> files = IO.getAllFiles(args[0], Patterns.TWITTER_JSON_FILES);
		
		Collections.sort(files, new Comparator<File>(){
			@Override
			public int compare(File o1, File o2) {
				Long size1 = o1.length();
				return size1.compareTo(o2.length());
			}
		});
		
		MongoWrapper mongo = new MongoWrapper();
		mongo.init("", "", "ang", "localhost", "27017", "angdata");
		TwitterPreProcessor proc = new TwitterPreProcessor(mongo);
		int resultsCount = 0;
		
		for (File f : files){
			System.out.println("\n===== " + f.getName() + " ("
					+ FormatConvert.getReadableDataSize(f.length()) + ") [file "
					+ (bMark.getCurrentMarkerCount()+1)
					+ "/" + files.size() + "] =====");
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
