package com.novelbio.analysis.seq.mapping;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.genomeNew.GffChrAbs;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;

public interface MapRNA {
	public void setGffChrAbs(GffChrAbs gffChrAbs);
	/**
	 * �趨tophat���ڵ��ļ����Լ����ȶԵ�·��
	 * @param exePath ����ڸ�Ŀ¼��������Ϊ""��null
	 * @param exePathBowtie
	 */
	public void setExePath(String exePath, String exePathBowtie);
	
	public void setFileRef(String chrFile);
	
	public void setOutPathPrefix(String outPathPrefix);
	/** �趨indel */
	public void setIndelLen(int indelLen);

	/** �߳�������Ĭ��4�߳� */
	public void setThreadNum(int threadNum);
	/**
	 * STRAND_NULL�ȣ�ò��������RNA-Seq�Ƿ�Ϊ�������Բ���ģ��Բ�׼
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
	 * ���볤�ȣ�Ĭ����illumina��450
	 * @param insert
	 */
	public void setInsert(int insert);
	
	public void setLeftFq(List<FastQ> lsLeftFq);
	public void setRightFq(List<FastQ> lsRightFq);
	
	/** ���䣬�����Ĭ�ϱȽϺã�Ĭ��Ϊ2 */
	public void setMismatch(int mismatch);

	/** �����趨��������solid ��û����gtf��ѡ�Ҳ����Ĭ��û��gtf */
	public void mapReads();
	SoftWare getBowtieVersion();
	
}
