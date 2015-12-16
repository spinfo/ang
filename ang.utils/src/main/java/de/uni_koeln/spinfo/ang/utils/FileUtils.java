package de.uni_koeln.spinfo.ang.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

public class FileUtils {

	private FileUtils() {
		throw new AssertionError();
	}

	public static String outputPath = "../ang.data/output/";
	public static String inputPath = "../ang.data/input/";

	public static List<String> fileToList(String filePath) throws IOException {

		ArrayList<String> normalized = new ArrayList<String>();

		ArrayList<String> lines = (ArrayList<String>) Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);

		for (String s : lines) {

			s = Normalizer.normalize(s, Normalizer.Form.NFC);
			normalized.add(s);
		}
		return normalized;
	}

	public static <T> void serializeList(List<T> list, String fileName) throws IOException {

		ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(outputPath + fileName));

		outputStream.writeObject(list);

		outputStream.close();

	}

	public static <T> void serializeList(List<T> list, String destPath, String fileName) throws IOException {

		fileName = fileName + ".ser";

		File file = new File(destPath + fileName);

		ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));

		outputStream.writeObject(list);

		outputStream.close();

	}

	public static <K, V> File printMap(Map<K, V> map, String destPath, String fileName) throws IOException {

		File file = new File(destPath + fileName + getISO8601StringForCurrentDate() + ".txt");
		Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF8"));

		for (Map.Entry<K, V> entry : map.entrySet()) {
			out.append(entry.getKey() + " : " + entry.getValue());
			out.append("\n");
		}

		out.flush();
		out.close();

		return file;
	}

	public static <T> File printSet(Set<T> set, String destPath, String filename) throws IOException {

		File file = new File(destPath + filename + getISO8601StringForCurrentDate() + ".txt");

		Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF8"));

		for (Object o : set) {
			writer.append(o + "\n");
		}

		writer.flush();
		writer.close();

		return file;
	}

	public static <T> File listToTXT(List<T> list, String destPath, String filename) throws IOException {

		File file = new File(destPath + filename + getISO8601StringForCurrentDate() + ".txt");

		Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF8"));

		for (Object o : list) {
			writer.append(o + "\n");
		}

		writer.flush();
		writer.close();

		return file;
	}

	public static <T> File printList(List<T> list, String destPath, String filename, String fileFormat)
			throws IOException {

		File file = new File(destPath + filename + fileFormat);

		Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF8"));

		for (Object o : list) {
			writer.append(o + "\n");
		}

		writer.flush();
		writer.close();

		return file;
	}

	public static String getISO8601StringForCurrentDate() {
		Date now = new Date();
		return getISO8601StringForDate(now);
	}

	private static String getISO8601StringForDate(Date date) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.GERMANY);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+1"));
		return dateFormat.format(date);
	}

}
