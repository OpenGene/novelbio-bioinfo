package com.novelbio.analysis.seq.sam;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;

public class BamPileup {

//	samtools mpileup -EB  -C50 -f $chrmFa -Q 13 $bamFile > $out
	
	String ExePath = "";
	String bamFile;
	String referenceFile;
	int mapQuality = 13;
	boolean realign = false;
	
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
	public void setRealign(boolean realign) {
		this.realign = realign;
	}
	/** 最低可以设置为0，这时候就是看覆盖度的。
	 * 最高不能高于35*/
	public void setMapQuality(int mapQuality) {
		if (mapQuality < 0 || mapQuality > 35) {
			return;
		}
		this.mapQuality = mapQuality;
	}
	public String pileup() {
		String pileupFile = FileOperate.changeFileSuffix(bamFile, "_pileup", "gz");
		return pileup(pileupFile);
	}
	/** 直接压缩 */
	public String pileup(String outFile) {
		String cmd = "";
		if (outFile.toLowerCase().endsWith("gz")) {
			cmd = ExePath + "samtools mpileup -E -C50 " + getIsRealign() + getMapQuality() + getReferenceFile()
					+ CmdOperate.addQuot(bamFile) + " | gzip > " + CmdOperate.addQuot(outFile);
		}
		else {
			cmd = ExePath + "samtools mpileup -E -C50 " + getIsRealign() + getMapQuality() + getReferenceFile()
					+ CmdOperate.addQuot(bamFile) + " > " + CmdOperate.addQuot(outFile);
		}
		CmdOperate cmdOperate = new CmdOperate(cmd,"BamPileUp");
		cmdOperate.run();
		return bamFile;
	}
	private String getReferenceFile() {
		return "-f " + "\"" + referenceFile +"\" ";
	}
	private String getMapQuality() {
		return "-Q " + mapQuality + " ";
	}
	private String getIsRealign() {
		if (realign) {
			return "";
		}
		else {
			return "-B ";
		}
	}
}
