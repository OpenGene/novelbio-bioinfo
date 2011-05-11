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
	String filepath;// 得到文本文件的路径

	/**
	 * 
	 * @param filepath
	 *            要读取或写入的文件名filepath
	 * @param createNew
	 *            当文本不存在时，是否需要新建文本
	 * @param append
	 *            是接着写入还是写新的。<b>读取文本时必须设置为true</b>
	 * @return true：成功设置文本参数<br>
	 *         false：没有设好文本参数
	 */
	public boolean setParameter(String filepath, boolean createNew,
			boolean append) {
		txtfile = new File(filepath);
		if (txtfile.exists() == false) {
			if (createNew)// 如果文本文件不存在则创建它
			{
				try {
					txtfile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
				txtfile = new File(filepath); // 重新实例化
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
	 * 给定文本，将文本读取到list中
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
	 * 暂时不能用
	 * 将规则的txt文本按照excel的方法读取,自动跳过空行
	 * 
	 * @param sep
	 *            txt文本的分割符,为正则表达式，tab是"\t"
	 * @param rowStartNum
	 *            实际读取起始行
	 * @param columnStartNum
	 *            实际读取起始列
	 * @param rowEndNum
	 *            实际读取终止行
	 * @param columnEndNum
	 *            实际读取终止列,当该项=-1时，读取所有列，反正是ArrayList--String[]嘛<br>
	 *            如果该项大于最大列，那么就把本行都读取了
	 * @param colNotNone
	 *            主键列，该列不能为""，否则把该列为""的行删除，如果本项<=0，则不考虑
	 * @return 返回ArrayList<String[]> 数组,数组中null项用""替换
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
