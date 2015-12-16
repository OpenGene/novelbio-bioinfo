package com.novelbio.database.domain.cosmic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.novelbio.database.model.modgeneid.GeneID;

@Document(collection = "cosCanGene")
public class CancerGene implements Serializable {
	private static final int taxID = 9606;
	
	/** gene ID */
	@Id
	private int geneId;
	
	/** The location of gene in genome*/
	private String genomeLocation;
	
	/** The location of gene in genome*/
	private String chrBand;
	
	/** Is somatic or not*/
	private boolean isSomatic = false;
	
	/** Is germline or not*/
	private  boolean isGermline = false;
	
	private List<String> lsTumTypeSom;
	
	private List<String> lsTumTypeGer;
	
	/** cancer syndrome*/
	private String canSyndrome;
	
	/** Tissue Type*/
	private List<String> lsTissueType;
	
	/** molecular Genetics*/
	private String moleGenetics;
	
	private List<String> lsMutaType;
	
	/** The location of gene in genome*/
	private String TransPartner;
	
	/** The location of gene in genome*/
	private String otherSyndrome;
	
	/** The location of gene in genome*/
	private List<String> lsSynonyms;
	
	public void setGeneId(int geneId) {
		this.geneId = geneId;
	}
	public int getGeneId() {
		return geneId;
	}
	public void setGenomeLocation(String genomeLocation) {
		this.genomeLocation = genomeLocation;
	}
	public String getGenomeLocation() {
		return genomeLocation;
	}
	public void setChrBand(String chrBand) {
		this.chrBand = chrBand;
	}
	public String getChrBand() {
		return chrBand;
	}
	public void setLsTumTypeSom(List<String> lsTumTypeSom) {
		this.lsTumTypeSom = lsTumTypeSom;
	}
	public List<String> getLsTumTypeSom() {
		return lsTumTypeSom;
	}
	public void setLsTumTypeGer(List<String> lsTumTypeGer) {
		this.lsTumTypeGer = lsTumTypeGer;
	}
	public List<String> getLsTumTypeGer() {
		return lsTumTypeGer;
	}
	public void setCanSyndrome(String canSyndrome) {
		this.canSyndrome = canSyndrome;
	}
	public String getCanSyndrome() {
		return canSyndrome;
	}
	public void setLsTissueType(List<String> lsTissueType) {
		this.lsTissueType = lsTissueType;
	}
	public List<String> getLsTissueType() {
		return lsTissueType;
	}
	public void setLsMutaType(List<String> lsMutaType) {
		this.lsMutaType = lsMutaType;
	}
	public List<String> getLsMutaType() {
		return lsMutaType;
	}
	public void setMoleGenetics(String moleGenetics) {
		this.moleGenetics = moleGenetics;
	}
	public String getMoleGenetics() {
		return moleGenetics;
	}
	public void setTransPartner(String transPartner) {
		TransPartner = transPartner;
	}
	public String getTransPartner() {
		return TransPartner;
	}
	public void setOtherSyndrome(String otherSyndrome) {
		this.otherSyndrome = otherSyndrome;
	}
	public String getOtherSyndrome() {
		return otherSyndrome;
	}
	public void setLsSynonyms(List<String> lsSynonyms) {
		this.lsSynonyms = lsSynonyms;
	}
	public List<String> getLsSynonyms() {
		return lsSynonyms;
	}
	public static CancerGene getInstanceFromCancerGene(String content) {
		if (content.equals("")) {
			return null;
		}
		String[] arrGeneLine = content.split("\t");	
		GeneID copedID = new GeneID(arrGeneLine[0], taxID, false);
		CancerGene cosCancerGene = new CancerGene();
		String geneID = copedID.getGeneUniID();
		if (!geneID.matches("[0-9]+")) {
			return null;
		}
		cosCancerGene.setGeneId(Integer.parseInt(geneID));
		cosCancerGene.setGenomeLocation(arrGeneLine[3]);
		cosCancerGene.setChrBand(arrGeneLine[4]);
		if (arrGeneLine[5].equals("yes")) {
			cosCancerGene.isSomatic = true;
		}
		if (arrGeneLine[6].equals("yes")) {
			cosCancerGene.isGermline = true;
		}
		List<String> listTumTypeSom = cosCancerGene.getListInfor(arrGeneLine[7], ",");
		cosCancerGene.setLsTumTypeSom(listTumTypeSom);
		List<String> listTumTypeGer = cosCancerGene.getListInfor(arrGeneLine[8], ",");
		cosCancerGene.setLsTumTypeGer(listTumTypeGer);
		cosCancerGene.setCanSyndrome(arrGeneLine[9]);
		List<String> listTissueType = cosCancerGene.getListInfor(arrGeneLine[10], ",");
		cosCancerGene.setLsTissueType(listTissueType);
		cosCancerGene.setMoleGenetics(arrGeneLine[11]);
		List<String> listMutationType = cosCancerGene.getListInfor(arrGeneLine[12], ",");
		cosCancerGene.setLsMutaType(listMutationType);
		if (arrGeneLine.length>13) {
			cosCancerGene.setTransPartner(arrGeneLine[13]);
			if (arrGeneLine.length>15) {
				cosCancerGene.setOtherSyndrome(arrGeneLine[15]);
			}
			if (arrGeneLine.length>16) {
				List<String> lsSynonyms = cosCancerGene.getListInfor(arrGeneLine[16], ",");
				cosCancerGene.setLsSynonyms(lsSynonyms);		
			}
		}


		return cosCancerGene;
	}
	
	public List<String> getListInfor(String content, String separator) {
		List<String> listInfor = new ArrayList<>();
		if (content.length()>0) {
			String[] arrInfor = content.split(separator);
			listInfor =  Arrays.asList(arrInfor); 
			return listInfor;
		} else {
			return null;
		}
	}
}
