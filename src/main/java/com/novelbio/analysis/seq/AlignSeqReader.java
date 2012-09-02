package com.novelbio.analysis.seq;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.base.dataOperate.TxtReadandWrite;

public interface AlignSeqReader {
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
}
