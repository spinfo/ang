package de.uni_koeln.spinfo.ang.proc;

import java.io.Serializable;

public class Stats implements Serializable {

	private static final long serialVersionUID = -8961601067704428917L;

	private String filename;
	private long duration;
	private int total;
	private int german;
	private float percentageDE;
	private int errors;

	public Stats(String filename, long duration, int total, int german, float percentageDE, int errors) {
		this.filename = filename;
		this.duration = duration;
		this.total = total;
		this.german = german;
		this.percentageDE = percentageDE;
		this.errors = errors;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public long getDuration() {
		return duration;
	}

	public int getTotal() {
		return total;
	}

	public int getGerman() {
		return german;
	}

	public float getPercentageDE() {
		return percentageDE;
	}

	public int getErrors() {
		return errors;
	}

	@Override
	public String toString() {

		StringBuffer buffer = new StringBuffer();

		buffer.append(filename);
		buffer.append("\n");
		buffer.append("duration: ");
		buffer.append(duration);
		buffer.append("\n");
		buffer.append("total: ");
		buffer.append(total);
		buffer.append("\n");
		buffer.append("german: ");
		buffer.append(german);
		buffer.append("\n");
		buffer.append("percentage DE: ");
		buffer.append(percentageDE);
		buffer.append("\n");
		buffer.append("errors: ");
		buffer.append(errors);
		buffer.append("\n");
		return buffer.toString();
	}

}
