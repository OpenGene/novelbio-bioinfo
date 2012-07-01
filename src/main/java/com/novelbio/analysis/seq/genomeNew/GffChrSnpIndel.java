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
public class GffChrSnpIndel {
	GffChrAbs gffChrAbs;
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	/**
	 * ����indel����snpע���ϸ�snp����Ϣ
	 * @param mapInfo
	 * @return
	 */
	public GffCodGene getSnpIndel(MapInfoSnpIndel mapInfo) {
		if (mapInfo.getType().equals(MapInfoSnpIndel.TYPE_MISMATCH)) {
			return getSnp(mapInfo);
		}
		else {
			return getIndel(mapInfo);
		}
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
		GffCodGene gffCodeGene = gffChrAbs.getGffHashGene().searchLocation(mapInfo.getRefID(), mapInfo.getRefSnpIndelStart());
		GffGeneIsoInfo gffGeneIsoInfo = gffCodeGene.getCodInExonIso();
		if (gffGeneIsoInfo != null ) {
			mapInfo.setGffIso(gffGeneIsoInfo);
			mapInfo.setExon(true);
			//mRNA����
			//�������������У���������ǷǱ���rna��������UTR�����У�Ҳ����
			if (!gffGeneIsoInfo.isCodInAAregion(gffCodeGene.getCoord())) {
				return gffCodeGene;
			}
			int LocStart = gffGeneIsoInfo.getLocAAbefore(mapInfo.getRefSnpIndelStart());//��λ������AA�ĵ�һ��loc
			int LocEnd = gffGeneIsoInfo.getLocAAend(mapInfo.getRefSnpIndelStart());
			if (LocEnd <0) {//�������ת¼����
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
				NR = gffChrAbs.getSeqHash().getSeq(gffGeneIsoInfo.isCis5to3(), mapInfo.getRefID(), LocStart, LocEnd);
			}
			else {
				NR = gffChrAbs.getSeqHash().getSeq(mapInfo.getRefID(), lsTmp, false);
			}
			mapInfo.setSeq(NR,false);
			mapInfo.setReplaceLoc(-gffGeneIsoInfo.getLocAAbeforeBias(mapInfo.getRefSnpIndelStart()) + 1);
		}
		else {
			if (gffCodeGene.isInsideLoc()) {
				GffGeneIsoInfo gffGeneIsoInfo2 = gffCodeGene.getGffDetailThis().getLongestSplit();
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
		GffCodGene gffCodeGeneStart = gffChrAbs.getGffHashGene().searchLocation(mapInfo.getRefID(), mapInfo.getRefSnpIndelStart());
		GffCodGene gffCodeGeneEnd = gffChrAbs.getGffHashGene().searchLocation(mapInfo.getRefID(), mapInfo.getRefSnpIndelEnd());
		GffGeneIsoInfo gffGeneIsoInfoStart = gffCodeGeneStart.getCodInExonIso();
		GffGeneIsoInfo gffGeneIsoInfoEnd = gffCodeGeneEnd.getCodInExonIso();
		//�����յ���ͬһ����������
		if (gffGeneIsoInfoStart != null && gffGeneIsoInfoEnd != null && 
				gffGeneIsoInfoStart.equals(gffGeneIsoInfoEnd) &&
				gffGeneIsoInfoStart.getLocInEleNum(mapInfo.getRefSnpIndelStart()) == gffGeneIsoInfoEnd.getLocInEleNum(mapInfo.getRefSnpIndelEnd()) 
				&& gffGeneIsoInfoStart.isCodInAAregion(mapInfo.getRefSnpIndelStart())) {
			mapInfo.setExon(true);
			mapInfo.setGffIso(gffGeneIsoInfoStart);
			int LocStart = gffGeneIsoInfoStart.getLocAAbefore(mapInfo.getRefSnpIndelStartCis());
			int LocEnd = gffGeneIsoInfoStart.getLocAAend(mapInfo.getRefSnpIndelEndCis());
			mapInfo.setStartEndLoc(LocStart, LocEnd);
			ArrayList<ExonInfo> lsTmp = gffGeneIsoInfoStart.getRangeIso(LocStart, LocEnd);
			//TODO ������һЩ����
			if (lsTmp == null) {
				return gffCodeGeneStart;
			}
			SeqFasta NR = gffChrAbs.getSeqHash().getSeq(mapInfo.getRefID(), lsTmp, false);
			mapInfo.setSeq(NR,false);//��Ϊ�����Ѿ��������
			mapInfo.setReplaceLoc(-gffGeneIsoInfoStart.getLocAAbeforeBias(mapInfo.getRefSnpIndelStartCis()) + 1);
		}
		else {
			if (gffGeneIsoInfoStart != null) {
				//�������
				mapInfo.setGffIso(gffGeneIsoInfoStart);
				mapInfo.setExon(true);
			}
			else if (gffGeneIsoInfoEnd != null) {
				mapInfo.setGffIso(gffGeneIsoInfoEnd);
				mapInfo.setExon(true);
			}
			else {
				if (gffCodeGeneStart.isInsideLoc()) {
					GffGeneIsoInfo gffGeneIsoInfo2 = gffCodeGeneStart.getGffDetailThis().getLongestSplit();
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
		gffChrAbs.setMapReads(bedFile, 1);
		gffChrAbs.loadMapReads();
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
	public int getSnpReadsNum(MapInfo mapInfo) {
		double[] readsNum = gffChrAbs.getMapReads().getRengeInfo(1, mapInfo.getRefID(), mapInfo.getFlagSite(), mapInfo.getFlagSite(), 0);
		int Num = (int)MathComput.mean(readsNum);
		return Num;
	}
}
