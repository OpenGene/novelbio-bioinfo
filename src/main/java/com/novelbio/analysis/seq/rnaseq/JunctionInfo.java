package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.novelbio.base.SepSign;
import com.novelbio.base.dataStructure.listOperate.ListDetailAbs;
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
public class JunctionInfo extends ListDetailAbs {
	List<JunctionUnit> lsJunctionUnits = new ArrayList<JunctionUnit>();
	/** key是junctionUnit.getKey() */
	Map<String, JunctionUnit> mapJunSite2Unit = new HashMap<String, JunctionUnit>();
	boolean considerStrand = false;
	
	/**
	 * @param considerStrand 是否考虑junction方向
	 * @param junctionUnit
	 */
	public JunctionInfo(boolean considerStrand, JunctionUnit junctionUnit) {
		super(junctionUnit.getRefID(), junctionUnit.key(false), true);
		this.considerStrand = considerStrand;
		numberstart = junctionUnit.getStartAbs();
		numberend = junctionUnit.getEndAbs();
		lsJunctionUnits.add(junctionUnit);
		mapJunSite2Unit.put(junctionUnit.key(considerStrand), junctionUnit);
	}
	
	public void addJuncInfo(JunctionInfo junctionInfo) {
		for (JunctionUnit junctionUnit : junctionInfo.lsJunctionUnits) {
			addJuncUnit(junctionUnit);
		}
	}
	
	public void addJuncUnit(JunctionUnit junctionUnit) {
		if (mapJunSite2Unit.containsKey(junctionUnit.key(considerStrand))) {
			JunctionUnit junctionUnit2 = mapJunSite2Unit.get(junctionUnit.key(considerStrand));
			junctionUnit2.addReadsJuncUnit(junctionUnit);
		} else {
			mapJunSite2Unit.put(junctionUnit.key(considerStrand), junctionUnit);
			lsJunctionUnits.add(junctionUnit);
		}
		if (numberstart > junctionUnit.getStartAbs())
			numberstart = junctionUnit.getStartAbs();
		if (numberend < junctionUnit.getEndAbs())
			numberend = junctionUnit.getEndAbs();
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
		//先不比较两个exon所在转录本的名字
		if (numberstart == element.numberstart && numberend == element.numberend && super.cis5to3 == element.cis5to3 ) {
			if (getRefID().equalsIgnoreCase(getRefID())) {
				return true;
			}
		}
		return false;
	}
	@Override
	public int hashCode() {
		int i = 1;
		if (cis5to3) {
			i = -1;
		}
		return numberstart * 100000 + numberend * i + getRefID().hashCode();
	}
	
	public static class JunctionUnit extends ListDetailAbs {
		/** 记载与该jun相邻的前一个jun，与基因的方向无关 */
		Map<String, JunctionUnit> mapJunBefore = new HashMap<>();
		/**  记载与该jun相邻的后一个jun，与基因的方向无关 */
		Map<String, JunctionUnit> mapJunAfter = new HashMap<>();
		
		/** value是为了地址传递才采用数组 */
		Map<String, int[]> mapCond2JunNum = new HashMap<String, int[]>();
		/**
		 * 根据正反向自动设定起点和终点
		 * @param start 从1开始记数
		 * @param end 从1开始记数
		 * @param cis
		 */
		public JunctionUnit(String chrID, int start, int end) {
			super(chrID, start + "_" +end, true);
			numberstart = Math.min(start, end);
			numberend = Math.max(start, end);
		}
		
		/** 添加上一个Jun，如果上一个jun存在，则把readsNum的数字加到上一个Jun中*/
		public void addJunBeforeAbs(JunctionUnit junBefore) {
			if (junBefore == null) return;
			
			String key = junBefore.key(true);
			JunctionUnit junBeforeExist = mapJunBefore.get(key);
			if (junBeforeExist == null) {
				mapJunBefore.put(key, junBefore);
			} else {
				junBeforeExist.addReadsNum(junBefore);
			}
		}
		/** 添加下一个Jun，如果下一个jun存在，则把readsNum的数字加到下一个Jun中*/
		public void addJunAfterAbs(JunctionUnit junAfter) {
			if (junAfter == null) return;
			
			String key = junAfter.key(true);
			JunctionUnit junAfterExist = mapJunAfter.get(key);
			if (junAfterExist == null) {
				mapJunAfter.put(key, junAfter);
			} else {
				junAfterExist.addReadsNum(junAfter);
			}
		}
		
		/** 没有则返回空的list */
		public List<JunctionUnit> getLsJunAfterAbs() {
			return new ArrayList<>(mapJunAfter.values());
		}
		/** 没有则返回空的list */
		public List<JunctionUnit> getLsJunBeforeAbs() {
			return new ArrayList<>(mapJunBefore.values());
		}
		
		public void setReadsNum(String condition, int readsNum) {
			mapCond2JunNum.put(condition, new int[]{readsNum});
		}
		/** readsNum+1 */
		public void addReadsNum(JunctionUnit junctionUnit) {
			for (String condition : junctionUnit.mapCond2JunNum.keySet()) {
				int[] numAdd = junctionUnit.mapCond2JunNum.get(condition);
				if (mapCond2JunNum.containsKey(condition)) {
					int[] num = mapCond2JunNum.get(condition);
					num[0] = num[0] + numAdd[0];
				} else {
					mapCond2JunNum.put(condition, numAdd);
				}
			}
		}
		
		/** readsNum+1 */
		public void addReadsNum1(String condition) {
			addReadsNum(condition, 1);
		}
		/** readsNum+1 */
		public void addReadsNum(String condition, int num) {
			int[] readsNum = mapCond2JunNum.get(condition);
			if (readsNum == null) {
				readsNum = new int[1];
				mapCond2JunNum.put(condition, readsNum);
			}
			readsNum[0] += num;
		}
		
		/** 把另一个junctionUnit中的信息全部加过来 */
		protected void addReadsJuncUnit(JunctionUnit junctionUnit) {
			for (String condition : junctionUnit.mapCond2JunNum.keySet()) {
				addReadsNum(condition, junctionUnit.mapCond2JunNum.get(condition)[0]);
			}
		}
		
		public int getReadsNum(String condition) {
			int[] readsNum = mapCond2JunNum.get(condition);
			if (readsNum == null) {
				return 0;
			}
			return readsNum[0];
		}
		/** 返回所有时期的junction reads总和 */
		public int getReadsNumAll() {
			int numAll = 0;
			for (int[] readsNums : mapCond2JunNum.values()) {
				numAll += readsNums[0];
			}
			return numAll;
		}
		
		/**
		 * @param considerStrand 是否考虑方向
		 * @return
		 */
		protected String key(boolean considerStrand) {
			String key = "";
			if (considerStrand) {
				key = getKey(isCis5to3(), getRefID(), getStartAbs(), getEndAbs());
			} else {
				key = getKey(null, getRefID(), getStartAbs(), getEndAbs());
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
			if (numberstart == element.numberstart && numberend == element.numberend && super.cis5to3 == element.cis5to3 ) {
				if (getRefID().equalsIgnoreCase(getRefID())) {
					return true;
				}
			}
			return false;
		}
		@Override
		public int hashCode() {
			int i = 1;
			if (cis5to3) {
				i = -1;
			}
			return numberstart * 100000 + numberend * i + getRefID().hashCode();
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
			return getRefID() + " " + getStartAbs() + " " + getEndAbs();
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

