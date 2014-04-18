package com.novelbio.analysis.seq.sam;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.base.PathDetail;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.cmd.ExceptionCmd;
import com.novelbio.base.dataStructure.ArrayOperate;
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
	
	String ExePath = "";
	String refSequenceFile;
	String bamSortedFile;
	private String unsafe = GATKRealign.ALL;
	List<String> lsCmdInfo = new ArrayList<>();
	
	public BamRealign() {
		SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.GATK);
		this.ExePath = softWareInfo.getExePathRun();
	}
	public void setRefSequenceFile(String refSequencFile) {
		this.refSequenceFile = refSequencFile;
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
		cmdOperate.run();
		if (!cmdOperate.isFinishedNormal()) {
			throw new ExceptionCmd("realign error:\n" + cmdOperate.getCmdExeStrReal());
		}
		lsCmdInfo.add(cmdOperate.getCmdExeStr());
		cmdOperate = new CmdOperate(getLsCmdIndelRealign(outFileTmp));
		cmdOperate.run();
		if (!cmdOperate.isFinishedNormal()) {
			FileOperate.DeleteFileFolder(getOutIntervalFile(outFileTmp)[1]);
			FileOperate.DeleteFileFolder(outFileTmp);
			throw new ExceptionCmd("realign error:\n" + cmdOperate.getCmdExeStrReal());
		}
		FileOperate.moveFile(true, outFileTmp, outFile);
		FileOperate.moveFile(true, FileOperate.getFileNameSep(outFileTmp)[0] + ".bai", outFile + ".bai");
		FileOperate.moveFile(true, FileOperate.getFileNameSep(outFileTmp)[0] + ".intervals", FileOperate.getFileName(outFile) + ".intervals");
		lsCmdInfo.add(cmdOperate.getCmdExeStr());
		return outFile;
	}
	
	private List<String> getLsCmdCreator(String outFile) {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add("java");
		ArrayOperate.addArrayToList(lsCmd, getTmpPath());
		lsCmd.add("-Xmx4g");
		lsCmd.add("-jar");
		lsCmd.add(ExePath + "GenomeAnalysisTK.jar");
		lsCmd.add("-T"); lsCmd.add("RealignerTargetCreator");
		ArrayOperate.addArrayToList(lsCmd, getRefSequenceFile());
		ArrayOperate.addArrayToList(lsCmd, getSortedBam());
		ArrayOperate.addArrayToList(lsCmd, getOutIntervalFile(outFile));
		return lsCmd;
	}
	
	private List<String> getLsCmdIndelRealign(String outFile) {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add("java");
		ArrayOperate.addArrayToList(lsCmd, getTmpPath());
		lsCmd.add("-Xmx10g");
		lsCmd.add("-jar");
		lsCmd.add(ExePath + "GenomeAnalysisTK.jar");
		lsCmd.add("-T"); lsCmd.add("IndelRealigner");
		ArrayOperate.addArrayToList(lsCmd, getRealignType());
		ArrayOperate.addArrayToList(lsCmd, getRefSequenceFile());
		ArrayOperate.addArrayToList(lsCmd, getSortedBam());
		ArrayOperate.addArrayToList(lsCmd, getInIntervalFile(outFile));
		ArrayOperate.addArrayToList(lsCmd, getOutRealignBam(outFile));
		ArrayOperate.addArrayToList(lsCmd, getUnsafe());
		return lsCmd;
	}
	
	private String[] getRefSequenceFile() {
		return new String[]{"-R", refSequenceFile};
	}
	private String[] getSortedBam() {
		return new String[]{"-I", bamSortedFile};
	}
	private String[] getRealignType() {
		return new String[]{"--consensusDeterminationModel", "USE_SW"};
	}
	private String[] getOutIntervalFile(String outFile) {
		return new String[]{"-o", FileOperate.changeFileSuffix(outFile, "", "intervals")};
	}

	private String[] getTmpPath() {
		return new String[]{"-Djava.io.tmpdir=" + PathDetail.getTmpPath()};
	}
	private String[] getInIntervalFile(String outFile) {
		return new String[]{"-targetIntervals", FileOperate.changeFileSuffix(outFile, "", "intervals")};
	}
	private String[] getOutRealignBam(String outFile) {
		return new String[]{"-o", outFile};
	}
	private String[] getUnsafe() {
		return new String[]{"--unsafe", unsafe};
	}
	@Override
	public List<String> getCmdExeStr() {
		return lsCmdInfo;
	}
}
