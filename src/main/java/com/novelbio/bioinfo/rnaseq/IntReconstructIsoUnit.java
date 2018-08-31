package com.novelbio.bioinfo.rnaseq;

import com.novelbio.bioinfo.IntCmdSoft;

public interface IntReconstructIsoUnit extends IntCmdSoft {
	/** 获得该bam文件所对应的输出GTF */
	String getOutGtfName(String bamFile);
	/**
	 * 用本bam文件重建转录本，输出的文件名必须与 {@link #getOutGtfName(String)}获得的文件名一致
	 * @param bamFile
	 */
	void reconstruct(String bamFile);
	
	/** 设定输出文件夹 */
	void setOutPath(String outPath);
}
