package com.novelbio.bioinfo.mappedreads;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.Alignment;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.bioinfo.mappedreads.MapReads.ChrMapReadsInfo;
import com.novelbio.bioinfo.mappedreads.MapReadsBSP.EnumCpGmethyType;

/**
 * 给马红那边，杨红星生成的
 * @author zong0jie
 *
 */
public class MapReadsBSP extends MapReadsAbs {
	private static final Logger logger = LoggerFactory.getLogger(MapReadsBSP.class);
	
	public static enum EnumBspCpGCalculateType {
		CpGCoverage,
		CpGRatio,
	}
	
	public static enum EnumCpGmethyType {
		CHG, CG, CHH, ALL;
		
		static BiMap<Integer, EnumCpGmethyType> biMapIntFlag2Type = HashBiMap.create();
		static {
			/**
			 * 这个编码里面必须大于0，因为后面还有要覆盖度之类的，
			 * 9999 9999 0
			 * 前四位甲基化覆盖度，中间四位非甲基化覆盖度，最后一位编码，正负号表示方向。
			 * 如果都为0，就无法区分正负号了
			 */
			biMapIntFlag2Type.put(1, CG);
			biMapIntFlag2Type.put(2, CHG);
			biMapIntFlag2Type.put(3, CHH);

		}
		public static EnumCpGmethyType getCGType(int value) {
			return biMapIntFlag2Type.get(value);
		}
		public static int getCGFlag(EnumCpGmethyType type) {
			return biMapIntFlag2Type.inverse().get(type);
		}
		public static int getCGFlag(String type) {
			EnumCpGmethyType typeCGmethy = EnumCpGmethyType.valueOf(type);
			return getCGFlag(typeCGmethy);
		}
	}
	
	CpGCalculator cpGCalculator;
	
	Map<String, ChrMapReadsInfo> mapChrID2ReadsInfo = new HashMap<>();
	String bedFileBSP;

	 
	 /**
	  * 仅对方法{@link #getReadsDensity(String, int, int, int)}起作用<br>
	  * 只需要在调用该方法之前设置好本参数即可
	  * @param cpGCalculator
	  */
	 public void setCpGCalculator(CpGCalculator cpGCalculator) {
		this.cpGCalculator = cpGCalculator;
	}
	
	 @Deprecated
	 public void setAllReadsNum(long allReadsNum) {}
	 /** 是否仅考虑unique mapping的reads */
	 @Deprecated
	 public void setisUniqueMapping(boolean booUniqueMapping) {}
	 
