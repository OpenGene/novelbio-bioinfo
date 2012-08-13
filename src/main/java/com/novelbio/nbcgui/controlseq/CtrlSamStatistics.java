package com.novelbio.nbcgui.controlseq;

import com.novelbio.analysis.seq.mapping.SamFile;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/** 各种统计，主要是统计Sam文件的 */
public class CtrlSamStatistics {
	SamFile samFile;
	String outFile;
	String prefix;

	String txtFile;

	public void setSamFile(String samFile) {
		this.samFile = new SamFile(samFile);
		if (prefix == null) {
			prefix = FileOperate.getFileNameSep(samFile)[0];
		}
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	public void setOutFile(String outFile) {
		this.outFile = outFile;
	}
	public void writeSamStatistics() {
		TxtReadandWrite txtWrite = new TxtReadandWrite(outFile, true);
		txtWrite.ExcelWrite(samFile.getStatistics().getMappingInfo());
	}
	
	public void setTxtFile(String txtFile) {
		this.txtFile = txtFile;
		if (prefix == null) {
			prefix = FileOperate.getFileNameSep(txtFile)[0];
		}
	}
	public void writeTxtStatistics() {
		TxtReadandWrite txtWrite = new TxtReadandWrite(outFile, true);
		long lines = 0;
		for (String string : txtWrite.readlines()) {
			lines ++;
		}
		txtWrite.writefile(outFile + "\t" +lines);
	}
	
}
