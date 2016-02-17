package de.uni_koeln.spinfo.ang.preprocess;

import de.uni_koeln.spinfo.ang.benchmark.BenchmarkData;

public class Main {

	public static void main(String[] args) {
		TwitterPreProcessor proc = new TwitterPreProcessor();
		BenchmarkData bMarkData = proc.process("/Users/bkiss/Documents/testdata/test.json");
		System.out.println(bMarkData);
	}

}
