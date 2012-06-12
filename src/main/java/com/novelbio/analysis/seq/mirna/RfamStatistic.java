package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.novelbio.analysis.seq.BedRecord;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

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
		rfamHash.countRfamInfo(rfamFile, mapBedFile, outFile);
		
		mapBedFile = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX7/H12_rfam.bed";
		outFile = FileOperate.changeFileSuffix(mapBedFile, "_statistics", "txt");
		rfamHash.countRfamInfo(rfamFile, mapBedFile, outFile);
		
		mapBedFile = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX6/C_rfam.bed";
		outFile = FileOperate.changeFileSuffix(mapBedFile, "_statistics", "txt");
		rfamHash.countRfamInfo(rfamFile, mapBedFile, outFile);
		
		mapBedFile = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX5/N36_rfam.bed";
		outFile = FileOperate.changeFileSuffix(mapBedFile, "_statistics", "txt");
		rfamHash.countRfamInfo(rfamFile, mapBedFile, outFile);
		
		mapBedFile = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX4/N6_rfam.bed";
		outFile = FileOperate.changeFileSuffix(mapBedFile, "_statistics", "txt");
		rfamHash.countRfamInfo(rfamFile, mapBedFile, outFile);
		
		mapBedFile = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX3/H6_rfam.bed";
		outFile = FileOperate.changeFileSuffix(mapBedFile, "_statistics", "txt");
		rfamHash.countRfamInfo(rfamFile, mapBedFile, outFile);
	}
	
	
	/**
	 * RfamID2Info的信息
	 */
	HashMap<String, String[]> hashRfamID2Info = new HashMap<String, String[]>();	
	/**
	 * 具体看每个RfamID的counts
	 */
	HashMap<String, Double> hashRfam2Counts = new HashMap<String, Double>();
	
	/**
	 * @param rfamFile rfam的数据库
	 * @param mapBedFile mapping至Rfam序列的bed文件
	 * @param outFile 输出文件
	 */
	public void countRfamInfo(String rfamFile, String mapBedFile, String outFile) {
		readRfamFile(rfamFile);
		readRfamBed(mapBedFile);
		ArrayList<String[]> lsResult = getInfo();
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		txtOut.ExcelWrite(lsResult, "\t", 1, 1);
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
			hashRfamID2Info.put(key, value);
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
		for (BedRecord bedRecord : bedSeq.readlines()) {
			String RfamID = bedRecord.getRefID().split("//")[0];
			Double thisCount = (double)1/bedRecord.getMappingNum();
			if (hashRfam2Counts.containsKey(RfamID)) {
				double newCounts = hashRfam2Counts.get(RfamID) + thisCount;
				hashRfam2Counts.put(RfamID, newCounts);
				continue;
			}
			hashRfam2Counts.put(RfamID, thisCount);
		}
	}
	
	/**
	 * 获得每个RfamID对应的count信息和具体信息
	 * 得到的信息可以直接写入文本中
	 */
	private ArrayList<String[]> getInfo() {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		for (Entry<String, Double> entry : hashRfam2Counts.entrySet()) {
			String[] rfamInfo = hashRfamID2Info.get(entry.getKey());
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
}
