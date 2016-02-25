package de.uni_koeln.spinfo.ang.data;

public enum CorpusObjectField {
	
	ID_STRING ("id", String.class),
	TEXT_STRING ("text", String.class),
	SOURCE_STRING ("source", String.class),
	SOURCE_ARCHIVE_STRING ("source_archive", String.class),
	SOURCE_FILE_STRING ("source_file", String.class),
	DATE_YEAR_INT ("date_year", Integer.class),
	DATE_MONTH_INT ("date_month", Integer.class),
	LENGTH_INT ("length", Integer.class);
	
	private String value;
	private Class<?> type;
	
	private CorpusObjectField(String value, Class<?> type){
		this.value = value;
		this.type = type;
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
	
	public Class<?> getType(){
		return type;
	}
	
}
