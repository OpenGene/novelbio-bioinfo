package com.novelbio.analysis.seq.chipseq;

public class RegionValue {
	String name;
	double score;
	double[] values;
	
	protected void setName(String name) {
		this.name = name;
	}
	protected void setScore(double score) {
		this.score = score;
	}
	protected void setValues(double[] values) {
		this.values = values;
	}
	public double[] getValues() {
		return values;
	}
	public String getName() {
		return name;
	}
	public double getScore() {
		return score;
	}
	
}
