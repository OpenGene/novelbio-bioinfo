package com.novelbio.bioinfo.gff;

import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.bioinfo.fasta.SeqHash;
import com.novelbio.bioinfo.gffchr.GffChrAbs;
import com.novelbio.database.domain.modgeneid.GeneType;
import com.novelbio.software.snpanno.SnpAnnotation;

public class GffHashGeneGBK extends GffHashGeneAbs {
	private static final Logger logger = Logger.getLogger(GffHashGeneGBK.class);
	static String geneFlag = "Gene";
	static String mRNAFlag = "mRNA";
	static String cdsFlag = "cds";
	
	String flag;
	public static void main(String[] args) {
		GffHashGeneGBK gffHashGeneGBK = new GffHashGeneGBK();
		gffHashGeneGBK.ReadGffarray("/media/winE/APAU02.1.gbff.gz");
		gffHashGeneGBK.writeToGTF("/media/winE/APAU02.1.gtf", "GeneBank");
	}
	/**
	 * 这里输入的应该是一个文件夹，包含了所有GBK的文件
	 */
	@Override
	protected void ReadGffarrayExcepTmp(String gfffilename) {
//		List<String> lsGBKfile = FileOperate.getFoldFileNameLs(gfffilename, "*", "gbk");
//		for (String string : lsGBKfile) {		
//		}
		mapChrID2ListGff = new LinkedHashMap<String, ListGff>();
		readGBKfile(gfffilename);
	}
	
	private void readGBKfile(String gbkFile) {

		TxtReadandWrite	txtRead = new TxtReadandWrite(gbkFile);
		boolean skip = true;
		String tmpInfo = "";
		GffGene gffDetailGene = null;
		ListGff listGff = null;
		for (String content : txtRead.readlines()) {
			if (content.trim().equals("//")) {
				continue;
			}
			if(content.startsWith("VERSION")) {
				String chrId = content.replaceFirst("VERSION", "").trim();
				chrId = chrId.split(" ")[0];
				listGff = new ListGff();
				listGff.setName(chrId);
				mapChrID2ListGff.put(chrId.toLowerCase(), listGff);
			}
			if (content.startsWith("ORIGIN") || content.trim().startsWith("//")) {
				skip = true;
			}
			try {
				content = content.substring(5);
			} catch (Exception e) {
				content = content.trim();
				logger.error("string error: " + content);
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
				continue;
			} else {
				if (tmpInfo.startsWith("gene")) {
					flag = geneFlag;
					gffDetailGene = addGene(listGff, tmpInfo);
					listGff.add(gffDetailGene);
				} else if (tmpInfo.startsWith("CDS")) {
					flag = cdsFlag;
					addExon(gffDetailGene, tmpInfo);
				} else if(!tmpInfo.equals("") && !tmpInfo.startsWith(" ") && !tmpInfo.equalsIgnoreCase("gap")) {
					flag = mRNAFlag;
					GeneType geneType = GeneType.getGeneType(tmpInfo.trim().split(" +")[0]);
					addmRNA(gffDetailGene, geneType, tmpInfo);
				} else {
					if (geneFlag.equals(flag)) {
						setGeneName(gffDetailGene, tmpInfo);
						flag = "";
					} else if (mRNAFlag.equals(flag)) {
						setmRNAname(gffDetailGene, tmpInfo);
						flag = "";
					}
				}
			}
			tmpInfo = content;
		}
		txtRead.close();
		for (GffGene gffDetailGeneTmp : listGff) {
			   if (gffDetailGeneTmp.getLsCodSplit().size() == 0) {
				   gffDetailGeneTmp.addsplitlist(gffDetailGeneTmp.getNameSingle(), gffDetailGeneTmp.getNameSingle(), GeneType.ncRNA);
				   gffDetailGeneTmp.addExon(null, gffDetailGeneTmp.getStartAbs(), gffDetailGeneTmp.getEndAbs());
			   }
			   for (GffIso gffGeneIsoInfo : gffDetailGeneTmp.getLsCodSplit()) {
				   if (gffGeneIsoInfo.size() == 0) {
					   gffGeneIsoInfo.addExon(null, gffDetailGeneTmp.getStartCis(), gffDetailGeneTmp.getEndCis());
				   }
			   }
		}
	}
	
	
	
	
	private GffGene addGene(ListGff listGff, String content) {
		String[] ss = content.trim().split(" +");
		GffGene gffDetailGene = new GffGene(listGff, null, !ss[1].contains("complement"));
		String locTmp = ss[1].replace("complement", "").replace("join", "").replace("(", "").replace(")", "");
		String[] loc = locTmp.split("\\.\\.");
		int start = Integer.parseInt(loc[0]); int end = Integer.parseInt(loc[1]);
		gffDetailGene.setStartAbs(Math.min(start, end));
		gffDetailGene.setEndAbs(Math.max(start, end));
		return gffDetailGene;
	}
	
	private void setGeneName(GffGene gffDetailGene, String content) {
		content = content.trim();
		gffDetailGene.addItemName(content.split("=")[1].replace("\"", ""));
	}
	
	private GffIso addmRNA(GffGene gffDetailGene, GeneType geneTypes, String content) {
		String[] ss = content.trim().split(" +");
		boolean cis5to3 = true;
		if (ss[1].contains("complement")) {
			cis5to3 = false;
		}
		GffIso gffGeneIsoInfo = null;
		gffGeneIsoInfo = GffIso.createGffGeneIso(gffDetailGene.getNameSingle(), gffDetailGene.getNameSingle(), gffDetailGene, geneTypes, cis5to3);
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
	private void setmRNAname(GffGene gffDetailGene, String content) {
		String isoName = content.split("=")[1].replace("\"", "");
		List<GffIso> lsIso = gffDetailGene.getLsCodSplit();
		GffIso gffGeneIsoInfo = lsIso.get(lsIso.size() - 1);
		gffGeneIsoInfo.setName(isoName);
	}
	
	private void addExon(GffGene gffDetailGene, String content) {
		GffIso gffGeneIsoInfo = null;
		if (gffDetailGene.getLsCodSplit().size() == 0) {
			gffGeneIsoInfo = addmRNA(gffDetailGene, GeneType.mRNA, content);
		} else {
			for (GffIso gffGeneIsoInfoTmp : gffDetailGene.getLsCodSplit()) {
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


