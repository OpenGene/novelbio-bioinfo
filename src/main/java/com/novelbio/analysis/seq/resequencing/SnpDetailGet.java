package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.database.domain.geneanno.SepSign;

/** 指定一系列的snp位点，以及多个pileup文件，获得这些位点的实际reads数量 */
public class SnpDetailGet extends RunProcess<SnpFilterDetailInfo> {
	private static Logger logger = Logger.getLogger(SnpDetailGet.class);
	
	GffChrAbs gffChrAbs;

	/** key为小写 */
	Map<String, ArrayList<RefSiteSnpIndel>> mapChrID2LsSnpSite = new HashMap<String, ArrayList<RefSiteSnpIndel>>();
	/** 样本信息 */
	LinkedHashMap<String, String> mapSample2PileupFile = new LinkedHashMap<String, String>();
	/** 读取文件时去除重复snp位点 */
	HashSet<String> setSnpSiteRemoveFromReading = new HashSet<String>();
	String outFile;
	
	/** 统计数字 */
	long readLines;
	long readByte;
	int findSnp;
	
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	
	/** 清空所有snp和输入文件 */
	public void clear() {
		mapChrID2LsSnpSite.clear();
		mapSample2PileupFile.clear();
		setSnpSiteRemoveFromReading.clear();
		readLines = 0;
		readByte = 0;
		findSnp = 0;
	}
	public void addSample2PileupFile(String sampleName, String pileupFileName) {
		mapSample2PileupFile.put(sampleName, pileupFileName);
	}
	/** 输出文件 */
	public void setOutFile(String outFile) {
		this.outFile = outFile;
	}
	/**返回以K为单位的估计文件的总和，gz的文件就会加倍估计
	 * @return
	 */
	public double getFileSizeEvaluateK() {
		double allFileSize = 0;
		for (String pileUpFile : mapSample2PileupFile.values()) {
			double size = FileOperate.getFileSize(pileUpFile);
			//如果是压缩文件就假设源文件为6倍大 */
			if (FileOperate.getFileNameSep(pileUpFile)[1].toLowerCase().equals("gz"))
				size = size * 6;
			else
				size = size * 1.2;
			
			allFileSize = allFileSize + size;
		} 
		return allFileSize;
	}
	/** 将文件读入lsSnpSite
	 * @param snpFile
	 * @param colChrID 实际列数
	 * @param colSiteStart 实际列数
	 */
	public void readSnpSiteInfo(String snpFile, int colChrID, int colSiteStart) {
		colChrID--; colSiteStart--;
		TxtReadandWrite txtReadSnp = new TxtReadandWrite(snpFile, false);
		for (String snpInfo : txtReadSnp.readlines()) {
			String[] ss = snpInfo.split("\t");
			int snpSiteLoc = 0;
			try { snpSiteLoc = Integer.parseInt(ss[colSiteStart]); } catch (Exception e) {
				continue;
			}
			if (isReplicateSnpSite(ss[colChrID], snpSiteLoc))
				continue;
			
			RefSiteSnpIndel snpSiteSimple = new RefSiteSnpIndel(gffChrAbs, ss[colChrID], snpSiteLoc);
			String snpChrID = snpSiteSimple.getRefID().toLowerCase();
			
			ArrayList<RefSiteSnpIndel> lsSnpSiteSimples = mapChrID2LsSnpSite.get(snpChrID);
			if (lsSnpSiteSimples == null) {
				lsSnpSiteSimples = new ArrayList<RefSiteSnpIndel>();
				mapChrID2LsSnpSite.put(snpChrID, lsSnpSiteSimples);
			}
			lsSnpSiteSimples.add(snpSiteSimple);
		}
	}
	
