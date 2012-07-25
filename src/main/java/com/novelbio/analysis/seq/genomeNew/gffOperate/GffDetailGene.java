package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.gffOperate.ExonInfo.ExonCluster;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.listOperate.ListDetailAbs;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.SepSign;
import com.novelbio.database.model.modcopeid.GeneID;
import com.novelbio.generalConf.NovelBioConst;
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
public class GffDetailGene extends ListDetailAbs {
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
	/** 顺序存储每个转录本的的坐标情况 */
	private ArrayList<GffGeneIsoInfo> lsGffGeneIsoInfos = new ArrayList<GffGeneIsoInfo>();//存储可变剪接的mRNA
	ListGff listGff;
	int taxID = 0;
	
	boolean removeDuplicateIso = false;
	/**
	 * 一个基因如果有不止一个的转录本，那么这些转录本的同一区域的exon就可以提取出来，并放入该list
	 * 也就是每个exoncluster就是一个exon类，表示 
	 */
	ArrayList<ExonCluster> lsExonClusters = new ArrayList<ExonCluster>();
	/**
	 * @param chrID 内部小写
	 * @param locString
	 * @param cis5to3
	 */
	public GffDetailGene(String chrID, String locString, boolean cis5to3) {
		super(chrID, locString, cis5to3);
	}
	public GffDetailGene(ListGff listGff, String locString, boolean cis5to3) {
		super(listGff, locString, cis5to3);
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
	 * 返回所有的转录本信息
	 * @param coord
	 */
	public ArrayList<GffGeneIsoInfo> getLsCodSplit() {
		return lsGffGeneIsoInfos;
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
			String[] tmpName = gffGeneIsoInfo.getName().split(SepSign.SEP_ID);
			for (String string : tmpName) {
				string = GeneID.removeDot(string);
				if(string.equalsIgnoreCase( GeneID.removeDot(isoName) )) {
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
	 * 给最后一个转录本添加exon坐标，<br>
	 * 只需要注意按照次序装，也就是说如果正向要从小到大的加，反向从大到小的加
	 * 然而具体加入这一对坐标的时候，并不需要分别大小，程序会根据gene方向自动判定
	 * <b>如果发现一个没有转录本的，则新添加一个gene设置类型为pseudo</b>
	 */
	protected void addExon(int locStart,int locEnd) {
		if (lsGffGeneIsoInfos.size() == 0) {//如果发现一个没有转录本的，则新添加一个gene设置类型为pseudo
			addsplitlist(getName().get(0), GffGeneIsoInfo.TYPE_GENE_PSEU_TRANSCRIPT);
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
	protected void addsplitlist(String splitName, int geneTpye) {
		removeDuplicateIso = false;
		GffGeneIsoInfo gffGeneIsoInfo = null;
		if (cis5to3) {
			gffGeneIsoInfo = new GffGeneIsoCis(splitName,this, geneTpye);
		}
		else {
			gffGeneIsoInfo = new GffGeneIsoTrans(splitName,this, geneTpye);
		}
		lsGffGeneIsoInfos.add(gffGeneIsoInfo);
	}
	/**
	 * 直接添加转录本，之后用addcds()方法给该转录本添加exon
	 */
	protected void addsplitlist(String splitName, int geneTpye, boolean cis5to3) {
		removeDuplicateIso = false;
		GffGeneIsoInfo gffGeneIsoInfo = null;
		if (cis5to3) {
			gffGeneIsoInfo = new GffGeneIsoCis(splitName,this, geneTpye);
		}
		else {
			gffGeneIsoInfo = new GffGeneIsoTrans(splitName,this, geneTpye);
		}
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
	 * 如果为null，则返回最长转录本的方向
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
	/** 获得该基因中最长的一条转录本的信息 */
	public GffGeneIsoInfo getLongestSplit() {
		int id = getLongestSplitID();
		return lsGffGeneIsoInfos.get(id);
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
			if (lslength.get(i) > max) {
				max = lslength.get(i);
				id = i;
			}
		}
		return id;
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
		if (num > gffGeneIsoInfo.size()) {
			return 0;
		}
		if (type.equals(INTRON)) {
			return gffGeneIsoInfo.getLenIntron(num);
		}
		if (type.equals(EXON)) {
			return gffGeneIsoInfo.getLenExon(num);
		}
		if (type.equals(UTR5)) {
			return gffGeneIsoInfo.getLenUTR5();
		}
		if (type.equals(UTR3)) {
			return gffGeneIsoInfo.getLenUTR3();
		}
		return -1000000;
	}
	public void clearIso() {
		lsGffGeneIsoInfos.clear();
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
		HashSet<GeneID> hashCopedID = new HashSet<GeneID>();
		if (isCodInGeneExtend(coord)) {
			for (GffGeneIsoInfo gffGeneIsoInfo : getLsCodSplit()) {
				if (gffGeneIsoInfo.isCodInIsoExtend(coord)) {
					hashCopedID.add(gffGeneIsoInfo.getGeneID());
				}
			}
			for (GeneID copedID : hashCopedID) {
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
				anno[4] = getLongestSplit().toStringCodLocStr(coord);
			}
			else {
				for (GffGeneIsoInfo gffGeneIsoInfo : getLsCodSplit()) {
					if (gffGeneIsoInfo.isCodInIsoExtend(coord)) {
						anno[4] = gffGeneIsoInfo.toStringCodLocStr(coord);
						break;
					}
				}
			}
		}
		return anno;
	}
	/**
	 * 将gffDetailGene中含有新的名字的iso添加入本类
	 * @param gffDetailGene
	 */
	public void addIsoSimple(GffDetailGene gffDetailGene) {
		for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
			addIso(gffGeneIsoInfo);
		}
	}
	
	/**
	 * 去除重复Isoform
	 */
	public void removeDupliIso() {
		if (removeDuplicateIso) {
			return;
		}
		removeDuplicateIso = true;
		HashMap<GffGeneIsoInfo, Integer> hashIso = new HashMap<GffGeneIsoInfo, Integer>();
		ArrayList<GffGeneIsoInfo> lsResult = new ArrayList<GffGeneIsoInfo>();
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
			if (hashIso.containsKey(gffGeneIsoInfo)) {
				continue;
			}
			lsResult.add(gffGeneIsoInfo);
		}
		this.lsGffGeneIsoInfos = lsResult;
	}
	
	/**
	 * 效率低下，等待优化
	 * 添加新的转录本
	 * 同时重新设定该基因的numberstart和numberend
	 * @param gffDetailGeneParent
	 */
	public void addIso(GffGeneIsoInfo gffGeneIsoInfo) {
		gffGeneIsoInfo.setGffDetailGeneParent(this);
		removeDuplicateIso = false;
		//TODO
		if (gffGeneIsoInfo == null || gffGeneIsoInfo.size() == 0) {
			return;
		}
		
		if (cis5to3 != null && gffGeneIsoInfo.isCis5to3() != cis5to3) {
			cis5to3 = null;
		}
		for (GffGeneIsoInfo gffGeneIsoInfoOld : lsGffGeneIsoInfos) {
			//比较两个list是否一致，exon的equals只比较起点终点
			if (gffGeneIsoInfoOld.equalsIso(gffGeneIsoInfo)) {
				return;
			}
		}

		String IsoName = gffGeneIsoInfo.getName();
		int i = lsGffGeneIsoInfos.size();
		//修改名字
		while (isContainsIso(IsoName)) {
			IsoName = FileOperate.changeFileSuffix(IsoName, "", ""+i).replace("/", "");
			i++;
		}
		gffGeneIsoInfo.setName(IsoName);
		lsGffGeneIsoInfos.add(gffGeneIsoInfo);
		
		if (numberstart < 0 || numberstart > gffGeneIsoInfo.getStartAbs()) {
			numberstart = gffGeneIsoInfo.getStartAbs();
		}
		if (numberend < 0 || numberend < gffGeneIsoInfo.getEndAbs()) {
			numberend = gffGeneIsoInfo.getEndAbs();
		}
	}
	/**
	 * 返回有差异的exon系列
	 * @return
	 */
	public ArrayList<ExonCluster> getDifExonCluster() {
		lsExonClusters = GffGeneIsoInfo.getExonCluster(isCis5to3(), lsGffGeneIsoInfos);
		ArrayList<ExonCluster> lsDifExon = new ArrayList<ExonCluster>();
		for (ExonCluster exonClusters : lsExonClusters) {
			if (exonClusters.isSameExon()) {
				continue;
			}
			lsDifExon.add(exonClusters);
		}
		return lsDifExon;
	}
	/**
	 * 给定一个转录本，返回与之最接近的转录本，相似度必须在指定范围内
	 * 没有
	 * @param gffGeneIsoInfo
	 * @param likelyhood 相似度 0-1之间
	 * @return 没有则返回null
	 */
	public GffGeneIsoInfo getSimilarIso(GffGeneIsoInfo gffGeneIsoInfo, double likelyhood) {
		HashMap<int[], GffGeneIsoInfo> mapCompInfo2GeneIso = new HashMap<int[], GffGeneIsoInfo>();
		ArrayList<int[]> lsCompInfo = new ArrayList<int[]>();
		for (GffGeneIsoInfo gffGeneIsoInfoRef : lsGffGeneIsoInfos) {
			int[] compareInfo = GffGeneIsoInfo.compareIso(gffGeneIsoInfoRef, gffGeneIsoInfo);
			mapCompInfo2GeneIso.put(compareInfo, gffGeneIsoInfoRef);
			lsCompInfo.add(compareInfo);
		}
		//排序，挑选出最相似的转录本
		Collections.sort(lsCompInfo, new Comparator<int[]>() {
			public int compare(int[] o1, int[] o2) {
				Double int1 = (double)o1[0]/o1[1];
				Double int2 = (double)o2[0]/o2[1];
				return -int1.compareTo(int2);
			}
		});
		int[] compareInfo = lsCompInfo.get(0);
		double ratio = (double)compareInfo[0]/Math.min(compareInfo[2], compareInfo[3]);
		if (ratio < likelyhood) {
			return null;
		}
		return mapCompInfo2GeneIso.get(lsCompInfo.get(0));
		
	}

	public String getGTFformate(String title) {
		String geneGTF = "";
		for (GffGeneIsoInfo gffGeneIsoInfo : getLsCodSplit()) {
			gffGeneIsoInfo.sort();
			geneGTF = geneGTF + gffGeneIsoInfo.getGTFformat(getNameSingle(), title);
		}
		return geneGTF;
	}
	/**
	 * 返回gff格式的信息
	 * @param title 公司名等信息
	 * @return
	 */
	public String getGFFformate(String title) {
		if (title == null || title.trim().equals("")) {
			title = NovelBioConst.COMPANY_NAME_ABBR;
		}
		String strand = "+";
		if (!isCis5to3()) {
			strand = "-";
		}
		String geneGFF = getParentName() + "\t" +title + "\tgene\t" + getStartAbs()+ "\t" + getEndAbs()
        + "\t"+"."+"\t" +strand+"\t.\t"+ "ID=" + getNameSingle()
        +";Name=" + getNameSingle() + ";Name=" + getNameSingle() + " " + TxtReadandWrite.ENTER_LINUX;
		for (GffGeneIsoInfo gffGeneIsoInfo : getLsCodSplit()) {
			String strandmRNA = "+";
			if (!gffGeneIsoInfo.isCis5to3()) {
				strandmRNA = "-";
			}
			geneGFF = geneGFF + getParentName() + "\t" +title + "\tmRNA\t" +gffGeneIsoInfo.getStartAbs()+ "\t" + gffGeneIsoInfo.getEndAbs()
	        + "\t"+"."+"\t" +strandmRNA+"\t.\t"+ "ID=" + gffGeneIsoInfo.getName() 
	        +";Name="+gffGeneIsoInfo.getName()+ ";Parent="+ getNameSingle() + " " + TxtReadandWrite.ENTER_LINUX;
			gffGeneIsoInfo.sort();
			geneGFF = geneGFF + gffGeneIsoInfo.getGFFformat(getNameSingle(), title);
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
			return super.getCod2Start(coord);
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
		result.lsGffGeneIsoInfos = (ArrayList<GffGeneIsoInfo>) lsGffGeneIsoInfos.clone();
		return result;
	}
}
