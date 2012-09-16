package com.novelbio.analysis.seq.sam;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;

public class BamPileup {

//	samtools mpileup -EB  -C 50 -f $chrmFa -Q 13 $bamFile > $out
	
	String ExePath = "";
	String bamFile;
	String referenceFile;
	int mapQuality = 13;
	boolean realign = false;
	
	/**
	 * �趨samtools���ڵ��ļ����Լ����ȶԵ�·��
	 * @param exePath ����ڸ�Ŀ¼��������Ϊ""��null
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
	/** ��Ϳ�������Ϊ0����ʱ����ǿ����Ƕȵġ�
	 * ��߲��ܸ���35*/
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
	/** ֱ��ѹ�� */
	public String pileup(String outFile) {
		String cmd = "";
		if (outFile.toLowerCase().endsWith("gz")) {
			cmd = ExePath + "samtools mpileup -E -C50 " + getIsRealign() + getMapQuality() + getReferenceFile() + "\"" + bamFile + "\"" + " | gzip > " + "\"" + outFile + "\"";
		}
		else {
			cmd = ExePath + "samtools mpileup -E -C50 " + getIsRealign() + getMapQuality() + getReferenceFile() + "\"" + bamFile + "\"" + " > " + "\"" + outFile + "\"";
		}
		CmdOperate cmdOperate = new CmdOperate(cmd,"samToBam");
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
