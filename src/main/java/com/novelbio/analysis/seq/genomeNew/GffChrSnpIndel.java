package com.novelbio.analysis.seq.genomeNew;

import java.util.ArrayList;

import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFasta;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfo;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfoSnpIndel;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.generalConf.NovelBioConst;
/**
 * ����snp��indel����Ϣ����øı�İ������
 * @author zong0jie
 *
 */
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
		SeqFasta aaa = gffChrSeq.getSeq(true, "chr1", 1, 1);
		System.out.println(aaa);
	}
	
	/**
	 * ����indel����snpע���ϸ�snp����Ϣ
	 * @param mapInfo
	 * @return
	 */
	public GffCodGene getSnpIndel(MapInfoSnpIndel mapInfo) {
		if (mapInfo.getFlagSite() > 0) {
			return getSnp(mapInfo);
		}
		else {
			return getIndel(mapInfo);
		}
	}
	
	/**
	 * �������к���ʼλ�㣬��snpλ��ȥ�滻����
	 * @param cis5to3 ������
	 * @param NR ��������
	 * @param mapInfo ����snp��Ϣ
	 * @param startBias  �����е���һ���㿪ʼ�滻
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
	 * ������
	 * ����mapInfo������mapInfo��flagLocΪsnp����λ�㣬chrIDΪȾɫ��λ��
	 * ��ø�snp���ڵ�����������Լ�����Ӧ�İ�����
	 * startLocΪ��������꣬endLocΪ�յ�������
	 * Ĭ�������ת¼��
	 * �������������Ϣ���mapInfo
	 * ���ظ�snp�Ķ�λ��Ϣ
	 */
	private GffCodGene getSnp(MapInfoSnpIndel mapInfo) {
		GffCodGene gffCodeGene = gffHashGene.searchLocation(mapInfo.getChrID(), mapInfo.getFlagSite());
		GffGeneIsoInfo gffGeneIsoInfo = gffCodeGene.getCodInExonIso();
		if (gffGeneIsoInfo != null ) {
			mapInfo.setExon(true);
			//mRNA����
			//�������������У���������ǷǱ���rna��������UTR�����У�Ҳ����
			if (!gffGeneIsoInfo.isCodInAAregion(gffCodeGene.getCoord())) {
				mapInfo.setProp( (double)gffGeneIsoInfo.getCod2TSSmRNA() / (gffGeneIsoInfo.getCod2TSSmRNA() - gffGeneIsoInfo.getCod2TESmRNA()) );
				mapInfo.setGffIso(gffGeneIsoInfo);
				return gffCodeGene;
			}
			mapInfo.setProp( (double)gffGeneIsoInfo.getCod2ATGmRNA() / (gffGeneIsoInfo.getCod2ATGmRNA() - gffGeneIsoInfo.getCod2UAGmRNA()) );

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
			SeqFasta NR = null;
			
			ArrayList<ExonInfo> lsTmp = gffGeneIsoInfo.getRangeIso(LocStart, LocEnd);

			if (lsTmp == null) {
				NR = seqHash.getSeq(gffGeneIsoInfo.isCis5to3(), mapInfo.getChrID(), LocStart, LocEnd);
			}
			else {
				NR = seqHash.getSeq(mapInfo.getChrID(), lsTmp, false);
			}
			mapInfo.setRefAAnr(NR.toString());
//			System.out.println(seqHash.getSeq(mapInfo.getChrID(),  gffGeneIsoInfo.getRangeIso(LocStart-4, LocEnd+4), false));
			mapInfo.setRefAAseq(NR.toStringAA());
			System.out.println(mapInfo.getFlagSite());
			String NRthis = replaceSnpIndel(gffGeneIsoInfo.isCis5to3(), NR.toString(), mapInfo, gffGeneIsoInfo.getLocAAbeforeBias(mapInfo.getFlagSite()));
			mapInfo.setThisAAnr(NRthis);
			AminoAcid aminoAcidThis = new AminoAcid(NRthis);
			mapInfo.setThisAaSeq(aminoAcidThis.convertDNA2AA());
			mapInfo.setOrfShift(aminoAcidThis.getOrfShitf());
			mapInfo.setTitle(gffGeneIsoInfo.getName());
			mapInfo.setGffIso(gffGeneIsoInfo);
		}
		else {
			if (gffCodeGene.isInsideLoc()) {
				GffGeneIsoInfo gffGeneIsoInfo2 = gffCodeGene.getGffDetailThis().getLongestSplit();
				mapInfo.setProp( (double)gffGeneIsoInfo2.getCod2Tss() / (gffGeneIsoInfo2.getCod2ATGmRNA() - gffGeneIsoInfo2.getCod2Tes()) );
				mapInfo.setGffIso(gffGeneIsoInfo2);
			}
			mapInfo.setExon(false);
		}
		//�������
		
		return gffCodeGene;
	}
	
	/**
	 * ������
	 * ����mapInfo������mapInfo��flagLocΪsnp����λ�㣬chrIDΪȾɫ��λ��
	 * ��ø�snp���ڵ�����������Լ�����Ӧ�İ�����
	 * startLocΪ��������꣬endLocΪ�յ�������
	 * Ĭ�������ת¼��
	 * �������������Ϣ���mapInfo
	 * ���ظ�snp�Ķ�λ��Ϣ
	 */
	private GffCodGene getIndel(MapInfoSnpIndel mapInfo) {
		GffCodGene gffCodeGeneStart = gffHashGene.searchLocation(mapInfo.getChrID(), mapInfo.getStart());
		GffCodGene gffCodeGeneEnd = gffHashGene.searchLocation(mapInfo.getChrID(), mapInfo.getEnd());
		GffGeneIsoInfo gffGeneIsoInfoStart = gffCodeGeneStart.getCodInExonIso();
		GffGeneIsoInfo gffGeneIsoInfoEnd = gffCodeGeneEnd.getCodInExonIso();
		
		if (gffGeneIsoInfoStart != null && gffGeneIsoInfoEnd != null && 
				gffGeneIsoInfoStart.equals(gffGeneIsoInfoEnd) && gffGeneIsoInfoStart.getCodExInNum() == gffGeneIsoInfoEnd.getCodExInNum() && gffGeneIsoInfoStart.isCodInAAregion()) {
			mapInfo.setExon(true);
			mapInfo.setProp( (double)gffGeneIsoInfoStart.getCod2ATGmRNA() / (gffGeneIsoInfoStart.getCod2ATGmRNA() - gffGeneIsoInfoStart.getCod2UAGmRNA()) );

			
			int LocStart = 0; int LocEnd = 0;
			if (gffGeneIsoInfoStart.isCis5to3()) {
				LocStart = gffGeneIsoInfoStart.getLocAAbefore(mapInfo.getStart());
				LocEnd = gffGeneIsoInfoStart.getLocAAend(mapInfo.getEnd());
			}
			else {
				LocStart = gffGeneIsoInfoStart.getLocAAbefore(mapInfo.getEnd());
				LocEnd = gffGeneIsoInfoStart.getLocAAend(mapInfo.getStart());
			}
			ArrayList<ExonInfo> lsTmp = gffGeneIsoInfoStart.getRangeIso(LocStart, LocEnd);
			//TODO ������һЩ����
			if (lsTmp == null) {
				return gffCodeGeneStart;
			}
			SeqFasta NR = seqHash.getSeq(mapInfo.getChrID(), lsTmp, false);
			mapInfo.setRefAAseq(NR.toStringAA());
			mapInfo.setRefAAnr(NR.toString());
			String NRthis = replaceSnpIndel(gffGeneIsoInfoStart.isCis5to3(), NR.toString(), mapInfo, gffGeneIsoInfoStart.getLocAAbeforeBias(mapInfo.getFlagSite()));
			mapInfo.setThisAAnr(NRthis);
			AminoAcid aminoAcidThis = new AminoAcid(NRthis);
			mapInfo.setThisAaSeq(aminoAcidThis.convertDNA2AA());
			mapInfo.setOrfShift(aminoAcidThis.getOrfShitf());
			mapInfo.setTitle(gffGeneIsoInfoStart.getName());
			mapInfo.setGffIso(gffGeneIsoInfoStart);
		}
		else {
			if (gffGeneIsoInfoStart != null) {
				//�������
				mapInfo.setProp( (double)gffGeneIsoInfoStart.getCod2TSSmRNA() / (gffGeneIsoInfoStart.getCod2TSSmRNA() - gffGeneIsoInfoStart.getCod2TESmRNA()) );
				mapInfo.setGffIso(gffGeneIsoInfoStart);
				mapInfo.setExon(true);
			}
			else if (gffGeneIsoInfoEnd != null) {
				mapInfo.setProp( (double)gffGeneIsoInfoEnd.getCod2TSSmRNA() / (gffGeneIsoInfoEnd.getCod2TSSmRNA() - gffGeneIsoInfoEnd.getCod2TESmRNA()) );
				mapInfo.setGffIso(gffGeneIsoInfoEnd);
				mapInfo.setExon(true);
			}
			else {
				if (gffCodeGeneStart.isInsideLoc()) {
					GffGeneIsoInfo gffGeneIsoInfo2 = gffCodeGeneStart.getGffDetailThis().getLongestSplit();
					mapInfo.setProp( (double)gffGeneIsoInfo2.getCod2Tss() / (gffGeneIsoInfo2.getCod2Tss() - gffGeneIsoInfo2.getCod2Tes()) );
					mapInfo.setGffIso(gffGeneIsoInfo2);
				}
				mapInfo.setExon(false);
			}
		}
		return gffCodeGeneStart;
	}
	
	
	public void readSnpNum(String excelfile, int colChrID, int colSummit, String bedFile, String outFile ) {
		ArrayList<String[]> lsTmp = ExcelTxtRead.readLsExcelTxt(excelfile, 1, 0, 1, 0);
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		setMapReads(bedFile, 1);
		loadMapReads();
		for (String[] strings : lsTmp) {
			MapInfo mapInfo = new MapInfo(strings[colChrID], Integer.parseInt(strings[colSummit]), Integer.parseInt(strings[colSummit]) );
			int Num = getSnpReadsNum(mapInfo);
			String[] tmpOut =  ArrayOperate.copyArray(strings, strings.length + 1);
			tmpOut[tmpOut.length - 1] = Num+"";
			lsResult.add(tmpOut);
		}
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		txtOut.writefile(lsResult);
		txtOut.close();
	}
	
	
	/**
	 * λ����mapInfo��flagsite
	 * @param mapInfo
	 */
	public int getSnpReadsNum(MapInfo mapInfo)
	{
		double[] readsNum = mapReads.getRengeInfo(1, mapInfo.getChrID(), mapInfo.getFlagSite(), mapInfo.getFlagSite(), 0);
		int Num = (int)MathComput.mean(readsNum);
		return Num;
	}
	
	
	
}
