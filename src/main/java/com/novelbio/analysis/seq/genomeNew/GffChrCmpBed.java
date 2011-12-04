package com.novelbio.analysis.seq.genomeNew;

import java.util.ArrayList;
import java.util.List;

import org.broadinstitute.sting.jna.lsf.v7_0_6.LibBat.newDebugLog;
import org.broadinstitute.sting.utils.collections.CircularArray.Int;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfo;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReads;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * �Ƚ�����bed�ļ�֮�������
 * @author zong0jie
 *
 */
public class GffChrCmpBed extends GffChrAbs{

	public static void main(String[] args) {
		GffChrCmpBed gffChrCmpBed = new GffChrCmpBed(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ, NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM);
		
		gffChrCmpBed.setMapReads("/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/mapping/WEextend_sort.bed", 5);
		
		gffChrCmpBed.setMapReadsCmp("/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/mapping/W-4_Extend.bed", 5);
		gffChrCmpBed.loadMapReads();
		String txtExcelFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/bivalent_anno_-2k+2kTss.xls";
		String txtOutFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/bivalent_anno_WEto4Wdown.xls";
		gffChrCmpBed.readGeneList(txtExcelFile, 6, 1.5, txtOutFile);
		
		
		gffChrCmpBed.setMapReads("/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/mapping/KEextend_Sort.bed", 5);
		gffChrCmpBed.setMapReadsCmp("/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/mapping/K-4_Extend.bed", 5);
		gffChrCmpBed.loadMapReads();
		 txtExcelFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/bivalent_anno_-2k+2kTss.xls";
		 txtOutFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/bivalent_anno_WE_K4notChange.xls";
		gffChrCmpBed.readGeneList(txtExcelFile, 6, 1.15, txtOutFile);
	}
	
	
	public GffChrCmpBed(String gffType, String gffFile, String chrFile) {
		super(gffType, gffFile, chrFile, null, 0);
		// TODO Auto-generated constructor stub
	}
	
	MapReads mapReads2;
	/**
	 * @param readsFile mapping�Ľ���ļ��������Ź���һ��Ϊbed��ʽ
	 * @param binNum ÿ������λ����������趨Ϊ1�����㷨��仯��Ȼ���ܾ�ȷ
	 */
	public void setMapReadsCmp(String readsFile2, int binNum) {
		if (FileOperate.isFileExist(readsFile2)) {
			mapReads2 = new MapReads(binNum, readsFile2);
			mapReads2.setChrLenFile(getRefLenFile());
			mapReads2.setNormalType(mapNormType);
			mapReads2.ReadMapFile();
		}
	}
	
	
	public void readGeneList(String txtExcelFile, int colAccID, double filterValue, String txtOutFile) {
		colAccID--;
		TxtReadandWrite txtOut = new TxtReadandWrite(txtOutFile, true);
		ArrayList<String[]> lsResult = ExcelTxtRead.readLsExcelTxt(txtExcelFile, 1, -1, 1, -1);
		String[] title = lsResult.get(0);
		List<String[]> lsTmp = lsResult.subList(1, lsResult.size());
		for (String[] strings : lsTmp) {
			String geneID = strings[colAccID].split("/")[0];
			if (!compRegionTssXLY(geneID, filterValue)) {
				continue;
			}
			txtOut.writefileln(strings);
		}
	}
	
	/**
	 * ר�ż��û��ͨ�����Ե�
	 * @param txtExcelFile
	 * @param colAccID
	 * @param filterValue
	 * @param txtOutFile
	 */
	public void readGeneListFalse(String txtExcelFile, int colAccID, double filterValue, String txtOutFile) {
		colAccID--;
		TxtReadandWrite txtOut = new TxtReadandWrite(txtOutFile, true);
		ArrayList<String[]> lsResult = ExcelTxtRead.readLsExcelTxt(txtExcelFile, 1, -1, 1, -1);
		String[] title = lsResult.get(0);
		List<String[]> lsTmp = lsResult.subList(1, lsResult.size());
		for (String[] strings : lsTmp) {
			String geneID = strings[colAccID].split("/")[0];
			if (compRegionTssXLY(geneID, filterValue)) {
				continue;
			}
			txtOut.writefileln(strings);
		}
	}
	
	
	
	
	/**
	 * �ֳ�tssǰ2����tss��2��������бȽ�
	 * @param geneID
	 * @return
	 */
	private boolean compRegionTssXLY(String geneID, double filterValue)
	{
		ArrayList<int[]> lsDectect = new ArrayList<int[]>();
		lsDectect.add(new int[]{-3000,3000});
//		lsDectect.add(new int[]{-1000,1000});
//		lsDectect.add(new int[]{1000,3000});
		
		GffGeneIsoInfo gffGeneIsoInfo = gffHashGene.searchISO(geneID);
		ArrayList<int[]> lsTmpTss = gffGeneIsoInfo.getRegionNearTss(lsDectect);
		return compRegion(gffGeneIsoInfo.getChrID(), lsTmpTss, 1, filterValue);
	}
	
	
	
	
	
	/**
	 * �Ƚ�ָ�������reads�ľ�ֵ
	 * @param arrayList-int[2]���м���int[2]���ͱȽϼ�������
	 * @param filterRegion ��ֵ���Ƚϵ������������ж������򳬹���ֵ����Ϊͨ��
	 * @param filterValue ��ֵ��ÿ�������mean reads���ٳ�����ֵ����Ϊͨ��
	 */
	private boolean compRegion(String chrID,List<int[]> lsCmpRegion, int filterRegion, double filterValue) {
		int passNum = 0;
		for (int[] is : lsCmpRegion) {
			MapInfo mapInfo = new MapInfo(chrID, is[0], is[1]);
			MapReads.CmpMapReg(mapReads, mapReads2, mapInfo);
			if (mapInfo.getWeight() >= filterValue) {
				passNum ++;
			}
		}
		if (passNum >= filterRegion) {
			return true;
		}
		return false;
	}
	
	

	
	
}
