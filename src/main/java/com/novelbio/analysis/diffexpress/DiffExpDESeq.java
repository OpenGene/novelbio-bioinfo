package com.novelbio.analysis.diffexpress;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.novelbio.base.PathDetail;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.DateTime;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.SepSign;
import com.novelbio.generalConf.NovelBioConst;
import com.novelbio.generalConf.TitleFormatNBC;

/**
 * 1. 输入一个文本，并设定geneID列，该列没有重复
 * 2. 标记有几列是A组，有几列是B组，有几列是C组
 * 3. 设定比较组 A比B，B比C之类
 * 4. 将geneID列和所有组提取出来，成为一个单独文本
 * 5. 自动生成DEseq脚本，然后送DEseq分析
 * 调用DEseq算法，适用于数reads的试验，譬如miRNAseq或DGE
 * @author zong0jie
 *
 */
public class DiffExpDESeq extends DiffExpAbs {
	/** 实验是否有重复，貌似有一次重复就算有重复了 */
	boolean isRepeatExp = false;
	
	public DiffExpDESeq() {
		rawScript = PathDetail.getRworkspace() + "DESeqJava.txt";
	}
	
	/** 仅供测试 */
	public boolean isRepeatExp() {
		return isRepeatExp;
	}
	protected void setOutScriptPath() {
		outScript = workSpace + "deseq_"+ DateTime.getDateAndRandom() +".R";
	}
	protected void setFileNameRawdata() {
		fileNameRawdata = workSpace + "deseqGeneInfo_"+ DateTime.getDateAndRandom() + ".txt";
	}
	protected void generateScript() {
		TxtReadandWrite txtReadScript = new TxtReadandWrite(rawScript, false);
		TxtReadandWrite txtOutScript = new TxtReadandWrite(outScript, true);
		for (String content : txtReadScript.readlines()) {
			if (content.startsWith("#workspace"))
				txtOutScript.writefileln(getWorkSpace(content));
			else if (content.startsWith("#filename"))
				txtOutScript.writefileln(getFileName(content));
			else if (content.startsWith("#compare_group")) {
				txtOutScript.writefileln(getGroupFactorAndSetIsRepeatExp(content));
			}
			else if (content.startsWith("#DuplicateExp")) {
				txtOutScript.writefileln(getIsDuplicateDate(content));
			}
			else if (content.startsWith("#CompareAndWriteInFile")) {
				for (Entry<String, String[]> entry : mapOutFileName2Compare.entrySet()) {
					String[] writeInfo = getCompareAndWriteToFile(content, entry.getValue(), entry.getKey());
					txtOutScript.writefileln(writeInfo[0]);
					txtOutScript.writefileln(writeInfo[1]);
				}
			}
			else {
				txtOutScript.writefileln(content);
			}
		}
		txtOutScript.close();
	}

	private String getGroupFactorAndSetIsRepeatExp(String content) {
		String Group = content.split(SepSign.SEP_ID)[1];
		Group = Group.replace("{$Group}", getGroupFactorAndSetRepeatExp());
		return Group;
	}
	/**
	 * 返回这种东西factor( c("A", "A", "B", "B", "C", "C") )
	 * 同时设定是否有重复项
	 * @return
	 */
	private String getGroupFactorAndSetRepeatExp() {
		HashSet<String> setSearchDuplicateGroup = new HashSet<String>();
		String result = "";
		result = "\"" + lsSampleColumn2GroupName.get(0)[1] + "\"";
		setSearchDuplicateGroup.add(lsSampleColumn2GroupName.get(0)[1]);
		
		for (int i = 1; i < lsSampleColumn2GroupName.size(); i++) {
			String group = lsSampleColumn2GroupName.get(i)[1];
			if (setSearchDuplicateGroup.contains(group)) {
				isRepeatExp = true;
			}
			result = result + ", " + "\"" + group + "\"";
			setSearchDuplicateGroup.add(group);
		}
		return result;
	}
	
