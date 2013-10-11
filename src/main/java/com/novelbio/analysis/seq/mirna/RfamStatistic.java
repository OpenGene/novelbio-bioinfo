package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.generalConf.TitleFormatNBC;

/**
 * 读取Rfam的文件，获得RfamID对应的具体信息
 * @author zong0jie
 */
public class RfamStatistic {
	Logger logger = Logger.getLogger(RfamStatistic.class);
	/** RfamID2Info的信息
	 * key: RfamID
	 * value 有 <br>
	 * 0 rfamType<br>
	 * 1 rfamAnno<br>
	 * 2 rfam description<br>
	 * 3 rfam class<br>
	 *  
	 *  */
	HashMap<String, String[]> mapRfamID2Info = new HashMap<String, String[]>();	
	/** 具体看每个RfamID的counts */
	HashMap<String, Double> mapRfamID2Counts;
	String outputFile = "";
	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}
	public void readRfamTab(String rfamFile) {
		readRfamFile(rfamFile);
	}
	/**
	 * @param rfamFile rfam的数据库
	 * @param mapBedFile mapping至Rfam序列的bed文件
	 * @param outFile 输出文件
	 */
	public void countRfamInfo(AlignSeq alignSeq) {
		mapRfamID2Counts = new HashMap<String, Double>();
		readRfamBam(alignSeq);
		ArrayList<String[]> lsResult = getInfo();
		TxtReadandWrite txtOut = new TxtReadandWrite(outputFile, true);
		txtOut.ExcelWrite(lsResult);
	}
	/**
	 * 输入rfam文件，读取Rfam的信息进入hash表
	 * @param rfamFile
	 */
	private void readRfamFile(String rfamFile) {
		if (mapRfamID2Info.size() > 0) {
			return;
		}
		
		TxtReadandWrite txtRead = new TxtReadandWrite(rfamFile, false);
		for (String string : txtRead.readlines()) {
			String[] ss = string.split("\t");
			String key = ss[2];
			String[] value = new String[4];
			value[0] = ss[3]; value[1] = ss[4]; value[2] = ss[11]; value[3] = ss[18];
			mapRfamID2Info.put(key, value);
		}
	}
	
	/**
	 * bed文件格式<br>
	 * RF00019//Y_RNA//ABBA01048699.1/58018-58105	1	32	8A23	32M	+	4	2KRM5:42:742<br>
	 * RF00019//Y_RNA//AF065396.1/1723-1629	0	32	32M	32M	+	4	2KRM5:42:742<br>
	 * RF00019//Y_RNA//AADD01101414.1/3467-3372	0	32	32M	32M	+	4	2KRM5:42:742<br>
	 * RF00019//Y_RNA//AADD01134564.1/10390-10295	0	32	32M	32M	+	4	2KRM5:42:742<br>
	 * mapping 至 rfam 的 bed 文件
	 * @param bedFile
	 */
	private void readRfamBam(AlignSeq alignSeq) {
		for (AlignRecord samRecord : alignSeq.readLines()) {
			if (!samRecord.isMapped()) {
				continue;
			}
			String RfamID = samRecord.getRefID().split("//")[0];
			Double thisCount = (double)1/samRecord.getMappedReadsWeight();
			if (mapRfamID2Counts.containsKey(RfamID)) {
				double newCounts = mapRfamID2Counts.get(RfamID) + thisCount;
				mapRfamID2Counts.put(RfamID, newCounts);
				continue;
			}
			mapRfamID2Counts.put(RfamID, thisCount);
		}
		alignSeq.close();
	}
	
	/**
	 * 获得每个RfamID对应的count信息和具体信息
	 * 得到的信息可以直接写入文本中
	 */
	private ArrayList<String[]> getInfo() {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		for (Entry<String, Double> entry : mapRfamID2Counts.entrySet()) {
			String[] rfamInfo = mapRfamID2Info.get(entry.getKey());
			if (rfamInfo == null) {
				logger.error("出现未知ID" + entry.getKey());
				continue;
			}
			String[] tmpResult = new String[rfamInfo.length + 2];
			tmpResult[0] = entry.getKey();
			tmpResult[1] = entry.getValue() + "";
			for (int i = 2; i < tmpResult.length; i++) {
				tmpResult[i] = rfamInfo[i - 2];
			}
			lsResult.add(tmpResult);
		}
		return lsResult;
	}
	
	public HashMap<String, Double> getMapRfam2Counts() {
		return mapRfamID2Counts;
	}
	
	/** 将给定的几组miRNA的值合并起来 */
	public ArrayList<String[]> combValue(Map<String, Map<String, Double>> mapPrefix2_RfamID2Value) {
		CombRfamMap combRfamMap = new CombRfamMap(mapRfamID2Info);
		return combRfamMap.combValue(mapPrefix2_RfamID2Value);
	}
	
}

class CombRfamMap extends MirCombMapGetValueAbs {
	/** RfamID2Info的信息
	 * key: RfamID
	 * value 有 <br>
	 * 0 rfamType<br>
	 * 1 rfamAnno<br>
	 * 2 rfam description<br>
	 * 3 rfam class<br>
	 *  */
	HashMap<String, String[]> mapRfamID2Info;
	public CombRfamMap(HashMap<String, String[]> mapRfamID2Info) {
		this.mapRfamID2Info = mapRfamID2Info;
	}
	@Override
	protected String[] getTitleIDAndInfo() {
		String[] titleStart = new String[3];
		titleStart[0] = TitleFormatNBC.RfamID.toString();
		titleStart[1] = TitleFormatNBC.RfamType.toString();
		titleStart[2] = TitleFormatNBC.RfamClass.toString();
		return titleStart;
	}

	@Override
	protected void fillMataInfo(String id, ArrayList<String> lsTmpResult) {
		String[] rfamInfo = mapRfamID2Info.get(id);
		lsTmpResult.add(id);
		lsTmpResult.add(rfamInfo[0].replace("\\N", ""));
		lsTmpResult.add(rfamInfo[3].replace("\\N", ""));
	}
	@Override
	protected Integer getExpValue(String condition, Double readsCount) {
		return readsCount.intValue();
	}
	
}
