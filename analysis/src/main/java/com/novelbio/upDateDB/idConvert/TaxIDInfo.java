package com.novelbio.upDateDB.idConvert;

import java.io.BufferedReader;
import java.io.ObjectInputStream.GetField;
import java.util.HashSet;

import com.novelBio.base.dataOperate.TxtReadandWrite;


public class TaxIDInfo {
	/**
	 * 读取文件，默认第一列是taxID
	 * 将taxID和指定的TaxIDfile比较，如果taxID在TaxIDfile第一列中出现，
	 * 则将inputFile该行导出。
	 * @throws Exception 
	 */
	public static void getTaxIDInfo(String taxIDfile, String inputFile, String outputFile) throws Exception 
	{
		TxtReadandWrite taxIDrReadandWrite=new TxtReadandWrite();
		taxIDrReadandWrite.setParameter(taxIDfile, false,true);
		HashSet<String> hashTaxID=new HashSet<String>();
		BufferedReader Taxreader=taxIDrReadandWrite.readfile();
		String content="";
		while ((content=Taxreader.readLine())!=null) 
		{
			String[] ss=content.split("\t");
			hashTaxID.add(ss[0]);
		}
		
		TxtReadandWrite inputReadandWrite=new TxtReadandWrite();
		inputReadandWrite.setParameter(inputFile,false, true);
		
		TxtReadandWrite outputReadandWrite=new TxtReadandWrite();
		outputReadandWrite.setParameter(outputFile, true,false);
		
		BufferedReader inputrReader=inputReadandWrite.readfile();
		
		String content2="";
		while((content2=inputrReader.readLine())!=null)
		{
			String[] ss=content2.split("\t");
			if (hashTaxID.contains(ss[0])) 
			{
				outputReadandWrite.writefile(content2+"\n", false);
			}
		}
		outputReadandWrite.writefile("", true);
	}


}
