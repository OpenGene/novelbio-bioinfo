package com.novelbio.bioinfo.base;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.bioinfo.fastq.FastQ;
import com.novelbio.bioinfo.mappedreads.SiteSeqInfo;

public interface AlignSeq {
	/**
	 * 读取前几行，不影响{@link #readLines()}
	 * @param num
	 * @return
	 */
	public ArrayList<? extends AlignRecord> readHeadLines(int num);
	/**
	 * 读取前几行，不影响{@link #readLines()}
	 * @param num
	 * @return
	 */
	public AlignRecord readFirstLine();

	public Iterable<? extends AlignRecord> readLines();
	/**
	 * 从第几行开始读，是实际行
	 * @param lines 如果lines小于1，则从头开始读取
	 * @return
	 */
	public Iterable<? extends AlignRecord> readLines(int lines);
	
	public AlignSeq sort();
	
	public String getFileName();
	
	public FastQ getFastQ();
	public FastQ getFastQ(String outFileName);
	
	public double getReadPercentage();
	/** 实际读取长度，考虑了压缩在里面 */
	public long getReadByte();
	public void close();
}
