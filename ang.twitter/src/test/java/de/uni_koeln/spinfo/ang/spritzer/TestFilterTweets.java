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
	public void testFirstFilter() throws IOException {

		String file = FileUtils.inputPath + "twitter/tweet2013061711:58.json";
		extract.firstFilter(file, true);

	}

	@Ignore
	@Test
	public void testSecondFilter() throws IOException {

		String file = FileUtils.inputPath + "twitter/tweet2013061711:58_ff.json";
		extract.secondFilter(file);

	}

	@Ignore
	@Test
	public void testGetSome() throws IOException {
		String file = FileUtils.inputPath + "twitter/tweet2013061711:58.json";
		long startTime = System.nanoTime();

		extract.getSome(file);

		long endTime = System.nanoTime();

		long duration = (endTime - startTime);

		System.out.println("dur: " + duration);

	}

	@Ignore
	@Test
	public void testExtractSome() throws IOException {
		String file = FileUtils.inputPath + "twitter/tweet2013010607:18.json";
		long startTime = System.nanoTime();

		extract.secondFilter(file);

		long endTime = System.nanoTime();

		long duration = (endTime - startTime);

		System.out.println("dur: " + duration);

	}

}
