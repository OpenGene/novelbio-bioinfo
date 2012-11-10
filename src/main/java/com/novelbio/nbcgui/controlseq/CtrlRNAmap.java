package com.novelbio.nbcgui.controlseq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.mapping.MapLibrary;
import com.novelbio.analysis.seq.mapping.MapRNA;
import com.novelbio.analysis.seq.mapping.MapRsem;
import com.novelbio.analysis.seq.mapping.MapTophat;
import com.novelbio.analysis.seq.mapping.StrandSpecific;
import com.novelbio.base.HashMapLsValue;
import com.novelbio.base.dataStructure.MathComput;

public class CtrlRNAmap {
	public static final int TOP_HAT = 2;
	public static final int RSEM = 4;
	int mapType;
	MapLibrary mapLibrary;
	StrandSpecific strandSpecific;
	
	int threadNum = 4;
	HashMap<String, ArrayList<ArrayList<FastQ>>> mapPrefix2LsFastq;
	
	MapRNA mapRNA;
	GffChrAbs gffChrAbs;
	/** tophat�Ƿ���GTF�ļ�����У����Ĭ��Ϊtrue����������Ҫ���ǲ���GTF */
	boolean useGTF = true;
	String outPrefix;
	/** �������ս����ֻ��rsem�Ż���
	 * ��һ��Ϊ����
	 * ֮��ÿһ��Ϊ���������
	 *  */
	ArrayList<ArrayList<String>> lsExpResultRsemRPKM = new ArrayList<ArrayList<String>>();
	/** �������ս����ֻ��rsem�Ż���
	 * ��һ��Ϊ����
	 * ֮��ÿһ��Ϊ���������
	 *  */
	ArrayList<ArrayList<String>> lsExpResultRsemCounts = new ArrayList<ArrayList<String>>();
	
