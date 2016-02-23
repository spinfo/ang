package de.uni_koeln.spinfo.ang.util;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import de.uni_koeln.spinfo.ang.data.CorpusObject;

public class MongoWrapper {
	
	MongoClient mongoClient;
	MongoDatabase database;
	MongoCollection<Document> coll;

	
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
		if (coll == null || database == null || mongoClient == null){
			System.err.println("[DBWRP]\tMongoWrapper not properly initiated!");
			return;
		}
		coll.insertOne(corpusObject.getBsonDocument());
	}

}
