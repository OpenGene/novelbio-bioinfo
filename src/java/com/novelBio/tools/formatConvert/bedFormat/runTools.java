package com.novelBio.tools.formatConvert.bedFormat;

public class runTools {

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try {
//			WCM2Bed.cutBedbyChrID("/media/winE/NBC/Project/ChIPSeq_CDG101101/mapping/mR1/mR1allsort.bed",
//					"/media/winE/NBC/Project/ChIPSeq_CDG101101/mapping/mR1/CombineChainByChrID/", "mR1all.bed");
			WCM2Bed.cutBedbyChrID("/media/winE/NBC/Project/ChIPSeq_CDG101101/mapping/mRNP/mRNPallsort.bed",
					"/media/winE/NBC/Project/ChIPSeq_CDG101101/mapping/mRNP/CombineChainByChrID/", "mRNPall.bed");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
