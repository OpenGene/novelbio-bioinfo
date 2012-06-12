package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.database.model.modcopeid.CopedID;
/**
 * 将GffHash和GffHashGene的方法全部列在了这里
 * @author zong0jie
 *
 */
public interface GffHashGeneInf  {
	
	/**
	 * 	返回外显子总长度，内含子总长度等信息，只统计最长转录本的信息
	 * 有问题
	 * 为一个ArrayList-Integer
	 * 0: all5UTRLength <br>
	 * 1: all3UTRLength <br>
	 * 2: allExonLength 不包括5UTR和3UTR的长度 <br> 
	 * 3: allIntronLength <br>
	 * 4: allup2kLength <br>
	 * 5: allGeneLength <br>
	 * @return 
	 */
	public ArrayList<Long> getGeneStructureLength(int upBp);

	/**
	 * 起点是否为闭区间，不是则为开区间，<br>
	 * False: 开区间的意思是，24表示从0开始计数的24位，也就是实际的25位<br>
	 * True: 闭区间的意思是，24就代表第24位<br>
	 * UCSC的默认文件的终点是闭区间间
	 */
	public void setEndRegion(boolean region);
	
	
	/**
	 * 返回List顺序存储每个基因号或条目号，这个打算用于提取随机基因号。
	 * 不能通过该方法获得某个LOC在基因上的定位
	 */
	public ArrayList<String> getLOCIDList();
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
	 * 输入CopedID，返回基因的坐标信息等
	 * @param copedID 
	 * @return
	 * 没有就返回null
	 */
	GffDetailGene searchLOC(CopedID copedID);
	/**
	 * 给定LOCID，返回所对应的转录本
	 * @param LOCID
	 * @return
	 */
	GffGeneIsoInfo searchISO(String LOCID);
	/**
	 * 获得该转录本组的物种ID
	 * @return
	 */
	int getTaxID();

	void writeToGTF(String GTFfile, String title);
	/**
	 * 仅将有可变剪接的转录本写入GFF3文件
	 * @param GTFfile
	 * @param title
	 */
	void writeToGFFIso(String GFFfile, String title);
	/**
	 * 主要用于Rsem，产生一个GeneID对应IsoID的表
	 * @param Gene2IsoFile
	 */
	void writeGene2Iso(String Gene2IsoFile);

}