	/**
	 * 根据是否重复，选择不同的代码
	 * @param content
	 * @return
	 */
	private String getIsDuplicateDate(String content) {
		String[] tmpResult = content.split(SepSign.SEP_ID);
		if (isRepeatExp) {
			return tmpResult[1];
		} else {
			return tmpResult[3];
		}
	}
	
	private String[] getCompareAndWriteToFile(String content, String[] compareGroup, String outFileName) {
		String[] writeFinal = new String[2];
		String compareGroupWrite = "\"" + compareGroup[1] + "\", \"" + compareGroup[0] + "\"";//先输入control再输入treatment
		String[] write = content.split(SepSign.SEP_ID);
		writeFinal[0] = write[1].replace("{$CompareGroup}", compareGroupWrite);
		writeFinal[1] = write[2].replace("{$OutFileName}", outFileName.replace("\\", "/"));
		return writeFinal;
	}
	/**
	 * 获得选定的基因ID和具体值
	 * 排序方式按照输入的lsSampleColumn2GroupName进行排序，不做调整
	 * @return
	 * 0： geneID
	 * 1-n：value value都为整数
	 */
	protected  ArrayList<String[]> getAnalysisGeneInfo() {
		ArrayList<String[]> lsResultGeneInfo = new ArrayList<String[]>();
		for (int m = 0; m < lsGeneInfo.size(); m++) {
			String[] strings = lsGeneInfo.get(m);

			String[] tmpResult = new String[lsSampleColumn2GroupName.size() + 1];
			tmpResult[0] = strings[colAccID];
			for (int i = 0; i < lsSampleColumn2GroupName.size(); i++) {
				int colNum = Integer.parseInt(lsSampleColumn2GroupName.get(i)[0]) - 1;
				//title
				if (m == 0) {
					tmpResult[i + 1] = strings[colNum];
					continue;
				}
				
				if (strings[colNum].equalsIgnoreCase("NA")) {
					tmpResult[i + 1] = 1 + "";
					continue;
				}
				
				double value = Double.parseDouble(strings[colNum]);
				int valueInt = (int)(value + 1);
				tmpResult[i + 1] = valueInt + "";
			}
			lsResultGeneInfo.add(tmpResult);
		}
		return lsResultGeneInfo;
	}
	@Override
	protected void run() {
		Rrunning("DEseq");
	}
	protected void modifySingleResultFile(String outFileName, String treatName, String controlName) {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		ArrayList<String[]> lsDifGene = ExcelTxtRead.readLsExcelTxt(outFileName, 1);
		String[] title = new String[]{TitleFormatNBC.AccID.toString() , treatName, controlName, TitleFormatNBC.FoldChange.toString(),
				TitleFormatNBC.Log2FC.toString(), TitleFormatNBC.Pvalue.toString(), TitleFormatNBC.FDR.toString()};
		lsResult.add(title);
		
		ArrayList<int[]> lsIndelItem = new ArrayList<int[]>();
		lsIndelItem.add(new int[]{1,-1});
		
		for (int i = 1; i < lsDifGene.size(); i++) {
			String[] tmpResult = ArrayOperate.indelElement(lsDifGene.get(i), lsIndelItem, "");
			for (int j = 0; j < tmpResult.length; j++) {
				tmpResult[j] = tmpResult[j].replace("\"", "");
			}
			//交换treatment和control
			String tmp = tmpResult[1];
			tmpResult[1] = tmpResult[2];
			tmpResult[2] = tmp;
			//
			lsResult.add(tmpResult);
		}
		//防止R还没输出结果就去读取
		try { Thread.sleep(50); } catch (Exception e) { }
		
		TxtReadandWrite txtOutFinal = new TxtReadandWrite(outFileName, true);
		txtOutFinal.ExcelWrite(lsResult);
	}
 }
