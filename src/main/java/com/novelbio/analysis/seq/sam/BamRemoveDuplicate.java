package com.novelbio.analysis.seq.sam;

import com.novelbio.base.PathDetail;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 用samtools来去除pcr duplicate
 * @author zong0jie
 *
 */
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
		boolean isFinish = removeDuplicate(bamNoDuplicateFile);
		if (isFinish) {
			return bamNoDuplicateFile;
		} else {
			return null;
		}
	}
	public boolean removeDuplicate(String outFile) {
		String cmdInertval = ExePath + "samtools rmdup " + getInputBam() + getOutFile(outFile);
		CmdOperate cmdOperate = new CmdOperate(cmdInertval,"removePcrDuplicate");
		cmdOperate.run();
		
		return  cmdOperate.isFinished();
	}
	
	private String getInputBam() {
		return  " " + CmdOperate.addQuot(bamSortedFile) + " ";
	}

	private String getOutFile(String outFile) {
		return  FileOperate.changeFileSuffix(outFile, "", "bam");
	}

}
