package com.novelbio.analysis.seq.sam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.base.StringOperate;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.cmd.ExceptionCmd;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.ExceptionNbcFileInputNotExist;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.information.SoftWareInfo;
import com.novelbio.database.model.information.SoftWareInfo.SoftWare;

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
	
	String exePath = "";
	String refSequenceFile;
	String bamSortedFile;
	int threadNum = 4;
	/** 输入文件路径+vcf文件名 */
	private Set<String> setSnpDBVcfFilePath = new HashSet<String>();
	private String unsafe = GATKRealign.ALL;

	List<String> lsCmdInfo = new ArrayList<>();
	
	public BamRecalibrate() {
		SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.GATK);
		exePath = softWareInfo.getExePathRun();
	}
	public void setRefSequenceFile(String refSequencFile) {
		ExceptionNbcFileInputNotExist.validateFile(refSequencFile, "Recalibrate cannot run without a refSequencFile");
		
		this.refSequenceFile = refSequencFile;
	}
	public void setBamFile(String bamFile) {
		this.bamSortedFile = bamFile;
	}
	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum;
	}
	public void setUnsafe(String unsafe) {
		this.unsafe = unsafe;
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
		return reCalibrate(bamRealignFile, false);
	}
	public String reCalibrate(String outFile, boolean isCover) {
		lsCmdInfo.clear();
		if (!isCover && FileOperate.isFileExistAndBigThanSize(outFile, 0)) {
			return outFile;
		}
		String outFileTmp = FileOperate.changeFileSuffix(outFile, "_tmp", null);
		CmdOperate cmdOperate = new CmdOperate(getLsCmdBaseRecal(outFileTmp));
		cmdOperate.setRedirectOutToTmp(true);
		cmdOperate.addCmdParamOutput(getRecalTableName(outFileTmp));
		try {
			cmdOperate.runWithExp("gatk base recal error");
		} catch (Exception e) {
			throw e;
		} finally {
			lsCmdInfo.add(cmdOperate.getCmdExeStr());
		}
		
		cmdOperate = new CmdOperate(getLsCmdPrintReads(outFileTmp));
		cmdOperate.setRedirectOutToTmp(true);
		cmdOperate.addCmdParamOutput(outFileTmp);
		try {
			cmdOperate.runWithExp("gatk print reads error:");
		} catch (Exception e) {
			throw e;
		} finally {
			lsCmdInfo.add(cmdOperate.getCmdExeStr());
		}
		FileOperate.moveFile(true, outFileTmp, outFile);
		return outFile;
	}
	
	private List<String> getLsCmdBaseRecal(String outFile) {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add("java");
		lsCmd.add("-Xmx4g");
		lsCmd.add("-jar");
		lsCmd.add(exePath + "GenomeAnalysisTK.jar");
		lsCmd.add("-T"); lsCmd.add("BaseRecalibrator");
		ArrayOperate.addArrayToList(lsCmd, getThread());
		ArrayOperate.addArrayToList(lsCmd, getRefSequenceFile());
		ArrayOperate.addArrayToList(lsCmd, getSortedBam());
		ArrayOperate.addArrayToList(lsCmd, getRecalTable(outFile));
		ArrayOperate.addArrayToList(lsCmd, getRecalPdf(outFile));
		lsCmd.addAll(getKnownSite());
		
		return lsCmd;
	}
	
	private String[] getThread() {
		if (threadNum <= 0) return null;
		if (threadNum > 20) threadNum = 20;
		
		return new String[]{"-nct", threadNum + ""};
	}
	
	private List<String> getLsCmdPrintReads(String outFile) {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add("java");
		lsCmd.add("-Xmx10g");
		lsCmd.add("-jar");
		lsCmd.add(exePath + "GenomeAnalysisTK.jar");
		lsCmd.add("-T"); lsCmd.add("PrintReads");
		ArrayOperate.addArrayToList(lsCmd, getThread());
		ArrayOperate.addArrayToList(lsCmd, getRefSequenceFile());
		ArrayOperate.addArrayToList(lsCmd, getSortedBam());
		ArrayOperate.addArrayToList(lsCmd, getUnsafe());
		lsCmd.add("-BQSR"); lsCmd.add(getRecalTableName(outFile));
		ArrayOperate.addArrayToList(lsCmd, getOutRecalibrateBam(outFile));
		
		return lsCmd;
	}
	
	private String[] getRefSequenceFile() {
		return new String[]{"-R", refSequenceFile};
	}
	private String[] getSortedBam() {
		return new String[]{"-I", bamSortedFile};
	}

	private List<String> getKnownSite() {
		List<String> lsKnowSite = new ArrayList<>();
		for (String vcfFile : setSnpDBVcfFilePath) {
			lsKnowSite.add("-knownSites");
			lsKnowSite.add(vcfFile);
		}
		return lsKnowSite;
	}
	
	private String[] getRecalTable(String outFile) {
		return new String[]{"-o", getRecalTableName(outFile)};
	}
	private String[] getRecalPdf(String outFile) {
		return new String[]{"-plots", getRecalTableName(outFile) + ".pdf"};
	}
	private String getRecalTableName(String outFile) {
		return FileOperate.changeFileSuffix(outFile, "_recal_data", "grp");
	}
	
	private String[] getOutRecalibrateBam(String outFile) {
		return new String[]{"-o", outFile};
	}
	@Override
	public List<String> getCmdExeStr() {
		return lsCmdInfo;
	}
	private String[] getUnsafe() {
		if (StringOperate.isRealNull(unsafe) || GATKRealign.SAFE.equalsIgnoreCase(unsafe)) {
			return null;
		}
		return new String[]{"--unsafe", unsafe};
	}
}
