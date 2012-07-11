package com.novelbio.analysis.seq.genomeNew;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfo;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class GffChrStatistics {
	private static final Logger logger = Logger.getLogger(GffChrAnno.class);
	
	GffChrAbs gffChrAbs;
	
	public GffChrStatistics(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	
	/**
	 * ����txt���ļ�����Ⱦɫ���ţ�Ⱦɫ������յ㣬������ļ�����peak���ǵ�������ע�ͳ���
	 * @param txtFile
	 * @param colChrID
	 * @param colStart
	 * @param colEnd
	 * @param outTxtFile
	 */
	public void getSummitStatistic(String txtFile, int colChrID, int colSummit, int rowStart, String outTxtFile) {
		ArrayList<String[]> lsIn = ExcelTxtRead.readLsExcelTxt(txtFile, new int[]{colChrID, colSummit}, rowStart, 0);
		ArrayList<MapInfo> lsTmpMapInfos = ReadInfo(lsIn);
		int[] region = getStatisticInfo(lsTmpMapInfos);
		TxtReadandWrite txtOut = new TxtReadandWrite(outTxtFile, true);
		txtOut.writefileln("Up" + gffChrAbs.tssUpBp +"bp\t"+region[0]);
		txtOut.writefileln("Exon\t"+region[1]);
		txtOut.writefileln("Intron\t"+region[2]);
		txtOut.writefileln("InterGenic\t"+region[3]);
		txtOut.writefileln("5UTR\t"+region[4]);
		txtOut.writefileln("3UTR\t"+region[5]);
		txtOut.writefileln("GeneEnd"+gffChrAbs.geneEnd3UTR+"\t"+region[6]);
		txtOut.writefileln("Tss\t"+region[7]);
		txtOut.close();
	}
	/**
	 * ����������Ϣlist�����ظ���������Ӧ��mapinfo
	 * @param lsIn  string[2] �򷵻� chrID summit
	 * string[3] �򷵻�chrID start end
	 * @return
	 */
	protected ArrayList<MapInfo> ReadInfo(ArrayList<String[]> lsIn) {
		ArrayList<MapInfo> lsResult = new ArrayList<MapInfo>();
		for (String[] strings : lsIn) {
			MapInfo mapInfo = new MapInfo(strings[0]);
			if (strings.length == 2) {
				mapInfo.setFlagLoc(Integer.parseInt(strings[1].trim()));
			}
			else if (strings.length == 3) {
				int tmpStart = Integer.parseInt(strings[1].trim());
				int tmpEnd = Integer.parseInt(strings[2].trim());
				mapInfo.setStartEndLoc(Math.min(tmpStart, tmpEnd), Math.max(tmpStart, tmpEnd));
			}
			else {
				String tmp = "";
				for (String string : strings) {
					tmp = tmp + "\t" + string;
				}
				logger.error("����δ֪ID��"+ tmp.trim());
			}
			lsResult.add(mapInfo);
		}
		return lsResult;
	}
	
	/**
	 * ���뵥������λ�㣬���ض�λ��Ϣ������ͳ��λ��Ķ�λ���,�������ӻ����ں���
	 * ֻ�ж��ת¼��
	 * @param mapinfoRefSeqIntactAA
	 * @param summit true����flagSite���ж�λ��false�������˽��ж�λ
	 * @return int[8]
	 * 0: UpNbp,N��setStatistic()������TSS����
	 * 1: Exon<br>
	 * 2: Intron<br>
	 * 3: InterGenic--�����<br>
	 * 4: 5UTR
	 * 5: 3UTR
	 * 6: GeneEnd���ڻ������β�� ��setStatistic()������GeneEnd����
	 * 7: Tss ����Tss�Ϻ�Tss�£���filterTss����
	 */
	public int[] getStatisticInfo(ArrayList<MapInfo> lsMapInfos) {
		int[] result = new int[8];
		for (MapInfo mapInfo : lsMapInfos) {
			int[] tmp = searchSite(mapInfo);
			if (tmp == null) {
				continue;
			}
			for (int i = 0; i < tmp.length; i++) {
				result[i] = result[i] + tmp[i];
			}
		}
		return result;
	}
	
	
	
	/**
	 * ���뵥������λ�㣬���ض�λ��Ϣ������ͳ��λ��Ķ�λ���
	 * ֻ�ж��ת¼��
	 * @param mapInfo
	 * @return int[8]
	 * 0: UpNbp,N��setStatistic()������TSS����
	 * 1: Exon<br>
	 * 2: Intron<br>
	 * 3: InterGenic--�����<br>
	 * 4: 5UTR
	 * 5: 3UTR
	 * 6: GeneEnd���ڻ������β�� ��setStatistic()������GeneEnd����
	 * 7: Tss ����Tss�Ϻ�Tss�£���filterTss����
	 */
	private int[] searchSite(MapInfo mapInfo) {
		boolean flagIntraGenic = false;//��gene�ڵı��
		int[] result = new int[8];
		GffCodGene gffCodGene = gffChrAbs.getGffHashGene().searchLocation(mapInfo.getRefID(), mapInfo.getFlagSite());
		if (gffCodGene == null) {
			return null;
		}
		if (gffCodGene.isInsideLoc()) {
			gffCodGene.getGffDetailThis().setTssRegion(gffChrAbs.tss);
			gffCodGene.getGffDetailThis().setTesRegion(gffChrAbs.tes);
			flagIntraGenic = true;
			//Tss
			if (gffCodGene.getGffDetailThis().getLongestSplit().isCodInIsoTss(gffCodGene.getCoord()) ) {
				result[7] ++;
			}
			//Exon
			if (gffCodGene.getGffDetailThis().getLongestSplit().getCodLoc(gffCodGene.getCoord()) == GffGeneIsoInfo.COD_LOC_EXON) {
				result[1] ++;
			}
			else if (gffCodGene.getGffDetailThis().getLongestSplit().getCodLoc(gffCodGene.getCoord()) == GffGeneIsoInfo.COD_LOC_INTRON) {
				result[2] ++;
			}
			//UTR
			if (gffCodGene.getGffDetailThis().getLongestSplit().getCodLoc(gffCodGene.getCoord()) == GffGeneIsoInfo.COD_LOCUTR_5UTR) {
				result[4] ++;
			}
			if (gffCodGene.getGffDetailThis().getLongestSplit().getCodLoc(gffCodGene.getCoord()) == GffGeneIsoInfo.COD_LOCUTR_3UTR) {
				result[5] ++;
			}
		}
		else {
			if (gffCodGene.getGffDetailUp() != null ) {
				gffCodGene.getGffDetailUp().setTssRegion(gffChrAbs.tss);
				gffCodGene.getGffDetailUp().setTesRegion(gffChrAbs.tes);
			}
			if (gffCodGene.getGffDetailDown() != null) {
				gffCodGene.getGffDetailDown().setTssRegion(gffChrAbs.tss);
				gffCodGene.getGffDetailDown().setTesRegion(gffChrAbs.tes);
			}

			//UpNbp
			if (gffCodGene.getGffDetailUp() != null && gffCodGene.getGffDetailUp().isCodInPromoter(gffCodGene.getCoord())) {
				result[0]++;flagIntraGenic =true;
			}
			else if (gffCodGene.getGffDetailDown() != null && gffCodGene.getGffDetailDown().isCodInPromoter(gffCodGene.getCoord())) {
				result[0] ++;flagIntraGenic =true;
			}
			//GeneEnd
			if (gffCodGene.getGffDetailUp() != null && gffCodGene.getGffDetailUp().isCodInGenEnd(gffCodGene.getCoord())) {
				result[6] ++;flagIntraGenic =true;
			}
			else if ( gffCodGene.getGffDetailDown() != null && gffCodGene.getGffDetailDown().isCodInGenEnd(gffCodGene.getCoord())) {
				result[6] ++;flagIntraGenic =true;
			}
			//Tss
			if ( gffCodGene.getGffDetailUp() != null && !gffCodGene.getGffDetailUp().isCis5to3() 
					&& gffCodGene.getGffDetailUp().getLongestSplit().getCod2Tss(gffCodGene.getCoord()) > this.gffChrAbs.tss[0]  ) {
				result[7] ++;flagIntraGenic =true;
			}
			else if (gffCodGene.getGffDetailDown() != null && gffCodGene.getGffDetailDown().isCis5to3() 
					&& gffCodGene.getGffDetailDown().getLongestSplit().getCod2Tss(gffCodGene.getCoord()) > this.gffChrAbs.tss[0]) {
				result[7] ++;flagIntraGenic =true;
			}
		}
		if (flagIntraGenic == false) {
			result[3] ++;
		}
		return result;
	}
	
	
	
	
	
}
