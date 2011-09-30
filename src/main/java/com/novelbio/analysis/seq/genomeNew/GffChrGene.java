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
	 * 每个区域内所含的bp数，大于等于binNum，最好是binNum的倍数 如果binNum ==1 && binNum == 1，结果会很精确
	 */
	int mapNormType = MapReads.NORMALIZATION_ALL_READS;
	int upBp = 5000;//tss和tes以及其他位点的上游长度，默认5000
	int downBp = 5000;//tss和tes以及其他位点的下游长度，默认5000
	
	int tssUpBp = 3000;
	int tssDownBp = 1500;
	int geneEnd3UTR = 100;
	
	
	
	
	/**
	 * 
	 * 设定mapreads的一系列参数，以及后期画Tss和Tes等类似图的上下游长度
	 * @param mapNormType 在 MapReads.NORMALIZATION_ALL_READS 中选择，默认MapReads.NORMALIZATION_ALL_READS
	 * @param upBp tss和tes以及其他位点的上游长度，默认5000
	 * @param downBp tss和tes以及其他位点的下游长度，默认5000
	 */
	public void setMapNormType(int mapNormType, int upBp, int downBp) {
		this.mapNormType = mapNormType;
		this.upBp = upBp;
		this.downBp = downBp;
	}
	/**
	 * 设定基因的定位区域信息
	 * @param tssUpBp Tss上游多少
	 * @param tssDownBp Tss下游多少
	 * @param geneEnd3UTR geneEnd向后延伸多少
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
	 * @param readsFile mapping的结果文件，必须排过序，一般为bed格式
	 * @param binNum 每隔多少位计数
	 */
	protected void setMapReads(String readsFile, int binNum) {
		if (FileOperate.isFileExist(readsFile)) {
			mapReads = new MapReads(binNum, readsFile);
		}
	}
	///////////////////////////    reads on region   //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 给定一系列位点然后获得信息
	 * @param lssummit mapInfo 有四项： 0:chrID 1:summit 2:weight 3:Name
	 * @param range
	 * @param sortmin2max
	 * @param thisBinNum 待分割的区域数目
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
	 * 设定每个区域的bp，然后获得结果
	 * @param lsPeakInfo mapInfo必须有 chrID 和 startLoc 和 endLoc 三项
	 * @param thisInvNum 每个区域内所含的bp数，大于等于binNum，最好是binNum的倍数 如果binNum ==1 && binNum == 1，结果会很精确
	 * @param structure GffDetailGene.TSS等
	 */
	protected ArrayList<MapInfo> getPeakStructure(ArrayList<MapInfo> lsPeakInfo, int thisInvNum, String structure) {
		Set<GffDetailGene> setgffDetailGenes = getPeakGeneStructure(lsPeakInfo, structure);
		ArrayList<MapInfo> lsMapInfos = getMapInfoFromGffGene(setgffDetailGenes, structure);
		mapReads.getRegionLs(lsMapInfos, thisInvNum, 0, mapNormType);
		return lsMapInfos;
	}
	
	/**
	 * 
	 * 指定待分割的区域，然后获得结果
	 * @param thisBinNum 待分割的区域数目
	 * @param lsPeakInfo mapInfo必须有 chrID 和 startLoc 和 endLoc 三项
	 * @param structure GffDetailGene.TSS等
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
	 * 给定染色体位置和坐标，返回注释信息
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
		//在上一个gene内
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
	 * 给定一系列gffDetailGene，以及想要的部分，返回对应区域的LsMapInfo
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
	 * 前面设定upBp和downBp
	 * 给定gffDetailGene，以及想要的部分，返回对应区域的MapInfo
	 * @param gffDetailGene
	 * @param structure GffDetailGene.TSS等
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
			logger.error("还没添加该种类型的structure");
			return null;
		}
	}
	
	/**
	 * 给定peak的信息，chrID和起点终点，返回被peak覆盖到Tss的基因名和覆盖情况，用于做Tss图
	 * 自动去冗余基因
	 * @param lsPeakInfo mapInfo必须有 chrID 和 startLoc 和 endLoc 三项
	 */
	private HashSet<GffDetailGene> getPeakGeneStructure(ArrayList<MapInfo> lsPeakInfo, String structure) {
		//存储最后基因的数量
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
	 * 给定坐标区域，返回该peak所覆盖的GffDetailGene
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
			logger.error("暂时没有除Tss和Tes之外的基因结构");
			return null;
		}
	}
	
