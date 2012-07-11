package com.novelbio.analysis.diffexpress;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.SepSign;

/**
 * 必须输入一个文本，其中有几列是实验组有几列是对照组
 * 然后查找差异基因。
 * 调用DEseq算法，适用于数reads的试验，譬如miRNAseq或DGE
 * @author zong0jie
 *
 */
public class DiffExpDESeq {
	String rawScript = "/media/winE/Bioinformatics/R/Protocol/DESeq.txt";
	String workSpace = "";
	String fileNameRawdata = "";
	String outScript = "";
	/** 实验是否有重复，貌似有一次重复就算有重复了 */
	boolean repeatExp = false;
	
	ArrayList<String[]> lsGeneInfo = new ArrayList<String[]>();
	/**
	 * 一系列的表示基因信息的列
	 * 0: colNum
	 * 1: SampleGroup
	 */
	ArrayList<String[]> lsSampleColumn2GroupName;
	/**基因唯一ID，必须没有重复 */
	int colAccID = 0;
	/**
	 * 比较组，可以输入一系列组
	 * map: condition to compare group <br>
	 * list比较的信息，只有两项<br>
	 * 0：treatment<br>
	 * 1：control
	 */
	ArrayList<String[]> lsCompareGroup = new ArrayList<String[]>();
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TxtReadandWrite txtWrite = new TxtReadandWrite("Rstatistic/test",true);
		txtWrite.writefile("fesfes");
		txtWrite.close();
	}
	private void setWorkSpace() {
		workSpace = FileOperate.getProjectPath() + "Tmp/";
	}

	
	private void generateScript() {
		TxtReadandWrite txtReadScript = new TxtReadandWrite(rawScript, false);
		TxtReadandWrite txtOutScript = new TxtReadandWrite(outScript, true);
		for (String content : txtReadScript.readlines()) {
			if (content.startsWith("#workspace"))
				txtOutScript.writefileln(getWorkSpace(content));
			else if (content.startsWith("#filename"))
				txtOutScript.writefileln(getFileName(content));
			else if (repeatExp) {
				
			}
			
			
		}
	}
	
	private String getWorkSpace(String content) {
		String RworkSpace = content.split(SepSign.SEP_ID)[1];
		RworkSpace = RworkSpace.replace("$workspace", workSpace);
		return RworkSpace;
	}
	private String getFileName(String content) {
		String fileNameRawdata = content.split(SepSign.SEP_ID)[1];
		fileNameRawdata = fileNameRawdata.replace("$filename", fileNameRawdata);
		return fileNameRawdata;
	}
	private String getGroup(String content) {
		String Group = content.split(SepSign.SEP_ID)[1];
		Group = Group.replace("$Group", fileNameRawdata);
		return Group;
	}
	private String getGroupFactor() {
		String colUnKnown = "\"unknownSample\"";
		String result = "";
		//按列进行排序
		Collections.sort(lsSampleColumn2GroupName, new Comparator<String[]>() {
			public int compare(String[] o1, String[] o2) {
				Integer col1 = Integer.parseInt(o1[0]);
				Integer col2 = Integer.parseInt(o2[0]);
				return col1.compareTo(col2);
			}
		});
		int allLine = Integer.parseInt(lsSampleColumn2GroupName.get(lsSampleColumn2GroupName.size() - 1)[0]);
		
		if (!lsSampleColumn2GroupName.get(0)[0].equals("1")) {
			result = colUnKnown;
		}
		else {
			result = "\""+lsSampleColumn2GroupName.get(0)[1]+"\"";
		}
		
		int colSample = 2; int colDetail = 1;
		while (colDetail <= allLine) {
			colSample = Integer.parseInt(lsSampleColumn2GroupName.get(colDetail)[1]);
			if (colSample > colDetail) {
				colDetail++;
				continue;
			}
			else if (colSample == colDetail) {
				result =", "+ "\""+lsSampleColumn2GroupName.get(colDetail)[1]+"\"";
			}
		}
	}
}
