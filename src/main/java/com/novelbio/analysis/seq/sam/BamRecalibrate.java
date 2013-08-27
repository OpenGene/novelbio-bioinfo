package com.novelbio.analysis.seq.sam;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
	/** 输入文件路径+vcf文件名 */
	private Set<String> setSnpDBVcfFilePath = new HashSet<String>();
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
	/**
	 * @param snpVcfFile 已知的snpdb等文件，用于校正。可以添加多个
	 */
	public void addSnpVcfFile(String snpVcfFile) {
		if (FileOperate.isFileExistAndBigThanSize(snpVcfFile, 0)) {
			setSnpDBVcfFilePath.add(snpVcfFile);
		}
	}
	
	/**
	 * @param snpVcfFile 已知的snpdb等文件，用于校正。可以添加多个
	 */
	public void setSnpVcfFile(Collection<String> colSnpVcfFile) {
		if (colSnpVcfFile == null || colSnpVcfFile.size() == 0) {
			return;
		}
		setSnpDBVcfFilePath.clear();
		for (String snpVcfFile : colSnpVcfFile) {
			if (FileOperate.isFileExistAndBigThanSize(snpVcfFile, 0)) {
				setSnpDBVcfFilePath.add(snpVcfFile);
			}
		}
	}
	
	public String reCalibrate() {
		String bamRealignFile = FileOperate.changeFileSuffix(bamSortedFile, "_Recalibrate", "bam");
		return reCalibrate(bamRealignFile);
	}
	public String reCalibrate(String outFile) {
		String cmdCountCovariates ="java -Xmx4g -jar " + ExePath + "GenomeAnalysisTK.jar " +  "-T BaseRecalibrator " 
				+ getRefSequenceFile() + getSortedBam() + getThreadNum() + " -o "+ getRecalTableName() + getKnownSite();
		CmdOperate cmdOperate = new CmdOperate(cmdCountCovariates, "CountCovariates");
		cmdOperate.run();
		
		outFile = FileOperate.changeFileSuffix(outFile, "", "bam");
		String cmdRealign = "java -Xmx10g " + ExePath + "GenomeAnalysisTK.jar " +  "-T PrintReads " 
				+ getRefSequenceFile() + getSortedBam() + " -BQSR " + getRecalTableName() + " -o " + CmdOperate.addQuot(outFile) + " ";
		cmdOperate = new CmdOperate(cmdRealign,"samToBam");
		cmdOperate.run();
		if (FileOperate.isFileExistAndBigThanSize(outFile, 0)) {
			return outFile;
		}
		return null;
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
	private String getKnownSite() {
		String knownSite = "";
		for (String string : setSnpDBVcfFilePath) {
			knownSite = knownSite + " -knownSites " + CmdOperate.addQuot(string);
		}
		return knownSite + " ";
	}

	
	private String getRecalTableName() {
		return CmdOperate.addQuot(FileOperate.changeFilePrefix(bamSortedFile, "_recal_data", "grp"));
	}

	private String getOutRecalibrateBam(String outFile) {
		return " -o " + CmdOperate.addQuot(FileOperate.changeFileSuffix(outFile, "", "bam"));
	}
	
}
