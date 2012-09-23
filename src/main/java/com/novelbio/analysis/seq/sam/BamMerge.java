package com.novelbio.analysis.seq.sam;

import java.util.ArrayList;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;

public class BamMerge {	
	String ExePath = "";
	ArrayList<String> lsBamFile = new ArrayList<String>();
	String outFileName;
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
	public void addBamFile(String bamFile) {
		lsBamFile.add(bamFile);
	}
	public void setLsBamFile(ArrayList<String> lsBamFile) {
		this.lsBamFile = lsBamFile;
	}
	/** �����׺��Ϊbam�����ļ���׺�Զ����.bam */
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
	/** ��������Ѿ��趨��bam�ļ� */
	public void clear() {
		lsBamFile.clear();
	}
	/** ����merge������֣�"" ��ʾû�гɹ� */
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
