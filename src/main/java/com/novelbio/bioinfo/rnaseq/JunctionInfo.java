package com.novelbio.bioinfo.rnaseq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.novelbio.base.SepSign;
import com.novelbio.bioinfo.base.Align;
import com.novelbio.bioinfo.base.AlignExtend;
import com.novelbio.bioinfo.base.binarysearch.ListEle;

/**
 * 本类重写了equal代码，用于比较两个loc是否一致
 * 重写了hashcode 仅比较ChrID + "//" + numberstart + "//" + numberstart;
 * 
 * 不比较两个exon所在转录本的名字<br>
 * 包括<br>
 * 条目起点 numberstart<br>
 * 条目终点 numberend<br>
 * 条目方向 cis5to3
 * @author zong0jie
 *
 */
public class JunctionInfo extends AlignExtend {
	List<JunctionUnit> lsJunctionUnits = new ArrayList<JunctionUnit>();
	/** key是junctionUnit.getKey() */
	Map<String, JunctionUnit> mapJunSite2Unit = new HashMap<String, JunctionUnit>();
	boolean considerStrand = false;
	
	String name;
	
	public String getName() {
		return name;
	}
	
	@Deprecated
	public void setParent(ListEle<? extends AlignExtend> parent) {}
	
	/**
	 * @param considerStrand 是否考虑junction方向
	 * @param junctionUnit
	 */
	public JunctionInfo(boolean considerStrand, JunctionUnit junctionUnit) {
		setStartAbs(junctionUnit.getStartAbs());
		setEndAbs(junctionUnit.getEndAbs());
		setChrId(junctionUnit.getChrId());
		this.name = junctionUnit.key(false);
		this.considerStrand = considerStrand;

		lsJunctionUnits.add(junctionUnit);
		mapJunSite2Unit.put(junctionUnit.key(considerStrand), junctionUnit);
	}
	
	public void addJuncInfo(JunctionInfo junctionInfo) {
		for (JunctionUnit junctionUnit : junctionInfo.lsJunctionUnits) {
			addJuncUnit(junctionUnit);
		}
	}
	public List<JunctionUnit> getLsJunctionUnits() {
		return lsJunctionUnits;
	}
	public void addJuncUnit(JunctionUnit junctionUnit) {
		if (mapJunSite2Unit.containsKey(junctionUnit.key(considerStrand))) {
			JunctionUnit junctionUnit2 = mapJunSite2Unit.get(junctionUnit.key(considerStrand));
			junctionUnit2.addReadsJuncUnit(junctionUnit);
		} else {
			mapJunSite2Unit.put(junctionUnit.key(considerStrand), junctionUnit);
			lsJunctionUnits.add(junctionUnit);
		}
		if (getStartAbs() > junctionUnit.getStartAbs())
			setStartAbs(junctionUnit.getStartAbs());
		if (getEndAbs() < junctionUnit.getEndAbs())
			setEndAbs(junctionUnit.getEndAbs());
	}
	
	/**
	 * 不能判断不同染色体上相同的坐标位点
	 * 不比较两个exon所在转录本的名字
	 * 也不比较他们自己的名字
	 * 仅比较坐标和方向
	 */
	public boolean equals(Object elementAbs) {
		if (this == elementAbs) return true;
		
		if (elementAbs == null) return false;
		
		if (getClass() != elementAbs.getClass()) return false;
		JunctionInfo element = (JunctionInfo)elementAbs;
		return equalsRefAndLoc(element);
	}
	@Override
	public int hashCode() {
		int i = 1;
		if (cis5to3) {
			i = -1;
		}
		return getStartAbs() * 100000 + getEndAbs() * i + getChrId().hashCode();
	}
	
	public static class JunctionUnit extends Align {
		/** 记载与该jun相邻的前一个jun，与基因的方向无关 */
		Set<String> setJuncKeyBefore = new HashSet<>();
		/**  记载与该jun相邻的后一个jun，与基因的方向无关 */
		Set<String> setJuncKeyAfter = new HashSet<>();
		
		String name;
		
