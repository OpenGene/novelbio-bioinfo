package com.novelbio.analysis.tools.formatConvert;

import java.io.BufferedReader;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.novelbio.analysis.seq.genome.getChrSequence.ChrStringHash;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class ChromFaConvert {

	/**
	 * ��ChrFa�ļ���ÿ�еĻ��з�ת��Ϊ"\n",
	 * ��Ϊ��ЩchromFa���ļ�����\r\n��β�ģ�Ʃ��ˮ����chromFa����ô�ڲ������������ȡ���ļ�ʱ������Ĭ�ϻ��з�ֻ��1λ��
	 * ���Ի���ɶ�ȡ��λ������ÿ�����µ�chromFa����Ҫ�øó����ȶ�����д���
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			changeFormat("/media/winE/Bioinformatics/GenomeData/Rice/TIGRRice/ChromFa");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * ��ChrFa�ļ���ÿ�еĻ��з�ת��Ϊ"\n",
	 * @param chrFilePath
	 * @throws Exception
	 */
	public static void changeFormat(String chrFilePath) throws Exception {
		if (!chrFilePath.endsWith(File.separator)) {  
			chrFilePath = chrFilePath + File.separator;  
		}
		ArrayList<String[]> chrFile=FileOperate.getFoldFileName(chrFilePath, "\\bchr\\w*", "*");
		for (int i = 0; i < chrFile.size(); i++) 
		{
			String[] chrFileName=chrFile.get(i);
			String fileNam=""; String fileNamNew = "";
			if(chrFileName[1].equals(""))
			{
				fileNam = chrFilePath + chrFileName[0];
				fileNamNew = chrFilePath + chrFileName[0] + ".fa";
			}
			else
			{
				fileNam = chrFilePath + chrFileName[0] + "." + chrFileName[1];
				fileNamNew = chrFilePath+chrFileName[0] + "." + chrFileName[1] + "new";
			}
			TxtReadandWrite txtFile = new TxtReadandWrite();
			txtFile.setParameter(fileNam, false, true);
			BufferedReader chrReader = txtFile.readfile();
			
			TxtReadandWrite txtFileNew = new TxtReadandWrite();
			txtFileNew.setParameter(fileNamNew, true, false);
			
			String content = "";
			while ((content = chrReader.readLine()) != null) {
				txtFileNew.writefile(content+"\n",false);
			}
			txtFileNew.writefile("");
			txtFile.close();
			txtFileNew.close();
		}

	}
}
