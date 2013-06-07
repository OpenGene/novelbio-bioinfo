package com.novelbio.aoplog;

import org.apache.hadoop.fs.FileSystem;

import com.novelbio.base.SepSign;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileHadoop;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.service.SpringFactory;

/**
 * 报告抽象类
 * @author novelbio
 *
 */
public abstract class ReportBuilder {
	// 定义所有需要的参数
	// 图表1
	protected String excelParam = "lsExcels" + SepSign.SEP_INFO + "EXCEL::";
	protected String picParam = "lsPictures" + SepSign.SEP_INFO + "PICTURE::";
	// 图表2
	protected String excelParam1 = "lsExcels1" + SepSign.SEP_INFO + "EXCEL::";
	protected String picParam1 = "lsPictures1" + SepSign.SEP_INFO + "PICTURE::";
	// 测试方法参数
	protected String testMethodParam = "testMethod" + SepSign.SEP_INFO;
	// 筛选条件参数
	protected String finderConditionParam = "finderCondition" + SepSign.SEP_INFO;
	// 上调数参数
	protected String upRegulationParam = "upRegulation" + SepSign.SEP_INFO;
	// 下调数参数
	protected String downRegulationParam = "downRegulation" + SepSign.SEP_INFO;
	
	
	/**
	 * 创建excel文件以及excel文件的说明
	 * @return 是否成功
	 */
	public abstract boolean buildExcels();
	
	/**
	 * 创建图表文件及其说明
	 * @return 是否成功
	 */
	public abstract boolean buildImages();
	
	/**
	 * 创建描述文件param.txt
	 * @return 是否成功
	 */
	public abstract boolean buildDescFile();
	
	/**
	 * 取得参数文件
	 * 
	 * @param savePath
	 * @return
	 */
	protected TxtReadandWrite getParamsTxt(String savePath) {
		String paramsTxtPath = null;
		// 判断这些参数是放在本地还是hdfs上
		Boolean isHdfs = savePath.substring(0, 3).equalsIgnoreCase("HDFS");
		if (savePath.endsWith("/") || savePath.endsWith("\\")) {
			paramsTxtPath = savePath + "params.txt";
		} else {
			paramsTxtPath = FileOperate.getParentPathName(savePath) + "params.txt";
		}
		
		TxtReadandWrite txtReadandWrite = null;
		if (isHdfs) {
			txtReadandWrite = new TxtReadandWrite(new FileHadoop((FileSystem)SpringFactory.getFactory().getBean("fsHadoop"), paramsTxtPath), true, true);
		} else {
			txtReadandWrite = new TxtReadandWrite(paramsTxtPath, true, true);
		}

		return txtReadandWrite;
	}
}
