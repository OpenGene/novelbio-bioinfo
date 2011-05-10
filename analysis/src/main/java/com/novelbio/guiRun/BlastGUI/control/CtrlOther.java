package com.novelbio.guiRun.BlastGUI.control;

import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.database.DAO.FriceDAO.DaoFSTaxID;
import com.novelbio.database.entity.friceDB.TaxInfo;


public class CtrlOther {
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
}
