package com.novelbio.database.domain.cosmic;

import java.awt.print.Printable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.novelbio.test.mytest;

@Document(collection = "cosCompleteExport")
public class CodingMuts implements Serializable {
	
	/** chromosome*/
	private String chr;	
	/** the position of mutation*/
	private long pos;
	/** COSMIC ID*/
	@Id
	private String cosmicId;
	private String ref;
	private String alt;
	private int geneId;
	/** the strand of gene location*/
	private char strand;
	/** CDS change*/
	private String cdsChange;
	/** amino acid change*/
	private String AAChange;
	
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
	public void setGeneId(int geneId) {
		this.geneId = geneId;
	}
	public int getGeneId() {
		return geneId;
	}
	public void setStrand(char strand) {
		this.strand = strand;
	}
	public char getStrand() {
		return strand;
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
	
	public static CodingMuts getInstanceFromCodingMuts(String content) {
		
		CodingMuts codingMuts = new CodingMuts();
		if (content.equals("")) {
			return null;
		}
		String[] arrCodingMuts = content.split("\t");	
		String geneName = "";
		String geneID = "0";
//		GeneID copedID = new GeneID(geneName, taxID, false);
//		String geneID = copedID.getGeneUniID();
//		if (!geneID.matches("[0-9]+")) {
//			return null;
//		}
		codingMuts.setGeneId(Integer.parseInt(geneID));
		codingMuts.setChr(arrCodingMuts[0]);
		codingMuts.setPos(Long.parseLong(arrCodingMuts[1]));
		codingMuts.setCosmicId(arrCodingMuts[2]);
		codingMuts.setRef(arrCodingMuts[3]);
		codingMuts.setAlt(arrCodingMuts[4]);
		HashMap<String, String> maInfor = codingMuts.getInfor(arrCodingMuts[7], ";");
		codingMuts.setStrand(maInfor.get("STRAND").charAt(0));
		codingMuts.setCdsChange(maInfor.get("CDS"));
		codingMuts.setAAChange(maInfor.get("AA"));
		return codingMuts;
		
	}
	
	public HashMap<String, String> getInfor(String content, String separator) {
		HashMap<String, String> maInfor = new HashMap<>();
		String[] arrValue;
		if (content.length()>0) {
			String[] arrInfor = content.split(separator);
			for (int i = 0; i < arrInfor.length; i++) {
				if ( arrInfor[i].contains("=")) {
					arrValue = arrInfor[i].split("=");
					maInfor.put(arrValue[0], arrValue[1]);
				}
			}
			return maInfor;
		} else {
			return null;
		}
	}
	
	
	
}
