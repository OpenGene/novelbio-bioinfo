package com.novelbio.analysis.seq.fastq;

public class FQrecordFilterModifyQuality extends FQrecordFilter {
	/** 是否要修改fastq的quality值，将其变成最大 */
	boolean isModifyQuality;
	@Override
	protected int trimLeft(FastQRecord fastQRecord) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int trimRight(FastQRecord fastQRecord) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isUsing() {
		// TODO Auto-generated method stub
		return false;
	}

}
