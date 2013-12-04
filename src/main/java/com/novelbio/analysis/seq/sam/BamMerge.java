package com.novelbio.analysis.seq.sam;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.cmd.ExceptionCmd;
import com.novelbio.base.fileOperate.FileOperate;

public class BamMerge implements IntCmdSoft {	
	String ExePath = "";
	List<String> lsBamFile = new ArrayList<String>();
	String outFileName;
	List<String> lsCmdLine = new ArrayList<>();
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
	public void setLsBamFile(List<String> lsBamFile) {
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
	
	/** 返回merge后的SamFile，null 表示没有成功 */
	public SamFile mergeSam() {
		String outFileName = merge();
		if (outFileName.equals("")) {
			return null;
		}
		return new SamFile(outFileName);
	}
	
	/** 返回merge后的名字，"" 表示没有成功 */
	public String merge() {
		lsCmdLine.clear();
		if (lsBamFile.size() == 0) {
			return "";
		} else if (lsBamFile.size() == 1) {
			FileOperate.moveFile(lsBamFile.get(0), outFileName, true);
		} else {
			CmdOperate cmdOperate = new CmdOperate(getLsCmd());
			cmdOperate.run();
			if (!cmdOperate.isFinishedNormal()) {
				throw new ExceptionCmd("sam merge error:\n" + cmdOperate.getCmdExeStrReal());
			}
		}
		return outFileName;
	}
	
	private List<String> getLsCmd() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(ExePath + "samtools");
		lsCmd.add("merge");
		lsCmd.add(outFileName);
		for (String bamfile : lsBamFile) {
			lsCmd.add(bamfile);
		}
		return lsCmd;
	}
	
	@Override
	public List<String> getCmdExeStr() {
		return lsCmdLine;
	}

}
