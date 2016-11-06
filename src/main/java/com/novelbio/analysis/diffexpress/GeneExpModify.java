package com.novelbio.analysis.diffexpress;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.MathComput;

/**
 * 两个功能
 * 1. 把输入的基因表达表中相关的表达值提取出来。<br>
 * 譬如第一列为AccId，第二列为annotation，第三列为sample A，第四列为sample B
 * 这时候会把 AccId、A、B 这三列提取出来。
 * 注意如果表中有 A、B、C 三组，但是我们只比较 A和B，则仅把A、B两组相关的基因提取出来
 * @author zong0jie
 * @date 2016年10月27日
 */
public class GeneExpModify {
	/** 输入的基因表格，里面可能会有注释信息等 */
	List<String[]> lsGeneInfo = new ArrayList<String[]>();
	/**
	 * 一系列的表示基因分组的列，输入的时候就按照col进行了排序<br>
	 * 0: colNum, 实际number，从0开始计数<br>
	 * 1: SampleGroupName<br>
	 * 譬如第二列是分组A，第三列也是分组A<br>
	 * 则写为 String[]{"1","A"}	和String[]{"2","A"}
	 */
	List<String[]> lsSampleColumn2GroupName;
	
	/** 做差异基因的时候各个样本表达量的值之和不能小于该数值 */
	double minSampleSumNum = 0;
	/** 做差异基因的时候每个样本表达量的值不能都小于该数值
	 * 意思如果为 2, 3, 2, 1.3, 3
	 * 则当值设定为3时，上述基因删除 
	 */
	double minSampleSepNum = 0;
	
	/**基因唯一ID，必须没有重复，从0开始计算 */
	int colAccID = 0;
	
	/** 输入的值是否需要为counts，譬如DESeq和edgeR都要求输入的为counts，也就是必须为整数 */
	boolean isCount = false;
	
	/** 基因名--对应样本名--对应平均表达值
	 * 如果某个文件最后没有平均值，可以用这个来加上
	 */
	Map<String, Map<String, Double>> mapGeneID_2_Sample2MeanValue;
	
	
	/**基因唯一ID，必须没有重复，从0开始计算 */
	public void setColAccID(int colAccID) {
		this.colAccID = colAccID;
	}
	
	/** 做差异基因的时候各个样本表达量的值之和不能小于该数值
	 * 譬如如果多个样本表达量都为0，那就不考虑了
	 * @param addAllLine 默认为0，意思就是多个样本表达量之和为0 就不考虑
	 * 是小于的关系，不包括等于
	 */
	public void setMinSampleSumNum(double minSampleSumNum) {
		this.minSampleSumNum = minSampleSumNum;
	}
	/** 做差异基因的时候每个样本表达量的值不能都小于该数值
	 * 意思如果为 2, 3, 2, 1.3, 3
	 * 则当值设定为4时，上述基因删除
	 * @param minSampleSepNum 是小于的关系，不包括等于
	 */
	public void setMinSampleSepNum(double minSampleSepNum) {
		this.minSampleSepNum = minSampleSepNum;
	}
	
	/** 输入的值是否需要为counts，譬如DESeq和edgeR都要求输入的为counts，也就是必须为整数
	 * @param isCount 默认为false
	 */
	public void setIsCount(boolean isCount) {
		this.isCount = isCount;
	}
	
	public void setLsGeneInfo(List<String[]> lsGeneInfo) {
		this.lsGeneInfo = lsGeneInfo;
	}
	
	public void readGeneExpFile(String geneExpFile) {
		this.lsGeneInfo = ExcelTxtRead.readLsExcelTxt(geneExpFile, 1);
	}
	
