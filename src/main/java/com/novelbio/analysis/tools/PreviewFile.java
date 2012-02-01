package com.novelbio.analysis.tools;

import com.novelbio.base.dataOperate.TxtReadandWrite;

public class PreviewFile {
	public static void main(String[] args) {
		String fileName = "/Volumes/DATA/work/Bioinformatics/DataBase/GO/gene_association.goa_uniprot.gz";
		int Num = 500;
		String fileFormat = TxtReadandWrite.GZIP;
		previewGZ(fileName, Num, fileFormat);
	}
	/**
	 * 看gz压缩格式的文本的内容
	 */
	private static void previewGZ(String gzfile, int ReadNum, String fileFormat)
	{
		TxtReadandWrite txtRead = new TxtReadandWrite(fileFormat, gzfile);
		int i = 0;
		Iterable<String> itString = txtRead.readlines(1);		
		for (String string : itString) {
			System.out.println(string);
			i ++;
			if (i>ReadNum) {
				break;
			}
		}
	}
}
