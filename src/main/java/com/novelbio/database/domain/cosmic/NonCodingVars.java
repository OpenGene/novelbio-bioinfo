package com.novelbio.database.domain.cosmic;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "cosNonCodingVars")
@CompoundIndexes({
    @CompoundIndex(unique = false, name = "chr_pos_alt", def = "{'chr': 1,'pos': 1,'alt': 1}"),
 })
public class NonCodingVars implements Serializable {

	/** chromosome*/
	@Indexed
	private String chr;	
	/** the position of mutation*/
	private long pos;
	/** COSMIC ID*/
	@Id
	private String cosmicId;
	private String ref;
	private String alt;
	private static List<Allele> alleles = new ArrayList<>();
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
	public static NonCodingVars getInstanceFromNonCodingVars(VariantContext variantContext) {
		NonCodingVars nonCodingVars = new NonCodingVars();	
		nonCodingVars.setChr(variantContext.getContig());
		nonCodingVars.setPos(variantContext.getStart());
		nonCodingVars.setCosmicId(variantContext.getID());
		alleles = variantContext.getAlleles();
		nonCodingVars.setRef(alleles.get(0).toString().replaceAll("\\*", ""));
		nonCodingVars.setAlt(alleles.get(1).toString().replaceAll("\\*", ""));		
		return nonCodingVars;
	}
	
}
