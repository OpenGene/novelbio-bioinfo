package com.novelbio.database.domain.geneanno;

import java.util.ArrayList;

/**
 * 联合查询结果，保存查询到的UniID全表，UniGeneInfo全表，UniGene2Go全表
 * @author zong0jie
 *
 */
public class Uni2GoInfo {

	private int taxID;
	private String uniID;
	private String quaryID;
	private String dbInfo;
	private String refStatus;
	
	private ArrayList<UniGene2Go> lsUniGOInfo;
	private UniGeneInfo uniGeneInfo;
	
	public int getTaxID() {
		return taxID;
	}
	public void setTaxID(int taxID) {
		this.taxID = taxID;
	}
	
	public String getUniID() {
		return uniID;
	}
	public void setUniID(String uniID) {
		this.uniID =uniID;
	}
	
	public String getQuaryID() {
		return quaryID;
	}
	public void setQuaryID(String quaryID) {
		this.quaryID = quaryID;
	}
	
	public String getDataBase() {
		return dbInfo;
	}
	public void setDataBase(String dbInfo) {
		this.dbInfo = dbInfo;
	}
	
	
	public String getRefStatus() {
		return refStatus;
	}
	public void setRefStatus(String refStatus) {
		this.refStatus = refStatus;
	}

	public ArrayList<UniGene2Go> getLsUniGOInfo() {
		return lsUniGOInfo;
	}
	public void setLsUniGOInfo(ArrayList<UniGene2Go> lsGOInfo) {
		this.lsUniGOInfo = lsGOInfo;
	}
	
	public UniGeneInfo getUniGeneInfo() {
		return uniGeneInfo;
	}
	public void setUniGeneInfo(UniGeneInfo uniGeneInfo) {
		this.uniGeneInfo = uniGeneInfo;
	}
	

	
}
