package com.novelbio.analysis.seq.genomeNew;

import java.util.ArrayList;
import java.util.HashSet;
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
	/**
	 * �趨һϵ�е�����λ��
	 */
	ArrayList<MapInfo> lsMapInfos = null;
	/**
	 * �趨������Ҫ���з�����MapInfo list
	 * @param lsMapInfos
	 */
	public void setLsMapInfos(ArrayList<MapInfo> lsMapInfos) {
		this.lsMapInfos = lsMapInfos;
	}
	/**
	 * ��ñ�����Ҫ������MapInfo list
	 * @return
	 */
	public ArrayList<MapInfo> getLsMapInfos() {
		return lsMapInfos;
	}
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
	int upBp = 5000;//tss��tes�Լ�����λ������γ��ȣ�Ĭ��5000
	int downBp = 5000;//tss��tes�Լ�����λ������γ��ȣ�Ĭ��5000
	
	int tssUpBp = 3000;
	int tssDownBp = 2000;
	int geneEnd3UTR = 100;
	
	
	static String chrFile = "";
	static String chrRegx = "";
	
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
	 * @param upBp tss��tes�Լ�����λ������γ��ȣ�Ĭ��5000
	 * @param downBp tss��tes�Լ�����λ������γ��ȣ�Ĭ��5000
	 */
	public void region(int upBp, int downBp) {
		this.upBp = upBp;
		this.downBp = downBp;
	}
	/**
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
	
	protected void loadChrFile() {
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
	
	protected void loadMapReads() {
		if (FileOperate.isFileExist(chrFile) || FileOperate.isFileDirectory(chrFile)) {
			 seqHash = new SeqHash(chrFile, chrRegx);
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
	public void getFileRegion(String txtExcel, int colChrID, int colStartLoc, int colEndLoc, int colScore,int rowStart)
	{
		int[] columnID = null;
		if (colScore <= 0 ) {
			 columnID = new int[]{colChrID,colStartLoc,colEndLoc};
		}
		else {
			columnID = new int[]{colChrID,colStartLoc,colEndLoc, colScore};
		}
		ArrayList<String[]> lstmp = ExcelTxtRead.readLsExcelTxt(txtExcel, columnID, rowStart, 0);
		lsMapInfos = new ArrayList<MapInfo>();
		for (String[] strings : lstmp) {
			MapInfo mapInfo = new MapInfo(strings[0]);
			mapInfo.setStartLoc(Integer.parseInt(strings[1]));
			mapInfo.setEndLoc(Integer.parseInt(strings[2]));
			if (mapInfo.getStart() <0 && mapInfo.getStart() > -1000) {
				mapInfo.setStartLoc(0);;
			}
			if (colScore > 0) {
				mapInfo.setWeight(Double.parseDouble(strings[3]));
			}
			lsMapInfos.add(mapInfo);
		}
		MapInfo.sortLsMapInfo(lsMapInfos, distanceMapInfo);
	}
	/**
	 * ���summit���˸�region�������ܹ�����region*2+1������
	 * �������λ���յ�ļ����distanceMapInfo���ڣ��ͻ�ɾ���Ǹ�Ȩ�ص͵�
	 * @param txtExcel
	 * @param region
	 * @param colChrID
	 * @param colSummit
	 * @param rowStart
	 */
	public void getFileSite(String txtExcel,int region ,int colChrID, int colSummit, int colScore, int rowStart)
	{
		int[] columnID = null;
		if (colScore <= 0 ) {
			 columnID = new int[]{colChrID, colSummit, colScore};
		}
		else {
			columnID = new int[]{colChrID, colSummit, colScore};
		}
		ArrayList<String[]> lstmp = ExcelTxtRead.readLsExcelTxt(txtExcel, columnID, rowStart, 0);
		lsMapInfos = new ArrayList<MapInfo>();
		for (String[] strings : lstmp) {
			MapInfo mapInfo = new MapInfo(strings[0]);
			int start = Integer.parseInt(strings[1]);
			mapInfo.setFlagLoc(Integer.parseInt(strings[1]));
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
	}
	
	/**
	 * 
	 * @param structure GffDetailGene.TSS��
	 * @return
	 */
	public ArrayList<MapInfo> getPeakCoveredGeneMapInfo(String structure)
	{
		Set<GffDetailGene> setGffDetailGenes = getPeakGeneStructure(structure);
		 ArrayList<MapInfo> lsResult = getMapInfoFromGffGene(setGffDetailGenes, structure);
		 return lsResult;
	}
	
	
	/**
	 * ����ǰ���趨upBp��downBp
	 * ����һϵ��gffDetailGene���Լ���Ҫ�Ĳ��֣����ض�Ӧ�����LsMapInfo
	 * @param setgffDetailGenes
	 * @param structure
	 * @return
	 */
	protected ArrayList<MapInfo> getMapInfoFromGffGene(Set<GffDetailGene> setgffDetailGenes, String structure)
	{
		lsMapInfos = new ArrayList<MapInfo>();
		for (GffDetailGene gffDetailGene : setgffDetailGenes) {
			lsMapInfos.add(getStructureLoc(gffDetailGene, structure));
		}
		return lsMapInfos;
	}

	
	/**
	 * ����peak����Ϣ��chrID������յ㣬���ر�peak���ǵ�Tss�Ļ������͸��������������Tssͼ
	 * �Զ�ȥ�������
	 * @param lsPeakInfo mapInfo������ chrID �� startLoc �� endLoc ����
	 */
	protected HashSet<GffDetailGene> getPeakGeneStructure(String structure) {
		//�洢�����������
		HashSet<GffDetailGene> hashGffDetailGenes = new HashSet<GffDetailGene>();
		for (MapInfo mapInfo : lsMapInfos) {
			if (mapInfo.getStart() <0 && mapInfo.getStart() > -1000) {
				mapInfo.setStartLoc(0);;
			}
			hashGffDetailGenes.addAll(getPeakStructureGene(mapInfo.getChrID(), mapInfo.getStart(), mapInfo.getEnd(), structure ) );
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
	 * @param gffDetailGene
	 * @param structure GffDetailGene.TSS��
	 * @return
	 */
	private MapInfo getStructureLoc(GffDetailGene gffDetailGene, String structure)
	{
		if (structure.equals(GffDetailGene.TSS)) {
			int tss = gffDetailGene.getLongestSplit().getTSSsite();
			return new MapInfo(gffDetailGene.getChrID(), tss - upBp, tss + downBp, tss,0, gffDetailGene.getLongestSplit().getIsoName());
		}
		else if (structure.equals(GffDetailGene.TES)) {
			int tes = gffDetailGene.getLongestSplit().getTSSsite();
			return new MapInfo(gffDetailGene.getChrID(), tes - upBp, tes + downBp, tes, 0, gffDetailGene.getLongestSplit().getIsoName());
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
