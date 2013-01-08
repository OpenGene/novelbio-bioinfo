package com.novelbio.database.domain.geneanno;

import java.util.HashMap;

import com.novelbio.database.service.servgeneanno.ServTaxID;

/**
 * 有关taxID的表格
 * @author zong0jie
 */
public class TaxInfo {
	ServTaxID servTaxID = new ServTaxID();
	/** NCBI的物种ID */	
	private int taxID;
	/** KEGG上的缩写 */
	private String abbr;//缩写
	/** 拉丁名 */
	private String latin;//常用名
	/** 常用名 */
	private String comName;//常用名
	/** 中文名 */
	private String chnName;//中文名
	/**
	 * NCBI的物种ID
	 * @param taxID
	 */
	public void setTaxID(int taxID) {
		if (taxID == 0) {
			return;
		}
		this.taxID=taxID;
	}
	/** NCBI的物种ID */
	public int getTaxID() {
		return this.taxID;
	}
	/** KEGG上的缩写 */
	public void setAbbr(String abbr) {
		if (abbr == null) {
			return;
		}
		this.abbr=abbr.trim();
	}
	/** KEGG上的缩写 */
	public String getAbbr() {
		if (abbr == null) {
			return "";
		}
		return this.abbr;
	}
	/** 拉丁名 */
	public void setLatin(String latin) {
		if (latin == null) {
			return;
		}
		this.latin=latin.trim();
	}
	/** 拉丁名 */
	public String getLatin() {
		if (latin == null) {
			return "";
		}
		return this.latin;
	}
	/** 常用名 */
	public void setComName(String comName) {
		if (comName == null) {
			return;
		}
		this.comName=comName.trim();
	}
	/** 常用名 */
	public String getComName() {
		if (comName == null) {
			return "";
		}
		return this.comName;
	}
	/** 中文名 */
	public void setChnName(String chnName) {
		if (chnName == null) {
			return;
		}
		this.chnName=chnName.trim();
	}
	/** 中文名 */
	public String getChnName() {
		if (chnName == null) {
			return "";
		}
		return this.chnName;
	}
	/**
	 * 返回常用名对taxID
	 * @return
	 */
	public static HashMap<String, Integer> getHashNameTaxID(boolean allID) {
		ServTaxID servTaxID = new ServTaxID();
		return servTaxID.getSpeciesNameTaxID(allID);
	}
	/**
	 * 返回taxID对常用名
	 * @return
	 */
	public static HashMap<Integer,String> getHashTaxIDName() {
		ServTaxID servTaxID = new ServTaxID();
		return servTaxID.getHashTaxIDName();
	}
	public void update() {
		servTaxID.update(this);
	}
	/**
	 * 不仅仅比较taxID，全部比较一遍
	 * 且比较染色体长度
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
