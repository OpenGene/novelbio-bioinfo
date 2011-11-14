package com.novelbio.analysis.seq.genomeNew;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math.stat.descriptive.moment.ThirdMoment;
import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFastaHash;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqHash;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoCis;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfo;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReads;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
/**
 * GffHashGene��SeqHash����static��Ҳ����һ��ֻ�ܶ�һ�����ֽ��з���
 * MapReads����static��Ҳ���ǿ���ͬʱ������mapping�ļ�
 * @author zong0jie
 *
 */
public class GffChrAbs {
private static final Logger logger = Logger.getLogger(GffChrGene.class);
	
	private int taxID = 0;
	private int distanceMapInfo = 3000;
	static GffHashGene gffHashGene = null;
			
	static SeqHash seqHash = null;
	
	MapReads mapReads = null;
//	/**
//	 * �趨һϵ�е�����λ��
//	 */
//	ArrayList<MapInfo> lsMapInfos = null;
//	/**
//	 * �趨������Ҫ���з�����MapInfo list
//	 * @param lsMapInfos
//	 */
//	public void setLsMapInfos(ArrayList<MapInfo> lsMapInfos) {
//		this.lsMapInfos = lsMapInfos;
//	}
//	/**
//	 * ��ñ�����Ҫ������MapInfo list
//	 * @return
//	 */
//	public ArrayList<MapInfo> getLsMapInfos() {
//		return lsMapInfos;
//	}
	public static GffHashGene getGffHashGene() {
		return gffHashGene;
	}
	public static SeqHash getSeqHash() {
		return seqHash;
	}
	public MapReads getMapReads() {
		return mapReads;
	}
	
	/**
	 * ��׼��������Ĭ��Ϊ����׼��
	 */
	int mapNormType = MapReads.NORMALIZATION_NO;
	/**
	 * ��Ҫ���ڻ�ͼ
	 */
	int upBp = 5000;//tss��tes�Լ�����λ������γ��ȣ�Ĭ��5000
	int downBp = 5000;//tss��tes�Լ�����λ������γ��ȣ�Ĭ��5000
	
	int tssUpBp = 3000;
	int tssDownBp = 2000;
	int geneEnd3UTR = 100;
	
	
	static String chrFile = "";
	static String chrRegx = null;
	
	/**
	 * 
	 * @param gffType
	 * @param gffFile
	 * @param chrFile
	 * @param readsBed
	 * @param binNum ÿ������λ����������趨Ϊ1�����㷨��仯��Ȼ���ܾ�ȷ
	 */
	public GffChrAbs(String gffType, String gffFile, String chrFile,String readsBed ,int binNum)
	{
		setGffFile(gffType, gffFile);
		setChrFile(chrFile, null);
		this.setMapReads(readsBed, binNum);
	}
	/**
	 * @param gffType
	 * @param gffFile
	 * @param chrFile �����ļ��������ļ���
	 * @param regx ����������ļ������ø�������ʽ��ȡÿ�����е����֣�����������ļ��У�
	   ���ø�������ʽ��ȡ���и��ļ������ļ� ���ļ�Ĭ��Ϊ"";�ļ���Ĭ��Ϊ"\\bchr\\w*"��
	 * @param readsBed
	 * @param binNum ÿ������λ����������趨Ϊ1�����㷨��仯��Ȼ���ܾ�ȷ

	 */
	public GffChrAbs(String gffType, String gffFile, String chrFile, String regx, String readsBed, int binNum)
	{
		setGffFile(gffType, gffFile);
		setChrFile(chrFile, regx);
		this.setMapReads(readsBed, binNum);
	}
	/**
	 * �趨mapreads�ı�׼������
	 * @param mapNormType �� MapReads.NORMALIZATION_ALL_READS ��ѡ��Ĭ��MapReads.NORMALIZATION_ALL_READS
	 */
	public void setMapNormType(int mapNormType) {
		this.mapNormType = mapNormType;
		if (mapReads != null) {
			mapReads.setNormalType(mapNormType);
		}
	}
	/**
	 * ר�����ڻ�ͼʱ�Ĳ���
	 * @param upBp tss��tes�Լ�����λ������γ��ȣ�Ĭ��5000
	 * @param downBp tss��tes�Լ�����λ������γ��ȣ�Ĭ��5000
	 */
	public void setPlotRegion(int upBp, int downBp) {
		this.upBp = upBp;
		this.downBp = downBp;
	}
	/**
	 * ר�����ڻ���λʱ�Ĳ���
	 * �趨����Ķ�λ������Ϣ
	 * @param tssUpBp �趨�����ת¼������γ��ȣ�Ĭ��Ϊ3000bp
	 * @param tssDownBp �趨�����ת¼������γ��ȣ�Ĭ��Ϊ2000bp
	 * @param geneEnd3UTR �趨�����β��������ĳ��ȣ�Ĭ��Ϊ100bp
	 */
	public void setGeneRange(int tssUpBp, int tssDownBp, int geneEnd3UTR) {
		this.tssUpBp = tssDownBp;
		this.tssDownBp = tssDownBp;
		this.geneEnd3UTR = geneEnd3UTR;
		GffDetailGene.setCodLocation(tssUpBp, tssDownBp, geneEnd3UTR);
	}


	
	public static void setGffFile(String gffType, String gffFile)
	{
		if (FileOperate.isFileExist(gffFile)) {
			gffHashGene = new GffHashGene(gffType, gffFile);
		}
	}
	
