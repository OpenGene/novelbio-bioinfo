package com.novelbio.analysis.project.fy;

import com.novelbio.base.dataOperate.TxtReadandWrite;

/**
 * 删除gff中的CDS项目
 * @author zong0jie
 *
 */
public class Script {
	public static void main(String[] args) {
		removeCDS("/media/winE/bioinformaticsTools/RNA-Seq/mm9/Mus_musculus.NCBIM37.65.gff", "/media/winE/bioinformaticsTools/RNA-Seq/mm9/aaas");
	}
	/**
	 * @param inFile
	 * @param outFile
	 */
	private static void removeCDS(String inFile, String outFile) {
		TxtReadandWrite txtRead = new TxtReadandWrite(inFile, false);
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		for (String string : txtRead.readlines()) {
			if (string.startsWith("#") || string.trim().equals("")) {
				continue;
			}
			String[] ss = string.split("\t");
			if (ss[2].equals("CDS")) {
				continue;
			}
			txtOut.writefileln(string);
		}
		txtRead.close();
		txtOut.close();
	}
}
