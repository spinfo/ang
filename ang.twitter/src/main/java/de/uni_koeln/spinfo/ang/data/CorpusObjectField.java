package de.uni_koeln.spinfo.ang.data;

public enum CorpusObjectField {
	
	ID ("id"),
	TEXT ("text"),
	SOURCE ("source"),
	SOURCE_ARCHIVE ("source_archive"),
	SOURCE_FILE ("source_file"),
	DATE_YEAR ("date_year"),
	DATE_MONTH ("date_month"),
	LENGTH ("length");
	
	private String value;
	
	private CorpusObjectField(String s){
		value = s;
	}
	
	@Override
	public String toString() {
		return value;
	}
	
	public String getValue() {
		return value;
	}
	
	public String v(){
		return value;
	}
	
}
