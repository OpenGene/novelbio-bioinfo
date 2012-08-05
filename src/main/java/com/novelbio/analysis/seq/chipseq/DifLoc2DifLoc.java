package com.novelbio.analysis.seq.chipseq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.impl.xb.xsdschema.impl.ExplicitGroupImpl;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ListDetailBin;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ListHashBin;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfo;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReads;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.listOperate.ListCodAbs;
import com.novelbio.base.dataStructure.listOperate.ListCodAbsDu;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.species.Species;
import com.novelbio.generalConf.NovelBioConst;

/**
 * ���������ڴ�
 * �о�λ������λ������
 * Ʃ���ȡһ�������miRNA��һ������ļ׻�����Ȼ���о�����������
 * Ҳ�ɶ���������׻������
 * x�᣺�������ratio
 * y�᣺������ļ׻�������sicer-dif���
 * @author zong0jie
 *
 */
public class DifLoc2DifLoc {
	Logger logger = Logger.getLogger(DifLoc2DifLoc.class);

	public static void main(String[] args) {
		DifLoc2DifLoc difLoc2DifLoc = new DifLoc2DifLoc();
		Species species = new Species(39947);
		difLoc2DifLoc.setSpecies(species);
		String bedMethy1 = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/RawData_and_AlignmentResult/mappingFile/2Nextend_sort.bed";
		String bedMethy2 = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/RawData_and_AlignmentResult/mappingFile/3Nextend_sort.bed";
		
		String miRNAtxt = "/media/winE/NBC/Project/Project_ZHY_Lab/�ź���2012/miRNA/miRNAexp/ZHY2Nvs3N_Target.xls";
		
		String outFile = "/media/winE/NBC/Project/Project_ZHY_Lab/�ź���2012/miRNA/miRNAexp/2N-3N.txt";
		
		difLoc2DifLoc.addMapInfo("mthy", bedMethy1, bedMethy2);
		difLoc2DifLoc.readAndCalExpGeneTxt2DifInfo(typeTss, miRNAtxt, 8, 5, 1, outFile, "mthy");
	}
	
	/** ����2K */
	int[] tssRegion = new int[]{-2000,2000};
	/** ��Сֵ����������0 */
	double min = 0.1;
	/**�Ҳ������ź�ֵ��ʲô��ȡ�� */
	double nullValue = 0;
	GffHashGene gffHashGene = new GffHashGene();

	public static int typeTss = 2;
	public static int typeGeneAll = 4;
	public static int typeGeneBody = 8;
	
	int binNum = 20;
	/** ����Bed�ļ�����Ϣ */
	LinkedHashMap<String, ArrayList<MapReads>> mapPrefix2MapReads = new LinkedHashMap<String, ArrayList<MapReads>>();
	/** ����sicerdif����Ϣ */
	HashMap<String, ListHashBin> mapPrefix2listHashBin = new HashMap<String, ListHashBin>();
	/**	�������׵���Ϣ */
	HashMap<String, HashMap<String, Double>> mapPrefix2_MapGeneID2Exp = new HashMap<String, HashMap<String,Double>>();
	Species species;
	int compareType = typeGeneAll;
	/** Ĭ�϶�ȡbed�ļ��Ľ��, false���ȡpeak�ļ��Ľ�� */
	boolean readPeak = false;
	
	ArrayList<String> lsGeneID;
	
	public void setSpecies(Species species) {
		this.species = species;
		gffHashGene = new GffHashGene(species.getGffFile()[0], species.getGffFile()[1]);
	}
	
