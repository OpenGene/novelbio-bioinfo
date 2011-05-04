package com.novelBio.tools.compare;

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
