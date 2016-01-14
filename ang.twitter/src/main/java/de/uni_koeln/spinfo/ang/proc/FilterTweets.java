package de.uni_koeln.spinfo.ang.proc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilterTweets {

	public static void main(String[] args) throws IOException {

		FilterTweets extract = new FilterTweets();
		Properties prop = extract.getAccess();
		extract.findFiles(prop.getProperty("startat"));
		

	}

	public void findFiles(String inputPath) throws IOException {

		File f = new File(inputPath);
		File[] subFiles = f.listFiles();
		for (File file : subFiles) {
			if (file.isFile() && file.getName().endsWith(".json")) {
				System.out.println("reading:	" + file.getName());
				filterGermanTweets(file.getPath());
			}
			if (file.isDirectory()) {
				System.out.println("dir:	" + file.getName());
				findFiles(file.toString());
			}
		}

	}

	public void filterGermanTweets(String file) throws IOException {

		FileInputStream fis = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(fis, "UTF8");
		LineNumberReader reader = new LineNumberReader(isr);

		FileWriter output = new FileWriter(file.replace(".json", "") + "_de.json");

		String currentLine;

		Pattern pt = Pattern.compile("lang\":\"de\"");

		int german = 0;

		while ((currentLine = reader.readLine()) != null) {

			Matcher mc = pt.matcher(currentLine);

			if (mc.find()) {
				output.write(currentLine);
				output.write("\n");
				german++;
			}

		}

		System.out.println("total german tweets in " + file + ": " + german);

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
