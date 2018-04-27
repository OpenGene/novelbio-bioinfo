package com.novelbio.analysis.seq.sam;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.base.StringOperate;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.cmd.ExceptionCmd;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.ExceptionNbcFileInputNotExist;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.information.SoftWareInfo;
import com.novelbio.database.model.information.SoftWareInfo.SoftWare;

public class BamPileup implements IntCmdSoft {
//	samtools mpileup -EB  -C50 -f $chrmFa -Q 13 $bamFile > $out
	
	String ExePath = "";
	String bamFile;
	String referenceFile;
	int mapQuality = 13;
	boolean realign = false;
	String outPathName;
	
	public BamPileup() {
		SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.samtools);
		ExePath = softWareInfo.getExePathRun();
	}
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
	
	public void setOutPathName(String outPathName) {
		this.outPathName = outPathName;
	}
	
	public void setBamFile(String bamFile) {
		this.bamFile = bamFile;
	}
	public void setReferenceFile(String referenceFile) {
		ExceptionNbcFileInputNotExist.validateFile(referenceFile, "Pileup cannot run without a refSequencFile");

		this.referenceFile = referenceFile;
	}
	/** 是否要realign，默认是false */
	public void setRealign(boolean realign) {
		this.realign = realign;
	}
	/** 最低可以设置为0，这时候就是看覆盖度的。
	 * 最高不能高于35
	 * @param mapQuality 默认为13
	 */
	public void setMapQuality(int mapQuality) {
		if (mapQuality < 0 || mapQuality > 35) {
			return;
		}
		this.mapQuality = mapQuality;
	}
	
	public String pileup() {
		if (StringOperate.isRealNull(outPathName)) {
			outPathName = FileOperate.changeFileSuffix(bamFile, "_pileup", "gz");
		}
		return pileup(outPathName, false);
	}
	
	/** 直接压缩 */
	public String pileup(String outFile, boolean isCover) {
		if (!isCover && FileOperate.isFileExistAndBigThanSize(outFile, 0)) {
			return outFile;
		}
		outPathName = outFile;
		String outFileTmp = FileOperate.changeFileSuffix(outFile, "_tmp", null);
		CmdOperate cmdOperate = new CmdOperate(getLsCmd(outFileTmp));
		cmdOperate.setInputFile(bamFile);
		cmdOperate.run();
		if (!cmdOperate.isFinishedNormal()) {
			throw new ExceptionCmd("pileup error:\n" + cmdOperate.getCmdExeStrReal());
		}
		FileOperate.moveFile(true, outFileTmp, outFile);
		return bamFile;
	}
	
	private List<String> getLsCmd(String outFile) {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(ExePath + "samtools");
		lsCmd.add("mpileup");
		ArrayOperate.addArrayToList(lsCmd, getParam());
		ArrayOperate.addArrayToList(lsCmd, getIsRealign());
		ArrayOperate.addArrayToList(lsCmd, getMapQuality());
		ArrayOperate.addArrayToList(lsCmd, getReferenceFile());
		lsCmd.add("-");
		
		lsCmd.add(">");
		lsCmd.add(outFile);
		return lsCmd;
	}
	
	private String[] getParam() {
		return new String[]{"-E", "-C0", "-A"};
	}
	
	private String[] getReferenceFile() {
		return new String[]{"-f", referenceFile};
	}
	private String[] getMapQuality() {
		return new String[]{"-Q", mapQuality + ""};
	}
	private String[] getIsRealign() {
		if (realign) {
			return null;
		}
		else {
			return new String[]{"-B"};
		}
	}
	
	@Override
	public List<String> getCmdExeStr() {
		String pileupFile = FileOperate.changeFileSuffix(bamFile, "_pileup", "gz");
		CmdOperate cmdOperate = new CmdOperate(getLsCmd(pileupFile));
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(cmdOperate.getCmdExeStr());
		return lsCmd;
	}
}
