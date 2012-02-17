package com.novelbio.analysis.tools;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.LinkedList;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class PreviewFile {
	public static void main(String[] args) {
		String parent = "/media/winD/ElectronicBook/coding&math/notes/java/对文本文件读取和写入.txt";
		String grepContent = "FileReader fileread=new";
		TxtReadandWrite txtRead = new TxtReadandWrite(parent, false);
		txtRead.setGrepContent(grepContent);
		ArrayList<String> lsResult = txtRead.grepInfo(4, false, false);
		for (String string : lsResult) {
			System.out.println(string);
		}
	}
	/**
	 * 压缩方式
	 */
	String zipType = "";
	/**
	 * TxtReadAndWrite里面有几种zipType
	 * @param zipType
	 */
	public void setZipType(String zipType) {
		this.zipType = zipType;
	}

	/**
	 * 获取抓取信息以及其前后几行的信息
	 * @param txtFile
	 * @param zipType
	 * @param grepContent 可以是正则表达式
	 * @param range
	 * @param regx 是否是正则表达式，如果是正则表达式那么速度会慢
	 * @return
	 */
	private LinkedList<String> grepInfo(String txtFile, String zipType, String grepContent, int range, boolean caseSensitive, boolean regx)
	{
		LinkedList<String> lsResult = new LinkedList<String>();
		TxtReadandWrite txtRead = new TxtReadandWrite(zipType, txtFile);
		String[] tmpContent = new String[range];
		boolean findInfo = false;//是否找到文件
		/**
		 * 存储获得string上面的string
		 * 本来想用list存储的，但是考虑效率问题，所以用string数组来存储
		 * 依次保存上面的几行信息，循环保存
		 */
		int i = 0;
		BufferedReader reader = txtRead.readfile();
		String content = "";
		while ((content = reader.readLine()) != null) {
			if (grepInfo(content, grepContent, caseSensitive, regx)) {
				int num = 0;//计数器，将前面的几行全部加入list
				i++;
				//加入前面保存的序列
				while (num < range) {
					if (i >= range) {
						i = 0;
					}
					lsResult.add(tmpContent[i]);
					num ++;
				}
				
				//将后几行加入list，然后结束
				int rest = 0;
				while ((content = reader.readLine()) != null) {
					if (rest >= range) {
						return lsResult;
					}
					lsResult.add(content);
					rest++;
				}
				return lsResult;
			}
			tmpContent[i] = content; i++;
			if (i >= range) {
				i = 0;
			}
		}
		
		lsResult.add(e);
		txtRead.close();
	}
	String grepContent = "";
	private boolean grepInfo(String content, boolean caseSensitive, boolean regx)
	{
		if (!regx) {
			if (!caseSensitive)
				if (content.toLowerCase().contains(grepContent))
					return true;
			else
				if (content.contains(grepContent)) 
					return true;
			return false;
		}
		else {
			
		}
	}
}
