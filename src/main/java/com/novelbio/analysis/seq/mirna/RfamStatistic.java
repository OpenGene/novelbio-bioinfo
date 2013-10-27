package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.sam.SamFile;
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
	 */
	Map<String, String[]> mapRfamID2Info = new HashMap<String, String[]>();	
	/** 具体看每个RfamID的counts */
	Map<String, Double> mapRfamID2Counts;
	Map<String, Double> mapRfamType2Counts;
	Map<String, Double> mapRfamClass2Counts;
	
	AlignSeq samFile;
	
	public void setSamFile(SamFile samFile) {
		this.samFile = samFile;
	}
	/**
	 * 输入rfam文件，读取Rfam的信息进入hash表
	 * @param rfamFile
	 */
	public void readRfamTab(String rfamFile) {
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
		txtRead.close();
	}
	public List<String> getLsRfamID(List<String> lsRfamIDraw) {
		List<String> lsRfamID = new ArrayList<>();
		for (String rfamInfo : lsRfamIDraw) {
			String rfamID = rfamInfo.split("//")[0];
			lsRfamID.add(rfamID);
		}
		return lsRfamID;
	}
	public List<String> getLsRfamType(List<String> lsRfamIDraw) {
		List<String> lsRfamType = new ArrayList<>();
		for (String rfamInfo : lsRfamIDraw) {
			String rfamID = rfamInfo.split("//")[0];
			String rfamType = mapRfamID2Info.get(rfamID)[0];
			lsRfamType.add(rfamType);
		}
		return lsRfamType;
	}
	public List<String> getLsRfamClass(List<String> lsRfamIDraw) {
		List<String> lsRfamClass = new ArrayList<>();
		for (String rfamInfo : lsRfamIDraw) {
			String rfamID = rfamInfo.split("//")[0];
			String rfamClass = mapRfamID2Info.get(rfamID)[3];
			lsRfamClass.add(rfamClass);
		}
		return lsRfamClass;
	}
	
	/** RfamID2Info的信息
	 * key: RfamID
	 * value 有 <br>
	 * 0 rfamType<br>
	 * 1 rfamAnno<br>
	 * 2 rfam description<br>
	 * 3 rfam class<br>
	 */
	public Map<String, String[]> getMapRfamID2Info() {
		return mapRfamID2Info;
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
	public void countRfamBam() {
		for (AlignRecord samRecord : samFile.readLines()) {
			if (!samRecord.isMapped()) {
				continue;
			}
			String RfamID = samRecord.getRefID().split("//")[0];
			String rfamType = mapRfamID2Info.get(RfamID)[0];
			String rfamClass = mapRfamID2Info.get(RfamID)[3];
			Double thisCount = (double)1/samRecord.getMappedReadsWeight();
			
			addCounts(RfamID, mapRfamID2Counts, thisCount);
			addCounts(rfamClass, mapRfamClass2Counts, thisCount);
			addCounts(rfamType, mapRfamType2Counts, thisCount);
		}
		samFile.close();
	}
	
	private void addCounts(String id, Map<String, Double> mapId2Value, double value) {
		if (mapId2Value.containsKey(id)) {
			double newCounts = mapId2Value.get(id) + value;
			mapId2Value.put(id, newCounts);
		} else {
			mapId2Value.put(id, value);
		}
	}
	
	public Map<String, Double> getMapRfamID2Counts() {
		return mapRfamID2Counts;
	}
	public Map<String, Double> getMapRfamClass2Counts() {
		return mapRfamClass2Counts;
	}
	public Map<String, Double> getMapRfamType2Counts() {
		return mapRfamType2Counts;
	}
	
	public static List<String> getLsTitleRfamIDAnno() {
		List<String> lsTitle = new ArrayList<>();
		lsTitle.add(TitleFormatNBC.RfamType.toString());
		lsTitle.add(TitleFormatNBC.RfamAnnotaion.toString());
		lsTitle.add(TitleFormatNBC.RfamDescription.toString());
		lsTitle.add(TitleFormatNBC.RfamClass.toString());
		return lsTitle;
	}
	
}
