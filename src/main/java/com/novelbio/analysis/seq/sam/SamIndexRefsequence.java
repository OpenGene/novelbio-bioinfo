package com.novelbio.analysis.seq.sam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.cmd.ExceptionCmd;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.PatternOperate;
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
	
	public String getIndexSequence() {
		return sequence + ".fai";
	}
	
	/** 如果有索引并且索引比文件新，则直接返回
	 * @return 返回建好的索引文件名
	 */
	public String indexSequence() {
		String faidx = getIndexSequence();
		if (FileOperate.isFileExistAndBigThanSize(faidx, 0)) {
//			if (FileOperate.getTimeLastModify(sequence) <= FileOperate.getTimeLastModify(faidx)) {
//			
//			}
			return faidx;
		}
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(ExePath + "samtools");
		lsCmd.add("faidx");
		lsCmd.add(sequence);
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.setRedirectInToTmp(true);
		cmdOperate.setRedirectOutToTmp(true);
		cmdOperate.addCmdParamInput(sequence);
		cmdOperate.addCmdParamOutput(sequence);
		cmdOperate.run();

		if (!cmdOperate.isFinishedNormal()) {
			throw new ExceptionCmd("make index error:" + cmdOperate.getCmdExeStrReal() + "\n" + cmdOperate.getErrOut());
		}
		return faidx;
	}
	
	/** 读取产生的fai文件，并返回chrId和Length的值
	 * @param indexFile
	 * @return key都为小写
	 */
	public static Map<String, Long> getMapChrId2Len(String indexFile) {
		Map<String, Long> mapChrId2Len = new HashMap<>();
		PatternOperate patternOperate = null;
		String regx = null;
		
		TxtReadandWrite txtRead = new TxtReadandWrite(indexFile);
		for (String string : txtRead.readlines()) {
			String[] ss = string.split("\t");
			String chrID = null;
			if (" ".equals(regx)) {
				chrID = ss[0].split(" ")[0];
			} else if (patternOperate != null) {
				chrID = patternOperate.getPatFirst(ss[0]);
				if (chrID == null) {
					chrID = ss[0];
				}
			} else {
				chrID = ss[0];
			}
			String chrIDlowcase = chrID.toLowerCase();
			long length = Long.parseLong(ss[1].trim());
			mapChrId2Len.put(chrIDlowcase, length);
		}
		txtRead.close();
		return mapChrId2Len;
	}
	
	public static String getIndexFile(String seqFile) {
		return seqFile + ".fai";
	}
	
	/** 产生并读取fai文件，并返回chrId和Length的值
	 * @param indexFile
	 * @return key都为小写
	 */
	public static Map<String, Long> generateIndexAndGetMapChrId2Len(String seqFile) {
		String indexFile = SamIndexRefsequence.getIndexFile(seqFile);
		if (!FileOperate.isFileExistAndBigThan0(indexFile)) {
			SamIndexRefsequence samIndexRefsequence = new SamIndexRefsequence();
			samIndexRefsequence.setRefsequence(seqFile);
			samIndexRefsequence.indexSequence();
        }
		return SamIndexRefsequence.getMapChrId2Len(indexFile);
	}
}
