package com.novelbio.analysis.seq.fasta;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.PatternOperate;

public class ChrIndexDecoder {

	/**
	 * 每个文本所对应的单行长度
	 *  Seq文件第二行的长度，也就是每行序列的长度+1，1是回车 
	 * 现在是假设Seq文件第一行都是>ChrID,第二行开始都是Seq序列信息
	 * 并且每一行的序列都等长<br>
	 *  <b>key小写</b>
	 */
	Map<String, Integer> mapChrId2LenRow = new LinkedHashMap<>();
	/**
	 * 行中内容加上换行符和空格等<br>
	 * <b>key小写</b>
	 */
	Map<String, Integer> mapChrId2LenRowEnter = new LinkedHashMap<>();
	
	/** key小写，value 正常 */
	Map<String, String> mapChrIdLowcase2ChrId = new LinkedHashMap<>();
	
	/** 以下哈希表的键是染色体名称，都是小写，格式如：chr1，chr2，chr10 */
	Map<String, Long> mapChrId2Start = new LinkedHashMap<>();
	
	/**
	 * 保存chrID和chrLength的对应关系<br>
	 * <b>key小写</b>
	 */
	LinkedHashMap<String, Long> mapChrID2Length = new LinkedHashMap<String, Long>();
	
	/** index 文件格式如下
	 * chrID chrLength start    rowLength rowLenWithEnter
	 * @throws IOException 
	 */
	private void readIndex(String indexFile, String regx) {
		mapChrId2Start.clear();
		mapChrIdLowcase2ChrId.clear();
		mapChrId2LenRow.clear();
		mapChrId2LenRowEnter.clear();
		PatternOperate patternOperate = null;
		if (regx != null && !regx.equals("") && !regx.equals(" ")) {
			patternOperate = new PatternOperate(regx, false);
		}
		
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
			long start = Long.parseLong(ss[2].trim());
			int lenRow = Integer.parseInt(ss[3].trim());
			int lenRowEnter = Integer.parseInt(ss[4].trim());
			mapChrId2LenRow.put(chrIDlowcase, lenRow);
			mapChrId2Start.put(chrIDlowcase, start);
			mapChrID2Length.put(chrIDlowcase, length);
			mapChrId2LenRowEnter.put(chrIDlowcase, lenRowEnter);
		}
		txtRead.close();
	}
	
	public long getChrStart(String chrId) {
		return mapChrId2Start.get(chrId.toLowerCase());
	}
}
