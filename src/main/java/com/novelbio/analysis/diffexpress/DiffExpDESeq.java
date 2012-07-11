package com.novelbio.analysis.diffexpress;

import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.base.dataOperate.TxtReadandWrite;

/**
 * 必须输入一个文本，其中有几列是实验组有几列是对照组
 * 然后查找差异基因。
 * 调用DEseq算法，适用于数reads的试验，譬如miRNAseq或DGE
 * @author zong0jie
 *
 */
public class DiffExpDESeq {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TxtReadandWrite txtWrite = new TxtReadandWrite("Rstatistic/test",true);
		txtWrite.writefile("fesfes");
		txtWrite.close();
	}
	ArrayList<String[]> lsGeneInfo = new ArrayList<String[]>();
	/**
	 * 一系列的表示基因信息的列
	 */
	ArrayList<Integer> lsColAccID;
	/**基因唯一ID，必须没有重复 */
	int colAccID = 0;
	/**
	 * 比较组，可以输入一系列组
	 * map: condition to compare group <br>
	 * list比较的信息，只有两项<br>
	 * 0：treatment<br>
	 * 1：control
	 */
	HashMap<String, ArrayList<int[]>> mapCond2CompareGroup = new HashMap<String, ArrayList<int[]>>();
	
//	private void 
	
	
}
