package com.novelbio.database.service.servgeneanno;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.novelbio.database.domain.geneanno.TaxInfo;
import com.novelbio.database.mapper.geneanno.MapTaxID;
import com.novelbio.database.service.AbsGetSpring;

@Component
//public class ServTaxID extends AbsGetSpring implements MapTaxID{
public class ServTaxID extends AbsGetSpring  implements MapTaxID{
	static HashMap<String, Integer> hashNameTaxID = new LinkedHashMap<String, Integer>();
	static HashMap<Integer, String> hashTaxIDName = new LinkedHashMap<Integer, String>();

	@Autowired
	private MapTaxID mapTaxID;

	public ServTaxID()
	{
		mapTaxID = (MapTaxID) factory.getBean("mapTaxID");
	}
	@Override
	public TaxInfo queryTaxInfo(TaxInfo taxInfo) {
		// TODO Auto-generated method stub
		return mapTaxID.queryTaxInfo(taxInfo);
	}

	@Override
	public ArrayList<TaxInfo> queryLsTaxInfo(TaxInfo taxInfo) {
		// TODO Auto-generated method stub
		return mapTaxID.queryLsTaxInfo(taxInfo);
	}

	@Override
	public void InsertTaxInfo(TaxInfo taxInfo) {
		mapTaxID.InsertTaxInfo(taxInfo);
		
	}

	@Override
	public void upDateTaxInfo(TaxInfo taxInfo) {
		mapTaxID.upDateTaxInfo(taxInfo);
	}
	/**
	 * 返回常用名对taxID
	 * @return
	 */
	public HashMap<String, Integer> getHashNameTaxID() {
		setHashTaxID();
		return hashNameTaxID;
	}
	/**
	 * 返回taxID对常用名
	 * @return
	 */
	public HashMap< Integer,String> getHashTaxIDName() {
		setHashTaxID();
		return hashTaxIDName;
	}
	
	private void setHashTaxID()
	{
		if (hashNameTaxID.size()>0 && hashTaxIDName.size() > 0) {
			return;
		}
		
		TaxInfo taxInfo = new TaxInfo();
		ArrayList<TaxInfo> lsTaxID = mapTaxID.queryLsTaxInfo(taxInfo);
		for (TaxInfo taxInfo2 : lsTaxID) {
			if (taxInfo2.getAbbr() == null || taxInfo2.getAbbr().trim().equals("")) {
				continue;
			}
			hashNameTaxID.put(taxInfo2.getComName().trim(), taxInfo2.getTaxID());
			hashTaxIDName.put(taxInfo2.getTaxID(), taxInfo2.getComName().trim());
		}
	}
}
