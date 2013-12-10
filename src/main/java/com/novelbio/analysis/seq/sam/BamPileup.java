package com.novelbio.analysis.seq.sam;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.cmd.ExceptionCmd;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;

public class BamPileup implements IntCmdSoft {
//	samtools mpileup -EB  -C50 -f $chrmFa -Q 13 $bamFile > $out
	
	String ExePath = "";
	String bamFile;
	String referenceFile;
	int mapQuality = 13;
	boolean realign = false;
	
	List<String> lsCmdInfo = new ArrayList<>();
	
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
	public void setBamFile(String bamFile) {
		this.bamFile = bamFile;
	}
	public void setReferenceFile(String referenceFile) {
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
		String pileupFile = FileOperate.changeFileSuffix(bamFile, "_pileup", "gz");
		return pileup(pileupFile, false);
	}
	/** 直接压缩 */
	public String pileup(String outFile, boolean isCover) {
		lsCmdInfo.clear();
		if (!isCover && FileOperate.isFileExistAndBigThanSize(outFile, 0)) {
			return outFile;
		}
		CmdOperate cmdOperate = new CmdOperate(getLsCmd(outFile));
		cmdOperate.run();
		if (!cmdOperate.isFinishedNormal()) {
			throw new ExceptionCmd("pileup error:\n" + cmdOperate.getCmdExeStrReal());
		}
		lsCmdInfo.add(cmdOperate.getCmdExeStr());
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
		lsCmd.add(bamFile);
		lsCmd.add(">");
		lsCmd.add(outFile);
		return lsCmd;
	}
	
	private String[] getParam() {
		return new String[]{"-E", "-C50"};
	}
	
	private String[] getReferenceFile() {
		return new String[]{"-f", referenceFile};
	}
	private String[] getMapQuality() {
		return new String[]{"-Q " + mapQuality};
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
		return lsCmdInfo;
	}
}
