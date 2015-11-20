package de.spinfo.uni_koeln.ang.htmlunit;

import java.io.Serializable;
import java.util.ArrayList;

public class S1Entry implements Serializable{

	private static final long serialVersionUID = 6019734746134450572L;
	
	private String search_term;
	private String date;
	private String pub_date;
	private ArrayList<Integer> pageList = new ArrayList<Integer>();
	private ArrayList<String> titleList = new ArrayList<String>();
	private ArrayList<String> contentList = new ArrayList<String>();
	private StringBuffer buffer = new StringBuffer();
	public String getDate() {
		return date;
	}

	public String getSearch_term() {
		return search_term;
		
	}

	public void setSearch_term(String search_term) {
		this.search_term = search_term;
		buffer.append("\n");
		buffer.append("search_term:");
		buffer.append("\t");
		buffer.append(search_term);
		buffer.append("\n");
	}
	
	
	
	public void setDate(String date) {
		this.date = date;
		buffer.append("Date:");
		buffer.append("\t");
		buffer.append(date);
		buffer.append("\n");
	}

	public String getPub_date() {
		return pub_date;

	}

	public void setPub_date(String pub_date) {
		this.pub_date = pub_date;
		buffer.append("pub_date:");
		buffer.append("\t");
		buffer.append(pub_date);
		buffer.append("\n");
	}



	public ArrayList<Integer> getPage() {
		return pageList;
	}

	public void setPage(Integer page) {
		
		pageList.add(page);
		buffer.append("page:");
		buffer.append("\t");
		buffer.append(page);
		buffer.append("\n\n");
	}

	public ArrayList<String> getTitle() {
		return titleList;
	}

	public void setTitle(String title) {
		titleList.add(title);
		buffer.append("title:");
		buffer.append("\t");
		buffer.append(title);
		buffer.append("\n");
	}

	public ArrayList<String> getContent() {
		return contentList;
	}

	public void setContent(String content) {
		contentList.add(content);
		buffer.append("content:");
		buffer.append("\t");
		buffer.append(content);
		buffer.append("\n");
	}

	public String toString() {
		String entryString = buffer.toString();	
		return entryString ;
	}

}
