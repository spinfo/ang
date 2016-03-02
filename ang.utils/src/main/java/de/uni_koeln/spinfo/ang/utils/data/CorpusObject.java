package de.uni_koeln.spinfo.ang.utils.data;

import java.util.HashMap;
import java.util.Map;

import org.bson.Document;

public class CorpusObject {
	
	private Map<String, Object> data;
	
	public CorpusObject(){
		data = new HashMap<String, Object>();
	}
	
	public void addData(CorpusObjectField fieldKey, Object data){
		if (!data.getClass().equals(fieldKey.getType())){
			System.err.println("[ERROR]\tdata \"" + data.toString()
			+ "\" added to CorpusObject instance has wrong type (should be "
					+ fieldKey.getType() + ")!");
			return;
		}
		this.data.put(fieldKey.toString(), data);
	}
	
	public void removeData(CorpusObjectField fieldKey){
		data.remove(fieldKey.toString());
	}
	
	public String getData(CorpusObjectField fieldKey){
		return (String)data.get(fieldKey.toString());
	}
	
	public Document getBsonDocument(){
		return new Document(data);
	}

}