	public void setTssRegion(int[] tssRegion) {
		this.tssRegion = tssRegion;
	}
	/**
	 * ������Ƚϵ���peak���� mapping �������ô�ͱȽϸ�λ�õķ���
	 * @param compareType
	 */
	public void setCompareType(int compareType) {
		this.compareType = compareType;
	}
	public void setMinAndNullValue(double min, double nullValue) {
		this.min = min;
		this.nullValue = nullValue;
	}
	/**�趨��Ҫ�ȽϵĻ��� */
	public void setLsGeneID(ArrayList<String> lsGeneID) {
		this.lsGeneID = lsGeneID;
	}
	public void setGenomeWide() {
		this.lsGeneID = gffHashGene.getLsNameNoRedundent();
	}
	public void compare(String outFile, String prefix1, String prefix2) {
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		txtOut.writefileln("GeneID\t" + prefix1 + "\t" + prefix2);
		String[] tmpResult = new String[3];
		for (String geneID : lsGeneID) {
			tmpResult[0] = geneID;
			tmpResult[1] = getInfo(geneID, prefix1) + "";
			tmpResult[2] = getInfo(geneID, prefix2) + "";
			txtOut.writefileln(tmpResult);
		}
		txtOut.close();
	}
	
	private Double getInfo(String geneID, String prefix) {
		Double result = null;
		if (mapPrefix2listHashBin.containsKey(prefix)) {
			if (compareType == typeGeneAll) {
				result = getGeneFullLengthPeakSicerDifScore(prefix, geneID);
			}
			else if (compareType == typeGeneBody) {
				result = getGeneBodyPeakSicerDifScore(prefix, geneID);
			}
			else if (compareType == typeTss) {
				result = getGeneTssPeakSicerDifScore(prefix, geneID);
			}
		}
		else if (mapPrefix2MapReads.containsKey(prefix)) {
			if (compareType == typeGeneAll) {
				result = getGeneFullLengthMapRatio(prefix, geneID);
			}
			else if (compareType == typeGeneBody) {
				result = getGeneBodyMapRatio(prefix, geneID);
			}
			else if (compareType == typeTss) {
				result = getGeneTssMapRatio(prefix, geneID);
			}
		}
		else if (mapPrefix2_MapGeneID2Exp.containsKey(prefix)) {
			result = mapPrefix2_MapGeneID2Exp.get(prefix).get(geneID);
		}
		if (result == null) {
			result = nullValue;
		}
		return result;
	}
	/**
	 * ����˳����룬����
	 * �����mapFile1����mapFile2
	 * @param prefix
	 * @param mapFile1
	 * @param mapFile2
	 */
	public void addMapInfo(String prefix, String mapFile1, String mapFile2) {
		ArrayList<MapReads> lsMapReads = new ArrayList<MapReads>();
		MapReads mapReads1 = null;
		if (FileOperate.isFileExist(mapFile1)) {
			mapReads1 = new MapReads();
			mapReads1.setInvNum(binNum);
			mapReads1.setBedSeq(mapFile1);
			mapReads1.setMapChrID2Len(species.getMapChromInfo());
			
			mapReads1.running();
			lsMapReads.add(mapReads1);
		}
		
		MapReads mapReads2 = null;
		if (FileOperate.isFileExist(mapFile2)) {
			mapReads2 = new MapReads();
			mapReads2.setInvNum(binNum);
			mapReads2.setBedSeq(mapFile2);
			mapReads2.setMapChrID2Len(species.getMapChromInfo());
			
			mapReads2.running();
			lsMapReads.add(mapReads2);
		}
		mapPrefix2MapReads.put(prefix, lsMapReads);
	}
	/**
	 * ��ȡsicer�ļ���score���������������sicer��scoreȥ����������ô�Ͳ��ø÷���
	 * @param sicerFile
	 * @param colScore ��ֵ�������Ϣ
	 */
	public void addSicerScore(String prefix, String sicerFile, int colScore) {
		ListHashBin listHashBin = new ListHashBin(true, 1, 2, 3, 2);
		listHashBin.setColScore(colScore);
		listHashBin.ReadGffarray(sicerFile);
		mapPrefix2listHashBin.put(prefix, listHashBin);
	}
	/**
	 * ���excelTxtFile1����excelTxtFile2�ı�ֵ��ĳ������ֻ��һ���У�����д1��
	 * ���ֻ��һ��excelTxtFile������д��excelTxtFile��ֵ
	 * @param excelTxtFile1
	 * @param excelTxtFile2
	 * @param colGeneID
	 * @param colDifExp
	 */
	public void addGeneExp(String prefix, String excelTxtFile1, String excelTxtFile2, int colGeneID, int colDifExp) {
		HashMap<String, Double> mapGeneID2Exp1 = null, mapGeneID2Exp2 = null;
		if (FileOperate.isFileExistAndBigThanSize(excelTxtFile1, 100)) {
			ArrayList<String[]> lsGene2Exp = ExcelTxtRead.readLsExcelTxt(excelTxtFile1, new int[]{colGeneID, colDifExp}, 1, -1);
			mapGeneID2Exp1 = getMapGene2Exp(lsGene2Exp);
		}
		if (FileOperate.isFileExistAndBigThanSize(excelTxtFile2, 100)) {
			ArrayList<String[]> lsGene2Exp = ExcelTxtRead.readLsExcelTxt(excelTxtFile2, new int[]{colGeneID, colDifExp}, 1, -1);
			mapGeneID2Exp2 = getMapGene2Exp(lsGene2Exp);
		}
		if (mapGeneID2Exp1 == null && mapGeneID2Exp2 != null) {
			mapPrefix2_MapGeneID2Exp.put(prefix, mapGeneID2Exp2);
		}
		else if (mapGeneID2Exp1 != null && mapGeneID2Exp2 == null) {
			mapPrefix2_MapGeneID2Exp.put(prefix, mapGeneID2Exp1);
		}
		else if (mapGeneID2Exp1 != null && mapGeneID2Exp2 != null) {
			HashMap<String, Double> mapGene2Ratio = getMapGene2Dif(mapGeneID2Exp1, mapGeneID2Exp2);
			mapPrefix2_MapGeneID2Exp.put(prefix, mapGene2Ratio);
		}
	}
	/**
	 * �趨�����mapGeneID2Exp
	 * @param lsGene2Exp 0�� geneID  1��geneExp
	 * @param mapGeneID2Exp
	 */
	private HashMap<String, Double> getMapGene2Exp(ArrayList<String[]> lsGene2Exp) {
		 HashMap<String, Double> mapGeneID2Exp = new HashMap<String, Double>();
		for (String[] strings : lsGene2Exp) {
			String geneID = strings[0];
			Double exp = 0.0;
			try {
				exp = Double.parseDouble(strings[1]);
			} catch (Exception e) {
				continue;
			}
			mapGeneID2Exp.put(geneID, exp);
		}
		return mapGeneID2Exp;
	}
	
