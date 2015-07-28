package com.novelbio.analysis.seq.denovo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.novelbio.analysis.seq.genome.gffOperate.GffFile;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class SplitNrByDiv {
//	public static String path = "E:\\上海烈冰\\数据库\\Tax_Database\\taxdump\\";
//	public static String resultpath = "E:\\上海烈冰\\数据库\\Tax_Database\\taxdump\\Result2\\";
//	public static String node = "UNA";
	static Map<Integer, Integer> mapGi2TaxId = new HashMap<>();
	static Map<Integer, Integer> mapTax2DivId = new HashMap<>();
	public static void main(String[] args) {
		String path = args[0];
		String resultpath = args[1];
		String node = args[2];
//		String divisionFile =  path + "division.dmp";
		String giToTaxIdFile =  path + "parGiToTax_" + node + ".txt.gz";
		String taxNodeFile =  path + node+  ".nodes.txt";
		String nrFile = path + "nr.gz";	
//		String nrFile = "E:\\上海烈冰\\数据库\\Nr所有蛋白序列\\nr.gz";	
//		String parGiToTaxid = resultpath + "parGiToTax_" + node + ".txt";
		
		TxtReadandWrite txtTaxNodeRead = new TxtReadandWrite(taxNodeFile);
		for (String string : txtTaxNodeRead.readlines()) {		
			TaxNode taxNode = new TaxNode();
			TaxNode taxNodeTmp = taxNode.genetNode(string);
			mapTax2DivId.put(taxNodeTmp.getTaxId(), taxNodeTmp.getDivsionId());	
		}
		txtTaxNodeRead.close();
		
		TxtReadandWrite txtGiToTaxRead = new TxtReadandWrite(giToTaxIdFile);
//		TxtReadandWrite txtGiWrite = new TxtReadandWrite(parGiToTaxid,true);
		for (String string : txtGiToTaxRead.readlines()) {			
				String[] giInfo =  string.split("\t");
//				if (mapTax2DivId.containsKey(Integer.parseInt(giInfo[1]))) {
//					txtGiWrite.writefileln(string);
					mapGi2TaxId.put(Integer.parseInt(giInfo[0]), Integer.parseInt(giInfo[1]));			
//				}
		}
		txtGiToTaxRead.close();
//		txtGiWrite.close();
		SplitNrByDiv splitNrByDiv = new SplitNrByDiv();
		splitNrByDiv.SplitNrByDiv(nrFile,resultpath,node);
	}
	
	private int getDiv(Integer gi) {
		if (!mapGi2TaxId.containsKey(gi)) {
			return -1;
		}
		int taxId = mapGi2TaxId.get(gi);
		if (!mapTax2DivId.containsKey(taxId)) {
			return -1;
		}
		int div = mapTax2DivId.get(taxId);
		return div;
	}

	public static void SplitNrByDiv (String NrFile,String resultpath,String node) {
		TxtReadandWrite txtNrRead = new TxtReadandWrite(NrFile);
		String txtWriteFile = resultpath + node+".fa";
		TxtReadandWrite txtWrite = new TxtReadandWrite(txtWriteFile,true);
		String seq = "";
		String id = ">";
		boolean flag = false;
		for (String string : txtNrRead.readlines()) {		
			if (string.startsWith(">")) {
				if (flag) {
					txtWrite.writefileln(seq);
				}
				seq = "";
				flag = false;
				HashMap<Integer, String> mapGI2Name = SplitNrByDiv.SplitNrID(string.substring(1));
				id = ">";
				Iterator iterator = mapGI2Name.keySet().iterator();
				while (iterator.hasNext()) {
					int gi = (int) iterator.next();
					if (id.equals(">")) {
						id += mapGI2Name.get(gi);
					} else {
						id += "@" + mapGI2Name.get(gi);
					}
					SplitNrByDiv splitNrByDiv = new SplitNrByDiv();
					int div = splitNrByDiv.getDiv(gi);
					if (div>-1) {
						flag = true;
					}
				}
			} else {
				if (!((id.equals(">")) || (id ==null))) {
					if (flag) {
						txtWrite.writefileln(id);
					}
					id =">";
				}
				if (seq.equals("")) {
					seq +=  string;
				} else {
					seq = seq + "\n" +  string;
				}
			}	
	}
		if (flag) {
			txtWrite.writefileln(seq);
		}
		txtNrRead.close();
		txtWrite.close();
	}
	
	public static HashMap<Integer, String> SplitNrID(String faId) {
		HashMap<Integer, String> mapGI2Name = new HashMap<>(); 
		String[] idInfo =  faId.split("gi");
		for (int i = 1; i < idInfo.length; i++) {			
			if (idInfo[i].indexOf("|")>-1) {
				String[] arrNrID= idInfo[i].split("\\|");
				if ((arrNrID.length>3) && (isNum(arrNrID[1]) && (!arrNrID[3].equals("")) && (!(arrNrID[3]=="")))) {
					mapGI2Name.put(Integer.parseInt(arrNrID[1]), arrNrID[3]);
				}
				
			}	
		}
		return mapGI2Name;
	}

	public static boolean isNum(String str) {
		return str.matches("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$");
	}
}
