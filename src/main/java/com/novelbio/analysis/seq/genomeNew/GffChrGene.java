package com.novelbio.analysis.seq.genomeNew;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqHash;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfo;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReads;
import com.novelbio.base.fileOperate.FileOperate;

public class GffChrGene {
	private static final Logger logger = Logger.getLogger(GffChrGene.class);
	
	private int taxID = 0;
	
	private static GffHashGene gffHashGene = null;
			
	private static SeqHash seqHash = null;
	
	private MapReads mapReads = null;
	
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
	 * ÿ��������������bp�������ڵ���binNum�������binNum�ı��� ���binNum ==1 && binNum == 1�������ܾ�ȷ
	 */
	int mapNormType = MapReads.NORMALIZATION_ALL_READS;
	int upBp = 5000;//tss��tes�Լ�����λ������γ��ȣ�Ĭ��5000
	int downBp = 5000;//tss��tes�Լ�����λ������γ��ȣ�Ĭ��5000
	
	int tssUpBp = 3000;
	int tssDownBp = 1500;
	int geneEnd3UTR = 100;
	
	
	
	
	/**
	 * 
	 * �趨mapreads��һϵ�в������Լ����ڻ�Tss��Tes������ͼ�������γ���
	 * @param mapNormType �� MapReads.NORMALIZATION_ALL_READS ��ѡ��Ĭ��MapReads.NORMALIZATION_ALL_READS
	 * @param upBp tss��tes�Լ�����λ������γ��ȣ�Ĭ��5000
	 * @param downBp tss��tes�Լ�����λ������γ��ȣ�Ĭ��5000
	 */
	public void setMapNormType(int mapNormType, int upBp, int downBp) {
		this.mapNormType = mapNormType;
		this.upBp = upBp;
		this.downBp = downBp;
	}
	/**
	 * �趨����Ķ�λ������Ϣ
	 * @param tssUpBp Tss���ζ���
	 * @param tssDownBp Tss���ζ���
	 * @param geneEnd3UTR geneEnd����������
	 */
	public void setGeneRange(int tssUpBp, int tssDownBp, int geneEnd3UTR) {
		this.tssUpBp = tssDownBp;
		this.tssDownBp = tssDownBp;
		this.geneEnd3UTR = geneEnd3UTR;
		GffDetailGene.setCodLocation(tssUpBp, tssDownBp, geneEnd3UTR);
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
	
	protected void setGffFile(String gffType, String gffFile)
	{
		if (FileOperate.isFileExist(gffFile)) {
			gffHashGene = new GffHashGene(gffType, gffFile);
		}
	}
	
	protected void setChrFile(String chrFile, String regx) {
		if (FileOperate.isFileExist(chrFile) || FileOperate.isFileDirectory(chrFile)) {
			 seqHash = new SeqHash(chrFile, regx);
		}
	}
	/**
	 * @param readsFile mapping�Ľ���ļ��������Ź���һ��Ϊbed��ʽ
	 * @param binNum ÿ������λ����
	 */
	protected void setMapReads(String readsFile, int binNum) {
		if (FileOperate.isFileExist(readsFile)) {
			mapReads = new MapReads(binNum, readsFile);
		}
	}
	///////////////////////////    reads on region   //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * ����һϵ��λ��Ȼ������Ϣ
	 * @param lssummit mapInfo ����� 0:chrID 1:summit 2:weight 3:Name
	 * @param range
	 * @param sortmin2max
	 * @param thisBinNum ���ָ��������Ŀ
	 * @return
	 */
	protected List<MapInfo> getLocInfo(ArrayList<MapInfo> lssummit, int range, boolean sortmin2max, int thisBinNum) {
		ArrayList<MapInfo> lsTmp = new ArrayList<MapInfo>();
		for (MapInfo mapInfo : lssummit) {
			MapInfo mapInfo2 = new MapInfo(mapInfo.getChrID(), mapInfo.getSummit() - range, mapInfo.getSummit() + range, mapInfo.getSummit(), mapInfo.getWeight(),mapInfo.getTitle());
			lsTmp.add(mapInfo2);
		}
		MapInfo.sortPath(sortmin2max);
		List<MapInfo> lsResult= MapInfo.sortLsMapInfo(lsTmp, range);
		mapReads.getRegionLs(thisBinNum, lsResult, 0, mapNormType);
		return lsResult;
	}
	
	/**
	 * �趨ÿ�������bp��Ȼ���ý��
	 * @param lsPeakInfo mapInfo������ chrID �� startLoc �� endLoc ����
	 * @param thisInvNum ÿ��������������bp�������ڵ���binNum�������binNum�ı��� ���binNum ==1 && binNum == 1�������ܾ�ȷ
	 * @param structure GffDetailGene.TSS��
	 */
	protected ArrayList<MapInfo> getPeakStructure(ArrayList<MapInfo> lsPeakInfo, int thisInvNum, String structure) {
		Set<GffDetailGene> setgffDetailGenes = getPeakGeneStructure(lsPeakInfo, structure);
		ArrayList<MapInfo> lsMapInfos = getMapInfoFromGffGene(setgffDetailGenes, structure);
		mapReads.getRegionLs(lsMapInfos, thisInvNum, 0, mapNormType);
		return lsMapInfos;
	}
	
	/**
	 * 
	 * ָ�����ָ������Ȼ���ý��
	 * @param thisBinNum ���ָ��������Ŀ
	 * @param lsPeakInfo mapInfo������ chrID �� startLoc �� endLoc ����
	 * @param structure GffDetailGene.TSS��
	 * @return
	 */
	protected ArrayList<MapInfo> getPeakStructure(int thisBinNum,ArrayList<MapInfo> lsPeakInfo, String structure) {
		Set<GffDetailGene> setgffDetailGenes = getPeakGeneStructure(lsPeakInfo, structure);
		ArrayList<MapInfo> lsMapInfos = getMapInfoFromGffGene(setgffDetailGenes, structure);
		mapReads.getRegionLs(thisBinNum, lsMapInfos, 0, mapNormType);
		return lsMapInfos;
	}
	///////////////////////////   annotation   ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * ����Ⱦɫ��λ�ú����꣬����ע����Ϣ
	 * @param chrID
	 * @param summit
	 * @return
	 */
	protected String[][] getGenInfo(String chrID, int summit) {
		String[][] anno = new String[3][4];
		for (int i = 0; i < anno.length; i++) {
			for (int j = 0; j < anno[0].length; j++) {
				anno[i][j] = "";
			}
		}
		GffCodGene gffCodGene = gffHashGene.searchLocation(chrID, summit);
		if (gffCodGene == null) {
			return anno;
		}
		//����һ��gene��
		if (gffCodGene.getGffDetailUp() != null) {
			anno[0] = gffCodGene.getGffDetailUp().getInfo();
		}
		if (gffCodGene.getGffDetailThis() != null) {
			anno[1] = gffCodGene.getGffDetailThis().getInfo();
		}
		if (gffCodGene.getGffDetailDown() != null) {
			anno[2] = gffCodGene.getGffDetailDown().getInfo();
		}
		return anno;
	}
	
	protected String[] getGenInfoFilter(String chrID, int summit,
			int[] filterTss, int[] filterGenEnd, boolean filterGeneBody, boolean filter5UTR, 
			boolean filter3UTR, boolean filterExon, boolean filterIntron) {
		String[] anno = new String[3];
		GffCodGene gffCodGene = gffHashGene.searchLocation(chrID, summit);
		if (gffCodGene.isInsideLoc()) {
			anno[1] = gffCodGene.getGffDetailThis().getLongestSplit().getCodLocStrFilter(filterTss, filterGenEnd, filterGeneBody, filter5UTR, filter3UTR, filterExon, filterIntron);
		}
		return anno;
	}

	/**
	 * ����һϵ��gffDetailGene���Լ���Ҫ�Ĳ��֣����ض�Ӧ�����LsMapInfo
	 * @param setgffDetailGenes
	 * @param structure
	 * @return
	 */
	private ArrayList<MapInfo> getMapInfoFromGffGene(Set<GffDetailGene> setgffDetailGenes, String structure)
	{
		ArrayList<MapInfo> lsMapInfos = new ArrayList<MapInfo>();
		for (GffDetailGene gffDetailGene : setgffDetailGenes) {
			lsMapInfos.add(getStructureLoc(gffDetailGene, structure));
		}
		return lsMapInfos;
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
	 * ����peak����Ϣ��chrID������յ㣬���ر�peak���ǵ�Tss�Ļ������͸��������������Tssͼ
	 * �Զ�ȥ�������
	 * @param lsPeakInfo mapInfo������ chrID �� startLoc �� endLoc ����
	 */
	private HashSet<GffDetailGene> getPeakGeneStructure(ArrayList<MapInfo> lsPeakInfo, String structure) {
		//�洢�����������
		HashSet<GffDetailGene> hashGffDetailGenes = new HashSet<GffDetailGene>();
		for (MapInfo mapInfo : lsPeakInfo) {
			if (mapInfo.getStart() <0 && mapInfo.getStart() > -1000) {
				mapInfo.setStart(0);;
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
	
////////////////////////////////////// peak statistics //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * ����peak����Ϣ��chrID������յ㣬���ر�peak���ǵ�Tss�Ļ������͸��������������Tssͼ
	 * �Զ�ȥ�������
	 * @param lsPeakInfo mapInfo������ chrID ��summit ����
	 */
	private void getPeakGenStructureStatistics(ArrayList<MapInfo> lsPeakInfo) {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		lsResult.add(new String[]{"5UTR","0"});
		lsResult.add(new String[]{"3UTR","0"});
		lsResult.add(new String[]{"Exon","0"});
		lsResult.add(new String[]{"Intron","0"});
		lsResult.add(new String[]{"Up"+(double)tssUpBp/1000 + "K","0"});
		lsResult.add(new String[]{"5UTR","0"});
		lsResult.add(new String[]{"GeneBody","0"});
		
		for (MapInfo mapInfo : lsPeakInfo) {
			GffCodGene gffCodGene = gffHashGene.searchLocation(mapInfo.getChrID(), mapInfo.getSummit());
			if (gffCodGene.isInsideLoc()) {
				String[] tmpAll = lsResult.get(5); //GeneBody
				tmpAll[1] = Integer.parseInt(tmpAll[1]) + 1 + "";
				if (gffCodGene.getGffDetailThis().getLongestSplit().getCodLoc() == GffGeneIsoInfo.COD_LOC_EXON) {
					String[] tmp = lsResult.get(2); //Exon
					tmp[1] = Integer.parseInt(tmp[1]) + 1 + "";
				}
				else if (gffCodGene.getGffDetailThis().getLongestSplit().getCodLoc() == GffGeneIsoInfo.COD_LOC_INTRON) {
					String[] tmp = lsResult.get(3); //Intron
					tmp[1] = Integer.parseInt(tmp[1]) + 1 + "";
				}
				else if (gffCodGene.getGffDetailThis().getLongestSplit().getCodLoc() == GffGeneIsoInfo.COD_LOCUTR_5UTR) {
					String[] tmp = lsResult.get(1); //5UTR
					tmp[1] = Integer.parseInt(tmp[1]) + 1 + "";
				}
				else if (gffCodGene.getGffDetailThis().getLongestSplit().getCodLoc() == GffGeneIsoInfo.COD_LOCUTR_3UTR) {
					String[] tmp = lsResult.get(1); //3UTR
					tmp[1] = Integer.parseInt(tmp[1]) + 1 + "";
				}
			}
			else {
				//����һ��������
				if(gffCodGene.getGffDetailUp().isCodInGenExtend()) {
					if (gffCodGene.getGffDetailUp().isCodInPromoter()) {
						String[] tmp = lsResult.get(4); //Promoter
						tmp[1] = Integer.parseInt(tmp[1]) + 1 + "";
					}
				}
				else if(gffCodGene.getGffDetailDown().isCodInGenExtend()) {
					if (gffCodGene.getGffDetailDown().isCodInPromoter()) {
						String[] tmp = lsResult.get(4); //Promoter
						tmp[1] = Integer.parseInt(tmp[1]) + 1 + "";
					}
				}
			}
		}
	}
	
	private void getChrStructure() {
		ArrayList<Long> lsStructureLen = gffHashGene.getGeneStructureLength(tssUpBp);
	}
	
	
	
	
////////////////////////////////////��   ȡ   ��   ��//////////////////////////////////////////////////////////////////////////
	/**
	 * ����Item�����γ��ȣ����ػ�õ�����������item���ľ��룬ָ���Ƿ�Ҫ��������������
	 * @param LOCID item��������gffHash�в�ͬ��LOCID����Ҳ������accID
	 * @param length
	 * @param considerDirection ����������
	 * @param direction �����������������ôtrue����ȫ������,false����ȫ�ַ��򡣷��򷵻ظû�������/����
	 * ���������������ôtrue���ظû�������false���ظû�����
	 * @return
	 */
	public String getUpItemSeq(String LocID,int length,boolean considerDirection,boolean direction)
	{
		GffDetailGene gffDetailGene = gffHashGene.searchLOC(LocID);
		if (gffDetailGene == null)
			return null;
		int StartNum = 0;
		if (considerDirection)// ���������򣬷��صĶ��Ǳ����������
		{
			if (gffDetailGene.isCis5to3()) {
				StartNum = gffDetailGene.getNumberstart();
				return seqHash.getSeq(direction, gffDetailGene.getChrID(), StartNum - length, StartNum);
			} else {
				StartNum = gffDetailGene.getNumberend();
				return seqHash.getSeq(!direction,gffDetailGene.getChrID(), StartNum, StartNum + length);
			}
		} else // �����������򣬷��صľ���Ĭ���������
		{
			if (gffDetailGene.isCis5to3()) {
				StartNum = gffDetailGene.getNumberstart();
				return seqHash.getSeq(direction, gffDetailGene.getChrID(), StartNum - length, StartNum);
			} else {
				StartNum = gffDetailGene.getNumberend();
				return seqHash.getSeq(direction, gffDetailGene.getChrID(), StartNum, StartNum + length);
			}
		}
	}
	
	/**
	 * ����Ⱦɫ����ţ����꣬�������߳��ȣ����ظ������������������
	 * �������ڻ����ڲ�ʱ��������Ŀ�ķ���,����ڻ���䣬�򷵻�����<br>
	 * ��ν�����ڻ����ڲ���ָ��������Ŀ����UpstreamTSSbp������GeneEnd3UTR֮�������
	 * @param ChrID ,chr����������ʽץȡ������ν��Сд�����Զ�ת��ΪСд
	 * @param codloc peak����
	 * @param lenght peak�������˳���
	 * @param condition Ϊ 0,1,2 �������<br>
	 * 0:����peak��gff��������ȡ��Ҳ���ǻ����ڰ������򣬻���������<br>
	 * 1: ͨͨ��ȡ����<br>
	 * 2: ͨͨ��ȡ����<br>
	 * @return
	 */
	public String getPeakSeq(String ChrID, int codloc ,int lenght,int condition)
	{
		if (condition==0) 
		{
			GffCodGene peakInfo = gffHashGene.searchLocation(ChrID, codloc);
			boolean flaginside=false;//�Ƿ�������3000bp���ڣ�Ĭ��������
			boolean cis5to3=true;
			if(!peakInfo.isInsideLoc()) {
				if (peakInfo.getGffDetailUp() != null && peakInfo.getGffDetailUp().isCodInGenExtend()) {
					flaginside = true;
					cis5to3 = peakInfo.getGffDetailUp().isCis5to3();
				}
				else if (peakInfo.getGffDetailDown() != null && peakInfo.getGffDetailDown().isCodInGenExtend()) {
					flaginside = true;
					cis5to3 = peakInfo.getGffDetailDown().isCis5to3();
				}
			}
			else {
				cis5to3=peakInfo.getGffDetailThis().isCis5to3();;
				flaginside=true;
			}
			if(flaginside) {
				return seqHash.getSeq(ChrID, codloc, lenght, cis5to3);
			}
			return seqHash.getSeq(ChrID, codloc, lenght,true);
		}
		else if (condition==1) {
			return seqHash.getSeq(ChrID, codloc, lenght, true);
		}
		else if (condition==2) {
			return seqHash.getSeq(ChrID, codloc, lenght, false);
		}
		else {
			logger.error("get sequence error");
			return null;
		}
	}
	
/////////////////////////////////////////  �� �� bed �� �� �� �� �� mapping �� �� //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * ָ���Ⱦɫ���ֵ�����ذ�����ÿ��Ⱦɫ����Ӧֵ��Ⱦɫ�����������,resolution��int[resolution]�������ڻ�ͼ
	 * ��ôresolution���Ƿ��ص�int[]�ĳ���
	 * 
	 * @param chrID
	 * @param maxresolution
	 */
	public int[] getChrRes(String chrID, int maxresolution) throws Exception {
		return seqHash.getChrRes(chrID, maxresolution);
	}
	
	/**
	 * ����chrID�;����������䣬�Լ��ֱ��ʣ�����double[]����:��Ⱦɫ����tag���ܶȷֲ����������Ǹ�������reads�ķֲ����
	 * @param chrID
	 * @param startLoc
	 * @param endLoc
	 * @param binNum ���ָ�Ŀ���
	 * @return
	 */
	public double[] getChrReadsDist(String chrID,int startLoc,int endLoc,int binNum) 
	{
		return mapReads.getReadsDensity(chrID, startLoc, endLoc, binNum);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
	
	
	
	
	


