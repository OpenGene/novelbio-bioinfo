package com.novelbio.analysis.seq.genome.gffOperate;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.model.modgeneid.GeneType;

/** 细菌的GFF文件，因为细菌只有一个染色体，所以默认名字是novelbio */
public class GffHashGeneBacterium extends GffHashGeneAbs {
	private static Logger logger = Logger.getLogger(GffHashGTF.class);
	double likelyhood = 0.4;//相似度在0.4以内的转录本都算为同一个基因

	HashMap<String, GffGeneIsoInfo> mapID2Iso = new HashMap<String, GffGeneIsoInfo>();
	
	/** 记录转录本名字和基因名字的对照表，用于最后将GffDetailGene的名字换为基因名字
	 * key为小写
	 */
	HashMap<String, String> mapIsoName2GeneName = new HashMap<String, String>();
	
	@Override
	protected void ReadGffarrayExcepTmp(String gfffilename) {
		mapChrID2ListGff = new LinkedHashMap<String, ListGff>();
		ArrayListMultimap<String, GffGeneIsoInfo> mapChrID2LsIso = ArrayListMultimap.create();
		TxtReadandWrite txtgff = new TxtReadandWrite(gfffilename, false);
		
		GffGeneIsoInfo gffGeneIsoInfo = null;
		String tmpChrID = "";
		for (String content : txtgff.readlines() ) {
			if (content.charAt(0) == '#') {
				continue;
			}
			String[] ss = content.split("\t");// 按照tab分开
			
			// 新的染色体
			if (!tmpChrID.equals(ss[0]) ) {
				tmpChrID = ss[0];
			}
			String geneName = getGeneName(ss[8]);
			
			boolean cis = ss[6].equals("+") || ss[6].equals(".");
			gffGeneIsoInfo = GffGeneIsoInfo.createGffGeneIso(geneName, geneName, getGeneType(ss[2]), cis);
			gffGeneIsoInfo.setATGUAGauto( Integer.parseInt(ss[3]), Integer.parseInt(ss[4]));
			gffGeneIsoInfo.addExon(cis, Integer.parseInt(ss[3]), Integer.parseInt(ss[4]));
			mapID2Iso.put(geneName, gffGeneIsoInfo);
			mapChrID2LsIso.put(tmpChrID, gffGeneIsoInfo);
		}
		CopeChrIso(mapChrID2LsIso);
		txtgff.close();
		mapID2Iso = null;
	}

	
	private String getGeneName(String ss8) {
		String geneNameFlag = "gene_name";
		if (!ss8.contains(geneNameFlag) && ss8.contains("gene_id")) {
			geneNameFlag = "gene_id";
		} else if (!ss8.contains(geneNameFlag) && !ss8.contains("gene_id") && ss8.contains("Name")) {
			geneNameFlag = "Name";
		}
		
		String geneName = "";
		 String[] info = ss8.split(";| ");
		 for (String name : info) {
			if (name.contains(geneNameFlag)) {
				geneName = name.replace(geneNameFlag, "").replace("\"", "").replace("=", "").trim();
			}
		}
		 return geneName;
	}
	
	private void CopeChrIso(ArrayListMultimap<String, GffGeneIsoInfo> hashChrIso) {
		for (String chrID : hashChrIso.keySet()) {
			List<GffGeneIsoInfo> listIso = hashChrIso.get(chrID);
			copeChrInfo(chrID, listIso);
		}
	}
	
	/**
	 * 整理某条染色体的信息
	 * 将重叠的Iso放到一个gffDetail基因里面
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
				gffDetailGene = createGffDetailGene(lsResult, gffGeneIsoInfo);
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
				gffDetailGene = createGffDetailGene(lsResult, gffGeneIsoInfo);
				continue;
			}
		}
		mapChrID2ListGff.put(chrID.toLowerCase(), lsResult);
	}
	
	private GffDetailGene createGffDetailGene(ListGff lsParent, GffGeneIsoInfo gffGeneIsoInfo) {
		String geneName = gffGeneIsoInfo.getName();//这里实际上是Iso Name
		if (mapIsoName2GeneName.containsKey(gffGeneIsoInfo.getName())) {
			geneName = mapIsoName2GeneName.get(geneName);
		}
		GffDetailGene gffDetailGene = new GffDetailGene(lsParent, geneName, gffGeneIsoInfo.isCis5to3());
		gffDetailGene.addIso(gffGeneIsoInfo);
		lsParent.add(gffDetailGene);
		return gffDetailGene;
	}

	private GeneType getGeneType(String idType) {
		if (idType.equalsIgnoreCase("gene")) {
			return GeneType.mRNA;
		} else if (idType.equalsIgnoreCase("tRNA")) {
			return GeneType.tRNA;
		} else if (idType.equalsIgnoreCase("rRNA")) {
			return GeneType.rRNA;
		}
		return GeneType.mRNA;
	}
}
