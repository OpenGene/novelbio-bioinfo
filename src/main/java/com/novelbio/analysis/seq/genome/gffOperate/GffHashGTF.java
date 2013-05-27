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
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.modgeneid.GeneType;

public class GffHashGTF extends GffHashGeneAbs{
	private static Logger logger = Logger.getLogger(GffHashGTF.class);
	GffHashGene gffHashRef;
	double likelyhood = 0.4;//相似度在0.4以内的转录本都算为同一个基因
	String transcript = "transcript";

	HashMap<String, GffGeneIsoInfo> mapID2Iso = new HashMap<String, GffGeneIsoInfo>();

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
		ArrayListMultimap<String, GffGeneIsoInfo> mapChrID2LsIso = ArrayListMultimap.create();
		TxtReadandWrite txtgff = new TxtReadandWrite(gfffilename, false);
		
		GffGeneIsoInfo gffGeneIsoInfo = null;
		String tmpChrID = "";
		String tmpTranscriptNameLast = "";
		for (String content : txtgff.readlines() ) {
			if (content.charAt(0) == '#') {
				continue;
			}
			if (content.contains("ENSG00000178591.5")) {
				logger.debug("stop");
			}
			String[] ss = content.split("\t");// 按照tab分开
			
			// 新的染色体
			if (!tmpChrID.equals(ss[0].toLowerCase()) ) {
				tmpChrID = ss[0].toLowerCase();
			}
			
			String[] isoName2GeneName = getIsoName2GeneName(ss[8]);
			String tmpTranscriptName = isoName2GeneName[0];
			
			if (ss[2].equalsIgnoreCase("gene")) {
				continue;
			}
			
			if (ss[2].equals(transcript) 
				||
				(!tmpTranscriptName.equals(tmpTranscriptNameLast) 
						&& !mapID2Iso.containsKey(tmpTranscriptName) )
					) 
			{
				boolean cis = getLocCis(ss[6], tmpChrID, Integer.parseInt(ss[3]), Integer.parseInt(ss[4]));
				gffGeneIsoInfo = GffGeneIsoInfo.createGffGeneIso(tmpTranscriptName, isoName2GeneName[1], GeneType.mRNA, cis);
				mapID2Iso.put(tmpTranscriptName, gffGeneIsoInfo);
				mapChrID2LsIso.put(tmpChrID, gffGeneIsoInfo);
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
	
	private String[] getIsoName2GeneName(String ss8) {
		String geneNameFlag = "gene_name";
		if (!ss8.contains(geneNameFlag) && ss8.contains("gene_id")) {
			geneNameFlag = "gene_id";
		} else if (!ss8.contains(geneNameFlag) && !ss8.contains("gene_id") && ss8.contains("Name")) {
			geneNameFlag = "Name";
		}
		
		String[] iso2geneName = new String[2];
		 String[] info = ss8.split(";");
		 for (String name : info) {
			if (name.contains("transcript_id")) {
				iso2geneName[0] = name.replace("transcript_id", "").replace("\"", "").trim();
			} else if (name.contains(geneNameFlag)) {
				iso2geneName[1] = name.replace(geneNameFlag, "").replace("\"", "").trim();
			}
		}
		 return iso2geneName;
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
		mapChrID2ListGff.put(chrID, lsResult);
	}
	
	private GffDetailGene createGffDetailGene(ListGff lsParent, GffGeneIsoInfo gffGeneIsoInfo) {
		String geneName = gffGeneIsoInfo.getParentGeneName();
		GffDetailGene gffDetailGene = new GffDetailGene(lsParent, geneName, gffGeneIsoInfo.isCis5to3());
		gffDetailGene.addIso(gffGeneIsoInfo);
		lsParent.add(gffDetailGene);
		return gffDetailGene;
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
			} else {
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
	
	/**
	 * 修正ensembl的GTF文件，主要是将一些没用的行，譬如H开头，G开头的删除
	 * 同时将染色体名字从 1 改为 chr1
	 */
	public static void modifyEnsemblGTF(String ensemblGTF) {
		TxtReadandWrite txtRead = new TxtReadandWrite(ensemblGTF, false);
		String fileName = FileOperate.changeFileSuffix(ensemblGTF, "_modify", null);
		fileName = FileOperate.changeFilePrefix(fileName, "Ensembl_", null);
		TxtReadandWrite txtWrite = new TxtReadandWrite(fileName, true);
		for (String string : txtRead.readlines()) {
			if (string.startsWith("H") || string.startsWith("G") || string.startsWith("N")) {
				continue;
			}
			txtWrite.writefileln("chr" + string);
		}
		txtRead.close();
		txtWrite.close();
	}
}
