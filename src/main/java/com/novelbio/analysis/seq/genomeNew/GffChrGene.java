package com.novelbio.analysis.seq.genomeNew;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqHash;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfo;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReads;
import com.novelbio.base.fileOperate.FileOperate;

public class GffChrGene {
	private static final Logger logger = Logger.getLogger(GffChrGene.class);
	
	
	private static GffHashGene gffHashGene = null;
			
	private static SeqHash seqHash = null;
	
	private MapReads mapReads = null;
	
	/**
	 * ÿ��������������bp�������ڵ���binNum�������binNum�ı��� ���binNum ==1 && binNum == 1�������ܾ�ȷ
	 */
	int mapNormType = MapReads.NORMALIZATION_ALL_READS;
	int upBp = 5000;//tss��tes�Լ�����λ������γ��ȣ�Ĭ��5000
	int downBp = 5000;//tss��tes�Լ�����λ������γ��ȣ�Ĭ��5000
	/**
	 * 
	 * �趨mapreads��һϵ�в���
	 * @param mapNormType �� MapReads.NORMALIZATION_ALL_READS ��ѡ��Ĭ��MapReads.NORMALIZATION_ALL_READS
	 * @param upBp tss��tes�Լ�����λ������γ��ȣ�Ĭ��5000
	 * @param downBp tss��tes�Լ�����λ������γ��ȣ�Ĭ��5000
	 */
	public void setMapNormType(int mapNormType, int upBp, int downBp) {
		this.mapNormType = mapNormType;
		this.upBp = upBp;
		this.downBp = downBp;
	}
	
	
	public GffChrGene(String gffType, String gffFile, String chrFile, String readsBed, int binNum)
	{
		this.setGffFile(gffType, gffFile);
		this.setChrFile(chrFile, null);
		this.setMapReads(readsBed, binNum);
	}
	public GffChrGene(String gffType, String gffFile, String chrFile, String regx, String readsBed, int binNum)
	{
		this.setGffFile(gffType, gffFile);
		this.setChrFile(chrFile, regx);
		this.setMapReads(readsBed, binNum);
	}
	
	public void setGffFile(String gffType, String gffFile)
	{
		if (FileOperate.isFileExist(gffFile)) {
			gffHashGene = new GffHashGene(gffType, gffFile);
		}
	}
	
	public void setChrFile(String chrFile, String regx) {
		if (FileOperate.isFileExist(chrFile) || FileOperate.isFileDirectory(chrFile)) {
			 seqHash = new SeqHash(chrFile, regx);
		}
	}
	/**
	 * @param readsFile mapping�Ľ���ļ��������Ź���һ��Ϊbed��ʽ
	 * @param binNum ÿ������λ����
	 */
	public void setMapReads(String readsFile, int binNum) {
		if (FileOperate.isFileExist(readsFile)) {
			mapReads = new MapReads(binNum, readsFile);
		}
	}
	
	
	
	/**
	 * 
	 * ����һϵ��λ��Ȼ������Ϣ
	 * @param lssummit summit��Ϣ 0:chrID 1:summit 2:weight
	 * @param range
	 * @param sortmin2max
	 * @param thisBinNum ���ָ��������Ŀ
	 * @return
	 */
	protected List<MapInfo> getLocInfo(ArrayList<String[]> lssummit, int range, boolean sortmin2max, int thisBinNum) {
		ArrayList<MapInfo> lsTmp = new ArrayList<MapInfo>();
		for (String[] strings : lssummit) {
			int locSummit = Integer.parseInt(strings[1]);
			double weight = Double.parseDouble(strings[2]);
			MapInfo mapInfo = new MapInfo(strings[0], locSummit - range, locSummit + range, locSummit, weight,strings[0]+ locSummit);
		}
		MapInfo.sortPath(sortmin2max);
		List<MapInfo> lsResult= MapInfo.sortLsMapInfo(lsTmp, range);
		mapReads.getRegionLs(thisBinNum, lsResult, 0, mapNormType);
		return lsResult;
	}
	
	/**
	 * �趨ÿ�������bp��Ȼ���ý��
	 * @param lsPeakInfo peak��Ϣ 0:chrID 1:startLoc 2:endLoc
	 * @param upBp
	 * @param downBp
	 * @param thisInvNum ÿ��������������bp�������ڵ���binNum�������binNum�ı��� ���binNum ==1 && binNum == 1�������ܾ�ȷ
	 */
	protected ArrayList<MapInfo> getPeakStructure(ArrayList<String[]> lsPeakInfo, int thisInvNum, String structure) {
		Set<GffDetailGene> setgffDetailGenes = getPeakGeneStructure(lsPeakInfo, structure);
		ArrayList<MapInfo> lsMapInfos = getMapInfoFromGffGene(setgffDetailGenes, structure);
		mapReads.getRegionLs(lsMapInfos, thisInvNum, 0, mapNormType);
		return lsMapInfos;
	}
	
	/**
	 * ָ�����ָ������Ȼ���ý��
	 * @param thisBinNum ���ָ��������Ŀ
	 * @param lsPeakInfo 0:chrID 1:startLoc 2:endLoc
	 * @param upBp
	 * @param downBp
	 */
	protected ArrayList<MapInfo> getPeakStructure(int thisBinNum,ArrayList<String[]> lsPeakInfo, String structure) {
		Set<GffDetailGene> setgffDetailGenes = getPeakGeneStructure(lsPeakInfo, structure);
		ArrayList<MapInfo> lsMapInfos = getMapInfoFromGffGene(setgffDetailGenes, structure);
		mapReads.getRegionLs(thisBinNum, lsMapInfos, 0, mapNormType);
		return lsMapInfos;
	}
	
	private ArrayList<MapInfo> getMapInfoFromGffGene(Set<GffDetailGene> setgffDetailGenes, String structure)
	{
		ArrayList<MapInfo> lsMapInfos = new ArrayList<MapInfo>();
		for (GffDetailGene gffDetailGene : setgffDetailGenes) {
			lsMapInfos.add(getStructureLoc(gffDetailGene, structure));
		}
		return lsMapInfos;
	}
	
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
	 * ����peak����Ϣ��chrID������յ㣬���ر�peak���ǵ�Tss�Ļ������͸��������������Tssͼ
	 * @param lsPeakInfo 0:chrID 1:startLoc 2:endLoc
	 */
	private HashSet<GffDetailGene> getPeakGeneStructure(ArrayList<String[]> lsPeakInfo, String structure) {
		//�洢�����������
		HashSet<GffDetailGene> hashGffDetailGenes = new HashSet<GffDetailGene>();
		for (String[] strings : lsPeakInfo) {
			int startLoc = Integer.parseInt(strings[1]);
			if (startLoc <0 && startLoc > -1000) {
				startLoc = 0;
			}
			int endLoc = Integer.parseInt(strings[2]);
			hashGffDetailGenes.addAll(getPeakStructureGene(strings[0], startLoc, endLoc, structure ) );
		}
		return hashGffDetailGenes;
	}
	
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
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
	
	
	
	
	


