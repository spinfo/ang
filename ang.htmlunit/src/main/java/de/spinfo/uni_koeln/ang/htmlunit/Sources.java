package de.spinfo.uni_koeln.ang.htmlunit;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Sources {

	private ArrayList<String> source = new ArrayList<String>();

	public void readSource() throws FileNotFoundException {

		File fileName = new File("sources.properties");

		Scanner scan = new Scanner(fileName);

		while (scan.hasNextLine()) {
			String line = scan.nextLine().split("\\=")[1];
			source.add(line);
		}
		
		scan.close();
	}

	public String getSource(int sourceNumber) throws FileNotFoundException {
		readSource();
		String s = source.get(sourceNumber);
		return s;
	}

}
