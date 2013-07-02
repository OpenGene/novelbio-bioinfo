package com.novelbio.database.service.servgeneanno;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.novelbio.database.domain.geneanno.TaxInfo;
import com.novelbio.database.mongorepo.geneanno.RepoTaxInfo;
import com.novelbio.database.service.SpringFactory;

public class ManageTaxID {
	static double[] lock = new double[0];
	/**
	 * 全体物种ID
	 */
	static HashMap<String, Integer> hashNameTaxID = new LinkedHashMap<String, Integer>();
	static ArrayList<Integer> lsAllTaxID = new ArrayList<Integer>();
	/**
	 * 仅包含有缩写的物种
	 */
	static HashMap<String, Integer> hashNameTaxIDUsual = new LinkedHashMap<String, Integer>();
	static HashMap<Integer, String> hashTaxIDName = new LinkedHashMap<Integer, String>();
	static HashMap<Integer, TaxInfo> mapTaxID2TaxInfo = new HashMap<Integer, TaxInfo>();
	@Autowired
	private RepoTaxInfo repoTaxInfo;

	public ManageTaxID() {
		repoTaxInfo = (RepoTaxInfo)SpringFactory.getFactory().getBean("repoTaxInfo");
		setMapInfo();
	}
	
	private void setMapInfo() {
		synchronized (lock) {
			if (mapTaxID2TaxInfo.size() > 0) {
				return;
			}
			
			for (TaxInfo taxInfo : repoTaxInfo.findAll()) {
				mapTaxID2TaxInfo.put(taxInfo.getTaxID(), taxInfo);
			}
			for (Integer taxID : mapTaxID2TaxInfo.keySet()) {
				TaxInfo taxInfo = mapTaxID2TaxInfo.get(taxID);
				if (taxInfo.getAbbr() != null || !taxInfo.getAbbr().trim().equals("")) {
					hashNameTaxIDUsual.put(taxInfo.getComName().trim(), taxInfo.getTaxID());
				}
				hashNameTaxID.put(taxInfo.getComName().trim(), taxInfo.getTaxID());
				hashTaxIDName.put(taxInfo.getTaxID(), taxInfo.getComName().trim());
				lsAllTaxID.add(taxInfo.getTaxID());
			}
		}
	}
	/**
	 * @param taxID 0 则返回null
	 * @return
	 */
	public TaxInfo queryTaxInfo(int taxID) {
		if (!mapTaxID2TaxInfo.containsKey(taxID)) {
			return null;
		}
		return mapTaxID2TaxInfo.get(taxID).clone();
	}
	/**
	 * @param taxIDfile 0 则返回null
	 * @return
	 */
	public TaxInfo queryAbbr(String abbr) {
		List<TaxInfo> lsTaxInfos = repoTaxInfo.findByAbbr(abbr);
		if (lsTaxInfos.size() == 0) {
			return null;
		}
		return lsTaxInfos.get(0);
	}
	/**
	 * 没有就插入，有就升级
	 * @param taxInfo
	 */
	public void update(TaxInfo taxInfo) {
		if (taxInfo.getTaxID() == 0) {
			return;
		}
		synchronized (lock) {
			TaxInfo taxInfoS = queryTaxInfo(taxInfo.getTaxID());
			if (taxInfoS == null) {
				repoTaxInfo.save(taxInfo);
				mapTaxID2TaxInfo.put(taxInfo.getTaxID(), taxInfo);
				changeMapInfo(null, taxInfo);
			}
			else if (!taxInfoS.equals(taxInfo)) {
				//因为taxID是mongoDB的ID
				repoTaxInfo.save(taxInfo);
				mapTaxID2TaxInfo.put(taxInfo.getTaxID(), taxInfo);
				changeMapInfo(taxInfoS, taxInfo);
			}
		}
	}

	/**
	 * 返回常用名对taxID
	 * @param allSpecies
	 * @return
	 */
	public HashMap<String, Integer> getSpeciesNameTaxID(boolean allSpecies) {
		if (allSpecies) {
			return hashNameTaxID;
		} else {
			return hashNameTaxIDUsual;
		}
	}
	/**
	 * 返回taxID对常用名
	 * @return
	 */
	public HashMap< Integer,String> getHashTaxIDName() {
		return hashTaxIDName;
	}
	/**
	 * 返回taxID对常用名
	 * @return
	 */
	public ArrayList< Integer> getLsAllTaxID() {
		return lsAllTaxID;
	}

	
	/** 修改taxInfo之后修正map */
	private void changeMapInfo(TaxInfo taxInfoOld, TaxInfo taxInfoNew) {
		if (taxInfoOld == null) {
			if (taxInfoNew.getAbbr() != null || !taxInfoNew.getAbbr().trim().equals("")) {
				hashNameTaxIDUsual.put(taxInfoNew.getComName().trim(), taxInfoNew.getTaxID());
			}
			hashNameTaxID.put(taxInfoNew.getComName().trim(), taxInfoNew.getTaxID());
			hashTaxIDName.put(taxInfoNew.getTaxID(), taxInfoNew.getComName().trim());
			lsAllTaxID.add(taxInfoNew.getTaxID());
		} else {
			if (taxInfoNew.getAbbr() != null || !taxInfoNew.getAbbr().trim().equals("")) {
				hashNameTaxIDUsual.remove(taxInfoOld.getComName().trim());
				hashNameTaxIDUsual.put(taxInfoNew.getComName().trim(), taxInfoNew.getTaxID());
			}
			hashNameTaxID.remove(taxInfoOld.getComName().trim());
			hashNameTaxID.put(taxInfoNew.getComName().trim(), taxInfoNew.getTaxID());
			
			hashTaxIDName.put(taxInfoNew.getTaxID(), taxInfoNew.getComName().trim());
		}
	}
	
}