////////////////////////////////////// peak statistics //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * 给定peak的信息，chrID和起点终点，返回被peak覆盖到Tss的基因名和覆盖情况，用于做Tss图
	 * 自动去冗余基因
	 * @param lsPeakInfo mapInfo必须有 chrID 和summit 两项
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
				//在上一个基因内
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
	
	
	
	
////////////////////////////////////提   取   序   列//////////////////////////////////////////////////////////////////////////
	/**
	 * 输入Item和上游长度，返回获得的上游序列与item起点的距离，指定是否要考虑序列正反向
	 * @param LOCID item名，各个gffHash有不同的LOCID名，也可以是accID
	 * @param length
	 * @param considerDirection 考虑正反向
	 * @param direction 如果不考虑正反向，那么true返回全局正向,false返回全局反向。否则返回该基因正向/反向。
	 * 如果考虑正反向，那么true返回该基因正向，false返回该基因反向
	 * @return
	 */
	public String getUpItemSeq(String LocID,int length,boolean considerDirection,boolean direction)
	{
		GffDetailGene gffDetailGene = gffHashGene.searchLOC(LocID);
		if (gffDetailGene == null)
			return null;
		int StartNum = 0;
		if (considerDirection)// 考虑正反向，返回的都是本基因的正向
		{
			if (gffDetailGene.isCis5to3()) {
				StartNum = gffDetailGene.getNumberstart();
				return seqHash.getSeq(direction, gffDetailGene.getChrID(), StartNum - length, StartNum);
			} else {
				StartNum = gffDetailGene.getNumberend();
				return seqHash.getSeq(!direction,gffDetailGene.getChrID(), StartNum, StartNum + length);
			}
		} else // 不考虑正反向，返回的就是默认正向或反向
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
	 * 输入染色体序号，坐标，坐标两边长度，返回该坐标的左右两边序列
	 * 当坐标在基因内部时，考虑条目的方向,如果在基因间，则返回正链<br>
	 * 所谓坐标在基因内部，指坐标在条目上游UpstreamTSSbp到下游GeneEnd3UTR之间的区域
	 * @param ChrID ,chr采用正则表达式抓取，无所谓大小写，会自动转变为小写
	 * @param codloc peak坐标
	 * @param lenght peak左右两端长度
	 * @param condition 为 0,1,2 三种情况<br>
	 * 0:按照peak在gff里的情况提取，也就是基因内按基因方向，基因外正向<br>
	 * 1: 通通提取正向<br>
	 * 2: 通通提取反向<br>
	 * @return
	 */
	public String getPeakSeq(String ChrID, int codloc ,int lenght,int condition)
	{
		if (condition==0) 
		{
			GffCodGene peakInfo = gffHashGene.searchLocation(ChrID, codloc);
			boolean flaginside=false;//是否在上游3000bp以内，默认在以外
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
	
/////////////////////////////////////////  获 得 bed 文 件 得 到 的 mapping 结 果 //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 指定最长染色体的值，返回按比例每条染色体相应值下染色体的坐标数组,resolution和int[resolution]，可用于画图
	 * 那么resolution就是返回的int[]的长度
	 * 
	 * @param chrID
	 * @param maxresolution
	 */
	public int[] getChrRes(String chrID, int maxresolution) throws Exception {
		return seqHash.getChrRes(chrID, maxresolution);
	}
	
	/**
	 * 给定chrID和具体坐标区间，以及分辨率，返回double[]数组:该染色体上tag的密度分布，数组中是该区间内reads的分布情况
	 * @param chrID
	 * @param startLoc
	 * @param endLoc
	 * @param binNum 待分割的块数
	 * @return
	 */
	public double[] getChrReadsDist(String chrID,int startLoc,int endLoc,int binNum) 
	{
		return mapReads.getReadsDensity(chrID, startLoc, endLoc, binNum);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
	
	
	
	
	


