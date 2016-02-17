package de.uni_koeln.spinfo.ang.benchmark;

public class SimpleBenchmark {
	
	private static final String ERROR_NOT_RUNNING 	= "[BMARK]\tError: No benchmark running";
	private static final String ERROR_RUNNING 		= "[BMARK]\tError: Benchmark currently running";
	private static final String INFO_START			= "[BMARK]\tStarting new benchmark: ";
	
	private long startTime;
	private long stopTime;
	private long stepCount;
	private String benchmarkTitle;
	private boolean running;
	
	
	public void startNewBenchmark(String benchmarkTitle){
		if (running){
			System.err.println(ERROR_RUNNING + " \"" + benchmarkTitle + "\"");
			return;
		}
		
		System.out.println(INFO_START + benchmarkTitle);
		running = true;
		stepCount = 0;
		stopTime = 0;
		this.benchmarkTitle = benchmarkTitle;
		startTime = System.currentTimeMillis();
	}
	
	
	public BenchmarkData stopBenchMark(){
		stopTime = System.currentTimeMillis();
		
		if (!running){
			System.err.println(ERROR_NOT_RUNNING);
			return null;
		}
		
		running = false;
		return new BenchmarkData(benchmarkTitle, startTime, stopTime, stepCount);
	}

	
	public void newStep(){
		if (!running){
			System.err.println(ERROR_NOT_RUNNING);
			return;
		}
		stepCount++;
	}
}
