package com.novelbio.database.domain.geneanno;

import java.util.ArrayList;
/**
 * 用accID查找NCBIID、refSeq信息、GoInfo、geneInfo四个表
 * @author zong0jie
 *
 */
public class Gene2GoInfo {

	private int taxID;
	private long geneID;
	private String quaryID;
	private String dbInfo;
	private String refStatus;
	
	private ArrayList<Gene2Go> lsGOInfo;
	private GeneInfo geneInfo;
	
	public int getTaxID() {
		return taxID;
	}
	public void setTaxID(int taxID) {
		this.taxID = taxID;
	}
	
	public long getGeneId() {
		return geneID;
	}
	public void setGeneId(long geneID) {
		this.geneID = geneID;
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

	public ArrayList<Gene2Go> getLsGOInfo() {
		return lsGOInfo;
	}
	public void setLsGOInfo(ArrayList<Gene2Go> lsGOInfo) {
		this.lsGOInfo = lsGOInfo;
	}
	
	public GeneInfo getGeneInfo() {
		return geneInfo;
	}
	public void setGeneInfo(GeneInfo geneInfo) {
		this.geneInfo = geneInfo;
	}
	
	
}
