package de.uni_koeln.spinfo.ang.benchmark;

public class SimpleBenchmark {
	
	public static final String ERR_NOT_RUNNING = "[BMARK]\tError: No benchmark running";
	public static final String ERR_RUNNING = "[BMARK]\tError: Benchmark currently running";
	
	private long startTime;
	private long stopTime;
	private long stepCount;
	private String benchmarkTitle;
	private boolean running;
	
	
	public void startNewBenchmark(String benchmarkTitle){
		if (running){
			System.err.println(ERR_RUNNING + " \"" + benchmarkTitle + "\"");
			return;
		}
		
		running = true;
		stepCount = 0;
		stopTime = 0;
		this.benchmarkTitle = benchmarkTitle;
		startTime = System.currentTimeMillis();
	}
	
	
	public BenchmarkData stopBenchMark(){
		stopTime = System.currentTimeMillis();
		
		if (!running){
			System.err.println(ERR_NOT_RUNNING);
			return null;
		}
		
		running = false;
		return new BenchmarkData(benchmarkTitle, startTime, stopTime, stepCount);
	}

	
	public void newStep(){
		if (!running){
			System.err.println(ERR_NOT_RUNNING);
			return;
		}
		stepCount++;
	}
}
