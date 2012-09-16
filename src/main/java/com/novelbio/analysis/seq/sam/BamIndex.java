package com.novelbio.analysis.seq.sam;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;

public class BamIndex {

//	samtools view -bt /media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_chromFa/UCSC_hg19.fa.fai $SAMFile  > "$SAMPrix".bam
	
	String ExePath = "";
	String bamFile;
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
	public String index() {
		if (FileOperate.isFileExistAndBigThanSize(bamFile + ".bai", 1000)) {
			return bamFile;
		}
		String cmd = ExePath + "samtools index " + "\"" + bamFile + "\"";
		CmdOperate cmdOperate = new CmdOperate(cmd,"samIndex");
		cmdOperate.run();
		return bamFile;
	}

}
