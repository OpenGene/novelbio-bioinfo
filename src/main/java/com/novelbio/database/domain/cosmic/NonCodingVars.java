package com.novelbio.database.domain.cosmic;

import java.io.Serializable;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "cosNonCodingVars")
public class NonCodingVars implements Serializable {

	/** sample name*/
	private String sampleName;
	/** sample ID*/
	private int sampleId;	
	/** COSMIC ID for non coding variants*/
	private String cOSMICId;
	/** zygosity*/
	private String zygosity;
	/** mutation somatic status*/
	private String mutaSomSta;
	/** WT SEQ*/
	private String wtSeq;
	/** mutation SEQ*/
	private String mutaSeq;
	/** SNP*/
	private char snp;
	/** FATHMM MKL NON CODING SCORE*/
	private float FathmmNonCodScore;
	/** FATHMM MKL NON CODING GROUPS*/
	private String FathmmNonCodGroups;
	/** FATHMM MKL CODING SCORE*/
	private float FathmmCodScore;
	/** FATHMM MKL CODING GROUPS*/
	private String FathmmCodGroups;
	private boolean isGenomeSeq = false;
	private boolean isExomeSeq = false;
	/** study ID*/
	private int studyId;
	/** pubmed PMID*/
	private long pubmedPMID;
	public void setSampleName(String sampleName) {
		this.sampleName = sampleName;
	}
	public String getSampleName() {
		return sampleName;
	}
	public void setSampleId(int sampleId) {
		this.sampleId = sampleId;
	}
	public int getSampleId() {
		return sampleId;
	}
	public void setcOSMICId(String cOSMICId) {
		this.cOSMICId = cOSMICId;
	}
	public String getcOSMICId() {
		return cOSMICId;
	}
	public void setZygosity(String zygosity) {
		this.zygosity = zygosity;
	}
	public String getZygosity() {
		return zygosity;
	}
	public void setMutaSomSta(String mutaSomSta) {
		this.mutaSomSta = mutaSomSta;
	}
	public String getMutaSomSta() {
		return mutaSomSta;
	}
	public void setWtSeq(String wtSeq) {
		this.wtSeq = wtSeq;
	}
	public String getWtSeq() {
		return wtSeq;
	}
	public void setMutaSeq(String mutaSeq) {
		this.mutaSeq = mutaSeq;
	}
	public String getMutaSeq() {
		return mutaSeq;
	}
	public void setSnp(char snp) {
		this.snp = snp;
	}
	public char getSnp() {
		return snp;
	}
	public void setFathmmNonCodGroups(String fathmmNonCodGroups) {
		FathmmNonCodGroups = fathmmNonCodGroups;
	}
	public String getFathmmNonCodGroups() {
		return FathmmNonCodGroups;
	}
	public void setFathmmNonCodScore(float fathmmNonCodScore) {
		FathmmNonCodScore = fathmmNonCodScore;
	}
	public float getFathmmNonCodScore() {
		return FathmmNonCodScore;
	}
	public void setFathmmCodGroups(String fathmmCodGroups) {
		FathmmCodGroups = fathmmCodGroups;
	}
	public String getFathmmCodGroups() {
		return FathmmCodGroups;
	}
	public void setFathmmCodScore(float fathmmCodScore) {
		FathmmCodScore = fathmmCodScore;
	}
	public float getFathmmCodScore() {
		return FathmmCodScore;
	}
	public void setStudyId(int studyId) {
		this.studyId = studyId;
	}
	public int getStudyId() {
		return studyId;
	}
	public void setPubmedPMID(long pubmedPMID) {
		this.pubmedPMID = pubmedPMID;
	}
	public long getPubmedPMID() {
		return pubmedPMID;
	}
	public static NonCodingVars getInstanceFromNonCodingVars(String content) {
		
		if (content.equals("")) {
			return null;
		}
		String[] arrLineInfor = content.split("\t");	
		NonCodingVars nonCodingVars = new NonCodingVars();
		nonCodingVars.setSampleName(arrLineInfor[0]);
		nonCodingVars.setSampleId(Integer.parseInt(arrLineInfor[1]));
		nonCodingVars.setcOSMICId(arrLineInfor[2]);
		nonCodingVars.setZygosity(arrLineInfor[3]);
		nonCodingVars.setMutaSomSta(arrLineInfor[6]);
		nonCodingVars.setWtSeq(arrLineInfor[7]);
		nonCodingVars.setMutaSeq(arrLineInfor[8]);
		nonCodingVars.setSnp(arrLineInfor[9].charAt(0));
		nonCodingVars.setFathmmNonCodGroups(arrLineInfor[10]);
		if (!(arrLineInfor[11].equals(null)||arrLineInfor[11].equals(""))) {
			nonCodingVars.setFathmmNonCodScore(Float.parseFloat(arrLineInfor[11]));
		}
		nonCodingVars.setFathmmCodGroups(arrLineInfor[12]);
		if (!(arrLineInfor[13].equals(null)||arrLineInfor[13].equals(""))) {
			nonCodingVars.setFathmmCodScore(Float.parseFloat(arrLineInfor[13]));
		}
		if (arrLineInfor[14].equals("y")) {
			nonCodingVars.isGenomeSeq = true;
		}
		if (arrLineInfor[15].equals("y")) {
			nonCodingVars.isExomeSeq = true;
		}
		if (!(arrLineInfor[16].equals(null)||arrLineInfor[16].equals(""))) {
			nonCodingVars.setStudyId(Integer.parseInt(arrLineInfor[16]));
		}
		if (!(arrLineInfor[17].equals(null)||arrLineInfor[17].equals(""))) {
			nonCodingVars.setPubmedPMID(Long.parseLong(arrLineInfor[17]));
		}
		return nonCodingVars;
	}
}
