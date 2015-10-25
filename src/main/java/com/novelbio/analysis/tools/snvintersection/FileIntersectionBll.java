package com.novelbio.analysis.tools.snvintersection;

import java.io.File;

public class FileIntersectionBll {

	public static void main(String[] args) {
		String parentPath = "/home/novelbio/intersection";
		String[] sortFileArr = new String[] { "F.indel.zl_Filter.txt", "M.indel.zl_Filter.txt",
				"P.indel.zl_Filter.txt", "F.snp.zl_Filter.txt", "M.snp.zl_Filter.txt", "P.snp.zl_Filter.txt" };
		//		for (int i = 0; i < sortFileArr.length; i++) {
		//			String filePath = parentPath + "/" + sortFileArr[i];
		//			//			FileSorter.sort(filePath, strategy);
		//		}
		String[] intersectionFileArr = new String[sortFileArr.length];
		File[] fileArray = new File[sortFileArr.length];
		for (int i = 0; i < intersectionFileArr.length; i++) {
			intersectionFileArr[i] = parentPath + "/" + sortFileArr[i] + ".sort";
			fileArray[i] = new File(intersectionFileArr[i]);
		}
		EqualAndSortStrategy4Bll strategy = new EqualAndSortStrategy4Bll();
		FileIntersection.intersection(new File[] { fileArray[0], fileArray[1], fileArray[2] }, strategy, strategy);
		FileIntersection.intersection(new File[] { fileArray[3], fileArray[4], fileArray[5] }, strategy, strategy);
		FileIntersection.intersection(new File[] { fileArray[0], fileArray[1] }, strategy, strategy);
		FileIntersection.intersection(new File[] { fileArray[0], fileArray[2] }, strategy, strategy);
		FileIntersection.intersection(new File[] { fileArray[1], fileArray[2] }, strategy, strategy);
		FileIntersection.intersection(new File[] { fileArray[3], fileArray[4] }, strategy, strategy);
		FileIntersection.intersection(new File[] { fileArray[3], fileArray[5] }, strategy, strategy);
		FileIntersection.intersection(new File[] { fileArray[4], fileArray[5] }, strategy, strategy);
	}
}
