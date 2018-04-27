package com.novelbio.database.model.cosmic;

import htsjdk.variant.variantcontext.VariantContext;

import java.io.Serializable;

import org.springframework.data.mongodb.core.mapping.Document;
@Document(collection = "cosmicAbb")
public class CosmicAbb implements Serializable {
	
	/** abreviation*/
	private String abbreviation;
	/** term description*/
	private String term;
	
	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}
	public String getAbbreviation() {
		return abbreviation;
	}
	public void setTerm(String term) {
		this.term = term;
	}
	public String getTerm() {
		return term;
	}
	public static CosmicAbb getInstanceFromCosmicAbb(String content) {
		if (content.equals("")) {
			return null;
		}
		String[] arrGeneLine = content.split("\t");	
		CosmicAbb cosmicAbb = new CosmicAbb();	
		cosmicAbb.setAbbreviation(arrGeneLine[0]);
		cosmicAbb.setTerm(arrGeneLine[1]);
		return cosmicAbb;
	}
}
