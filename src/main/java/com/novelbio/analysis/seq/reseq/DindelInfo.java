package com.novelbio.analysis.seq.reseq;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFasta;
import com.novelbio.base.dataOperate.TxtReadandWrite;
/**
 * 保存Dindel软件得到的信息，每一行一个DindelInfo 类
 * @author zong0jie
 *
 */
public class DindelInfo implements Comparable<DindelInfo>{
	/**
	 * 目标链的名字
	 */
	String seqName = "";
	/**
	 * 最后在目标序列的起点，实际起点
	 */
	int startSeq = 0;
	
	String refBase = "";
	String  altBase = "";
	int scoreQuality = 0;
	String filter = "";
	private DindelInfo(String value)
	{
		setParam(value);
	}
	public int getStart() {
		return startSeq;
	}
	
	public int getEnd() {
		return startSeq + refBase.length() - 1;
	}
	
	public String getAltBase() {
		return altBase;
	}
	
	public String getRefBase() {
		return refBase;
	}
	
	/**
	 * 指定align文本，读取信息
	 */
	public static  ArrayList<DindelInfo> readInfo(String dindelInfoFile) {
		ArrayList<DindelInfo> lsdDindelInfos = new ArrayList<DindelInfo>();
		
		TxtReadandWrite txtIndel = new TxtReadandWrite(dindelInfoFile, false);
		//很有可能没东西，也就是lsInfo.size == 0
		ArrayList<String> lsInfo = txtIndel.readfileLs();
		for (String string : lsInfo) {
			if (string.startsWith("#")) {
				continue;
			}
			DindelInfo deDindelInfo = new DindelInfo(string);
			lsdDindelInfos.add(deDindelInfo);
		}
		return lsdDindelInfos;
	}
	
	private void setParam(String value)
	{
		String[] ss = value.split("\t");
		seqName = ss[0];
		startSeq = Integer.parseInt(ss[1]);
		refBase = ss[3].trim();
		altBase = ss[4].trim();
		scoreQuality = Integer.parseInt(ss[5]);
		filter = ss[6].trim();
	}
	@Override
	public int compareTo(DindelInfo o) {
		Integer a = startSeq;
		Integer b = o.getStart();
		return a.compareTo(b);
	}
}
