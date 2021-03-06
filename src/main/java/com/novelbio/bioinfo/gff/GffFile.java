package com.novelbio.bioinfo.gff;

import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.database.domain.species.Species;
import com.novelbio.database.service.servgff.MgmtGffDetailGene;

@Document(collection = "gfffileinfo")
@CompoundIndexes({
    @CompoundIndex(unique = true, name = "taxid_version_dbinfo", def = "{'taxID': 1, 'version': 1, 'dbinfo': 1}"),
 })
public class GffFile {

	@Id
	String id;
	@Indexed
	String fileName;
	@Indexed
	int taxID;
	@Indexed
	String version;
	String dbinfo;

	/** 导入日期 */
	String dateImport = DateUtil.getDateDetail();
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public void setDbinfo(String dbinfo) {
		this.dbinfo = dbinfo;
	}
	public String getDbinfo() {
		return dbinfo;
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
	
	public static GffFile findGffFile(int taxId, String version, String dbinfo) {
		return MgmtGffDetailGene.getInstance().findGffFile(taxId, version, dbinfo);
	}
}
