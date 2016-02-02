package de.uni_koeln.spinfo.ang.benchmark;

public class BenchmarkData {
	
	private String title;
	private long startTime;
	private long endTime;
	private long stepCount;
	
	
	public BenchmarkData(String title, long startTime, long endTime, long stepCount) {
		super();
		this.title = title;
		this.startTime = startTime;
		this.endTime = endTime;
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
				+ (double)((endTime - startTime)/1000) + " seconds.";
	}


	public String getTitle() {
		return title;
	}


	public long getStartTime() {
		return startTime;
	}


	public long getEndTime() {
		return endTime;
	}


	public long getStepCount() {
		return stepCount;
	}
	
}
