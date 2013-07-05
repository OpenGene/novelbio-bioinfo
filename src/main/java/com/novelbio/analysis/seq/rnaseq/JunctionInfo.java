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
	
	/**
	 * 根据正反向自动设定起点和终点
	 * @param start 从1开始记数
	 * @param end 从1开始记数
	 * @param cis
	 */
	public JunctionInfo(JunctionUnit junctionUnit) {
		super(junctionUnit.getRefID(), junctionUnit.key(), true);
		numberstart = junctionUnit.getStartAbs();
		numberend = junctionUnit.getEndAbs();
		lsJunctionUnits.add(junctionUnit);
		mapJunSite2Unit.put(junctionUnit.key(), junctionUnit);
	}
	
	public void addJuncUnit(JunctionUnit junctionUnit) {
		if (mapJunSite2Unit.containsKey(junctionUnit.key())) {
			JunctionUnit junctionUnit2 = mapJunSite2Unit.get(junctionUnit.key());
			junctionUnit2.addReadsJuncUnit(junctionUnit);
		} else {
			mapJunSite2Unit.put(junctionUnit.key(), junctionUnit);
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
}

class JunctionUnit extends ListDetailAbs {
	/** value是为了地址传递才采用数组 */
	Map<String, int[]> mapCond2JunNum = new HashMap<String, int[]>();
	/**
	 * 根据正反向自动设定起点和终点
	 * @param start 从1开始记数
	 * @param end 从1开始记数
	 * @param cis
	 */
	JunctionUnit(String chrID, int start, int end) {
		super(chrID, start + "_" +end, true);
		numberstart = Math.min(start, end);
		numberend = Math.max(start, end);
	}
	
	public void setReadsNum(String condition, int readsNum) {
		mapCond2JunNum.put(condition, new int[]{readsNum});
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
	
	protected String key() {
		return getKey(getRefID(), getStartAbs(), getEndAbs());
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
	protected static String getKey(String chrID, int start, int end) {
		return chrID + start + SepSign.SEP_INFO + end;
	}
}
