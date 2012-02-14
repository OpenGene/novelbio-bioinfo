package com.novelbio.analysis.tools;

import java.util.ArrayList;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class PreviewFile {
	public static void main(String[] args) {
		String parent = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/all/";
		String bedFile = parent + "WEall_sorted.bed";
		String sort = FileOperate.changeFileSuffix(bedFile, "_extend", null);
		BedSeq bedSeq = new BedSeq(bedFile);
//		bedSeq.extend(240, sort);
		
		 parent = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/all/";
		 bedFile = parent + "KEall_sorted.bed";
		 sort = FileOperate.changeFileSuffix(bedFile, "_extend", null);
		 bedSeq = new BedSeq(bedFile);
		bedSeq.extend(240, sort);
		
		 parent = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/all/";
		 bedFile = parent + "W4all_sorted.bed";
		 sort = FileOperate.changeFileSuffix(bedFile, "_extend", null);
		 bedSeq = new BedSeq(bedFile);
		bedSeq.extend(240, sort);
		
		 parent = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/all/";
		 bedFile = parent + "K4all_sorted.bed";
		 sort = FileOperate.changeFileSuffix(bedFile, "_extend", null);
		 bedSeq = new BedSeq(bedFile);
		bedSeq.extend(240, sort);
	}
	/**
	 * 看gz压缩格式的文本的内容
	 */
	private static void previewGZ(String zipType, int ReadNum, String txtFile)
	{
		TxtReadandWrite txtRead = new TxtReadandWrite(txtFile, zipType);
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
	/**
	 * 获取抓取信息以及其前后几行的信息
	 * @param txtFile
	 * @param zipType
	 * @param grepContent
	 * @param range
	 * @return
	 */
	private static ArrayList<String> grepInfo(String txtFile, String zipType, String grepContent, int range)
	{
		
	}
}
