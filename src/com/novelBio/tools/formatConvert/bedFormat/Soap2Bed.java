package com.novelBio.tools.formatConvert.bedFormat;

import java.io.BufferedReader;
import java.util.ArrayList;

import com.novelBio.base.dataOperate.TxtReadandWrite;


public class Soap2Bed {
	/**
	 * �?oap??��???正�??��?�?	 * @param soapFile 
	 * @param outPut1 正�?
	 * @param outPut2 �??
	 * @param outError ???信�?
	 * @throws Exception
	 */
	public static void getBed2Macs(String soapFile,String outPut1,String outPut2,String outError) throws Exception {
		TxtReadandWrite txtSoap = new TxtReadandWrite();
		txtSoap.setParameter(soapFile, false, true);
		TxtReadandWrite txtOut1 = new TxtReadandWrite();
		txtOut1.setParameter(outPut1, true, false);
		TxtReadandWrite txtOut2 = new TxtReadandWrite();
		txtOut2.setParameter(outPut2, true, false);
		TxtReadandWrite txtOuterror = new TxtReadandWrite();
		txtOuterror.setParameter(outError, true, false);
		
		String content = "";
		BufferedReader readSoap = txtSoap.readfile();
		String tmpcontent=""; String tmp = "";String tmpPrespre = "";
		String[] tmpresPre =null;
		while ((content = readSoap.readLine()) != null) {
			String[] ss = content.split("\t");
			String tmpres = ss[7] + "\t"+ ss[8] +"\t"+ (Long.parseLong(ss[8])+49)+"\t"+ ss[3]+"\t"+ss[9]+"\t"+ss[6];
			//tmpPrespre only save content while ss[0].split("#/")[1].equals("1")
			if (ss[0].split("#/")[1].equals("1")) {
				tmpcontent = content;
				tmpresPre = ss;
				tmpPrespre = tmpres;
				continue;
			}
			
			if ((ss[0].split("#/")[1].equals("2")&&ss[6].equals("-"))
					&& tmpresPre[0].split("#/")[1].equals("1")&&tmpresPre[6].equals("+")
			)
			{
				txtOut1.writefile(tmpPrespre+"\n"+tmpres+"\n");
			}
			else if ((ss[0].split("#/")[1].equals("2")&&ss[6].equals("+"))
					&& tmpresPre[0].split("#/")[1].equals("1")&&tmpresPre[6].equals("-")
			) 
			{
				txtOut2.writefile(tmpPrespre+"\n"+tmpres+"\n");
			}
			else {
				txtOuterror.writefile(tmpcontent+"\n"+content+"\n");
			}
		}
	}
	/**
	 * �?oap�????acs�????ed??�� 
	 * @param soapFile
	 * @param outPut1
	 * @throws Exception
	 */
	public static void getBed2Macs(String soapFile,String outPut1) throws Exception {
		TxtReadandWrite txtSoap = new TxtReadandWrite();
		txtSoap.setParameter(soapFile, false, true);
		TxtReadandWrite txtOut1 = new TxtReadandWrite();
		txtOut1.setParameter(outPut1, true, false);


		String content = "";
		BufferedReader readSoap = txtSoap.readfile();
		while ((content = readSoap.readLine()) != null) {
			String[] ss = content.split("\t");
			String tmpres = ss[7] + "\t"+ ss[8] +"\t"+ (Long.parseLong(ss[8])+49)+"\t"+ ss[3]+"\t"+ss[9]+"\t"+ss[6];
			txtOut1.writefile(tmpres+"\n");
		}
	}
	
	/**
	 * �?��?��?�?��txt???�?????
	 * @param inFile
	 * @param percent �?��?��?�?�?0�?0�?0...100
	 * @param outFile
	 * @throws Exception
	 */
	public static void getTiduTxt(String inFile,int[] percent,String outFile) throws Exception {
		TxtReadandWrite txtIn = new TxtReadandWrite();
		txtIn.setParameter(inFile, false, true);
		//�??大�??��???00以�??
		for (int i = 0; i < percent.length; i++) {
			if (percent[i]>100) {
				percent[i] = 100;
			}
		}
		ArrayList<TxtReadandWrite> lstxtWrite = new ArrayList<TxtReadandWrite>();
		for (int i = 0; i < percent.length; i++) {
			TxtReadandWrite txtWrite = new TxtReadandWrite();
			txtWrite.setParameter(outFile+percent[i], true, false);
			lstxtWrite.add(txtWrite);
		}
		int rowAllNum = txtIn.ExcelRows();
		BufferedReader reader = txtIn.readfile();
		String content = "";
		int rowNum = 0;
		while ((content = reader.readLine()) != null) {
			for (int i = 0; i < percent.length; i++) {
				 int tmpNum =percent[i]*rowAllNum;
				if (rowNum<tmpNum/100) {
					lstxtWrite.get(i).writefile(content+"\n");
				}
			}
			rowNum++;
		}
		for (TxtReadandWrite txtReadandWrite : lstxtWrite) {
			txtReadandWrite.close();
		}
		txtIn.close();
	}
	
	
	
}
