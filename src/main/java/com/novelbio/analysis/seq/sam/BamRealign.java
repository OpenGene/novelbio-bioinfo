package com.novelbio.analysis.seq.sam;

import com.novelbio.base.PathDetail;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;

public class BamRealign {
//	java -Xmx4g -jar $GATK \
//    -T RealignerTargetCreator  \
//    -R $Human_Ref \
//    -I "$SAMPrix"_sorted.bam \
//    -o "$SAMPrix".intervals
//  
//
//java -Xmx10g -Djava.io.tmpdir=$TMPDIR \
//    -jar $GATK \
//	 -T IndelRealigner \
//	 -R $Human_Ref \
//    -I "$SAMPrix"_sorted.bam \
//    -targetIntervals "$SAMPrix".intervals \
//    -o "$SAMPrix"_GATKrealigned.bam
	
	String ExePath = "";
	String refSequenceFile;
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
	public void setRefSequenceFile(String refSequencFile) {
		this.refSequenceFile = refSequencFile;
	}
	public void setBamFile(String bamFile) {
		this.bamSortedFile = bamFile;
	}
	public String realign() {
		String bamRealignFile = FileOperate.changeFileSuffix(bamSortedFile, "_realign", "bam");
		return realign(bamRealignFile);
	}
	public String realign(String outFile) {
		String cmdInertval ="java -Xmx4g -jar " + ExePath + "GenomeAnalysisTK.jar " +  "-T RealignerTargetCreator " 
				+ getRefSequenceFile() + getSortedBam() + getOutIntervalFile();
		CmdOperate cmdOperate = new CmdOperate(cmdInertval,"samToBam");
		cmdOperate.run();
		
		String cmdRealign = "java -Xmx10g -jar " + getTmpPath() + ExePath + "GenomeAnalysisTK.jar " +  "-T IndelRealigner " 
				+ getRefSequenceFile() + getSortedBam() + getInIntervalFile() + getOutRealignBam(outFile);
		cmdOperate = new CmdOperate(cmdRealign,"samToBam");
		cmdOperate.run();
		
		return FileOperate.changeFileSuffix(outFile, "", "bam");
	}
	
	private String getRefSequenceFile() {
		return "-R " + "\"" + refSequenceFile + "\" ";
	}
	private String getSortedBam() {
		return "-I " + "\"" + bamSortedFile + "\" ";
	}
	private String getOutIntervalFile() {
		return "-o " + "\"" + FileOperate.changeFileSuffix(bamSortedFile, "", "intervals") + "\" ";
	}

	private String getTmpPath() {
		return "-Djava.io.tmpdir=" + "\""+PathDetail.getTmpPath() +"\" ";
	}
	private String getInIntervalFile() {
		return "-targetIntervals " + "\"" + FileOperate.changeFileSuffix(bamSortedFile, "", "intervals") + "\" ";
	}
	private String getOutRealignBam(String outFile) {
		return "-o " + "\"" + FileOperate.changeFileSuffix(outFile, "", "bam") + "\" ";
	}
	
}
