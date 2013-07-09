package com.novelbio.aoplog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import uk.ac.babraham.FastQC.Modules.BasicStats;

import com.novelbio.analysis.seq.fastq.FQrecordCopeInt;
import com.novelbio.analysis.seq.fastq.FastQC;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.nbcgui.controlseq.CtrlFastQ;

/** 全体reads过滤完后再拦截一次，产生汇总报告 */
public class AopFastQFilterAll {
	private static Logger logger = Logger.getLogger(AopFastQFilter.class);
	/**
	 * 用来拦截CtrlFastQ的running方法
	 * @param CtrlFastQ
	 */
	@After("execution (* com.novelbio.nbcgui.controlseq.CtrlFastQ.running(..)) && target(ctrlFastQ)")
	public void fastQPoint(CtrlFastQ ctrlFastQ) {
		ReportBuilder fastQBuilder = new FastQBuilder(ctrlFastQ);
		fastQBuilder.writeInfo();
	}

	/**
	 * fastQ报告参数生成器
	 * @author novelbio
	 * 
	 */
	private class FastQBuilder extends ReportBuilder {
		/** 结果的存放路径 */
		private String savePath;
		/** 拦截的对象 */
		private CtrlFastQ ctrlFastQ;
		/** 所有basicStats表格数据集合 */
		private List<String> lsBaseTableLines = new ArrayList<String>();

		public FastQBuilder(CtrlFastQ ctrlFastQ) {
			this.ctrlFastQ = ctrlFastQ;
			String outFilePrefix = ctrlFastQ.getOutFilePrefix();
			this.savePath = outFilePrefix.endsWith(FileOperate.getSepPath()) ? outFilePrefix : FileOperate.getParentPathName(outFilePrefix);
			setParamPath(savePath);
		}
		
		@Override
		protected boolean buildExcels() {
			try {
				for (String key : ctrlFastQ.getMapCond2FastQCAfter().keySet()) {
					FastQC[] fastQCs = ctrlFastQ.getMapCond2FastQCBefore().get(key);
					readFastQC(fastQCs,key,ctrlFastQ.isFiltered(), ctrlFastQ.isQcBefore());
					if (ctrlFastQ.isFiltered()) {
						fastQCs = ctrlFastQ.getMapCond2FastQCAfter().get(key);
						readFastQC(fastQCs, key, ctrlFastQ.isFiltered(), ctrlFastQ.isQcAfter());
					}
				}
				TxtReadandWrite txtWrite = new TxtReadandWrite(savePath + "basicStats_all.xls", true);
				for (String string : lsBaseTableLines) {
					txtWrite.writefileln(string);
				}
				txtWrite.close();
				
				addParamInfo(Param.excelParam, "basicStats_all.xls");
			} catch (Exception e) {
				logger.error("aopFastQ生成excel出错！");
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		/**
		 * 读取fastQC并生成excel表格，并遍历图片
		 * @param fastQCs
		 * @param key
		 * @param isBefore
		 * @param isQc
		 * @throws Exception
		 */
		private void readFastQC(FastQC[] fastQCs, String prefix, boolean isFiltered, boolean isBefore) throws Exception{
			for (int i = 0; i < fastQCs.length; i++) {
				String key = prefix;
				if (isFiltered) {
					if (fastQCs.length == 1 || fastQCs[1] == null) {
						key += isBefore?"_BeforeFilter":"_AfterFilter";
					} else {
						key += isBefore?"_BeforeFilter_"+(i+1):"_AfterFilter_"+(i+1);
					}
				}

				FastQC fastQC = fastQCs[i];
				if (fastQC == null) continue;
				for (FQrecordCopeInt fQrecordCopeInt : fastQC.getLsModules()) {
					if (fQrecordCopeInt instanceof BasicStats) {
						Map<String, String> mapTable = ((BasicStats)fQrecordCopeInt).getResult();
						addToTotalTableList(key,mapTable);
					}
				}
			}
		}

		@Override
		public boolean buildImages() {
			return true;
		}

		@Override
		protected boolean fillDescFile() {
			return true;
		}
		
		private void addToTotalTableList(String key,Map<String, String> mapTable){
			if (lsBaseTableLines.size() == 0) {
				String allTitles = "SampleName";
				for (String title : mapTable.keySet()) {
					allTitles += "\t" + title;
				}
				lsBaseTableLines.add(allTitles);
			}
			for (String title : mapTable.keySet()) {
				key += "\t" + mapTable.get(title);
			}
			lsBaseTableLines.add(key);
		}

	}


}
