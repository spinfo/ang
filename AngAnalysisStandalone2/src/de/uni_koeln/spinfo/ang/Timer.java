package de.uni_koeln.spinfo.ang;


public class Timer {
	
	private long time;
	
	public void start(){
		time = System.currentTimeMillis();
	}
	
	public long stop(){
		return System.currentTimeMillis() - time;
	}
	
	public String stopAndFormat(){
		long dur = System.currentTimeMillis() - time;
		return format(dur);
	}
	
	public String format(long millis){
	    double curr = millis;
	    //sec
	    curr /= 1000d;
	    if (curr < 60) return curr + " sec";
	    //min
	    curr /= 60d; 
	    if (curr < 60) return curr + " min";
	    //h
	    curr /= 60d;
	    if (curr < 24) return curr + " h";
	    //days
	    curr /= 24d;
	    return curr + " d";
	}

}
