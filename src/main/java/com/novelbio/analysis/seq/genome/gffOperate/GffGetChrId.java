package com.novelbio.analysis.seq.genome.gffOperate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.PatternOperate;

/** 从gff文件中获取转换过的chrId信息 */
public class GffGetChrId {
	private static final Logger logger = Logger.getLogger(GffGetChrId.class);
	
	/**
	 * key: 类似NC_XXX，为小写
	 * value: 类似chr1
	 */
	Map<String, String> mapId2ChrId = new HashMap<>();
	
	Set<String> setChrIdAll = new HashSet<>();
	
	public boolean isHaveChrId(String chrId) {
		return setChrIdAll.contains(chrId.toLowerCase());
	}
	
	/** 把给定的chrId做一个转换，转化成gff修正后的chrId，如将NC_123转化成chr1 */
	public String getChrId(String chrId) {
		String chrIdNew = mapId2ChrId.get(chrId.toLowerCase());
		if (chrIdNew == null) {
			chrIdNew = chrId;
		}
		return chrIdNew;
	}
	
	/**
	 * 逐行输入，然后就可以返回具体的Id
	 * 输入NC编号，返回染色体ID
	 * @param ss
	 * @return
	 */
	public String getChrID(String[] ss) {
		String chrID = mapId2ChrId.get(ss[0].toLowerCase());
		if (chrID != null) return chrID;
		if (ss[0].toLowerCase().startsWith("chr")) {
			mapId2ChrId.put(ss[0].toLowerCase(), ss[0].toLowerCase());
			setChrIdAll.add(ss[0].toLowerCase());
			return ss[0].toLowerCase();
		}

		try {
			if (ss[2].equals("region")) {
				String regxChrID = "(?<=chromosome\\=)[\\w\\.\\-%\\:]+";
				String regxName = "(?<=Name\\=)[\\w\\.\\-%\\:]+";
				String ss8Lowcase = ss[8].toLowerCase();
				if (ss8Lowcase.contains("genome=genomic")) {
					chrID = ss[0];
				} else if (ss8Lowcase.contains("genome=mitochondrion")) {
					chrID = "chrm";
				} else if (ss8Lowcase.contains("genome=chloroplast")) {
					chrID = "chrc";
				} else if (ss8Lowcase.contains("genome=unknown") || ss8Lowcase.contains("genome=un")
						|| (!ss8Lowcase.contains("chromosome=") && ss8Lowcase.contains("name=anonymous"))) {
					chrID = ss[0];
				} else {
					List<String[]> lsRegx = PatternOperate.getPatLoc(ss[8], regxChrID, false);
					if (lsRegx.isEmpty()) lsRegx = PatternOperate.getPatLoc(ss[8], regxName, false);
					try {
						String chrName = lsRegx.get(0)[0];
						if (chrName.startsWith("NC_")) {
							chrID = chrName;
						} else {
							chrID = "chr" + chrName;
						}
					} catch (Exception e) {
						logger.error("本位置出错，错误的region，本来一个region应该是一个染色体，这里不知道是什么 " + ArrayOperate.cmbString(ss, "\t"));
						chrID = ss[0];
					}
				}
				setChrIdAll.add(ss[0].toLowerCase());
				setChrIdAll.add(chrID.toLowerCase());

				mapId2ChrId.put(ss[0].toLowerCase(), chrID);
			}
		} catch (Exception e) {
		}
		String chrIDResult = mapId2ChrId.get(ss[0].toLowerCase());
		if (chrIDResult == null) {
			mapId2ChrId.put(ss[0].toLowerCase(), ss[0]);
			setChrIdAll.add(ss[0].toLowerCase());
			chrIDResult = ss[0];
		}
		return chrIDResult;
	}
	
	public void clear() {
		mapId2ChrId.clear();
	}
	
}
