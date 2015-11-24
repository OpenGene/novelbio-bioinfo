package com.novelbio.analysis.seq.fasta.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.novelbio.analysis.seq.fasta.ChrSeqHash;
import com.novelbio.analysis.seq.genome.gffOperate.GffGetChrId;
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
	int maxNum;
	Set<String> setChrIdInclude;
	Set<String> setChrIdExclude;
	
	/**
	 * @param setChrId chrId 统统小写
	 */
	public void setIncludeChrId(Set<String> setChrId) {
		this.setChrIdInclude = setChrId;
	}
	
	public void setGffFile(String gffFile) {
		if (!FileOperate.isFileExistAndBigThanSize(gffFile, 0)) return;
		
		GffGetChrId gffGetChrId = new GffGetChrId();
		setChrIdInclude = new HashSet<>();
		TxtReadandWrite txtRead = new TxtReadandWrite(gffFile);
		for (String content : txtRead.readlines()) {
			if (content.startsWith("#")) {
				continue;
			}
			String[] ss = content.split("\t");
			if (ss.length > 4 ) {
				gffGetChrId.getChrID(ss);
				ss[2] = ss[2].toLowerCase();
				if (ss[2].equals("gene") || ss[2].equals("exon") || ss[2].equals("cds")) {
					setChrIdInclude.add(gffGetChrId.getChrID(ss).toLowerCase());
				}
			}
		}
		txtRead.close();
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
	
	public void setMaxNum(int maxNum) {
		this.maxNum = maxNum;
	}
	
	public void setResultSeq(String outSeq) {
		this.outSeq = outSeq;
	}
	
	public void rebuild() {
		if (setChrIdInclude == null) setChrIdInclude = new HashSet<>();
		
		if (StringOperate.isRealNull(outSeq)) {
			outSeq = refseq;
		}
		ChrSeqHash chrSeqHash = new ChrSeqHash(refseq, "");
		Map<String, Long> mapChrId2Len = chrSeqHash.getMapChrLength();
		chrSeqHash.close();
		Set<String> setChrIdByLenAndGff = cutChrIdByLenAndGff(mapChrId2Len);
		Set<String> setChrIdFinal = maxNum > 0 ? cutChrIdBySort(mapChrId2Len, setChrIdByLenAndGff) : setChrIdByLenAndGff;
		
		extractSeq(setChrIdFinal);
		
		SamIndexRefsequence samIndexRefsequence = new SamIndexRefsequence();
		samIndexRefsequence.setRefsequence(outSeq);
		FileOperate.DeleteFileFolder(samIndexRefsequence.getIndexSequence());
		samIndexRefsequence.indexSequence();
	}

	
	protected Set<String> cutChrIdByLenAndGff(Map<String, Long> mapChrId2Len) {
		Set<String> setChrIdLenAndGff = new HashSet<>();
		for (String chrId : mapChrId2Len.keySet()) {
			if (setChrIdExclude != null && setChrIdExclude.contains(chrId)) {
				continue;
			} else if (setChrIdInclude != null && setChrIdInclude.contains(chrId)) {
				setChrIdLenAndGff.add(chrId);
			} else {
				long len = mapChrId2Len.get(chrId);
				if (minLen > 0 && len >= minLen) {
					setChrIdLenAndGff.add(chrId);
				}
			}
		}
		return setChrIdLenAndGff;
	}
	
	/** 将染色体长度从小到大排列，仅保留最长的 {@link #maxNum} 条染色体 */
	protected Set<String> cutChrIdBySort(Map<String, Long> mapChrId2Len, Set<String> setChrIdNeedToFilter) {
		Set<String> setChrId = new HashSet<>();
		setChrId.addAll(setChrIdInclude);
		List<String[]> lsChrId2Len = new ArrayList<>();
		for (String chrId : mapChrId2Len.keySet()) {
			if (!setChrIdNeedToFilter.contains(chrId) || setChrIdInclude.contains(chrId)) {
				continue;
			}
			lsChrId2Len.add(new String[]{chrId, mapChrId2Len.get(chrId) + ""});
		}
		Collections.sort(lsChrId2Len, new Comparator<String[]>() {
			public int compare(String[] o1, String[] o2) {
				Long long1 = Long.parseLong(o1[1]);
				Long long2 = Long.parseLong(o2[1]);
				return -long1.compareTo(long2);
			}
		});
		int i = 1;
		for (String[] string : lsChrId2Len) {
			if (i++ > maxNum - setChrIdInclude.size()) {
				break;
			}
			setChrId.add(string[0]);
		}
		return setChrId;
	}
	
	protected void extractSeq(Set<String> setChrId) {
		String outSeqTmp = FileOperate.changeFileSuffix(outSeq, "_tmp", null);
		TxtReadandWrite txtRead = new TxtReadandWrite(refseq);
		TxtReadandWrite txtWrite = new TxtReadandWrite(outSeqTmp, true);
		boolean isNeed = false;
		for (String content : txtRead.readlines()) {
			if (content.startsWith(">")) {
				content = content.toLowerCase();
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
