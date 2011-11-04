package com.novelbio.database.domain.kegg;

import java.util.ArrayList;



/**
 * 用NCBIID对象查找 IDgen2Keg、IDkeg2Ko、entry三张表
 * @author zong0jie
 *
 */
public class KGCgen2Entry {
	private long geneID;
	public long getGenID() {
		return this.geneID;
	}
	public void setGenID(long geneID) {
		this.geneID=geneID;
	}
	
	private String keggID;
	public String getKegID() {
		return this.keggID;
	}
	public void setKegID(String keggID) {
		this.keggID=keggID;
	}
	
	private int taxID;
	public int getTaxID() {
		return this.taxID;
	}
	public void setTaxID(int taxID) {
		this.taxID=taxID;
	}
	
	private ArrayList<KGentry> lsKGentries;
	public ArrayList<KGentry> getLsKGentries() {
		return this.lsKGentries;
	}
	public void setLsKGentries(ArrayList<KGentry> lsKGentries) {
		this.lsKGentries=lsKGentries;
	}
	
}
