package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.listOperate.ListAbsSearch;
import com.novelbio.database.model.modcopeid.GeneID;

public class GffHashCufflinkGTF extends GffHashGeneAbs{
	GffHashGene gffHashRef;
	/**
	 * 设定参考基因的Gff文件
	 * @param gffHashRef
	 */
	public void setGffHashRef(GffHashGene gffHashRef) {
		this.gffHashRef = gffHashRef;
	}
	
	String transcript = "transcript";
	@Override
	protected void ReadGffarrayExcep(String gfffilename) throws Exception {
		// 实例化四个表
		Chrhash = new LinkedHashMap<String, ListGff>();// 一个哈希表来存储每条染色体
		locHashtable = new LinkedHashMap<String, GffDetailGene>();// 存储每个LOCID和其具体信息的对照表
		LOCIDList = new ArrayList<String>();// 顺序存储每个基因号，这个打算用于提取随机基因号
		HashMap<String, ArrayList<GffGeneIsoInfo>> hashChrIso = new HashMap<String, ArrayList<GffGeneIsoInfo>>();
		TxtReadandWrite txtgff = new TxtReadandWrite(gfffilename, false);
		// 基因名字
		String chrnametmpString = ""; // 染色体的临时名字
		ArrayList<GffGeneIsoInfo> lsGeneIsoInfos = null;
		GffGeneIsoInfo gffGeneIsoInfo = null;
		String tmpChrID = "";
		String tmpTranscriptName = "";
		for (String content : txtgff.readlines() )// 读到结尾
		{
			if (content.charAt(0) == '#')
				continue;
			String[] ss = content.split("\t");// 按照tab分开
			chrnametmpString = ss[0].toLowerCase();// 小写的chrID
			
			// 新的染色体
			if (!tmpChrID.equals(chrnametmpString) )
			{
				tmpChrID = chrnametmpString;
				if (!hashChrIso.containsKey(chrnametmpString)) {
					lsGeneIsoInfos = new ArrayList<GffGeneIsoInfo>();
					hashChrIso.put(chrnametmpString,lsGeneIsoInfos);
				}
				else {
					lsGeneIsoInfos = hashChrIso.get(tmpChrID);
				}
			}
			if (ss[2].equals(transcript)) {
				String isoName = ss[8].split(";")[1].replace("transcript_id", "").replace("\"", "").trim();
				tmpTranscriptName = isoName;
				boolean cis = getLocCis(ss[6], chrnametmpString, Integer.parseInt(ss[3]), Integer.parseInt(ss[4]));
				if (cis) 
					gffGeneIsoInfo = new GffGeneIsoCis(isoName, GffGeneIsoInfo.TYPE_GENE_MRNA);
//					(isoName, chrnametmpString, GffGeneIsoInfo.TYPE_GENE_MRNA);
				else 
					gffGeneIsoInfo = new GffGeneIsoTrans(isoName, GffGeneIsoInfo.TYPE_GENE_MRNA);
				lsGeneIsoInfos.add(gffGeneIsoInfo);
				continue;
			}
			else if (!ss[8].split(";")[1].replace("transcript_id", "").replace("\"", "").trim().equals(tmpTranscriptName)) {
				tmpTranscriptName = ss[8].split(";")[1].replace("transcript_id", "").replace("\"", "").trim();
				boolean cis = getLocCis(ss[6], chrnametmpString, Integer.parseInt(ss[3]), Integer.parseInt(ss[4]));
				if (cis) 
					gffGeneIsoInfo = new GffGeneIsoCis(tmpTranscriptName, GffGeneIsoInfo.TYPE_GENE_MRNA);
				else 
					gffGeneIsoInfo = new GffGeneIsoTrans(tmpTranscriptName, GffGeneIsoInfo.TYPE_GENE_MRNA);
				lsGeneIsoInfos.add(gffGeneIsoInfo);
			}
			gffGeneIsoInfo.addExon( Integer.parseInt(ss[3]), Integer.parseInt(ss[4]));
		}
		CopeChrIso(hashChrIso);
		txtgff.close();
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
		if (lsGeneIsoInfos == null)// 如果已经存在了LOCList，也就是前一个LOCList，那么截短并装入LOCChrHashIDList
			return;
		//排序
		Collections.sort(lsGeneIsoInfos, new Comparator<GffGeneIsoInfo>() {
			@Override
			public int compare(GffGeneIsoInfo o1, GffGeneIsoInfo o2) {
				Integer o1Start = -1;
				if (o1.isCis5to3()) {
					o1Start = o1.get(0).getStartCis();
				}
				else {
					o1Start = o1.get(o1.size() - 1).getEndCis();
				}
				Integer o2Start = -1;
				if (o2.isCis5to3()) {
					o2Start = o2.get(0).getStartCis();
				}
				else {
					o2Start = o2.get(o2.size() - 1).getEndCis();
				}
				return o1Start.compareTo(o2Start);
			}
		});
		//依次装入gffdetailGene中
		GffDetailGene gffDetailGene = null;
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGeneIsoInfos) {
			gffGeneIsoInfo.sort();
			if (gffDetailGene == null) {
				gffDetailGene = new GffDetailGene(chrID, gffGeneIsoInfo.getName(), gffGeneIsoInfo.isCis5to3());
				gffDetailGene.addIso(gffGeneIsoInfo);
				lsResult.add(gffDetailGene);
				locHashtable.put(gffGeneIsoInfo.getName().toLowerCase(), gffDetailGene);
				locHashtable.put(GeneID.removeDot(gffGeneIsoInfo.getName()).toLowerCase(), gffDetailGene);
				locHashtable.put(GeneID.removeDot(gffDetailGene.getName()).toLowerCase(), gffDetailGene);
				continue;
			}
			
			double[] gffIsoRange = new double[]{gffGeneIsoInfo.getStartAbs(), gffGeneIsoInfo.getEndAbs()};
			double[] gffGeneRange = new double[]{gffDetailGene.getStartAbs(), gffDetailGene.getEndAbs()};
			
			double[] compResult = ArrayOperate.cmpArray(gffIsoRange, gffGeneRange);
			if (compResult[2] > GffDetailGene.OVERLAP_RATIO || compResult[3] > GffDetailGene.OVERLAP_RATIO) {
				gffDetailGene.addIso(gffGeneIsoInfo);
				locHashtable.put(gffGeneIsoInfo.getName().toLowerCase(), gffDetailGene);
				locHashtable.put(GeneID.removeDot(gffGeneIsoInfo.getName()).toLowerCase(), gffDetailGene);

			}
			else {
				
				gffDetailGene = new GffDetailGene(chrID, gffGeneIsoInfo.getName(), gffGeneIsoInfo.isCis5to3());
				locHashtable.put(GeneID.removeDot(gffDetailGene.getName()).toLowerCase(), gffDetailGene);
				gffDetailGene.addIso(gffGeneIsoInfo);
				lsResult.add(gffDetailGene);
				locHashtable.put(gffGeneIsoInfo.getName().toLowerCase(), gffDetailGene);
				locHashtable.put(GeneID.removeDot(gffGeneIsoInfo.getName()).toLowerCase(), gffDetailGene);
				continue;
			}
		}
		lsResult.setName(chrID);
		Chrhash.put(chrID, lsResult);
	}
	
	
	/**
	 * 给定chrID和坐标，返回该点应该是正链还是负链
	 * 如果不清楚正负链且没有给定相关的refGff，则直接返回true
	 * @param chrID
	 * @param LocID
	 * @return
	 */
	private boolean getLocCis(String ss, String chrID, int LocIDStart, int LocIDEnd)
	{
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
//				System.out.println("error");
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
