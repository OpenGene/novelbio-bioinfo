package com.novelbio.analysis.seq.sam;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;

public class SamIndexRefsequence {
	
	String ExePath = "";
	String sequence;
	/**
	 * 设定samtools所在的文件夹以及待比对的路径
	 * @param exePath 如果在根目录下则设置为""或null
	 */
	public void setExePath(String exePath) {
		if (exePath == null || exePath.trim().equals(""))
			this.ExePath = "";
		else
			this.ExePath = FileOperate.addSep(exePath);
	}
	public void setRefsequence(String sequence) {
		this.sequence = sequence;
	}
	public void indexSequence() {
		if (FileOperate.isFileExistAndBigThanSize(sequence + ".fai", 0)) {
			return;
		}
		String cmd = ExePath + "samtools faidx " + "\"" + sequence + "\"";
		CmdOperate cmdOperate = new CmdOperate(cmd,"sortBam");
		cmdOperate.run();
	}

}