	/**
	 * 一系列的表示基因分组的列<br>
	 * 0: colNum, 实际number，从1开始计数<br>
	 * 1: SampleGroupName
	 */
	public void setCol2SampleFrom1(List<String[]> lsSampleColumn2GroupName) {
		//按列进行排序
		Collections.sort(lsSampleColumn2GroupName, new Comparator<String[]>() {
			public int compare(String[] o1, String[] o2) {
				Integer col1 = Integer.parseInt(o1[0]);
				Integer col2 = Integer.parseInt(o2[0]);
				return col1.compareTo(col2);
			}
		});
		this.lsSampleColumn2GroupName = new ArrayList<>();
		for (String[] col2Group : lsSampleColumn2GroupName) {
			this.lsSampleColumn2GroupName.add(new String[]{(Integer.parseInt(col2Group[0])-1) + "", col2Group[1]});
		}
	}
	
	/**
	 * 将输入的文件重整理成所需要的txt格式写入文本
	 * 
	 */
	public void writeToGeneFile(String outFileName) {
		lsGeneInfo = removeDuplicate();
		lsGeneInfo = getAnalysisGeneInfo();
		
		TxtReadandWrite txtWrite = new TxtReadandWrite(outFileName, true);
		String[] title = lsGeneInfo.get(0);
		txtWrite.writefileln(title);
		
		int m = 0;
		for (String[] info : lsGeneInfo) {
			if (m++ <= 0) continue; //跳过第一行
			info[0] = CmdOperate.addQuot(info[0].replace("\"", ""));
			double sum = 0;
			boolean biggerThanMinSampleSep = false;
			for (int i = 1; i < info.length; i++) {
				double num = Double.parseDouble(info[i]);
				if (num >= minSampleSepNum) {
					biggerThanMinSampleSep = true;
				}
				sum += num;
			}
			if ((minSampleSumNum < 0 || sum >= minSampleSumNum)
					&& (minSampleSepNum < 0 || biggerThanMinSampleSep) 
				) {
				txtWrite.writefileln(info);
			}
		}
		txtWrite.close();
	}
	
	/** 将lsGeneInfo去重复 */
	private List<String[]> removeDuplicate() {
		ArrayList<Integer> lsColID = new ArrayList<Integer>();
		for (String[] col2Group : lsSampleColumn2GroupName) {
			lsColID.add(Integer.parseInt(col2Group[0]) + 1);
		}
		List<String[]> lsTmpResult = MathComput.getMedian(lsGeneInfo.subList(1, lsGeneInfo.size()), colAccID+1, lsColID);
		lsTmpResult.add(0, lsGeneInfo.get(0));
		return lsTmpResult;
	}
	
	/**
	 * 获得选定的基因ID和具体值
	 * 排序方式按照输入的lsSampleColumn2GroupName进行排序，不做调整
	 * @return
	 * 0： geneID
	 * 1-n：value
	 */
	private List<String[]> getAnalysisGeneInfo() {
		ArrayList<String[]> lsResultGeneInfo = new ArrayList<>();
		List<String[]> lsSample2GroupNew = new ArrayList<>();
		for (int m = 0; m < lsGeneInfo.size(); m++) {
			String[] strings = lsGeneInfo.get(m);
			
			String[] tmpResult = new String[lsSampleColumn2GroupName.size() + 1];
			tmpResult[0] = strings[colAccID];
			for (int i = 0; i < lsSampleColumn2GroupName.size(); i++) {
				int colNum = Integer.parseInt(lsSampleColumn2GroupName.get(i)[0]);
				//title
				if (m == 0) {
					tmpResult[i + 1] = strings[colNum];
					lsSample2GroupNew.add(new String[]{(i + 1) + "", lsSampleColumn2GroupName.get(i)[1]});
					continue;
				}
				
				if (strings[colNum].equalsIgnoreCase("NA")) {
					tmpResult[i + 1] = 0 + "";
					continue;
				}
				
				try {
					tmpResult[i + 1] = Double.parseDouble(strings[colNum].trim()) + "";
				} catch (Exception e) {
					tmpResult[i + 1] = 0 + "";
				}
			}
			lsResultGeneInfo.add(tmpResult);
		}
		
		if (isCount) {
			for (int i = 1; i < lsResultGeneInfo.size(); i++) {
				String[] strings = lsResultGeneInfo.get(i);
				for (int j = 1; j < strings.length; j++) {
					strings[j] = Math.round(Double.parseDouble(strings[j])) + "";
				}
			}
		}
		lsSampleColumn2GroupName = lsSample2GroupNew;
		return lsResultGeneInfo;
	}
	
