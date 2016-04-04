package ang.ui;

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
	
	
	public FindIterable<Document> getSearchResults(
			String query,
			String source,
			boolean regex,
			boolean casesens,
			boolean useyear,
			int yearfrom,
			int yearto){
		
		if (!isInitiated() || query == null || query.length() == 0) return null;
		
		BasicDBObject q = new BasicDBObject();
		//text search (regex or index)
		if (regex){
			Pattern queryPattern = casesens ?
					Pattern.compile(query)
					: Pattern.compile(query, Pattern.CASE_INSENSITIVE);
			q.put("text", queryPattern);
		} else {
			BasicDBObject search = new BasicDBObject();
			search.put("$search", query);
			search.put("$caseSensitive", casesens);
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
		
		//procetion
		BasicDBObject keys = new BasicDBObject();
		keys.put("text", 1);
		keys.put("source", 1);
		
		return coll.find(q).projection(keys);
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
