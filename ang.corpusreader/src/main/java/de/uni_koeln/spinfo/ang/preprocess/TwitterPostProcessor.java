package de.uni_koeln.spinfo.ang.preprocess;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;

import de.uni_koeln.spinfo.ang.utils.AngStringUtils;
import de.uni_koeln.spinfo.ang.utils.MongoWrapper;
import de.uni_koeln.spinfo.ang.utils.Patterns;

public class TwitterPostProcessor {

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
		double total = coll.count();
		
		for (Document doc : coll.find()){
			System.out.println((++count/total)*100);
			doc = normalizeDoc(doc); //normalize Doc

			String originalText;
			String cleanedText = doc.getString("text");
			
			//replace Hashtags
			do {
				originalText = cleanedText.toString();
				cleanedText = cleanedText
					.replaceFirst(Patterns.TWITTER_HASHTAG,
							"[HASHTAG" + AngStringUtils.generateID(cleanedText) + "]");
				cleanedText = cleanedText
						.replaceFirst(Patterns.TWITTER_RETWEET_SHORT,
								"[RETWEET" + AngStringUtils.generateID(cleanedText) + "]");
				cleanedText = cleanedText
						.replaceFirst(Patterns.TWITTER_MENTION,
								"[MENTION" + AngStringUtils.generateID(cleanedText) + "]");
				cleanedText = cleanedText
						.replaceFirst(Patterns.URL,
								"[URL" + AngStringUtils.generateID(cleanedText) + "]");
			} while (!cleanedText.equals(originalText));
			
			doc.put("text", cleanedText);
			coll.replaceOne(new BasicDBObject("_id", doc.get("_id")), doc);
		}
		
		mongo.close();
	}
	
	
	private static Document normalizeDoc(Document doc){
		String txtOrg;
		
		if (doc.containsKey("text_original")){
			txtOrg = doc.getString("text_original");
		} else {
			txtOrg = doc.getString("originaltxt");
		}
		
		doc.put("text", txtOrg);
		doc.put("text_original", txtOrg);
		doc.remove("originaltxt");
		
		return doc;
	}

}