	/** 是否为重复snp */
	private boolean isReplicateSnpSite(String chrID, int snpSite) {
		String siteInfo = chrID.toLowerCase() + SepSign.SEP_ID + snpSite;
		if (setSnpSiteRemoveFromReading.contains(siteInfo)) {
			return true;
		}
		setSnpSiteRemoveFromReading.add(siteInfo);
		return false;
		
	}
	/** 将输入文件整理为<br>
	 * chrID----List--MapInfo<br>
	 * 的格式<br>
	 * @param lsSite
	 * @return
	 */
	public void setMapChrID2InfoSnpIndel(Collection<RefSiteSnpIndel> lsSite) {
		/** 每个chrID对应一组mapinfo，也就是一个list */
		// 按照chr位置装入hash表
		for (RefSiteSnpIndel refSiteSnpIndel : lsSite) {
			ArrayList<RefSiteSnpIndel> lsMap = mapChrID2LsSnpSite.get(refSiteSnpIndel.getRefID().toLowerCase());
			if (lsMap == null) {
				lsMap = new ArrayList<RefSiteSnpIndel>();
				mapChrID2LsSnpSite.put(refSiteSnpIndel.getRefID().toLowerCase(), lsMap);
			}
			lsMap.add(refSiteSnpIndel);
		}
	}
	
	protected void running() {
		sortSnp();
		for (Entry<String, String> entry : mapSample2PileupFile.entrySet()) {
			String sampleName = entry.getKey();
			String pileupFile = entry.getValue();
			
			////表示读取完一个文件/////
			setThreadInfo(readLines, readByte, "reading file " + FileOperate.getFileName(pileupFile));
			//////////////////////////////////////
			getSiteInfo_FromPileUp(sampleName, pileupFile);
		}
		if (outFile != null) {
			writeToFile(outFile);
		}
	}
	/**
	 * 输出的中间结果
	 * @param alllines
	 * @param allByte
	 * @param message 不需要显示message就输入null
	 */
	private void setThreadInfo(long alllines, double allByte, String message) {
		SnpFilterDetailInfo snpFilterDetailInfo = new SnpFilterDetailInfo();
		snpFilterDetailInfo.allLines = readLines;
		snpFilterDetailInfo.allByte = readByte;
		snpFilterDetailInfo.showMessage = message;
		setRunInfo(snpFilterDetailInfo);
	}
	
	/** 将读入的snp排序 */
	private void sortSnp() {
		setThreadInfo(0, 0, "sorting snps");
		
		for (ArrayList<RefSiteSnpIndel> lsSnpSiteSimples : mapChrID2LsSnpSite.values()) {
			///////////////多线程使用 ///////////////////////
			suspendCheck();
			if (flagStop) {
				break;
			}
			/////////////////////////////////////////////////////////
			Collections.sort(lsSnpSiteSimples);
		}
	}
	
