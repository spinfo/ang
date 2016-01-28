package de.uni_koeln.spinfo.ang.proc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

public class FilterTweets {

	public static void main(String[] args) throws IOException {

	}

	public void recFirstFilter(String inputPath, FileWriter info) throws IOException {

		File f = new File(inputPath);
		File[] subFiles = f.listFiles();
		for (File file : subFiles) {
			if (file.isFile()) {
				boolean isJson;
				if (file.getName().endsWith(".json")) {

					isJson = true;
					System.out.println("reading:	" + file.getName());
					info.write(file.getName());
					info.write("\n");
					firstFilter(file.getPath(), isJson);

				}

				if (file.getName().endsWith(".geo")) {

					isJson = false;
					System.out.println("reading:	" + file.getName());
					info.write(file.getName());
					info.write("\n");
					firstFilter(file.getPath(), isJson);

				}

			}
			if (file.isDirectory()) {
				System.out.println("dir:	" + file.getName());
				recFirstFilter(file.toString(), info);
			}
		}

	}

	public void recSecondFilter(String inputPath, FileWriter info) throws IOException {

		File f = new File(inputPath);
		File[] subFiles = f.listFiles();
		for (File file : subFiles) {
			if (file.isFile()) {

				if (file.getName().endsWith("_ff.json")) {

					System.out.println("reading:	" + file.getName());
					info.write(file.getName());
					info.write("\n");
					secondFilter(file.getPath());

				}

			}
			if (file.isDirectory()) {
				System.out.println("dir:	" + file.getName());
				recSecondFilter(file.toString(), info);
			}
		}

	}

	/**
	 * Filters Tweets containing 'lang=de'. This does not mean that the tweet's
	 * language is actually 'de'. It could also be the user's language.
	 * 
	 * 
	 * @param file
	 * @param isJson
	 * @throws IOException
	 */
	public void firstFilter(String file, boolean isJson) throws IOException {

		FileInputStream fis = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(fis, "UTF8");
		LineNumberReader reader = new LineNumberReader(isr);

		FileWriter output;
		FileWriter stats;
		FileWriter errors;

		if (isJson) {
			output = new FileWriter(file.replace(".json", "") + "_ff.json");
			stats = new FileWriter(file.replace(".json", "") + "_ff.info");
			errors = new FileWriter(file.replace(".json", "") + "_ff.err");

		} else {
			output = new FileWriter(file.replace(".geo", "") + "_ff.json");
			stats = new FileWriter(file.replace(".geo", "") + "_ff.info");
			errors = new FileWriter(file.replace(".geo", "") + "_ff.err");
		}

		String currentLine;

		Pattern pt = Pattern.compile("lang\":\"de\"");

		int german = 0;
		int total = 0;
		int err = 0;

		long startTime = System.nanoTime();

		while ((currentLine = reader.readLine()) != null) {

			if (currentLine.trim().matches("\\d+")) {

				continue;

			} else {

				total++;

				Matcher mc = pt.matcher(currentLine);

				if (mc.find() && currentLine.startsWith("{") && currentLine.endsWith("}")) {
					output.write(currentLine);
					output.write("\n");
					german++;
				}

				else if (!currentLine.startsWith("{") || !currentLine.endsWith("}")) {
					err++;
					errors.write(currentLine);
					errors.write("\n");

				}

			}

		}

		float percentageDE = german * 100.0f / total;

		long endTime = System.nanoTime();

		long duration = (endTime - startTime);

		stats.write(duration + "\t" + total + "\t" + german + "\t" + percentageDE + "\t" + err);

		reader.close();
		output.close();
		stats.close();
		errors.close();

	}

	/**
	 * To be used after {@link #firstFilter}. This method extracts tweets whose
	 * language has been marked as 'de'
	 * 
	 * 
	 * @param file
	 * @param isJson
	 * @throws IOException
	 */

