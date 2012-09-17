package com.novelbio.analysis.seq.sam;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;

/** 对于每个碱基质量的重估计 */
public class BamRecalibrate {
//#重新计算每个碱基的质量  第一步，计算变异
//java -Xmx4g -jar $GATK \
//     -T CountCovariates \
//	 -I "$SAMPrix"_RealnDeDup.bam \
//     -R $Human_Ref \
//	  -nt $ThreadNum \
//	  --default_platform ILLUMINA \
//     -l INFO \
//      -cov ReadGroupCovariate \
//      -cov QualityScoreCovariate \
//      -cov CycleCovariate \
//      -cov DinucCovariate \
//      -recalFile "$SAMPrix"_recal_data.csv
//
//#重新计算每个碱基的质量  第二步，将变异的矩阵表用于修正bam文件
//java -Xmx4g -jar $GATK \
//  -T TableRecalibration \
//  -I "$SAMPrix"_RealnDeDup.bam \
//  -R $Human_Ref \
//   -recalFile "$SAMPrix"_recal_data.csv \
//   -l INFO \
//   --default_platform ILLUMINA \
//   -o "$SAMPrix"_recal.bam
	
	String ExePath = "";
	String refSequenceFile;
	String bamSortedFile;
	int threadNum = 4;
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
	public String reCalibrate() {
		String bamRealignFile = FileOperate.changeFileSuffix(bamSortedFile, "_Recalibrate", "bam");
		return reCalibrate(bamRealignFile);
	}
	public String reCalibrate(String outFile) {
		String cmdCountCovariates ="java -Xmx4g -jar " + ExePath + "GenomeAnalysisTK.jar " +  "-T CountCovariates " 
				+ getRefSequenceFile() + getSortedBam() + getThreadNum() + getParamCountCovariates() + getOutRecalCsv();
		CmdOperate cmdOperate = new CmdOperate(cmdCountCovariates, "CountCovariates");
		cmdOperate.run();
		
		String cmdRealign = "java -Xmx10g " + ExePath + "GenomeAnalysisTK.jar " +  "-T TableRecalibration " 
				+ getRefSequenceFile() + getSortedBam() + getOutRecalCsv() + getParamRecalibrate() + getOutRecalibrateBam(outFile);
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
	private String getThreadNum() {
		return "-nt " + threadNum + " ";
	}
	private String getParamCountCovariates() {
		return "--default_platform ILLUMINA -l INFO -cov ReadGroupCovariate -cov QualityScoreCovariate -cov CycleCovariate -cov DinucCovariate ";
	}
	private String getOutRecalCsv() {
		return "-recalFile " + "\""+ FileOperate.changeFilePrefix(bamSortedFile, "_recal_data", "csv") + "\" ";
	}
	private String getParamRecalibrate() {
		return "-l INFO --default_platform ILLUMINA ";
	}
	private String getOutRecalibrateBam(String outFile) {
		return "-o " + "\"" + FileOperate.changeFileSuffix(outFile, "", "bam") + "\" ";
	}
	

}
