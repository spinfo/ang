package ang.ui;

import static spark.Spark.*;
import org.bson.Document;
import com.mongodb.client.FindIterable;
import de.uni_koeln.spinfo.ang.utils.MongoWrapper;


public class Test {
	
	private static MongoWrapper mongo;
	private static final String HTML_HEADER = "<!DOCTYPE html> <html> <head> <title>ANG-DB</title> </head> <body>";
	private static final String HTML_FOOTER = "</body> </html>";

	
	public static void main(String[] args) {
		init();
		mapRoutes();
	}
	
	
	private static void init(){
		mongo = new MongoWrapper();
		mongo.init("",	//USER
				"",		//PASS
				"ang",
				"pott.spinfo.uni-koeln.de",
				"27017",
				"angdata");
	}
	
	
	private static void mapRoutes(){
		//set port
		port(8080);
		
		// MAP /search
		get("/search", (request, response) -> {
			String query = request.queryParams("query");
			String source = request.queryParams("source");
			
			if (query != null){
				FindIterable<Document> results = mongo.getSearchResults(query, source);
				if (results == null) return "Query error.";
				
				StringBuilder sb = new StringBuilder();
				sb.append("<table style=\"width:100%\">"
						+ "<tr class=\"tableHeader\" style=\"font-weight:bold\">"
						+ "<td>ID</td><td>Quelle</td><td>Text</td></tr>");
				for (Document doc : results){
					sb.append("<tr>");
					sb.append("<td>" + doc.getString("id") + "</td>");
					sb.append("<td>" + doc.getString("source") + "</td>");
					sb.append("<td>" + doc.getString("text") + "</td>");
					sb.append("</tr>");
				}
				sb.append("</table>");
				
				return HTML_HEADER
						+"Query: " + query
						+ "<br>============================<br><br>"
						+ sb.toString()
						+ HTML_FOOTER;
			} else {
				return "No Query sent!";
			}
		});
		
		//MAP /search/
		get("/search/", (request, response) -> {
			response.redirect("/search");
		    return null;
		});
		
		//MAP /stop
		get("/stop", (request, response) -> {
			exit();
		    return null;
		});
	}
	
	
	private static void exit(){
		mongo.close();
		stop();
	}

}
