package com.novelbio.analysis.seq.sam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.cmd.ExceptionCmd;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;

/** 对于每个碱基质量的重估计 */
public class BamRecalibrate implements IntCmdSoft {
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
	
	List<String> lsCmdInfo = new ArrayList<>();
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
		lsCmdInfo.clear();
		CmdOperate cmdOperate = new CmdOperate(getLsCmdBaseRecal(outFile));
		cmdOperate.run();
		if (!cmdOperate.isFinishedNormal()) {
			throw new ExceptionCmd("gatk base recal error:\n" + cmdOperate.getCmdExeStrReal());
		}
		lsCmdInfo.add(cmdOperate.getCmdExeStr());

		cmdOperate = new CmdOperate(getLsCmdPrintReads(outFile));
		cmdOperate.run();
		if (!cmdOperate.isFinishedNormal()) {
			throw new ExceptionCmd("gatk print reads error:\n" + cmdOperate.getCmdExeStrReal());
		}
		lsCmdInfo.add(cmdOperate.getCmdExeStr());
		return outFile;
	}
	
	private List<String> getLsCmdBaseRecal(String outFile) {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add("java");
		lsCmd.add("-Xmx4g");
		lsCmd.add("-jar");
		lsCmd.add(ExePath + "GenomeAnalysisTK.jar");
		lsCmd.add("-T"); lsCmd.add("BaseRecalibrator");
		ArrayOperate.addArrayToList(lsCmd, getRefSequenceFile());
		ArrayOperate.addArrayToList(lsCmd, getSortedBam());
		ArrayOperate.addArrayToList(lsCmd, getThreadNum());
		ArrayOperate.addArrayToList(lsCmd, getRecalTableName(outFile));
		lsCmd.addAll(getKnownSite());
		return lsCmd;
	}
	
	private List<String> getLsCmdPrintReads(String outFile) {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add("java");
		lsCmd.add("-Xmx10g");
		lsCmd.add("-jar");
		lsCmd.add(ExePath + "GenomeAnalysisTK.jar");
		lsCmd.add("-T"); lsCmd.add("PrintReads");
		ArrayOperate.addArrayToList(lsCmd, getRefSequenceFile());
		ArrayOperate.addArrayToList(lsCmd, getSortedBam());
		lsCmd.add("-BQSR");
		ArrayOperate.addArrayToList(lsCmd, getRecalTableName(outFile));
		ArrayOperate.addArrayToList(lsCmd, getOutRecalibrateBam(outFile));
		lsCmd.addAll(getKnownSite());
		return lsCmd;
	}
	
	private String[] getRefSequenceFile() {
		return new String[]{"-R", refSequenceFile};
	}
	private String[] getSortedBam() {
		return new String[]{"-I", bamSortedFile};
	}
	private String[] getThreadNum() {
		return new String[]{"-nt", threadNum + ""};
	}
	private List<String> getKnownSite() {
		List<String> lsKnowSite = new ArrayList<>();
		for (String vcfFile : setSnpDBVcfFilePath) {
			lsKnowSite.add("-knownSites");
			lsKnowSite.add(vcfFile);
		}
		return lsKnowSite;
	}
	
	private String[] getRecalTableName(String outFile) {
		String recalTable = FileOperate.changeFileSuffix(outFile, "_recal_data", "grp");
		return new String[]{"-o", recalTable};
	}

	private String[] getOutRecalibrateBam(String outFile) {
		return new String[]{"-o", outFile};
	}
	@Override
	public List<String> getCmdExeStr() {
		return lsCmdInfo;
	}
	
}
