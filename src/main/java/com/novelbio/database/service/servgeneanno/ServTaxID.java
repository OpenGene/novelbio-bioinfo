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
	/**
	 * ȫ������ID
	 */
	static HashMap<String, Integer> hashNameTaxID = new LinkedHashMap<String, Integer>();
	static ArrayList<Integer> lsAllTaxID = new ArrayList<Integer>();
	/**
	 * ����������д������
	 */
	static HashMap<String, Integer> hashNameTaxIDUsual = new LinkedHashMap<String, Integer>();
	static HashMap<Integer, String> hashTaxIDName = new LinkedHashMap<Integer, String>();

	@Autowired
	private MapTaxID mapTaxID;

	public ServTaxID()
	{
		mapTaxID = (MapTaxID) factory.getBean("mapTaxID");
	}
	@Override
	public TaxInfo queryTaxInfo(TaxInfo taxInfo) {
		return mapTaxID.queryTaxInfo(taxInfo);
	}
	/**
	 * @param taxID 0 �򷵻�null
	 * @return
	 */
	public TaxInfo queryTaxInfo(int taxID) {
		if (taxID == 0) {
			return null;
		}
		TaxInfo taxInfo = new TaxInfo();
		taxInfo.setTaxID(taxID);
		return mapTaxID.queryTaxInfo(taxInfo);
	}
	/**
	 * û�оͲ��룬�о�����
	 * @param taxInfo
	 */
	public void update(TaxInfo taxInfo) {
		if (taxInfo.getTaxID() == 0) {
			return;
		}
		TaxInfo taxInfoQ = queryTaxInfo(taxInfo.getTaxID());
		if (taxInfoQ == null) {
			InsertTaxInfo(taxInfo);
		}
		else if (!taxInfoQ.equals(taxInfo)) {
			upDateTaxInfo(taxInfo);
		}
	}
	@Override
	public ArrayList<TaxInfo> queryLsTaxInfo(TaxInfo taxInfo) {
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
	 * @param allSpecies
	 * @return
	 */
	public HashMap<String, Integer> getSpeciesNameTaxID(boolean allSpecies) {
		setHashTaxID();
		if (allSpecies) {
			return hashNameTaxID;
		}
		else {
			return hashNameTaxIDUsual;
		}
	}
	/**
	 * ����taxID�Գ�����
	 * @return
	 */
	public HashMap< Integer,String> getHashTaxIDName() {
		setHashTaxID();
		return hashTaxIDName;
	}
	/**
	 * ����taxID�Գ�����
	 * @return
	 */
	public ArrayList< Integer> getLsAllTaxID() {
		setHashTaxID();
		return lsAllTaxID;
	}
	private void setHashTaxID() {
		if (hashNameTaxID.size()>0 && hashTaxIDName.size() > 0) {
			return;
		}
		TaxInfo taxInfo = new TaxInfo();
		ArrayList<TaxInfo> lsTaxID = null;
		try {
			lsTaxID = mapTaxID.queryLsTaxInfo(taxInfo);
		} catch (Exception e) {
			return;
		}
		for (TaxInfo taxInfo2 : lsTaxID) {
			if (taxInfo2.getAbbr() != null || !taxInfo2.getAbbr().trim().equals("")) {
				hashNameTaxIDUsual.put(taxInfo2.getComName().trim(), taxInfo2.getTaxID());
			}
			hashNameTaxID.put(taxInfo2.getComName().trim(), taxInfo2.getTaxID());
			hashTaxIDName.put(taxInfo2.getTaxID(), taxInfo2.getComName().trim());
			lsAllTaxID.add(taxInfo2.getTaxID());
		}
	}
}
