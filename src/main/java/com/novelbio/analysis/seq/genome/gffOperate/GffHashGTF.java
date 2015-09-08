package com.novelbio.analysis.seq.genome.gffOperate;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.genome.ExceptionGFF;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.HttpFetch;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.modgeneid.GeneType;

public class GffHashGTF extends GffHashGeneAbs{
	private final static Logger logger = Logger.getLogger(GffHashGTF.class);
	final static String startCodeFlag = "start_codon";
	final static String stopCodeFlag = "stop_codon";
	GffHashGene gffHashRef;
	double likelyhood = 0.4;//相似度在0.4以内的转录本都算为同一个基因
	/** gene类似名 */
	private static Set<String> setIsGene = new HashSet<String>();
	/** 需要跳过contig等 */
	private static Set<String> setContig = new HashSet<String>();
	
	ArrayListMultimap<String, GffGeneIsoInfo> mapID2Iso = ArrayListMultimap.create();
	private HashMap<String, Boolean> mapIso2IsHaveExon = new HashMap<String, Boolean>();
	
	String geneNameFlag = null;
	
	/** geneName是哪一项，默认是 gene_name */
	public void setGeneNameFlag(String geneNameFlag) {
		this.geneNameFlag = geneNameFlag;
	}
	/**
	 * 设定参考基因的Gff文件
	 * @param gffHashRef
	 */
	public void setGffHashRef(GffHashGene gffHashRef) {
		this.gffHashRef = gffHashRef;
	}
	/** 设定mRNA和gene的类似名，在gff文件里面出现的 */
	private void setGeneName() {
		if (setIsGene.isEmpty()) {
			setIsGene.add("gene");
			setIsGene.add("transposable_element_gene");
			setIsGene.add("transposable_element");
			setIsGene.add("pseudogene");
		}
	}
	/** 设定mRNA和gene的类似名，在gff文件里面出现的 */
	private void setContig() {
		if (setContig.isEmpty()) {
			setContig.add("contig");
			setIsGene.add("chromosome");
		}
	}
	@Override
	protected void ReadGffarrayExcepTmp(String gfffilename) {
		setGeneName(); setContig();
		mapChrID2ListGff = new LinkedHashMap<String, ListGff>();
		ArrayListMultimap<String, GffGeneIsoInfo> mapChrID2LsIso = ArrayListMultimap.create();
		TxtReadandWrite txtgff = new TxtReadandWrite(gfffilename);
		
		GffGeneIsoInfo gffGeneIsoInfo = null;
		String tmpChrID = "";
		String tmpTranscriptNameLast = "";
		int line = 0;
		for (String content : txtgff.readlines() ) {
			line++;
			if (StringOperate.isRealNull(content) || content.charAt(0) == '#') continue;
			String[] ss = content.split("\t");// 按照tab分开
			ss[8] = HttpFetch.decode(ss[8]);
			if (setContig.contains(ss[2].toLowerCase())) continue;
			
			int exonStart = Integer.parseInt(ss[3]), exonEnd = Integer.parseInt(ss[4]);
			Boolean cisExon = null;
			if (ss[6].equals("+")) {
				cisExon = true;
			} else if (ss[6].equals("-")) {
				cisExon = false;
			}
			
			// 新的染色体
			if (!tmpChrID.equals(ss[0]) ) {
				tmpChrID = ss[0];
			}
			
			String[] isoName2GeneName = getIsoName2GeneName(ss[8]);
			if (isoName2GeneName == null) {
				throw new ExceptionGFF("line " + line + " error, no isoName exist: " + content);
			}
			String tmpTranscriptName = isoName2GeneName[0], tmpGeneName = isoName2GeneName[1];
			
			if (setIsGene.contains(ss[2].toLowerCase())) continue;
			
			//出现新转录本有两种可能：
			//1： 单开一行标记新的transcript
			//2： 还是标记exon，只是后面的transcriptID 变了
			if (GeneType.getMapMRNA2GeneType().containsKey(ss[2].toLowerCase())
				||
				(!tmpTranscriptName.equals(tmpTranscriptNameLast) 
						&& !isHaveIso(tmpGeneName, tmpTranscriptName))
					) 
			{
				GeneType geneType = GeneType.getMapMRNA2GeneType().get(ss[2].toLowerCase());
				if (geneType == null) geneType = GeneType.mRNA;
					
				boolean cis = getLocCis(ss[6], tmpChrID, exonStart, exonEnd);
				gffGeneIsoInfo = GffGeneIsoInfo.createGffGeneIso(tmpTranscriptName, tmpGeneName, geneType, cis);
				addGffIso(tmpGeneName, gffGeneIsoInfo);
				mapChrID2LsIso.put(tmpChrID, gffGeneIsoInfo);
				tmpTranscriptNameLast = tmpTranscriptName;
				mapIso2IsHaveExon.put(tmpTranscriptName, false);
				if (GeneType.getMapMRNA2GeneType().containsKey(ss[2].toLowerCase())) {
					continue;
				}
			}

			gffGeneIsoInfo = getGffIso(tmpGeneName, tmpTranscriptName, exonStart, exonEnd);
			if (gffGeneIsoInfo == null && !ss[2].toLowerCase().contains("utr")) {
				logger.error("没找到其对应的转录本：" + content);
				continue;
			}
			if (ss[2].equals("exon")) {
				if (mapIso2IsHaveExon.get(tmpTranscriptName) == false) {
					gffGeneIsoInfo.addExon(cisExon, exonStart, exonEnd);
					mapIso2IsHaveExon.put(tmpTranscriptName, true);
				} else {
					gffGeneIsoInfo.addExon(cisExon, exonStart, exonEnd);
				}	
			} else if (ss[2].toLowerCase().equals("cds")) {
				//TODO  ncbi上的gff3，cds的末尾是uag，而
				//ucsc上的GTF，cds的末尾不是uag，而是uag的前一位。
				//所以该方法在这里不适用，不过后面有个专门设定uag的方法，所以倒也无所谓了。
				gffGeneIsoInfo.setATGUAGauto(exonStart, exonEnd);
				if (mapIso2IsHaveExon.get(tmpTranscriptName) == null) {
					logger.error("没有找到相应的GeneID:" + tmpTranscriptName);
				}
				if (!mapIso2IsHaveExon.get(tmpTranscriptName)) {
					gffGeneIsoInfo.addExon(cisExon, exonStart, exonEnd);
				}
			} else if (ss[2].toLowerCase().equals(startCodeFlag)) {
				if (cisExon == null || cisExon) {
					gffGeneIsoInfo.setATG(exonStart);
				} else {
					gffGeneIsoInfo.setATG(exonEnd);
				}
			} else if (ss[2].toLowerCase().equals(stopCodeFlag)) {
				if (cisExon == null || cisExon) {
					gffGeneIsoInfo.setUAG(exonEnd);
				} else {
					gffGeneIsoInfo.setUAG(exonStart);
				}
			}
		}
		CopeChrIso(mapChrID2LsIso);
		txtgff.close();
		mapID2Iso = null;
	}
	