	/** 这里是bsp专门软件出来的格式
	 * chr7	3000254	-	0	0	CHH	CTC
	 */
	public void setReadsInfoFile(String bedFileBSP) {
		this.bedFileBSP = bedFileBSP;
	}
	/** 在外部自己做标准化 */
	@Deprecated
	 public void setNormalType(EnumMapNormalizeType normalType) {
		NormalType = normalType;
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
	
	Map<String, int[]> mapChrId2NumCannotFind = new HashMap<>();
	
	/**
	 * 查看全染色体分布的
	 */
	@Override
	public double[] getReadsDensity(String chrID, int startLoc, int endLoc, int binNum) {
		double[] tmpResult = getRangeInfo(1, chrID, startLoc, endLoc, 0);
		return cpGCalculator.calculateCpGInfo(tmpResult, binNum);
	}
	
	/**
	 * 经过标准化，和equations修正
	 * 输入坐标区间，和每个区间的bp数，返回该段区域内reads的数组
	 * 定位到两个端点所在的 读取invNum区间，然后计算新的invNum区间，如果该染色体在mapping时候不存在，则返回null
	 * @param thisInvNum 每个区域内所含的bp数，大于等于invNum，最好是invNum的倍数<br>
	 * 如果thisInvNum <= 0，则thisInvNum = invNum<br>
	 * 如果invNum ==1 && thisInvNum == 1，结果会很精确
	 * @param chrID 一定要小写
	 * @param startNum 起点坐标，为实际起点，如果startNum<=0 并且endNum<=0，则返回全长信息
	 * @param endNum 终点坐标，为实际终点
	 * 如果(endNum - startNum + 1) / thisInvNum >0.7，则将binNum设置为1
	 * @param type 0：加权平均 1：取最高值，2：加权但不平均--也就是加和
	 * @return 如果没有找到该染色体位点，则返回null
	 */
	public double[] getRangeInfo(int thisInvNum,String chrID,int startNum,int endNum,int type) {
		if (thisInvNum <= 0) thisInvNum = 1;
		if (thisInvNum != 1) {
			throw new RuntimeException("thisInvNum must be 1");
		}
		double[] result = null;
		if (!mapChrID2ReadsInfo.containsKey(chrID.toLowerCase())) {
			int[] num = mapChrId2NumCannotFind.get(chrID.toLowerCase());
			if (num == null) {
				num = new int[]{0};
				mapChrId2NumCannotFind.put(chrID.toLowerCase(), num);
			}
			num[0]++;
			if (num[0] == 1 || num[0] % 1000 == 0) {
				logger.error("cannot find chromosome {} for {} times", chrID, num[0]+"");
			}
			return result;
		}
		return getRangeInfoInv1(chrID, startNum, endNum);
	}
	/**
	 * 间断为1的精确版本，经过标准化，和equations修正
	 * @param chrID 染色体ID
	 * @param startNum 实际起点，从1开始记数
	 * @param endNum 实际终点，从1开始记数
	 */
	private double[] getRangeInfoInv1(String chrID, int startNum, int endNum) {
		ChrMapReadsInfo chrMapReadsInfo = mapChrID2ReadsInfo.get(chrID.toLowerCase());
		if (chrMapReadsInfo == null) {
			logger.info("cannot find this chromosome: " + chrID);
			return null;
		}
		int[] startEnd = correctStartEnd(mapChrID2Len, chrID, startNum, endNum);
		if (startEnd == null) {
			return null;
		}
		startEnd[0] = startEnd[0] - 1;
		startEnd[1] = startEnd[1] - 1;
		double[] result = new double[startEnd[1] - startEnd[0] + 1];
		
		int[] invNumReads = chrMapReadsInfo.getSumChrBpReads();
		if (mapChrID2LsAlignmentFilter != null && mapChrID2LsAlignmentFilter.containsKey(chrID.toLowerCase())) {
			List<? extends Alignment> lsAlignments = mapChrID2LsAlignmentFilter.get(chrID.toLowerCase());
			invNumReads = MapReads.cleanInfoNotInAlignment(lsAlignments, invNumReads, 1);
		}
		int k = 0;
		for (int i = startEnd[0]; i <= startEnd[1]; i++) {
			result[k] = (double)invNumReads[i];
			k++;
		}
		return result;
	}
	@Override
	protected long getAllReadsNum() {
		return 0;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}
	
	/** 用来画CpG覆盖度情况的 */
	public Map<EnumCpGmethyType, DepthPercentage> getMapCpGType2DepthInfo() {
		Map<EnumCpGmethyType, DepthPercentage> mapCpGType2DepthInfo = new HashMap<>();
		mapCpGType2DepthInfo.put(EnumCpGmethyType.ALL, new DepthPercentage());
		mapCpGType2DepthInfo.put(EnumCpGmethyType.CG, new DepthPercentage());
		mapCpGType2DepthInfo.put(EnumCpGmethyType.CHG, new DepthPercentage());
		mapCpGType2DepthInfo.put(EnumCpGmethyType.CHH, new DepthPercentage());

		for (ChrMapReadsInfo chrInfo : mapChrID2ReadsInfo.values()) {
			for (int cpGint : chrInfo.getSumChrBpReads()) {
				if (cpGint == 0) continue;
				CpGInfo cpGInfo = CpGInfo.decodeInt2Cpg(cpGint);
				DepthPercentage depthPercentageAll = mapCpGType2DepthInfo.get(EnumCpGmethyType.ALL);
				depthPercentageAll.addCoverageSite(cpGInfo);
				
				DepthPercentage depthPercentageDetail = mapCpGType2DepthInfo.get(cpGInfo.getEnumCpGmethyType());
				depthPercentageDetail.addCoverageSite(cpGInfo);
			}
		}
		return mapCpGType2DepthInfo;
	}

	public static class CpGCalculator {
		/** CpG的计算方式 */
		EnumBspCpGCalculateType cpGCalculateType;
		/** CpG甲基化的类型 */
		EnumCpGmethyType cpGmethyType = EnumCpGmethyType.ALL;
		
		int coverageFilter=10;
		
		/** 最少也要计算10bp window中的 甲基化情况 */
		int minWindow = 1;
		//MC比例超过这个的才算甲基化的C
		double thresholdProp = 0.2;
		
		Boolean isCis5To3 = null;
		
		public void setCpGCalculateType(EnumBspCpGCalculateType cpGCalculateType) {
			this.cpGCalculateType = cpGCalculateType;
		}
		public void setCpGmethyType(EnumCpGmethyType cpGmethyType) {
			this.cpGmethyType = cpGmethyType;
		}
		public void setCoverageFilter(int coverageFilter) {
			this.coverageFilter = coverageFilter;
		}
		/** 是否需要过滤方向
		 * null 表示不过滤方向
		 * @param isCis5To3
		 */
		public void setIsCis5To3(Boolean isCis5To3) {
			this.isCis5To3 = isCis5To3;
		}
		/** 默认为10
		 * 最少也要计算10bpwindow中的甲基化情况。
		 * 譬如一个位置有80bp，那么就最最少每10bp采一个点，然后做扩展之类的工作
 		 * @param minWindow
		 */
		public void setMinWindow(int minWindow) {
			if (minWindow <= 0) return;
			this.minWindow = minWindow;
		}
		public int getMinWindow() {
			return minWindow;
		}
		public double calculateCpG(int[] cpGValues) {
			if (cpGCalculateType == null && cpGmethyType == null) {
				return MathComput.mean(cpGValues);
			}
			cpGValues = filterCpGType(cpGValues);
			
			return calculateHsMetrics(cpGValues);
		}
		
		/**
		 * 给定输入的CpG编码信息--就是通过 {@link CpGInfo#encodeCpg2Int()} 编码后的信息
		 * 指定计算公式，具体需要计算的CpG类型，分块长度，最后获得甲基化的结果数组，可用于画图
		 * @param tmpResult
		 * @param binNum 小于等于0表示不切分
		 * @return
		 */
		@VisibleForTesting
		public double[] calculateCpGInfo(double[] tmpResult, int binNum) {
			if (binNum <= 0) {
				binNum = tmpResult.length;
			}
			int binLength = tmpResult.length/binNum;
			int binNumReal = binNum;
			if (binLength < 1) {
				binLength = 1;
				binNumReal = tmpResult.length;
			}
			
			double[] result = calculateCpGInfo(tmpResult, binNumReal, binLength);
			
			if (result.length < binNum) {
				result = MathComput.mySpline(result, binNum);
			}
			return result;
		}
		
		/**
		 * 给定输入的CpG编码信息--就是通过 {@link CpGInfo#encodeCpg2Int()} 编码后的信息
		 * 指定计算公式，具体需要计算的CpG类型，分块长度，最后获得甲基化的结果数组，可用于画图
		 * @param tmpResult
		 * @return
		 */
		@VisibleForTesting
		public double[] calculateCpGInfoLength(double[] tmpResult) {
			int binNumReal = Math.round(tmpResult.length/minWindow);
			return calculateCpGInfo(tmpResult, binNumReal, minWindow);
		}
		
		private double[] calculateCpGInfo(double[] tmpResult, int binNumReal, int binLength) {
			double[] result = new double[binNumReal];
			int[] tmpValue = new int[binLength];
			
			int i = 0, j = 0, resultIndex = 0;
			for (; i < tmpResult.length; i++) {
				if (i > 0 && i%binLength == 0) {
					resultIndex = i/binLength - 1;
					result[resultIndex] = calculateCpG(tmpValue);
					tmpValue = new int[binLength]; j = 0;
				}
				tmpValue[j++] = (int) tmpResult[i];
			}
			if (resultIndex < result.length - 1) {
				result[i/binLength - 1] = calculateCpG(tmpValue);
			}
			return result;
		}
		
		private double calculateHsMetrics(int[] cpGValues) {
	
			//全体MC的覆盖度
			int coverageC = 0;
			//全体非MC的覆盖度
			int coverageT = 0;
			//甲基化的C
			int numMC = 0;
			//全体甲基化C的覆盖度百分比之和
			double CPropertyAll = 0;
			//全部的C
			int numC = 0;
			//全部的碱基数
			int numAll = cpGValues.length;
			for (int i : cpGValues) {
				if (i == 0) continue;
				CpGInfo cpGInfo = CpGInfo.decodeInt2Cpg(i);
				coverageC += cpGInfo.getDepthMethy();
				coverageT += cpGInfo.getDepthNonMethy();
				if (coverageC+coverageT > 0) {
					CPropertyAll += (double)coverageC/(coverageC+coverageT);
				}
				if (coverageC+coverageT > 0 && (double)coverageC/(coverageC+coverageT) >= thresholdProp) {
					numMC++;
				}
				numC++;
			}
			
			if (coverageC+coverageT == 0) {
				return 0;
			}
			
			if (cpGCalculateType == EnumBspCpGCalculateType.CpGCoverage) {
				return (double)coverageC/(coverageC+coverageT);
			} else if (cpGCalculateType == EnumBspCpGCalculateType.CpGRatio) {
				return CPropertyAll/numC;
			}
			throw new RuntimeException("does not support Calculate Type "+ cpGCalculateType);
		}
		
		/** 根据CpG类型进行过滤 */
		@VisibleForTesting
		protected int[] filterCpGType(int[] cpGValues) {
			//过滤非选中甲基化的位点
			if (cpGmethyType == EnumCpGmethyType.ALL && coverageFilter <= 0 && isCis5To3 == null) {
				return cpGValues;
			}

			int[] cpGValuesNew = new int[cpGValues.length];
			for (int i = 0; i < cpGValues.length; i++) {
				cpGValuesNew[i] = 0;
				int cpGUnit = cpGValues[i];
				if (cpGUnit == 0) {
					continue;
				}
				CpGInfo cpGInfo = CpGInfo.decodeInt2Cpg(cpGUnit);
				if (cpGInfo.getEnumCpGmethyType() == cpGmethyType || cpGmethyType == EnumCpGmethyType.ALL) {
					if (cpGInfo.getCoverage() >= coverageFilter
							&& (isCis5To3 == null || cpGInfo.isCis5to3() == isCis5To3)
							) {
						cpGValuesNew[i] = cpGUnit;
					}
				}
			}
			return cpGValuesNew;
		}
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
	
	public int getDepthMethy() {
		return depthMethy;
	}
	public int getDepthNonMethy() {
		return depthNonMethy;
	}
	public int getCoverage() {
		return depthMethy + depthNonMethy;
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
	public EnumCpGmethyType getEnumCpGmethyType() {
		return enumCpGmethyType;
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