	private HashMap<String, Double> getMapGene2Dif(HashMap<String, Double> mapGeneID2Exp1, HashMap<String, Double> mapGeneID2Exp2) {
		HashMap<String, Double> mapGeneID2Ratio = new HashMap<String, Double>();
		for (String geneID : mapGeneID2Exp1.keySet()) {
			Double exp1 = mapGeneID2Exp1.get(geneID);
			Double exp2 = mapGeneID2Exp2.get(geneID);
			double ratio = 1;
			if (exp1 != null && exp2 != null) {
				if (exp2 == 0) {
					exp2 = exp2 + min;
				}
				ratio = exp1/exp2;
			}
			mapGeneID2Ratio.put(geneID, ratio);
		}
		return mapGeneID2Ratio;
	}
	/**
	 * 
	 * @param excelTxtFile
	 * @param colGeneID geneID��
	 * @param colExp score��
	 * @param rowStart
	 * @param txtOutTss
	 * @param txtOutGeneBody
	 * @param mapPrix
	 */
	public void readAndCalExpGeneTxt2DifInfo(int type, String excelTxtFile, int colGeneID, int colDifExp, int rowStart, String txtOut, String mapPrix) {
		ArrayList<String[]> lsGene2Ratio = ExcelTxtRead.readLsExcelTxt(excelTxtFile, new int[]{colGeneID, colDifExp}, 1, -1);
//		calculateDifExpGene2DifInfo(type, lsGene2Ratio, rowStart, txtOut, mapPrix);
	}
	
//	/**
//	 * @param lsGene2Ratio 0: geneID 1��ratio/exp
//	 * @param rowStart
//	 * @param txtOutTss
//	 * @param txtOutGeneBody
//	 */
//	private void calculateDifExpGene2DifInfo(int type, ArrayList<String[]> lsGene2Ratio, int rowStart, String txtOut, String mapPrix) {
//		ArrayList<String[]> lsOut = new ArrayList<String[]>();
//		lsOut.add(new String[]{"geneID", mapPrix, "methylation"});
//		for (String[] strings : lsGene2Ratio) {
//			Double Info = null;
//			if (readPeak) {
//				if (type == typeTss) {
//					Info = getGeneTssPeakSicerDifScore(strings[0]);
//				}
//				else if (type == typeGeneAll) {
//					Info = getGeneAllPeakSicerDifScore(strings[0]);
//				}
//				else if (type == typeGeneBody) {
//					Info = getGeneBodyPeakSicerDifScore(strings[0]);
//				}
//			}
//			else {
//				if (type == typeTss) {
//					Info = getGeneTssMapRatio(mapPrix, strings[0]);
//				} else if (type == typeGeneAll) {
//					Info = getGeneFullLengthMapRatio(mapPrix, strings[0]);
//				} else if (type == typeGeneBody) {
//					Info = getGeneBodyMapRatio(mapPrix, strings[0]);
//				}
//			}
//			if (Info != null) {
//				String[] strTss = new String[]{strings[0], strings[1], Info + ""};
//				lsOut.add(strTss);
//			}
//		}
//		TxtReadandWrite txt = new TxtReadandWrite(txtOut, true);
//		txt.ExcelWrite(lsOut, "\t", 1, 1);
//		txt.close();
//	}
	/**
	 * ȫ�����������, prefix1��ratio �� prefix2��ratio
	 * @param type ���ͣ���tss����geneEnd
	 * @param prefix1 ��һ�����ͣ���sRNA
	 * @param prefix2 �ڶ������ͣ���methylation
	 * @param lsAccID 
	 * @param txtOutInfo
	 */
	public void compareDifAllGene(int type, String prefix1, String prefix2,  String txtOutInfo) {
		ArrayList<String> lsGeneID = gffHashGene.getLsNameNoRedundent();
		compareDifInfoGene(type, prefix1, prefix2, lsGeneID, txtOutInfo);
	}
	/**
	 * @param type ���ͣ���tss����geneEnd
	 * @param prefix1 ��һ�����ͣ���sRNA
	 * @param prefix2 �ڶ������ͣ���methylation
	 * @param lsGeneID 
	 * @param txtOutInfo
	 */
	public void compareDifInfoGene(int type, String prefix1, String prefix2, ArrayList<String> lsGeneID, String txtOutInfo) {
		ArrayList<String[]> lsOutGeneInfo = new ArrayList<String[]>();
		for (String strings : lsGeneID) {
			Double tssInfo1 = null,tssInfo2 = null;
			if (type == typeTss) {
				tssInfo1 = getGeneTssMapRatio(prefix1, strings);
				tssInfo2 = getGeneTssMapRatio(prefix2, strings);
			}
			else if (type == typeGeneBody) {
				tssInfo1 = getGeneBodyMapRatio(prefix1, strings);
				tssInfo2 = getGeneBodyMapRatio(prefix2, strings);
			}
			else if (type == typeGeneAll) {
				tssInfo1 = getGeneFullLengthMapRatio(prefix1, strings);
				tssInfo2 = getGeneFullLengthMapRatio(prefix2, strings);
			}
			if (tssInfo1 != null && tssInfo2 != null) {
				String[] strTss = new String[]{strings, tssInfo1 + "", tssInfo2 + ""};
				lsOutGeneInfo.add(strTss);
			}
		}
		TxtReadandWrite txtInfo = new TxtReadandWrite(txtOutInfo, true);
		txtInfo.ExcelWrite(lsOutGeneInfo, "\t", 1, 1);
		txtInfo.close();
	}
	
