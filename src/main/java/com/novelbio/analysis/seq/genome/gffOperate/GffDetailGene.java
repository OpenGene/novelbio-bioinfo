package com.novelbio.analysis.seq.genome.gffOperate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.Id;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.genome.ExceptionNbcGFF;
import com.novelbio.base.SepSign;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.modgeneid.GeneType;
import com.novelbio.generalConf.TitleFormatNBC;
import com.novelbio.listOperate.ListCodAbs;
import com.novelbio.listOperate.ListDetailAbs;
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
@Document(collection="gffgene")
@CompoundIndexes({
	@CompoundIndex(unique = false, name = "fileid_chr_start_end_idx", def = "{'gffFileId': 1, 'parentName': 1, 'numberstart': 1, 'numberend': 1}"),
    @CompoundIndex(unique = false, name = "fileid_chr_start_end_idx", def = "{'gffFileId': 1 , 'parentName': 1, 'numberstart': 1, 'numberend': 1}")
})
public class GffDetailGene extends ListDetailAbs {
	private final static Logger logger = LoggerFactory.getLogger(GffDetailGene.class);
	/** 两个转录本的overlap 覆盖 必须大于0.6才算是一个基因 */
	public final static double OVERLAP_RATIO = 0.6;
	
	@Id
	String id;
	
	/** 顺序存储每个转录本的的坐标情况 */
	@DBRef
	ArrayList<GffGeneIsoInfo> lsGffGeneIsoInfos = new ArrayList<GffGeneIsoInfo>();//存储可变剪接的mRNA
	
	@Indexed(unique = false)
	int taxID = 0;
	@Indexed(unique = false)
	String gffFileId;
	@Transient
	boolean removeDuplicateIso = false;
	
	/** 仅保存数据库使用 */
	Set<String> setNameLowcase;
		
	public GffDetailGene() {}
	/**
	 * @param listGff
	 * @param locString 没名字就写null
	 * @param cis5to3
	 */
	public GffDetailGene(ListGff listGff, String locString, boolean cis5to3) {
		super(listGff, locString, cis5to3);
	}
	/**
	 * @param chrID 内部小写
	 * @param locString 没名字就写null
	 * @param cis5to3
	 */
	public GffDetailGene(String chrID, String locString, boolean cis5to3) {
		super(chrID, locString, cis5to3);
	}

	/** 仅供数据库使用 */
	public void setGffFileId(String gffFileId) {
		this.gffFileId = gffFileId;
	}
	/** 仅供数据库使用 */
	public String getGffFileId() {
		return gffFileId;
	}
	/** 仅供数据库使用 */
	public void setId(String id) {
		this.id = id;
	}
	/** 仅供数据库使用 */
	public String getId() {
		return id;
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
	 */
	public ArrayList<GffGeneIsoInfo> getLsCodSplit() {
		return lsGffGeneIsoInfos;
	}
	
	/**
	 * 如果本GffDetailGene中包含两个以上的GffDetailGene，譬如两个parentName那种<br>
	 * 则用这个返回。<br>
	 * 仅用于GFF3的结果，如NCBI的等<br>
	 */
	public List<GffDetailGene> getlsGffDetailGenes() {
		Map<String, GffDetailGene> mapName2Gene = new LinkedHashMap<>();
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
			String parentName = gffGeneIsoInfo.getParentGeneName();
			GffDetailGene gffDetailGene = mapName2Gene.get(parentName);
			if (gffDetailGene == null) {
				gffDetailGene = getGffDetailGeneClone();
				gffDetailGene.setItemName.add(parentName);
				mapName2Gene.put(parentName, gffDetailGene);
			}
			gffDetailGene.addIsoSimple(gffGeneIsoInfo);
		}
		return new ArrayList<>(mapName2Gene.values());
	}
	
	/** 返回一个和现在GffDetailGene一样的GffDetailGene */
	private GffDetailGene getGffDetailGeneClone() {
		GffDetailGene gffDetailGene = this.clone();
		gffDetailGene.setItemName.clear();
		gffDetailGene.setStartAbs(-1);
		gffDetailGene.setEndAbs(-1);
		gffDetailGene.setCis5to3(null);
		gffDetailGene.lsGffGeneIsoInfos = new ArrayList<>();
		return gffDetailGene;
	}
	
	public GffGeneIsoInfo getIsoByName(String isoName) {
		for (GffGeneIsoInfo iso : lsGffGeneIsoInfos) {
			if (iso.getName().equalsIgnoreCase(isoName)) {
				return iso;
			}
		}
		return null;
	}
	
