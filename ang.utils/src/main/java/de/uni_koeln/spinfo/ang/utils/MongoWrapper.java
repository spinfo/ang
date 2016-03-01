package de.uni_koeln.spinfo.ang.utils;

import java.util.Set;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import de.uni_koeln.spinfo.ang.utils.data.CorpusObject;


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
	
	
	public void addDocument(CorpusObject corpusObject){
		if (isInitiated()) coll.insertOne(corpusObject.getBsonDocument());
	}
	
	
	public void addDocuments(Set<CorpusObject> corpusObjects){
		if (!isInitiated()) return;
		for (CorpusObject co : corpusObjects){
			coll.insertOne(co.getBsonDocument());
		}
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
