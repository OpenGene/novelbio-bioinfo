package com.novelbio.database.service.servgeneanno;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.inject.Inject;

import org.broadinstitute.sting.utils.collections.CircularArray.Int;
import org.springframework.stereotype.Service;

import com.novelbio.database.domain.geneanno.TaxInfo;
import com.novelbio.database.mapper.geneanno.MapFSTaxID;
import com.novelbio.database.mapper.geneanno.MapNCBIID;
import com.novelbio.database.mapper.geneanno.MapTaxID;
import com.novelbio.database.service.AbsGetSpring;

@Service
//public class ServTaxID extends AbsGetSpring implements MapTaxID{
public class ServTaxID   implements MapTaxID{
	HashMap<String, Integer> hashNameTaxID = new LinkedHashMap<String, Integer>();
	HashMap<Integer, String> hashTaxIDName = new LinkedHashMap<Integer, String>();
	
	
	
	@Inject
	private MapTaxID mapTaxID;
//	public ServTaxID()
//	{
//		mapTaxID = (MapTaxID) factory.getBean("mapTaxID");
//	}
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
	 * ���س�������taxID
	 * @return
	 */
	public HashMap<String, Integer> getHashNameTaxID() {
		setHashTaxID();
		return hashNameTaxID;
	}
	/**
	 * ����taxID�Գ�����
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