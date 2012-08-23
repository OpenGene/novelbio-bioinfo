package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.GffChrAbs;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.SepSign;
/** 单个pileup文件的snp calling */
public class SnpCalling {
	public final int Homo = 5;
	public final int HetoLess = 10;
	public final int Heto = 20;
	public final int HetoMore = 30;
	
	Logger logger = Logger.getLogger(SNPGATKcope.class);
	GffChrAbs gffChrAbs;
	
	/** 从pileup文本中获取snp的信息 */
	ArrayList<String[]> lsSample2PileUpFiles = new ArrayList<String[]>();
	HashMap<String, SampleDetail> setSample2SampleDetail = new HashMap<String, SampleDetail>();
	
	/** 用于多个样本的snp去冗余的，其中key表示该snp所在的起点信息，value就是该位点具体的snp情况 */
	HashMap<String, MapInfoSnpIndel> mapSiteInfo2MapInfoSnpIndel = new HashMap<String, MapInfoSnpIndel>();
	
	
	
	/** 用来过滤样本的 */
	SnpSampleFilter sampleFilter = new SnpSampleFilter();
	SampleDetail sampleDetail = new SampleDetail();
	/**
	 * @param snpLevel Homo，HetoLess等
	 */
	public void setSnpLevel(int snpLevel) {
		if (snpLevel == Homo) {
			sampleDetail.setSampleRefHomoNum(1, 1);
		}
		else if (snpLevel == HetoLess) {
			sampleDetail.setSampleSnpIndelNum(1, 1);
		}
		else if (snpLevel == Heto) {
			sampleDetail.setSampleSnpIndelNum(1, 1);
			sampleDetail.setSampleSnpIndelHetoLessNum(0, 0);
		}
		else if (snpLevel == HetoMore) {
			sampleDetail.setSampleSnpIndelNum(1, 1);
			sampleDetail.setSampleSnpIndelHetoLessNum(0, 0);
			sampleDetail.setSampleSnpIndelHetoNum(0, 0);
		}
	}
	/** 也可以直接设定SampleDetail */
	public void setSampleDetail(SampleDetail sampleDetail) {
		this.sampleDetail = sampleDetail;
	}
	
	public void setSnpFromPileUpFile(String sampleName, SampleDetail sampleDetail, String pileUpfile) {
		setSample2SampleDetail.put(sampleName, sampleDetail);
		lsSample2PileUpFiles.add(new String[]{sampleName, pileUpfile});
	}

	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	/** 
	 * 不从vcf，而是从pileUp中获取snp的方法
	 * 将pileUp的snp信息加入mapSiteInfo2MapInfoSnpIndel中
	 * 同时导出一份snp的信息表
	 * @param sampleName
	 * @param sampleDetail 过滤器，设定过滤的状态
	 * @param pileUpFile
	 */
	private void addPileupToLsSnpIndel(String sampleName, SampleDetail sampleDetail, String pileUpFile) {
		String outPutFile = FileOperate.changeFileSuffix(pileUpFile, "_SnpInfo", "txt");
		TxtReadandWrite txtOut = new TxtReadandWrite(outPutFile, true);
		
		TxtReadandWrite txtReadPileUp = new TxtReadandWrite(pileUpFile, false);
		sampleDetail.clearSampleName();
		sampleDetail.addSampleName(sampleName);
		sampleFilter.clearSampleFilterInfo();
		sampleFilter.addSampleFilterInfo(sampleDetail);
		int snpNum = 0;
		int allNum = 0;
		for (String pileupLines : txtReadPileUp.readlines()) {
			MapInfoSnpIndel mapInfoSnpIndel = new MapInfoSnpIndel(gffChrAbs, sampleName);
			mapInfoSnpIndel.setSamToolsPilup(pileupLines);

			if (sampleFilter.isFilterdSnp(mapInfoSnpIndel)) {
				addSnp_2_mapSiteInfo2MapInfoSnpIndel(mapInfoSnpIndel);
				
				ArrayList<String[]> lsInfo = mapInfoSnpIndel.toStringLsSnp();
				for (String[] strings : lsInfo) {
					txtOut.writefileln(strings);
				}
				
				snpNum++;
				if (snpNum %100 == 0) {
					logger.info("找到" + snpNum + "个snp");
				}
			}
			allNum++;
			if (allNum %100000 == 0) {
				logger.info("扫描过" + allNum + "个snp");
			}
		}
		txtOut.close();
	}
	
	private void addSnp_2_mapSiteInfo2MapInfoSnpIndel(MapInfoSnpIndel mapInfoSnpIndel) {
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
	
	/** 在设定snp的情况下，从pileup文件中获取snp信息 */
	public void readSnpDetailFromPileUp() {
		for (String[] sample2PileupFile : lsSample2PileUpFiles) {
			SampleDetail sampleDetail = setSample2SampleDetail.get(sample2PileupFile[0]);
			addPileupToLsSnpIndel(sample2PileupFile[0], sampleDetail, sample2PileupFile[1]);
		}
	}

}
