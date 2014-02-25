package com.novelbio.database.service.servgeneanno;

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
	/**
	 * 仅包含有缩写的物种
	 */
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
			if (mapTaxID2TaxInfo.size() > 0) return;
			
			for (TaxInfo taxInfo : repoTaxInfo.findAll()) {
				addToMap(taxInfo);
			}
		}
	}
	
	private void addToMap(TaxInfo taxInfo) {
		mapTaxID2TaxInfo.put(taxInfo.getTaxID(), taxInfo);
		hashNameTaxID.put(taxInfo.getComName().trim(), taxInfo.getTaxID());
		hashTaxIDName.put(taxInfo.getTaxID(), taxInfo.getComName().trim());
	}
	
	/**
	 * @param taxID 0 则返回null
	 * @return
	 */
	public TaxInfo queryTaxInfo(int taxID) {
		if (!mapTaxID2TaxInfo.containsKey(taxID)) {
			TaxInfo taxInfo = repoTaxInfo.findByTaxID(taxID);
			if (taxInfo != null) {
				addToMap(taxInfo);
				return taxInfo;
			} else {
				return null;
			}
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
				addToMap(taxInfoS);
				changeMapInfo(null, taxInfo);
			}
			else if (!taxInfoS.equals(taxInfo)) {
				//因为taxID是mongoDB的ID
				repoTaxInfo.save(taxInfo);
				addToMap(taxInfoS);
				changeMapInfo(taxInfoS, taxInfo);
			}
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
	public List<TaxInfo> getLsAllTaxID() {
		List<TaxInfo> lsTaxInfos = repoTaxInfo.findAll();
		return lsTaxInfos;
	}

	
	/** 修改taxInfo之后修正map */
	private void changeMapInfo(TaxInfo taxInfoOld, TaxInfo taxInfoNew) {
		if (taxInfoOld == null) {
			hashNameTaxID.put(taxInfoNew.getComName().trim(), taxInfoNew.getTaxID());
			hashTaxIDName.put(taxInfoNew.getTaxID(), taxInfoNew.getComName().trim());
		} else {
			hashNameTaxID.remove(taxInfoOld.getComName().trim());
			hashNameTaxID.put(taxInfoNew.getComName().trim(), taxInfoNew.getTaxID());
			
			hashTaxIDName.put(taxInfoNew.getTaxID(), taxInfoNew.getComName().trim());
		}
	}
	
	static class ManageTaxIDHold {
		private static ManageTaxID manageTaxID = new ManageTaxID();
	}
	
	
	public static ManageTaxID getInstance() {
		return ManageTaxIDHold.manageTaxID;
	}
	
}
