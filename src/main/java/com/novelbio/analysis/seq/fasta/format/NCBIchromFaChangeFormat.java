package com.novelbio.analysis.seq.fasta.format;

import java.util.ArrayList;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 修正从NCBI上下载的序列，将fasta格式中的序列名改为文件名
 * 然后合并为一个文件
 * @author zong0jie
 */
public class NCBIchromFaChangeFormat {
	public static void main(String[] args) {
		NCBIchromFaChangeFormat ncbIchromFaChangeFormat = new NCBIchromFaChangeFormat();
		ncbIchromFaChangeFormat.setChromFaPath("/media/winE/Bioinformatics/genome/pig/sus10_NCBI/ChromFa", "");
		ncbIchromFaChangeFormat.writeToSingleFile("/media/winE/Bioinformatics/genome/pig/sus10_NCBI/ChromFa/all/chrAll.fa");
	}
	
	String chrFile = ""; String regx = null;
	public void setChromFaPath(String chromFaPath, String regx) {
		this.chrFile = chromFaPath;
		this.regx = regx;
	}
	public void writeToSingleFile(String outFile) {
		TxtReadandWrite txtWrite = new TxtReadandWrite(outFile, true);
		for (String[] chrFileName : initialAndGetFileList()) {
			String fileName = getFileName(chrFileName);
			TxtReadandWrite txtRead = new TxtReadandWrite(fileName, false);
			writeToFile(chrFileName[0], txtRead, txtWrite);
		}
		txtWrite.close();
	}
	
	/** 初始化并返回文件夹中的所有符合正则表达式的文本名<br>
	 * string[2] 1:文件名 2：后缀 */
	private ArrayList<String[]> initialAndGetFileList() {
		chrFile = FileOperate.addSep(chrFile);
		if (regx.equals("") || regx == null)
			regx = "\\bchr\\w*";
		return FileOperate.getFoldFileName(chrFile,regx, "*");
	}
	private String getFileName(String[] chrFileName) {
		String fileNam;
		if (chrFileName[1].equals(""))
			fileNam = chrFile + chrFileName[0];
		else
			fileNam = chrFile + chrFileName[0] + "." + chrFileName[1];
		return fileNam;
	}
	/**
	 * 将chr合并起来，并且将第一行的名字改为chrID
	 * @param txtRead
	 * @param txtWrite
	 */
	private void writeToFile(String chrID, TxtReadandWrite txtRead, TxtReadandWrite txtWrite) {
		txtWrite.writefileln(">" + chrID);
		for (String seq : txtRead.readlines(2)) {
			txtWrite.writefileln(seq);
		}
		txtRead.close();
	}
}
