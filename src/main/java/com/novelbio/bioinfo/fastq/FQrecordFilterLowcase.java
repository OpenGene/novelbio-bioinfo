package com.novelbio.bioinfo.fastq;

public class FQrecordFilterLowcase extends FQrecordFilter {
	boolean isFiterLowcase;
	public void setFiterLowcase(boolean isFiterLowcase) {
		this.isFiterLowcase = isFiterLowcase;
	}
	
	@Override
	protected int trimLeft(FastQRecord fastQRecord) {
		char[] info = fastQRecord.seqFasta.toString().toCharArray();
		int numStart = 0;
		//从前向后，遇到小写就计数
		for (char c : info) {
			if ((int)c > 90 )
				numStart++;
			else
				break;
		}
		return numStart;
	}

	@Override
	protected int trimRight(FastQRecord fastQRecord) {
		char[] info = fastQRecord.seqFasta.toString().toCharArray();
		int numEnd = info.length;
		for (int i = info.length - 1; i >= 0; i--) {
			if ((int)info[i] > 90 )
				numEnd--;
			else
				break;
		}
		return numEnd;
	}

	@Override
	public boolean isUsing() {
		return isFiterLowcase;
	}
}
