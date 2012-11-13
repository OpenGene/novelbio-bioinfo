package com.novelbio.analysis.seq.fastq;

import java.util.concurrent.Callable;


public class FastQrecordFilterRun implements Callable<FastQrecordFilterRun> {
	FastQRecordFilter fastQRecordFilter;
	FastQRecord fastQRecord1;
	FastQRecord fastQRecord2;
	
	String fastqResult1;
	String fastqResult2;
	
	boolean filterSucess = false;
	
	public void setFastQRecordFilter(FastQRecordFilter fastQRecordFilter) {
		this.fastQRecordFilter = fastQRecordFilter;
	}
	/** ������õ���û�о�����ʼ����fastqRecord */
	public void setFastQRecordSE(FastQRecord fastQRecord1) {
		this.fastQRecord1 = fastQRecord1;
	}
	/** ������õ���û�о�����ʼ����fastqRecord */
	public void setFastQRecordPE(FastQRecord fastQRecord1, FastQRecord fastQRecord2) {
		this.fastQRecord1 = fastQRecord1;
		this.fastQRecord2 = fastQRecord2;
	}
	/** �ڲ����fastqRecord���г�ʼ�� */
	@Override
	public FastQrecordFilterRun call() throws Exception {
		try {
			fastQRecord1.initialReadRecord();
			if (fastQRecord2 == null) {
				filterSucess = fastQRecordFilter.filterFastQRecordSE(fastQRecord1);
			} else {
				fastQRecord2.initialReadRecord();
				filterSucess = fastQRecordFilter.filterFastQRecordPE(fastQRecord1, fastQRecord2);
			}
			if (filterSucess) {
				fastqResult1 = fastQRecord1.toString();
				if (fastQRecord2 != null) {
					fastqResult2 = fastQRecord2.toString();
				}
			}
		} catch (Exception e) {
			filterSucess = false;
		}
		
		return this;
	}
	
	public boolean isPairEnd() {
		if (fastQRecord2 == null) {
			return false;
		}
		return true;
	}
	/** �Ƿ���˳ɹ� */
	public boolean isFilterSucess() {
		return filterSucess;
	}
	
	public String getFastQRecord1Filtered() {
		return fastqResult1;
	}
	public String getFastQRecord2Filtered() {
		return fastqResult2;
	}
	
}
