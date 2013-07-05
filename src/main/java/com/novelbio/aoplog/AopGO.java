package com.novelbio.aoplog;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.novelbio.analysis.annotation.functiontest.FunctionTest;
import com.novelbio.analysis.annotation.functiontest.StatisticTestResult;
import com.novelbio.aoplog.AopPath.PathBuilder;
import com.novelbio.base.SepSign;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.plot.GraphicCope;
import com.novelbio.database.domain.geneanno.GOtype;
import com.novelbio.nbcgui.controltest.CtrlGO;
import com.novelbio.nbcgui.controltest.CtrlTestGOInt;

/**
 * 给GOPath添加report相关的参数说明
 * 
 * @author zong0jie
 * 
 */
@Component
@Aspect
public class AopGO {
	private static final Logger logger = Logger.getLogger(AopGO.class);

	/**
	 * 用来拦截GOPath的生成excel的方法，在生成excel之前，先画一幅图，并向配置文件params.txt中加入生成报告所需的参数
	 * @param excelPath
	 * @param ctrlTestGOInt
	 */
	@After("execution (* com.novelbio.nbcgui.controltest.CtrlTestGOInt.saveExcel(*)) && target(ctrlTestGOInt)")
	public void goPathPoint(CtrlTestGOInt ctrlTestGOInt) {
		ReportBuilder goPathBuilder = new GoBuilder(ctrlTestGOInt);
		goPathBuilder.writeInfo();
	}

	/**
	 * gopath报告参数生成器
	 * 
	 * @author novelbio
	 * 
	 */
	private class GoBuilder extends ReportBuilder {
		/** 画的柱状图的柱的数量上限 */
		private static final int barMaxNumVertical = 20;
		/** 画的柱状图的柱的数量上限 */
		private static final int barMaxNumHorizon = 15;
		/** 拦截的对象 */
		private CtrlTestGOInt ctrlTestGOInt;
		/** 筛选条件 */
		private String finderCondition = null;
		
		/**
		 * 
		 * @param excelPath
		 *            拦截的excel的存放路径
		 * @param ctrlTestGOInt
		 *            拦截的对象
		 */
		public GoBuilder(CtrlTestGOInt ctrlTestGOInt) {
			this.ctrlTestGOInt = ctrlTestGOInt;
			setParamPath(ctrlTestGOInt.getSaveParentPath());
			addParamInfo(Param.testMethodParam, ctrlTestGOInt.getGoAlgorithm().toString());
		}

		@Override
		protected boolean buildExcels() {
			boolean result = true;
			for (CtrlGO ctrlGO : ctrlTestGOInt.getMapResult_Prefix2FunTest().values()) {
				if (ctrlGO.getGOClass() == GOtype.BP) {
					if (!buildExcels(ctrlGO)) {
						result = false;
					}
					return result;
				}
			}
			logger.error("GO的biological process没有结果");
			return false;
		}
		
