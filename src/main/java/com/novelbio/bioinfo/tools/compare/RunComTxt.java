package com.novelbio.bioinfo.tools.compare;
/**
 * 功能：
 * 1. 取中位数
 * 2. 将多个excel根据ID合并为1个，方便取交集和并集的操作
 * @author zong0jie
 *
 */
public class RunComTxt {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String parentFile="/media/winE/NBC/Project/ChIPSeq_CDG110225/result/compare/comparek4k27/";
		String sepReg="";

		try {
			String file1=parentFile+"EW2W0interaction.xls";
			String file2=parentFile+"plosOneESCk4+k27.xls";
			String outPutFile = parentFile+"EW2W0interaction_vsPlosOneK4+K27";
			ComTxt.getCompFile(file1, 1, file2, 1, 11,7, sepReg, outPutFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
