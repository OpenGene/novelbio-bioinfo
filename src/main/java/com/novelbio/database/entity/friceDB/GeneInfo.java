package com.novelbio.database.entity.friceDB;

public class GeneInfo {
	private long geneID;
	private String symbol;
	private String locusTag;
	
	private String synonyms;
	private String dbXrefs;
	private String chromosome;
	private String mapLocation;
	
	private String description;
	private String typeOfGene;
	private String symNome;
	private String fullNameNome; 
	private String nomStat;
	private String otherDesign;
	private String modDate;
	
	public long getGeneID() {
		return geneID;
	}
	public void setGeneID(long geneID) {
		this.geneID = geneID;
	}
	
	
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	
	public String getLocusTag() {
		return locusTag;
	}
	public void setLocusTag(String locusTag) {
		this.locusTag = locusTag;
	}
	
	public String getSynonyms() {
		return synonyms;
	}
	public void setSynonyms(String synonyms) {
		this.synonyms = synonyms;
	}  
	public String getDbXrefs() {
		return dbXrefs;
	}
	public void setDbXrefs(String dbXrefs) {
		this.dbXrefs = dbXrefs;
	}  
	
	public String getChromosome() {
		return chromosome;
	}
	public void setChromosome(String chromosome) {
		this.chromosome = chromosome;
	}  
	
	public String getMapLocation() {
		return mapLocation;
	}
	public void setMapLocation(String mapLocation) {
		this.mapLocation = mapLocation;
	}  
	

	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getTypeOfGene() {
		return typeOfGene;
	}
	public void setTypeOfGene(String typeOfGene) {
		this.typeOfGene = typeOfGene;
	}

	/**
	 * 没有则为"-"或者null或者""
	 * @return
	 */
	public String getSymNome() {
		return symNome;
	}
	public void setSymNome(String symNome) {
		this.symNome = symNome;
	}
	/**
	 * 没有则为"-"或者null或者""
	 * @return
	 */
	public String getFullName() {
		return fullNameNome;
	}
	public void setFullName(String fullNameFromNomenclature) {
		this.fullNameNome = fullNameFromNomenclature;
	}
	
	public String getNomStat() {
		return nomStat;
	}
	public void setNomStat(String nomStat) {
		this.nomStat = nomStat;
	}
	
	
	public String getOtherDesign() {
		return otherDesign;
	}
	public void setOtherDesign(String otherDesign) {
		this.otherDesign = otherDesign;
	}
	
	public String getModDate() {
		return modDate;
	}
	public void setModDate(String modDate) {
		this.modDate = modDate;
	}
	
	
}
