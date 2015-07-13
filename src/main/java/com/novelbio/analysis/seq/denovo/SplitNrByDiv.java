package com.novelbio.analysis.seq.denovo;

import java.util.ArrayList;
import java.util.HashMap;

import antlr.collections.List;
import bsh.This;

import com.novelbio.base.dataOperate.TxtReadandWrite;

public class SplitNrByDiv {
//	String nrFile = "E:\\上海烈冰\\数据库\\Nr所有蛋白序列\\nr.gz";
	public static String nrFile = "E:\\上海烈冰\\数据库\\Nr所有蛋白序列\\nr_sub.txt";
	public static void main(String[] args) {
		String divisionFile = "E:\\上海烈冰\\数据库\\Tax_Database\\taxdump\\division.dmp";
		String giToTaxIdFile = "E:\\上海烈冰\\数据库\\Tax_Database\\taxdump\\gi_taxid_prot.zip";
		
		
		
//		TxtReadandWrite txtDivRead = new TxtReadandWrite(divisionFile);
//		HashMap<Integer, String> maDivIdToCode = new HashMap<>();
//		for (String string : txtDivRead.readlines()) {			
//				String[] divInfo =  string.split("\t");
//				maDivIdToCode.put(Integer.parseInt(divInfo[0]), divInfo[2]);
//		}
//		txtDivRead.close();
		
//		TxtReadandWrite txtGiToTaxRead = new TxtReadandWrite(giToTaxIdFile);
//
//		HashMap<Integer, Integer> maGiToTaxId = new HashMap<>();
//		for (String string : txtGiToTaxRead.readlines()) {			
//				String[] giInfo =  string.split("\t");
//				maGiToTaxId.put(Integer.parseInt(giInfo[0]), Integer.parseInt(giInfo[1]));				
//		}
//		txtGiToTaxRead.close();
		
//		TxtReadandWrite txtWrite = new TxtReadandWrite("e:\\上海烈冰\\数据库\\Nr所有蛋白序列\\nr_sub_id.txt", true);
		SplitNrByDiv splitNrByDiv = new SplitNrByDiv();
		splitNrByDiv.SplitNrByDiv(nrFile);
		
	}
	public static void SplitNrByDiv (String NrFile) {
		TxtReadandWrite txtNrRead = new TxtReadandWrite(NrFile);
		int i=0;
		for (String string : txtNrRead.readlines()) {			
			if (string.startsWith(">")) {
				ArrayList<Integer> giList = SplitNrByDiv.SplitNrID(string.substring(1));
				for (Integer integer : giList) {
//					System.out.println("nrInfo is " + integer);
				}
			
			}
			if (i++>2) {
				break;
			}
//			maGiToTaxId.put(Integer.parseInt(giInfo[0]), Integer.parseInt(giInfo[1]));				
	}
		txtNrRead.close();
	}
	public static ArrayList<Integer> SplitNrID(String faId) {
		ArrayList<Integer> giList = new ArrayList<>();
		String[] idInfo =  faId.split("gi");
		for (int i = 1; i < idInfo.length; i++) {
			String id =  idInfo[i].split("\\|")[1];
			giList.add(Integer.parseInt(id));
		}
		return giList;
	}
}
