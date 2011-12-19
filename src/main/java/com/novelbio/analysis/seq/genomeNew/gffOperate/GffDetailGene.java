package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.chipseq.regDensity.RegDensity;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGene;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.modcopeid.CopedID;
/**
 * 重写了clone但是没有重写equals和hash
 * hash同GffDetailAbs，仅比较ChrID + "//" + locString + "//" + numberstart + "//" + numberstart;
 * 专门存储UCSC的gene坐标文件
 * group:Genes and Gene Prediction Tracks
 * track:UCSC Genes
 * table:knownGene
 * output format:all fields from selected table
 * GffDetailList类中保存每个基因的起点终点和CDS的起点终点 
 * @author zong0jie
 * @GffHashGene读取Gff文件，每个基因可以获得以下信息
 * 基因名<br>
 * 本基因起点，这是UCSC konwn gene某位点所有基因的最靠前的exon的起点<br>
 * 本基因终点，这是UCSC konwn gene某位点所有基因的最靠后的intron的终点<br>
 * 本基因所在染色体编号<br>
 * 本基因的不同转录本<br>
 * 本基因转录方向<br>
 * 本类中的几个方法都和Gff基因有关<br>
 */
public class GffDetailGene extends GffDetailAbs
{
	private final static Logger logger = Logger.getLogger(GffDetailGene.class);
	/**
	 * 两个转录本的交集必须大于0.6才算是一个基因
	 */
	public final static double OVERLAP_RATIO = 0.6;
	public final static String INTRON = "intron";
	public final static String EXON_WITHOUT_UTR = "exon_without_utr";
	public final static String EXON = "exon";
	public final static String UTR5 = "5utr";
	public final static String UTR3 = "3utr";
	public final static String TSS = "tss";
	public final static String TES = "tes";
	/**
	 * 两个转录本在一个基因下，那么这个基因名可能是结合了两个名字，用该符号分割
	 */
	public final static String SEP_GENE_NAME = "/";
	
	int taxID = 0;
	/**
	 * 设定基因的转录起点终点位置信息
	 * @param upStreamTSSbp 设定基因的转录起点上游长度，默认为3000bp
	 * @param downStreamTssbp 设定基因的转录起点下游长度，默认为2000bp
	 * @param geneEnd3UTR 设定基因结尾向外延伸的长度，默认为100bp
	 */
	public static void setCodLocation(int upStreamTSSbp, int downStreamTssbp, int geneEnd3UTR) {
		UpStreamTSSbp = upStreamTSSbp;
		DownStreamTssbp = downStreamTssbp;
		GeneEnd3UTR = geneEnd3UTR;
		GffGeneIsoInfo.setCodLocation(upStreamTSSbp, downStreamTssbp, geneEnd3UTR);
	}
	
	protected void setTaxID(int taxID) {
		this.taxID = taxID;
	}
	public int getTaxID() {
		return taxID;
	}
	/**
	 * 删除转录本,从0开始计算
	 */
	public void removeIso(int id) {
		lsGffGeneIsoInfos.remove(id);
	}
	/**
	 * 给定转录本的名字，删除转录本
	 */
	public void removeIso(String isoName) {
		int id = getIsoID(isoName);
		removeIso(id);
	}
	
	/**
	 * 从0开始计数
	 * 返回-1表示没有该转录本 
	 * @param isoName
	 * @return
	 */
	private int getIsoID(String isoName)
	{
		for (int i = 0; i < lsGffGeneIsoInfos.size(); i++) {
			GffGeneIsoInfo gffGeneIsoInfo = lsGffGeneIsoInfos.get(i);
			if (gffGeneIsoInfo.getIsoName().equalsIgnoreCase(isoName)) {
				return i;
			}
		}
		return -1;
	}
	
	
	/**
	 * 顺序存储每个转录本的的坐标情况
	 */
	private ArrayList<GffGeneIsoInfo> lsGffGeneIsoInfos = new ArrayList<GffGeneIsoInfo>();//存储可变剪接的mRNA
	
