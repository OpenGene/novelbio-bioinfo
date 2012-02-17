package com.novelbio.analysis.tools;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.LinkedList;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class PreviewFile {
	public static void main(String[] args) {
		String parent = "/media/winD/ElectronicBook/coding&math/notes/java/���ı��ļ���ȡ��д��.txt";
		String grepContent = "FileReader fileread=new";
		TxtReadandWrite txtRead = new TxtReadandWrite(parent, false);
		txtRead.setGrepContent(grepContent);
		ArrayList<String> lsResult = txtRead.grepInfo(4, false, false);
		for (String string : lsResult) {
			System.out.println(string);
		}
	}
	/**
	 * ѹ����ʽ
	 */
	String zipType = "";
	/**
	 * TxtReadAndWrite�����м���zipType
	 * @param zipType
	 */
	public void setZipType(String zipType) {
		this.zipType = zipType;
	}

	/**
	 * ��ȡץȡ��Ϣ�Լ���ǰ���е���Ϣ
	 * @param txtFile
	 * @param zipType
	 * @param grepContent ������������ʽ
	 * @param range
	 * @param regx �Ƿ���������ʽ�������������ʽ��ô�ٶȻ���
	 * @return
	 */
	private LinkedList<String> grepInfo(String txtFile, String zipType, String grepContent, int range, boolean caseSensitive, boolean regx)
	{
		LinkedList<String> lsResult = new LinkedList<String>();
		TxtReadandWrite txtRead = new TxtReadandWrite(zipType, txtFile);
		String[] tmpContent = new String[range];
		boolean findInfo = false;//�Ƿ��ҵ��ļ�
		/**
		 * �洢���string�����string
		 * ��������list�洢�ģ����ǿ���Ч�����⣬������string�������洢
		 * ���α�������ļ�����Ϣ��ѭ������
		 */
		int i = 0;
		BufferedReader reader = txtRead.readfile();
		String content = "";
		while ((content = reader.readLine()) != null) {
			if (grepInfo(content, grepContent, caseSensitive, regx)) {
				int num = 0;//����������ǰ��ļ���ȫ������list
				i++;
				//����ǰ�汣�������
				while (num < range) {
					if (i >= range) {
						i = 0;
					}
					lsResult.add(tmpContent[i]);
					num ++;
				}
				
				//�����м���list��Ȼ�����
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
