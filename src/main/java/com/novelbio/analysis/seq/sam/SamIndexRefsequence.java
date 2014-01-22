package com.novelbio.analysis.seq.sam;

import java.io.IOException;

import com.novelbio.analysis.seq.fasta.ChrSeqHash;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileHadoop;
import com.novelbio.base.fileOperate.FileOperate;

public class SamIndexRefsequence {
	
	String ExePath = "";
	String sequence;
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
	public void setRefsequence(String sequence) {
		this.sequence = sequence;
	}
	
	/** 如果有索引并且索引比文件新，则直接返回 */
	public void indexSequence() {
		String faidx = sequence + ".fai";
		if (FileOperate.isFileExistAndBigThanSize(faidx, 0)) {
			if (FileOperate.getTimeLastModify(sequence) < FileOperate.getTimeLastModify(faidx)) {
				return;
			}
		}
		if (FileHadoop.isHdfs(sequence)) {
			try {
				ChrSeqHash.createIndex(sequence, faidx);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			String cmd = ExePath + "samtools faidx " + "\"" + sequence + "\"";
			CmdOperate cmdOperate = new CmdOperate(cmd,"sortBam");
			cmdOperate.run();
		}
	}

}