	/**
	 chrFile �����ļ��������ļ���
    regx ����������ļ������ø�������ʽ��ȡÿ�����е����֣�����������ļ��У�
    ���ø�������ʽ��ȡ���и��ļ������ļ� ���ļ�Ĭ��Ϊ"";�ļ���Ĭ��Ϊ"\\bchr\\w*"��
	 * @param chrFile
	 * @param regx
	 */
	public static void setChrFile(String chrFile, String regx) {
		GffChrAbs.chrFile = chrFile;
		GffChrAbs.chrRegx = regx;
	}
	
	public void loadChrFile() {
		if (FileOperate.isFileExist(chrFile) || FileOperate.isFileDirectory(chrFile)) {
			 seqHash = new SeqHash(chrFile, chrRegx);
		}
	}
	
	/**
	 * @param readsFile mapping�Ľ���ļ��������Ź���һ��Ϊbed��ʽ
	 * @param binNum ÿ������λ����������趨Ϊ1�����㷨��仯��Ȼ���ܾ�ȷ
	 */
	public void setMapReads(String readsFile, int binNum) {
		if (FileOperate.isFileExist(readsFile)) {
			mapReads = new MapReads(binNum, readsFile);
			mapReads.setChrLenFile(getRefLenFile());
			mapReads.setNormalType(mapNormType);
		}
	}
	
