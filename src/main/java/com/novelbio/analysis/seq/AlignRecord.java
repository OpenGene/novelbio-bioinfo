package com.novelbio.analysis.seq;

import java.util.ArrayList;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fastq.FastQRecord;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.dataStructure.Alignment;

public interface AlignRecord extends Alignment{
	/** 是否为unique mapping，不是的话mapping到了几个不同的位点上去 */
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
	/** 获得标志的位置，一般是中心点 */
	public int getFlagSite();
	
	boolean isUniqueMapping();
	
	public FastQRecord getFastQRecord();
}