	private String[] getIsoName2GeneName(String ss8) {
		String geneNameFlag = "gene_name";
		if (!StringOperate.isRealNull(this.geneNameFlag)) {
			geneNameFlag = this.geneNameFlag;
			if (!ss8.contains(geneNameFlag) && ss8.contains("gene_name")) {
				geneNameFlag = "gene_name";
			}
		}
				
		if (!ss8.contains(geneNameFlag) && ss8.contains("gene_id")) {
			geneNameFlag = "gene_id";
		} else if (!ss8.contains(geneNameFlag) && !ss8.contains("gene_id") && ss8.contains("Name")) {
			geneNameFlag = "Name";
		} else if (ss8.contains("Parent")) {
			geneNameFlag = "Parent";
		}
		
		String[] iso2geneName = new String[2];
		if (ss8.endsWith(";")) {
			ss8 = ss8 + " ";
		}
		 String[] info = ss8.split("; ");
		 for (String name : info) {
			 name = name.trim();
			if (name.startsWith("transcript_id")) {
				iso2geneName[0] = name.replace("transcript_id", "").replace("=", "").replace("\"", "").trim();
			} else if (name.startsWith(geneNameFlag)) {
				iso2geneName[1] = name.replace(geneNameFlag, "").replace("=", "").replace("\"", "").trim();
			} else if (name.startsWith("ID")) {
				iso2geneName[0] = name.replace("ID", "").replace("=", "").replace("\"", "").trim();
			}
		}
		 return iso2geneName;
	}

