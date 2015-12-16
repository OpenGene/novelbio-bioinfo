package com.novelbio.database.domain.cosmic;

import java.util.ArrayList;
import java.util.List;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "cosmicAllSNVs")
//@CompoundIndexes({
//    @CompoundIndex(unique = true, name = "chr_pos_ref_alt", def = "{'chr': 1, 'pos': 1, 'ref': 1, 'alt': 1}"),
// })
public class CosmicAllSNVs {
	/** chromosome*/
	private String chr;	
	/** the position of mutation*/
	private long pos;
	/** COSMIC ID*/
	@Id
	private String cosmicId;
	private String ref;
	private String alt;
	private boolean isCodingVars = false;
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
	public void setAlleles(List<Allele> alleles) {
		this.alleles = alleles;
	}
	public List<Allele> getAlleles() {
		return alleles;
	}
	public static CosmicAllSNVs getInstanceFromNonCodingVars(VariantContext variantContext, boolean isCodingVars) {
		CosmicAllSNVs cosmicAllSNVs = new CosmicAllSNVs();
		if (variantContext.equals("")) {
			return null;
		}
		cosmicAllSNVs.setChr(variantContext.getContig());
		cosmicAllSNVs.setPos(variantContext.getStart());
		cosmicAllSNVs.setCosmicId(variantContext.getID());
		cosmicAllSNVs.setAlleles(variantContext.getAlleles());
		alleles = variantContext.getAlleles();
		cosmicAllSNVs.setRef(alleles.get(0).toString().replaceAll("\\*", ""));
		cosmicAllSNVs.setAlt(alleles.get(1).toString().replaceAll("\\*", ""));
		if (isCodingVars) {
			cosmicAllSNVs.isCodingVars = true;
		}
		return cosmicAllSNVs;
	}
}
