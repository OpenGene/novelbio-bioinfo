package com.novelbio.database.model.species;

import java.util.Map;
import java.util.Set;

import javax.persistence.Id;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="speciesversion")
@CompoundIndexes({
    @CompoundIndex(unique = true, name = "species_version_idx", def = "{'taxId': 1, 'version': -1}"),
 })
public class SpeciesVersion {
	@Id
	String id;
	
	/** 物种Id */
	int taxId;
	/** 版本 */
	String version;
	
	String chrFile;
	
	/**
	 * 在数据字典记录
	 * 譬如 Repeat.gff, rrna.fa等
	 */
	Set<String> fileTypes;
	/**
	 * 文件类型对应文件名
	 * 这些文件都保存在 
	 * ${genome_path}/species/${taxId}/${version}/
	 * 中
	 */
	Map<String, String> fileType2Name;
	
}
