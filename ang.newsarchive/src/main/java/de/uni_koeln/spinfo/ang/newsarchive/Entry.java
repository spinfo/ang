package de.uni_koeln.spinfo.ang.newsarchive;

import java.io.Serializable;

public class Entry implements Serializable{

	private static final long serialVersionUID = 1L;
	private String ausgabe;
	private String contentText;
	private String url;
	private String date;
	
	
	public String getAusgabe() {
		return ausgabe;
	}
	public void setAusgabe(String ausgabe) {
		this.ausgabe = ausgabe;
	}
	public String getContentText() {
		return contentText;
	}
	
	public void setContentText(String contentText) {
		this.contentText = contentText;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public void setDate(String date) {
		this.date = date;
	} 
	
	@Override
	public String toString() {
		return "Entry [ausgabe=" + ausgabe + 
				", date=" + date + ", contentText="
				+ contentText + ", url=" + url  + "]";
	}
}
