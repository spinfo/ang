package de.uni_koeln.spinfo.ang.utils;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

public class AngParserTest {

	private static AngParser parser;

	@BeforeClass
	public static void init() {

		parser = new AngParser();

	}

	@Test
	public void testCleanAnglicisms() throws IOException {

		List<String> ang_raw = FileUtils.fileToList(FileUtils.inputPath
				+ "ang_raw.txt");

		List<String> cleaned = parser.cleanAnglicisms(ang_raw);
		Set<String> cleaned_set = new LinkedHashSet<>(cleaned);

		FileUtils.printSet(cleaned_set, FileUtils.outputPath, "ang_set_");
		FileUtils.listToTXT(cleaned, FileUtils.outputPath, "ang_list_");

	}

	

}
