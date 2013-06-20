package com.novelbio.analysis.seq.genome.gffOperate;

import java.util.LinkedHashMap;
import java.util.List;

import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.resequencing.SnpAnnotation;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.model.modgeneid.GeneType;

public class GffHashGeneGBK extends GffHashGeneAbs {
	int geneSpaceNum = 1;
	String chrID = "";
	public static void main(String[] args) {
		GffHashGeneGBK gffHashGeneGBK = new GffHashGeneGBK();
		gffHashGeneGBK.chrID = "dq167399";
		gffHashGeneGBK.ReadGffarray("/media/winD/zongjie/desktop/栾霁/dq167399.gbk");
		SnpAnnotation snpAnnotation = new SnpAnnotation();
		GffChrAbs gffChrAbs = new GffChrAbs();
		gffChrAbs.setGffHash(gffHashGeneGBK);
		SeqHash seqHash = new SeqHash("/media/winD/zongjie/desktop/栾霁/dq167399.txt");
		gffChrAbs.setSeqHash(seqHash);
		snpAnnotation.setGffChrAbs(gffChrAbs);
		snpAnnotation.addTxtSnpFile("/media/winD/zongjie/desktop/栾霁/399_807_snps.txt", "/media/winD/zongjie/desktop/栾霁/399_807_snps_anno.txt");
		snpAnnotation.setCol(1, 2, 3, 4);
		snpAnnotation.run();
	}
	
	/**
	 * 这里输入的应该是一个文件夹，包含了所有GBK的文件
	 */
	@Override
	protected void ReadGffarrayExcepTmp(String gfffilename) throws Exception {
//		List<String> lsGBKfile = FileOperate.getFoldFileNameLs(gfffilename, "*", "gbk");
//		for (String string : lsGBKfile) {		
//		}
		mapChrID2ListGff = new LinkedHashMap<String, ListGff>();
		readGBKfile(chrID, gfffilename);
	}
	
