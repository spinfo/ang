package de.uni_koeln.spinfo.ang.preprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import de.uni_koeln.spinfo.ang.utils.AngStringUtils;
import de.uni_koeln.spinfo.ang.utils.IO;
import de.uni_koeln.spinfo.ang.utils.data.CorpusObject;
import de.uni_koeln.spinfo.ang.utils.data.CorpusObjectField;

public class HamburgDTKorpusPreProcessor extends AbstractPreProcessor {

	@Override
	protected void transformCorpusObjects(File inputFile, int fileCount) {
		BufferedReader br = IO.getFileReader(inputFile.getAbsolutePath());
		
		String line;
		String text = "";
		long count = 1;
		
		try {
			while ((line = br.readLine()) != null){
				
				//text complete
				if (line.length() == 0){
					
					//add appropriate whitespaces
					text = text.replaceAll("\\s\\,\\s", ", ")
							.replaceAll("\\s\\.\\s", ". ").trim();
					
					text = AngStringUtils.cleanStringFromInvalidChars(text);
					text = AngStringUtils.normalize(text);
					int idHash = AngStringUtils.generateID(text);
					
					//cancel if text too short
					if (text.replaceAll("\\P{L}+", " ")
							.trim().split(" ").length < 2){
						text = "";
						continue;
					}
					
					CorpusObject obj = new CorpusObject();
					obj.addData(CorpusObjectField.ID_STRING, "hamburg-dependency-treebank-"
							+ fileCount + "" + count++ + "" + idHash);
					obj.addData(CorpusObjectField.TEXT_STRING, text);
					obj.addData(CorpusObjectField.SOURCE_STRING, "hamburg-dependency-treebank");
					obj.addData(CorpusObjectField.SOURCE_FILE_STRING, inputFile.getName());
					obj.addData(CorpusObjectField.SOURCE_ARCHIVE_STRING, "hdt-conll-1.0.tar.gz");
					obj.addData(CorpusObjectField.LENGTH_INT, text.length());
					mongo.addDocument(obj.getBsonDocument());
					
					text = "";
					continue;
				}
				
				//text continues...
				text += line.split("\t")[1] + " ";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
