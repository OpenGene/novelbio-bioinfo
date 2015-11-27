package com.novelbio.database.domain.cosmic;

import java.io.Serializable;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "cosCanGene")
public class CodingMuts implements Serializable {
	private static final int taxID = 9606;
	
	
	/** chromosome*/
	private String chr;
	
	/** the position of mutation*/
	private long pos;
	
	/** COSMIC ID*/
	private String cosmicId;
	
	private String ref;
	private String alt;
	private int geneId;
	
	/** CDS change*/
	private String cdsChange;
	
	/** amino acid change*/
	private String AAChange;
	
	/** HNBC ID*/
	private String HNGCID;
	
	/** primary site in cosmic*/
	private String priSiteCos;	
	/** site subtype1*/
	private String siteSubtype1;	
	/** site subtype2*/
	private String siteSubtype2;	
	/** site subtype1*/
	private String siteSubtype3;
	
	/** primary histology in cosmic*/
	private String prihistCos;	
	/** site subtype1*/
	private String histSubtype1;	
	/** site subtype2*/
	private String histSubtype2;
	/** site subtype1*/
	private String histSubtype3;
	
	/** FATHMM prediction*/
	private String FathmmPre;
	/** FATHMM score*/
	private float FathmmScore;
	
	public void setChr(String chr) {
		this.chr = chr;
	}
	public String getChr() {
		return chr;
	}
	public void setPos(long pos) {
		this.pos = pos;
	}
	public long getPos() {
		return pos;
	}
	public void setCosmicId(String cosmicId) {
		this.cosmicId = cosmicId;
	}
	public String getCosmicId() {
		return cosmicId;
	}
	public void setRef(String ref) {
		this.ref = ref;
	}
	public String getAlt() {
		return alt;
	}
	public void setGeneId(int geneId) {
		this.geneId = geneId;
	}
	public int getGeneId() {
		return geneId;
	}
	public void setCdsChange(String cdsChange) {
		this.cdsChange = cdsChange;
	}
	public String getCdsChange() {
		return cdsChange;
	}
	public void setAAChange(String aAChange) {
		AAChange = aAChange;
	}
	public String getAAChange() {
		return AAChange;
	}
	public void setHNGCID(String hNGCID) {
		HNGCID = hNGCID;
	}
	public String getHNGCID() {
		return HNGCID;
	}
	public void setPriSiteCos(String priSiteCos) {
		this.priSiteCos = priSiteCos;
	}
	public String getPriSiteCos() {
		return priSiteCos;
	}
	public void setSiteSubtype1(String siteSubtype1) {
		this.siteSubtype1 = siteSubtype1;
	}
	public String getSiteSubtype1() {
		return siteSubtype1;
	}
	public void setSiteSubtype2(String siteSubtype2) {
		this.siteSubtype2 = siteSubtype2;
	}
	public String getSiteSubtype2() {
		return siteSubtype2;
	}
	public void setSiteSubtype3(String siteSubtype3) {
		this.siteSubtype3 = siteSubtype3;
	}
	public String getSiteSubtype3() {
		return siteSubtype3;
	}
	public void setPrihistCos(String prihistCos) {
		this.prihistCos = prihistCos;
	}
	public String getPrihistCos() {
		return prihistCos;
	}
	public void setHistSubtype1(String histSubtype1) {
		this.histSubtype1 = histSubtype1;
	}
	public String getHistSubtype1() {
		return histSubtype1;
	}
	public void setHistSubtype2(String histSubtype2) {
		this.histSubtype2 = histSubtype2;
	}
	public String getHistSubtype2() {
		return histSubtype2;
	}
	public void setHistSubtype3(String histSubtype3) {
		this.histSubtype3 = histSubtype3;
	}
	public String getHistSubtype3() {
		return histSubtype3;
	}
	public void setFathmmPre(String fathmmPre) {
		FathmmPre = fathmmPre;
	}
	public String getFathmmPre() {
		return FathmmPre;
	}
	public void setFathmmScore(float fathmmScore) {
		FathmmScore = fathmmScore;
	}
	public float getFathmmScore() {
		return FathmmScore;
	}
	
	public static CodingMuts getInstanceFromCodingMuts(String content) {
		if (content.equals("")) {
			return null;
		}
		String[] arrGeneLine = content.split("\t");	
		String geneName = "";
//		GeneID copedID = new GeneID(geneName, taxID, false);
		CodingMuts codingMuts =new CodingMuts();
//		String geneID = copedID.getGeneUniID();
//		if (!geneID.matches("[0-9]+")) {
//			return null;
//		}
//		codingMuts.setGeneId(Integer.parseInt(geneID));
		
		return codingMuts;
	}
	
	
	
}
