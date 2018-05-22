package com.novelbio.generalconf;

import com.novelbio.base.SepSign;

/**
 * 自动化报告参数类
 */
public enum Param {
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
	
	@Override
	public String toString() {
		return paramKey;
	}
}
