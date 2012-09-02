package com.novelbio.analysis.seq;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.base.dataOperate.TxtReadandWrite;

public interface AlignSeqReader {
	/**
	 * ��ȡǰ���У���Ӱ��{@link #readLines()}
	 * @param num
	 * @return
	 */
	public ArrayList<? extends AlignRecord> readHeadLines(int num);
	/**
	 * ��ȡǰ���У���Ӱ��{@link #readLines()}
	 * @param num
	 * @return
	 */
	public AlignRecord readFirstLine();

	public Iterable<? extends AlignRecord> readLines();
	/**
	 * �ӵڼ��п�ʼ������ʵ����
	 * @param lines ���linesС��1�����ͷ��ʼ��ȡ
	 * @return
	 */
	public Iterable<? extends AlignRecord> readLines(int lines);
}
