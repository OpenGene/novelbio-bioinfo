package com.novelbio.database.domain.geneanno;

import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.database.mapper.geneanno.MapFSTaxID;
import com.novelbio.database.model.modcopeid.CopedID;
import com.novelbio.database.service.servgeneanno.ServTaxID;

/**
 * �й�taxID�ı��
 * @author zong0jie
 *
 */
public class TaxInfo {
	ServTaxID servTaxID = new ServTaxID();
	/** NCBI������ID */	
	private int taxID;
	/** KEGG�ϵ���д */
	private String abbr;//��д
	/** ������ */
	private String latin;//������
	/** ������ */
	private String comName;//������
	/** ������ */
	private String chnName;//������
	/** Ⱦɫ�峤����Ϣ�������ܳ��Ⱥ�ÿ��Ⱦɫ�峤�� */
	private String chrInfo;
	/**
	 * NCBI������ID
	 * @param taxID
	 */
	public void setTaxID(int taxID) {
		if (taxID == 0) {
			return;
		}
		this.taxID=taxID;
	}
	/** NCBI������ID */
	public int getTaxID() {
		return this.taxID;
	}
	

	/** KEGG�ϵ���д */
	public void setAbbr(String abbr) {
		if (abbr == null) {
			return;
		}
		this.abbr=abbr.trim();
	}
	/** KEGG�ϵ���д */
	public String getAbbr() {
		if (abbr == null) {
			return "";
		}
		return this.abbr;
	}

	/**
	 * ������
	 */
	public void setLatin(String latin) {
		if (latin == null) {
			return;
		}
		this.latin=latin.trim();
	}
	/**
	 * ������
	 */
	public String getLatin() {
		if (latin == null) {
			return "";
		}
		return this.latin;
	}
	/**
	 * ������
	 */
	public void setComName(String comName) {
		if (comName == null) {
			return;
		}
		this.comName=comName.trim();
	}
	/**
	 * ������
	 */
	public String getComName() {
		if (comName == null) {
			return "";
		}
		return this.comName;
	}
	/**
	 * ������
	 */
	public void setChnName(String chnName) {
		if (chnName == null) {
			return;
		}
		this.chnName=chnName.trim();
	}
	/**
	 * ������
	 */
	public String getChnName() {
		if (chnName == null) {
			return "";
		}
		return this.chnName;
	}
	/**
	 * ���س�������taxID
	 * @return
	 */
	public static HashMap<String, Integer> getHashNameTaxID(boolean allID) {
		ServTaxID servTaxID = new ServTaxID();
		return servTaxID.getSpeciesNameTaxID(allID);
	}
	/**
	 * ����taxID�Գ�����
	 * @return
	 */
	public static HashMap<Integer,String> getHashTaxIDName() {
		ServTaxID servTaxID = new ServTaxID();
		return servTaxID.getHashTaxIDName();
	}
	
	public void update() {
		if (taxID == 0) {
			return;
		}
		TaxInfo taxInfo = servTaxID.queryTaxInfo(taxID);
		if (taxInfo == null) {
			servTaxID.InsertTaxInfo(this);
		}
		else if (!equals(taxInfo)) {
			servTaxID.upDateTaxInfo(this);
		}
	}
	
	/**
	 * �������Ƚ�taxID��ȫ���Ƚ�һ��
	 */
	public boolean equals(Object obj) {
		if (this == obj) return true;
		
		if (obj == null) return false;
		
		if (getClass() != obj.getClass()) return false;
		TaxInfo otherObj = (TaxInfo)obj;
		
		if (getAbbr().equals(otherObj.getAbbr())
		&&		
		getChnName().equals(otherObj.getChnName())
		&&
		getComName().equals(otherObj.getComName())
		&&
		getLatin().equals(otherObj.getLatin())
		&&
		getTaxID() == otherObj.getTaxID()
		)
		{
			return true;
		}
		return false;
	}
}
