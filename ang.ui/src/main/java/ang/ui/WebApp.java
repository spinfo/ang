package ang.ui;

import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.staticFileLocation;
import static spark.Spark.stop;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bson.Document;

import com.mongodb.client.FindIterable;

import de.uni_koeln.spinfo.ang.utils.MongoWrapper;
import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerEngine;

public class WebApp {
	
	private MongoWrapper mongo;
	
	
	public WebApp(MongoWrapper mongo){
		this.mongo = mongo;
	}
	
	
	public void mapRoutes(){
		//set port
		port(8080);
		
		// MAP /search
		get("/search", (request, response) -> {
			String query = request.queryParams("query");
			String source = request.queryParams("source");
			source = (source == null ? null : (source.length() == 0 ? null : source));
			query = (query == null ? null : (query.length() == 0 ? null : query));
			
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("query", query);
			model.put("source", source);
			
			Set<DBData> data = new HashSet<DBData>();
			
			if (query != null){
				FindIterable<Document> results = mongo.getSearchResults(query, source);
				if (results != null){
					model.put("results", data);
					for (Document doc : results){
						data.add(new DBData(doc.getString("source"),
								doc.getString("text")));
					}
				}
			}
			
			return new ModelAndView(model, "index.ftl");
        }, new FreeMarkerEngine());
		
		//MAP /search/
		get("/search/", (request, response) -> {
			response.redirect("/search");
		    return null;
		});
		
		//MAP /
		get("/", (request, response) -> {
			response.redirect("/search");
		    return null;
		});
		
		//MAP /stop
		get("/stop", (request, response) -> {
			exit();
		    return null;
		});
	}
	
	
	public void exit(){
		mongo.close();
		stop();
	}
	
	
	private class DBData {
		private String source;
		private String text;
		
		public DBData(String source, String text) {
			super();
			this.source = source;
			this.text = text;
		}

		@SuppressWarnings("unused")
		public String getSource() {
			return source;
		}

		@SuppressWarnings("unused")
		public String getText() {
			return text;
		}
	}


}
