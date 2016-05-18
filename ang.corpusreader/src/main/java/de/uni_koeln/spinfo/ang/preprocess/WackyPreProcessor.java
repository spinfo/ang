package de.uni_koeln.spinfo.ang.preprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import de.uni_koeln.spinfo.ang.utils.AngStringUtils;
import de.uni_koeln.spinfo.ang.utils.IO;
import de.uni_koeln.spinfo.ang.utils.Patterns;
import de.uni_koeln.spinfo.ang.utils.data.CorpusObject;
import de.uni_koeln.spinfo.ang.utils.data.CorpusObjectField;

public class WackyPreProcessor extends AbstractPreProcessor {

	@Override
	protected void transformCorpusObjects(File inputFile, int fileCount) {
		BufferedReader br = IO.getFileReader(
				inputFile.getAbsolutePath(),
				Charset.forName("windows-1252"));
		
		String line;
		int countText = 0;
		int countSentence = 0;
		double totalChars = 10952100000D;
		double countChars = 0;
		
		try {
			while ((line = br.readLine()) != null){
				
				countChars += line.length();
				
				if (line.matches(Patterns.WACKY_URL_LINE)
						|| line.length() < 10) continue;
				
				line = AngStringUtils.cleanStringFromInvalidChars(line);
				line = AngStringUtils.normalize(line);
				
				//split text
				List<String> texts = new ArrayList<String>();
				int cut;
				
				while (line.length() > 0){
					if (line.length() > 500){
						cut = line.indexOf(". ", 300);
						texts.add(line.substring(0, cut + 1).trim());
						line = line.substring(cut + 2);
					} else {
						texts.add(line);
						line = "";
					}
				}
				
				//create db objects
				for (String text : texts){
					int idHash = AngStringUtils.generateID(text);
					CorpusObject obj = new CorpusObject();
					obj.addData(CorpusObjectField.ID_STRING,
							"wacky-txt" + countText++
							+ "-ln" + countSentence++
							+ "-" + idHash);
					obj.addData(CorpusObjectField.TEXT_STRING, text);
					obj.addData(CorpusObjectField.SOURCE_STRING, "wacky");
					obj.addData(CorpusObjectField.SOURCE_FILE_STRING, "dewac_preproc");
					obj.addData(CorpusObjectField.SOURCE_ARCHIVE_STRING, "dewac_preproc.gz");
					obj.addData(CorpusObjectField.LENGTH_INT, text.length());
					mongo.addDocument(obj.getBsonDocument());
				}
				System.out.println("[" + String.format("%.2f", (countChars/totalChars)*100) + "%]");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
