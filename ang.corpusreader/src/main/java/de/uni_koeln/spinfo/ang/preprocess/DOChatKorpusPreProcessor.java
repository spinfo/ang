package de.uni_koeln.spinfo.ang.preprocess;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import de.uni_koeln.spinfo.ang.utils.AngStringUtils;
import de.uni_koeln.spinfo.ang.utils.IO;
import de.uni_koeln.spinfo.ang.utils.data.CorpusObject;
import de.uni_koeln.spinfo.ang.utils.data.CorpusObjectField;


public class DOChatKorpusPreProcessor extends AbstractPreProcessor {

	@Override
	protected void transformCorpusObjects(File inputFile, int fileCount) {
		BufferedReader br = IO.getFileReader(inputFile.getAbsolutePath(), Charset.forName("windows-1252"));
		String dateString = null;
		String line;
		boolean skipMessage = false;
		boolean messageInNextLine = false;
		int dateYear = 0;
		int dateMonth = 0;
		
		try {
			while ((line = br.readLine()) != null){
				if (messageInNextLine){
					if (!skipMessage){
						String text = line.replaceAll("(<[^>]+>|</[^>]+>)", "");
						text = AngStringUtils.cleanStringFromInvalidChars(text);
						text = AngStringUtils.normalize(text);
						text = text.replaceAll("\\\\t", "");
						text = text.trim();
						if (text.matches(".*\\w+.*") && text.length() > 10 && text.split(" ").length > 2){
							CorpusObject obj = new CorpusObject();
							obj.addData(CorpusObjectField.ID_STRING,
									"dortmunder-chat-korpus-" + AngStringUtils.generateID(text));
							obj.addData(CorpusObjectField.TEXT_STRING, text);
							obj.addData(CorpusObjectField.LENGTH_INT, text.length());
							obj.addData(CorpusObjectField.SOURCE_STRING, "dortmunder chat-korpus");
							obj.addData(CorpusObjectField.SOURCE_FILE_STRING, inputFile.getName());
							obj.addData(CorpusObjectField.SOURCE_ARCHIVE_STRING, "Dortmunder Chat-Korpus (Releasekorpus).zip");
							if (dateYear != 0)
								obj.addData(CorpusObjectField.DATE_YEAR_INT, dateYear);
							if (dateMonth != 0)
								obj.addData(CorpusObjectField.DATE_MONTH_INT, dateMonth);
							
							mongo.addDocument(obj.getBsonDocument());
						}
					}
					messageInNextLine = false;
					skipMessage = false;
					continue;
				}
				if (dateString == null && line.matches(".*\\srecDate\\=\\\".*")){
					dateString = line.replaceFirst(".*\\srecDate\\=\\\"", "")
							.replaceFirst("(?<=\\d)\\\"\\s.*", "");
					if (dateString.matches("(19|20)\\d\\d\\-\\d\\d\\-?\\d?\\d?")){
						dateYear = Integer.parseInt(
								dateString.replaceAll("(?<=(19|20)\\d\\d)\\-?\\d?\\d?\\-?\\d?\\d?", "")); 
						dateMonth = Integer.parseInt(
								dateString.replaceAll("^(19|20)\\d\\d\\-?", "").replaceAll("(?<=\\d\\d?+).*$", ""));
					} else {
						dateYear = 0;
						dateMonth = 0;
					}
				}
				if (line.matches(".*\\<nickname\\>server\\<\\/nickname\\>$")){
					skipMessage = true;
				}
				if (line.matches(".*\\<messageBody\\>$")){
					messageInNextLine = true;
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
//		
//		
//		XML xml = null;
//
//		try {
//			xml = new XMLDocument(IO.readFile(inputFile.getAbsolutePath(), Charset.forName("windows-1252")));
//			List<XML> msgs = xml.nodes("//message");
//			
//			String date = xml.xpath("/logfile/head/record/@recDate").get(0);
//			int dateYear = Integer.parseInt(
//					date.replaceAll("(?<=(19|20)\\d\\d)\\-\\d\\d\\-\\d\\d", "")); 
//			int dateMonth = Integer.parseInt(
//					date.replaceAll("^(19|20)\\d\\d\\-", "").replaceAll("\\-\\d\\d$", "")); 
//			
//			for (XML msg : msgs){
//				String nick;
//				try {
//					nick = msg.xpath("messageHead/nickname/text()").get(0);
//					
//					if (nick == null || nick.equalsIgnoreCase("server")) continue;
//						
//					String text = msg.xpath("messageBody/text()").get(0);
//					text = AngStringUtils.cleanStringFromInvalidChars(text);
//					text = AngStringUtils.normalize(text);
//					text = text.replaceAll("\\\\t", "");
//					text = text.trim();
//					
//					if (text.length() < 5 || text.split(" ").length < 2) continue;
//					
//					CorpusObject obj = new CorpusObject();
//					obj.addData(CorpusObjectField.ID_STRING,
//							"dortmunder-chat-korpus-" + AngStringUtils.generateID(text));
//					obj.addData(CorpusObjectField.TEXT_STRING, text);
//					obj.addData(CorpusObjectField.LENGTH_INT, text.length());
//					obj.addData(CorpusObjectField.SOURCE_STRING, "dortmunder chat-korpus");
//					obj.addData(CorpusObjectField.SOURCE_FILE_STRING, inputFile.getName());
//					obj.addData(CorpusObjectField.SOURCE_ARCHIVE_STRING, "Dortmunder Chat-Korpus (Releasekorpus).zip");
//					obj.addData(CorpusObjectField.DATE_YEAR_INT, dateYear);
//					obj.addData(CorpusObjectField.DATE_MONTH_INT, dateMonth);
//					
//					mongo.addDocument(obj.getBsonDocument());
//				} catch (Exception e) {
//					continue;
//				}
//			}
//		} catch (Exception e) {
//			return;
//		}
	}

}
