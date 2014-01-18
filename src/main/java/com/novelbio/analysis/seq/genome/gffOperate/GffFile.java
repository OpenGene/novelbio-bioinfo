package com.novelbio.analysis.seq.genome.gffOperate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.database.model.species.Species;

@Document(collection = "gfffileinfo")
public class GffFile {

	@Id
	String id;
	@Indexed
	String fileName;
	@Indexed
	int taxID;
	@Indexed
	String version;

	/** 导入日期 */
	String dateImport = DateUtil.getDateDetail();
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getId() {
		return id;
	}
	
	public void setTaxID(int taxID) {
		this.taxID = taxID;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getVersion() {
		return version;
	}
	public int getTaxID() {
		return taxID;
	}
	public String getDateImport() {
		return dateImport;
	}
	
	public String getFileName() {
		return fileName;
	}

	public String getLatinName() {
		String name = null;
		try {
			Species species = new Species(taxID);
			name = species.getNameLatin();
		} catch (Exception e) {
		}
		return name;
	}

}
