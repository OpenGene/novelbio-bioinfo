package com.novelbio.analysis.diffexpress;

import java.util.ArrayList;
import java.util.Map;

/**
 * 用方法{@link #calculateResult()}来计算
 * @author zong0jie
 *
 */
public interface DiffExpInt {
	/**
	 * 一系列的表示基因分组的列<br>
	 * 0: colNum, 实际number<br>
	 * 1: SampleGroupName
	 */
	public void setCol2Sample(ArrayList<String[]> lsSampleColumn2GroupName);
	/**
	 * 设定输出文件夹和比较组
	 * @param fileName
	 * @param fold 需要新建的标准文件夹，在 {@link EnumReport.DiffExp.getResultFolder()} 中获得
	 * @param comparePair <br>
	 * 0: treatment<br>
	 * 1: control
	 */
	public void addFileName2Compare(String fileName, String fold, String[] comparePair);
	
	public void setGeneInfo(ArrayList<String[]> lsGeneInfo);
	
	/** 基因标记列，实际列 */
	public void setColID(int colID);

	/** 仅供测试 */
	public String getOutScript();
	
	/** 仅供测试 */
	public String getFileNameRawdata();
	
	/** 设定是否为log2value，仅在limma中使用 */
	public void setLog2Value(boolean isLog2Value);
	/** 返回是否为log过的值
	 * 主要用于limma，其实就是判断最大的表达值是否大于40
	 *  */
	public boolean isLogValue();
	
	/**
	 * 返回文件名，以及对应的比较<br>
	 * key：文件全名<br>
	 * value：对应的比较。譬如 String[]{Treat, Control}
	 * @return
	 */
	public Map<String, String[]> getMapOutFileName2Compare();
	
	public ArrayList<String> getResultFileName();
	
	/** 仅供AOP拦截使用，外界不要调用
	 * 拦截在其完成之后
	 */
	public void calculateResult();
		
	/** 删除中间文件 */
	public void clean();

}
