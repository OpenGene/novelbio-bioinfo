package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.GffChrAbs;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.database.domain.geneanno.SepSign;
/** 单个pileup文件的snp calling */
public class SnpCalling extends RunProcess<SnpFilterDetailInfo>{
	private static Logger logger = Logger.getLogger(SNPGATKcope.class);
	
	public static final int Homo = 5;
	public static final int HetoLess = 10;
	public static final int Heto = 20;
	public static final int HetoMore = 30;

	/** 主要写snp的基因信息 */
	GffChrAbs gffChrAbs;
	
	/** 从pileup文本中获取snp的信息
	 * 0:SamleName
	 * 1:SampleFile
	 * 2:OutputFile
	 *  */
	ArrayList<String[]> lsSample2PileUpFiles = new ArrayList<String[]>();
	
	/** 用于多个样本的snp去冗余的，其中key表示该snp所在的起点信息，value就是该位点具体的snp情况 */
	HashMap<String, MapInfoSnpIndel> mapSiteInfo2MapInfoSnpIndel;
	
	/** 用来过滤样本的 */
	SnpFilter sampleFilter = new SnpFilter();
	SnpGroupInfoFilter SnpGroupInfoFilter = new SnpGroupInfoFilter();
	
	long readLines;
	long readByte;
	int findSnp;
	
	TxtReadandWrite txtSnpOut;
	
	/** 找到的snp与名字会装到这个里面 */
	public void setMapSiteInfo2MapInfoSnpIndel(HashMap<String, MapInfoSnpIndel> mapSiteInfo2MapInfoSnpIndel) {
		this.mapSiteInfo2MapInfoSnpIndel = mapSiteInfo2MapInfoSnpIndel;
	}
	/**
	 * @param snpLevel Homo，HetoLess等
	 */
	public void setSnpLevel(int snpLevel) {
		if (snpLevel == Homo) {
			SnpGroupInfoFilter.setSampleRefHomoNum(1, 1);
		}
		else if (snpLevel == HetoLess) {
			SnpGroupInfoFilter.setSampleSnpIndelNum(1, 1);
		}
		else if (snpLevel == Heto) {
			SnpGroupInfoFilter.setSampleSnpIndelNum(1, 1);
			SnpGroupInfoFilter.setSampleSnpIndelHetoLessNum(0, 0);
		}
		else if (snpLevel == HetoMore) {
			SnpGroupInfoFilter.setSampleSnpIndelNum(1, 1);
			SnpGroupInfoFilter.setSampleSnpIndelHetoLessNum(0, 0);
			SnpGroupInfoFilter.setSampleSnpIndelHetoNum(0, 0);
		}
	}
	/** 也可以直接设定SampleDetail */
	public void setSampleDetail(SnpGroupInfoFilter sampleDetail) {
		this.SnpGroupInfoFilter = sampleDetail;
	}
	/**
	 * 可以加入多个文件，只要这些文件有相同的过滤规则就行
	 * @param sampleName
	 * @param pileUpfile
	 * @param outSnpFile 输出文件名，null则不输出，“”输出默认
	 */
	public void setSnpFromPileUpFile(String sampleName, String pileUpfile,String outSnpFile) {
		lsSample2PileUpFiles.add(new String[]{sampleName, pileUpfile, outSnpFile});
	}

	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	/**返回以K为单位的
	 * @return
	 */
	public double getFileSizeK() {
		double allFileSize = 0;
		for (String[] sample2PileupFile : lsSample2PileUpFiles) {
			String pileUpFile = sample2PileupFile[1];
			allFileSize = FileOperate.getFileSize(pileUpFile);
		} 
		return allFileSize;
	}
	@Override
	protected void running() {
		filterSnp();
	}
	
