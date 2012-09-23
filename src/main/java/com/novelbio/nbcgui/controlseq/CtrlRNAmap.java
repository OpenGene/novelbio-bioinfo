package com.novelbio.nbcgui.controlseq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.genomeNew.GffChrAbs;
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
	/** tophat是否用GTF文件进行校正，默认为true，如果出错就要考虑不用GTF */
	boolean useGTF = true;
	String outPrefix;
	/** 保存最终结果，只有rsem才会有 */
	ArrayList<ArrayList<String>> lsExpResultRsem = new ArrayList<ArrayList<String>>();
	
	/** 本项目务必第一个设定 */
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
	/** MapTop里面的参数 */
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
			mapRNA.mapReads();
			if (!useGTF) {
				mapRNA.setGtfFile(null);
			}
			setExpResult(prefix, mapRNA);
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
	/** 获得基因表达 */
	private void setExpResult(String prefix, MapRNA mapRNA) {
		if (mapType != RSEM) {
			return;
		}
		MapRsem mapRsem = (MapRsem) mapRNA;
		HashMapLsValue<String, Double> mapGeneID2LsExp = mapRsem.getMapGeneID2LsExp();
		//第一组结果直接装进去
		if (lsExpResultRsem.size() == 0) {
			ArrayList<String> lsTitle = new ArrayList<String>();
			lsTitle.add("GeneID"); lsTitle.add(prefix + "_RPKM");
			lsExpResultRsem.add(lsTitle);
			for (Entry<String, ArrayList<Double>> entry : mapGeneID2LsExp.entrySet()) {
				ArrayList<String> lsDetail = new ArrayList<String>();
				lsDetail.add(entry.getKey());
				lsDetail.add(MathComput.mean(entry.getValue()) + "" );//获得平均数
			}
		}
		//后面的就在hash表里面查
		else {
			lsExpResultRsem.get(0).add(prefix + "_RPKM");
			for (int i = 1; i < lsExpResultRsem.size(); i++) {
				ArrayList<String> lsDetail = lsExpResultRsem.get(i);
				ArrayList<Double> lsValue = mapGeneID2LsExp.get(lsDetail.get(0));
				lsDetail.add(MathComput.mean(lsValue) + "");
			}
		}
	}
	public ArrayList<String[]> getLsExpRsem() {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		for (ArrayList<String> lsTmpResult : lsExpResultRsem) {
			String[] tmpResult = new String[lsTmpResult.size()];
			for (int i = 0; i < tmpResult.length; i++) {
				tmpResult[i] = lsTmpResult.get(i);
			}
			lsResult.add(tmpResult);
		}
		return lsResult;
	}
}
