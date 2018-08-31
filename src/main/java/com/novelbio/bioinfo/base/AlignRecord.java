package com.novelbio.bioinfo.base;

import java.util.ArrayList;

import com.novelbio.base.dataStructure.Alignment;
import com.novelbio.bioinfo.fasta.SeqFasta;
import com.novelbio.bioinfo.fastq.FastQRecord;

public interface AlignRecord extends Alignment{
	/** 是否为unique mapping，不是的话mapping到了几个不同的位点上去<br>
	 * 但是一个文件里面可能仅出现一条该reads，如果要确认具体出现多少次该reads，请用{@link #getMappingNum()}
	 * @return
	 */
	public Integer getMappingNum();
	public Integer getMapQuality();
	
	
	/** 该bed文件是否被割成了一段一段的 */
	public boolean isJunctionCovered();
	public boolean isMapped();
	public String getName();
	public String getCIGAR();
	public SeqFasta getSeqFasta();
	/** 如果是mapping到junction上去，一条bed文件记录会被切成被切成的几块的样子保存在这里。
	 * 也就是一段一段的bed，那么返回每一段的信息，
	 * 都是绝对坐标，从1开始
	 * @return
	 */
	public ArrayList<Align> getAlignmentBlocks();
	
	public String getRawStringInfo();
	
	boolean isUniqueMapping();
	
	public FastQRecord toFastQRecord();
	/**
	 * reads的权重，意思相同的reads在本sam文件中出现了几次
	 * bwa的结果，一条reads只有一行，所以恒返回1
	 * tophat的结果，一条reads如果mapping至多个位置，在文件中就会出现多次，所以返回可能大于1
	 * */
	public int getMappedReadsWeight();
	
	/** 获得碱基长度 */
	public int getLengthReal();
}