	/** 在设定snp的情况下，从pileup文件中获取snp信息 */
	public void filterSnp() {
		for (String[] sample2PileupFile : lsSample2PileUpFiles) {
			suspendCheck();
			if (flagStop) {
				break;
			}
			
			String outPutFile = sample2PileupFile[2];
			if (outPutFile != null && outPutFile.trim().equals("")) {
				outPutFile = FileOperate.changeFileSuffix(sample2PileupFile[1], "_SnpInfo", "txt");
			}
			addPileupToLsSnpIndel(sample2PileupFile[0], sample2PileupFile[1], outPutFile);
		}
	}
	/** 
	 * 不从vcf，而是从pileUp中获取snp的方法
	 * 将pileUp的snp信息加入mapSiteInfo2MapInfoSnpIndel中
	 * 同时导出一份snp的信息表
	 * @param sampleName
	 * @param SnpGroupInfoFilter 过滤器，设定过滤的状态。本过滤器中的样本信息没有意义，会被清空
	 * @param pileUpFile
	 */
	private void addPileupToLsSnpIndel(String sampleName, String pileUpFile, String outPutFile) {
		txtSnpOut = new TxtReadandWrite(outPutFile, true);
		TxtReadandWrite txtReadPileUp = new TxtReadandWrite(pileUpFile, false);
		setFilter(sampleName);
		
		for (String pileupLines : txtReadPileUp.readlines()) {
			readLines ++;
			readByte += pileUpFile.length();
			////// 中间输出信息 /////////////////////
			suspendCheck();
			if (flagStop)
				break;
			if (readLines%10000 == 0 ) {
				SnpFilterDetailInfo snpFilterDetailInfo = new SnpFilterDetailInfo();
				snpFilterDetailInfo.allLines = readLines;
				snpFilterDetailInfo.allByte = readByte;
				setRunInfo(snpFilterDetailInfo);
			}
			////////////////////////////////////////////////
			MapInfoSnpIndel mapInfoSnpIndel = new MapInfoSnpIndel(gffChrAbs, sampleName);
			mapInfoSnpIndel.setSamToolsPilup(pileupLines);
			
			if (sampleFilter.isFilterdSnp(mapInfoSnpIndel)) {
				addSnp_2_mapSiteInfo2MapInfoSnpIndel(mapInfoSnpIndel);
				writeInFile(mapInfoSnpIndel);
			}
		}
		
		SnpFilterDetailInfo snpFilterDetailInfo = new SnpFilterDetailInfo();
		snpFilterDetailInfo.allLines = readLines;
		snpFilterDetailInfo.allByte = readByte;
		snpFilterDetailInfo.fileName = pileUpFile;
		setRunInfo(snpFilterDetailInfo);
		
		txtSnpOut.close();
	}
	/** 设定过滤器 */
	private void setFilter(String sampleName) {
		SnpGroupInfoFilter.clearSampleName();
		SnpGroupInfoFilter.addSampleName(sampleName);
		sampleFilter.clearSampleFilterInfo();
		sampleFilter.addSampleFilterInfo(SnpGroupInfoFilter);
	}
	/** 将结果装入哈希表里面 */
	private void addSnp_2_mapSiteInfo2MapInfoSnpIndel(MapInfoSnpIndel mapInfoSnpIndel) {
		if (mapSiteInfo2MapInfoSnpIndel == null)
			return;

		String key = mapInfoSnpIndel.getRefID() + SepSign.SEP_ID + mapInfoSnpIndel.getRefSnpIndelStart();
		if (mapSiteInfo2MapInfoSnpIndel.containsKey(key)) {
			MapInfoSnpIndel maInfoSnpIndelExist = mapSiteInfo2MapInfoSnpIndel.get(key);
			maInfoSnpIndelExist.addAllenInfo(mapInfoSnpIndel);
			return;
		}
		else {
			mapSiteInfo2MapInfoSnpIndel.put(key, mapInfoSnpIndel);
		}
	}
	
	private void writeInFile(MapInfoSnpIndel mapInfoSnpIndel) {
		if (txtSnpOut == null) {
			return;
		}
		ArrayList<String[]> lsInfo = mapInfoSnpIndel.toStringLsSnp();
		for (String[] strings : lsInfo) {
			txtSnpOut.writefileln(strings);
		}
	}

}

class SnpFilterDetailInfo {
	/** 所有读取的字节 */
	long allByte;
	long allLines;
	int findSnp;
	/** 当不为null时表示完成了该样本的snp查找 */
	String fileName;
}
