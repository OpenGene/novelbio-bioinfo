package com.novelbio.base.dataOperate;
import info.monitorenter.cpdetector.CharsetPrinter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.google.common.io.Files;
import com.google.common.io.LineProcessor;

public class TxtRW {
	File txtfile;
	FileReader fileread;
	FileWriter filewriter;
	BufferedReader bufread;
	BufferedWriter bufwriter;
	String filepath;// �õ��ı��ļ���·��

	/**
	 * 
	 * @param filepath
	 *            Ҫ��ȡ��д����ļ���filepath
	 * @param createNew
	 *            ���ı�������ʱ���Ƿ���Ҫ�½��ı�
	 * @param append
	 *            �ǽ���д�뻹��д�µġ�<b>��ȡ�ı�ʱ��������Ϊtrue</b>
	 * @return true���ɹ������ı�����<br>
	 *         false��û������ı�����
	 */
	public boolean setParameter(String filepath, boolean createNew,
			boolean append) {
		txtfile = new File(filepath);
		if (txtfile.exists() == false) {
			if (createNew)// ����ı��ļ��������򴴽���
			{
				try {
					txtfile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
				txtfile = new File(filepath); // ����ʵ����
			} else {
				return false;
			}
		}
		try {
			filewriter = new FileWriter(txtfile, append);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * �����ı������ı���ȡ��list��
	 * @param txtPath
	 * @return
	 * @throws IOException
	 */
	public List<String> readTxt(String txtPath) throws IOException {
		File fileTxt = new File(txtPath);
		CharsetPrinter cha = new CharsetPrinter();
		String charset = cha.guessEncoding(fileTxt);
		Charset thisCharset = Charset.forName(charset);
		return Files.readLines(new File(txtPath), thisCharset);
	}
	
	/**
	 * ��ʱ������
	 * �������txt�ı�����excel�ķ�����ȡ,�Զ���������
	 * 
	 * @param sep
	 *            txt�ı��ķָ��,Ϊ������ʽ��tab��"\t"
	 * @param rowStartNum
	 *            ʵ�ʶ�ȡ��ʼ��
	 * @param columnStartNum
	 *            ʵ�ʶ�ȡ��ʼ��
	 * @param rowEndNum
	 *            ʵ�ʶ�ȡ��ֹ��
	 * @param columnEndNum
	 *            ʵ�ʶ�ȡ��ֹ��,������=-1ʱ����ȡ�����У�������ArrayList--String[]��<br>
	 *            ��������������У���ô�Ͱѱ��ж���ȡ��
	 * @param colNotNone
	 *            �����У����в���Ϊ""������Ѹ���Ϊ""����ɾ�����������<=0���򲻿���
	 * @return ����ArrayList<String[]> ����,������null����""�滻
	 * @throws Exception
	 */
	public ArrayList<String[]> readTxt(String txtPath, String sep) throws IOException 
	{
		File fileTxt = new File(txtPath);
		CharsetPrinter cha = new CharsetPrinter();
		String charset = cha.guessEncoding(fileTxt);
		Charset thisCharset = Charset.forName(charset);
		
		
		class GetSep implements LineProcessor<ArrayList<String[]>>
		{
			ArrayList<String[]> lsResult = new ArrayList<String[]>();
			int rowCount = 0;
			public boolean processLine(String line) throws IOException {
				
				
				rowCount++;
				return true;
			}

			public ArrayList<String[]> getResult() {
				// TODO Auto-generated method stub
				return null;
			}
		}
		GetSep getSep = new GetSep();
		
		Files.readLines(fileTxt, thisCharset, getSep);
		return getSep.getResult();
	}
	
	
	
}
