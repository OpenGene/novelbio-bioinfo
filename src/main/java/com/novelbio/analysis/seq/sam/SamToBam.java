package com.novelbio.analysis.seq.sam;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;

/** ��sam�ļ�ת��Ϊbam�ļ� */
class SamToBam {
//	samtools view -bt /media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_chromFa/UCSC_hg19.fa.fai $SAMFile  > "$SAMPrix".bam
	
	String ExePath = "";
	String samBamFile;
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
	public void setSamFile(String samFile) {
		this.samBamFile = samFile;
	}
	public String convertToBam() {
		String bamFile = FileOperate.changeFileSuffix(samBamFile, "", "bam");
		return convertToBam(bamFile);
	}
	public String convertToBam(String outFile) {
		if (outFile == null || outFile.equals("")) {
			outFile = FileOperate.changeFileSuffix(samBamFile, "", "bam");
		}
		String bamFile = FileOperate.changeFileSuffix(outFile, "", "bam");
		String cmd = ExePath + "samtools view -Sb " + "\"" + samBamFile + "\"" + " > " + "\"" + bamFile + "\"";
		CmdOperate cmdOperate = new CmdOperate(cmd,"samToBam");
		cmdOperate.run();
		return bamFile;
	}
}
