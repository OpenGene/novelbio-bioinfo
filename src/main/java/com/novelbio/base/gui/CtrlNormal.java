package com.novelbio.base.gui;

import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.database.entity.friceDB.TaxInfo;
import com.novelbio.database.mapper.geneanno.MapFSTaxID;


public class CtrlNormal {	
	
	/**
	 * ��ȡ���ݿ��е�taxID�������е�species��ȡ����������ΪtaxID,species����
	 * ������Ǹ������˵�ѡ����������
	 * @return
	 * HashMap - key:String speciesName value:Integer taxID
	 */
	public static HashMap<String, Integer> getSpecies() 
	{
		TaxInfo taxInfo = new TaxInfo();
		ArrayList<TaxInfo> lsTaxID = MapFSTaxID.queryLsTaxInfo(taxInfo);
		HashMap<String, Integer> hashTaxID = new HashMap<String, Integer>();
		for (TaxInfo taxInfo2 : lsTaxID) {
			if (taxInfo2.getAbbr() == null || taxInfo2.getAbbr().trim().equals("")) {
				continue;
			}
			hashTaxID.put(taxInfo2.getLatin().trim(), taxInfo2.getTaxID());
		}
		return hashTaxID;
	}
	
	/**
	 * ָ��GoClass��ȫ��������ĳ��GOclass
	 */
	public static String getGoClass(String goClass) 
	{
		if (goClass.equals("Biological Process")) {
			return "P";
		}
		else if (goClass.equals("Molecular Function")) {
			return "F";
		}
		else if (goClass.equals("Cellular Component")) {
			return "C";
		}
		else {
			return "";
		}
	}
	
}
