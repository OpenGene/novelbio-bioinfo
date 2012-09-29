package com.novelbio.analysis.seq.genome;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.mappingOperate.MapInfo;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReads;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * �Ƚ�����bed�ļ�֮�������
 * ��com.novelbio.analysis.project.cdg.CmpTssMethy��ʹ��
 * @author zong0jie
 * 
 */
public class GffChrCmpBed extends GffChrAbs {
	public static void main(String[] args) {
		
	}
	
	
	MapReads mapReads2;

	/**
	 * @param readsFile
	 *            mapping�Ľ���ļ��������Ź���һ��Ϊbed��ʽ
	 * @param binNum
	 *            ÿ������λ����������趨Ϊ1�����㷨��仯��Ȼ���ܾ�ȷ
	 */
	public void setMapReadsCmp(String readsFile2, int binNum) {
		if (FileOperate.isFileExist(readsFile2)) {
			mapReads2 = new MapReads();
			mapReads2.setBedSeq(readsFile2);
			mapReads2.setInvNum(binNum);
			mapReads2.setMapChrID2Len(super.species.getMapChromInfo());
			mapReads2.setNormalType(mapNormType);
			mapReads2.running();
		}
	}

	/**
	 * �Ƚ�����ʵ����ĳ�������TSS����reads�����仯���
	 * @param txtExcelFile
	 * @param colAccID
	 * @param region ���� int[]{-2000, 2000}
	 * @param filterValue
	 * @param bigThan ������ֵ����С����ֵ
	 * @param txtOutFile
	 */
	public void readGeneList(String txtExcelFile, int colAccID,
			int[] region, double filterValue, boolean bigThan, String txtOutFile) {
		colAccID--;
		TxtReadandWrite txtOut = new TxtReadandWrite(txtOutFile, true);
		ArrayList<String[]> lsResult = ExcelTxtRead.readLsExcelTxt(txtExcelFile, 1);
		//û�ж�ȥtitle
		List<String[]> lsTmp = lsResult.subList(1, lsResult.size());
		for (String[] strings : lsTmp) {
			String geneID = strings[colAccID].split("/")[0];
			double tmpCmpResult = compRegionTssXLY(geneID, region);
			if ((bigThan && tmpCmpResult < filterValue)
					||
				(!bigThan && tmpCmpResult > filterValue)
			) {
				continue;
			}
			String[] tmpResult = ArrayOperate.copyArray(strings,
					strings.length + 1);
			tmpResult[tmpResult.length - 1] = tmpCmpResult + "";
			txtOut.writefileln(tmpResult);
		}
		txtOut.close();
	}

	/**
	 * ֱ��Tss������бȽϣ��趨TSSǰ��Χ �ṩ��ֵ
	 * @param geneID
	 * @param region �Ƚ�����
	 * @return
	 */
	private double compRegionTssXLY(String geneID, int[] region) {
		ArrayList<int[]> lsDectect = new ArrayList<int[]>();
		lsDectect.add(region);
		GffGeneIsoInfo gffGeneIsoInfo = gffHashGene.searchISO(geneID);
		ArrayList<int[]> lsTmpTss = gffGeneIsoInfo.getRegionNearTss(lsDectect);
		return compRegion(gffGeneIsoInfo.getChrID(), lsTmpTss)[0];
	}

	/**
	 * �Ƚ�ָ�������reads�ľ�ֵ
	 * 
	 * @param arrayList
	 *            -int[2]���м���int[2]���ͱȽϼ�������
	 * @param filterRegion
	 *            ��ֵ���Ƚϵ������������ж������򳬹���ֵ����Ϊͨ��
	 * @param filterValue
	 *            ��ֵ��ÿ�������mean reads���ٳ�����ֵ����Ϊͨ��
	 * @return ���رȽ�����Ľ����ÿ������һ����ֵ
	 */
	private double[] compRegion(String chrID, List<int[]> lsCmpRegion) {
		double[] result = new double[lsCmpRegion.size()];
		for (int i = 0; i < lsCmpRegion.size(); i++) {
			int[] is = lsCmpRegion.get(i);
			MapInfo mapInfo = new MapInfo(chrID, is[0], is[1]);
			MapReads.CmpMapReg(mapReads, mapReads2, mapInfo);
			result[i] = mapInfo.getScore();
		}
		return result;
	}

}