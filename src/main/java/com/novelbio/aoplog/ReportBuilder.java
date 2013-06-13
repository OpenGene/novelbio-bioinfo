package com.novelbio.aoplog;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.fs.FileSystem;
import org.apache.log4j.Logger;

import com.google.common.collect.HashMultimap;
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
	private static final Logger logger = Logger.getLogger(ReportBuilder.class);
	// 定义所有需要的参数
	HashMultimap<String, String> mapParam2Detail = HashMultimap.create();
	/** param文件所在的路径 */
	String paramPath;
	
	public HashMultimap<String, String> getMapParam2Detail() {
		if (mapParam2Detail.size() == 0) {
			for (Param param : Param.values()) {
				mapParam2Detail.put(param.toString(), "");
			}
		}
		return mapParam2Detail;
	}
	
	public void addParamInfo(Param param, String info) {
		getMapParam2Detail().put(param.toString(), info);
	}
	
	public void setParamPath(String paramPath) {
		this.paramPath = paramPath;
	}
	public void writeInfo() {
		if (buildExcels() && buildImages() && writeDescFile(paramPath))
			return;
		logger.error("aopFastQ生成报告图表参数出现异常！");
	}

	/**
	 * 创建excel文件以及excel文件的说明
	 * @return 是否成功
	 */
	protected abstract boolean buildExcels();
	
	/**
	 * 创建图表文件及其说明
	 * @return 是否成功
	 */
	protected abstract boolean buildImages();
	/**
	 * 创建描述文件param.txt
	 * @return 是否成功
	 */
	protected abstract boolean fillDescFile();
	
	/** 写参数 */
	protected boolean writeDescFile(String savePath) {
		TxtReadandWrite txtReadandWrite = null;
		try {
			txtReadandWrite = getParamsTxt(savePath);
			for (String param : mapParam2Detail.keySet()) {
				Set<String> setValue = mapParam2Detail.get(param);
				if (setValue == null) {
					continue;
				}
				String txtValue = "";
				for (String string : setValue) {
					if (string != null && !string.equals("")) {
						txtValue += string + ";";
					}
				}
				if (!txtValue.equals("")) {
					txtReadandWrite.writefileln(param + txtValue);
				}
			}
			txtReadandWrite.flash();
		} catch (Exception e) {
			logger.error("GOPath生成自动化报告参数文件param.txt出错！");
			return false;
		} finally {
			try {
				txtReadandWrite.close();
			} catch (Exception e2) {
				logger.error("GOPath生成自动化报告参数文件param.txt出错！");
				return false;
			}
		}
		return true;
	}
	
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

enum Param {
	// 图表1
	excelParam("lsExcels" + SepSign.SEP_INFO + "EXCEL::"), picParam("lsPictures" + SepSign.SEP_INFO + "PICTURE::"),
	// 图表2
	excelParam1("lsExcels1" + SepSign.SEP_INFO + "EXCEL::"), picParam1("lsPictures1" + SepSign.SEP_INFO + "PICTURE::"),
	// 测试方法参数
	testMethodParam("testMethod" + SepSign.SEP_INFO),
	// 筛选条件参数
	finderConditionParam("finderCondition" + SepSign.SEP_INFO),
	// 上调数参数
	upRegulationParam("upRegulation" + SepSign.SEP_INFO),
	// 下调数参数
	downRegulationParam("downRegulation" + SepSign.SEP_INFO);
	String paramKey;

	Param(String paramKey) {
		this.paramKey = paramKey;
	}
}
