package com.novelbio.nbcgui.controlseq;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import com.novelbio.analysis.seq.FastQ;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 双端测序的两个fastq文件的集合
 * @author zong0jie
 */
public class CtrlFastQ {
	boolean pe = false;
	FastQ fastQ1;
	FastQ fastQ2;
	public void setFastQPE(String fastQ1File, String fastQ2File, int QUALITY) {
		this.pe = true;
		this.fastQ1 = new FastQ(fastQ1File, QUALITY);
		this.fastQ2 = new FastQ(fastQ2File, QUALITY);
	}
	public void setFastQSE(String fastQFile, int QUALITY) {
		this.pe = false;
		this.fastQ1 = new FastQ(fastQFile, QUALITY);
	}
	/**
	 * 过滤序列
	 */
	private void filtered() {
		if (pe) {
			fastQ1.filterReads(fastQ2);
		}
		else {
			fastQ1.filterReads();
		}
	}
	public void 
}
