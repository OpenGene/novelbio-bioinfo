package com.novelbio.bioinfo.gwas.convertformat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.novelbio.base.dataOperate.TxtReadandWrite;

/** 把plinkped文件转化为haploview的输入文件格式 */
public class PlinkPed2HaploView {
	
	String ped;
	String mid;
	 
	public void setPlinkPedMid(String ped, String mid) {
		this.ped = ped;
		this.mid = mid;
	}
	
	/**
	 * 返回haploView的格式，注意仅支持snp
	 * 	其中N:0 A:1 T:2 G:3 C:4
	 * @param name
	 * @param lsAllele
	 * @return
	 */
	public void convert(String pedOut) {
		List<PlinkMid> lsPlinkMids = new ArrayList<>();
		TxtReadandWrite txtReadMid = new TxtReadandWrite(mid);
		for (String content : txtReadMid.readlines()) {
			if (content.startsWith("#")) {
				continue;
			}
			PlinkMid plinkMid = new PlinkMid(content);
			lsPlinkMids.add(plinkMid);
		}
		txtReadMid.close();
		
		TxtReadandWrite txtReadPed = new TxtReadandWrite(ped);
		TxtReadandWrite txtWritePed = new TxtReadandWrite(pedOut, true);
		int num = 1;
		for (String content : txtReadPed.readlines()) {
			String[] ss = content.split("\t");
			String strainName = ss[0];
			StringBuilder sBuilder = new StringBuilder();
			sBuilder.append(strainName);
			sBuilder.append("\t").append(num).append("\t0\t0\t0\t0");
			for (int i = 6; i < ss.length; i++) {
				PlinkMid plinkMid = lsPlinkMids.get(i-6);
				sBuilder.append("\t").append(getAlleleHaplo(plinkMid, ss[i]));
			}
			num++;
			txtWritePed.writefileln(sBuilder.toString());
		
		}
		txtReadPed.close();
		txtWritePed.close();
	}
	Map<String, Integer> mapBase2Int = getMapSnp2Value();
	/**
	 * 输入 A A 这种，返回可以个haploView使用的格式
	 * @param allele
	 * @return
	 */
	private String getAlleleHaplo(PlinkMid plinkMid, String allele) {
		String[] alleleUnit = allele.split(" ");
		String allele1 = getAllelePed(plinkMid.getAlt(), alleleUnit[0], plinkMid.getRef());
		String allele2 = getAllelePed(plinkMid.getAlt(), alleleUnit[1], plinkMid.getRef());
		int intAllele1 = mapBase2Int.get(allele1);
		int intAllele2 = mapBase2Int.get(allele2);
		return intAllele1 + " " + intAllele2;
	}
	
	private String getAllelePed(String alleleDefault, String allele, String ref) {
		if (!allele.equals(alleleDefault) && !allele.equals(ref+"") && !allele.equals("0") && !allele.equals("N")) {
			return alleleDefault;
		}
		return allele;
	}
	
	public static Map<String, Integer> getMapSnp2Value() {
		Map<String, Integer> mapBase2Num = new HashMap<>();
		mapBase2Num.put("A", 1);
		mapBase2Num.put("T", 2);
		mapBase2Num.put("G", 3);
		mapBase2Num.put("C", 4);
		mapBase2Num.put("N", 0);
		mapBase2Num.put("0", 0);
		return mapBase2Num;
	}
	
	public static Map<Integer, String> getMapValue2Snp() {
		Map<Integer, String> mapNum2Base = new HashMap<>();
		mapNum2Base.put(1, "A");
		mapNum2Base.put(2, "T");
		mapNum2Base.put(3, "G");
		mapNum2Base.put(4, "C");
		mapNum2Base.put(0, "N");
		return mapNum2Base;
	}
}
