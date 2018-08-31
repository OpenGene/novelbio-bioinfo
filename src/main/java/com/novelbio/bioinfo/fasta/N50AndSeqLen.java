package com.novelbio.bioinfo.fasta;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.plot.BarStyle;
import com.novelbio.base.plot.ImageUtils;
import com.novelbio.listoperate.HistList;

/** 计算序列的N50和其他统计长度信息，最后画出柱状图 */
public class N50AndSeqLen {
	public static void main(String[] args) {
		N50AndSeqLen n50AndSeqLen = new N50AndSeqLen("/media/hdfs/nbCloud/public/Testforzong/ToxoDB-10.0_TgondiiME49_Genome.fasta");
		n50AndSeqLen.setLengthStep(500);
		n50AndSeqLen.setMaxContigLen(10000);
		n50AndSeqLen.doStatistics();
		HistList histList = n50AndSeqLen.gethListLength();
		histList.getPlotHistBar(new BarStyle()).saveToFile("/media/hdfs/nbCloud/public/Testforzong/reads Len distribution2.png", 1000, 1000);

		BufferedImage img = histList.getPlotHistBar("SeqStatistics", "Reads Length", "Reads Num").createBufferedImage(1200, 1000);
		ImageUtils.saveBufferedImage(img, "/media/hdfs/nbCloud/public/Testforzong/reads Len distribution.png");
		
		List<String[]> lsInfo = n50AndSeqLen.getLsNinfo();
		TxtReadandWrite txtWrite = new TxtReadandWrite("/media/hdfs/nbCloud/public/Testforzong/N50.txt", true);
		for (String[] strings : lsInfo) {
			txtWrite.writefileln(strings);
		}
		txtWrite.close();
	}
	/** 记录N25，N50等信息 */
	List<String[]> lsNinfo = new ArrayList<String[]>();
	ArrayListMultimap<String, Integer> mapContigName2Length = ArrayListMultimap.create();
 	String seqFileName;
 	/** N50这个统计的步进 */
	int NvalueStep = 5;
	/** contig长度的步进 */
	int lengthStep = 200;
	/** 最短多长的contig，0表示全部统计，200表示统计长度200bp以上的contig */
	int minContigLen = 200;
	/** 最长统计到多长的contig */
	int maxContigLen = 3000;
	/** 最短的contig长度 */
	int realMaxConLen = 0;
	/** 最长的contig长度 */
	int realMinConLen = 0;
	/** 统计contig的平均值 */
	int contigMeanLen = 0;
	/** 统计contig的中位值 */
	int contigMedianLen = 0;
	/** 统计所有contigs的个数 */
	int allContigsNum;
	/** 统计所有contigs的长度 */
	long allContigsLen;
	/** 统计N50的长度 */
	int N50Len = 0;
	/** 统计Contigs长度的中位数 */
	int medianLen = 0;
	
	/** 长度统计 */
	HistList hListLength;
	
	/** 设定后就会计算
	 * @param seqFileName 输入的需要统计N50的文件
	 *  */
	public N50AndSeqLen(String seqFileName) {
		this.seqFileName = seqFileName;
	}
	/** 统计contig的步长，默认为 200 */
	public void setLengthStep(int lengthStep) {
		this.lengthStep = lengthStep;
	}
	/** 最短多长的contig，0表示全部统计，200表示统计长度200bp以上的contig，默认为200 */
	public void setMinContigLen(int minContigLen) {
		this.minContigLen = minContigLen;
	}
	public void setMaxContigLen(int maxContigLen) {
		this.maxContigLen = maxContigLen;
	}
	/** 设定后就会计算 */
	public void setSeqFileName(String seqFileName) {
		this.seqFileName = seqFileName;
		clear();
	}
	/** 设定步长 */
	public void setNvalueStep(int nvalueStep) {
		NvalueStep = nvalueStep;
	}
	
	/** 开始统计 */
	public void doStatistics() {
		getInfo();
		CalculateN50();
		statisticContigLen();
	}
	
	/** 获得N5,N10这种，步进为{@link #setNvalueStep(int)}的N50所有信息 */
	public List<String[]> getLsNinfo() {
		if (lsNinfo.size() > 1) {
			return lsNinfo;
		}
		CalculateN50();
		return lsNinfo;
	}
	
	public HistList gethListLength() {
		return hListLength;
	}
	
	public Integer getLenAvg() {
		return contigMeanLen;
	}
	
