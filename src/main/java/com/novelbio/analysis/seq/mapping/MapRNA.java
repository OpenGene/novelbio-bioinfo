package com.novelbio.analysis.seq.mapping;

import java.util.List;

import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;

public interface MapRNA extends IntCmdSoft {
	public void setGffChrAbs(GffChrAbs gffChrAbs);
	
	/** 设定index或与index相关的ref */
	public void setRefIndex(String chrFile);
	
	public void setOutPathPrefix(String outPathPrefix);
	/** 设定indel */
	public void setIndelLen(int indelLen);

	/** 线程数量，默认4线程 */
	public void setThreadNum(int threadNum);
	/**
	 * STRAND_NULL等，貌似是设置RNA-Seq是否为链特异性测序的，吃不准
	 * 
	 * @param strandSpecifictype
	 * <br>
	 *            <b>fr-unstranded</b> Standard Illumina Reads from the
	 *            left-most end of the fragment (in transcript coordinates) map
	 *            to the transcript strand, and the right-most end maps to the
	 *            opposite strand.<br>
	 *            <b>fr-firststrand</b> dUTP, NSR, NNSR Same as above except we
	 *            enforce the rule that the right-most end of the fragment (in
	 *            transcript coordinates) is the first sequenced (or only
	 *            sequenced for single-end reads). Equivalently, it is assumed
	 *            that only the strand generated during first strand synthesis
	 *            is sequenced.<br>
	 *            <b>fr-secondstrand</b> Ligation, Standard SOLiD Same as above
	 *            except we enforce the rule that the left-most end of the
	 *            fragment (in transcript coordinates) is the first sequenced
	 *            (or only sequenced for single-end reads). Equivalently, it is
	 *            assumed that only the strand generated during second strand
	 *            synthesis is sequenced.
	 */
	public void setStrandSpecifictype(StrandSpecific strandSpecifictype);
	
	/**
	 * 插入长度，默认是illumina：450
	 * @param insert
	 */
	public void setInsert(int insert);
	
	public void setLeftFq(List<FastQ> lsLeftFq);
	public void setRightFq(List<FastQ> lsRightFq);
	
	/** 错配，这个走默认比较好，默认为2 */
	public void setMismatch(int mismatch);

	/** 参数设定不能用于solid 还没加入gtf的选项，也就是默认没有gtf */
	public void mapReads();
	SoftWare getBowtieVersion();
	
	/**
	 * Tophat:<br>用gtf文件辅助mapping
	 * 如果设定为null，则表示不使用gtf文件
	 * 如果设定为“”等不存在文件的，则使用GffChrAbs中的Gff文件
	 * <p>
	 * RSEM<br>
	 * 设定Gene2Iso文件，如果有文件就用这个文件。
	 * 如果文件不存在，则从GffChrAbs中生成这个文件
	 * <p>
	 * MapSplicer<br>
	 * 设定含有全体ref序列的文件夹
	 * @param gtfFile
	 */
	public void setGtf_Gene2Iso(String gtfFile);
	
	/** 预测的结果文件的文件名 */
	public String getFinishName();
}
