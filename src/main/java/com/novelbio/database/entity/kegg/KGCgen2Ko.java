package com.novelbio.database.entity.kegg;

import java.util.ArrayList;



public class KGCgen2Ko {
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
	/**
	 * keggID��KO�Ƕ�Զ�Ĺ�ϵ
	 */
	private ArrayList<KGIDkeg2Ko> lsKgiDkeg2Kos;
	/**
	 * keggID��KO�Ƕ�Զ�Ĺ�ϵ
	 */
	public ArrayList<KGIDkeg2Ko> getLsKgiDkeg2Kos() {
		return this.lsKgiDkeg2Kos;
	}
	/**
	 * keggID��KO�Ƕ�Զ�Ĺ�ϵ
	 */
	public void setLsKgiDkeg2Kos(ArrayList<KGIDkeg2Ko> lsKgiDkeg2Kos) {
		this.lsKgiDkeg2Kos=lsKgiDkeg2Kos;
	}
}
