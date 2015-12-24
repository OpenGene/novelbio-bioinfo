package com.novelbio.database.domain.cosmic;

import java.util.ArrayList;
import java.util.List;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "cosmicAllSNVs")
@CompoundIndexes({
    @CompoundIndex(unique = false, name = "chr_pos_alt", def = "{'chr': 1, 'pos': 1, 'alt': 1}"),
 })
public class CosmicAllSNVs {
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
	private boolean isCodingVars = false;
	
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

	public static CosmicAllSNVs getInstanceFromNonCodingVars(String content, boolean isCodingVars) {
		CosmicAllSNVs cosmicAllSNVs = new CosmicAllSNVs();
		if (content.equals("")) {
			return null;
		}
		String[] arrVars = content.split("\t");	
		cosmicAllSNVs.setChr(arrVars[0]);
		cosmicAllSNVs.setPos(Long.parseLong(arrVars[1]));
		cosmicAllSNVs.setCosmicId(arrVars[2]);
		cosmicAllSNVs.setRef(arrVars[3]);
		cosmicAllSNVs.setAlt(arrVars[4]);
		if (isCodingVars) {
			cosmicAllSNVs.isCodingVars = true;
		}
		return cosmicAllSNVs;
	}
}
