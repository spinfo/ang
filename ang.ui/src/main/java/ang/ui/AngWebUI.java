package ang.ui;

import de.uni_koeln.spinfo.ang.utils.MongoWrapper;


public class AngWebUI {
	
	private static WebApp webApp;
	
	
	public static void main(String[] args) {
		webApp = new WebApp(init());
		webApp.mapRoutes();
	}
	
	
	private static MongoWrapper init(){
		MongoWrapper mongo = new MongoWrapper();
		mongo.init("",	//USER
				"",		//PASS
				"ang",
				"pott.spinfo.uni-koeln.de",
				"27017",
				"angdata");
		return mongo;
	}

}