	public void loadMapReads() {
		try {
			mapReads.ReadMapFile();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * ���ָ���ļ��ڵ�������Ϣ
	 * �������λ���յ�ļ����distanceMapInfo���ڣ��ͻ�ɾ���Ǹ�Ȩ�ص͵�
	 * @param txtExcel
	 * @param colChrID
	 * @param colStartLoc
	 * @param colEndLoc
	 * @param colScore ��֣�Ҳ����Ȩ�أ�û�и��еĻ���������Ϊ0
	 * @param rowStart
	 */
	public ArrayList<MapInfo> readFileRegionMapInfo(String txtExcel, int colChrID, int colStartLoc, int colEndLoc, int colScore,int rowStart)
	{
		int[] columnID = null;
		if (colScore <= 0 ) {
			 columnID = new int[]{colChrID,colStartLoc,colEndLoc};
		}
		else {
			columnID = new int[]{colChrID,colStartLoc,colEndLoc, colScore};
		}
		ArrayList<String[]> lstmp = ExcelTxtRead.readLsExcelTxt(txtExcel, columnID, rowStart, 0);
		ArrayList<MapInfo> lsMapInfos = new ArrayList<MapInfo>();
		for (String[] strings : lstmp) {
			MapInfo mapInfo = new MapInfo(strings[0]);
			
			try {
				mapInfo.setStartLoc(Integer.parseInt(strings[1]));
				mapInfo.setEndLoc(Integer.parseInt(strings[2]));
			} catch (Exception e) {
				logger.error("�����������⣺"+mapInfo.getChrID());
				continue;
			}
		
			if (mapInfo.getStart() <0 && mapInfo.getStart() > -1000) {
				mapInfo.setStartLoc(0);;
			}
			if (colScore > 0) {
				mapInfo.setWeight(Double.parseDouble(strings[3]));
			}
			lsMapInfos.add(mapInfo);
		}
		MapInfo.sortLsMapInfo(lsMapInfos, distanceMapInfo);
		return lsMapInfos;
	}
	/**
	 * ����reads���MapInfo
	 * ���summit���˸�region�������ܹ�����region*2+1������
	 * �������λ���յ�ļ����distanceMapInfo���ڣ��ͻ�ɾ���Ǹ�Ȩ�ص͵�
	 * @param txtExcel
	 * @param region
	 * @param colChrID
	 * @param colSummit
	 * @param rowStart
	 */
	public ArrayList<MapInfo> readFileSiteMapInfo(String txtExcel,int region ,int colChrID, int colSummit, int colScore, int rowStart)
	{
		int[] columnID = null;
		if (colScore <= 0 ) {
			 columnID = new int[]{colChrID, colSummit, colScore};
		}
		else {
			columnID = new int[]{colChrID, colSummit, colScore};
		}
		ArrayList<String[]> lstmp = ExcelTxtRead.readLsExcelTxt(txtExcel, columnID, rowStart, 0);
		ArrayList<MapInfo> lsMapInfos = new ArrayList<MapInfo>();
		for (String[] strings : lstmp) {
			MapInfo mapInfo = new MapInfo(strings[0]);
			try {
				mapInfo.setFlagLoc(Integer.parseInt(strings[1]));
			} catch (Exception e) {
				logger.error("�����������⣺"+mapInfo.getChrID());
				continue;
			}
			mapInfo.setStartLoc(mapInfo.getFlagSite() - region);
			mapInfo.setEndLoc(mapInfo.getFlagSite() + region);
			if (mapInfo.getStart() <0 && mapInfo.getStart() > -1000) {
				mapInfo.setStartLoc(0);;
			}
			if (colScore > 0) {
				mapInfo.setWeight(Double.parseDouble(strings[2]));
			}
			lsMapInfos.add(mapInfo);
		}
		MapInfo.sortLsMapInfo(lsMapInfos, distanceMapInfo);
		return lsMapInfos;
	}
	
	/**
	 * ���������Զ���û���
	 * ����ǰ���趨upBp��downBp
	 * @param lsMapInfos
	 * @param structure GffDetailGene.TSS��
	 * @param binNum �ֳɼ���
	 * @return
	 */
	public ArrayList<MapInfo> getPeakCoveredGeneMapInfo(ArrayList<MapInfo> lsMapInfos, String structure, int binNum)
	{
		HashMap<GffDetailGene,Double>  hashGffDetailGenes = getPeakGeneStructure(lsMapInfos, structure);
		 ArrayList<MapInfo> lsResult = getMapInfoFromGffGene(hashGffDetailGenes, structure);
		 mapReads.getRegionLs(binNum, lsResult, 0);
		 return lsResult;
	}

	/**
	 * ���geneID�Լ���ӦȨ�أ��ڲ��Զ�ȥ���࣬����Ȩ�ظߵ��Ǹ������������Ӧ��reads
	 * һ�����ڸ���gene express ��heapmapͼ
	 * @param txtExcel
	 * @param colGeneID
	 * @param colScore
	 * @param rowStart
	 * @param Structure ������ĸ����ֵĽṹ
	 * @param binNum ������ֳɼ���
	 */
	public ArrayList<MapInfo> readFileGeneMapInfo(String txtExcel,int colGeneID, int colScore, int rowStart, String Structure, int binNum)
	{
		////////////////////     �� �� ��   ////////////////////////////////////////////
		int[] columnID = null;
		if (colScore <= 0 ) {
			 columnID = new int[]{colGeneID};
		}
		else {
			columnID = new int[]{colGeneID, colScore};
		}	
		ArrayList<String[]> lstmp = ExcelTxtRead.readLsExcelTxt(txtExcel, columnID, rowStart, 0);
		return getLsGeneMapInfo(lstmp, Structure, binNum);
	}
	
	/**
	 * ���geneID�Լ���ӦȨ�أ��ڲ��Զ�ȥ���࣬����Ȩ�ظߵ��Ǹ������������Ӧ��reads
	 * һ�����ڸ���gene express ��heapmapͼ
	 * @param lsGeneValue string[2] 0:geneID 1:value ����1 ����û�У���ô����string[1] 0:geneID
	 * @param rowStart
	 * @param Structure ������ĸ����ֵĽṹ
	 * @param binNum ������ֳɼ���
	 * @return
	 */
	public ArrayList<MapInfo> getLsGeneMapInfo(ArrayList<String[]> lsGeneValue, String Structure, int binNum) {
 		HashMap<GffDetailGene, Double> hashGene2Value = new HashMap<GffDetailGene, Double>();
		for (String[] strings : lsGeneValue) {
			GffDetailGene gffDetailGene = gffHashGene.searchLOC(strings[0]);
			if (gffDetailGene == null) {
				continue;
			}
			if (hashGene2Value.containsKey(gffDetailGene)) {
				if (strings.length > 1) {
					double score = Double.parseDouble(strings[1]);
					if (MapInfo.isMin2max()) {
						if (hashGene2Value.get(gffDetailGene) < score) {
							hashGene2Value.put(gffDetailGene, score);
						}
					}
					else {
						if (hashGene2Value.get(gffDetailGene) > score) {
							hashGene2Value.put(gffDetailGene, score);
						}
					}
				}
			} else {
				if (strings.length > 1) {
					hashGene2Value.put(gffDetailGene, Double.parseDouble(strings[1]));
				} else {
					hashGene2Value.put(gffDetailGene, 0.0);
				}
			}
		}
		ArrayList<MapInfo> lsMapInfoGene = getMapInfoFromGffGene(hashGene2Value, Structure);
		mapReads.getRegionLs(binNum, lsMapInfoGene, 0);
		return lsMapInfoGene;
	}
	
	
	
	
	
	
	
	/**
	 * ����ǰ���趨upBp��downBp
	 * ����һϵ��gffDetailGene���Լ���Ҫ�Ĳ��֣����ض�Ӧ�����LsMapInfo
	 * <b>ע������û�����reads��double[] value</b>
	 * @param setgffDetailGenes
	 * @param structure
	 * @return
	 */
	private ArrayList<MapInfo> getMapInfoFromGffGene(HashMap<GffDetailGene,Double> setgffDetailGenes, String structure)
	{
		ArrayList<MapInfo> lsMapInfos = new ArrayList<MapInfo>();
		for (Entry<GffDetailGene, Double> gffDetailValue : setgffDetailGenes.entrySet()) {
			lsMapInfos.add(getStructureLoc(gffDetailValue.getKey(),gffDetailValue.getValue(), structure));
		}
		return lsMapInfos;
	}

	
	/**
	 * ����peak����Ϣ��chrID������յ㣬���ر�peak���ǵ�Tss�Ļ������͸��������������Tssͼ
	 * �Զ�ȥ�������
	 * @param lsPeakInfo mapInfo������ chrID �� startLoc �� endLoc ����
	 */
	private HashMap<GffDetailGene,Double> getPeakGeneStructure(ArrayList<MapInfo> lsMapInfos, String structure) {
		//�洢�����������
		HashMap<GffDetailGene,Double> hashGffDetailGenes = new HashMap<GffDetailGene,Double>();
		for (MapInfo mapInfo : lsMapInfos) {
			if (mapInfo.getStart() <0 && mapInfo.getStart() > -1000) {
				mapInfo.setStartLoc(0);;
			}
			Set<GffDetailGene> setGffDetailGene = getPeakStructureGene(mapInfo.getChrID(), mapInfo.getStart(), mapInfo.getEnd(), structure );
			for (GffDetailGene gffDetailGene : setGffDetailGene) {
				if (hashGffDetailGenes.containsKey(gffDetailGene)) {
					if (MapInfo.isMin2max()) {
						if (mapInfo.getWeight() < hashGffDetailGenes.get(gffDetailGene)) {
							hashGffDetailGenes.put(gffDetailGene, mapInfo.getWeight());
						}
					}
					else {
						if (mapInfo.getWeight() > hashGffDetailGenes.get(gffDetailGene)) {
							hashGffDetailGenes.put(gffDetailGene, mapInfo.getWeight());
						}
					}
				}
				else
					hashGffDetailGenes.put(gffDetailGene, mapInfo.getWeight());
			}
		}
		return hashGffDetailGenes;
	}
	/**
	 * �����������򣬷��ظ�peak�����ǵ�GffDetailGene
	 * @param chrID
	 * @param startLoc
	 * @param endLoc
	 * @param structure
	 * @return
	 */
	private Set<GffDetailGene> getPeakStructureGene(String chrID, int startLoc, int endLoc, String structure) {
		GffCodGeneDU gffCodGeneDU = gffHashGene.searchLocation(chrID, startLoc, endLoc);
		if (structure.equals(GffDetailGene.TSS)) {
			return gffCodGeneDU.getTSSGene();
		}
		else if (structure.equals(GffDetailGene.TES)) {
			return gffCodGeneDU.getTESGene();
		}
		else {
			logger.error("��ʱû�г�Tss��Tes֮��Ļ���ṹ");
			return null;
		}
	}
	/**
	 * ǰ���趨upBp��downBp
	 * ����gffDetailGene���Լ���Ҫ�Ĳ��֣����ض�Ӧ�����MapInfo
	 * <b>ע������û�����reads��double[] value</b>
	 * @param gffDetailGene
	 * @param value �û�������Ӧ����ֵ
	 * @param structure GffDetailGene.TSS��
	 * @return
	 */
	private MapInfo getStructureLoc(GffDetailGene gffDetailGene, Double value,String structure)
	{
		if (structure.equals(GffDetailGene.TSS)) {
			int tss = gffDetailGene.getLongestSplit().getTSSsite();
			MapInfo mapInfo = null;
			if (gffDetailGene.isCis5to3())
				mapInfo = new MapInfo(gffDetailGene.getChrID(), tss - upBp, tss + downBp, tss,0, gffDetailGene.getLongestSplit().getIsoName());
			else 
				mapInfo = new MapInfo(gffDetailGene.getChrID(), tss - downBp, tss + upBp, tss, 0, gffDetailGene.getLongestSplit().getIsoName());
			mapInfo.setCis5to3(gffDetailGene.isCis5to3());
			mapInfo.setWeight(value);
			return mapInfo;
		}
		else if (structure.equals(GffDetailGene.TES)) {
			int tes = gffDetailGene.getLongestSplit().getTESsite();
			MapInfo mapInfo = null;
			if (gffDetailGene.isCis5to3())
				mapInfo = new MapInfo(gffDetailGene.getChrID(), tes - upBp, tes + downBp, tes, 0, gffDetailGene.getLongestSplit().getIsoName());
			else 
				mapInfo = new MapInfo(gffDetailGene.getChrID(), tes - downBp, tes + upBp, tes, 0, gffDetailGene.getLongestSplit().getIsoName());
			mapInfo.setCis5to3(gffDetailGene.isCis5to3());
			mapInfo.setWeight(value);
			return mapInfo;
		}
		else {
			logger.error("��û��Ӹ������͵�structure");
			return null;
		}
	}
	/**
	 * ���ÿ��ת¼���ĳ��ȣ����outFile�����ڣ���ô�������һ��outFile���ļ���
	 * ����SeqHash�����Ǵ��ڵ�
	 * @param RefSeqFile
	 * @return
	 */
	public String getRefLenFile()
	{
		String outFile = FileOperate.changeFileSuffix(chrFile, "_chrLen", "list");
		if (FileOperate.isFileExist(outFile)) {
			return outFile;
		}
		 loadChrFile();
		ArrayList<String[]> lsChrLen = seqHash.getChrLengthInfo();
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite(outFile, true);
		txtReadandWrite.ExcelWrite(lsChrLen, "\t", 1, 1);
		return outFile;
	}
	
}
