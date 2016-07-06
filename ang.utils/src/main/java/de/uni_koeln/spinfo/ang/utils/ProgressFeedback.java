package de.uni_koeln.spinfo.ang.utils;

public class ProgressFeedback {

	private String processName;
	private double steps;
	private double step;

	public ProgressFeedback(String processName, long steps) {
		this.steps = (double) steps;
		this.step = 0D;
		this.processName = processName;
		System.out.println("\n");
	}

	public void step() {
		step++;
		System.out.print("\r[ " + processName + " ]\t" + String.format("%.2f", (step / steps) * 100D) + " %");

		if (steps == step)
			System.out.print("\r\n");
	}

	public void end() {
		System.out.print("\r\n");
	}

}