	public GffGeneIsoInfo pollIsoByName(String isoName) {
		GffGeneIsoInfo isoResult = null;
		ArrayList<GffGeneIsoInfo> lsIso = new ArrayList<>();
		int num = 0;
		for (GffGeneIsoInfo iso : lsGffGeneIsoInfos) {
			if (iso.getName().equalsIgnoreCase(isoName)) {
				if (num++>0) {
					throw new ExceptionNbcGFF("gene " + getNameSingle() + " cannot have two iso with same name " + isoName);
				}
				
				isoResult = iso;
				continue;
			}
			lsIso.add(iso);
		}
		lsGffGeneIsoInfos = lsIso;
		return isoResult;
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
			if (gffGeneIsoInfo.getName().equalsIgnoreCase(isoName)) {
				return i;
			}
		}
		
		for (int i = 0; i < lsGffGeneIsoInfos.size(); i++) {
			GffGeneIsoInfo gffGeneIsoInfo = lsGffGeneIsoInfos.get(i);
				if(gffGeneIsoInfo.getParentGeneName().equalsIgnoreCase(isoName)) {
					return i;
			}
		}
		
		return -1;
	}
	/** 全体item的名字 */
	public ArrayList<String > getName() {
		HashSet<String> setIsoName = new LinkedHashSet<String>();
		for (String string : this.setItemName) {
			setIsoName.add(string);
		}
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
			setIsoName.add(gffGeneIsoInfo.getName());
		}

		return ArrayOperate.getArrayListValue(setIsoName);
	}


	/** 根据mRNA的值重新设定起点和终点，因为NCBI的gff可能会出现起点终点与该起点终点不一致的情况 */
	protected void resetStartEnd() {
		if (lsGffGeneIsoInfos.isEmpty()) {
			return;
		}
		int startAbs = Integer.MAX_VALUE, endAbs = Integer.MIN_VALUE;
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
			if (startAbs > gffGeneIsoInfo.getStartAbs()) {
				startAbs = gffGeneIsoInfo.getStartAbs();
			}
			if (endAbs < gffGeneIsoInfo.getEndAbs()) {
				endAbs = gffGeneIsoInfo.getEndAbs();
			}
		}
		setStartAbs(startAbs);
		setEndAbs(endAbs);
		
	}
	/**
	 * 给最后一个转录本添加exon坐标，<br>
	 * 只需要注意按照次序装，也就是说如果正向要从小到大的加，反向从大到小的加
	 * 然而具体加入这一对坐标的时候，并不需要分别大小，程序会根据gene方向自动判定
	 * <b>如果发现一个没有转录本的，则新添加一个gene设置类型为pseudo</b>
	 * @param cis5to3 exon的方向
	 */
	protected void addExon(Boolean cis5to3, int locStart,int locEnd) {
		if (lsGffGeneIsoInfos.size() == 0) {//如果发现一个没有转录本的，则新添加一个gene设置类型为pseudo
			addsplitlist(getNameSingle(), getNameSingle(), GeneType.pseudogene);
		}
		GffGeneIsoInfo gffGeneIsoInfo = lsGffGeneIsoInfos.get(lsGffGeneIsoInfos.size()-1);//include one special loc start number to end number
		gffGeneIsoInfo.addExon(cis5to3, locStart, locEnd);
	}
	/**
	 * 添加exon坐标，不考虑排序的问题<br>
	 * <b>如果发现一个没有转录本的，则新添加一个gene设置类型为pseudo</b>
	 */
	protected void addExonNorm(Boolean cis5to3, int locStart,int locEnd) {
		if (lsGffGeneIsoInfos.size() == 0) {//如果发现一个没有转录本的，则新添加一个gene设置类型为pseudo
			addsplitlist(getNameSingle(), getNameSingle(), GeneType.pseudogene);
		}
		GffGeneIsoInfo gffGeneIsoInfo = lsGffGeneIsoInfos.get(lsGffGeneIsoInfos.size()-1);//include one special loc start number to end number
		gffGeneIsoInfo.addExonNorm(cis5to3, locStart, locEnd);
	}
	
	/**
	 * 给最后一个转录本添加ATG和UAG坐标，<br>
	 * 加入这一对坐标的时候，并不需要分别大小，程序会根据gene方向自动判定
	 * 会自动判定输入的起点是否小于已有的atg，终点是否大于已有的uag
	 * 是的话，才会设定，否则就不设定
	 */
	protected void setATGUAG(int atg, int uag) {
		GffGeneIsoInfo gffGeneIsoInfo = lsGffGeneIsoInfos.get(lsGffGeneIsoInfos.size()-1);//include one special loc start number to end number
		gffGeneIsoInfo.setATGUAGauto(atg, uag);
	}
	/** 如果是非编码RNA，则将atg和uag设置为最后一位 */
	protected void setATGUAGncRNA() {
		GffGeneIsoInfo gffGeneIsoInfo = lsGffGeneIsoInfos.get(lsGffGeneIsoInfos.size()-1);//include one special loc start number to end number
		gffGeneIsoInfo.sort();
		gffGeneIsoInfo.setATGUAGncRNA();
	}
	/**
	 * 合并最近添加的一个gffIso<br>
	 * 如果输入的是GffPlant的类型，
	 * 那么可能UTR和CDS会错位。这时候就需要先将exon排序，然后合并两个中间只差一位的exon
	 */
	protected void combineExon() {
		GffGeneIsoInfo gffGeneIsoInfo = lsGffGeneIsoInfos.get(lsGffGeneIsoInfos.size()-1);//include one special loc start number to end number
		gffGeneIsoInfo.combineExon();
	}
	/**
	 * 直接添加转录本，根据genedetail的信息设置cis5to3。之后用addcds()方法给该转录本添加exon
	 * @return 返回添加的转录本 
	 */
	protected GffGeneIsoInfo addsplitlist(String splitName, String geneParentName, GeneType geneTpye) {
		removeDuplicateIso = false;
		
		GffGeneIsoInfo gffGeneIsoInfo = GffGeneIsoInfo.createGffGeneIso(splitName, geneParentName, this, geneTpye, cis5to3);
		lsGffGeneIsoInfos.add(gffGeneIsoInfo);
		return gffGeneIsoInfo;
	}
	/**
	 * 直接添加转录本，之后用addcds()方法给该转录本添加exon
	 * @return 
	 */
	protected GffGeneIsoInfo addsplitlist(String splitName, String geneParentName, GeneType geneTpye, boolean cis5to3) {
		removeDuplicateIso = false;
		
		GffGeneIsoInfo gffGeneIsoInfo = GffGeneIsoInfo.createGffGeneIso(splitName, geneParentName, this, geneTpye, cis5to3);
		lsGffGeneIsoInfos.add(gffGeneIsoInfo);
		return gffGeneIsoInfo;
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
			return getLongestSplitMrna().isCis5to3();
		}
		return this.cis5to3;
	}
	/**
	 * 转录方向
	 * 如果为null，说明本基因中的iso方向有不相同的
	 */
	public Boolean isCis5to3Real() {
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
	/** 获得该基因中最长的一条转录本的信息
	 * 如果本位点同时存在lnc和mRNA，优先返回mRNA
	 *  否则也能返回lnc
	 */
	public GffGeneIsoInfo getLongestSplitMrna() {
		int id = getLongestSplitIDMrna();
		return lsGffGeneIsoInfos.get(id);
	}
	
	/**
	 * <b>不能返回ATG和UAG位点</b><br>
	 *  获得coord在该基因内部的最长的一条转录本的信息
	 * @param coord 坐标位置
	 * @param geneStructure 基因结构，意思优先返回指定的基因结构，如果为CDS，UTR这种，不在CDS和UTR内部的优先返回在exon上，还不在就返回在Intron上的
	 * 如果基因结构指定了Intron则直接返回Intron，如果不在Intron上，就返回最长的在exon上的。
	 * @return
	 */
	public GffGeneIsoInfo getLongestmRNAIso(int coord, GeneStructure geneStructure) {
		return getLongestmRNAIso(coord, geneStructure, null, null);
	}
	
	/**
	 *  获得coord在该基因内部的最长的一条转录本的信息
	 * @param coord 坐标位置
	 * @param geneStructure 基因结构，意思优先返回指定的基因结构，如果为CDS，UTR这种，不在CDS和UTR内部的优先返回在exon上，还不在就返回在Intron上的
	 * 如果基因结构指定了Intron则直接返回Intron，如果不在Intron上，就返回最长的在exon上的。
	 * @return
	 */
	public GffGeneIsoInfo getLongestmRNAIso(int coord, GeneStructure geneStructure, int[] tssRange, int[] tesRange) {
    	if (lsGffGeneIsoInfos.size() == 1) {
			return lsGffGeneIsoInfos.get(0);
		}
    	ArrayListMultimap<GeneStructure, GffGeneIsoInfo> mapGeneStruct2LsIso = ArrayListMultimap.create();    	
    	//获得位点情况
    	for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
			Set<GeneStructure> setGeneStructures = gffGeneIsoInfo.getLsCoordOnGeneStructure(coord, tssRange, tesRange);
			for (GeneStructure geneStructureThis : setGeneStructures) {
				mapGeneStruct2LsIso.put(geneStructureThis, gffGeneIsoInfo);
			}
		}
		
		if (mapGeneStruct2LsIso.containsKey(geneStructure)) {
			List<GffGeneIsoInfo> lsIsos = mapGeneStruct2LsIso.get(geneStructure);
			Collections.sort(lsIsos, new IsoCompareM2S());
			return lsIsos.get(0);
		} else if (geneStructure == GeneStructure.INTRON || geneStructure == GeneStructure.ATG
				|| geneStructure == GeneStructure.UAG || geneStructure == GeneStructure.CDS 
				|| geneStructure == GeneStructure.UTR3 || geneStructure == GeneStructure.UTR5) {
			List<GffGeneIsoInfo> lsIsos = mapGeneStruct2LsIso.get(GeneStructure.EXON);
			if (lsIsos.isEmpty()) {
				lsIsos = mapGeneStruct2LsIso.get(GeneStructure.INTRON);
			}
			if (lsIsos.isEmpty()) {
				return getLongestSplitMrna();
			}
			
			Collections.sort(lsIsos, new IsoCompareM2S());
			return lsIsos.get(0);
		}
		return getLongestSplitMrna();
	}
	
	private static class IsoCompareM2S implements Comparator<GffGeneIsoInfo> {
		@Override
		public int compare(GffGeneIsoInfo o1, GffGeneIsoInfo o2) {
			int compare = Boolean.valueOf(o1.ismRNA()).compareTo(o2.ismRNA());
			if (compare == 0) {
				compare = -Integer.valueOf(o1.getLen()).compareTo(o2.getLen());
			}
			return compare;
		}
	}
	
    private int getLongestSplitIDMrna() {
    	if (lsGffGeneIsoInfos.size() == 1) {
			return 0;
		}
		ArrayList<Integer> lslength = new ArrayList<Integer>();
		//判定是否为mRNA
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
			
			if (gffGeneIsoInfo.size() == 0 || (isMRNA() && !gffGeneIsoInfo.ismRNA()) ) {
				lslength.add(0);
			} else {
				lslength.add(gffGeneIsoInfo.getLen());
			}
		}
		if (lslength.size() == 0) {
			logger.error("没有长度的iso");
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
    /** 本基因是否编码蛋白 */
    public boolean isMRNA() {
		//判定是否为mRNA
    	boolean ismRNA = false;
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
			if (gffGeneIsoInfo.ismRNA()) {
				ismRNA = true;
				break;
			}
		}
		return ismRNA;
    }
    
    /**
     * 本基因是否为mRNA
     * 直接看其中的iso的geneType是否为mRNA
     * 有些基因譬如ATP6，没有标注为mRNA但是有CDS，这里就不会被识别为mRNA
     * @return
     */
    public boolean isMRNAgeneType() {
		//判定是否为mRNA
    	boolean ismRNA = false;
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
			if (gffGeneIsoInfo.flagTypeGene == GeneType.mRNA) {
				ismRNA = true;
				break;
			}
		}
		return ismRNA;
    }
    
    /** 遍历该基因内部全体转录本，返回该基因是什么类型
     * 如果既有mRNA又有别的类型，优先mRNA
     * 优先级排序为 mRNA ncRNA miRNA
     * miRNA优先级最后
     *  */
    public GeneType getGeneType() {
    	GeneType geneType = null;
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
			GeneType geneTypeTmp = gffGeneIsoInfo.getGeneType();
			if (geneTypeTmp == GeneType.mRNA) {
				geneType = GeneType.mRNA;
				break;
			} else if (geneTypeTmp == GeneType.miRNA && geneType != null && geneType != GeneType.miRNA) {
				continue;
			} else {
				geneType = geneTypeTmp;
			}
		}
		return geneType;
    }
    /**
     * 获得该基因中最长的一条转录本的部分区域的信息。已经考虑过开闭区间问题
     * @param type 指定为INTRON,EXON,UTR5,UTR3
     * @param num 如果type为"Intron"或"Exon"，指定第几个，如果超出，则返回0
     * num 为实际个数。
     * 如果5UTR直接返回全长5UTR
     * 3UTR也直接返回全长3UTR
     * @return 
     */
	public int getTypeLength(GeneStructure geneStructure,int num) {
		GffGeneIsoInfo gffGeneIsoInfo = getLongestSplitMrna();
		if (num > gffGeneIsoInfo.size()) {
			return 0;
		}
		if (geneStructure.equals(GeneStructure.INTRON)) {
			return gffGeneIsoInfo.getLenIntron(num);
		}
		if (geneStructure.equals(GeneStructure.EXON)) {
			return gffGeneIsoInfo.getLenExon(num);
		}
		if (geneStructure.equals(GeneStructure.UTR5)) {
			return gffGeneIsoInfo.getLenUTR5();
		}
		if (geneStructure.equals(GeneStructure.UTR3)) {
			return gffGeneIsoInfo.getLenUTR3();
		}
		return -1000000;
	}
	public void clearIso() {
		setItemName.clear();
		cis5to3 = null;
		numberstart = ListCodAbs.LOC_ORIGINAL; // loc start number
		numberend = ListCodAbs.LOC_ORIGINAL; //loc end number
		tss2UpGene = ListCodAbs.LOC_ORIGINAL;
		tes2DownGene = ListCodAbs.LOC_ORIGINAL;
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
	public String[] getInfo(int[] tss, int[] geneEnd, int coord) {
		String[] anno = new String[4];
		for (int i = 0; i < anno.length; i++) {
			anno[i] = "";
		}
		HashSet<GeneID> hashCopedID = new HashSet<GeneID>();
		if (isCodInGeneExtend(tss, geneEnd, coord)) {
			for (GffGeneIsoInfo gffGeneIsoInfo : getLsCodSplit()) {
				if (gffGeneIsoInfo.isCodInIsoExtend(tss, geneEnd, coord)) {
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
			if (getLongestSplitMrna().isCodInIsoExtend(tss, geneEnd, coord)) {
				anno[4] = getLongestSplitMrna().toStringCodLocStr(tss, coord);
			}
			else {
				for (GffGeneIsoInfo gffGeneIsoInfo : getLsCodSplit()) {
					if (gffGeneIsoInfo.isCodInIsoExtend(tss, geneEnd, coord)) {
						anno[4] = gffGeneIsoInfo.toStringCodLocStr(tss, coord);
						break;
					}
				}
			}
		}
		return anno;
	}

	/**
	 * 去除重复Isoform
	 * 如果里面含有以"tcons"开头的基因，会被替换掉，为的就是防止cufflinks的iso替换ref的iso
	 */
	public void removeDupliIso() {
		if (removeDuplicateIso) {
			return;
		}

		removeDuplicateIso = true;
		HashMap<String, GffGeneIsoInfo> mapIso = new HashMap<String, GffGeneIsoInfo>();
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
			String key = getRefID() + gffGeneIsoInfo.isCis5to3();
			for (ExonInfo exonInfo : gffGeneIsoInfo) {
				key = key + SepSign.SEP_INFO + exonInfo.getStartAbs() + SepSign.SEP_ID + exonInfo.getEndAbs();
			}
			if (mapIso.containsKey(key)) {
				GffGeneIsoInfo gffGeneIsoInfoOld = mapIso.get(key);
				if (gffGeneIsoInfoOld.getName().toLowerCase().startsWith("tcons") && !gffGeneIsoInfo.getName().toLowerCase().startsWith("tcons")) {
					gffGeneIsoInfoOld.setName(gffGeneIsoInfo.getName());
				}
				if (gffGeneIsoInfoOld.getATGsite() < 0 && gffGeneIsoInfo.getATGsite() > 0) {
					gffGeneIsoInfoOld.setATGUAGauto(gffGeneIsoInfo.getATGsite(), gffGeneIsoInfo.getUAGsite());
				}
			} else {
				mapIso.put(key, gffGeneIsoInfo);
			}
		}
		this.lsGffGeneIsoInfos = new ArrayList<GffGeneIsoInfo>(mapIso.values());
	}
	/**
	 * 将gffDetailGene中含有新的名字的iso添加入本类
	 * 没有删除重复的iso
	 * @param gffDetailGene
	 */
	public void addIsoSimple(GffDetailGene gffDetailGene) {
		setItemName.addAll(gffDetailGene.getName());
		for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
			addIso(gffGeneIsoInfo);
		}
	}
	/**
	 * 效率低下，等待优化
	 * 添加新的转录本
	 * 没有删除重复的iso
	 * 同时重新设定该基因的numberstart和numberend
	 * @param gffGeneIsoInfo 输入的iso必须不能为null，并且要有exon信息的存在
	 */
	public void addIso(GffGeneIsoInfo gffGeneIsoInfo) {
		if (gffGeneIsoInfo == null || gffGeneIsoInfo.size() == 0) {
			return;
		}
		
		gffGeneIsoInfo.setGffDetailGeneParent(this);
		removeDuplicateIso = false;
		if (cis5to3 != null && gffGeneIsoInfo.isCis5to3() != cis5to3) {
			cis5to3 = null;
		}
		
		for (GffGeneIsoInfo gffGeneIsoInfoOld : lsGffGeneIsoInfos) {
			if (gffGeneIsoInfoOld.equalsIso(gffGeneIsoInfo) && gffGeneIsoInfoOld.getName().equals(gffGeneIsoInfo.getName())) {//比较两个list是否一致，exon的equals只比较起点终点
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
	 * 添加新的转录本，不设定removeDuplicateIso和cis5to3
	 * 不考虑重复iso，不修改同名iso
	 * 同时重新设定该基因的numberstart和numberend
	 * @param gffGeneIsoInfo 输入的iso必须不能为null，并且要有exon信息的存在
	 */
	public void addIsoSimple(GffGeneIsoInfo gffGeneIsoInfo) {
		if (gffGeneIsoInfo == null || gffGeneIsoInfo.size() == 0) {
			return;
		}
		if (cis5to3 == null) {
			if (lsGffGeneIsoInfos.isEmpty()) {
				cis5to3 = gffGeneIsoInfo.isCis5to3();
			}
		} else if (cis5to3 != gffGeneIsoInfo.isCis5to3()) {
			cis5to3 = null;
		}
		
		//修改名字
		String IsoName = gffGeneIsoInfo.getName();
		int i = 1;
		while (isContainsIso(IsoName)) {
			IsoName = FileOperate.changeFileSuffix(IsoName, "", ""+i).replace("/", "");
			i++;
		}
		gffGeneIsoInfo.setName(IsoName);
		gffGeneIsoInfo.setGffDetailGeneParent(this);
		lsGffGeneIsoInfos.add(gffGeneIsoInfo);
		
		if (numberstart < 0 || numberstart > gffGeneIsoInfo.getStartAbs()) {
			numberstart = gffGeneIsoInfo.getStartAbs();
		}
		if (numberend < 0 || numberend < gffGeneIsoInfo.getEndAbs()) {
			numberend = gffGeneIsoInfo.getEndAbs();
		}
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
	
	/**
	 * 给定一个转录本，返回与之最接近的转录本，相似度必须在指定范围内
	 * 所谓最接近，就是除了首位边界可以不同，其他边界必须相同
	 * @param gffGeneIsoInfo
	 * @param likelyhood 相似度必须高于该值
	 * @return 没有则返回null
	 */
	public GffGeneIsoInfo getAlmostSameIso(GffGeneIsoInfo gffGeneIsoInfo) {
		HashMap<int[], GffGeneIsoInfo> mapCompInfo2GeneIso = new HashMap<int[], GffGeneIsoInfo>();
		ArrayList<int[]> lsCompInfo = new ArrayList<int[]>();
		for (GffGeneIsoInfo gffGeneIsoInfoRef : lsGffGeneIsoInfos) {
			if (GffGeneIsoInfo.isExonEdgeSame_NotConsiderBound(gffGeneIsoInfoRef, gffGeneIsoInfo)) {
				int[] compareInfo = GffGeneIsoInfo.compareIso(gffGeneIsoInfoRef, gffGeneIsoInfo);
				mapCompInfo2GeneIso.put(compareInfo, gffGeneIsoInfoRef);
				lsCompInfo.add(compareInfo);
			}
		}
		if (lsCompInfo.size() == 0) {
			return null;
		} else if (lsCompInfo.size() == 1) {
			return mapCompInfo2GeneIso.get(lsCompInfo.get(0));
		}
		
		//排序，挑选出最相似的转录本
		Collections.sort(lsCompInfo, new Comparator<int[]>() {
			public int compare(int[] o1, int[] o2) {
				Double int1 = (double)o1[0]/o1[1];
				Double int2 = (double)o2[0]/o2[1];
				return -int1.compareTo(int2);
			}
		});
		return mapCompInfo2GeneIso.get(lsCompInfo.get(0));
	}
	
	/**
	 * 给定一个转录本，返回与之最接近的转录本，相似度必须在指定范围内
	 * 所谓最接近，就是除了首位边界可以不同，其他边界必须相同
	 * @param gffGeneIsoInfo
	 * @param likelyhood 相似度必须高于该值
	 * @return 没有则返回null
	 */
	public GffGeneIsoInfo getMostSameIso(GffGeneIsoInfo gffGeneIsoInfo) {
		HashMap<int[], GffGeneIsoInfo> mapCompInfo2GeneIso = new HashMap<int[], GffGeneIsoInfo>();
		ArrayList<int[]> lsCompInfo = new ArrayList<int[]>();
		for (GffGeneIsoInfo gffGeneIsoInfoRef : lsGffGeneIsoInfos) {
			int[] compareInfo = GffGeneIsoInfo.compareIso(gffGeneIsoInfoRef, gffGeneIsoInfo);
			mapCompInfo2GeneIso.put(compareInfo, gffGeneIsoInfoRef);
			lsCompInfo.add(compareInfo);
		}
		if (lsCompInfo.size() == 0) {
			return null;
		} else if (lsCompInfo.size() == 1) {
			return mapCompInfo2GeneIso.get(lsCompInfo.get(0));
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
		double value = (double)compareInfo[0]/compareInfo[1];
		if (value < 0.6) {
			return null;
		}
		return mapCompInfo2GeneIso.get(lsCompInfo.get(0));
	}
	
	/**
	 * 将本基因输出为bed格式
	 * @param chrID 染色体名，主要是为了大小写问题，null表示走默认
	 * @param title
	 * @return
	 */
	public String toBedFormate(String chrID, String title) {
		String bed = "";
		int i = 0;
		for (GffGeneIsoInfo gffGeneIsoInfo : getLsCodSplit()) {
			gffGeneIsoInfo.sort();
			if (i++ == 0) {
				bed = gffGeneIsoInfo.getBedFormat(chrID, title);
			} else {
				bed = bed + TxtReadandWrite.ENTER_LINUX + gffGeneIsoInfo.getBedFormat(chrID, title);
			}			
		}
		return bed;
	}

	/**
	 * 将本基因输出为gtf文件，就这个基因的几行
	 * @param chrID 染色体名，主要是为了大小写问题，null表示走默认
	 * @param title
	 * @return
	 */
	public String toGTFformate(String chrID, String title) {
		StringBuilder geneGTF = new StringBuilder();
		for (GffGeneIsoInfo gffGeneIsoInfo : getLsCodSplit()) {
			gffGeneIsoInfo.sort();
			geneGTF.append(gffGeneIsoInfo.getGTFformat(chrID, title));
		}
		return geneGTF.toString();
	}
	
	public List<String> toGFFformate(String title) {
		return toGFFformate(null, title);
	}
	//TODO 待修正
	/**
	 * 返回gff格式的信息
	 * @param title 公司名等信息
	 * @return
	 */
	public List<String> toGFFformate(String chrId, String title) {
		if (chrId == null) chrId = getRefID();
		
		List<String> lsResult = new ArrayList<>();
		if (title == null || title.trim().equals("")) {
			title = TitleFormatNBC.CompanyNameAbbr.toString();
		}
		String strand = "+";
		if (!isCis5to3()) {
			strand = "-";
		}
		
		List<String> lsGene = new ArrayList<>();
		lsGene.add(chrId); lsGene.add(title); lsGene.add("gene"); lsGene.add(getStartAbs() + ""); lsGene.add(getEndAbs() + "");
		lsGene.add(".");  lsGene.add(strand); lsGene.add("."); lsGene.add("ID=" + getNameSingle() + ";" + "Name=" + getNameSingle());
		String geneGFF = ArrayOperate.cmbString(lsGene.toArray(new String[0]), "\t");
		lsResult.add(geneGFF);
		//TODO 这里的getLsCodSplit 以后要改成获得不同的分组，这样可以将相同来源的iso放在一组
		for (GffGeneIsoInfo gffGeneIsoInfo : getLsCodSplit()) {
			String strandmRNA = "+";
			if (!gffGeneIsoInfo.isCis5to3()) {
				strandmRNA = "-";
			}
			List<String> lsmRNA = new ArrayList<>();
			lsmRNA.add(chrId); lsmRNA.add(title); lsmRNA.add(gffGeneIsoInfo.getGeneType().toString());
			lsmRNA.add(gffGeneIsoInfo.getStartAbs() + ""); lsmRNA.add(gffGeneIsoInfo.getEndAbs() + "");
			lsmRNA.add("."); lsmRNA.add(strandmRNA); lsmRNA.add(".");
			lsmRNA.add("ID=" + gffGeneIsoInfo.getName() + ";Name="+gffGeneIsoInfo.getName()+ ";Parent="+ gffGeneIsoInfo.getParentGeneName());
			lsResult.add(ArrayOperate.cmbString(lsmRNA.toArray(new String[0]), "\t"));			
			gffGeneIsoInfo.sort();
			lsResult.add(gffGeneIsoInfo.getGFFformat(title));
		}
		return lsResult;
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
		return getLongestSplitMrna().getCod2Tes(coord);
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
		return getLongestSplitMrna().getCod2Tss(coord);
	}
	/**
	 * 判断是否存在该名字的转录本
	 * @param IsoName
	 */
	private boolean isContainsIso(String IsoName) {
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
			if (gffGeneIsoInfo.getName().toLowerCase().equals(IsoName.toLowerCase())) {
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
	/**
	 * 深度clone，lsGffGeneIsoInfos中的iso也被克隆了。
	 * 但是每个iso没有被clone
	 */
	public GffDetailGene cloneDeep() {
		GffDetailGene result = null;
		result = (GffDetailGene) super.clone();
		result.taxID = taxID;
		result.lsGffGeneIsoInfos = new ArrayList<GffGeneIsoInfo>();
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
			GffGeneIsoInfo gffGeneIsoInfo2 = gffGeneIsoInfo.clone();
			gffGeneIsoInfo2.gffDetailGeneParent = result;
			result.lsGffGeneIsoInfos.add(gffGeneIsoInfo2);
		}
		return result;
	}
	public static enum GeneStructure {
		ALLLENGTH("AllLength"),
		INTRON("Intron"), CDS("CDS"), EXON("Exon"), UTR5("5-UTR"), UTR3("3-UTR"), 
		TSS("Tss"), TES("Tes"), ATG("Atg"), UAG("Uag");
		String name;
		private GeneStructure(String name) {
			this.name = name;
		}
		public String toString() {
			return name;
		}
		/**
		 * 返回文字对应的GeneStructure
		 * @return
		 */
		public static HashMap<String, GeneStructure> getMapInfo2GeneStr() {
			HashMap<String, GeneStructure> mapStr2GeneStructure = new HashMap<String, GffDetailGene.GeneStructure>();
			mapStr2GeneStructure.put("Full Length", ALLLENGTH);
			mapStr2GeneStructure.put("Intron", INTRON);
			mapStr2GeneStructure.put("CDS", CDS);
			mapStr2GeneStructure.put("Exon", EXON);
			mapStr2GeneStructure.put("5-UTR", UTR5);
			mapStr2GeneStructure.put("3-UTR", UTR3);
			mapStr2GeneStructure.put("Tss", TSS);
			mapStr2GeneStructure.put("Tes", TES);
			mapStr2GeneStructure.put("Atg", ATG);
			mapStr2GeneStructure.put("Uag", UAG);
			return mapStr2GeneStructure;
		}
		/**
		 * 返回GeneStructure对应的文字
		 * @return
		 */
		public static HashMap<GeneStructure, String> getMapGene2Str() {
			HashMap<GeneStructure, String> mapGeneStructure2Str = new HashMap<GffDetailGene.GeneStructure, String>();
			HashMap<String, GeneStructure> mapStr2GeneStructure = getMapInfo2GeneStr();
			for (Entry<String, GeneStructure> entry : mapStr2GeneStructure.entrySet()) {
				mapGeneStructure2Str.put(entry.getValue(), entry.getKey());
			}
			return mapGeneStructure2Str;
		}
	}
	
	/** 仅数据库使用 */
	public void setNameLowcase() {
		setNameLowcase = new HashSet<>();
		for (String string : this.setItemName) {
			setNameLowcase.add(string.toLowerCase());
		}
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
			setNameLowcase.add(gffGeneIsoInfo.getName().toLowerCase());
			setNameLowcase.add(GeneID.removeDot(gffGeneIsoInfo.getName().toLowerCase()));
			GeneID geneID = new GeneID(gffGeneIsoInfo.getName(), taxID);
			setNameLowcase.add(geneID.getGeneUniID().toLowerCase());
		}
	}
}
