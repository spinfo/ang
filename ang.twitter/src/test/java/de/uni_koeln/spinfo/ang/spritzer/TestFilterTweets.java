package de.uni_koeln.spinfo.ang.spritzer;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.uni_koeln.spinfo.ang.proc.FilterTweets;
import de.uni_koeln.spinfo.ang.utils.FileUtils;

public class TestFilterTweets {

	private static FilterTweets extract;

	@BeforeClass
	public static void init() {

		extract = new FilterTweets();

	}

	@Ignore
	@Test
	public void testSearch() throws IOException {
	extract.findFiles(FileUtils.outputPath);
	
	}

	@Ignore
	@Test
	public void testReadJSON() throws IOException {
		String file = FileUtils.inputPath + "twitter/tweet2013010607:18.json";
		long startTime = System.nanoTime();

		extract.filterGermanTweets(file);

		long endTime = System.nanoTime();

		long duration = (endTime - startTime);

		long mill = duration / 1000000;
		long min = mill / 60000;

		System.out.println("millliseconds: " + mill);
		System.out.println("minutes: " + min);

	}

}
