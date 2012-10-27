package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map.Entry;

import com.novelbio.analysis.seq.BedRecord;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.SepSign;
import com.novelbio.generalConf.TitleFormatNBC;

/**
 * 读取Rfam的文件，获得RfamID对应的具体信息
 * @author zong0jie
 */
public class RfamStatistic {
	public static void main(String[] args) {
		RfamStatistic rfamHash = new RfamStatistic();
		String rfamFile = "/media/winE/Bioinformatics/DataBase/sRNA/rfam/rfam.txt";
		String mapBedFile = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX8/H36_rfam.bed";
		String outFile = FileOperate.changeFileSuffix(mapBedFile, "_statistics", "txt");
		rfamHash.countRfamInfo(rfamFile, mapBedFile);
	}
	
	/** RfamID2Info的信息
	 * value 有
	 * 0
	 * 1
	 * 2
	 * 3
	 *  
	 *  */
	HashMap<String, String[]> mapRfamID2Info = new HashMap<String, String[]>();	
	/** 具体看每个RfamID的counts */
	HashMap<String, Double> mapRfam2Counts = new HashMap<String, Double>();
	String outputFile = "";
	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}
	/**
	 * @param rfamFile rfam的数据库
	 * @param mapBedFile mapping至Rfam序列的bed文件
	 * @param outFile 输出文件
	 */
	public void countRfamInfo(String rfamFile, String mapBedFile) {
		readRfamFile(rfamFile);
		readRfamBed(mapBedFile);
		ArrayList<String[]> lsResult = getInfo();
		TxtReadandWrite txtOut = new TxtReadandWrite(outputFile, true);
		txtOut.ExcelWrite(lsResult);
	}
	/**
	 * 输入rfam文件，读取Rfam的信息进入hash表
	 * @param rfamFile
	 */
	private void readRfamFile(String rfamFile) {
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
	private void readRfamBed(String bedFile) {
		BedSeq bedSeq = new BedSeq(bedFile);
		for (BedRecord bedRecord : bedSeq.readLines()) {
			String RfamID = bedRecord.getRefID().split("//")[0];
			Double thisCount = (double)1/bedRecord.getMappingNum();
			if (mapRfam2Counts.containsKey(RfamID)) {
				double newCounts = mapRfam2Counts.get(RfamID) + thisCount;
				mapRfam2Counts.put(RfamID, newCounts);
				continue;
			}
			mapRfam2Counts.put(RfamID, thisCount);
		}
	}
	
	/**
	 * 获得每个RfamID对应的count信息和具体信息
	 * 得到的信息可以直接写入文本中
	 */
	private ArrayList<String[]> getInfo() {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		for (Entry<String, Double> entry : mapRfam2Counts.entrySet()) {
			String[] rfamInfo = mapRfamID2Info.get(entry.getKey());
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
		return mapRfam2Counts;
	}
	
	/** 将给定的几组miRNA的值合并起来 */
	public ArrayList<String[]> combValue(HashMap<String, HashMap<String, Double>> mapPrefix2_mapMiRNAMature2Value) {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		String[] title = getTitlePre(mapPrefix2_mapMiRNAMature2Value);
		lsResult.add(title);
		
		HashSet<String> setMirNameAll = getAllName(mapPrefix2_mapMiRNAMature2Value);
		
		for (String mirName : setMirNameAll) {
			ArrayList<String> lsTmpResult = new ArrayList<String>();
			String[] miRNAinfo = new String[title.length + 2];
			miRNAinfo[0] = mirName;
			for (int i = 2; i < title.length; i++) {
				HashMap<String, Double> mapMirna2Value = mapPrefix2_mapMiRNAMature2Value.get(title[i]);
				Double value = mapMirna2Value.get(mirName);
				if (value == null) {
					miRNAinfo[i] = 0 + "";
				} else {
					miRNAinfo[i] = value.intValue() + "";
				}
			}
			String[] seqName = mirName.split(SepSign.SEP_ID);
			miRNAinfo[miRNAinfo.length - 1] = getSeq(seqName[0], seqName[1]);
			lsResult.add(miRNAinfo);
		}
		return lsResult;
	}
	
	/** 返回涉及到的所有miRNA的名字 */
	private String[] getTitlePre(HashMap<String, ? extends Object> mapPrefix2Info) {
		String[] title = new String[mapPrefix2Info.size() + 2];
		title[0] = TitleFormatNBC.RfamID.toString();
		int i = 1;
		for (String prefix : mapPrefix2Info.keySet()) {
			title[i] = prefix;
			i ++;
		}
		title[title.length - 1] = TitleFormatNBC.mirPreSequence.toString();
		return title;
	}
	
	/** 返回涉及到的所有miRNA的名字 */
	private HashSet<String> getAllName(HashMap<String, HashMap<String, Double>> mapPrefix2_mapMiRNA2Value) {
		LinkedHashSet<String> setMirNameAll = new LinkedHashSet<String>();
		for (HashMap<String, Double> mapMiRNA2Value : mapPrefix2_mapMiRNA2Value.values()) {
			for (String miRNAname : mapMiRNA2Value.keySet()) {
				setMirNameAll.add(miRNAname);
			}
		}
		return setMirNameAll;
	}
}
