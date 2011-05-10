package com.novelbio.annotation.GO.execute;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import com.novelBio.base.dataOperate.TxtReadandWrite;
import com.novelBio.base.fileOperate.FileOperate;


/**
 * 给php使用的GO软件
 * @author zong0jie
 *
 */
public class runGOphp {
	static String param = "param.txt";
	static double evalue = 1e-10;
	static String resultExcel2003elim = "goResult/GOresultElim";
	static String resultExcel2003QM = "goResult/GOresultQM";
	
	static String OKflag = "OK";
	
	public static void main(String[] args) {
		try {
			String thisFilePath = runGOphp.class.getResource("/").toURI().getPath();
			System.out.println(thisFilePath);
		///	String thisFilePath = "/media/winD/fedora/workspace/NBCphp/CodeIgniter_2.0.0/gofile/";
			getRun(thisFilePath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void getRun(String path) throws Exception
	{
		
		 if (!path.endsWith(File.separator)) {
    		 path = path + File.separator;  
	         }
		 //读入参数
		 param = path + param;
		TxtReadandWrite txtParamRead = new TxtReadandWrite();
		txtParamRead.setParameter(param, false, true);
		List<String> lsParam = txtParamRead.readfileLs();
		HashMap<String, String> hashParam = new HashMap<String, String>();
		for (String string : lsParam) 
		{
			String[] tmp = string.split("\t");
			if (tmp.length == 1) {
				hashParam.put(tmp[0], "");
				continue;
			}
			hashParam.put(tmp[0],tmp[1]);
		}
	
		int QTaxID = Integer.parseInt(hashParam.get("species"));
		int STaxID = Integer.parseInt(hashParam.get("BlastTax"));
		String enrichMethod = hashParam.get("GoSelectGroup");//elim和QM
		String GoClass = hashParam.get("GoTermGroup");
		int[] colID = new int[2];
		colID[0] = Integer.parseInt(hashParam.get("AccCol"));
		colID[1] = Integer.parseInt(hashParam.get("ExpCol"));
		boolean blast = false;
		if (hashParam.get("Blast").equals("on")) {
			blast = true;
		}
		boolean cluster = false;
		if (hashParam.get("Cluser").equals("on")) {
			cluster = true;
		}
		String geneFile=path+hashParam.get("GOanalysis");
		String backGroundFile=path+hashParam.get("BG");
		double up = Double.parseDouble(hashParam.get("up"));
		double down = Double.parseDouble(hashParam.get("down"));
		
		resultExcel2003elim = path+"goResult/GOresultElim";
		resultExcel2003QM = path+"goResult/GOresultQM";
		
		FileOperate.DeleteFolder(path+"goResult");
		FileOperate.createFolder(path+"goResult");
		
		//////////////正式开始Go分析///////////////////////////////////////////////
		if (enrichMethod.equals("QM")) {
			if (cluster) {
				GoFisher.getGoRunQM(QTaxID, geneFile, GoClass, colID, backGroundFile, blast, STaxID, evalue, resultExcel2003QM);
			}
			else {
				GoFisher.getGoRunQM(QTaxID, geneFile, GoClass, colID, up, down, backGroundFile, resultExcel2003QM+".xls", blast, STaxID, evalue, true);
			}
		}
		if (enrichMethod.equals("elim")) {
			if (cluster) {
				GoFisher.getGoRunElim(geneFile, GoClass, colID, backGroundFile, QTaxID, blast, STaxID, evalue, resultExcel2003elim);
			}
			else {
				GoFisher.getGoRunElim(geneFile, GoClass, colID, up, down, backGroundFile, QTaxID, blast, STaxID, evalue, resultExcel2003elim+".xls");
			}
		}
//		FileOperate.delFile(param);
//		FileOperate.delFile(backGroundFile);
//		FileOperate.delFile(geneFile);
	}
}
