package ang.cse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Ignore;
import org.junit.Test;

import com.google.api.services.customsearch.model.Result;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import de.uni_koeln.spinfo.ang.utils.FileUtils;

public class CSETest

{

	@Ignore
	@Test
	public void testSearch() throws IOException {

		String angList = FileUtils.inputPath + "20151214_neologismen.txt";
		ArrayList<String> inputList = (ArrayList<String>) FileUtils.fileToList(angList);

		CSE search_engine = new CSE();

		for (String s : inputList) {

			// trim string
			s = s.trim();

			// search
			List<Result> results = search_engine.search(s);

			if (results != null) {

				

				ArrayList<String> pretty = new ArrayList<>();

				for (Result r : results) {

					pretty.add(r.toPrettyString());

				}

				String fileName = s.replaceAll("\\s+", "_");
				// print results
				FileUtils.printList(pretty, FileUtils.outputPath + "google/20151216/", "20151216_" + fileName, ".json");

			} else {

				System.out.println(s);
			}

		}

	}


	@Ignore
	@Test
	public void testFormatAndAddNumberOfReults() throws IOException {

		File[] files = new File(FileUtils.outputPath + "google/20151214/").listFiles();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonParser jp = new JsonParser();

		for (File file : files) {

			if (file.getName().endsWith(".json")) {

				String fn = file.toString();
				System.out.println(fn);
				ArrayList<String> output = new ArrayList<>();
				ArrayList<String> input = (ArrayList<String>) FileUtils.fileToList(fn);

				int count = 0;

				for (String s : input) {

					JsonElement je = jp.parse(s);
					String prettyJsonString = gson.toJson(je);
					output.add(prettyJsonString);
					count += StringUtils.countMatches(prettyJsonString, "displayLink");
				}

				FileUtils.printList(output, FileUtils.outputPath + "google/20151214/cleaned/",
						file.getName().replace(".json", "") + "_" + count, ".json");

			}
		}

	}

	// @Ignore
	@Test
	public void testAddNumberOfResults() throws IOException {

		File[] files = new File(FileUtils.outputPath + "google/20151216/").listFiles();

		for (File file : files) {

			if (file.getName().endsWith(".json")) {

				String filePath = file.toString();
				System.out.println(filePath);
				ArrayList<String> input = (ArrayList<String>) FileUtils.fileToList(filePath);

				int count = 0;

				for (String s : input) {

					count += StringUtils.countMatches(s, "displayLink");

				}

				String fileName = file.getName();
				fileName = fileName.replace(".json", "");
				file.renameTo(
						new File(FileUtils.outputPath + "google/20151216/counted/" + fileName + "_" + count + ".json"));

			}
		}

	}

}
