package com.novelbio.analysis.seq.sam;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.cmd.ExceptionCmd;
import com.novelbio.base.fileOperate.FileHadoop;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;

public class SamIndexRefsequence {
	
	String ExePath = "";
	String sequence;
	
	public SamIndexRefsequence() {
		SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.samtools);
		ExePath = softWareInfo.getExePathRun();
	}
	
	public void setRefsequence(String sequence) {
		this.sequence = sequence;
	}
	
	/** 如果有索引并且索引比文件新，则直接返回
	 * @return 返回建好的索引文件名
	 */
	public String indexSequence() {
		String faidx = sequence + ".fai";
		if (FileOperate.isFileExistAndBigThanSize(faidx, 0)) {
			if (FileOperate.getTimeLastModify(sequence) < FileOperate.getTimeLastModify(faidx)) {
				return faidx;
			}
		}
		String sequenceLocal = sequence;
		if (FileHadoop.isHdfs(sequence)) {
			sequenceLocal = FileHadoop.convertToLocalPath(sequence);
		}
		
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(ExePath + "samtools");
		lsCmd.add("faidx");
		lsCmd.add(sequenceLocal);			
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.run();
		if (!cmdOperate.isFinishedNormal()) {
			throw new ExceptionCmd("make index error:" + cmdOperate.getCmdExeStrReal() + "\n" + cmdOperate.getErrOut());
		}
		return faidx;
	}
	
}