		boolean considerStrand;
		/**
		 * key1:condition<br>
		 * key2:group<br>
		 * value:junction number<p>
		 *  value是为了地址传递才采用数组
		 */
		Map<String, Map<String, double[]>> mapCond2group2JunNum = new HashMap<>();
		
		/**
		 * 需要后面手动设定起点和终点
		 * @param chrID
		 * @param start 从1开始记数
		 * @param end 从1开始记数
		 */
		public JunctionUnit(String chrID, int start, int end) {
			setChrId(chrID);
			setStartEndLoc(start, end);
			setCis5to3(true);
			setName(start + "_" +end);
		}
		
		/**
		 * @param chrID
		 * @param start 从1开始记数
		 * @param end 从1开始记数
		 * @param isCis5To3
		 */
		public JunctionUnit(String chrID, int start, int end, boolean isCis5To3) {
			setChrId(chrID);
			setStartEndLoc(start, end);
			setCis5to3(isCis5To3);
			setName(start + "_" +end);
		}
		public void setName(String name) {
			this.name = name;
		}
		/** 是否考虑链特异性 */
		public void setConsiderStrand(boolean considerStrand) {
			this.considerStrand = considerStrand;
		}
		
		/** 添加上一个Jun，如果上一个jun存在，则把readsNum的数字加到上一个Jun中*/
		public void addJunBeforeAbs(JunctionUnit junBefore) {
			if (junBefore == null) return;
			
			String key = junBefore.key();
			setJuncKeyBefore.add(key);
		}
		/** 添加下一个Jun，如果下一个jun存在，则把readsNum的数字加到下一个Jun中*/
		public void addJunAfterAbs(JunctionUnit junAfter) {
			if (junAfter == null) return;
			
			String key = junAfter.key();
			setJuncKeyAfter.add(key);
		}
		
		/** 没有则返回空的list */
		public List<JunctionUnit> getLsJunAfterAbs(TophatJunction tophatJunction) {
			List<JunctionUnit> lsJunctionUnits = new ArrayList<>();
			for (String key : setJuncKeyAfter) {
				JunctionUnit junctionUnit = tophatJunction.getJunctionSiteAll(key);
				if (junctionUnit == null) {
					throw new RuntimeException("cannot find junction unit " + key);
				}
				lsJunctionUnits.add(junctionUnit);
			}
			return lsJunctionUnits;
		}
		
		/** 没有则返回空的list */
		public List<JunctionUnit> getLsJunBeforeAbs(TophatJunction tophatJunction) {
			List<JunctionUnit> lsJunctionUnits = new ArrayList<>();
			for (String key : setJuncKeyBefore) {
				JunctionUnit junctionUnit = tophatJunction.getJunctionSiteAll(key);
				if (junctionUnit == null) {
					throw new RuntimeException("cannot find junction unit " + key);
				}
				lsJunctionUnits.add(junctionUnit);
			}
			return lsJunctionUnits;
			
//			return new ArrayList<>(mapJunBefore.values());
		}
		
		public void setReadsNum(String condition, String group, int readsNum) {
			if (group == null) group = "";
			
			Map<String, double[]> mapGroup2Value = null;
			if (mapCond2group2JunNum.containsKey(condition)) {
				mapGroup2Value = mapCond2group2JunNum.get(condition);
			} else {
				mapGroup2Value = new HashMap<>();
				mapCond2group2JunNum.put(condition, mapGroup2Value);
			}
			mapGroup2Value.put(group, new double[]{readsNum});
		}
		
		/** readsNum+1 */
		public void addReadsNum1(String condition, String group) {
			addReadsNum(condition, group, 1);
		}
		
		/** readsNum+1 */
		public void addReadsNum(String condition, String group, double num) {
			Map<String, double[]> mapGroup2Value = mapCond2group2JunNum.get(condition);
			if (mapGroup2Value == null) {
				mapGroup2Value = new HashMap<>();//方便排序正确
				mapCond2group2JunNum.put(condition, mapGroup2Value);
			}
			
			double[] readsNum = mapGroup2Value.get(group);
			if (readsNum == null) {
				readsNum = new double[1];
				mapGroup2Value.put(group, readsNum);
			}
			readsNum[0] += num;
		}
		
