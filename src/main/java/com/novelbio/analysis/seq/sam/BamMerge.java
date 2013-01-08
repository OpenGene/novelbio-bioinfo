package com.novelbio.analysis.seq.sam;

import java.util.ArrayList;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;

public class BamMerge {	
	String ExePath = "";
	ArrayList<String> lsBamFile = new ArrayList<String>();
	String outFileName;
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
	public void addBamFile(String bamFile) {
		lsBamFile.add(bamFile);
	}
	public void setLsBamFile(ArrayList<String> lsBamFile) {
		this.lsBamFile = lsBamFile;
	}
	/** 如果后缀不为bam，则文件后缀自动添加.bam */
	public void setOutFileName(String outFileName) {
		outFileName = outFileName.trim();
		if (!outFileName.endsWith(".bam")) {
			if (!outFileName.endsWith(".")) {
				outFileName = outFileName + ".";
			}
			outFileName = outFileName + "bam";
		}
		this.outFileName = outFileName;
	}
	/** 清空所有已经设定的bam文件 */
	public void clear() {
		lsBamFile.clear();
	}
	/** 返回merge后的名字，"" 表示没有成功 */
	public String merge() {
		if (lsBamFile.size() == 0) {
			return "";
		} else if (lsBamFile.size() == 1) {
			FileOperate.moveFile(lsBamFile.get(0), outFileName, true);
		} else {
			String cmd = ExePath + "samtools merge " + "\"" + outFileName + "\"";
			for (String bamFile : lsBamFile) {
				cmd = cmd + " \"" + bamFile + "\"";
			}
			CmdOperate cmdOperate = new CmdOperate(cmd,"mergeBam");
			cmdOperate.run();
		}
		return outFileName;
	}

}
