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

/** ָ��һϵ�е�snpλ�㣬�Լ����pileup�ļ��������Щλ���ʵ��reads���� */
public class SnpDetailGet extends RunProcess<SnpFilterDetailInfo> {
	private static Logger logger = Logger.getLogger(SnpDetailGet.class);
	
	GffChrAbs gffChrAbs;

	/** keyΪСд */
	Map<String, ArrayList<RefSiteSnpIndel>> mapChrID2LsSnpSite = new HashMap<String, ArrayList<RefSiteSnpIndel>>();
	/** ������Ϣ */
	LinkedHashMap<String, String> mapSample2PileupFile = new LinkedHashMap<String, String>();
	/** ��ȡ�ļ�ʱȥ���ظ�snpλ�� */
	HashSet<String> setSnpSiteRemoveFromReading = new HashSet<String>();
	String outFile;
	
	/** ͳ������ */
	long readLines;
	long readByte;
	int findSnp;
	
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	
	/** �������snp�������ļ� */
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
	/** ����ļ� */
	public void setOutFile(String outFile) {
		this.outFile = outFile;
	}
	/**������KΪ��λ�Ĺ����ļ����ܺͣ�gz���ļ��ͻ�ӱ�����
	 * @return
	 */
	public double getFileSizeEvaluateK() {
		double allFileSize = 0;
		for (String pileUpFile : mapSample2PileupFile.values()) {
			double size = FileOperate.getFileSize(pileUpFile);
			//�����ѹ���ļ��ͼ���Դ�ļ�Ϊ6���� */
			if (FileOperate.getFileNameSep(pileUpFile)[1].toLowerCase().equals("gz"))
				size = size * 6;
			else
				size = size * 1.2;
			
			allFileSize = allFileSize + size;
		} 
		return allFileSize;
	}
	/** ���ļ�����lsSnpSite
	 * @param snpFile
	 * @param colChrID ʵ������
	 * @param colSiteStart ʵ������
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
	
	/** �Ƿ�Ϊ�ظ�snp */
	private boolean isReplicateSnpSite(String chrID, int snpSite) {
		String siteInfo = chrID.toLowerCase() + SepSign.SEP_ID + snpSite;
		if (setSnpSiteRemoveFromReading.contains(siteInfo)) {
			return true;
		}
		setSnpSiteRemoveFromReading.add(siteInfo);
		return false;
		
	}
	/** �������ļ�����Ϊ<br>
	 * chrID----List--MapInfo<br>
	 * �ĸ�ʽ<br>
	 * @param lsSite
	 * @return
	 */
	public void setMapChrID2InfoSnpIndel(Collection<RefSiteSnpIndel> lsSite) {
		/** ÿ��chrID��Ӧһ��mapinfo��Ҳ����һ��list */
		// ����chrλ��װ��hash��
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
			
			////��ʾ��ȡ��һ���ļ�/////
			setThreadInfo(readLines, readByte, "reading file " + FileOperate.getFileName(pileupFile));
			//////////////////////////////////////
			getSiteInfo_FromPileUp(sampleName, pileupFile);
		}
		if (outFile != null) {
			writeToFile(outFile);
		}
	}
	/**
	 * ������м���
	 * @param alllines
	 * @param allByte
	 * @param message ����Ҫ��ʾmessage������null
	 */
	private void setThreadInfo(long alllines, double allByte, String message) {
		SnpFilterDetailInfo snpFilterDetailInfo = new SnpFilterDetailInfo();
		snpFilterDetailInfo.allLines = readLines;
		snpFilterDetailInfo.allByte = readByte;
		snpFilterDetailInfo.showMessage = message;
		setRunInfo(snpFilterDetailInfo);
	}
	
	/** �������snp���� */
	private void sortSnp() {
		setThreadInfo(0, 0, "sorting snps");
		
		for (ArrayList<RefSiteSnpIndel> lsSnpSiteSimples : mapChrID2LsSnpSite.values()) {
			///////////////���߳�ʹ�� ///////////////////////
			suspendCheck();
			if (flagStop) {
				break;
			}
			/////////////////////////////////////////////////////////
			Collections.sort(lsSnpSiteSimples);
		}
	}
	
	/**
	 * ����ѡ�е�mapInfo����ȡsamtools������pileup file���ÿ��λ��ľ�����Ϣ
	 * @param sampleName �������֡���������mapSortedChrID2LsMapInfo�Ѿ��и�������Ϣ����ô������
	 * @param mapChrID2SortedLsMapInfo LsMapInfo�Ź����list
	 * @param samToolsPleUpFile
	 * @param gffChrAbs
	 * @return �½�һ��hash��Ȼ�󷵻أ����hash��������ı���deep copy��ϵ
	 */
	private void getSiteInfo_FromPileUp(String sampleName, String samToolsPleUpFile) {
		/** ÿ��chrID��Ӧһ��mapinfo��Ҳ����һ��list */
		TxtReadandWrite txtReadSam = new TxtReadandWrite(samToolsPleUpFile, false);
		String tmpChrID = ""; ArrayList<RefSiteSnpIndel> lsMapInfos = null;
		int mapInfoIndex = 0;// ���ν�����ȥ
		for (String samtoolsLine : txtReadSam.readlines()) {
			/////////////////////���߳�ʹ�� /////////////////////////////////////////
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
//					logger.info("����δ֪ chrID��" + tmpChrID);
					continue;
				}
			}
			//����lsMapInfos�е���Ϣ�����������
			if (lsMapInfos == null || mapInfoIndex >= lsMapInfos.size()) continue;

			//һ��һ������ȥ��ֱ���ҵ�����Ҫ��λ��
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
		for (ArrayList<RefSiteSnpIndel> lsRefSiteSnpIndel : mapChrID2LsSnpSite.values()) {//ÿ��Ⱦɫ��
			for (RefSiteSnpIndel refSiteSnpIndel : lsRefSiteSnpIndel) {//ÿ��λ��
				ArrayList<String[]> lsResult = refSiteSnpIndel.toStringLsSnp(mapSample2PileupFile.keySet(), false);
				for (String[] strings : lsResult) {//ÿ��λ�������ڵ�snp
					txtWrite.writefileln(strings);
				}
			}
		}
		txtWrite.close();
	}
	
	/** ɨ���˵���û��reads��pileupline */
	private void setPassedSnp(String sampleName, ArrayList<RefSiteSnpIndel> lsMapInfos, int mapInfoIndex) {
		RefSiteSnpIndel refSiteSnpIndel = lsMapInfos.get(mapInfoIndex);
		refSiteSnpIndel.setSampleName(sampleName);
		refSiteSnpIndel.setSearchSamPileUpFileTrue();
	}
	/** �������ж� loc�Ƿ��뵱ǰ��refSiteSnpIndelλ��һ�� */
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