	public void setCoord(int coord) {
		this.coord = coord;
		ArrayList<GffGeneIsoInfo> lsGffInfo = getLsCodSplit();
		if (lsGffInfo == null || lsGffInfo.size() < 1) {
			return;
		}
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGffInfo) {
			gffGeneIsoInfo.setCoord(coord);
		}
	}
	
	/**
	 * @param chrID
	 * @param locString
	 * @param cis5to3
	 */
	protected GffDetailGene(String chrID, String locString, boolean cis5to3) {
		super(chrID, locString, cis5to3);
	}
	/**
	 * 给最后一个转录本添加exon坐标，<br>
	 * 只需要注意按照次序装，也就是说如果正向要从小到大的加，反向从大到小的加
	 * 然而具体加入这一对坐标的时候，并不需要分别大小，程序会根据gene方向自动判定
	 */
	protected void addExonUCSC(int locStart,int locEnd)
	{
		GffGeneIsoInfo gffGeneIsoInfo = lsGffGeneIsoInfos.get(lsGffGeneIsoInfos.size()-1);//include one special loc start number to end number
		gffGeneIsoInfo.addExonUCSC(locStart, locEnd);
	}
	/**
	 * 针对水稻拟南芥的GFF文件
	 * 给最后一个转录本添加exon坐标，<br>
	 * 只需要注意按照次序装，也就是说如果正向要从小到大的加，反向从大到小的加
	 * 然而具体加入这一对坐标的时候，并不需要分别大小，程序会根据gene方向自动判定
	 */
	protected void addExonGFF(int locStart,int locEnd)
	{
		GffGeneIsoInfo gffGeneIsoInfo = lsGffGeneIsoInfos.get(lsGffGeneIsoInfos.size()-1);//include one special loc start number to end number
		gffGeneIsoInfo.addExonGFF(locStart, locEnd);
	}
	/**
	 * 针对水稻拟南芥的GFF文件
	 * 给转录本添加exon坐标，GFF3的exon的格式是
	 * 当gene为反方向时，exon是从大到小排列的
	 * 在添加exon的时候，如果本CDS与UTR之间是连着的，那么就将本CDS和UTR连在一起，放在一个exon中 如果不连，就按原来的来
	 */
	protected void addExonGFFCDSUTR(int locStart,int locEnd)
	{
		GffGeneIsoInfo gffGeneIsoInfo = lsGffGeneIsoInfos.get(lsGffGeneIsoInfos.size()-1);//include one special loc start number to end number
		gffGeneIsoInfo.addExonGFFCDSUTR(locStart, locEnd);
	}
	
	/**
	 * 给最后一个转录本添加ATG和UAG坐标，<br>
	 * 加入这一对坐标的时候，并不需要分别大小，程序会根据gene方向自动判定
	 */
	protected void addATGUAG(int atg, int uag)
	{

		GffGeneIsoInfo gffGeneIsoInfo = lsGffGeneIsoInfos.get(lsGffGeneIsoInfos.size()-1);//include one special loc start number to end number
		if (Math.abs(atg - uag)<=1) {
			gffGeneIsoInfo.mRNA = false;
			atg = Math.min(atg, uag);
			uag = Math.min(atg, uag);
		}
		if (cis5to3) {
			gffGeneIsoInfo.ATGsite = Math.min(atg, uag);
			gffGeneIsoInfo.UAGsite = Math.max(atg, uag);
		}
		else {
			gffGeneIsoInfo.ATGsite = Math.max(atg, uag);
			gffGeneIsoInfo.UAGsite = Math.min(atg, uag);
		}
	}
	/**
	 * 直接添加转录本，之后用addcds()方法给该转录本添加exon
	 */
	protected void addsplitlist(String splitName, String geneTpye) {
		GffGeneIsoInfo gffGeneIsoInfo = null;
		if (cis5to3) {
			gffGeneIsoInfo = new GffGeneIsoCis(splitName,this, geneTpye);
		}
		else {
			gffGeneIsoInfo = new GffGeneIsoTrans(splitName,this, geneTpye);
		}
		gffGeneIsoInfo.setTaxID(this.taxID);
		lsGffGeneIsoInfos.add(gffGeneIsoInfo);
	}
	/**
	 * 直接添加转录本，之后用addcds()方法给该转录本添加exon
	 */
	protected void addsplitlist(String splitName, String geneTpye, boolean cis5to3) {
		GffGeneIsoInfo gffGeneIsoInfo = null;
		if (cis5to3) {
			gffGeneIsoInfo = new GffGeneIsoCis(splitName,this, geneTpye);
		}
		else {
			gffGeneIsoInfo = new GffGeneIsoTrans(splitName,this, geneTpye);
		}
		gffGeneIsoInfo.setTaxID(this.taxID);
		lsGffGeneIsoInfos.add(gffGeneIsoInfo);
	}
	/**
	 * @return 返回转录本的数目
	 */
	public int getSplitlistNumber() {
		return lsGffGeneIsoInfos.size();
    }
	
	/**
	 * 转录方向，假设同一基因不管多少转录本都同一转录方向
	 * 如果为null，说明没有方向，一个转录本里面既有正向也有反向，总体就没有方向
	 */
	public Boolean isCis5to3() {
		if (cis5to3 == null) {
			return getLongestSplit().isCis5to3();
		}
		return this.cis5to3;
	}
	
    /**
     * 给定编号(从0开始，编号不是转录本的具体ID)<br>
     * 返回某个转录本的具体信息
     */
    public GffGeneIsoInfo getIsolist(int splitnum)
    {  
    	return lsGffGeneIsoInfos.get(splitnum);//include one special loc start number to end number	
    }
    /**
     * 给定转录本名(UCSC里实际上是基因名)<br>
     * 没有则返回null
     */
    public GffGeneIsoInfo getIsolist(String splitID)
    {
    	int index = getIsoID(splitID);
    	if (index == -1) {
    		logger.info("cannotFind the ID: "+ splitID);
			return null;
		}
    	return lsGffGeneIsoInfos.get(index);//include one special loc start number to end number	
    }

    private int getLongestSplitID() {
    	if (lsGffGeneIsoInfos.size() == 1) {
			return 0;
		}
		ArrayList<Integer> lslength = new ArrayList<Integer>();
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
			ArrayList<int[]> lsExon = gffGeneIsoInfo.getIsoInfo();
			if (lsExon.size() == 0)
				lslength.add(0);
			else
				lslength.add(Math.abs(lsExon.get(0)[0] - lsExon.get(lsExon.size()-1)[1]));
		}
		int max = lslength.get(0); int id = 0;
		for (int i = 0; i < lslength.size(); i++) {
			if (lslength.get(i) > max)
			{
				max = lslength.get(i);
				id = i;
			}
		}
		return id;
	}
    
	/**
	 * 获得该基因中最长的一条转录本的信息
	 * 
	 * @return <br>
	 */
	public GffGeneIsoInfo getLongestSplit() {
		int id = getLongestSplitID();
		return lsGffGeneIsoInfos.get(id);
	}
	/**
	 * 返回所有的转录本信息
	 * @param coord
	 */
	public ArrayList<GffGeneIsoInfo> getLsCodSplit() {
		return lsGffGeneIsoInfos;
	}
    /**
     * 获得该基因中最长的一条转录本的部分区域的信息。已经考虑过开闭区间问题
     * @param type 指定为INTRON,UTR5等，是该类的常量里面的数值
     * @param num 如果type为"Intron"或"Exon"，指定第几个，如果超出，则返回0
     * num 为实际个数。
     * 如果5UTR直接返回全长5UTR
     * 3UTR也直接返回全长3UTR
     * @return 
     */
	public int getTypeLength(String type,int num)  
	{
		GffGeneIsoInfo gffGeneIsoInfo = getLongestSplit();
		ArrayList<int[]> lsExon = gffGeneIsoInfo.getIsoInfo();
		int exonNum = lsExon.size();
		//TODO 如果超出需要返回0
		if (type.equals(INTRON)) {
			return Math.abs(lsExon.get(num)[0] - lsExon.get(num-1)[1]) - 1;
		}
		if (type.equals(EXON)) {
			return Math.abs(lsExon.get(num)[1] - lsExon.get(num)[0]) + 1;
		}
		if (type.equals(UTR5)) 
		{
			int FUTR=0;
			if (gffGeneIsoInfo.isCis5to3()) { //0    1     2     3     4     5   每个外显子中 1 > 0      0    atg   1
				for (int i = 0; i <exonNum; i++) 
				{
					if(lsExon.get(i)[1] < gffGeneIsoInfo.getATGSsite())    // 0       1   atg    
						FUTR = FUTR + lsExon.get(i)[1] - lsExon.get(i)[0] + 1;
					else if (lsExon.get(i)[0] < gffGeneIsoInfo.getATGSsite() && lsExon.get(i)[1] >= gffGeneIsoInfo.getATGSsite())  //     0    atg    1 
						FUTR = FUTR + gffGeneIsoInfo.getATGSsite() - lsExon.get(i)[0];
					else if (lsExon.get(i)[0] >= gffGeneIsoInfo.getATGSsite())  //     atg   0       1   
						break;
				}
			}
			else { //5  4   3   2   1   0    每个外显子中 0 > 1     1    gta   0
				for (int i = 0; i < exonNum; i++) 
				{
					if(lsExon.get(i)[1] > gffGeneIsoInfo.getATGSsite())  // gta   1      0
						FUTR = FUTR + lsExon.get(i)[0] - lsExon.get(i)[1] + 1;
					else if (lsExon.get(i)[0] > gffGeneIsoInfo.getATGSsite()  && lsExon.get(i)[1] <= gffGeneIsoInfo.getATGSsite() ) //   1     gta      0
						FUTR = FUTR + lsExon.get(i)[0] - gffGeneIsoInfo.getATGSsite();
					else if (lsExon.get(i)[0] <= gffGeneIsoInfo.getATGSsite())   //   1        0      gta 
						break;
				}
			}
			return FUTR;
		}
		if (type.equals(UTR3)) 
		{
			int TUTR=0;
			if (gffGeneIsoInfo.isCis5to3()) { //0    1     2     3     4     5   每个外显子中 0 < 1      0    uag   1
				for (int i = exonNum - 1; i >=0 ; i--) 
				{
					if(lsExon.get(i)[0] > gffGeneIsoInfo.getUAGsite())  //      uag     0      1
						TUTR = TUTR + lsExon.get(i)[1] - lsExon.get(i)[0] + 1;
					else if (lsExon.get(i)[1] > gffGeneIsoInfo.getUAGsite() && lsExon.get(i)[0] <= gffGeneIsoInfo.getUAGsite())  //     0     uag    1
						TUTR = TUTR + lsExon.get(i)[1] - gffGeneIsoInfo.getUAGsite();
					else if (lsExon.get(i)[1] <= gffGeneIsoInfo.getUAGsite())   //   0      1     uag   
						break;
				}
			}
			else { //5  4   3   2   1   0    每个外显子中 0 > 1      1    gau  0
				for (int i = exonNum-1; i >=0 ; i--) 
				{
					if(lsExon.get(i)[0] < gffGeneIsoInfo.getUAGsite())  //     1      0     gau
						TUTR = TUTR + lsExon.get(i)[0] - lsExon.get(i)[1] + 1;
					else if (lsExon.get(i)[0] >= gffGeneIsoInfo.getUAGsite() && lsExon.get(i)[1] < gffGeneIsoInfo.getUAGsite())  //     1    gau    0     
						TUTR = TUTR + gffGeneIsoInfo.getUAGsite() - lsExon.get(i)[1];
					else if (lsExon.get(i)[1] >= gffGeneIsoInfo.getUAGsite())   //   gau   1      0     
						break;
				}
			}
			return TUTR;
		}
		return -1000000;
	}
	
	/**
	 * 是否在该基因内，具体情况
	 * @return
	 * 返回anno[4]
	 * 0：accID
	 * 1：symbol
	 * 2：description
	 * 3：location
	 * 没有就返回“”
	 */
	public String[] getInfo() {
		String[] anno = new String[4];
		for (int i = 0; i < anno.length; i++) {
			anno[i] = "";
		}
		HashSet<CopedID> hashCopedID = new HashSet<CopedID>();
		if (isCodInGenExtend()) {
			for (GffGeneIsoInfo gffGeneIsoInfo : getLsCodSplit()) {
				if (gffGeneIsoInfo.isCodInIsoExtend()) {
					hashCopedID.add(gffGeneIsoInfo.getCopedID());
				}
			}
			for (CopedID copedID : hashCopedID) {
				if (anno.equals("")) {
					anno[0] = copedID.getAccID();
					anno[1] = copedID.getSymbo();
					anno[2] = copedID.getDescription();
				}
				else {
					anno[0] = anno[0]+"//"+copedID.getAccID();
					anno[1] = anno[1]+"//"+copedID.getSymbo();
					anno[2] = anno[2]+"//"+copedID.getDescription();
				}
			}
			if (getLongestSplit().isCodInIsoExtend()) {
				anno[4] = getLongestSplit().getCodLocStr();
			}
			else {
				for (GffGeneIsoInfo gffGeneIsoInfo : getLsCodSplit()) {
					if (gffGeneIsoInfo.isCodInIsoExtend()) {
						anno[4] = gffGeneIsoInfo.getCodLocStr();
						break;
					}
				}
			}
		}
		return anno;
	}

	@Override
	public GffDetailGene clone() {
		GffDetailGene gffDetailGene = new GffDetailGene(getChrID(), getLocString(), cis5to3);
		this.clone(gffDetailGene);
		gffDetailGene.taxID = taxID;
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
			gffDetailGene.lsGffGeneIsoInfos.add(gffGeneIsoInfo.clone());
		}
		return gffDetailGene;
	}
	
	/**
	 * 用于冯英的项目，将两个gffdetailGene的转录本头尾连接起来，并且取代原来的转录本信息
	 * 如果两个iso有交集但是交集小于0.3，则合并，否则增加一个新的iso
	 * @param gffDetailGene
	 */
	public void addIso(GffDetailGene gffDetailGene)
	{
		ArrayList<GffGeneIsoInfo> lsGeneIsoInfosFinal = new ArrayList<GffGeneIsoInfo>();
		ArrayList<GffGeneIsoInfo> lsIsoAdd = gffDetailGene.getLsCodSplit();
		ArrayList<GffGeneIsoInfo> lsIsoThis = getLsCodSplit();
		
		
		for (int i = 0; i < lsIsoThis.size(); i++) {
			if (i >= lsIsoAdd.size()) {
				break;
			}
			GffGeneIsoInfo gffGeneIsoInfoTmp = lsIsoThis.get(i);
			GffGeneIsoInfo gffGeneIsoInfoAddTmp = lsIsoAdd.get(i);
			

			GffGeneIsoInfo gffGeneIsoInfo = gffGeneIsoInfoTmp.cloneDeep();
			GffGeneIsoInfo gffGeneIsoInfoAdd = gffGeneIsoInfoAddTmp.cloneDeep();
			//
			if (gffGeneIsoInfo.isCis5to3() != gffGeneIsoInfoAdd.isCis5to3()) {
				lsGeneIsoInfosFinal.add(gffGeneIsoInfo);
				lsGeneIsoInfosFinal.add(gffGeneIsoInfoAdd);
				logger.error("两个方向不一致的gff不能合并："+ gffGeneIsoInfo.getIsoName() + " " + gffGeneIsoInfoAdd.getIsoName());
				continue;
			}
			//似乎下面已经把这个问题解决了//////////////////////////////////////////////////////////////////////
			//如果第一个转录本的尾部和第二个转录本的头部有交集
			//////////////////////////////////////////////////////////////////////
			if (gffGeneIsoInfo.getEndAbs() > gffGeneIsoInfoAdd.getStartAbs()) {
				double[] region1 = new double[]{gffGeneIsoInfo.getStartAbs(), gffGeneIsoInfo.getEndAbs()};
				double[] region2 = new double[]{gffGeneIsoInfoAdd.getStartAbs(), gffGeneIsoInfoAdd.getEndAbs()};
				double[] overlapInfo = ArrayOperate.cmpArray(region1, region2);
				//如果重叠区域太长，那么就分开加入成两个转录本
				if (overlapInfo[2] > OVERLAP_RATIO || overlapInfo[3] > OVERLAP_RATIO) {
					lsGeneIsoInfosFinal.add(gffGeneIsoInfo);
					lsGeneIsoInfosFinal.add(gffGeneIsoInfoAdd);
					continue;
				}
				//如果重叠区域短长，那么就将短的掐头去尾
				else {
					if (gffGeneIsoInfo.getIsoLen() >= gffGeneIsoInfoAdd.getIsoLen()) {
						while (gffGeneIsoInfoAdd.getIsoInfo().size() > 0 && gffGeneIsoInfo.getEndAbs() > gffGeneIsoInfoAdd.getStartAbs()) {
							gffGeneIsoInfoAdd.getIsoInfo().remove(0);
						}
					}
					else {
						while (gffGeneIsoInfo.getIsoInfo().size() > 0 && gffGeneIsoInfo.getEndAbs() > gffGeneIsoInfoAdd.getStartAbs()) {
							gffGeneIsoInfo.getIsoInfo().remove(gffGeneIsoInfo.getIsoInfo().size() - 1);
						}
					}
				}
			}
			GffGeneIsoInfo gffGeneIsoInfoTmpFinal = gffGeneIsoInfo;
			gffGeneIsoInfoTmpFinal.IsoName = gffGeneIsoInfoTmpFinal.getIsoName() + "///" + gffGeneIsoInfoAdd.getIsoName();
			//如果exon在上一个转录本的内部，直接删除
			if (gffGeneIsoInfoAdd.isCis5to3()) {
				if (gffGeneIsoInfoAdd.getIsoInfo().size() > 0 && gffGeneIsoInfoTmpFinal.getIsoInfo().size() > 0 && gffGeneIsoInfoAdd.getIsoInfo().get(0)[0] < gffGeneIsoInfoTmpFinal.getIsoInfo().get(gffGeneIsoInfoTmpFinal.getIsoInfo().size() - 1)[1]) {
					logger.error("出现重叠转录本：" + gffGeneIsoInfoAdd.getIsoName());
				}
				gffGeneIsoInfoTmpFinal.getIsoInfo().addAll( gffGeneIsoInfoAdd.getIsoInfo());
			} else {
					if (gffGeneIsoInfoAdd.getIsoInfo().size() > 0 && gffGeneIsoInfoTmpFinal.getIsoInfo().size() > 0 && gffGeneIsoInfoAdd.getIsoInfo().get( gffGeneIsoInfoAdd.getIsoInfo().size() - 1)[1] < gffGeneIsoInfoTmpFinal.getIsoInfo().get(0)[0]) {
						logger.error("出现重叠转录本：" + gffGeneIsoInfoAdd.getIsoName());
					}
				gffGeneIsoInfoTmpFinal.getIsoInfo().addAll(0, gffGeneIsoInfoAdd.getIsoInfo());
			}
			gffGeneIsoInfoTmpFinal.sortIso();
			lsGeneIsoInfosFinal.add(gffGeneIsoInfoTmpFinal);
		}		
		
		lsGffGeneIsoInfos = lsGeneIsoInfosFinal;		
		//重置起点和终点
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGeneIsoInfosFinal) {
			if (gffGeneIsoInfo.getIsoInfo().get(0)[0] < numberstart) {
				numberstart = gffGeneIsoInfo.getIsoInfo().get(0)[0];
			}
			if (gffGeneIsoInfo.getIsoInfo().get(0)[0] > numberend) {
				numberend = gffGeneIsoInfo.getIsoInfo().get(0)[0];
			}
			if (gffGeneIsoInfo.getIsoInfo().get(gffGeneIsoInfo.getIsoInfo().size() - 1)[1] < numberstart) {
				numberstart = gffGeneIsoInfo.getIsoInfo().get(gffGeneIsoInfo.getIsoInfo().size() - 1)[1];
			}
			if (gffGeneIsoInfo.getIsoInfo().get(gffGeneIsoInfo.getIsoInfo().size() - 1)[1] > numberend) {
				numberend = gffGeneIsoInfo.getIsoInfo().get(gffGeneIsoInfo.getIsoInfo().size() - 1)[1];
			}
		}
	}
	
	/**
	 * 循环添加，也就是说如果有2vs2的转录本，会加成4个iso
	 * 用于冯英的项目，将两个gffdetailGene的转录本头尾连接起来，并且取代原来的转录本信息
	 * 如果两个iso有交集但是交集小于0.3，则合并，否则增加一个新的iso
	 * @param gffDetailGene
	 */
	public void addIsoOls(GffDetailGene gffDetailGene)
	{
		ArrayList<GffGeneIsoInfo> lsGeneIsoInfosFinal = new ArrayList<GffGeneIsoInfo>();
		ArrayList<GffGeneIsoInfo> lsIsoAdd = gffDetailGene.getLsCodSplit();
		ArrayList<GffGeneIsoInfo> lsIsoThis = getLsCodSplit();
		for (GffGeneIsoInfo gffGeneIsoInfoTmp : lsIsoThis) {
			for (GffGeneIsoInfo gffGeneIsoInfoAddTmp : lsIsoAdd) {
				GffGeneIsoInfo gffGeneIsoInfo = gffGeneIsoInfoTmp.cloneDeep();
				GffGeneIsoInfo gffGeneIsoInfoAdd = gffGeneIsoInfoAddTmp.cloneDeep();
				//
				if (gffGeneIsoInfo.isCis5to3() != gffGeneIsoInfoAdd.isCis5to3()) {
					lsGeneIsoInfosFinal.add(gffGeneIsoInfo);
					lsGeneIsoInfosFinal.add(gffGeneIsoInfoAdd);
					logger.error("两个方向不一致的gff不能合并："+ gffGeneIsoInfo.getIsoName() + " " + gffGeneIsoInfoAdd.getIsoName());
					continue;
				}
				//似乎下面已经把这个问题解决了//////////////////////////////////////////////////////////////////////
				//如果第一个转录本的尾部和第二个转录本的头部有交集
				//////////////////////////////////////////////////////////////////////
				if (gffGeneIsoInfo.getEndAbs() > gffGeneIsoInfoAdd.getStartAbs()) {
					double[] region1 = new double[]{gffGeneIsoInfo.getStartAbs(), gffGeneIsoInfo.getEndAbs()};
					double[] region2 = new double[]{gffGeneIsoInfoAdd.getStartAbs(), gffGeneIsoInfoAdd.getEndAbs()};
					double[] overlapInfo = ArrayOperate.cmpArray(region1, region2);
					//如果重叠区域太长，那么就分开加入成两个转录本
					if (overlapInfo[2] > OVERLAP_RATIO || overlapInfo[3] > OVERLAP_RATIO) {
						lsGeneIsoInfosFinal.add(gffGeneIsoInfo);
						lsGeneIsoInfosFinal.add(gffGeneIsoInfoAdd);
						continue;
					}
					//如果重叠区域短长，那么就将短的掐头去尾
					else {
						if (gffGeneIsoInfo.getIsoLen() >= gffGeneIsoInfoAdd.getIsoLen()) {
							while (gffGeneIsoInfoAdd.getIsoInfo().size() > 0 && gffGeneIsoInfo.getEndAbs() > gffGeneIsoInfoAdd.getStartAbs()) {
								gffGeneIsoInfoAdd.getIsoInfo().remove(0);
							}
						}
						else {
							while (gffGeneIsoInfo.getIsoInfo().size() > 0 && gffGeneIsoInfo.getEndAbs() > gffGeneIsoInfoAdd.getStartAbs()) {
								gffGeneIsoInfo.getIsoInfo().remove(gffGeneIsoInfo.getIsoInfo().size() - 1);
							}
						}
					}
				}
				GffGeneIsoInfo gffGeneIsoInfoTmpFinal = gffGeneIsoInfo;
				gffGeneIsoInfoTmpFinal.IsoName = gffGeneIsoInfoTmpFinal.getIsoName() + "///" + gffGeneIsoInfoAdd.getIsoName();
				//如果exon在上一个转录本的内部，直接删除
				if (gffGeneIsoInfoAdd.isCis5to3()) {
					if (gffGeneIsoInfoAdd.getIsoInfo().size() > 0 && gffGeneIsoInfoTmpFinal.getIsoInfo().size() > 0 && gffGeneIsoInfoAdd.getIsoInfo().get(0)[0] < gffGeneIsoInfoTmpFinal.getIsoInfo().get(gffGeneIsoInfoTmpFinal.getIsoInfo().size() - 1)[1]) {
						logger.error("出现重叠转录本：" + gffGeneIsoInfoAdd.getIsoName());
					}
					gffGeneIsoInfoTmpFinal.getIsoInfo().addAll( gffGeneIsoInfoAdd.getIsoInfo());
				} else {
						if (gffGeneIsoInfoAdd.getIsoInfo().size() > 0 && gffGeneIsoInfoTmpFinal.getIsoInfo().size() > 0 && gffGeneIsoInfoAdd.getIsoInfo().get( gffGeneIsoInfoAdd.getIsoInfo().size() - 1)[1] < gffGeneIsoInfoTmpFinal.getIsoInfo().get(0)[0]) {
							logger.error("出现重叠转录本：" + gffGeneIsoInfoAdd.getIsoName());
						}
					gffGeneIsoInfoTmpFinal.getIsoInfo().addAll(0, gffGeneIsoInfoAdd.getIsoInfo());
				}
				gffGeneIsoInfoTmpFinal.sortIso();
				lsGeneIsoInfosFinal.add(gffGeneIsoInfoTmpFinal);
			}
		}
		lsGffGeneIsoInfos = lsGeneIsoInfosFinal;		
		//重置起点和终点
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGeneIsoInfosFinal) {
			if (gffGeneIsoInfo.getIsoInfo().get(0)[0] < numberstart) {
				numberstart = gffGeneIsoInfo.getIsoInfo().get(0)[0];
			}
			if (gffGeneIsoInfo.getIsoInfo().get(0)[0] > numberend) {
				numberend = gffGeneIsoInfo.getIsoInfo().get(0)[0];
			}
			if (gffGeneIsoInfo.getIsoInfo().get(gffGeneIsoInfo.getIsoInfo().size() - 1)[1] < numberstart) {
				numberstart = gffGeneIsoInfo.getIsoInfo().get(gffGeneIsoInfo.getIsoInfo().size() - 1)[1];
			}
			if (gffGeneIsoInfo.getIsoInfo().get(gffGeneIsoInfo.getIsoInfo().size() - 1)[1] > numberend) {
				numberend = gffGeneIsoInfo.getIsoInfo().get(gffGeneIsoInfo.getIsoInfo().size() - 1)[1];
			}
		}
	}
	
	/**
	 * 去除重复Isoform
	 */
	public void removeDupliIso()
	{
		ArrayList<GffGeneIsoInfo> lsNew = new ArrayList<GffGeneIsoInfo>();
		
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
			boolean flag = true;
			for (GffGeneIsoInfo gffGeneIsoInfo2 : lsNew) {
				if (gffGeneIsoInfo2.compIso(gffGeneIsoInfo.getIsoInfo())) {
					flag = false;
					break;
				}
			}
			if (flag) {
				lsNew.add(gffGeneIsoInfo);
			}
		}
		lsGffGeneIsoInfos = lsNew;
	}
	
	/**
	 * 用于冯英的项目，添加新的转录本
	 * 同时重新设定该基因的numberstart和numberend
	 * @param gffDetailGene
	 */
	public void addIso(GffGeneIsoInfo gffGeneIsoInfo)
	{
		ArrayList<int[]> lsExonThis = gffGeneIsoInfo.getIsoInfo();
		if (lsExonThis == null || lsExonThis.size() == 0) {
			return;
		}
		
		if (cis5to3 != null && gffGeneIsoInfo.isCis5to3() != cis5to3) {
			cis5to3 = null;
		}
		for (GffGeneIsoInfo gffGeneIsoInfoOld : lsGffGeneIsoInfos) {
			if (gffGeneIsoInfoOld.compIso(lsExonThis)) {
				return;
			}
		}
		
		lsGffGeneIsoInfos.add(gffGeneIsoInfo);
		String IsoName = gffGeneIsoInfo.IsoName;
		int i = lsGffGeneIsoInfos.size();
		//修改名字
		while (isContainsIso(IsoName)) {
			IsoName = FileOperate.changeFileSuffix(IsoName, "", ""+i).replace("/", "");
			i++;
		}
		gffGeneIsoInfo.IsoName = IsoName;

		if (numberstart < 0 || numberstart > gffGeneIsoInfo.getStartAbs()) {
			numberstart = gffGeneIsoInfo.getStartAbs();
		}
		if (numberend < 0 || numberend < gffGeneIsoInfo.getEndAbs()) {
			numberend = gffGeneIsoInfo.getEndAbs();
		}
	}
	
	public String getGTFformate(String title) {
		String geneGTF = "";
		for (GffGeneIsoInfo gffGeneIsoInfo : getLsCodSplit()) {
			gffGeneIsoInfo.sortIso();
			geneGTF = geneGTF + gffGeneIsoInfo.getGTFformat(getLocString().split(SEP_GENE_NAME)[0], title);
		}
		return geneGTF;
	}
	
	public String getGFFformate(String title) {
		String geneGFF = getChrID() + "\t" +title + "\tmRNA\t" + getNumberstart()+ "\t" + getNumberend()
        + "\t"+"."+"\t" +isCis5to3()+"\t.\t"+ "ID=" + getLocString().split(SEP_GENE_NAME)[0]
        +";Name="+getLocString().split(SEP_GENE_NAME)[0]+ ";Name="+getLocString().split(SEP_GENE_NAME)[0] + " \r\n";
		for (GffGeneIsoInfo gffGeneIsoInfo : getLsCodSplit()) {
			geneGFF = geneGFF + getChrID() + "\t" +title + "\tmRNA\t" +gffGeneIsoInfo.getStartAbs()+ "\t" + gffGeneIsoInfo.getEndAbs()
	        + "\t"+"."+"\t" +gffGeneIsoInfo.isCis5to3()+"\t.\t"+ "ID=" + gffGeneIsoInfo.getIsoName() 
	        +";Name="+gffGeneIsoInfo.getIsoName()+ ";Parent="+ getLocString().split(SEP_GENE_NAME)[0] + " \r\n";
			gffGeneIsoInfo.sortIso();
			geneGFF = geneGFF + gffGeneIsoInfo.getGFFformat(getLocString().split(SEP_GENE_NAME)[0], title);
		}
		return geneGFF;
	}
	
	/**
	 * 判断是否存在该名字的转录本
	 * @param IsoName
	 */
	public boolean isContainsIso(String IsoName)
	{
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
			if (gffGeneIsoInfo.getIsoName().equalsIgnoreCase(IsoName)) {
				return true;
			}
		}
		return false;
	}
}
