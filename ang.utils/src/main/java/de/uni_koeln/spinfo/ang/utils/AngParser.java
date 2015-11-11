package de.uni_koeln.spinfo.ang.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AngParser {

	public List<String> getAnglicisms() throws IOException {

		List<String> ang_raw = FileUtils.fileToList(FileUtils.inputPath
				+ "ang_raw.txt");

		return ang_raw;
	}

	public List<String> cleanAnglicisms(List<String> rawAnglicisms) {

		List<String> cleanAnglicisms = new ArrayList<>();

		for (String s : rawAnglicisms) {

			s = s.trim();

			if (s.startsWith("(") || s.endsWith("-:")) {
				// System.out.println(s);
				continue;
			}

			String[] sA = s.split(":");

			String ang = sA[0].replaceAll("[\\p{Nd}]", "");

			// System.out.println(ang);

			// avoid forms such as 'ab-'
			if (!ang.endsWith("-")) {

				// Remove parenthesis and its content
				ang = ang.replaceAll("\\(.*\\)", "").trim();

				// If it contains a comma
				if (ang.contains(",")) {

					String[] angComm = ang.split(",");

					for (String s1 : angComm) {
						cleanAnglicisms.add(s1.trim());

					}

				}

				// or a slash
				else if (ang.contains("/")) {

					String[] angSlash = ang.split("/");

					for (String s1 : angSlash) {
						cleanAnglicisms.add(s1.trim());

					}

				}

				// default
				else {

					if (!ang.equals("")) {
						cleanAnglicisms.add(ang);
					}
				}

			}

		}

		return cleanAnglicisms;
	}

}
