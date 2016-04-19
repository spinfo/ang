package ang.ui;

import java.util.List;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;


public class MongoWrapper {
	
	private static MongoClient mongoClient;
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
		if (mongoClient != null) mongoClient.close();
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
	
	
	public FindIterable<Document> getSearchResults(
			String query,
			String source,
			boolean casesens,
			boolean regex,
			boolean useyear,
			int yearfrom,
			int yearto,
			int limitresults){
		
		if (!isInitiated() || query == null || query.length() == 0) return null;
		BasicDBObject q = new BasicDBObject();
		
		//search (regex or index)
		if (regex){
			//regex search
			BasicDBObject regexQuery = new BasicDBObject();
			regexQuery.put("$regex", query);
			if (!casesens) regexQuery.put("$options", "i");
			q.put("text", regexQuery);
		} else {
			//text search
			BasicDBObject search = new BasicDBObject();
			search.put("$search", query);
			search.put("$caseSensitive", casesens);
			search.put("$language", "none");
			q.put("$text", search);
		}
		
		//source
		if (source != null && source.length() > 0)
			q.put("source", source);
		
		//year
		if (useyear){
			BasicDBObject year = new BasicDBObject();
			year.put("$gte", yearfrom);
			year.put("$lte", yearto);
			q.put("date_year", year);
		}
		
		//projection
		BasicDBObject keys = new BasicDBObject();
		keys.put("text", 1);
		keys.put("source", 1);
		
		return coll.find(q).projection(keys).limit(limitresults);
	}
	
	
	public MongoCollection<Document> getCollection(String collectionName){
		if (!isInitiated()
				|| collectionName == null
				|| collectionName.length() == 0)
			return null;
		
		return database.getCollection(collectionName);
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
