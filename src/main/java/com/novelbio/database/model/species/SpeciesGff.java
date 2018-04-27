package com.novelbio.database.model.species;

import javax.persistence.Id;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="speciesgff")
@CompoundIndexes({
    @CompoundIndex(unique = true, name = "species_version_idx", def = "{'taxId': 1, 'version': -1}"),
 })
public class SpeciesGff {
	@Id
	String id;
	
	/** 物种Id */
	int taxId;
	/** 版本 */
	String version;
	
	/** 上传的gff文件名会转成这个名字 */
	String gffdb;
	
	boolean isGffEsist;
	String gffName;
	
	boolean isGtfExist;
	String gtfName;
	
	public int getTaxId() {
		return taxId;
	}
	public String getVersion() {
		return version;
	}
	public String getGffdb() {
		return gffdb;
	}
	
	public boolean isGffEsist() {
		return isGffEsist;
	}
	public boolean isGtfExist() {
		return isGtfExist;
	}
	/** 改名之前的gff名字，上传之后会改成
	 * {@link #gffdb}.gff
	 * @return
	 */
	public String getGffName() {
		return gffName;
	}
	/** 改名之前的gtf名字，上传之后会改成
	 * {@link #gffdb}.gtf
	 * @return
	 */
	public String getGtfName() {
		return gtfName;
	}
}
