package com.novelBio.tools.compare;

public class RunComTxt {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String parentFile="/media/winE/NBC/Project/ChIPSeq_CDG110225/result/compare/compare2plos/";
		String sepReg="[/ ]+";

		try {
			String file1=parentFile+"Chromatin states of analyzed promoters in mES cells.xls";
			String file2=parentFile+"k4_peakFilter.xls";
			String outPutFile = parentFile+"PlosmESvsk4";
			ComTxt.getCompFile(file1, 1, file2, 1, 6, 10, sepReg, outPutFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		try {
			String file1=parentFile+"Chromatin states of analyzed promoters in mES cells.xls";
			String file2=parentFile+"k0_peakFilter.xls";
			String outPutFile = parentFile+"PlosmESvsk0";
			ComTxt.getCompFile(file1, 1, file2, 1, 6, 10, sepReg, outPutFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
