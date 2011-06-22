package com.novelbio.analysis.seq;

import java.io.BufferedReader;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;
/**
 * ��ȡ�����ļ����ࡣ
 * �����ļ��й̶��ĸ�ʽ����Fasta��ʽ��ͬ��
 * fasta��ʽֻҪ��ͷ��>���ɣ��������ȡ�Ĳ����ļ�ÿ�ж�Ҫ�й̶��ĺ��壬
 * Ҳ����ÿ�����ж��й̶�������
 * @author zong0jie
 *
 */
public abstract class Seq {
	TxtReadandWrite txtSeqFile = new TxtReadandWrite();
	String seqFile = "";
	int block = 1;
	
	/**
	 * fastQ�ļ��������������
	 */
	int seqNum = -1;
	
	private static Logger logger = Logger.getLogger(Seq.class);  
	/**
	 * 
	 * @param seqFile
	 * @param block ÿ������ռ���У�Ʃ��fastQ�ļ�ÿ������ռ4��
	 */
	public Seq(String seqFile, int block) {
		this.seqFile = seqFile;
		this.block = block;
	}
	public String getSeqFile() {
		return seqFile;
	}
	/**
	 * ������е�����������˫�˵��ˣ���ֻ����һ�˵Ĳ���������Ҳ����fragment������
	 * �������С��0��˵������
	 * @throws Exception 
	 */
	public int getSeqNum(){
		if (seqNum >= 0) {
			return seqNum;
		}
		txtSeqFile.setParameter(seqFile, false, true);
		int readsNum = 0;
		try {
			readsNum =  txtSeqFile.ExcelRows()/block;
			txtSeqFile.close();
		} catch (Exception e) {
			logger.error(seqFile + " may not exist " + e.toString());
			return -1;
		}
		seqNum = readsNum;
		return seqNum;
	}
	/**
	 * �ݶ���ȡ����Gradient
	 * @param block 
	 * @param percent �ٷֱȣ��� 0-100
	 * @param outFile
	 * @throws Exception
	 */
	public void getGradTxt( int[] percent,String outFile) throws Exception {
		txtSeqFile.setParameter(seqFile, false, true);
		for (int i = 0; i < percent.length; i++) {
			if (percent[i]>100) {
				percent[i] = 100;
			}
		}
		ArrayList<TxtReadandWrite> lstxtWrite = new ArrayList<TxtReadandWrite>();
		for (int i = 0; i < percent.length; i++) {
			TxtReadandWrite txtWrite = new TxtReadandWrite();
			txtWrite.setParameter(outFile+percent[i], true, false);
			lstxtWrite.add(txtWrite);
		}
		int rowAllNum = txtSeqFile.ExcelRows();
		BufferedReader reader = txtSeqFile.readfile();
		String content = "";
		int rowNum = 0;
		while ((content = reader.readLine()) != null) {
			for (int i = 0; i < percent.length; i++) {
				 int tmpNum =percent[i]*(rowAllNum/block)*block;
				if (rowNum<tmpNum/100) {
					lstxtWrite.get(i).writefile(content+"\n");
				}
			}
			rowNum++;
		}
		for (TxtReadandWrite txtReadandWrite : lstxtWrite) {
			txtReadandWrite.close();
		}
		txtSeqFile.close();
	}
	
}
