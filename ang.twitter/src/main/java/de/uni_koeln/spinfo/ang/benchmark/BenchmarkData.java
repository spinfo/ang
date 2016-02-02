package de.uni_koeln.spinfo.ang.benchmark;

public class BenchmarkData {
	
	private String title;
	private long startTime;
	private long stopTime;
	private long stepCount;
	
	
	public BenchmarkData(String title, long startTime, long stopTime, long stepCount) {
		super();
		this.title = title;
		this.startTime = startTime;
		this.stopTime = stopTime;
		this.stepCount = stepCount;
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
				+ (float)((float)(stopTime - startTime)/(float)1000) + " seconds (" + stepCount + " steps).";
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


	public long getStepCount() {
		return stepCount;
	}
	
}
