package com.novelbio.analysis.seq.fastq;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import net.sf.samtools.util.RuntimeEOFException;

import com.novelbio.base.dataOperate.HttpFetch;

/**
 * 放在线程池里面的每一个运行单元
 * @author zong0jie
 *
 */
public class FastQrecordCopeUnit implements Callable<FastQrecordCopeUnit> {
	public static void main(String[] args) {
		HttpFetch.getInstance();
		System.out.println("aaa");
	}
	FastQC fastQC;
	
	/** 左右是一样的，所以最后就是左边拷贝给右边 */
	List<FQrecordCopeInt> lsFQrecordCopeLeft;
	List<FQrecordCopeInt> lsFQrecordCopeRight;
	
	FastQRecord fastQRecord1;
	FastQRecord fastQRecord2;
	
	String fastqResult1;
	String fastqResult2;
	
	boolean filterSucess = false;
	
	
	/** 设定过滤器，左右的过滤器顺序必须一致<br>
	 * 过滤器中先加Fastqc还是先加过滤，需要考虑一下
	 */
	public void setFastQRecordFilter(List<FQrecordCopeInt> lsFQrecordCopeLeft, List<FQrecordCopeInt> lsFQrecordCopeRight) {
		this.lsFQrecordCopeLeft = lsFQrecordCopeLeft;
		this.lsFQrecordCopeRight = lsFQrecordCopeRight;
	}

	/** 这里最好导入没有经过初始化的fastqRecord */
	public void setFastQRecordSE(FastQRecord fastQRecord1) {
		this.fastQRecord1 = fastQRecord1;
	}
	/** 这里最好导入没有经过初始化的fastqRecord */
	public void setFastQRecordPE(FastQRecord fastQRecord2) {
		this.fastQRecord2 = fastQRecord2;
	}
	/** 内部会对fastqRecord进行初始化 */
	@Override
	public FastQrecordCopeUnit call() throws Exception {
		try {
			fastQRecord1.initialReadRecord();
			if (fastQRecord2 == null) {
				filterSucess = copeFastQRecordSE(fastQRecord1);
			} else {
				fastQRecord2.initialReadRecord();
				filterSucess = copeFastQRecordPE(fastQRecord1, fastQRecord2);
			}
			if (filterSucess) {
				fastqResult1 = fastQRecord1.toString();
				if (fastQRecord2 != null) {
					fastqResult2 = fastQRecord2.toString();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			filterSucess = false;
		}
		return this;
	}
	
	/** 没有通过过滤就返回false */
	public boolean copeFastQRecordSE(FastQRecord fastQRecord) {
		if (fastQRecord == null) {
			return false;
		}
		
		boolean filtered = true;
		for (FQrecordCopeInt fQrecordFilter : lsFQrecordCopeLeft) {
			if (!fQrecordFilter.copeReads(fastQRecord)) {
				filtered = false;
				break;
			}
		}
		
		return filtered;
	}
	
	/** 没有通过过滤就返回false */
	public boolean copeFastQRecordPE(FastQRecord fastQRecord1, FastQRecord fastQRecord2) {
		//TODO 考虑判定左右端序列的名字是否一致
		if (fastQRecord1 == null || fastQRecord2 == null) {
			return false;
		}
		boolean filtered = true;
		Iterator<FQrecordCopeInt> itFqCoper = lsFQrecordCopeRight.iterator();
		for (FQrecordCopeInt fQrecordFilter : lsFQrecordCopeLeft) {
			boolean filter1 = fQrecordFilter.copeReads(fastQRecord1);
			boolean filter2 = itFqCoper.next().copeReads(fastQRecord2);
			if (!filter1 || !filter2) {
				filtered = false;
				break;
			}
		}
		return filtered;
	}
	
	public boolean isPairEnd() {
		if (fastQRecord2 == null) {
			return false;
		}
		return true;
	}
	/** 是否过滤成功 */
	public boolean isFilterSucess() {
		return filterSucess;
	}
	
	/** 如果reads有问题，则会返回null */
	public String getFastQRecord1Filtered() {
		return fastqResult1;
	}
	/** 如果reads有问题，则会返回null */
	public String getFastQRecord2Filtered() {
		return fastqResult2;
	}
	
}
