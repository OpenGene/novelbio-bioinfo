package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.log4j.Logger;

import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.listOperate.ListDetailAbs;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.modcopeid.CopedID;
/**
 * 考虑将其中的iso装入hash表中，以加快查找效率
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
public class GffDetailGene extends ListDetailAbs
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
	/** 两个转录本在一个基因下，那么这个基因名可能是结合了两个名字，用该符号分割 */
	public final static String SEP_GENE_NAME = "/";
	/**  同一个iso如果有多个名字，则用该符号分割ISO */
	private final static String SEP_ISO_NAME = "@//@";
	/** 顺序存储每个转录本的的坐标情况 */
	private ArrayList<GffGeneIsoInfo> lsGffGeneIsoInfos = new ArrayList<GffGeneIsoInfo>();//存储可变剪接的mRNA
	int taxID = 0;
	
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
	private int getIsoID(String isoName) {
		for (int i = 0; i < lsGffGeneIsoInfos.size(); i++) {
			GffGeneIsoInfo gffGeneIsoInfo = lsGffGeneIsoInfos.get(i);
			String[] tmpName = gffGeneIsoInfo.getName().split(SEP_ISO_NAME);
			for (String string : tmpName) {
				string = CopedID.removeDot(string);
				if(string.equalsIgnoreCase( CopedID.removeDot(isoName) ))
				{
					return i;
				}
			}
		}
		return -1;
	}
	/**
	 * 划定Tss范围上游为负数，下游为正数
	 * 同时设定里面当时含有的全部GffGeneIsoInfo
	 * @param upTss
	 * @param downTss
	 */
	@Override
	public void setTssRegion(int upTss, int downTss) {
		super.upTss = upTss;
		super.downTss = downTss;
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
			gffGeneIsoInfo.setTssRegion(upTss, downTss);
		}
	}
	/**
	 * 划定Tes范围上游为负数，下游为正数
	 * 同时设定里面当时含有的全部GffGeneIsoInfo
	 * @param upTes
	 * @param downTes
	 */
	@Override
	public void setTesRegion(int upTes, int downTes) {
		this.upGeneEnd3UTR = upTes;
		this.downGeneEnd3UTR = downTes;
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
			gffGeneIsoInfo.setTesRegion(upTes, downTes);
		}
	}
	/**
	 * 划定Tss范围上游为负数，下游为正数
	 * @param upTss
	 * @param downTss
	 */
	public void setTssRegion(int[] Tss) {
		if (Tss != null) {
			this.upTss = Tss[0];
			this.downTss = Tss[1];
			for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
				gffGeneIsoInfo.setTssRegion(upTss, downTss);
			}
		}
	}
	/**
	 * 划定Tss范围上游为负数，下游为正数
	 * @param upTss
	 * @param downTss
	 */
	public void setTesRegion(int[] Tes) {
		if (Tes != null) {
			this.upGeneEnd3UTR = Tes[0];
			this.downGeneEnd3UTR = Tes[1];
			for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
				gffGeneIsoInfo.setTesRegion(upGeneEnd3UTR, downGeneEnd3UTR);
			}
		}
	}
	/**
	 * @param chrID
	 * @param locString
	 * @param cis5to3
	 */
	public GffDetailGene(String chrID, String locString, boolean cis5to3) {
		super(chrID, locString, cis5to3);
	}
	/**
	 * 给最后一个转录本添加exon坐标，<br>
	 * 只需要注意按照次序装，也就是说如果正向要从小到大的加，反向从大到小的加
	 * 然而具体加入这一对坐标的时候，并不需要分别大小，程序会根据gene方向自动判定
	 * <b>如果发现一个没有转录本的，则新添加一个gene设置类型为pseudo</b>
	 */
	protected void addExon(int locStart,int locEnd) {
		if (lsGffGeneIsoInfos.size() == 0) {//如果发现一个没有转录本的，则新添加一个gene设置类型为pseudo
			addsplitlist(getName(), "pseudo");
		}
		GffGeneIsoInfo gffGeneIsoInfo = lsGffGeneIsoInfos.get(lsGffGeneIsoInfos.size()-1);//include one special loc start number to end number
		gffGeneIsoInfo.addExon(locStart, locEnd);
	}
	/**
	 * 针对水稻拟南芥的GFF文件
	 * 给转录本添加exon坐标，GFF3的exon的格式是
	 * 当gene为反方向时，exon是从大到小排列的
	 * 在添加exon的时候，如果本CDS与UTR之间是连着的，那么就将本CDS和UTR连在一起，放在一个exon中 如果不连，就按原来的来
	 */
	protected void addExonGFFCDSUTR(int locStart,int locEnd) {
		GffGeneIsoInfo gffGeneIsoInfo = lsGffGeneIsoInfos.get(lsGffGeneIsoInfos.size()-1);//include one special loc start number to end number
		gffGeneIsoInfo.addExonGFFCDSUTR(locStart, locEnd);
	}
	
	/**
	 * 给最后一个转录本添加ATG和UAG坐标，<br>
	 * 加入这一对坐标的时候，并不需要分别大小，程序会根据gene方向自动判定
	 * 会自动判定输入的起点是否小于已有的atg，终点是否大于已有的uag
	 * 是的话，才会设定，否则就不设定
	 */
	protected void setATGUAG(int atg, int uag) {
		GffGeneIsoInfo gffGeneIsoInfo = lsGffGeneIsoInfos.get(lsGffGeneIsoInfos.size()-1);//include one special loc start number to end number
		gffGeneIsoInfo.setATGUAG(atg, uag);
	}
	/**
	 * 如果是非编码RNA，则将atg和uag设置为最后一位
	 */
	protected void setATGUAGncRNA() {
		GffGeneIsoInfo gffGeneIsoInfo = lsGffGeneIsoInfos.get(lsGffGeneIsoInfos.size()-1);//include one special loc start number to end number
		gffGeneIsoInfo.sort();
		gffGeneIsoInfo.setATGUAGncRNA();
	}
	
	/**
	 * 直接添加转录本，根据genedetail的信息设置cis5to3。之后用addcds()方法给该转录本添加exon
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
    public GffGeneIsoInfo getIsolist(int splitnum) {  
    	return lsGffGeneIsoInfos.get(splitnum);//include one special loc start number to end number	
    }
    /**
     * 给定转录本名(UCSC里实际上是基因名)<br>
     * 没有则返回null
     */
    public GffGeneIsoInfo getIsolist(String splitID) {
    	int index = getIsoID(splitID);
    	if (index == -1) {
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
			if (gffGeneIsoInfo.size() == 0)
				lslength.add(0);
			else
				lslength.add(gffGeneIsoInfo.getLen());
		}
		int max = lslength.get(0);
		int id = 0;
		
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
	public int getTypeLength(String type,int num) {
		GffGeneIsoInfo gffGeneIsoInfo = getLongestSplit();
		//TODO 如果超出需要返回0
		if (type.equals(INTRON)) {
			return gffGeneIsoInfo.getLenIntron(num);
		}
		if (type.equals(EXON)) {
			return gffGeneIsoInfo.getLenExon(num);
		}
		if (type.equals(UTR5)) 
		{
			return gffGeneIsoInfo.getLenUTR5();
		}
		if (type.equals(UTR3)) 
		{
			return gffGeneIsoInfo.getLenUTR3();
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
	public String[] getInfo(int coord) {
		String[] anno = new String[4];
		for (int i = 0; i < anno.length; i++) {
			anno[i] = "";
		}
		HashSet<CopedID> hashCopedID = new HashSet<CopedID>();
		if (isCodInGeneExtend(coord)) {
			for (GffGeneIsoInfo gffGeneIsoInfo : getLsCodSplit()) {
				if (gffGeneIsoInfo.isCodInIsoExtend(coord)) {
					hashCopedID.add(gffGeneIsoInfo.getCopedID());
				}
			}
			for (CopedID copedID : hashCopedID) {
				if (anno.equals("")) {
					anno[0] = copedID.getAccID();
					anno[1] = copedID.getSymbol();
					anno[2] = copedID.getDescription();
				}
				else {
					anno[0] = anno[0]+"//"+copedID.getAccID();
					anno[1] = anno[1]+"//"+copedID.getSymbol();
					anno[2] = anno[2]+"//"+copedID.getDescription();
				}
			}
			if (getLongestSplit().isCodInIsoExtend(coord)) {
				anno[4] = getLongestSplit().getCodLocStr(coord);
			}
			else {
				for (GffGeneIsoInfo gffGeneIsoInfo : getLsCodSplit()) {
					if (gffGeneIsoInfo.isCodInIsoExtend(coord)) {
						anno[4] = gffGeneIsoInfo.getCodLocStr(coord);
						break;
					}
				}
			}
		}
		return anno;
	}
	
	/**
	 * 用于冯英的项目，将两个gffdetailGene的转录本头尾连接起来，并且取代原来的转录本信息
	 * 如果两个iso有交集但是交集小于0.3，则合并，否则增加一个新的iso
	 * @param gffDetailGene
	 */
	public void addIso(GffDetailGene gffDetailGene) {
		ArrayList<GffGeneIsoInfo> lsGeneIsoInfosFinal = new ArrayList<GffGeneIsoInfo>();
		ArrayList<GffGeneIsoInfo> lsIsoAdd = gffDetailGene.getLsCodSplit();
		ArrayList<GffGeneIsoInfo> lsIsoThis = getLsCodSplit();
		
		
		for (int i = 0; i < lsIsoThis.size(); i++) {
			if (i >= lsIsoAdd.size()) {
				break;
			}
			GffGeneIsoInfo gffGeneIsoInfoTmp = lsIsoThis.get(i);
			GffGeneIsoInfo gffGeneIsoInfoAddTmp = lsIsoAdd.get(i);
			

			GffGeneIsoInfo gffGeneIsoInfo = gffGeneIsoInfoTmp.clone();
			GffGeneIsoInfo gffGeneIsoInfoAdd = gffGeneIsoInfoAddTmp.clone();
			//
			if (gffGeneIsoInfo.isCis5to3() != gffGeneIsoInfoAdd.isCis5to3()) {
				lsGeneIsoInfosFinal.add(gffGeneIsoInfo);
				lsGeneIsoInfosFinal.add(gffGeneIsoInfoAdd);
				logger.error("两个方向不一致的gff不能合并："+ gffGeneIsoInfo.getName() + " " + gffGeneIsoInfoAdd.getName());
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
					if (gffGeneIsoInfo.getLen() >= gffGeneIsoInfoAdd.getLen()) {
						while (gffGeneIsoInfoAdd.size() > 0 && gffGeneIsoInfo.getEndAbs() > gffGeneIsoInfoAdd.getStartAbs()) {
							gffGeneIsoInfoAdd.remove(0);
						}
					}
					else {
						while (gffGeneIsoInfo.size() > 0 && gffGeneIsoInfo.getEndAbs() > gffGeneIsoInfoAdd.getStartAbs()) {
							gffGeneIsoInfo.remove(gffGeneIsoInfo.size() - 1);
						}
					}
				}
			}
			GffGeneIsoInfo gffGeneIsoInfoTmpFinal = gffGeneIsoInfo;
			gffGeneIsoInfoTmpFinal.setName( gffGeneIsoInfoTmpFinal.getName() + "///" + gffGeneIsoInfoAdd.getName() );
			//如果exon在上一个转录本的内部，直接删除
			if (gffGeneIsoInfoAdd.isCis5to3()) {
				if (gffGeneIsoInfoAdd.size() > 0 && gffGeneIsoInfoTmpFinal.size() > 0 && gffGeneIsoInfoAdd.get(0).getStartCis() < gffGeneIsoInfoTmpFinal.get(gffGeneIsoInfoTmpFinal.size() - 1).getEndCis()) {
					logger.error("出现重叠转录本：" + gffGeneIsoInfoAdd.getName());
				}
				gffGeneIsoInfoTmpFinal.addAll( gffGeneIsoInfoAdd);
			} else {
					if (gffGeneIsoInfoAdd.size() > 0 && gffGeneIsoInfoTmpFinal.size() > 0 && gffGeneIsoInfoAdd.get( gffGeneIsoInfoAdd.size() - 1).getEndCis() < gffGeneIsoInfoTmpFinal.get(0).getStartCis()) {
						logger.error("出现重叠转录本：" + gffGeneIsoInfoAdd.getName());
					}
				gffGeneIsoInfoTmpFinal.addAll(0, gffGeneIsoInfoAdd);
			}
			gffGeneIsoInfoTmpFinal.sort();
			lsGeneIsoInfosFinal.add(gffGeneIsoInfoTmpFinal);
		}		
		
		lsGffGeneIsoInfos = lsGeneIsoInfosFinal;		
		//重置起点和终点
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGeneIsoInfosFinal) {
			if (gffGeneIsoInfo.get(0).getStartCis() < numberstart) {
				numberstart = gffGeneIsoInfo.get(0).getStartCis();
			}
			if (gffGeneIsoInfo.get(0).getStartCis() > numberend) {
				numberend = gffGeneIsoInfo.get(0).getStartCis();
			}
			if (gffGeneIsoInfo.get(gffGeneIsoInfo.size() - 1).getEndCis() < numberstart) {
				numberstart = gffGeneIsoInfo.get(gffGeneIsoInfo.size() - 1).getEndCis();
			}
			if (gffGeneIsoInfo.get(gffGeneIsoInfo.size() - 1).getEndCis() > numberend) {
				numberend = gffGeneIsoInfo.get(gffGeneIsoInfo.size() - 1).getEndCis();
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
				GffGeneIsoInfo gffGeneIsoInfo = gffGeneIsoInfoTmp.clone();
				GffGeneIsoInfo gffGeneIsoInfoAdd = gffGeneIsoInfoAddTmp.clone();
				//
				if (gffGeneIsoInfo.isCis5to3() != gffGeneIsoInfoAdd.isCis5to3()) {
					lsGeneIsoInfosFinal.add(gffGeneIsoInfo);
					lsGeneIsoInfosFinal.add(gffGeneIsoInfoAdd);
					logger.error("两个方向不一致的gff不能合并："+ gffGeneIsoInfo.getName() + " " + gffGeneIsoInfoAdd.getName());
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
						if (gffGeneIsoInfo.getLen() >= gffGeneIsoInfoAdd.getLen()) {
							while (gffGeneIsoInfoAdd.size() > 0 && gffGeneIsoInfo.getEndAbs() > gffGeneIsoInfoAdd.getStartAbs()) {
								gffGeneIsoInfoAdd.remove(0);
							}
						}
						else {
							while (gffGeneIsoInfo.size() > 0 && gffGeneIsoInfo.getEndAbs() > gffGeneIsoInfoAdd.getStartAbs()) {
								gffGeneIsoInfo.remove(gffGeneIsoInfo.size() - 1);
							}
						}
					}
				}
				GffGeneIsoInfo gffGeneIsoInfoTmpFinal = gffGeneIsoInfo;
				gffGeneIsoInfoTmpFinal.setName( gffGeneIsoInfoTmpFinal.getName() + "///" + gffGeneIsoInfoAdd.getName()  );
				//如果exon在上一个转录本的内部，直接删除
				if (gffGeneIsoInfoAdd.isCis5to3()) {
					if (gffGeneIsoInfoAdd.size() > 0 && gffGeneIsoInfoTmpFinal.size() > 0 && gffGeneIsoInfoAdd.get(0).getStartCis() < gffGeneIsoInfoTmpFinal.get(gffGeneIsoInfoTmpFinal.size() - 1).getEndCis()) {
						logger.error("出现重叠转录本：" + gffGeneIsoInfoAdd.getName());
					}
					gffGeneIsoInfoTmpFinal.addAll( gffGeneIsoInfoAdd);
				} else {
						if (gffGeneIsoInfoAdd.size() > 0 && gffGeneIsoInfoTmpFinal.size() > 0 && gffGeneIsoInfoAdd.get( gffGeneIsoInfoAdd.size() - 1).getEndCis() < gffGeneIsoInfoTmpFinal.get(0).getStartCis()) {
							logger.error("出现重叠转录本：" + gffGeneIsoInfoAdd.getName());
						}
					gffGeneIsoInfoTmpFinal.addAll(0, gffGeneIsoInfoAdd);
				}
				gffGeneIsoInfoTmpFinal.sort();
				lsGeneIsoInfosFinal.add(gffGeneIsoInfoTmpFinal);
			}
		}
		lsGffGeneIsoInfos = lsGeneIsoInfosFinal;		
		//重置起点和终点
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGeneIsoInfosFinal) {
			if (gffGeneIsoInfo.get(0).getStartCis() < numberstart) {
				numberstart = gffGeneIsoInfo.get(0).getStartCis();
			}
			if (gffGeneIsoInfo.get(0).getStartCis() > numberend) {
				numberend = gffGeneIsoInfo.get(0).getStartCis();
			}
			if (gffGeneIsoInfo.get(gffGeneIsoInfo.size() - 1).getEndCis() < numberstart) {
				numberstart = gffGeneIsoInfo.get(gffGeneIsoInfo.size() - 1).getEndCis();
			}
			if (gffGeneIsoInfo.get(gffGeneIsoInfo.size() - 1).getEndCis() > numberend) {
				numberend = gffGeneIsoInfo.get(gffGeneIsoInfo.size() - 1).getEndCis();
			}
		}
	}
	/**
	 * 去除重复Isoform
	 */
	public void removeDupliIso()
	{
		HashMap<GffGeneIsoInfo, Integer> hashIso = new HashMap<GffGeneIsoInfo, Integer>();
		ArrayList<GffGeneIsoInfo> lsResult = new ArrayList<GffGeneIsoInfo>();
		int numIso = 0;
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
			//如果发现重复iso，则获得原来那个iso在lsResult里面的位置，并且将原来的iso的名字添上
			if (hashIso.containsKey(gffGeneIsoInfo)) {
				int num = hashIso.get(gffGeneIsoInfo);
				//获得原来的iso
				GffGeneIsoInfo gffGeneIsoInfoInside = lsResult.get(num);
				gffGeneIsoInfoInside.setName(gffGeneIsoInfoInside.getName() + SEP_ISO_NAME + gffGeneIsoInfo.getName());
				continue;
			}
			lsResult.add(gffGeneIsoInfo);
			hashIso.put(gffGeneIsoInfo, numIso); numIso ++;
		}
		this.lsGffGeneIsoInfos = lsResult;
	}
	
	/**
	 * 效率低下，等待优化
	 * 用于冯英的项目，添加新的转录本
	 * 同时重新设定该基因的numberstart和numberend
	 * @param gffDetailGene
	 */
	public void addIso(GffGeneIsoInfo gffGeneIsoInfo)
	{
		if (gffGeneIsoInfo == null || gffGeneIsoInfo.size() == 0) {
			return;
		}
		
		if (cis5to3 != null && gffGeneIsoInfo.isCis5to3() != cis5to3) {
			cis5to3 = null;
		}
		for (GffGeneIsoInfo gffGeneIsoInfoOld : lsGffGeneIsoInfos) {
			//比较两个list是否一致，exon的equals只比较起点终点
			if (gffGeneIsoInfoOld.compIso(gffGeneIsoInfo)) {
				return;
			}
		}
		lsGffGeneIsoInfos.add(gffGeneIsoInfo);
		String IsoName = gffGeneIsoInfo.getName();
		int i = lsGffGeneIsoInfos.size();
		//修改名字
		while (isContainsIso(IsoName)) {
			IsoName = FileOperate.changeFileSuffix(IsoName, "", ""+i).replace("/", "");
			i++;
		}
		gffGeneIsoInfo.setName(IsoName);

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
			gffGeneIsoInfo.sort();
			geneGTF = geneGTF + gffGeneIsoInfo.getGTFformat(getName().split(SEP_GENE_NAME)[0], title);
		}
		return geneGTF;
	}
	
	public String getGFFformate(String title) {
		String strand = "+";
		if (!isCis5to3()) {
			strand = "-";
		}
		String geneGFF = getParentName() + "\t" +title + "\tgene\t" + getStartAbs()+ "\t" + getEndAbs()
        + "\t"+"."+"\t" +strand+"\t.\t"+ "ID=" + getName().split(SEP_GENE_NAME)[0]
        +";Name="+getName().split(SEP_GENE_NAME)[0]+ ";Name="+getName().split(SEP_GENE_NAME)[0] + " \r\n";
		for (GffGeneIsoInfo gffGeneIsoInfo : getLsCodSplit()) {
			String strandmRNA = "+";
			if (!gffGeneIsoInfo.isCis5to3()) {
				strandmRNA = "-";
			}
			geneGFF = geneGFF + getParentName() + "\t" +title + "\tmRNA\t" +gffGeneIsoInfo.getStartAbs()+ "\t" + gffGeneIsoInfo.getEndAbs()
	        + "\t"+"."+"\t" +strandmRNA+"\t.\t"+ "ID=" + gffGeneIsoInfo.getName() 
	        +";Name="+gffGeneIsoInfo.getName()+ ";Parent="+ getName().split(SEP_GENE_NAME)[0] + " \r\n";
			gffGeneIsoInfo.sort();
			geneGFF = geneGFF + gffGeneIsoInfo.getGFFformat(getName().split(SEP_GENE_NAME)[0], title);
		}
		return geneGFF;
	}
	/**
	 * 获得坐标到该ItemEnd的距离
	 * 如果本基因包含了两条方向相反的基因，那么判断长的那条
	 * 坐标到条目终点的位置，考虑正反向<br/>
	 * 将该基因按照 >--------5start>--------->3end------->方向走
	 * 如果坐标在end的5方向，则为负数
	 * 如果坐标在end的3方向，则为正数
	 * @return
	 */
	public Integer getCod2End(int coord) {
		if (cis5to3 != null) {
			return super.getCod2End(coord);
		}
		return getLongestSplit().getCod2Tes(coord);
	}
	
	/**
	 * 获得坐标到该ItemStart的距离,如果coord小于0说明有问题，则返回null
	 * 用之前先设定coord
	 * 考虑item的正反
	 * 坐标到条目终点的位置，考虑正反向<br/>
	 * 将该基因按照 >--------5start>--------->3end------->方向走
	 * 如果坐标在start的5方向，则为负数
	 * 如果坐标在start的3方向，则为正数
	 * @return
	 */
	public Integer getCod2Start(int coord) {
		if (cis5to3 != null) {
			return super.getCod2End(coord);
		}
		return getLongestSplit().getCod2Tss(coord);
	}
	/**
	 * 判断是否存在该名字的转录本
	 * @param IsoName
	 */
	private boolean isContainsIso(String IsoName) {
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
			if (gffGeneIsoInfo.getName().toLowerCase().contains(IsoName.toLowerCase())) {
				return true;
			}
		}
		return false;
	}
	/**
	 * 浅度clone，lsGffGeneIsoInfos 克隆了。
	 * 但是每个iso没有被clone
	 */
	public GffDetailGene clone() {
		GffDetailGene result = null;
		result = (GffDetailGene) super.clone();
		result.taxID = taxID;
		result.lsGffGeneIsoInfos = new ArrayList<GffGeneIsoInfo>();
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
			result.getLsCodSplit().add(gffGeneIsoInfo);
		}
		return result;
	}
}
