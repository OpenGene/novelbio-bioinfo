package com.novelbio.analysis.seq.sam;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.cmd.ExceptionCmd;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.information.SoftWareInfo;
import com.novelbio.database.model.information.SoftWareInfo.SoftWare;

public class BamMerge implements BamMergeInt {
	String ExePath = "";
	List<String> lsBamFile = new ArrayList<String>();
	String outFileName;
	List<String> lsCmdLine = new ArrayList<>();
	
	public BamMerge() {
		SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.samtools);
		ExePath = softWareInfo.getExePathRun();
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
	private String merge() {
		lsCmdLine.clear();
		if (lsBamFile.size() == 0) {
			return "";
		} else if (lsBamFile.size() == 1) {
			FileOperate.moveFile(true, lsBamFile.get(0), outFileName);
		} else {
			String outFileNameTmp = FileOperate.changeFileSuffix(outFileName, "_tmp", null);
			CmdOperate cmdOperate = new CmdOperate(getLsCmd(outFileNameTmp));
			cmdOperate.run();
			if (!cmdOperate.isFinishedNormal()) {
				FileOperate.deleteFileFolder(outFileNameTmp);
				throw new ExceptionCmd("sam merge error:\n" + cmdOperate.getCmdExeStrReal());
			}
			FileOperate.moveFile(true, outFileNameTmp, outFileName);
		}
		return outFileName;
	}
	
	private List<String> getLsCmd(String outFileName) {
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
	
	public static BamMergeInt getInstance() {
		return new BamMergeJava();
	}
	
}