	/**
	 * 给定选中的mapInfo，读取samtools产生的pileup file获得每个位点的具体信息
	 * @param sampleName 样本名字。如果输入的mapSortedChrID2LsMapInfo已经有该样本信息，那么就跳过
	 * @param mapChrID2SortedLsMapInfo LsMapInfo排过序的list
	 * @param samToolsPleUpFile
	 * @param gffChrAbs
	 * @return 新建一个hash表然后返回，这个hash表与输入的表是deep copy关系
	 */
	private void getSiteInfo_FromPileUp(String sampleName, String samToolsPleUpFile) {
		/** 每个chrID对应一组mapinfo，也就是一个list */
		TxtReadandWrite txtReadSam = new TxtReadandWrite(samToolsPleUpFile, false);
		String tmpChrID = ""; ArrayList<RefSiteSnpIndel> lsMapInfos = null;
		int mapInfoIndex = 0;// 依次进行下去
		for (String samtoolsLine : txtReadSam.readlines()) {
			/////////////////////多线程使用 /////////////////////////////////////////
			suspendCheck();
			if (flagStop) {
				break;
			}
			readLines ++;
			readByte += samtoolsLine.length();
			if (readLines%10000 == 0 ) {
				setThreadInfo(readLines, readByte, null);
			}
			///////////////////////////////////////////////////////////////////////////////
			
			String[] ss = samtoolsLine.split("\t");
			int loc = Integer.parseInt(ss[1]);
			if (loc == 13438401 && ss[0].equalsIgnoreCase("chr12")) {
				logger.error("stop");
			}
			
			
			if (!ss[0].equalsIgnoreCase(tmpChrID)) {
				
				tmpChrID = ss[0].toLowerCase();
				lsMapInfos = mapChrID2LsSnpSite.get(tmpChrID);
				mapInfoIndex = 0;
				if (lsMapInfos == null) {
//					logger.info("出现未知 chrID：" + tmpChrID);
					continue;
				}
			}
			//所有lsMapInfos中的信息都查找完毕了
			if (lsMapInfos == null || mapInfoIndex >= lsMapInfos.size()) continue;

			//一行一行找下去，直到找到所需要的位点
			if (loc < lsMapInfos.get(mapInfoIndex).getRefSnpIndelStart()) {
				continue;
			} else {
				if (addMapSiteInfo(loc, lsMapInfos, mapInfoIndex, sampleName, samtoolsLine)) {
					mapInfoIndex++;
				}
				else {
					while (mapInfoIndex < lsMapInfos.size()&& loc > lsMapInfos.get(mapInfoIndex).getRefSnpIndelStart()) {
						setPassedSnp(sampleName, lsMapInfos, mapInfoIndex);
						mapInfoIndex++;
					}
					if (mapInfoIndex >= lsMapInfos.size()) {
						continue;
					}
					else if (addMapSiteInfo(loc, lsMapInfos, mapInfoIndex, sampleName, samtoolsLine)) {
						mapInfoIndex++;
					}
				}
			}
		}
		logger.info("readOverFile:" + samToolsPleUpFile);
	}
	
	private void writeToFile(String outFile) {
		setThreadInfo(readLines, readByte, "writeToFile");
		
		TxtReadandWrite txtWrite = new TxtReadandWrite(outFile, true);
		txtWrite.writefileln(RefSiteSnpIndel.getTitleFromSampleName(mapSample2PileupFile.keySet()));
		for (ArrayList<RefSiteSnpIndel> lsRefSiteSnpIndel : mapChrID2LsSnpSite.values()) {//每条染色体
			for (RefSiteSnpIndel refSiteSnpIndel : lsRefSiteSnpIndel) {//每个位点
				ArrayList<String[]> lsResult = refSiteSnpIndel.toStringLsSnp(mapSample2PileupFile.keySet(), false);
				for (String[] strings : lsResult) {//每个位点所存在的snp
					txtWrite.writefileln(strings);
				}
			}
		}
		txtWrite.close();
	}
	
	/** 扫描了但是没有reads的pileupline */
	private void setPassedSnp(String sampleName, ArrayList<RefSiteSnpIndel> lsMapInfos, int mapInfoIndex) {
		RefSiteSnpIndel refSiteSnpIndel = lsMapInfos.get(mapInfoIndex);
		refSiteSnpIndel.setSampleName(sampleName);
		refSiteSnpIndel.setSearchSamPileUpFileTrue();
	}
	/** 会首先判断 loc是否与当前的refSiteSnpIndel位点一致 */
	private boolean addMapSiteInfo(int loc, ArrayList<RefSiteSnpIndel> lsMapInfos, int mapInfoIndex, String sampleName, String samtoolsLine) {
		if (loc != lsMapInfos.get(mapInfoIndex).getRefSnpIndelStart())
			return false;
		RefSiteSnpIndel refSiteSnpIndel = lsMapInfos.get(mapInfoIndex);
		refSiteSnpIndel.setGffChrAbs(gffChrAbs);
		refSiteSnpIndel.setSampleName(sampleName);
		refSiteSnpIndel.setSamToolsPilup(samtoolsLine);
		return true;
	}
	
}