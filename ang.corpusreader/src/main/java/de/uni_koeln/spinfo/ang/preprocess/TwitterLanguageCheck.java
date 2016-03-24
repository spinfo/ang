package de.uni_koeln.spinfo.ang.preprocess;

import org.bson.Document;

import com.mongodb.client.MongoCollection;

import de.uni_koeln.spinfo.ang.langdetect.TikaLanguageDetector;
import de.uni_koeln.spinfo.ang.utils.MongoWrapper;
import de.uni_koeln.spinfo.ang.utils.Patterns;

public class TwitterLanguageCheck {

	public static void main(String[] args) {
		MongoWrapper mongo = new MongoWrapper();
		mongo.init("", //db user
				"", //db pass
				"ang", //db name
				"localhost", //db host
				"27017", //db port
				"cleanTweets");
		
		MongoCollection<Document> coll = mongo.getCollection("cleanTweets");
		double count = 0;
		
		TikaLanguageDetector detector = new TikaLanguageDetector();
		double countGerman = 0;
		double countEnglish = 0;
		
		for (Document doc : coll.find()){
			String text = doc.getString("text")
					.replaceAll(Patterns.TWITTER_PLACEHOLDER, "")
					.replaceAll("\\s\\s", " ");
			if (detector.isGerman(text)){
				countGerman++;
			}
			if (detector.isEnglish(text)){
				countEnglish++;
			}
			count++;
			if (count%1000 == 0){
				System.out.println("COUNT: " + count);
				System.out.println("==========\n"
						+ "DE: " + countGerman + "\n"
						+ "EN: " + countEnglish + "\n"
						+ "==========\n\n");
			}
		}
		
		mongo.close();
		
		System.out.println("==========\n"
				+ "DE: " + countGerman + "\n"
				+ "EN: " + countEnglish + "\n"
				+ "==========\n");
	}
	
}