	/**
	 * �������򣬻�øû���ȫ������ķ���
	 * @param prefix
	 * @param geneID
	 * @return ���а����û���tss����׻�����ֵ
	 */
	private Double getGeneFullLengthPeakSicerDifScore(String prefix, String geneID) {
		GffGeneIsoInfo gffGeneIsoInfo = gffHashGene.searchISO(geneID);
		if (gffGeneIsoInfo == null) {
			return null;
		}
		int startTmp = 0;
		if (gffGeneIsoInfo.isCis5to3()) {
			startTmp = gffGeneIsoInfo.getTSSsite() - tssRegion[0];
		}
		else {
			startTmp = gffGeneIsoInfo.getTSSsite() + tssRegion[0];
		}
		int start = Math.min(startTmp, gffGeneIsoInfo.getTESsite());
		int end = Math.max(startTmp, gffGeneIsoInfo.getTESsite());

		ListCodAbsDu<ListDetailBin, ListCodAbs<ListDetailBin>> lsDu = mapPrefix2listHashBin.get(prefix).searchLocation(gffGeneIsoInfo.getChrID(), start, end);
		ArrayList<ListDetailBin> lsBin = lsDu.getAllGffDetail();
		if (lsBin.size() == 0) {
			return 1.0;
		}
		Double score = 0.0;
		for (ListDetailBin listDetailBin : lsBin) {
			score = score + listDetailBin.getScore();
		}
		return score/lsBin.size();
	}
	
	
	/**
	 * �������򣬻�øû���tss����sicer-dif�ķ���
	 * @param geneID
	 * @return ���а����û���tss����׻�����ֵ
	 */
	private Double getGeneBodyPeakSicerDifScore(String prefix, String geneID) {
		GffGeneIsoInfo gffGeneIsoInfo = gffHashGene.searchISO(geneID);
		if (gffGeneIsoInfo == null) {
			return null;
		}
		int start = 0, end = 0;
		if (gffGeneIsoInfo.isCis5to3()) {
			start = gffGeneIsoInfo.getTSSsite() + tssRegion[1];
		}
		else {
			start = gffGeneIsoInfo.getTSSsite() - tssRegion[1];
		}
		end = gffGeneIsoInfo.getTESsite();
		
		ListCodAbsDu<ListDetailBin, ListCodAbs<ListDetailBin>> lsDu = mapPrefix2listHashBin.get(prefix).searchLocation(gffGeneIsoInfo.getChrID(), Math.min(start, end), Math.max(start, end));
		ArrayList<ListDetailBin> lsBin = lsDu.getAllGffDetail();
		if (lsBin.size() == 0) {
			return 1.0;
		}
		double score = 0;
		for (ListDetailBin listDetailBin : lsBin) {
			score = score + listDetailBin.getScore();
		}
		return score/lsBin.size();
	}
	/**
	 * �������򣬻�øû���tss����sicer-dif�ķ���
	 * @param geneID
	 * @return ���а����û���tss����׻�����ֵ
	 */
	private Double getGeneTssPeakSicerDifScore(String prefix, String geneID) {
		GffGeneIsoInfo gffGeneIsoInfo = gffHashGene.searchISO(geneID);
		if (gffGeneIsoInfo == null) {
			return null;
		}
		int start = 0, end = 0;
		if (gffGeneIsoInfo.isCis5to3()) {
			start = gffGeneIsoInfo.getTSSsite() + tssRegion[0];
			end = gffGeneIsoInfo.getTSSsite() + tssRegion[1];
		}
		else {
			start = gffGeneIsoInfo.getTSSsite() - tssRegion[0];
			end = gffGeneIsoInfo.getTSSsite() - tssRegion[1];
		}
		ListCodAbsDu<ListDetailBin, ListCodAbs<ListDetailBin>> lsDu = mapPrefix2listHashBin.get(prefix).searchLocation(gffGeneIsoInfo.getChrID(), start, end);
		ArrayList<ListDetailBin> lsBin = lsDu.getAllGffDetail();
		if (lsBin.size() == 0) {
			return 1.0;
		}
		double score = 0;
		for (ListDetailBin listDetailBin : lsBin) {
			score = score + listDetailBin.getScore();
		}
		return score/lsBin.size();
	}
	/**
	 * �������򣬻�øû���tss����sicer-dif�ķ���
	 * @param geneID
	 * @return ���а����û���tss����׻�����ֵ
	 */
	private Double getGeneBodyMapRatio(String prefix, String geneID) {
		GffGeneIsoInfo gffGeneIsoInfo = gffHashGene.searchISO(geneID);
		if (gffGeneIsoInfo == null) {
			return null;
		}
		MapInfo mapInfo = new MapInfo(gffGeneIsoInfo.getChrID());
		int start = 0, end = 0;
		if (gffGeneIsoInfo.isCis5to3()) {
			start = gffGeneIsoInfo.getTSSsite() + tssRegion[0];
		}
		else {
			start = gffGeneIsoInfo.getTSSsite() - tssRegion[0];
		}
		end = gffGeneIsoInfo.getTESsite();
		mapInfo.setStartEndLoc(start, end);
		ArrayList<MapReads> lsMapReads = mapPrefix2MapReads.get(prefix);
		return getRatio(mapInfo, lsMapReads);
	}
	
