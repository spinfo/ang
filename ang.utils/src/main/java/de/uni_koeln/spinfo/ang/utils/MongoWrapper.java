package de.uni_koeln.spinfo.ang.utils;

import java.util.List;
import java.util.regex.Pattern;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;


public class MongoWrapper {
	
	private MongoClient mongoClient;
	private MongoDatabase database;
	private MongoCollection<Document> coll;

	
	public MongoWrapper(){
		
	}
	
	public void init(String user,
			String pass,
			String db,
			String host,
			String port,
			String collection){
		
		System.out.println("[DBWRP]\tconnecting to " + host + ":" + port + " ...");
		MongoClientURI uri = new MongoClientURI(
				"mongodb://" + user + ":" + pass
				+ "@" + host + ":" + port
				+ "/?authSource=" + db + "&authMechanism=SCRAM-SHA-1");
		mongoClient = new MongoClient(uri);
		database = mongoClient.getDatabase(db);
		coll = database.getCollection(collection);
	}
	
	
	public void close(){
		mongoClient.close();
	}
	
	
	public void addDocument(Document corpusDocument){
		if (!isInitiated()) return;
		coll.insertOne(corpusDocument);
	}
	
	
	public void addDocuments(List<Document> corpusDocuments){
		if (!isInitiated()) return;
		coll.insertMany(corpusDocuments);
	}
	
	
	public FindIterable<Document> getSearchResults(String queryRegex, String source){
		if (!isInitiated() || queryRegex == null || queryRegex.length() == 0) return null;
		
		BasicDBObject q = new BasicDBObject();
		q.put("text", Pattern.compile(queryRegex, Pattern.CASE_INSENSITIVE));
		if (source != null) q.put("source", source);
		
		BasicDBObject keys = new BasicDBObject();
		keys.put("text", 1);
		keys.put("source", 1);
		
		return coll.find(q).projection(keys);
	}
	
	
	private boolean isInitiated(){
		if (coll == null || database == null || mongoClient == null){
			System.err.println("[DBWRP]\tMongoWrapper not properly initiated!");
			return false;
		} else {
			return true;
		}
	}

}
