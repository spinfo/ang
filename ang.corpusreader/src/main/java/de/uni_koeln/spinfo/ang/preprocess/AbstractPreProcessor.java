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


public abstract class AbstractPreProcessor {
	
	protected MongoWrapper mongo;
	protected SimpleBenchmark bMark;
	
	
	public void process(String dirPath,
			String fileNamePattern,
			String mongoUser,
			String mongoPassword,
			String mongoDatabaseName,
			String mongoHost,
			String mongoPort,
			String mongoCollection){
		
		bMark = new SimpleBenchmark();
		bMark.startNewBenchmark("processing of files in " + dirPath);
		List<File> files = IO.getAllFiles(dirPath, fileNamePattern);
		
		Collections.sort(files, new Comparator<File>(){
			@Override
			public int compare(File o1, File o2) {
				Long size1 = o1.length();
				return size1.compareTo(o2.length());
			}
		});
		
		System.out.println("\n\n==============================\n"
				+ "STARTING PROCESSING OF FILES:\n"
				+ "SRC DIR:\t" + dirPath + "\n"
				+ "PATTERN:\t" + fileNamePattern + "\n"
				+ "# FILES:\t" + files.size() + "\n"
				+ "==============================\n\n\n");
		
		//init mongo connection
		mongo = new MongoWrapper();
		mongo.init(mongoUser,
				mongoPassword,
				mongoDatabaseName,
				mongoHost,
				mongoPort,
				mongoCollection);
		
		int fileCount = 1;
		for (File f : files){
			System.out.println("[PRCSS] " + f.getName() + " ("
					+ FormatConvert.getReadableDataSize(f.length()) + ") [file "
					+ (bMark.getCurrentMarkerCount()+1)
					+ "/" + files.size() + "] ...");
			
			//build CorpusObject and add to DB
			transformCorpusObjects(f, fileCount++);
			
			bMark.newMarker();
		}
		
		mongo.close();
		
		BenchmarkData fullBMarkData = bMark.stopBenchMark();
		System.out.println("\n\n===== RESULTS =====\n"
				+ "processed files:\t" + fullBMarkData.getMarkerCount() + "\n"
				+ "processing time:\t" + fullBMarkData.getRecordedTimeAsString() + "\n");
	}
	
	
	/**
	 * Transforms input data to BSON Document instances and inserts into DB.
	 * This method must call "mongo.addDocument()" for every document created.
	 * @param inputFile
	 * @return List<Document> The resulting list of Documents
	 */
	protected abstract void transformCorpusObjects(File inputFile, int fileCount);
	
	
}
