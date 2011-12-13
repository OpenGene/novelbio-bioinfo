package com.novelbio.analysis.project.lcy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class FilterID {
	
	public static void main(String[] args) {
		FilterID filterID = new FilterID();
		String regx1 = "(?<=Gene_Symbol=)\\w+";
		String regx2 = "(?<=REFSEQ:)\\w+";
		filterID.setRegx(regx1,regx2);
		filterID.getRegInfo("/home/zong0jie/桌面/P11169.noredundant.iTRAQ.ProteinRatio.xls", 2, "/home/zong0jie/桌面/P11169_out.txt");
	}
	
	/**
	 * 正则表达式通通忽略大小写
	 */
	HashMap<String, Pattern> hashRegx = new LinkedHashMap<String, Pattern>();
	/**
	 * 设定正则表达式，可以设定多个
	 * @param regx
	 */
	public void setRegx(String... regx) {
		for (String string : regx) {
			Pattern pattern =Pattern.compile(string, Pattern.CASE_INSENSITIVE);
			hashRegx.put(string, pattern);
		}
	}
	
	public void getRegInfo(String excelTxtFile, int colID, String outFile) {
		ArrayList<String[]> lsInfo = ExcelTxtRead.readLsExcelTxt(excelTxtFile, new int[]{colID}, 1, -1);
		ArrayList<String> lsOut = new ArrayList<String>();
		for (String[] strings : lsInfo) {
			lsOut.add(filtereID(strings[0]));
		}
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		txtOut.writefile(lsOut);
	}
	
	/**
	 * 返回抓到的序列
	 * @param content
	 * @param regx
	 * @return
	 */
	public String filtereID(String content) {
		boolean flagFind = false;//是否找到
		Matcher matcher = null;
		for (Entry<String, Pattern> entry : hashRegx.entrySet()) {
			String regxTmp = entry.getKey();
			Pattern pat = entry.getValue();
			matcher = pat.matcher(content);
			if (matcher.find()) {
				flagFind = true;
				break;
			}
		}
		
		if (flagFind) {
			return matcher.group();
		}
		else {
			return "";
		}
	}
	
	
	
	
	
	
}
