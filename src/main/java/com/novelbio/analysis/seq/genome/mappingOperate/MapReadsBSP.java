package com.novelbio.analysis.seq.genome.mappingOperate;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReads.ChrMapReadsInfo;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReadsChangFang.EnumCpGmethyType;
import com.novelbio.base.ExceptionNbcParamError;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.Alignment;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 给马红那边，杨红星生成的
 * @author zong0jie
 *
 */
public class MapReadsBSP extends MapReadsAbs {
	public static void main(String[] args) {
		logger.debug("a");
		logger.info("a");
	}
	private static Logger logger = Logger.getLogger(MapReadsBSP.class);
	
	Map<String, ChrMapReadsInfo> mapChrID2ReadsInfo = new HashMap<>();
	String bedFileBSP;
	
	 /** 序列信息,名字都为小写 */
	 protected Map<String, Long> mapChrID2Len = new HashMap<String, Long>();
	 
	/** 这里是bsp专门软件出来的格式
	 * chr7	3000254	-	0	0	CHH	CTC
	 */
	public void setReadsInfoFile(String bedFileBSP) {
		this.bedFileBSP = bedFileBSP;
	}
	 
	 public void setChrFai(String chrFai) {
		 if (!FileOperate.isFileExistAndBigThanSize(chrFai, 0)) {
			 throw new ExceptionNbcParamError("must need file chrFai, but file " + chrFai + " is not exist");
		 }
		 mapChrID2Len = new LinkedHashMap<>();
		 TxtReadandWrite txtRead = new TxtReadandWrite(chrFai);
		 for (String content : txtRead.readlines()) {
			 String[] ss = content.split("\t");
			 mapChrID2Len.put(ss[0].toLowerCase(), Long.parseLong(ss[1]));
		 }
		 txtRead.close();
	 }
	 
	@Override
	protected void ReadMapFileExp() throws Exception {
		
		int[] chrBpReads = null;//保存每个bp的reads累计数
		String lastChr="";
		ChrMapReadsInfo chrMapReadsInfo = null;
		boolean flag = true;// 当没有该染色体时标记为false并且跳过所有该染色体上的坐标
		
		TxtReadandWrite txtCpGReader = new TxtReadandWrite(bedFileBSP, false);
		for (String cpGLines : txtCpGReader.readlines()) {
			CpGInfo cpGInfo = new CpGInfo(cpGLines);
			String tmpChrID = cpGInfo.getRefID().toLowerCase();
			if (!tmpChrID.equals(lastChr)) {
				if (chrMapReadsInfo != null) {
					chrMapReadsInfo.sumChrBp(chrBpReads, 1);
				}
				lastChr = tmpChrID;// 实际这是新出现的ChrID
				logger.info(lastChr);
				
				Long chrLength = mapChrID2Len.get(lastChr.toLowerCase());
				flag = true;
				if (chrLength == null) {
					logger.error("find unknown chrId "+lastChr);
					flag = false; continue;
				}

				chrBpReads = new int[(int) (chrLength + 1)];// 同样为方便，0位记录总长度。这样实际bp就是实际长度
				chrBpReads[0] = chrLength.intValue();
				chrMapReadsInfo = mapChrID2ReadsInfo.get(lastChr);
				if (chrMapReadsInfo == null) {
					chrMapReadsInfo = new ChrMapReadsInfo(lastChr, chrLength, 1);
					mapChrID2ReadsInfo.put(lastChr, chrMapReadsInfo);
				}
			}
			if (!flag) continue;
			chrBpReads[cpGInfo.getStartAbs()] = cpGInfo.encodeCpg2Int();
			
		}
		
		if (flag) {
			chrMapReadsInfo.sumChrBp(chrBpReads, 1);
		}
		txtCpGReader.close();
	}

