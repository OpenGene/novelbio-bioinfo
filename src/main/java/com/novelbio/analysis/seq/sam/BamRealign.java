package com.novelbio.analysis.seq.sam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.base.ExceptionNullParam;
import com.novelbio.base.ExceptionNbcParamError;
import com.novelbio.base.PathDetail;
import com.novelbio.base.StringOperate;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.cmd.ExceptionCmd;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.ExceptionNbcFile;
import com.novelbio.base.fileOperate.ExceptionNbcFileInputNotExist;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;

public class BamRealign implements IntCmdSoft {
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
	
	String exePath = "";
	String refSequenceFile;
	String bamSortedFile;
	private String unsafe = GATKRealign.ALL;
	private int threadNum = 8;
	
	/** 输入文件路径+vcf文件名 */
	private Set<String> setSnpDBVcfFilePath = new HashSet<String>();
	
	List<String> lsCmdInfo = new ArrayList<>();
	
	public BamRealign() {
		SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.GATK);
		this.exePath = softWareInfo.getExePathRun();
	}
	public void setRefSequenceFile(String refSequencFile) {
		ExceptionNbcFileInputNotExist.validateFile(refSequencFile, "Realign cannot run without a refSequencFile");
		
		this.refSequenceFile = refSequencFile;
	}
	
	/** @param snpVcfFile 已知的snpdb等文件，用于校正。可以添加多个 */
	public void addSnpVcfFile(String snpVcfFile) {
		if (FileOperate.isFileExistAndBigThanSize(snpVcfFile, 0)) {
			setSnpDBVcfFilePath.add(snpVcfFile);
		}
	}
	
	/** @param snpVcfFile 已知的snpdb等文件，用于校正。可以添加多个  */
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
	
	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum;
	}
	/** 默认为all */
	public void setUnsafe(String unsafe) {
		this.unsafe = unsafe;
	}
	
	public void setBamFile(String bamFile) {
		this.bamSortedFile = bamFile;
	}
	public String realign() {
		String bamRealignFile = FileOperate.changeFileSuffix(bamSortedFile, "_realign", "bam");
		return realign(bamRealignFile, false);
	}
	/** 是否覆盖 */
	public String realign(String outFile, boolean isCover) {
		lsCmdInfo.clear();
		if (!isCover && FileOperate.isFileExistAndBigThanSize(outFile, 0)) {
			return outFile;
		}
		String outFileTmp = FileOperate.changeFileSuffix(outFile, "_tmp", null);
		
		CmdOperate cmdOperate = new CmdOperate(getLsCmdCreator(outFileTmp));
		cmdOperate.setRedirectOutToTmp(true);
		cmdOperate.addCmdParamOutput(getOutInterVals(outFileTmp));
		try {
			cmdOperate.runWithExp("realign error:\n");
		} catch (Exception e) {
			throw e;
		} finally {
			lsCmdInfo.add(cmdOperate.getCmdExeStr());
		}
		
		cmdOperate = new CmdOperate(getLsCmdIndelRealign(outFileTmp));
		cmdOperate.setRedirectOutToTmp(true);
		cmdOperate.addCmdParamOutput(outFileTmp);
		
		try {
			cmdOperate.runWithExp("realign error:");
		} catch (Exception e) {
			FileOperate.deleteFileFolder(getOutIntervalFile(outFileTmp)[1]);
			FileOperate.deleteFileFolder(outFileTmp);
			throw e;
		} finally {
			lsCmdInfo.add(cmdOperate.getCmdExeStr());
		}
		
		FileOperate.moveFile(true, outFileTmp, outFile);
		FileOperate.moveFile(true, FileOperate.getFileNameSep(outFileTmp)[0] + ".bai", outFile + ".bai");
		FileOperate.moveFile(true, FileOperate.getFileNameSep(outFileTmp)[0] + ".intervals", FileOperate.getFileName(outFile) + ".intervals");
	
		return outFile;
	}
	
	private List<String> getLsCmdCreator(String outFile) {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add("java");
		ArrayOperate.addArrayToList(lsCmd, getTmpPath());
		lsCmd.add("-Xmx4g");
		lsCmd.add("-jar");
		lsCmd.add(exePath + "GenomeAnalysisTK.jar");
		lsCmd.add("-T"); lsCmd.add("RealignerTargetCreator");

		ArrayOperate.addArrayToList(lsCmd, getRefSequenceFile());
		ArrayOperate.addArrayToList(lsCmd, getSortedBam());
		ArrayOperate.addArrayToList(lsCmd, getThread());
		ArrayOperate.addArrayToList(lsCmd, getOutIntervalFile(outFile));
		lsCmd.addAll(getKnownSite());
		return lsCmd;
	}
	
	private List<String> getLsCmdIndelRealign(String outFile) {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add("java");
		ArrayOperate.addArrayToList(lsCmd, getTmpPath());
		lsCmd.add("-Xmx10g");
		lsCmd.add("-jar");
		lsCmd.add(exePath + "GenomeAnalysisTK.jar");
		lsCmd.add("-T"); lsCmd.add("IndelRealigner");
		ArrayOperate.addArrayToList(lsCmd, getRealignType());
		ArrayOperate.addArrayToList(lsCmd, getRefSequenceFile());
		ArrayOperate.addArrayToList(lsCmd, getSortedBam());
		ArrayOperate.addArrayToList(lsCmd, getInIntervalFile(outFile));
		ArrayOperate.addArrayToList(lsCmd, getOutRealignBam(outFile));
		ArrayOperate.addArrayToList(lsCmd, getUnsafe());
		lsCmd.addAll(getKnownSite());
		return lsCmd;
	}
	
	private List<String> getKnownSite() {
		List<String> lsKnowSite = new ArrayList<>();
		for (String vcfFile : setSnpDBVcfFilePath) {
			lsKnowSite.add("-known");
			lsKnowSite.add(vcfFile);
		}
		return lsKnowSite;
	}
	
	/** 仅用于 RealignerTargetCreator */
	private String[] getThread() {
		if (threadNum <= 0) return null;
		if (threadNum > 20) threadNum = 20;
		
		return new String[]{"-nt", threadNum + ""};
	}
	
	private String[] getRefSequenceFile() {
		return new String[]{"-R", refSequenceFile};
	}
	private String[] getSortedBam() {
		return new String[]{"-I", bamSortedFile};
	}
	private String[] getRealignType() {
		return new String[]{"--consensusDeterminationModel", "USE_READS"};
	}
	private String[] getOutIntervalFile(String outFile) {
		return new String[]{"-o", getOutInterVals(outFile)};
	}

	private String[] getTmpPath() {
		return new String[]{"-Djava.io.tmpdir=" + PathDetail.getTmpPathWithOutSep()};
	}
	private String[] getInIntervalFile(String outFile) {
		return new String[]{"-targetIntervals", getOutInterVals(outFile)};
	}
	
	private String getOutInterVals(String outFile) {
		return FileOperate.changeFileSuffix(outFile, "", "intervals");
	}
	
	private String[] getOutRealignBam(String outFile) {
		return new String[]{"-o", outFile};
	}
	private String[] getUnsafe() {
		if (StringOperate.isRealNull(unsafe) || GATKRealign.SAFE.equalsIgnoreCase(unsafe)) {
			return null;
		}
		return new String[]{"--unsafe", unsafe};
	}
	@Override
	public List<String> getCmdExeStr() {
		return lsCmdInfo;
	}
}