	/**
	 * �������򣬻�øû���tss����sicer-dif�ķ���
	 * @param geneID
	 * @return ���а����û���tss����׻�����ֵ
	 */
	private Double getGeneTssMapRatio(String prefix, String geneID) {
		GffGeneIsoInfo gffGeneIsoInfo = gffHashGene.searchISO(geneID);
		if (gffGeneIsoInfo == null) {
			return null;
		}
		int start = 0, end = 0;
		if (gffGeneIsoInfo.isCis5to3()) {
			start = gffGeneIsoInfo.getTSSsite() + tssRegion[0];
			end = gffGeneIsoInfo.getTSSsite() + tssRegion[1];
		}
		else {
			start = gffGeneIsoInfo.getTSSsite() - tssRegion[0];
			end = gffGeneIsoInfo.getTSSsite() - tssRegion[1];
		}
		MapInfo mapInfo = new MapInfo(gffGeneIsoInfo.getChrID(),start, end);
		ArrayList<MapReads> lsMapReads = mapPrefix2MapReads.get(prefix);
		return getRatio(mapInfo, lsMapReads);
	}
	
	/**
	 * �������򣬻�øû���ȫ������ķ���
	 * @param geneID
	 * @return ���а����û���tss����׻�����ֵ
	 */
	private Double getGeneFullLengthMapRatio(String prefix, String geneID) {
		GffGeneIsoInfo gffGeneIsoInfo = gffHashGene.searchISO(geneID);
		if (gffGeneIsoInfo == null) {
			return null;
		}
		int start = 0;
		if (gffGeneIsoInfo.isCis5to3()) {
			start = gffGeneIsoInfo.getTSSsite() - tssRegion[0];
		}
		else {
			start = gffGeneIsoInfo.getTSSsite() + tssRegion[0];
		}
		MapInfo mapInfo = new MapInfo(gffGeneIsoInfo.getChrID(),start, gffGeneIsoInfo.getTESsite());
		ArrayList<MapReads> lsMapReads = mapPrefix2MapReads.get(prefix);
		return getRatio(mapInfo, lsMapReads);
	}
	
	private Double getRatio(MapInfo mapInfo, ArrayList<MapReads> lsMapReads) {
		lsMapReads.get(0).getRegion(mapInfo, 20, 0);
		if (mapInfo.getDouble() == null) {
			logger.error("������δ֪ID��" + mapInfo.getRefID() + " " + mapInfo.getStartAbs() + " " + mapInfo.getEndAbs());
			return null;
		}
		Double score1 = mapInfo.getMean();
		if (score1 == null) {
			return null;
		}
		if (lsMapReads.size() == 1) {
			return score1;
		}
		lsMapReads.get(1).getRegion(mapInfo, 20, 0);
		Double score2 = mapInfo.getMean();
		return (score1 + 1)/(score2 + 1);
	}
	
}
