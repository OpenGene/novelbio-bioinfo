package com.novelbio.analysis.diffexpress;

import java.util.List;
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
	public void setCol2Sample(List<String[]> lsSampleColumn2GroupName);
	/**
	 * 设定输出文件夹和比较组
	 * @param fileName
	 * @param fold 需要新建的标准文件夹，在 {@link EnumReport.DiffExp.getResultFolder()} 中获得
	 * @param comparePair <br>
	 * 0: treatment<br>
	 * 1: control
	 */
	public void addFileName2Compare(String fileName, String[] comparePair);
	
	public void setGeneInfo(List<String[]> lsGeneInfo);
	
	/** 基因标记列，实际列 */
	public void setColID(int colID);

	/** 仅供测试 */
	public String getOutScript();
	
	/** 仅供测试 */
	public String getFileNameRawdata();
	
	/** 设定是否需要log值，仅在limma中使用 */
	public void setLogValue(boolean logTheValue);

	/**
	 * 返回文件名，以及对应的比较<br>
	 * key：文件全名<br>
	 * value：对应的比较。譬如 String[]{Treat, Control}
	 * @return
	 */
	public Map<String, String[]> getMapOutFileName2Compare();
	
	public List<String> getResultFileName();
	
	/** 计算差异
	 * 包含了{@link #generateGeneAndScript()}和{@link #runAndModifyResult()}
	 * 两个方法
	 */
	public void calculateResult();
		
	/** 删除中间文件 */
	public void clean();

}
