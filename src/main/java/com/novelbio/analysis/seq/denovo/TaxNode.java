package com.novelbio.analysis.seq.denovo;

public class TaxNode {

	/** node id in GenBank taxonomy database */
	private int taxId;
	/** parent node id in GeneBank taxonomy database */
	private int pTaxId;
	/** rank of this node ; e.g. superkingdom, kingdom, ... */
	private String rank;
	/** locus-name prefix; not unique */
	private String emblCode;
	/** divsion id */
	private int divsionId;
	/** 1 if node inherits division from parent  (1 or 0) */
	private int inheritedDivFlag;
	/** GenBank genetic code id */
	private int geneticId;
	/** 1 if node inherits genetic code from parent */
	private int inheritedGCFlag;
	/** mitochondrial genetic code id */
	private int mitGenCodId;
	/** 1 if node inherits mitochondrial gencode from parent */
	private int inhMGCFlag;
	/** 1 if name is suppressed in GenBank entry lineage  */
	private int hiddenFlag;
	
	/** 1 if this subtree has no sequence data yet  */
	private int subtreeRootFlag;
	
	/** comments  */
	private String comments;
	
	public void setDivsionId(int divsionId) {
		this.divsionId = divsionId;
	}
	public void setEmblCode(String emblCode) {
		this.emblCode = emblCode;
	}
	public void setGeneticId(int geneticId) {
		this.geneticId = geneticId;
	}
	public void setHiddenFlag(int hiddenFlag) {
		this.hiddenFlag = hiddenFlag;
	}
	public void setInheritedDivFlag(int inheritedDivFlag) {
		this.inheritedDivFlag = inheritedDivFlag;
	}
	public void setInheritedGCFlag(int inheritedGCFlag) {
		this.inheritedGCFlag = inheritedGCFlag;
	}
	public void setInhMGCFlag(int inhMGCFlag) {
		this.inhMGCFlag = inhMGCFlag;
	}
	public void setMitGenCodId(int mitGenCodId) {
		this.mitGenCodId = mitGenCodId;
	}
	public void setpTaxId(int pTaxId) {
		this.pTaxId = pTaxId;
	}
	public void setRank(String rank) {
		this.rank = rank;
	}
	public void setSubtreeRootFlag(int subtreeRootFlag) {
		this.subtreeRootFlag = subtreeRootFlag;
	}
	public void setTaxId(int taxId) {
		this.taxId = taxId;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	
	
	public int getTaxId() {
		return taxId;
	}
	public int getpTaxId() {
		return pTaxId;
	}
	public String getRank() {
		return rank;
	}
	public String getEmblCode() {
		return emblCode;
	}
	public int getDivsionId() {
		return divsionId;
	}
	public int getInheritedDivFlag() {
		return inheritedDivFlag;
	}
	public int getGeneticId() {
		return geneticId;
	}
	public int getInheritedGCFlag() {
		return inheritedGCFlag;
	}
	public int getMitGenCodId() {
		return mitGenCodId;
	}
	public int getInhMGCFlag() {
		return inhMGCFlag;
	}
	public int getHiddenFlag() {
		return hiddenFlag;
	}
	public String getComments() {
		return comments;
	}
	public int getSubtreeRootFlag() {
		return subtreeRootFlag;
	}
	
	public TaxNode genetNode (String content) {
		String[] arrNode = content.split("\t");
		TaxNode taxNode = new TaxNode();
			taxNode.setTaxId(Integer.parseInt(arrNode[0]));
			taxNode.setDivsionId(Integer.parseInt(arrNode[8]));
		return taxNode;
	}
}
