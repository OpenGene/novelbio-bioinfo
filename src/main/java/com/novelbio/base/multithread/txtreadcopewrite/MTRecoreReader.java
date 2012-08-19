package com.novelbio.base.multithread.txtreadcopewrite;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import com.novelbio.analysis.seq.fastq.FastQRecord;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.multithread.RunProcess;

/** ���߳���һ���̶߳�ȡ�ļ�<br>
 * T: ����ʵ�ʶ�ȡ�����ݣ�Ʃ��String��FastQRecord<br>
 * K: ��T��װ�ã�����absQueue����list�Ķ���<br>
 * 
 * ���̣߳���Ҫ���²��� <br>
 * 1. ��ѭ������� wait_To_Cope_AbsQueue()  �������̣߳�ͬʱ��ֹabsQueue���й���<br>
 * 2. ��ѭ���м�� flagRun ����ֹѭ��<br>
 * 3: ��ѭ������� setRunInfo() ��������ȡ����ʱ���ֵ���Ϣ
 * @author zong0jie
 *
 */
public abstract class MTRecoreReader <T, K extends MTRecordRead> extends RunProcess<K> {
	long readsNum = 0;
		
	protected int maxNumReadInLs = 5000;
	protected AbstractQueue<K> absQueue = new ArrayBlockingQueue<K>(maxNumReadInLs);
	
	
	/** ��ÿ��MTRecordCope�ж��趨����ȡ�࣬ͬʱ�� ��������absQueue���ø�ÿһ��MTRecordCope */
	public void setLsCopedThread(ArrayList<? extends MTrecordCoper<? extends MTRecordCope>> lsCopedRecords) {
		for (MTrecordCoper<? extends MTRecordCope> mtRecordCoper : lsCopedRecords) {
			addFilterReads(mtRecordCoper);
		}
	}
	/** ��ÿ��filterReads�ж��趨����ȡ��,ͬʱ�� ��������absQueue���ø�ÿһ��MTRecordCope */
	public void addFilterReads(MTrecordCoper<? extends MTRecordCope> mtRecordCoper) {
		mtRecordCoper.setReader(this);
		mtRecordCoper.setLsRecords(absQueue);
	}
	public long getReadsNum() {
		if (readsNum == 0) {
			for (T t : readlines()) {
				readsNum++;
			}
		}
		return readsNum;
	}
	public Iterable<T> readlines() {
		return readlines(0);
	}
	/**
	 * ��ȡǰ���У���Ӱ��{@link #readlines()}
	 * @param num
	 * @return
	 */
	public ArrayList<T> readHeadLines(int num) {
		ArrayList<T> lsResult = new ArrayList<T>();
		int i = 0;
		for (T info : readlines()) {
			if (i >= num) {
				break;
			}
			lsResult.add(info);
		}
		return lsResult;
	}
	/** �ȴ������߳̽�AbsQueue�����еļ�¼����� */
	protected void wait_To_Cope_AbsQueue() {
		suspendCheck();
		while (absQueue.size() == maxNumReadInLs) {
			try { Thread.sleep(50); } catch (InterruptedException e) { }
		}
	}
	/**
	 * �ӵڼ��п�ʼ������ʵ����
	 * @param lines ���linesС��1�����ͷ��ʼ��ȡ
	 * @return
	 */
	public abstract Iterable<T> readlines(int lines);
	/** �ر���Ҫ�رյ������ǵ���try��Χ */
	public abstract void close();
	

}
