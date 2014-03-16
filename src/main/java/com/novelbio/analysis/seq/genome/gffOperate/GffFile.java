package com.novelbio.analysis.seq.genome.gffOperate;

import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.novelbio.analysis.seq.mapping.MapBowtie;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.database.model.species.Species;

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
	/** 染色体ID都小写 */
	Map<String, List<int[]>> mapChrID2LsInterval;
	
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
	/** 设定每条染色体的区域
	 * 染色体ID都小写
	 * @param mapChrID2LsInterval
	 */
	public void setMapChrID2LsInterval(
			Map<String, List<int[]>> mapChrID2LsInterval) {
		this.mapChrID2LsInterval = mapChrID2LsInterval;
	}
	/** 给jbrowse用的东西，染色体ID都小写 */
	public Map<String, List<int[]>> getMapChrID2LsInterval() {
		return mapChrID2LsInterval;
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