		/** 把另一个junctionUnit中的信息全部加过来 */
		protected void addReadsJuncUnit(JunctionUnit junctionUnit) {
			for (String condition : junctionUnit.mapCond2group2JunNum.keySet()) {
				Map<String, double[]> mapGroup2Value = junctionUnit.mapCond2group2JunNum.get(condition);
				for (String group : mapGroup2Value.keySet()) {
					addReadsNum(condition, group, mapGroup2Value.get(group)[0]);
				}
			}
		}
		
		public double getReadsNum(String condition, String group) {
			try {
				double[] readsNum = mapCond2group2JunNum.get(condition).get(group);
				if (readsNum == null) {
					return 0;
				}
				return readsNum[0];
			} catch (Exception e) {
				return 0;
			}
		}
		
		/** 返回该时期全体组的reads num */
		public Map<String, Double> getReadsNum(String condition, Collection<String> lsGroups) {
			Map<String, Double> mapReads = new HashMap<>();
			Map<String, double[]> mapGroup2Value = mapCond2group2JunNum.get(condition);
			if (mapGroup2Value == null) {
				mapGroup2Value = new HashMap<>();
			}
			for (String group : lsGroups) {
				double[] value = mapGroup2Value.get(group);
				if (value == null) {
					mapReads.put(group, 0.0);
				} else {
					mapReads.put(group, value[0]);
				}
			}
			return mapReads;
		}
		
		/** 返回所有时期的junction reads总和 */
		public double getReadsNumAll() {
			int numAll = 0;
			for (Map<String, double[]> mapGroup2Value : mapCond2group2JunNum.values()) {
				for (double[] readsNums : mapGroup2Value.values()) {
					numAll += readsNums[0];
				}
			}
			return numAll;
		}
		
		protected String key() {
			return key(considerStrand);
		}
		
		/**
		 * @param considerStrand 是否考虑方向
		 * @return
		 */
		public String key(boolean considerStrand) {
			String key = "";
			if (considerStrand) {
				key = getKey(isCis5to3(), getChrId(), getStartAbs(), getEndAbs());
			} else {
				key = getKey(null, getChrId(), getStartAbs(), getEndAbs());
			}
			return key;
		}
		/**
		 * 不能判断不同染色体上相同的坐标位点
		 * 不比较两个exon所在转录本的名字
		 * 也不比较他们自己的名字
		 * 也不比较该junction所包含的readsNum
		 * 仅比较坐标和方向
		 */
		public boolean equals(Object elementAbs) {
			if (this == elementAbs) return true;
			
			if (elementAbs == null) return false;
			
			if (getClass() != elementAbs.getClass()) return false;
			JunctionUnit element = (JunctionUnit)elementAbs;
			//先不比较两个exon所在转录本的名字
			return equalsRefAndLoc(element);
		}
		@Override
		public int hashCode() {
			int i = 1;
			if (cis5to3) {
				i = -1;
			}
			return getStartAbs() * 100000 + getEndAbs() * i + getChrId().hashCode();
		}
		
		/** 指定坐标，返回key */
		protected static String getKey(Boolean cis5to3, String chrID, int startAbs, int endAbs) {
			String key = chrID.toLowerCase() + SepSign.SEP_INFO +  startAbs + SepSign.SEP_INFO + endAbs;
			if (cis5to3 != null) {
				key = cis5to3 + SepSign.SEP_ID + key;
			}
			return key;
		}
		
		public String toString() {
			String strand = ".";
			if (isCis5to3() != null) {
				strand = isCis5to3() ? "+" : "-";
			}
			double all = 0;
			for ( Map<String,double[]> mapGroup2Value : mapCond2group2JunNum.values()) {
				for (double[] value : mapGroup2Value.values()) {
					all += value[0];
				}
			}
			mapCond2group2JunNum.values();
			return getChrId() + "\t" + getStartAbs() + "\t" + getEndAbs() + "\t" + strand + "\t" + (int)all;
		}
	}
	
	public String toString() {
		String result = "";
		for (JunctionUnit junctionUnit : lsJunctionUnits) {
			result = result + junctionUnit.toString() + "   ";
		}
		return result;
	}

}

