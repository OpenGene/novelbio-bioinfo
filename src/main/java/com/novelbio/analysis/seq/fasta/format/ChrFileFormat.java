package com.novelbio.analysis.seq.fasta.format;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.novelbio.analysis.seq.fasta.ChrSeqHash;
import com.novelbio.analysis.seq.sam.SamIndexRefsequence;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 如果序列大于2000条，就将序列中长度小于 指定长度 的序列删除， 并重建 fai文件
 * @author novelbio
 *
 */
public class ChrFileFormat {
	String refseq;
	
	PatternOperate patternOperate = null;
	String regex;
	
	String outSeq;
	int minLen;
	Set<String> setChrIdInclude;
	Set<String> setChrIdExclude;
	
	public void setIncludeChrId(Set<String> setChrId) {
		this.setChrIdInclude = setChrId;
	}
	
	public void setRefSeq(String refSeq) {
		this.refseq = refSeq;
	}
	
	/** 输入正则表达式来抓取序列，类似 chr\\d+ 这种 */
	public void setRegex(String regex) {
		this.regex = regex;
		if (regex != null && !regex.equals("") && !regex.equals(" ")) {
			patternOperate = new PatternOperate(regex, false);
		} else {
			patternOperate = null;
		}
	}
	
	public void setMinLen(int minLen) {
		this.minLen = minLen;
	}
	
	public void setOutSeq(String outSeq) {
		this.outSeq = outSeq;
	}
	
	public void rebuild() {
		if (StringOperate.isRealNull(outSeq)) {
			outSeq = refseq;
		}
		Set<String> setChrIdFinal = getSetChrIdFinal();
		extractSeq(setChrIdFinal);
		
		SamIndexRefsequence samIndexRefsequence = new SamIndexRefsequence();
		samIndexRefsequence.setRefsequence(outSeq);
		FileOperate.DeleteFileFolder(samIndexRefsequence.getIndexSequence());
		samIndexRefsequence.indexSequence();
	}

	
	private Set<String> getSetChrIdFinal() {
		Set<String> setChrIdFinal = new HashSet<>();
		ChrSeqHash chrSeqHash = new ChrSeqHash(outSeq, "");
		Map<String, Long> mapChrId2Len = chrSeqHash.getMapChrLength();
		for (String chrId : mapChrId2Len.keySet()) {
			if (setChrIdExclude.contains(chrId)) {
				continue;
			} else if (setChrIdInclude.contains(chrId)) {
				setChrIdFinal.add(chrId);
			} else {
				long len = mapChrId2Len.get(chrId);
				if (len >= minLen) {
					setChrIdFinal.add(chrId);
				}
			}
		}
		chrSeqHash.close();
		return setChrIdFinal;
	}
	
	private void extractSeq(Set<String> setChrId) {
		String outSeqTmp = FileOperate.changeFileSuffix(outSeq, "_tmp", null);
		TxtReadandWrite txtRead = new TxtReadandWrite(refseq);
		TxtReadandWrite txtWrite = new TxtReadandWrite(outSeqTmp, true);
		boolean isNeed = false;
		for (String content : txtRead.readlines()) {
			if (content.startsWith(">")) {
				String chrId = getChrId(content);
				content = ">" + chrId;
				if (setChrId.contains(chrId)) {
					isNeed = true;
				} else {
					isNeed = false;
				}
			}
			
			if (isNeed) {
				txtWrite.writefileln(content);
			}
		}
		
		txtRead.close();
		txtWrite.close();
		
		if (outSeq.equals(refseq)) {
			FileOperate.moveFile(true, refseq, FileOperate.changeFileSuffix(refseq, "_Raw", null));
		}
		FileOperate.moveFile(true, outSeqTmp, outSeq);

	}
	
	protected String getChrId(String chrId) {
		chrId = chrId.replace(">", "").trim();
		String chrID = null;
		if (" ".equals(regex)) {
			chrID = chrId.split(" ")[0];
		} else if (patternOperate != null) {
			chrID = patternOperate.getPatFirst(chrId);
			if (chrID == null) {
				chrID = chrId;
			}
		} else {
			chrID = chrId;
		}
		return chrID;
	}

}
