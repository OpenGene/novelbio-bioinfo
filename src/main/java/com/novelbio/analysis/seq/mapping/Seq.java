package com.novelbio.analysis.seq.mapping;

import java.io.BufferedReader;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;

public abstract class Seq {
	TxtReadandWrite txtSeqFile = new TxtReadandWrite();
	String seqFile = "";
	int block = 1;
	
	/**
	 * fastQ文件里面的序列数量
	 */
	int seqNum = -1;
	
	private static Logger logger = Logger.getLogger(Seq.class);  
	/**
	 * 
	 * @param seqFile
	 * @param block 每个序列占几行，譬如fastQ文件每个序列占4行
	 */
	public Seq(String seqFile, int block) {
		this.seqFile = seqFile;
		this.block = block;
	}
	public String getFileName() {
		return seqFile;
	}
	/**
	 * 获得序列的数量，不管双端单端，都只返回一端的测序数量，也就是fragment的数量
	 * 如果返回小于0，说明出错
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
	 * 梯度提取序列Gradient
	 * @param block 
	 * @param percent 百分比，从 0-100
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
