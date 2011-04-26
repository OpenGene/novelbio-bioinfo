package com.novelBio.tools.formatConvert;

import info.monitorenter.cpdetector.CharsetPrinter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import com.novelBio.base.dataOperate.TxtReadandWrite;



public class PreviewTxt
{
	/**
	 * 给定文本，读取前指定行并写入新文本
	 * @param fileName
	 * @param outPut
	 * @throws Exception
	 */
	public static void getFileHead2(String fileName,String outPut,int rowNum) throws Exception {
		final int thisNum = rowNum;
		File file = new File(fileName);
		CharsetPrinter charsetPrinter =new CharsetPrinter();
		CharsetPrinter cha = new CharsetPrinter();
		String charset = cha.guessEncoding(file);
		Charset thisCharset = Charset.forName(charset);
		//ThisReadLines read = new ThisReadLines();
		//Files.readLines(file, charset, read);
		ArrayList<String> result = Files.readLines(file, thisCharset, new LineProcessor<ArrayList<String>>() {

			int count = 0;
			ArrayList<String> lsResult = new ArrayList<String>();
			@Override
			public ArrayList<String> getResult() {
				return lsResult;
			}

			@Override
			public boolean processLine(String arg0) throws IOException {
				lsResult.add(arg0);
				count++;
				if (count>thisNum) {
					return false;
				}
				return true;
			}
		});
		
		TxtReadandWrite txtWrite = new TxtReadandWrite();
		txtWrite.setParameter(outPut, true, false);
		txtWrite.writefile(result);
		
	}
	/**
	 * 给定文本，读取前指定行并写入新文本，用于预览文本
	 * @param fileName
	 * @param outPut
	 * @throws Exception
	 */
	public static void getFileHead(String fileName,String outPut,int rowNum) throws Exception {
		TxtReadandWrite txtRead = new TxtReadandWrite();
		txtRead.setParameter(fileName, false, true);
		BufferedReader reader = txtRead.readfile();
		TxtReadandWrite txtWrite = new TxtReadandWrite();
		txtWrite.setParameter(outPut, true, false);
		for (int i = 0; i < rowNum; i++) {
			txtWrite.writefile(reader.readLine()+"\n");
		}
		txtWrite.close();
		
		
	}
}