	@Override
	public double[] getReadsDensity(String chrID, int startLoc, int endLoc, int binNum) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] getRangeInfo(int thisInvNum, String chrID, int startNum, int endNum, int type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected double[] getRangeInfo(String chrID, int startNum, int endNum, int binNum, int type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected long getAllReadsNum() {
		return 0;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}
	

}
/**
 * 最后输出可以这么来
 * ============= 方法一 ==========================
 * 1 99999 999 9
 * 第一位和第三位组成 百分比，精确到小数点后1位。
 * 其中如果为100%，则第一位为1，否则第一位为0
 * 
 * 第二位是覆盖度
 * 第四位是CG类型
 * ============= 方法二  ================================
 * 9999 9999 9
 * 第一位 甲基化覆盖度
 * 第二位 非甲基化覆盖度
 * 第三位 CG类型
 * =======================
 * 目前选择方法二
 * @author zong0jie
 * @data 2017年8月31日
 */
class CpGInfo implements Alignment {
	/** 最大覆盖度为9999，不能乱改，因为表示了四位数的最大值 */
	static final int MaxDepth = 9999;
	String chrId;
	int startEnd;
	boolean isCis5To3;
	/** 该位点的甲基化覆盖度 */
	int depthMethy;
	/** 该位点的非甲基化覆盖度 */
	int depthNonMethy;
	EnumCpGmethyType enumCpGmethyType;
	
	private CpGInfo() {};
	
	@VisibleForTesting
	protected void setChrId(String chrId) {
		this.chrId = chrId;
	}
	@VisibleForTesting
	protected void setStartEnd(int startEnd) {
		this.startEnd = startEnd;
	}
	/**
	 * 输入行类似
	 * Chr7 3000254 - 0 0 CHH CTC
	 * chrom position strand methCount non-methCount C-context detail
	 */
	public CpGInfo(String line) {
		String[] ss = line.split("\t");
		chrId = ss[0];
		startEnd = Integer.parseInt(ss[1]);
		isCis5To3 = StringOperate.isEqual(ss[2].trim(), "+");
		depthMethy = Integer.parseInt(ss[3]);
		depthNonMethy = Integer.parseInt(ss[4]);
		int depthBiger = Math.max(depthMethy, depthNonMethy);
		int depthSmall = Math.min(depthMethy, depthNonMethy);
		if (depthBiger > MaxDepth) {
			depthSmall = (int) Math.round(((double)depthSmall* MaxDepth/depthBiger ));
			if (depthMethy < depthNonMethy) {
				depthMethy = depthSmall;
				depthNonMethy = MaxDepth;
			} else {
				depthMethy = MaxDepth;
				depthNonMethy = depthSmall;
			}
		}
		enumCpGmethyType = EnumCpGmethyType.valueOf(ss[5]);
	}
	
	@Override
	public int getStartAbs() {
		return startEnd;
	}

	@Override
	public int getEndAbs() {
		return startEnd;
	}

	@Override
	public int getStartCis() {
		return startEnd;
	}

	@Override
	public int getEndCis() {
		return startEnd;
	}

	@Override
	public Boolean isCis5to3() {
		return isCis5To3;
	}

	@Override
	public int getLength() {
		return 1;
	}

	@Override
	public String getRefID() {
		return chrId;
	}
	
	/** 用int表示的经过编码的数字
	 * 一定不为0
	 * @return
	 */
	public int encodeCpg2Int() {
		int result = depthMethy*100000 + depthNonMethy * 10 + EnumCpGmethyType.getCGFlag(enumCpGmethyType);
		if (!isCis5To3) result = -result;
		return result;
	}
	
	/** 给定编码好的CpG的int型数字，把具体的信息提取出来 */
	public static CpGInfo decodeInt2Cpg(int info) {
		CpGInfo cpGInfo = new CpGInfo();
		//理论上不会发生这种为0的情况
		if (info == 0) {
			throw new RuntimeException("info is 0 and cannot be decoder, please check");
		}
		cpGInfo.isCis5To3 = info > 0;
		info = Math.abs(info);
		cpGInfo.depthMethy = info/100000;
		info = info - (cpGInfo.depthMethy * 100000);
		cpGInfo.depthNonMethy = info/10;
		info = info - (cpGInfo.depthNonMethy * 10);
		cpGInfo.enumCpGmethyType = EnumCpGmethyType.getCGType(info);
		return cpGInfo;
	}
	
}
