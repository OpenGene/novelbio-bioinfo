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

/**
 * 用samtools来去除pcr duplicate
 * @author zong0jie
 *
 */
public class BamRemoveDuplicate implements IntCmdSoft {
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
	boolean samtools = false;
	List<String> lsCmdInfo = new ArrayList<>();
	
	
	/** 是否使用samtools，默认为true
	 * false则使用picard
	 * @param samtools
	 */
	public void setSamtools(boolean samtools) {
		this.samtools = samtools;
	}

	public void setBamFile(String bamFile) {
		this.bamSortedFile = bamFile;
	}
	
	/** 返回cmd命令 */
	public String removeDuplicate() {
		if (samtools) {
			SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.samtools);
			ExePath = softWareInfo.getExePathRun();
			return removeDuplicateSamtools();
		} else {
			SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.picard);
			ExePath = softWareInfo.getExePathRun();
			return removeDuplicatePicard();
		}
	}
	/** 返回cmd命令 */
	public String removeDuplicate(String outFile) {
		lsCmdInfo.clear();
		if (samtools) {
			return removeDuplicateSamtools(outFile);
		} else {
			return removeDuplicatePicard(outFile);
		}
	}
	/** 返回cmd命令 */
	public String removeDuplicate(String outFile, boolean isCover) {
		lsCmdInfo.clear();
		if (!isCover && FileOperate.isFileExistAndBigThanSize(outFile, 0)) {
			return outFile;
		}
		if (samtools) {
			return removeDuplicateSamtools(outFile);
		} else {
			return removeDuplicatePicard(outFile);
		}
	}
	
	private String removeDuplicateSamtools() {
		String bamNoDuplicateFile = FileOperate.changeFileSuffix(bamSortedFile, "_NoDuplicate", "bam");
		return removeDuplicate(bamNoDuplicateFile);
	}
	
	/**
	 * @param outFile
	 * @return 返回文件名
	 */
	private String removeDuplicateSamtools(String outFile) {
		CmdOperate cmdOperate = new CmdOperate(getLsCmdSamtools(outFile));
		cmdOperate.run();
		if (!cmdOperate.isFinishedNormal()) {
			throw new ExceptionCmd("samtools remove duplicate error:" + cmdOperate.getCmdExeStr());
		}
		lsCmdInfo.add(cmdOperate.getCmdExeStr());
		return outFile;
	}
	
	private List<String> getLsCmdSamtools(String outFile) {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(ExePath + "samtools");
		lsCmd.add("rmdup");
		lsCmd.add(bamSortedFile);
		lsCmd.add(outFile);
		return lsCmd;
	}
	
	private String removeDuplicatePicard() {
		String bamNoDuplicateFile = FileOperate.changeFileSuffix(bamSortedFile, "_NoDuplicate", "bam");
		return removeDuplicate(bamNoDuplicateFile);
	}
	
	/**
	 * @param outFile
	 * @return 返回文件名
	 */
	private String removeDuplicatePicard(String outFile) {
		List<String> lsCmd = getLsCmdPicard(outFile);
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.run();
		if (!cmdOperate.isFinishedNormal()) {
			throw new ExceptionCmd("picard remove duplicate error:\n" + cmdOperate.getCmdExeStrReal());
		}
		lsCmdInfo.add(cmdOperate.getCmdExeStr());
		return outFile;
	}
	
	private List<String> getLsCmdPicard(String outFile) {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add("java");
		ArrayOperate.addArrayToList(lsCmd, getTmpPath());
		lsCmd.add("-Xmx6g");
		lsCmd.add("-jar");
		lsCmd.add(ExePath + "MarkDuplicates.jar");
		
		ArrayOperate.addArrayToList(lsCmd, getInputBam());
		ArrayOperate.addArrayToList(lsCmd, getParam());
		ArrayOperate.addArrayToList(lsCmd, getMETRICS(outFile));
		ArrayOperate.addArrayToList(lsCmd, getOutFile(outFile));
		return lsCmd;
	}
	
	private String[] getInputBam() {
		return  new String[]{"INPUT=" + bamSortedFile};
	}
	private String[] getParam() {
		return new String[]{"REMOVE_DUPLICATES=true", "VALIDATION_STRINGENCY=LENIENT",  "AS=true" };
	}
	private String[] getTmpPath() {
		return new String[]{"-Djava.io.tmpdir=" + PathDetail.getTmpPath()};
	}
	/** duplicate的矩阵 */
	private String[] getMETRICS(String outFile) {
		return new String[]{"METRICS_FILE=" + FileOperate.changeFileSuffix(outFile, "_duplicate", "txt")};
	}
	private String[] getOutFile(String outFile) {
		return new String[]{"OUTPUT=" + outFile};
	}
	@Override
	public List<String> getCmdExeStr() {
		return lsCmdInfo;
	}
}
