package com.novelbio.analysis.seq.sam;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;

public class BamRemoveDuplicate {

//	java -Xmx4g -Djava.io.tmpdir=$TMPDIR \
//		     -jar "$PICARD_DIR"/MarkDuplicates.jar \
//		     INPUT="$SAMPrix"_GATKrealigned.bam \
//			 REMOVE_DUPLICATES=true \
//		     VALIDATION_STRINGENCY=LENIENT \
//			 AS=true \
//		     METRICS_FILE="$SAMPrix".dups \
//			 OUTPUT="$SAMPrix"_RealnDeDup.bam 
	
	String ExePath = "";
	String bamSortedFile;
	String tmpPath = "/home/tmp";
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
		this.bamSortedFile = bamFile;
	}
	public String removeDuplicate() {
		String bamNoDuplicateFile = FileOperate.changeFileSuffix(bamSortedFile, "_NoDuplicate", "bam");
		return removeDuplicate(bamNoDuplicateFile);
	}
	public String removeDuplicate(String outFile) {
		String cmdInertval ="java -Xmx6g -jar " + getTmpPath() + ExePath + "MarkDuplicates.jar "
				+ getInputBam() + getParam() + getMETRICS() + getOutFile(outFile);
		CmdOperate cmdOperate = new CmdOperate(cmdInertval,"samToBam");
		cmdOperate.run();
		
		return FileOperate.changeFileSuffix(outFile, "", "bam");
	}
	
	private String getInputBam() {
		return  "INPUT=" + "\"" + bamSortedFile + "\" ";
	}
	private String getParam() {
		return "REMOVE_DUPLICATES=true VALIDATION_STRINGENCY=LENIENT  AS=true ";
	}
	private String getTmpPath() {
		return "-Djava.io.tmpdir=" + "\""+tmpPath +"\" ";
	}
	/** duplicate的矩阵 */
	private String getMETRICS() {
		return "METRICS_FILE=" + "\"" + FileOperate.changeFilePrefix(bamSortedFile, "_duplicate", "txt") + "\" ";
	}
	private String getOutFile(String outFile) {
		return "OUTPUT=" + "\"" + FileOperate.changeFileSuffix(outFile, "", "bam") + "\" ";
	}

}
