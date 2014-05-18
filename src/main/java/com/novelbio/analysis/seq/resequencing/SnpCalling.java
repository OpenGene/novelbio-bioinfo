package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;
import java.util.Map;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.multithread.RunProcess;
/** 单个pileup文件的snp calling */
public class SnpCalling extends RunProcess<SnpFilterDetailInfo>{
	private static Logger logger = Logger.getLogger(SnpCalling.class);
	
	GffChrAbs gffChrAbs;
	
	/** 从pileup文本中获取snp的信息
	 * 0:SamleName
	 * 1:SampleFile
	 * 2:OutputFile
	 *  */
	ArrayList<String[]> lsSample2PileUpFiles = new ArrayList<String[]>();
	
	/** 用于多个样本的snp去冗余的，其中key表示该snp所在的起点信息，value就是该位点具体的snp情况 */
	Map<String, RefSiteSnpIndel> mapSiteInfo2RefSiteSnpIndel = null;
	Map<String, Align> mapSiteInfo2RefAlign = null;
	
	/** 用来过滤样本的 */
	SnpFilter snpFilter = new SnpFilter();
	
	long readLines, readByte;
	/** 找到的snp数量 */
	int findSnp;
	
	SnpLevel snpLevel = SnpLevel.HeteroMid;
	
	TxtReadandWrite txtSnpOut;
	
	/** 找到的snp与名字会装到这个里面 */
	public void setMapSiteInfo2RefSiteSnpIndel(Map<String, RefSiteSnpIndel> mapSiteInfo2RefSiteSnpIndel) {
		this.mapSiteInfo2RefSiteSnpIndel = mapSiteInfo2RefSiteSnpIndel;
	}
	/** 找到的snp与名字会装到这个里面 */
	public void setMapSiteInfo2RefSiteAlign(Map<String, Align> mapSiteInfo2Align) {
		this.mapSiteInfo2RefAlign = mapSiteInfo2Align;
	}
	/** snp过滤等级 */
	public void setSnpLevel(SnpLevel snpLevel) {
		this.snpLevel = snpLevel;
	}
	
	public void setSnp_Hete_Contain_SnpProp_Min(double setSnp_Hete_Contain_SnpProp_Min) {
		snpFilter.setSnp_Hete_Contain_SnpProp_Min(setSnp_Hete_Contain_SnpProp_Min);
	}
	public void setSnp_HetoMore_Contain_SnpProp_Min(double snp_HetoMore_Contain_SnpProp_Min) {
		snpFilter.setSnp_HetoMore_Contain_SnpProp_Min(snp_HetoMore_Contain_SnpProp_Min);
	}
	/**
	 * 可以加入多个文件，只要这些文件有相同的过滤规则就行
	 * @param sampleName
	 * @param pileUpfile
	 * @param outSnpFile 输出文件名，null则不输出，“”输出默认
	 */
	public void addSnpFromPileUpFile(String sampleName, String pileUpfile,String outSnpFile) {
		lsSample2PileUpFiles.add(new String[]{sampleName, pileUpfile, outSnpFile});
	}
	/** 清空 */
	public void clearSnpFromPileUpFile() {
		lsSample2PileUpFiles.clear();
		readLines = 0;
		readByte = 0;
		findSnp = 0;
	}

	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	/**返回以K为单位的估计文件的总和，gz的文件就会加倍估计
	 * @return
	 */
	public double getFileSizeEvaluateK() {
		ArrayList<String> lsFileName = new ArrayList<String>();
		for (String[] sample2PileupFile : lsSample2PileUpFiles) {
			lsFileName.add( sample2PileupFile[1]);
		} 
		return FileOperate.getFileSizeEvaluateK(lsFileName);
	}
	@Override
	protected void running() {
		filterSnp();
	}
	