	private void addGffIso(String geneName, GffGeneIsoInfo gffGeneIsoInfo) {
		mapID2Iso.put(geneName, gffGeneIsoInfo);
	}
	
	private boolean isHaveIso(String geneName, String isoName) {
		List<GffGeneIsoInfo> lsIsos = mapID2Iso.get(geneName);
		if (lsIsos == null || lsIsos.size() == 0) {
			return false;
		}
		for (GffGeneIsoInfo gffGeneIsoInfo : lsIsos) {
			if (gffGeneIsoInfo.getName().toLowerCase().equals(isoName.toLowerCase())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	    * 从hashRnaID2RnaName中获得该RNA的GffGeneIsoInfo
	    * 这里的genID不是我们数据库里面的geneID，而是NCBI gff所特有的ID
	    * @param rnaID 输入的rnaID
	    * @param startExon 输入exon的起点和终点，查找lsGffISO，只有当ISO cover 这对坐标时，才会返回相应的ISO
	    * 主要用于这种情况：<br>
	    * NC_000001.10	RefSeq	gene	94313129	94313213<br>
	    * NC_000001.10	RefSeq	tRNA	94313129	94313165<br>
	    * NC_000001.10	RefSeq	tRNA	94313178	94313213<br>
	    * NC_000001.10	RefSeq	exon	94313129	94313165<br>
	    * NC_000001.10	RefSeq	exon	94313178	94313213<br>
	    * 这时候两个tRNA的rnaID是一样的，但是这两个tRNA确实是两个不同的iso，所以就要根据坐标将两个exon分别装入两个iso中
	    * @param endExon 如果startExon和endExon中有一个小于0，则直接返回listIso的第一个ISO
	    * @param geneType 如果没有找到iso，则新建的iso是什么类型
	    * @return
	    */  
	private GffGeneIsoInfo getGffIso(String geneName, String isoName, int startExon, int endExon) {
		int start = Math.min(startExon, endExon), end = Math.max(startExon, startExon);
		List<GffGeneIsoInfo> lsIsos = mapID2Iso.get(geneName);
		if (lsIsos == null || lsIsos.size() == 0) {
			return null;
		}
		for (GffGeneIsoInfo gffGeneIsoInfo : lsIsos) {
			if (gffGeneIsoInfo.getName().toLowerCase().equals(isoName.toLowerCase())) {
				return gffGeneIsoInfo;
			}
		}
		for (GffGeneIsoInfo gffGeneIsoInfo : lsIsos) {
			if (gffGeneIsoInfo.size() == 0 || start <= gffGeneIsoInfo.getEndAbs() && end >= gffGeneIsoInfo.getStartAbs()) {
				return gffGeneIsoInfo;
			}
		}
		int maxDistance = 50000000;
		GffGeneIsoInfo gffGeneIsoInfoFinal = null;
		for (GffGeneIsoInfo gffGeneIsoInfo : lsIsos) {
			int distance = Math.min(Math.abs(start - gffGeneIsoInfo.getEndAbs()), Math.abs(gffGeneIsoInfo.getStartAbs() - end));
			if (distance < maxDistance) {
				maxDistance = distance;
				gffGeneIsoInfoFinal = gffGeneIsoInfo;
			}
		}
		return gffGeneIsoInfoFinal;
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
				Integer o1Start = 0;
				try {
					o1Start = o1.getStartAbs();
				} catch (Exception e) {
					o1Start = o1.getStartAbs();
				}
				
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