		private boolean buildExcels(CtrlGO ctrlGO) {
			try {
				// 拦截到对象中的结果集
				Map<String, FunctionTest> map = ctrlGO.getMapResult_Prefix2FunTest();
				for (Entry<String, FunctionTest> entry : map.entrySet()) {
					// excel中的testResult对象结果集
					List<StatisticTestResult> lsTestResults = entry.getValue().getTestResult();
					FunctionTest functionTest = entry.getValue();
					String prix = entry.getKey();
					finderCondition = getfindCondition(lsTestResults, finderCondition);

					//TODO 是否可以考虑不写上下调基因数量。参数开始赋值
					if (prix.equalsIgnoreCase("up")) {
						addParamInfo(Param.upRegulationParam, functionTest.getAllDifGeneNum() + "");
					}
					if (prix.equalsIgnoreCase("down")) {
						addParamInfo(Param.downRegulationParam, functionTest.getAllDifGeneNum() + "");
					}

					// 赋值excel
					Set<String> setSheetName = functionTest.getMapWriteToExcel().keySet();
					// 加上前缀名
					String excelPathOut = ctrlGO.getSaveExcelPrefix();

					for (String sheetName : setSheetName) {
						if (ctrlGO.isCluster()) {
							addParamInfo(Param.excelParam1, FileOperate.getFileName(excelPathOut) + "_" + prix + SepSign.SEP_INFO_SAMEDB + sheetName);
						} else {
							addParamInfo(Param.excelParam, FileOperate.getFileName(excelPathOut) + SepSign.SEP_INFO_SAMEDB + prix + sheetName);
						}
						// TODO excel的说明文件在这里写
						//String descFile = FileOperate.changeFileSuffix(excelPathOut, "_" + prix + sheetName + "_xls", ".txt");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("aopGoPath生成excel出错！");
				return false;
			}
			return true;
		}
		
		@Override
		public boolean buildImages() {
			try {
				for (String prefix : getPrefix(ctrlTestGOInt)) {
					List<BufferedImage> lsGOimage = new ArrayList<BufferedImage>();
					String excelSavePath = "";
					for (CtrlGO ctrlGO : ctrlTestGOInt.getMapResult_Prefix2FunTest().values()) {
						FunctionTest functionTest = ctrlGO.getMapResult_Prefix2FunTest().get(prefix);
						lsGOimage.add(PathBuilder.drawLog2PvaluePicture(functionTest.getTestResult(), ctrlGO.getResultBaseTitle()));
						excelSavePath = FileOperate.getParentPathName(ctrlGO.getSaveExcelPrefix());
					}
					BufferedImage bfImageCombine = GraphicCope.combineBfImage(true, 30, lsGOimage);
					String picNameLog2P = excelSavePath +  "GO-Analysis-Log2P_" + prefix + "_" + ctrlTestGOInt.getSavePrefix() + ".png";
					ImageIO.write(bfImageCombine, "png", new File(picNameLog2P));
					if (ctrlTestGOInt.isCluster()) {
						addParamInfo(Param.picParam1, FileOperate.getFileName(picNameLog2P));
					} else {
						addParamInfo(Param.picParam, FileOperate.getFileName(picNameLog2P));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("aopGoPath生成图表出错！");
				return false;
			}
			return true;
		}
		
		/** 将本次GO分析的前缀全部抓出来，方便画图 */
		private Set<String> getPrefix(CtrlTestGOInt ctrlTestGOInt) {
			Set<String> setPrefix = new HashSet<String>();
			for (CtrlGO ctrlGO : ctrlTestGOInt.getMapResult_Prefix2FunTest().values()) {
				Map<String, FunctionTest> map = ctrlGO.getMapResult_Prefix2FunTest();
				for (String prefix : map.keySet()) {
					setPrefix.add(prefix);
				}
			}
			return setPrefix;
		}
		
		@Override
		protected boolean fillDescFile() {
			return true;
		}
		
		/**
		 * 统计结果，返回筛选条件
		 * 
		 * @param lsTestResults
		 * @param knownCondition
		 *            已知条件，用来比较返回更宽松的条件
		 * @return
		 */
		private String getfindCondition(List<StatisticTestResult> lsTestResults, String knownCondition) {
			int fdrSum1 = 0;
			int fdrSum5 = 0;
			int pValueSum1 = 0;
			String[] result = { "FDR&lt;0.01", "FDR&lt;0.05", "P-value&lt;0.01", "P-value&lt;0.05" };
			int conditionNum = 0;
			if (knownCondition != null) {
				for (int i = 0; i < result.length; i++) {
					if (knownCondition.equals(result[i]))
						conditionNum = i;
				}
			}
			for (StatisticTestResult testResult : lsTestResults) {
				if (testResult.getPvalue() < 0.01) {
					pValueSum1++;
				}
				if (testResult.getPvalue() < 0.05) {
				}
				if (testResult.getFdr() < 0.01) {
					fdrSum1++;
				}
				if (testResult.getFdr() < 0.05) {
					fdrSum5++;
				}
			}
			int currentConditionNum = fdrSum1 > 8 ? 0 : (fdrSum5 > 8 ? 1 : (pValueSum1 > 8 ? 2 : 3));
			conditionNum = currentConditionNum > conditionNum ? currentConditionNum : conditionNum;
			return result[conditionNum];
		}

	}

}
