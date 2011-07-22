package com.novelbio.base.gui;

import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.database.DAO.FriceDAO.DaoFSTaxID;
import com.novelbio.database.entity.friceDB.TaxInfo;


public class CtrlNormal {	
	
	/**
	 * 读取数据库中的taxID表，将其中的species读取出来并保存为taxID,species两项
	 * 这个就是给下拉菜单选择物种名的
	 * @return
	 * HashMap - key:String speciesName value:Integer taxID
	 */
	public static HashMap<String, Integer> getSpecies() 
	{
		TaxInfo taxInfo = new TaxInfo();
		ArrayList<TaxInfo> lsTaxID = DaoFSTaxID.queryLsTaxInfo(taxInfo);
		HashMap<String, Integer> hashTaxID = new HashMap<String, Integer>();
		for (TaxInfo taxInfo2 : lsTaxID) {
			if (taxInfo2.getAbbr().trim().equals("")) {
				continue;
			}
			hashTaxID.put(taxInfo2.getLatin().trim(), taxInfo2.getTaxID());
		}
		return hashTaxID;
	}
	
	/**
	 * 指定GoClass的全名，返回某个GOclass
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
