package de.uni_koeln.spinfo.ang.benchmark;

public class BenchmarkData {
	
	private String title;
	private long startTime;
	private long stopTime;
	private long markers;
	
	
	public BenchmarkData(String title, long startTime, long stopTime, long markers) {
		super();
		this.title = title;
		this.startTime = startTime;
		this.stopTime = stopTime;
		this.markers = markers;
	}
	
	
	public String getReport(){
		return toString();
	}
	
	
	public void printReport(){
		System.out.println(toString());
	}
	
	
	@Override
	public String toString(){
		return "[BMARK]\tBenchmark \"" + title + "\" finished in "
				+ getReadableTimeString((float)(stopTime - startTime)) + " (" + markers + " markers set).";
	}
	
	
	public String getRecordedTimeAsString(){
		return getReadableTimeString((float)(stopTime - startTime));
	}
	
	
	private String getReadableTimeString(float millis){
		float sec = millis / (float)1000;
		if (sec > 86400) return sec/(float)60/(float)60/(float)24 + " days";
		if (sec > 3600) return sec/(float)60/(float)60 + " hours";
		if (sec > 60) return sec/(float)60 + " minutes";
		else return sec + " seconds";
	}


	public String getTitle() {
		return title;
	}


	public long getStartTime() {
		return startTime;
	}


	public long getStopTime() {
		return stopTime;
	}


	public long getMarkerCount() {
		return markers;
	}
	
}
