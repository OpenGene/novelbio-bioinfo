package com.novelbio.analysis.seq.genomeNew;

import java.util.ArrayList;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.AminoAcid;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFasta;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfo;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfoSnpIndel;

public class GffChrSnpIndel extends GffChrAbs {

	public GffChrSnpIndel(String gffType, String gffFile, String chrFile, String regx) {
		super(gffType, gffFile, chrFile, regx, null, 0);
		loadChrFile();
	}
	
	public GffChrSnpIndel(String gffType, String gffFile, String chrFile) {
		this(gffType, gffFile, chrFile, null);
		loadChrFile();
	}
	
	public static void main(String[] args) {
		GffChrSeq gffChrSeq = new GffChrSeq(NovelBioConst.GENOME_GFF_TYPE_UCSC,  null, NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM);
//		gffChrSeq.setGffFile(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, "/media/winE/NBC/Project/Project_FY_Lab/Result/cufflinkAll/cufcompare/cmpAll.combined_cope.gtf");
		gffChrSeq.loadChrFile();
		String aaa = gffChrSeq.getSeq(true, "chr1", 1, 1);
		System.out.println(aaa);
	}
	
	
	public GffCodGene getSnpIndel(MapInfoSnpIndel mapInfo) {
		if (mapInfo.getFlagSite() > 0) {
			return getSnp(mapInfo);
		}
		else {
			return getIndel(mapInfo);
		}
	}
	
	
	
	
	/**
	 * 待检验
	 * 给定mapInfo，其中mapInfo的flagLoc为snp坐标位点，chrID为染色体位置
	 * 获得该snp所在的三个碱基，以及所对应的氨基酸
	 * startLoc为起点碱基坐标，endLoc为终点碱基坐标
	 * 默认搜索最长转录本
	 * 将具体的序列信息填充mapInfo
	 * 返回该snp的定位信息
	 */
	private GffCodGene getSnp(MapInfoSnpIndel mapInfo) {
		GffCodGene gffCodeGene = gffHashGene.searchLocation(mapInfo.getChrID(), mapInfo.getFlagSite());
		GffGeneIsoInfo gffGeneIsoInfo = gffCodeGene.getCodInExonIso();
		if (gffGeneIsoInfo != null) {
			int endLen = gffGeneIsoInfo.getCod2UAG();
 
			int LocStart = gffGeneIsoInfo.getLocAAbefore(mapInfo.getFlagSite());
			int LocEnd = gffGeneIsoInfo.getLocAAend(mapInfo.getFlagSite());
			if (LocEnd <0) {
				if (gffGeneIsoInfo.isCis5to3()) {
					LocEnd = LocStart + 2;
				}
				else {
					LocEnd = LocStart - 2;
				}
			}
			if (mapInfo.getFlagSite() == 152212486) {
				System.out.println("ok");
			}
			String NR = "";
			
			ArrayList<int[]> lsTmp = gffGeneIsoInfo.getRangeIso(LocStart, LocEnd);
			if (lsTmp == null) {
				NR = seqHash.getSeq(gffGeneIsoInfo.isCis5to3(), mapInfo.getChrID(), LocStart, LocEnd);
			}
			else {
				NR = seqHash.getSeq(mapInfo.getChrID(), lsTmp, false);
			}
			
//			System.out.println(seqHash.getSeq(mapInfo.getChrID(),  gffGeneIsoInfo.getRangeIso(LocStart-4, LocEnd+4), false));
			AminoAcid aminoAcid = new AminoAcid(NR);
			mapInfo.setRefAAseq(aminoAcid.convertDNA2AA());
			System.out.println(mapInfo.getFlagSite());
			String mm = aminoAcid.convertDNA2AA();
			String NRthis = replaceSnpIndel(gffGeneIsoInfo.isCis5to3(), NR, mapInfo, gffGeneIsoInfo.getLocAAbeforeBias(mapInfo.getFlagSite()));
			AminoAcid aminoAcidThis = new AminoAcid(NRthis);
			mapInfo.setThisAaSeq(aminoAcidThis.convertDNA2AA());
			mapInfo.setOrfShift(aminoAcidThis.getOrfShitf());
			mapInfo.setTitle(gffGeneIsoInfo.getIsoName());
			mapInfo.setGffIso(gffGeneIsoInfo);
		}
		return gffCodeGene;
	}
	
