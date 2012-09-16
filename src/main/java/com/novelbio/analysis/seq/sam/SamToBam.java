package com.novelbio.analysis.seq.sam;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;

/** 将sam文件转化为bam文件 */
class SamToBam {
//	samtools view -bt /media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_chromFa/UCSC_hg19.fa.fai $SAMFile  > "$SAMPrix".bam
	
	String ExePath = "";
	String samBamFile;
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
