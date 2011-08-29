package com.novelbio.analysis.seq.reseq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

import edu.uci.ics.jung.graph.DirectedSparseGraph;

public class SoapsnpInfo  implements Comparable<SoapsnpInfo>{

	Logger logger = Logger.getLogger(LastzAlign.class);
	/**
	 * 目标链的名字
	 */
	String seqName = "";
	/**
	 * 最后在目标序列的起点，实际起点
	 */
	int startSeq = 0;
	
	char refBase = 'N';
	char consensunBase = 'N';
	int scoreQuality = 0;
	char bestBase = 'N';
	int avgQualityScoreOfBestBase = 0;
	int numBestBaseMappedUniq = 0;
	int numBestBaseMapped = 0;
	char bestBase2 = 'N';
	int avgQualityScoreOfBestBase2 = 0;
	int numBestBaseMappedUniq2 = 0;
	int numBestBaseMapped2 = 0;
	int depthOfBase = 0;
	double pvalue = 100;
	double avgCopyNumOfNearbyRegion = 0;
	/**
	 * 是否被dbsnp收录
	 */
	boolean booDBsnp = false;
	/**
	 * 文件名
	 */
	public String getSeqName() {
		return seqName;
	}
	public int getStart() {
		return startSeq;
	}
	public char getBestBase() {
		return bestBase;
	}
	/**
	 * 指定align文本，读取信息
	 */
	public static  ArrayList<SoapsnpInfo> readInfo(String soapsnpFile) {
		ArrayList<SoapsnpInfo> lsSoapsnpInfos = new ArrayList<SoapsnpInfo>();
		
		TxtReadandWrite txtSnp = new TxtReadandWrite(soapsnpFile, false);
		//很有可能没东西，也就是lsInfo.size == 0
		ArrayList<String> lsInfo = txtSnp.readfileLs();
		for (String string : lsInfo) {
			SoapsnpInfo soapsnpInfo = new SoapsnpInfo(string);
			lsSoapsnpInfos.add(soapsnpInfo);
		}
		return lsSoapsnpInfos;
	}
	/**
	 * soapsnp的每一行
	 * @param value
	 */
	private SoapsnpInfo(String value)
	{
		setParam(value);
	}
	
	private void setParam(String value)
	{
		String[] ss = value.split("\t");
		seqName = ss[0];
		startSeq = Integer.parseInt(ss[1]);
		refBase = ss[2].trim().charAt(0);
		consensunBase = ss[3].trim().charAt(0);
		scoreQuality = Integer.parseInt(ss[4]);
		bestBase = ss[5].trim().charAt(0);
		avgQualityScoreOfBestBase = Integer.parseInt(ss[6]);
		numBestBaseMappedUniq = Integer.parseInt(ss[7]);
		numBestBaseMapped = Integer.parseInt(ss[8]);
		bestBase2 = ss[9].trim().charAt(0);
		avgQualityScoreOfBestBase2 = Integer.parseInt(ss[10]);
		numBestBaseMappedUniq2 = Integer.parseInt(ss[11]);
		numBestBaseMapped2 = Integer.parseInt(ss[12]);
		depthOfBase = Integer.parseInt(ss[13]);
		pvalue = Double.parseDouble(ss[14]);
		avgCopyNumOfNearbyRegion = Double.parseDouble(ss[15]);
		booDBsnp = !ss[16].equals("0");
	}
	@Override
	public int compareTo(SoapsnpInfo o) {
		Integer a = startSeq;
		Integer b = o.getStart();
		return a.compareTo(b);
	}
}
