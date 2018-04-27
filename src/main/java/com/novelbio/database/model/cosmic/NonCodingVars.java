package com.novelbio.database.model.cosmic;

import htsjdk.variant.variantcontext.Allele;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "cosNonCodingVars")
//@CompoundIndexes({
//    @CompoundIndex(unique = false, name = "chr_pos_alt", def = "{'chr': 1, 'pos': 1, 'alt': 1}"),
// })
public class NonCodingVars implements Serializable {

	/** chromosome*/
//	@Indexed
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
		String[] arrNonCodingVars = content.split("\t");	
		nonCodingVars.setChr(arrNonCodingVars[0]);
		nonCodingVars.setPos(Long.parseLong(arrNonCodingVars[1]));
		nonCodingVars.setCosmicId(arrNonCodingVars[2]);
		nonCodingVars.setRef(arrNonCodingVars[3]);
		nonCodingVars.setAlt(arrNonCodingVars[4]);		
		return nonCodingVars;
	}
	
}