	/** 在设定snp的情况下，从pileup文件中获取snp信息 */
	public void filterSnp() {
		for (String[] sample2PileupFile : lsSample2PileUpFiles) {
			 notifyGUI(sample2PileupFile[1]);
			
			suspendCheck();
			if (flagStop) {
				break;
			}
			
			String outPutFile = sample2PileupFile[2];
			if (outPutFile != null && outPutFile.trim().equals("")) {
				outPutFile = FileOperate.changeFileSuffix(sample2PileupFile[1], "_SnpInfo", "txt");
			}
			try {
				addPileupToLsSnpIndel(sample2PileupFile[0], sample2PileupFile[1], outPutFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}
	private void notifyGUI(String pileupFileName) {
		SnpFilterDetailInfo snpFilterDetailInfo = new SnpFilterDetailInfo();
		snpFilterDetailInfo.allLines = readLines;
		snpFilterDetailInfo.allByte = readByte;
		snpFilterDetailInfo.showMessage = "reading file " + FileOperate.getFileName(pileupFileName);
		setRunInfo(snpFilterDetailInfo);
	}
	/** 
	 * 不从vcf，而是从pileUp中获取snp的方法
	 * 将pileUp的snp信息加入mapSiteInfo2RefSiteSnpIndel中
	 * 同时导出一份snp的信息表
	 * @param sampleName
	 * @param SnpGroupFilterInfo 过滤器，设定过滤的状态。本过滤器中的样本信息没有意义，会被清空
	 * @param pileUpFile
	 */
	private void addPileupToLsSnpIndel(String sampleName, String pileupFile, String outPutFile) {
		if (FileOperate.isFileDirectory(FileOperate.getParentPathName(outPutFile))) {
			txtSnpOut = new TxtReadandWrite(outPutFile, true);
		}
		TxtReadandWrite txtReadPileUp = new TxtReadandWrite(pileupFile, false);
		snpFilter.setSampleFilterInfoSingle(snpLevel);
		for (String pileupLines : txtReadPileUp.readlines()) {
			readLines ++;
			readByte += pileupFile.length();
			////// 中间输出信息 /////////////////////
			suspendCheck();
			if (flagStop) break;
			if (readLines%10000 == 0 ) {
				notifyGUI(readLines, readByte);
			}
			////////////////////////////////////////////////
			RefSiteSnpIndel refSiteSnpIndel = new RefSiteSnpIndel(sampleName);
			refSiteSnpIndel.setSamToolsPilup(pileupLines);
			
			ArrayList<SiteSnpIndelInfo> lsFilteredSnp = snpFilter.getFilterdSnp(refSiteSnpIndel);
			if (lsFilteredSnp.size() > 0) {
				if(!writeInFile(refSiteSnpIndel, lsFilteredSnp)) {
					logger.error("出现错误");
				}
				
				if (mapSiteInfo2RefSiteSnpIndel != null) {
					addSnp_2_mapSiteInfo2RefSiteSnpIndel(refSiteSnpIndel);
				}
				if (mapSiteInfo2RefAlign != null) {
					addSnp_2_mapSiteInfo2RefAlign(refSiteSnpIndel);
				}
			}
			refSiteSnpIndel = null;
		}
		txtReadPileUp.close();
		if (txtSnpOut != null) {
			txtSnpOut.close();
		}
	}
	
	private void notifyGUI(long readLines, long readByte) {
		SnpFilterDetailInfo snpFilterDetailInfo = new SnpFilterDetailInfo();
		snpFilterDetailInfo.allLines = readLines;
		snpFilterDetailInfo.allByte = readByte;
		setRunInfo(snpFilterDetailInfo);
		logger.info("readLines:" + readLines);
	}
	
	/** 将结果装入哈希表里面 */
	private void addSnp_2_mapSiteInfo2RefSiteSnpIndel(RefSiteSnpIndel refSiteSnpIndel) {
		String key = refSiteSnpIndel.getKeySiteInfo();
		if (mapSiteInfo2RefSiteSnpIndel.containsKey(key)) {
			RefSiteSnpIndel maInfoSnpIndelExist = mapSiteInfo2RefSiteSnpIndel.get(key);
			maInfoSnpIndelExist.addAllenInfo(refSiteSnpIndel);
		}
		else {
			mapSiteInfo2RefSiteSnpIndel.put(key, refSiteSnpIndel);
			
			int snpNum = mapSiteInfo2RefSiteSnpIndel.size();
			if (snpNum % 10000 == 0) {
				logger.info("tree map size: "+ snpNum);
			}
		}
	}
	
	/** 将结果装入哈希表里面 */
	private void addSnp_2_mapSiteInfo2RefAlign(RefSiteSnpIndel refSiteSnpIndel) {
		String key = refSiteSnpIndel.getKeySiteInfo();
		if (mapSiteInfo2RefAlign.containsKey(key)) return;
		
		Align align = new Align(refSiteSnpIndel.getRefID(), refSiteSnpIndel.refSnpIndelStart, refSiteSnpIndel.refSnpIndelStart);
		mapSiteInfo2RefAlign.put(key, align);
		int snpNum = mapSiteInfo2RefAlign.size();
		if (snpNum % 10000 == 0) {
			logger.info("tree map size: "+ snpNum);
		}
	}
	
	private boolean writeInFile(RefSiteSnpIndel refSiteSnpIndel, ArrayList<SiteSnpIndelInfo> lsFilteredSnp) {
		if (txtSnpOut == null) {
			return true;
		}
		refSiteSnpIndel.setGffChrAbs(gffChrAbs);
		ArrayList<String[]> lsInfo = null;
		try {
			lsInfo = refSiteSnpIndel.toStringLsSnp(lsFilteredSnp, false, true);
		} catch (Exception e) {
			return false;
		}
		
		refSiteSnpIndel.setGffChrAbs(null);
		if (lsInfo.size() == 0) {
			logger.error("error");
		}
		try {
			for (String[] strings : lsInfo) {
				txtSnpOut.writefileln(strings);
			}
			return true;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;
	}

}


