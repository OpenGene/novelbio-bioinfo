package com.novelbio.database.domain.cosmic;

import java.io.Serializable;
import java.util.HashMap;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "cosNonCodingVars")
public class NonCodingVars implements Serializable {

	/** chromosome*/
	private String chr;	
	/** the position of mutation*/
	private long pos;
	/** COSMIC ID*/
	@Id
	private String cosmicId;
	private String ref;
	private String alt;
	
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
	public String getRef() {
		return ref;
	}
	public void setAlt(String alt) {
		this.alt = alt;
	}
	public String getAlt() {
		return alt;
	}
	public static NonCodingVars getInstanceFromNonCodingVars(String content) {
		NonCodingVars nonCodingVars = new NonCodingVars();
		if (content.equals("")) {
			return null;
		}
		String[] arrCodingMuts = content.split("\t");	
		nonCodingVars.setChr(arrCodingMuts[0]);
		nonCodingVars.setPos(Long.parseLong(arrCodingMuts[1]));
		nonCodingVars.setCosmicId(arrCodingMuts[2]);
		nonCodingVars.setRef(arrCodingMuts[3]);
		nonCodingVars.setAlt(arrCodingMuts[4]);
		return nonCodingVars;
	}
	
}
