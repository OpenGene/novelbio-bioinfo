package com.novelbio.bioinfo.gff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.novelbio.bioinfo.base.Alignment;
import com.novelbio.bioinfo.gffchr.GffChrStatistics;
import com.novelbio.database.domain.modgeneid.GeneID;
/**
 * 将GffHash和GffHashGene的方法全部列在了这里
 * @author zong0jie
 *
 */
public interface GffHashGeneInf  {
	/**
	 * 返回List顺序存储每个基因号或条目号，这个打算用于提取随机基因号。
	 * 不能通过该方法获得某个LOC在基因上的定位
	 */
	public ArrayList<String> getLsNameNoRedundent();
	/**
	 * 返回读取的Gff文件名
	 * @return
	 */
	public String getGffFilename();
	
	/**
	 * 给定某个LOCID，返回该LOC在某条染色体中的位置序号号，第几位<br>
	 * 也就是Chrhash中某个chr下该LOC的位置<br>
	 * 该位置必须大于等于0，否则就是出错<br>
	 * 该比较是首先用单个LOCID从locHashtable获得其GffDetail类，然后用ChrID在Chrhash中获得某条染色体的gffdetail的List，然后比较他们的locString以及基因的起点和终点
	 * 仅仅将GffDetail的equal方法重写。
	 * @param LOCID 输入某基因编号
	 * @return string[2]<br>
	 * 0: 染色体编号，chr1,chr2等，都为小写<br>
	 * 1:该染色体上该LOC的序号，如1467等
	 */
	public String[] getLOCNum(String LOCID);
	
	/**
	 * 给定LOCID，返回所对应的转录本
	 * 没有就返回最长转录本
	 * @param LOCID
	 * @return
	 */
	GffIso searchISO(String LOCID);
	
	public GffCodGene searchLocation(String chrID, int Coordinate);

	public GffGene searchLOC(String LOCID);

	public GffGene searchLOC(String chrID, int LOCNum);
	/**
	 * 内部自动判断 cod1 和 cod2的大小
	 * @param chrID
	 * @param cod1
	 * @param cod2
	 * @return
	 */
	public GffCodGeneDU searchLocation(String chrID, int cod1, int cod2);
	
	/**
	 * 内部自动判断 cod1 和 cod2的大小
	 * @param chrID
	 * @param cod1
	 * @param cod2
	 * @return
	 */
	public GffCodGeneDU searchLocation(Alignment alignment);
	
	/**
	 * 获得该转录本组的物种ID
	 * @return
	 */
	int getTaxID();
	
	/** 返回所有不重复GffDetailGene，注意如果有overlap超过30%的基因，它们会合并在同一个gffDetailGene中
	 * 需要调用{@link GffGene#getlsGffDetailGenes()}方法来获得具体每一个Gene */
	public ArrayList<GffGene> getGffDetailAll();
	/** 获得单个gffDetailGene，而不是一系列gffDetailGene的Unit<br>
	 * 不需要再调用{@link GffGene#getlsGffDetailGenes()}方法
	 * @return
	 */
	public List<GffGene> getLsGffDetailGenes();
	/** 染色体都小写 */
	public  HashMap<String, ListGff> getMapChrID2LsGff();

	/**
	 * 主要用于Rsem，产生一个GeneID对应IsoID的表
	 * @param Gene2IsoFile
	 */
	void writeGene2Iso(String Gene2IsoFile);
	/**
	 * @param lsChrID 指定chrID的list，产生和其一样大小写的chrID的gff文件
	 * @param GTFfile
	 * @param title
	 */
	void writeToGTF(List<String> lsChrID, String GTFfile, String title);
	void writeToGTF(String GTFfile, String title);

	void writeToBED(List<String> lsChrID, String BEDfile, String title);
	
	void writeToBED(String GTFfile, String title);
	void writeToBED(String GTFfile);

}
