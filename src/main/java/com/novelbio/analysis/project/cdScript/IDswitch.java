package com.novelbio.analysis.project.cdScript;

import java.util.ArrayList;

import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 将excel中的ID类型如
 * ID1//ID2//ID3
 * 转换为一行
 * ID1
 * ID2
 * ID3
 * @author zong0jie
 *
 */
public class IDswitch {
	
	public static void main(String[] args) {
		IDswitch iDswitch = new IDswitch();
		String txtExcelFile = "/home/zong0jie/桌面/ID切换.xls";
		String txtOut = FileOperate.changeFileSuffix(txtExcelFile, "_switch2", "txt");
		iDswitch.idSwitch(txtExcelFile, txtOut, "///");
	}
	
	private void idSwitch(String txtExcelFile, String txt, String regx)
	{
		ArrayList<String[]> lsInput = ExcelTxtRead.readLsExcelTxt(txtExcelFile, new int[]{1}, 1, -1);
		ArrayList<String> lsResult = changeID(lsInput, regx);
		TxtReadandWrite txtOut = new TxtReadandWrite(txt, true);
		txtOut.writefile(lsResult);
	}
	
	private ArrayList<String> changeID(ArrayList<String[]> lsInput,String regx) {
		ArrayList<String> lsresult = new ArrayList<String>();
		for (String[] strings : lsInput) {
			String[] ss = strings[0].trim().split(regx);
			for (String string : ss) {
				if (string.trim().equals("")) {
					continue;
				}
				lsresult.add(string.trim());
			}
		}
		return lsresult;
	}
	
	
	
}
