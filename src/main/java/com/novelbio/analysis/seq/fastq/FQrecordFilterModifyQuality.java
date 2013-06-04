package com.novelbio.analysis.seq.fastq;

/** 是否将reads的质量改为最高 */
public class FQrecordFilterModifyQuality extends FQrecordFilter {
	/** 是否要修改fastq的quality值，将其变成最大 */
	boolean isModifyQuality;
	
	/** 是否将reads的质量改为最高 */
	public void setModifyQuality(boolean isModifyQuality) {
		this.isModifyQuality = isModifyQuality;
	}
	
	@Override
	protected int trimLeft(FastQRecord fastQRecord) {
		
		return -1;
	}

	@Override
	protected int trimRight(FastQRecord fastQRecord) {
		
		return -1;
	}
	/**
	 * 成功过滤则返回 true，并且输入的fastQRecord也被过滤
	 * reads质量太差或过滤失败则返回false
	 * @return
	 */
	public boolean copeReads(FastQRecord fastQRecord) {
		fastQRecord.setModifyQuality(isModifyQuality);
		return true;
	}
	
	@Override
	public boolean isUsing() {
		return isModifyQuality;
	}

}
