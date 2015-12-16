package com.novelbio.database.domain.cosmic;

import java.io.Serializable;
import org.springframework.data.mongodb.core.mapping.Document;
import com.novelbio.database.model.modgeneid.GeneID;

@Document(collection = "cosCompleteExport")
public class CompleteExport implements Serializable {
	private static final int taxID = 9606;
	/** gene ID*/
	private int geneId;
	/** accession Number*/
	private String accessionNum;
	/** gene CDS length*/
	private long cDSLength;
	/** HGNC ID*/
	private int hGNCId;
	/** Sample Name*/
	private String sampleName;
	/** Sample ID*/
	private int sampleID;
	/** tumour ID*/
	private int tumourID;
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
	/** genome-wide screen*/
	private boolean isGenomeScreen = false;
	/** mutation ID(COSMIC ID)*/
	private String mutationID;
	/** mutation Description*/
	private String mutationDes;
	/** mutation zygosity*/
	private String mutationZyg;
	/** LOH*/
	private String lOH;
	/** is snp*/
	private String snp;
	/** FATHMM prediction*/
	private String FathmmPre;
	/** FATHMM score*/
	private float FathmmScore;
	/** mutation somatic status*/
	private String mutSomSta;
	/** pubmed PMID*/
	private long pubmedPMID;
	/** study ID*/
	private int studyId;
	/** sample source*/
	private String sampleSource;
	/** tumor origin*/
	private String tumourOrigin;
	/** age*/
	private float age;
	
	public void setGeneId(int geneId) {
		this.geneId = geneId;
	}
	public int getGeneId() {
		return geneId;
	}
	public void setAccessionNum(String accessionNum) {
		this.accessionNum = accessionNum;
	}
	public String getAccessionNum() {
		return accessionNum;
	}
	public void setcDSLength(long cDSLength) {
		this.cDSLength = cDSLength;
	}
	public long getcDSLength() {
		return cDSLength;
	}
	public void sethGNCId(int hGNCId) {
		this.hGNCId = hGNCId;
	}
	public int gethGNCId() {
		return hGNCId;
	}
	public void setSampleName(String sampleName) {
		this.sampleName = sampleName;
	}
	public String getSampleName() {
		return sampleName;
	}
	public void setSampleID(int sampleID) {
		this.sampleID = sampleID;
	}
	public int getSampleID() {
		return sampleID;
	}
	public void setTumourID(int tumourID) {
		this.tumourID = tumourID;
	}
	public int getTumourID() {
		return tumourID;
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
	public void setMutationID(String mutationID) {
		this.mutationID = mutationID;
	}
	public String getMutationID() {
		return mutationID;
	}
	public void setMutationDes(String mutationDes) {
		this.mutationDes = mutationDes;
	}
	public String getMutationDes() {
		return mutationDes;
	}
	public void setMutationZyg(String mutationZyg) {
		this.mutationZyg = mutationZyg;
	}
	public String getMutationZyg() {
		return mutationZyg;
	}
	public void setlOH(String lOH) {
		this.lOH = lOH;
	}
	public String getlOH() {
		return lOH;
	}
	public void setSnp(String snp) {
		this.snp = snp;
	}
	public String getSnp() {
		return snp;
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
	public void setMutSomSta(String mutSomSta) {
		this.mutSomSta = mutSomSta;
	}
	public String getMutSomSta() {
		return mutSomSta;
	}
	public void setPubmedPMID(long pubmedPMID) {
		this.pubmedPMID = pubmedPMID;
	}
	public long getPubmedPMID() {
		return pubmedPMID;
	}
	public void setStudyId(int studyId) {
		this.studyId = studyId;
	}
	public int getStudyId() {
		return studyId;
	}
	public void setSampleSource(String sampleSource) {
		this.sampleSource = sampleSource;
	}
	public String getSampleSource() {
		return sampleSource;
	}
	public void setTumourOrigin(String tumourOrigin) {
		this.tumourOrigin = tumourOrigin;
	}
	public String getTumourOrigin() {
		return tumourOrigin;
	}
	public void setAge(float age) {
		this.age = age;
	}
	public float getAge() {
		return age;
	}
	public static CompleteExport getInstanceFromCodingMuts(String content) {
		if (content.equals("")) {
			return null;
		}
		String[] arrGeneLine = content.split("\t");	
		GeneID copedID = new GeneID(arrGeneLine[0], taxID, false);
		CompleteExport completeExport =new CompleteExport();
		String geneID = copedID.getGeneUniID();
		if (!geneID.matches("[0-9]+")) {
			return null;
		}
		completeExport.setGeneId(Integer.parseInt(geneID));
		completeExport.setAccessionNum(arrGeneLine[1]);
		completeExport.setcDSLength(Long.parseLong(arrGeneLine[2]));
		if(arrGeneLine[3].length()>0) {
			completeExport.sethGNCId(Integer.parseInt(arrGeneLine[3]));
		}
		
		completeExport.setSampleName(arrGeneLine[4]);
		completeExport.setSampleID(Integer.parseInt(arrGeneLine[5]));
		completeExport.setTumourID(Integer.parseInt(arrGeneLine[6]));
		completeExport.setPriSiteCos(arrGeneLine[7]);
		completeExport.setSiteSubtype1(arrGeneLine[8]);
		completeExport.setSiteSubtype2(arrGeneLine[9]);
		completeExport.setSiteSubtype3(arrGeneLine[10]);
		completeExport.setPrihistCos(arrGeneLine[11]);
		completeExport.setHistSubtype1(arrGeneLine[12]);
		completeExport.setHistSubtype2(arrGeneLine[13]);
		completeExport.setHistSubtype3(arrGeneLine[14]);
		if (arrGeneLine[15].equals("y")) {
			completeExport.isGenomeScreen = true;
		}
		completeExport.setMutationID(arrGeneLine[16]);
		completeExport.setMutationDes(arrGeneLine[19]);
		completeExport.setMutationZyg(arrGeneLine[20]);
		completeExport.setlOH(arrGeneLine[21]);
		completeExport.setSnp(arrGeneLine[25]);
		completeExport.setFathmmPre(arrGeneLine[26]);
		if (!(arrGeneLine[27].equals(null)||arrGeneLine[27].equals(""))) {
			completeExport.setFathmmScore(Float.parseFloat(arrGeneLine[27]));
		}
		completeExport.setMutSomSta(arrGeneLine[28]);
		if (!(arrGeneLine[29].equals(null)||arrGeneLine[29].equals("")) ) {
			completeExport.setPubmedPMID(Integer.parseInt(arrGeneLine[29]));
		}
		if (!(arrGeneLine[30].equals(null)||arrGeneLine[30].equals(""))) {
			completeExport.setStudyId(Integer.parseInt(arrGeneLine[30]));
		}
		completeExport.setSampleSource(arrGeneLine[31]);
		completeExport.setTumourOrigin(arrGeneLine[32]);
		if ((arrGeneLine.length>33) && (!(arrGeneLine[33].equals(null)||arrGeneLine[33].equals("")))) {
			completeExport.setAge(Float.parseFloat(arrGeneLine[33]));
		}
		return completeExport;
	}	
}