	private void readGBKfile(String chrID, String gbkFile) {
		ListGff listGff = new ListGff();
		mapChrID2ListGff.put(chrID.toLowerCase(), listGff);
		TxtReadandWrite	txtRead = new TxtReadandWrite(gbkFile);
		boolean skip = true;
		String tmpInfo = "";
		GffDetailGene gffDetailGene = null;
		for (String content : txtRead.readlines()) {
			if (content.startsWith("ORIGIN") || content.trim().startsWith("//")) {
				skip = true;
			}
			try {
				content = content.substring(5);
			} catch (Exception e) {
				content = content.trim();
				System.out.println(content);
				continue;
			}
			
			if (content.startsWith("gene")) {
				if (content.contains("join")) {
					skip = true;
				} else {
					skip = false;
				}
			} else if (content.trim().startsWith("/translation=") || content.trim().startsWith(" ")) {
				skip = true;
			}
			if (skip) continue;
			
			if (content.startsWith(" ") && !content.trim().startsWith("/")) {
				tmpInfo = tmpInfo + content.trim();
			} else {
				if (tmpInfo.startsWith("gene")) {
					gffDetailGene = addGene(listGff, tmpInfo);
					listGff.add(gffDetailGene);
				} else if (tmpInfo.startsWith("CDS")) {
					addExon(gffDetailGene, tmpInfo);
				} else if(!tmpInfo.equals("") && !tmpInfo.startsWith(" ")) {
					GeneType geneType = GeneType.getGeneType(tmpInfo.trim().split(" +")[0]);
					addmRNA(gffDetailGene, geneType, tmpInfo);
				} else {
					if (tmpInfo.trim().startsWith("/gene")) {
						setGeneName(gffDetailGene, tmpInfo);
					} else if (tmpInfo.trim().startsWith("/transcript_id=")) {
						setmRNAname(gffDetailGene, tmpInfo);
					}
				}
			}
			tmpInfo = content;
		}
		txtRead.close();
		for (GffDetailGene gffDetailGeneTmp : listGff) {
			   if (gffDetailGeneTmp.getLsCodSplit().size() == 0) {
				   gffDetailGeneTmp.addsplitlist(gffDetailGeneTmp.getNameSingle(), gffDetailGeneTmp.getNameSingle(), GeneType.ncRNA);
				   gffDetailGeneTmp.addExon(null, gffDetailGeneTmp.getStartAbs(), gffDetailGeneTmp.getEndAbs());
			   }
			   for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGeneTmp.getLsCodSplit()) {
				   if (gffGeneIsoInfo.size() == 0) {
					   gffGeneIsoInfo.addExon(null, gffDetailGeneTmp.getStartCis(), gffDetailGeneTmp.getEndCis());
				   }
			   }
		}
	}
	
	
	
	
	private GffDetailGene addGene(ListGff listGff, String content) {
		String[] ss = content.trim().split(" +");
		GffDetailGene gffDetailGene = new GffDetailGene(listGff, null, !ss[1].contains("complement"));
		String locTmp = ss[1].replace("complement", "").replace("join", "").replace("(", "").replace(")", "");
		String[] loc = locTmp.split("\\.\\.");
		int start = Integer.parseInt(loc[0]); int end = Integer.parseInt(loc[1]);
		gffDetailGene.setStartAbs(Math.min(start, end));
		gffDetailGene.setEndAbs(Math.max(start, end));
		return gffDetailGene;
	}
	
	private void setGeneName(GffDetailGene gffDetailGene, String content) {
		content = content.trim();
		if (content.startsWith("/gene=")) {
			gffDetailGene.addItemName(content.split("=")[1].replace("\"", ""));
		}
	}
	
	private GffGeneIsoInfo addmRNA(GffDetailGene gffDetailGene, GeneType geneTypes, String content) {
		String[] ss = content.trim().split(" +");
		boolean cis5to3 = true;
		if (ss[1].contains("complement")) {
			cis5to3 = false;
		}
		GffGeneIsoInfo gffGeneIsoInfo = null;
		gffGeneIsoInfo = GffGeneIsoInfo.createGffGeneIso(gffDetailGene.getNameSingle(), gffDetailGene.getNameSingle(), gffDetailGene, geneTypes, cis5to3);
		String[] exonInfo = ss[1].replace("complement", "").replace("join", "").replace("(", "").replace(")", "").split(",");
		for (String exonTmp : exonInfo) {
			String[] exonLoc = exonTmp.split("\\.\\.");
			int start = Integer.parseInt(exonLoc[0]); int end = Integer.parseInt(exonLoc[1]);
			gffGeneIsoInfo.addExon(null, start, end);
		}
		gffDetailGene.addIso(gffGeneIsoInfo);
		return gffGeneIsoInfo;
	}
	
	/**
	 * /transcript_id="NM_000983.3"
	 * @param gffDetailGene
	 * @param ss 类似 /transcript_id="NM_000983.3"
	 */
	private void setmRNAname(GffDetailGene gffDetailGene, String content) {
		String isoName = content.split("=")[1].replace("\"", "");
		List<GffGeneIsoInfo> lsIso = gffDetailGene.getLsCodSplit();
		GffGeneIsoInfo gffGeneIsoInfo = lsIso.get(lsIso.size() - 1);
		gffGeneIsoInfo.setName(isoName);
	}
	
	private void addExon(GffDetailGene gffDetailGene, String content) {
		GffGeneIsoInfo gffGeneIsoInfo = null;
		if (gffDetailGene.getLsCodSplit().size() == 0) {
			gffGeneIsoInfo = addmRNA(gffDetailGene, GeneType.mRNA, content);
		} else {
			for (GffGeneIsoInfo gffGeneIsoInfoTmp : gffDetailGene.getLsCodSplit()) {
				if (gffGeneIsoInfoTmp.getATGsite() < 0) {
					gffGeneIsoInfo = gffGeneIsoInfoTmp;
					break;
				}
			}
		}
		
		String[] ss = content.trim().split(" +");
		String[] cdsInfo = ss[1].replace("complement", "").replace("join", "").replace("(", "").replace(")", "").split(",");
		for (String cdsInfoTmp : cdsInfo) {
			String[] cdsLoc = cdsInfoTmp.split("\\.\\.");
			int start = Integer.parseInt(cdsLoc[0]); int end = Integer.parseInt(cdsLoc[1]);
			gffGeneIsoInfo.setATGUAGauto(start, end);
		}
	}
	
	
	private enum ReadType {
		gene, rna, exon, cds, seq;
	}
}