	/**
	 * 可以将该方法放入AminoAcid类中
	 * @param cis5to3
	 * @param NR
	 * @param mapInfo
	 * @param LocStart
	 * @param LocEnd
	 * @return
	 */
	private String replaceSnpIndel(boolean cis5to3, String NR, MapInfoSnpIndel mapInfo,int startBias )
	{
		String replace = ""; int LocMid = 0;
		if (!cis5to3) {
			replace = SeqFasta.reservecom(mapInfo.getThisBase());
		}
		else {
			replace = mapInfo.getThisBase();
		}
		
		if (mapInfo.getFlagSite() > 0) {
			LocMid = mapInfo.getFlagSite();
		}
		else {
			if (cis5to3) {
				LocMid = mapInfo.getStart();
			}
			else {
				LocMid = mapInfo.getEnd();
			}
		}
		startBias = Math.abs(startBias);
		int endBias =  startBias + mapInfo.getRefBase().length();
		String endString = "";
		if (endBias <= NR.length()) {
			endString = NR.substring(endBias, NR.length());
		}
		String NRthis = NR.substring(0,startBias) + replace + endString;
		return NRthis;
	}
	/**
	 * 待检验
	 * 给定mapInfo，其中mapInfo的flagLoc为snp坐标位点，chrID为染色体位置
	 * 获得该snp所在的三个碱基，以及所对应的氨基酸
	 * startLoc为起点碱基坐标，endLoc为终点碱基坐标
	 * 默认搜索最长转录本
	 * 将具体的序列信息填充mapInfo
	 * 返回该snp的定位信息
	 */
	private GffCodGene getIndel(MapInfoSnpIndel mapInfo) {
		GffCodGene gffCodeGeneStart = gffHashGene.searchLocation(mapInfo.getChrID(), mapInfo.getStart());
		GffCodGene gffCodeGeneEnd = gffHashGene.searchLocation(mapInfo.getChrID(), mapInfo.getEnd());
		GffGeneIsoInfo gffGeneIsoInfoStart = gffCodeGeneStart.getCodInExonIso();
		GffGeneIsoInfo gffGeneIsoInfoEnd = gffCodeGeneEnd.getCodInExonIso();
		
		if (gffGeneIsoInfoStart != null && gffGeneIsoInfoEnd != null && 
				gffGeneIsoInfoStart.equals(gffGeneIsoInfoEnd) && gffGeneIsoInfoStart.getCodExInNum() == gffGeneIsoInfoEnd.getCodExInNum()) {
			int startLen1 = gffGeneIsoInfoStart.getCod2ATGmRNA();
			int startLen2 = gffGeneIsoInfoEnd.getCod2ATGmRNA();
			int LocStart = 0; int LocEnd = 0;
			if (gffGeneIsoInfoStart.isCis5to3()) {
				LocStart = gffGeneIsoInfoStart.getLocAAbefore(mapInfo.getStart());
				LocEnd = gffGeneIsoInfoStart.getLocAAend(mapInfo.getEnd());
			}
			else {
				LocStart = gffGeneIsoInfoStart.getLocAAbefore(mapInfo.getEnd());
				LocEnd = gffGeneIsoInfoStart.getLocAAend(mapInfo.getStart());
			}
			ArrayList<int[]> lsTmp = gffGeneIsoInfoStart.getRangeIso(LocStart, LocEnd);
			//TODO 这里有一些问题
			if (lsTmp == null) {
				return gffCodeGeneStart;
			}
			String NR = seqHash.getSeq(mapInfo.getChrID(), lsTmp, false);
			AminoAcid aminoAcid = new AminoAcid(NR);
			mapInfo.setThisAaSeq(aminoAcid.convertDNA2AA());
			
			String NRthis = replaceSnpIndel(gffGeneIsoInfoStart.isCis5to3(), NR, mapInfo, gffGeneIsoInfoStart.getLocAAbeforeBias(mapInfo.getFlagSite()));
			
			AminoAcid aminoAcidThis = new AminoAcid(NRthis);
			mapInfo.setThisAaSeq(aminoAcidThis.convertDNA2AA());
			mapInfo.setOrfShift(aminoAcidThis.getOrfShitf());
			mapInfo.setTitle(gffGeneIsoInfoStart.getIsoName());
		}
		return gffCodeGeneStart;
	}
	
	
}
