package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.apache.log4j.Logger;

import com.novelbio.analysis.annotation.copeID.CopedID;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGene;
/**
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
	
	public final static String INTRON = "intron";
	public final static String EXON_WITHOUT_UTR = "exon_without_utr";
	public final static String EXON = "exon";
	public final static String UTR5 = "5utr";
	public final static String UTR3 = "3utr";
	public final static String TSS = "tss";
	public final static String TES = "tes";
	
	
	
	int taxID = 0;
	/**
	 * 设定基因的转录起点终点位置信息
	 * @param UpStreamTSSbp 设定基因的转录起点上游长度，默认为3000bp
	 * @param DownStreamTssbp 设定基因的转录起点下游长度，默认为2000bp
	 * @param GeneEnd3UTR 设定基因结尾向外延伸的长度，默认为100bp
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
	/**
	 * 顺序存储每个转录本的的坐标情况
	 */
	private ArrayList<GffGeneIsoInfo> lsGffGeneIsoInfos = new ArrayList<GffGeneIsoInfo>();//存储可变剪接的mRNA
	/**
	 * 顺序存储每个转录本的名字
	 */
	private ArrayList<String> lsIsoName = new ArrayList<String>();
	
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
	 * 
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
	protected void addsplitlist(String splitName) {
		GffGeneIsoInfo gffGeneIsoInfo = null;
		if (cis5to3) {
			gffGeneIsoInfo = new GffGeneIsoCis(splitName,this);
		}
		else {
			gffGeneIsoInfo = new GffGeneIsoTrans(splitName,this);
		}
		gffGeneIsoInfo.setTaxID(this.taxID);
		lsGffGeneIsoInfos.add(gffGeneIsoInfo);
		lsIsoName.add(splitName);
	}
	/**
	 * @return 返回转录本的数目
	 */
	public int getSplitlistNumber() {
		return lsGffGeneIsoInfos.size();
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
     */
    public GffGeneIsoInfo getIsolist(String splitID)
    {  
    	return lsGffGeneIsoInfos.get(lsIsoName.indexOf(splitID));//include one special loc start number to end number	
    }

    private int getLongestSplitID() {
    	if (lsGffGeneIsoInfos.size() == 1) {
			return 0;
		}
		ArrayList<Integer> lslength = new ArrayList<Integer>();
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
			ArrayList<int[]> lsExon = gffGeneIsoInfo.getIsoInfo();
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
	 * 用坐标查找具体的转录本信息，如果坐标信息相同，则返回以前的信息
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
			if (cis5to3) { //0    1     2     3     4     5   每个外显子中 1 > 0      0    atg   1
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
			if (cis5to3) { //0    1     2     3     4     5   每个外显子中 0 < 1      0    uag   1
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
}