	/** ����Ŀ��ص�һ���趨 */
	public void setMapType(int mapType) {
		if (mapType == TOP_HAT) {
			this.mapType = TOP_HAT;
		}
		else if (mapType == RSEM) {
			this.mapType = RSEM;
		}
	}
	public void setMapPrefix2LsFastq(HashMap<String, ArrayList<ArrayList<FastQ>>> mapPrefix2LsFastq) {
		this.mapPrefix2LsFastq = mapPrefix2LsFastq;
	}
	

	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	
	public void setOutPathPrefix(String outPathPrefix) {
		this.outPrefix = outPathPrefix;
	}
	/** MapTop����Ĳ��� */
	public void setStrandSpecifictype(StrandSpecific strandSpecifictype) {
		this.strandSpecific = strandSpecifictype;
	}
	public void setLibrary(MapLibrary mapLibrary) {
		this.mapLibrary = mapLibrary;
	}
	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum;
	}
	public void setIsUseGTF(boolean useGTF) {
		this.useGTF= useGTF;
	}
	public void mapping() {
		lsExpResultRsemRPKM = new ArrayList<ArrayList<String>>();
		lsExpResultRsemCounts = new ArrayList<ArrayList<String>>();
		for (Entry<String, ArrayList<ArrayList<FastQ>>> entry : mapPrefix2LsFastq.entrySet()) {
			if (!creatMapRNA()) {
				return;
			}
			setRefFile();
			String prefix = entry.getKey();
			ArrayList<ArrayList<FastQ>> lsFastqFR = entry.getValue();
			mapRNA.setGffChrAbs(gffChrAbs);
			mapRNA.setLeftFq(lsFastqFR.get(0));
			mapRNA.setRightFq(lsFastqFR.get(1));
			setMapLibrary(mapLibrary);
			mapRNA.setStrandSpecifictype(strandSpecific);
			mapRNA.setThreadNum(threadNum);
			mapRNA.setOutPathPrefix(outPrefix + prefix);
			if (!useGTF) {
				mapRNA.setGtfFile(null);
			}
			mapRNA.mapReads();
			setExpResultCounts(prefix, mapRNA);
			setExpResultRPKM(prefix, mapRNA);
		}
	}
	
	private boolean creatMapRNA() {
		if (mapType == TOP_HAT) {
			mapRNA = new MapTophat();
			return true;
		}
		else if (mapType == RSEM) {
			mapRNA = new MapRsem();
			return true;
		}
		else {
			return false;
		}
	}
	private void setRefFile() {
		if (mapType == TOP_HAT) {
			mapRNA.setFileRef(gffChrAbs.getSpecies().getIndexChr(mapRNA.getBowtieVersion()));
		}
		else {
			mapRNA.setFileRef(gffChrAbs.getSpecies().getRefseqFile());
		}
	}
	private void setMapLibrary(MapLibrary mapLibrary) {
		if (mapLibrary == MapLibrary.SingleEnd) {
			return;
		}
		else if (mapLibrary == MapLibrary.PairEnd) {
			mapRNA.setInsert(450);
		}
		else if (mapLibrary == MapLibrary.MatePair) {
			mapRNA.setInsert(4500);
		}
	}
	/** ��û����� */
	private void setExpResultRPKM(String prefix, MapRNA mapRNA) {
		if (mapType != RSEM) {
			return;
		}
		MapRsem mapRsem = (MapRsem) mapRNA;
		ArrayListMultimap<String, Double> mapGeneID2LsExp = mapRsem.getMapGeneID2LsExp();
		//��һ����ֱ��װ��ȥ
		if (lsExpResultRsemRPKM.size() == 0) {
			ArrayList<String> lsTitleRPKM = new ArrayList<String>();
			lsTitleRPKM.add("GeneID"); lsTitleRPKM.add(prefix + "_RPKM");
			lsExpResultRsemRPKM.add(lsTitleRPKM);
			
			for (String geneID : mapGeneID2LsExp.keySet()) {
				ArrayList<String> lsDetail = new ArrayList<String>();
				
				List<Double> lsValue = mapGeneID2LsExp.get(geneID);
				lsDetail.add(geneID);//��û�����
				lsDetail.add(MathComput.mean(lsValue) + "" );//���ƽ����
				lsExpResultRsemRPKM.add(lsDetail);
			}
		}
		//����ľ���hash�������
		else {
			lsExpResultRsemRPKM.get(0).add(prefix + "_RPKM");
			for (int i = 1; i < lsExpResultRsemRPKM.size(); i++) {
				ArrayList<String> lsDetail = lsExpResultRsemRPKM.get(i);
				List<Double> lsValue = mapGeneID2LsExp.get(lsDetail.get(0));
				lsDetail.add(MathComput.mean(lsValue) + "");
			}
		}
	}
	/** ��û����� */
	private void setExpResultCounts(String prefix, MapRNA mapRNA) {
		if (mapType != RSEM) {
			return;
		}
		MapRsem mapRsem = (MapRsem) mapRNA;
		ArrayListMultimap<String, Integer> mapGeneID2LsCounts = mapRsem.getMapGeneID2LsCounts();
		//��һ����ֱ��װ��ȥ
		if (lsExpResultRsemCounts.size() == 0) {
			ArrayList<String> lsTitleCounts = new ArrayList<String>();
			lsTitleCounts.add("GeneID"); lsTitleCounts.add(prefix + "_Counts");
			lsExpResultRsemCounts.add(lsTitleCounts);
			for (String geneID : mapGeneID2LsCounts.keySet()) {
				List<Integer> lsValue = mapGeneID2LsCounts.get(geneID);

				ArrayList<String> lsDetail = new ArrayList<String>();
				lsDetail.add(geneID);//��û�����
				lsDetail.add((int)MathComput.mean(lsValue) + "" );//���ƽ����
				lsExpResultRsemCounts.add(lsDetail);
			}
		}
		//����ľ���hash�������
		else {
			lsExpResultRsemCounts.get(0).add(prefix + "_Counts");
			for (int i = 1; i < lsExpResultRsemCounts.size(); i++) {
				ArrayList<String> lsDetail = lsExpResultRsemCounts.get(i);
				List<Integer> lsValue = mapGeneID2LsCounts.get(lsDetail.get(0));
				lsDetail.add((int)MathComput.mean(lsValue) + "");
			}
		}
	}
	
	public ArrayList<String[]> getLsExpRsemRPKM() {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		for (ArrayList<String> lsTmpResult : lsExpResultRsemRPKM) {
			String[] tmpResult = new String[lsTmpResult.size()];
			for (int i = 0; i < tmpResult.length; i++) {
				tmpResult[i] = lsTmpResult.get(i);
			}
			lsResult.add(tmpResult);
		}
		return lsResult;
	}
	
	public ArrayList<String[]> getLsExpRsemCounts() {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		for (ArrayList<String> lsTmpResult : lsExpResultRsemCounts) {
			String[] tmpResult = new String[lsTmpResult.size()];
			for (int i = 0; i < tmpResult.length; i++) {
				tmpResult[i] = lsTmpResult.get(i);
			}
			lsResult.add(tmpResult);
		}
		return lsResult;
	}
}
