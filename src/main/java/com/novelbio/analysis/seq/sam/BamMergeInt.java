package com.novelbio.analysis.seq.sam;

import java.util.List;

import com.novelbio.analysis.IntCmdSoft;

public interface BamMergeInt extends IntCmdSoft {

	public void addBamFile(String bamFile);
	public void setLsBamFile(List<String> lsBamFile);
	/** 如果后缀不为bam，则文件后缀自动添加.bam */
	public void setOutFileName(String outFileName);
	/** 清空所有已经设定的bam文件 */
	public void clear();
	
	/** 返回merge后的SamFile，null 表示没有成功 */
	public SamFile mergeSam();

}
