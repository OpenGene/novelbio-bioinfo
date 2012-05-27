package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;

import org.apache.velocity.app.event.ReferenceInsertionEventHandler.referenceInsertExecutor;
import org.springframework.ui.context.Theme;

import com.novelbio.analysis.seq.FastQOld;
/**
 * ����fastq�ļ�������rsem����
 * @author zong0jie
 *
 */
public class Rsem {
//   rsem-calculate-expression [options] upstream_read_file(s) reference_name sample_name
//	 rsem-calculate-expression [options] --paired-end upstream_read_file(s) downstream_read_file(s) reference_name sample_name
	String rsem_cal_exp = "rsem-calculate-expression ";
	boolean pairend = false;
	/** pairend���϶��ļ�����single end��fastq�ļ� */
	ArrayList<String> lsUpFile = new ArrayList<String>();
	/** pairend���¶��ļ� */
	ArrayList<String> lsDownFile = new ArrayList<String>();
	/** �߳�����Ĭ��Ϊ4 */
	int threadNum = 4;
	/** �Ƿ�Ϊphred64�ĸ�ʽ����Ϊnull��ʱ����Զ��жϣ���������������趨 */
	Boolean phred64 = null;
	boolean strandSpecific = false;
	/**
	 * �������л�����������
	 * @param upFile true����������
	 * false����������
	 */
	public void addFileUp(String fileName) {
		lsUpFile.add(fileName);
	}
	public void addFileDown(String fileName) {
		lsDownFile.add(fileName);
	}
	public void setThreadNum(int threadNum) {
		if (threadNum < 1 ) {
			return;
		}
		this.threadNum = threadNum;
	}
	public void setStrandSpecific(boolean strandSpecific) {
		this.strandSpecific = strandSpecific;
	}
	/**
	 * �������л�����������
	 * @param upFile
	 */
	public void addFile(String fileName) {
		lsUpFile.add(fileName);
	}
	/**
	 * illumina 1.3��ʱ����64����������Ҳ�ĳ�32��
	 * @param phred64
	 */
	public void setPhred64(boolean phred64) {
		this.phred64 = phred64;
	}
	
	/**
	 * �Ƿ�Ϊphred64��ʽ
	 * @return
	 */
	private String getPhred64() {
		if (phred64 == null) {
			FastQOld fastQ = new FastQOld(lsUpFile.get(0), FastQOld.QUALITY_MIDIAN);
			if (fastQ.getOffset() == FastQOld.FASTQ_ILLUMINA_OFFSET) {
				phred64 = true;
			}
			else {
				phred64 = false;
			}
		}
		if (phred64) {
			return "--phred64-quals ";
		} else {
			return "";
		}
	}
	private String getStrandSpecific() {
		if (strandSpecific) {
			return "--strand-specific ";
		}
		return "";
	}
	/**
	 * �����߳���
	 * @return
	 */
	private String getThreadNum() {
		return " -p " + threadNum;
	}
	/**
	 * �����ļ�
	 * @return
	 */
	private String getFileName() {
		String resultFile = "";
		if (lsUpFile.size() == 0 && lsDownFile.size() == 0) {
			return "";
		}
		
		if (lsUpFile.size() > 0) {
			resultFile = lsUpFile.get(0);
		}
		for (int i = 1; i < lsUpFile.size(); i++) {
			resultFile = resultFile + "," + lsUpFile.get(i);
		}
		
		if (lsDownFile.size() == 0) {
			pairend = false;
			return resultFile;
		}
		
		if (pairend)
			resultFile = resultFile + " " + lsDownFile.get(0);
		else
			resultFile = resultFile + "," + lsDownFile.get(0);
		
		for (int i = 1; i < lsDownFile.size(); i++) {
			resultFile = resultFile + "," + lsDownFile.get(i);
		}
		return resultFile;
	}
	
	
}
