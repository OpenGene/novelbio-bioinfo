package com.novelbio.analysis.seq.fasta.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 修正从NCBI上下载的序列，将fasta格式中的序列名改为文件名
 * 然后合并为一个文件
 * @author zong0jie
 */
public class NCBIchromFaChangeFormat {
	public static void main(String[] args) {
		String file = "/media/winE/Bioinformatics/genome/rice/tigr7/ChromFa";
		String out = file + "/all/tigr7chrAll.fa";
		FileOperate.createFolders(FileOperate.getParentPathName(out));
		NCBIchromFaChangeFormat ncbIchromFaChangeFormat = new NCBIchromFaChangeFormat();
		ncbIchromFaChangeFormat.setChromFaPath(file, "");
		ncbIchromFaChangeFormat.writeToSingleFile(out);
	}
	
	String chrFile = ""; String regx = "\\bchr\\w*";
	
	public void setChromFaPath(String chromFaPath, String regx) {
		this.chrFile = chromFaPath;
		this.regx = regx;
	}
	public void writeToSingleFile(String outFile) {
		TxtReadandWrite txtWrite = new TxtReadandWrite(outFile, true);
		for (String chrFileName : initialAndGetFileList()) {
			TxtReadandWrite txtRead = new TxtReadandWrite(chrFileName, false);
			writeToFile(FileOperate.getFileNameSep(chrFileName)[0], txtRead, txtWrite);
		}
		txtWrite.close();
	}
	
	/** 初始化并返回文件夹中的所有符合正则表达式的文本名<br>
	 * string[2] 1:文件名 2：后缀 */
	private ArrayList<String> initialAndGetFileList() {
		chrFile = FileOperate.addSep(chrFile);
		if (regx.equals("") || regx == null) {
			regx = "\\bchr\\w*";
		}
		
		final PatternOperate patNum = new PatternOperate("\\d+", false);
		ArrayList<String> lsFileName = FileOperate.getFoldFileNameLs(chrFile,regx, "*");
		//按照序号进行排序
		//这个是为了让GATK可以顺利运行
		Collections.sort(lsFileName, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				String id1 = patNum.getPatFirst(FileOperate.getFileName(o1));
				String id2 = patNum.getPatFirst(FileOperate.getFileName(o2));
				if (id1 == null && id2 == null) {
					return FileOperate.getFileName(o1).compareTo(FileOperate.getFileName(o2));
				} else if (id1 == null) {
					return 1;
				} else if (id2 == null) {
					return -1;
				} else {
					Integer num1 = Integer.parseInt(id1);
					Integer num2 = Integer.parseInt(id2);
					return num1.compareTo(num2);
				}
			}
		});
		return lsFileName;
	}
	
	/**
	 * 将chr合并起来，并且将第一行的名字改为chrID，并且小写
	 * @param txtRead
	 * @param txtWrite
	 */
	private void writeToFile(String chrID, TxtReadandWrite txtRead, TxtReadandWrite txtWrite) {
		txtWrite.writefileln(">" + chrID.toLowerCase());
		for (String seq : txtRead.readlines(2)) {
			txtWrite.writefileln(seq);
		}
		txtRead.close();
	}
}
