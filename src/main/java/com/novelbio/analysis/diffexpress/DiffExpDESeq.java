package com.novelbio.analysis.diffexpress;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileHadoop;
import com.novelbio.generalconf.TitleFormatNBC;

import freemarker.template.Template;

/**
 * 1. 输入一个文本，并设定geneID列，该列没有重复
 * 2. 标记有几列是A组，有几列是B组，有几列是C组
 * 3. 设定比较组 A比B，B比C之类
 * 4. 将geneID列和所有组提取出来，成为一个单独文本
 * 5. 自动生成DEseq脚本，然后送DEseq分析
 * 调用DEseq算法，适用于数reads的试验，譬如miRNAseq或DGE
 * @author zong0jie
 */
@Component
@Scope("prototype")
public class DiffExpDESeq extends DiffExpAbs {
	private static final Logger logger = Logger.getLogger(DiffExpDESeq.class);
	/** 实验是否有重复，貌似有一次重复就算有重复了 */
	boolean isRepeatExp = false;
	
	/** 有parametric和local两个选项 */
	boolean fitTypeParametric = true;
	/** 仅供测试 */
	public boolean isRepeatExp() {
		return isRepeatExp;
	}
	/** 只有parametric和local两个选项
	 * 
	 * @param fitTypeParametric true表示Parametric，false表示local
	 */
	public void setFitType(boolean fitTypeParametric) {
		this.fitTypeParametric = fitTypeParametric;
	}
	
	protected void setOutScriptPath() {
		outScript = workSpace + "deseq_"+ DateUtil.getDateAndRandom() +".R";
	}
	protected void setFileNameRawdata() {
		fileNameRawdata = workSpace + "deseqGeneInfo_"+ DateUtil.getDateAndRandom() + ".txt";
	}
	
	protected String generateScript() {
		Map<String,Object> mapData = new HashMap<String, Object>();
		mapData.put("workspace", getWorkSpace());
		mapData.put("filename", getFileName());
		mapData.put("Group", getGroupFactorAndSetRepeatExp());
		mapData.put("isRepeatExp", isRepeatExp);
		mapData.put("mapGroup2Out", getCompareAndWriteToFile());
		mapData.put("isSensitive", isSensitive);
		if (fitTypeParametric) {
			mapData.put("fitType", "parametric");
		} else {
			mapData.put("fitType", "local");
		}
		
		String scriptContent = null;
		try {
			Template template = freeMarkerConfiguration.getTemplate("/R/diffgene/DESeq.ftl");
			StringWriter sw = new StringWriter();
			TxtReadandWrite txtReadandWrite = new TxtReadandWrite(outScript, true);
			// 处理并把结果输出到字符串中
			template.process(mapData, sw);
			scriptContent = sw.toString();
			txtReadandWrite.writefile(scriptContent);
			txtReadandWrite.close();
		} catch (Exception e) {
			throw new ExceptionFreemarker("DESeq rendering error:" + outScript, e);		}
		return scriptContent;
	}
	
	/**
	 * 返回这种东西factor( c("A", "A", "B", "B", "C", "C") )
	 * 同时设定是否有重复项
	 * @return
	 */
	private String getGroupFactorAndSetRepeatExp() {
		HashSet<String> setSearchDuplicateGroup = new HashSet<String>();
		String result = "";
		result = CmdOperate.addQuot(lsSampleColumn2GroupName.get(0)[1]);
		setSearchDuplicateGroup.add(lsSampleColumn2GroupName.get(0)[1]);
		
		for (int i = 1; i < lsSampleColumn2GroupName.size(); i++) {
			String group = lsSampleColumn2GroupName.get(i)[1];
			if (setSearchDuplicateGroup.contains(group)) {
				isRepeatExp = true;
			}
			result = result + ", " + CmdOperate.addQuot(group);
			setSearchDuplicateGroup.add(group);
		}
		return result;
	}
	
	
	private Map<String, String> getCompareAndWriteToFile() {
		Map<String, String> mapGroup2Out = new LinkedHashMap<String, String>();
		for (String outFileName : mapOutFileName2Compare.keySet()) {
			String[] compareGroup = mapOutFileName2Compare.get(outFileName);
			String compareGroupStr = CmdOperate.addQuot(compareGroup[1]) + ", " + CmdOperate.addQuot(compareGroup[0]);
			mapGroup2Out.put(compareGroupStr, FileHadoop.convertToLocalPath(outFileName.replace("\\", "/")));
		}
		return mapGroup2Out;
	}
	/**
	 * 获得选定的基因ID和具体值
	 * 排序方式按照输入的lsSampleColumn2GroupName进行排序，不做调整
	 * @return
	 * 0： geneID
	 * 1-n：value value都为整数
	 */
	protected List<String[]> getAnalysisGeneInfo() {
		ArrayList<String[]> lsResultGeneInfo = new ArrayList<>();
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
					tmpResult[i + 1] = 0 + "";
					continue;
				}
				
				try {
					double value = Double.parseDouble(strings[colNum]);
					int valueInt = (int)(value);
					tmpResult[i + 1] = valueInt + "";
				} catch (Exception e) {
					tmpResult[i + 1] = 0 + "";
				}
			
			}
			lsResultGeneInfo.add(tmpResult);
		}
		
		for (int i = 1; i < lsResultGeneInfo.size(); i++) {
			String[] strings = lsResultGeneInfo.get(i);
			for (int j = 1; i < strings.length; i++) {
				strings[j] = (int)Double.parseDouble(strings[j]) + "";
			}
		}
		return lsResultGeneInfo;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@Override
	protected void run() {
		running("DEseq");
	}
	protected List<String[]> modifySingleResultFile(String outFileName, String treatName, String controlName) {
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
			
			double caseValue = -1, ctrlValue = -1;
			try { caseValue = Double.parseDouble(tmpResult[1]); } catch (Exception e) { }
			try { ctrlValue = Double.parseDouble(tmpResult[2]); } catch (Exception e) { }
			
			try {
				Double.parseDouble(tmpResult[4]);
			} catch (Exception e) {
				if ((caseValue == 0 || tmpResult[1].equalsIgnoreCase("NA") || tmpResult[1].equalsIgnoreCase("none") ) 
						&& (ctrlValue == 0 || tmpResult[2].equalsIgnoreCase("NA") || tmpResult[2].equalsIgnoreCase("none") )) {
					tmpResult[4] = "0";
				} else if (caseValue == 0) {
					tmpResult[4] = "-20";
				} else if (ctrlValue == 0) {
					tmpResult[4] = "20";
				}
			}
			//
			lsResult.add(tmpResult);
		}
		return lsResult;
	}
 }