	public Map<String, Map<String, Double>> getMapGeneID_2_Sample2MeanValue() {
		return mapGeneID_2_Sample2MeanValue;
	}
	
	/** 获得基因表达量的平均值<br>
	 * <b>需要先运行 {@link #writeToGeneFile(String)}</b>
	 * @param outFileName
	 */
	public void writeAvgInfo2File(String outFileName) {
		setMapSample_2_time2value();
		TxtReadandWrite txtWrite = new TxtReadandWrite(outFileName, true);
		List<String> lsSamples = new ArrayList<>(mapGeneID_2_Sample2MeanValue.values().iterator().next().keySet());
		List<String> lsTitle = new ArrayList<>();
		lsTitle.add("AccId"); lsTitle.addAll(lsSamples);
		txtWrite.writefileln(lsTitle.toArray(new String[0]));
		
		for (String geneId : mapGeneID_2_Sample2MeanValue.keySet()) {
			List<String> lsGene2SampleMeanValue = new ArrayList<>();
			lsGene2SampleMeanValue.add(geneId);
			Map<String, Double> mapSample2MeanValue = mapGeneID_2_Sample2MeanValue.get(geneId);
			for (String sample : lsSamples) {
				lsGene2SampleMeanValue.add(mapSample2MeanValue.get(sample) + "");
			}
			txtWrite.writefileln(lsGene2SampleMeanValue.toArray(new String[0]));
		}
		txtWrite.close();
	}

	private void setMapSample_2_time2value() {
		mapGeneID_2_Sample2MeanValue = new LinkedHashMap<>();
		for (int i = 1; i < lsGeneInfo.size(); i++) {
			String[] geneID2Info = lsGeneInfo.get(i);
			String geneName = geneID2Info[colAccID];
			try {
				Map<String, Double> mapTime2value = mapTime2AvgValue(geneID2Info);
				mapGeneID_2_Sample2MeanValue.put(geneName, mapTime2value);
			} catch (Exception e) {
			}
		}
	}
	
	/**
	 * 获得 
	 * 样本时期--该时期内平均值
	 * 的map
	 * @param info
	 * @return
	 */
	protected Map<String, Double> mapTime2AvgValue(String[] info) {
		Map<String, List<Double>> mapTime2LsValue = new LinkedHashMap<>();
		
		for (int i = 0; i < lsSampleColumn2GroupName.size(); i++) {
			int colNum = Integer.parseInt(lsSampleColumn2GroupName.get(i)[0]);
			double value = 0;
			try {
				value = Double.parseDouble(info[colNum]);
			} catch (Exception e) {
				
			}
			
			String timeInfo = lsSampleColumn2GroupName.get(i)[1];//时期
			List<Double> lsValue = add_and_get_LsValue(timeInfo, mapTime2LsValue);
			lsValue.add(value);
		}
		Map<String, Double> mapTime2AvgValue = new LinkedHashMap<>();
		for (Entry<String, List<Double>> entry : mapTime2LsValue.entrySet()) {
			Double avgValue = MathComput.mean(entry.getValue());
			mapTime2AvgValue.put(entry.getKey(), avgValue);
		}
		return mapTime2AvgValue;
	}
	
	/** 
	 * 设定样本时期到具体值的信息。
	 * 没有该timeInfo就产生个新的list，有的话就获得原来的list 
	 */
	protected List<Double> add_and_get_LsValue(String timeInfo, Map<String, List<Double>> mapTime2value) {
		List<Double> lsValue = mapTime2value.get(timeInfo);
		if (lsValue == null) {
			lsValue = new ArrayList<Double>();
			mapTime2value.put(timeInfo, lsValue);
		}
		return lsValue;
	}
}
