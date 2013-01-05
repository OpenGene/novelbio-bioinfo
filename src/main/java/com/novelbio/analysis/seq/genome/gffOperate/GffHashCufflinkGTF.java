package com.novelbio.analysis.seq.genome.gffOperate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.model.modgeneid.GeneType;

public class GffHashCufflinkGTF extends GffHashGeneAbs{
	private static Logger logger = Logger.getLogger(GffHashCufflinkGTF.class);
	GffHashGene gffHashRef;
	double likelyhood = 0.4;//相似度在0.4以内的转录本都算为同一个基因
	String transcript = "transcript";

	HashMap<String, GffGeneIsoInfo> mapID2Iso = new HashMap<String, GffGeneIsoInfo>();
	
	/** 记录转录本名字和基因名字的对照表，用于最后将GffDetailGene的名字换位基因名字
	 * key为小写
	 */
	HashMap<String, String> mapIsoName2GeneName = new HashMap<String, String>();
	
	/**
	 * 设定参考基因的Gff文件
	 * @param gffHashRef
	 */
	public void setGffHashRef(GffHashGene gffHashRef) {
		this.gffHashRef = gffHashRef;
	}
	
	@Override
	protected void ReadGffarrayExcepTmp(String gfffilename) throws Exception {
		mapChrID2ListGff = new LinkedHashMap<String, ListGff>();
		HashMap<String, ArrayList<GffGeneIsoInfo>> mapChrID2LsIso = new HashMap<String, ArrayList<GffGeneIsoInfo>>();
		
		TxtReadandWrite txtgff = new TxtReadandWrite(gfffilename, false);
		// 基因名字
		ArrayList<GffGeneIsoInfo> lsGeneIsoInfos = null;
		
		GffGeneIsoInfo gffGeneIsoInfo = null;
		String tmpChrID = "";
		String tmpTranscriptNameLast = "";
		for (String content : txtgff.readlines() ) {
			if (content.charAt(0) == '#') {
				continue;
			}
			if (content.contains("NM_001253689")) {
				logger.error("stop");
			}
			String[] ss = content.split("\t");// 按照tab分开
			
			// 新的染色体
			if (!tmpChrID.equals(ss[0].toLowerCase()) ) {
				tmpChrID = ss[0].toLowerCase();
				lsGeneIsoInfos = getChrID2LsGffGeneIso(tmpChrID, mapChrID2LsIso);
			}
			
			String[] isoName2GeneName = getIsoName2GeneName(ss[8]);
			String tmpTranscriptName = isoName2GeneName[0];
			mapIsoName2GeneName.put(tmpTranscriptName.toLowerCase(), isoName2GeneName[1]);
			
			if (ss[2].equals(transcript) 
				||
				(!tmpTranscriptName.equals(tmpTranscriptNameLast) 
						&& !mapID2Iso.containsKey(tmpTranscriptName) )
					) 
			{
				boolean cis = getLocCis(ss[6], tmpChrID, Integer.parseInt(ss[3]), Integer.parseInt(ss[4]));
				gffGeneIsoInfo = GffGeneIsoInfo.createGffGeneIso(tmpTranscriptName, GeneType.mRNA, cis);
				mapID2Iso.put(tmpTranscriptName, gffGeneIsoInfo);
				lsGeneIsoInfos.add(gffGeneIsoInfo);
				if (ss[2].equals(transcript)) {
					continue;
				}
				tmpTranscriptNameLast = tmpTranscriptName;
			}
			if (ss[2].equals("exon")) {
				gffGeneIsoInfo = mapID2Iso.get(tmpTranscriptName);
				gffGeneIsoInfo.addExon( Integer.parseInt(ss[3]), Integer.parseInt(ss[4]));
			}
		}
		CopeChrIso(mapChrID2LsIso);
		txtgff.close();
		mapID2Iso = null;
	}
	
	private ArrayList<GffGeneIsoInfo> getChrID2LsGffGeneIso(String chrID, HashMap<String, ArrayList<GffGeneIsoInfo>> mapChrID2LsIso) {
		 ArrayList<GffGeneIsoInfo> lsGeneIsoInfos = null;
		if (!mapChrID2LsIso.containsKey(chrID)) {
			lsGeneIsoInfos = new ArrayList<GffGeneIsoInfo>();
			mapChrID2LsIso.put(chrID,lsGeneIsoInfos);
		}
		else {
			lsGeneIsoInfos = mapChrID2LsIso.get(chrID);
		}
		return lsGeneIsoInfos;
	}
	