	public void secondFilter(String file) throws IOException {

		FileInputStream fis = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(fis, "UTF8");
		LineNumberReader reader = new LineNumberReader(isr);

		FileWriter output = new FileWriter(file.replace("_ff.json", "") + "_sf.json");
		FileWriter stats = new FileWriter(file.replace("_ff.json", "") + "_sf.info");
		FileWriter errors = new FileWriter(file.replace("_ff.json", "") + "_sf.err");

		String currentLine;
		int german = 0;
		int total = 0;
		int err = 0;

		long startTime = System.nanoTime();

		while ((currentLine = reader.readLine()) != null) {

			total++;

			try {

				final JSONObject json = new JSONObject(currentLine);
				final String lang = json.optString("lang");

				if (lang.toString().equals("de")) {
					german++;
					output.write(currentLine);
					output.write("\n");

				}
			}

			catch (org.json.JSONException exception) {

				err++;
				errors.write(currentLine);
				errors.write("\n");
			}

		}

		long endTime = System.nanoTime();
		long duration = (endTime - startTime);

		float percentageDE = german * 100.0f / total;

		stats.write(duration + "\t" + total + "\t" + german + "\t" + percentageDE + "\t" + err);

		reader.close();
		stats.close();
		output.close();
		errors.close();

	}

	public void processStats() throws IOException {

		FileWriter ffout = new FileWriter(getAccess().getProperty("startat") + "2013_ff.stats");
		FileWriter sfout = new FileWriter(getAccess().getProperty("startat") + "2013_sf.stats");

		long startTime = System.nanoTime();

		List<Stats> allStats = new ArrayList<>();

		allStats = getAllStats(getAccess().getProperty("startat"), allStats);

		int ff_total = 0;
		int ff_german = 0;
		int ff_errors = 0;
		long ff_dur = 0;

		int sf_total = 0;
		int sf_german = 0;
		int sf_errors = 0;
		long sf_dur = 0;

		for (Stats s : allStats) {

			String file = s.getFilename();

			if (file.endsWith("_ff.info")) {

				ff_total += s.getTotal();
				ff_german += s.getGerman();
				ff_errors += s.getErrors();
				ff_dur += s.getDuration();
			}

			if (file.endsWith("_sf.info")) {

				sf_total += s.getTotal();
				sf_german += s.getGerman();
				sf_errors += s.getErrors();
				sf_dur += s.getDuration();

			}

		}

		ffout.write(ff_total + "\t" + ff_german + "\t" + ff_errors + "\t" + ff_dur + "\t");
		sfout.write(sf_total + "\t" + sf_german + "\t" + sf_errors + "\t" + sf_dur + "\t");

		ffout.close();
		sfout.close();
		long endTime = System.nanoTime();

		long duration = (endTime - startTime);
		System.out.println("TOTAL TIME (NANOSECONDS): " + duration);

	}

	public List<Stats> getAllStats(String inputPath, List<Stats> stats) {

		File f = new File(inputPath);
		File[] subFiles = f.listFiles();
		for (File file : subFiles) {
			if (file.isFile()) {

				if (file.getName().endsWith("_ff.info") || file.getName().endsWith("_sf.info")) {

					System.out.println("reading:	" + file.getName());

					try {
						Stats st = getStats(file.toString());

						if (st != null) {
							stats.add(st);
						}

					} catch (IOException e) {
						System.out.println(file + " could not be processed");
						e.printStackTrace();
					}
				}

			}
			if (file.isDirectory()) {
				System.out.println("dir:	" + file.getName());
				getAllStats(file.toString(), stats);
			}
		}

		return stats;

	}

	public Stats getStats(String file) throws IOException {

		ArrayList<String> lines = (ArrayList<String>) Files.readAllLines(Paths.get(file), StandardCharsets.UTF_8);

		String stats = lines.get(0);

		String[] statsArr = stats.split("\t");

		Stats st = null;

		if (statsArr.length > 2) {
			st = new Stats(file, Long.parseLong(statsArr[0]), Integer.parseInt(statsArr[1]),
					Integer.parseInt(statsArr[2]), Float.parseFloat(statsArr[3]), Integer.parseInt(statsArr[4]));

		}

		return st;

	}

	public void getSome(String file) throws IOException {

		FileInputStream fis = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(fis, "UTF8");
		LineNumberReader reader = new LineNumberReader(isr);

		// Gson gson = new GsonBuilder().setPrettyPrinting().create();
		// JsonParser jp = new JsonParser();

		FileWriter output = new FileWriter("some_error_orig.json");

		String currentLine;

		while ((currentLine = reader.readLine()) != null && reader.getLineNumber() < 200) {

			// JsonElement je = jp.parse(currentLine);
			// String prettyJsonString = gson.toJson(je);
			// output.write(prettyJsonString);
			output.write(currentLine);
			output.write("\n");

		}

		reader.close();
		output.close();

	}

	public Properties getAccess() {

		Properties prop = new Properties();
		InputStream input = null;
		try {

			input = new FileInputStream("path.properties");
			prop.load(input);

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return prop;
	}

}