	public long getAllContigsLen() {
		return allContigsLen;
	}
	
	public int getAllContigsNum() {
		return allContigsNum;
	}
	
	public int getMinContigLen() {
		return minContigLen;
	}
	public int getMaxContigLen() {
		return maxContigLen;
	}
	
	public int getN50Len() {
		return N50Len;
	}
	
	public int getRealMinConLen() {
		return realMinConLen;
	}
	
	public int getRealMaxConLen() {
		return realMaxConLen;
	}
	
	public int getMedianLen() {
		return medianLen;
	}
	
	private void getInfo() {		
		TxtReadandWrite txtRead = new TxtReadandWrite(seqFileName);
		String tmpName = "";
		int tmpSeqLen = 0;
		for (String string : txtRead.readlines()) {
			string = string.trim();
			if (string.startsWith(">")) {
				summaryInfo(tmpName, tmpSeqLen);
				tmpName = string.replace(">", "").trim();
				tmpSeqLen = 0;
				continue;
			}
			tmpSeqLen += string.trim().replace(" ", "").replace("\t", "").length();
		}
		summaryInfo(tmpName, tmpSeqLen);
		txtRead.close();
	}
	
	private void summaryInfo(String seqName, int seqLen) {
		if (seqLen == 0) return;
		while (mapContigName2Length.containsKey(seqName)) {
			seqName = seqName + "<";
		}
		mapContigName2Length.put(seqName, seqLen);
	}
	
	private void CalculateN50() {
		List<Integer> lsSeqLen = new ArrayList<Integer>(mapContigName2Length.values());
		//降序排序
		Collections.sort(lsSeqLen, new Comparator<Integer>() {
			public int compare(Integer o1, Integer o2) {
				return -o1.compareTo(o2);
			}
		});
		
		medianLen = (int)MathComput.median(lsSeqLen);
		realMaxConLen = lsSeqLen.get(0);
		
		allContigsLen = (long)MathComput.sum(lsSeqLen);
		allContigsNum = lsSeqLen.size();
		contigMeanLen = (int) (allContigsLen/allContigsNum);
		realMinConLen = lsSeqLen.get(allContigsNum - 1);
		
		double tmpN = 0;
		int lastSeqLen = 0;
		int contigNum = 0;
		int Nvalue = NvalueStep;
		for (Integer seqLen : lsSeqLen) {
			contigNum ++;
			tmpN = tmpN + seqLen;
			if (tmpN*100 / allContigsLen >= Nvalue) {
				String[] tmpNvalue = new String[3];
				tmpNvalue[0] = "N" + Nvalue;
				if (lastSeqLen == 0 || tmpN*100 / allContigsLen == Nvalue) {
					tmpNvalue[1] = seqLen + "";
				} else {
					tmpNvalue[1] = lastSeqLen + "";
				}
				tmpNvalue[2] = contigNum + "";
				Nvalue += NvalueStep;
				if (Nvalue > 100) {
					break;
				}
				lsNinfo.add(tmpNvalue);
			}
			lastSeqLen = seqLen;
		}
		if (Nvalue != 100 ) {
			String[] tmpNvalue = new String[3];
			tmpNvalue[0] = "N" + 100;
			tmpNvalue[1] = lastSeqLen + "";
			tmpNvalue[2] = contigNum + "";
			lsNinfo.add(tmpNvalue);
		}
		lsNinfo.add(0, new String[]{"Nvalue", "Length", "ContigNum"});
		String[] arrN50Len = lsNinfo.get(9);
		N50Len = Integer.parseInt(arrN50Len[1]);
	}
	
	private void statisticContigLen() {
		hListLength = HistList.creatHistList("SeqLen", true);
		hListLength.setStartBin(lengthStep*2, lengthStep +"-" + lengthStep*2, lengthStep, lengthStep*2);
		for (int i = lengthStep*3; i < maxContigLen; i+= lengthStep) {
			hListLength.addHistBin(i, (i-lengthStep)+"-"+(i+lengthStep), i);
		}
		hListLength.addHistBin(maxContigLen, ">" + maxContigLen, 200000000);
		for (Integer length : mapContigName2Length.values()) {
			hListLength.addNum(length);
		}
	}
	
	public void clear() {
		/** 记录N25，N50等信息 */
		lsNinfo.clear();
		mapContigName2Length.clear();
		seqFileName = "";
	}
}