	private String[] getIsoName2GeneName(String ss8) {
		String[] iso2geneName = new String[2];
		 String[] info = ss8.split(";");
		 for (String name : info) {
			if (name.contains("transcript_id")) {
				iso2geneName[0] = name.replace("transcript_id", "").replace("\"", "").trim();
			} else if (name.contains("gene_id")) {
				iso2geneName[1] = name.replace("gene_id", "").replace("\"", "").trim();
			}
		}
		 return iso2geneName;
	}
	
	private void CopeChrIso(HashMap<String, ArrayList<GffGeneIsoInfo>> hashChrIso) {
		ArrayList<String> lsChrID = ArrayOperate.getArrayListKey(hashChrIso);
		for (String chrID : lsChrID) {
			ArrayList<GffGeneIsoInfo> arrayList = hashChrIso.get(chrID);
			copeChrInfo(chrID, arrayList);
		}
	}
	
	/**
	 * 整理某条染色体的信息
	 */
	private void copeChrInfo(String chrID, List<GffGeneIsoInfo> lsGeneIsoInfos) {
		ListGff lsResult = new ListGff();
		lsResult.setName(chrID);
		
		if (lsGeneIsoInfos == null)// 如果已经存在了LOCList，也就是前一个LOCList，那么截短并装入LOCChrHashIDList
			return;
		//排序
		Collections.sort(lsGeneIsoInfos, new Comparator<GffGeneIsoInfo>() {
			public int compare(GffGeneIsoInfo o1, GffGeneIsoInfo o2) {
				Integer o1Start = o1.getStartAbs();
				Integer o2Start = 0;
				o2Start = o2.getStartAbs();

				return o1Start.compareTo(o2Start);
			}
		});
		//依次装入gffdetailGene中
		GffDetailGene gffDetailGene = null;
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGeneIsoInfos) {
			gffGeneIsoInfo.sort();
			if (gffDetailGene == null) {
				gffDetailGene = new GffDetailGene(lsResult, gffGeneIsoInfo.getName(), gffGeneIsoInfo.isCis5to3());
				gffDetailGene.addIso(gffGeneIsoInfo);
				lsResult.add(gffDetailGene);
				continue;
			}
			
			double[] gffIsoRange = new double[]{gffGeneIsoInfo.getStartAbs(), gffGeneIsoInfo.getEndAbs()};
			double[] gffGeneRange = new double[]{gffDetailGene.getStartAbs(), gffDetailGene.getEndAbs()};
			double[] compResult = ArrayOperate.cmpArray(gffIsoRange, gffGeneRange);
			if (compResult[2] > GffDetailGene.OVERLAP_RATIO || compResult[3] > GffDetailGene.OVERLAP_RATIO
					||
					gffDetailGene.getSimilarIso(gffGeneIsoInfo, likelyhood) != null
					) {
				gffDetailGene.addIso(gffGeneIsoInfo);
			}
			else {
				String geneName = mapIsoName2GeneName.get(gffGeneIsoInfo.getName().toLowerCase());
				if (geneName == null) {
					geneName = gffGeneIsoInfo.getName();
				}
				if (gffGeneIsoInfo.getName().equals("NM_001253689")) {
					logger.error("stop");
				}
				gffDetailGene = new GffDetailGene(lsResult, geneName, gffGeneIsoInfo.isCis5to3());
				gffDetailGene.addIso(gffGeneIsoInfo);
				lsResult.add(gffDetailGene);
				continue;
			}
		}
		mapChrID2ListGff.put(chrID, lsResult);
	}
	/**
	 * 给定chrID和坐标，返回该点应该是正链还是负链
	 * 如果不清楚正负链且没有给定相关的refGff，则直接返回true
	 * @param chrID
	 * @param LocID
	 * @return
	 */
	private boolean getLocCis(String ss, String chrID, int LocIDStart, int LocIDEnd) {
		if (ss.equals("+")) {
			return true;
		}
		else if (ss.equals("-")) {
			return false;
		}
		else {
			if (gffHashRef == null) {
				return true;
			}
			int LocID = (LocIDStart + LocIDEnd )/2;
			GffCodGene gffCodGene = gffHashRef.searchLocation(chrID, LocID);
			if (gffCodGene == null) {
				return true;
			}
			if (gffCodGene.isInsideLoc()) {
				return gffCodGene.getGffDetailThis().isCis5to3();
			}
			else {
				int a = Integer.MAX_VALUE;
				int b = Integer.MAX_VALUE;
				if (gffCodGene.getGffDetailUp() != null) {
					a = gffCodGene.getGffDetailUp().getCod2End(LocID);
				}
				if (gffCodGene.getGffDetailDown() != null) {
					b = gffCodGene.getGffDetailDown().getCod2Start(LocID);
				}
				if (a < b) {
					return gffCodGene.getGffDetailUp().isCis5to3();
				}
				else {
					return gffCodGene.getGffDetailDown().isCis5to3();
				}
			}
		}
	}
	
}
