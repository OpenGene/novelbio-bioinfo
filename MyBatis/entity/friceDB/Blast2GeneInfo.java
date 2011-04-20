package entity.friceDB;

import java.util.Set;

public class Blast2GeneInfo {
	/**
	 * 待查找的gene详细信息
	 */
	Gene2GoInfo queryGene2GoInfo;
	public void setQueryGene2GoInfo(Gene2GoInfo queryGene2GoInfo) 
	{
		this.queryGene2GoInfo=queryGene2GoInfo;
	}
	public Gene2GoInfo getQueryGene2GoInfo() {
		return this.queryGene2GoInfo;
	}
	/**
	 * 待查找的Unigene详细信息
	 */
	Uni2GoInfo queryUni2GoInfo;
	public void setQueryUni2GoInfo(Uni2GoInfo queryUni2GoInfo) 
	{
		this.queryUni2GoInfo=queryUni2GoInfo;
	}
	public Uni2GoInfo getQueryUniGene2GoInfo() {
		return this.queryUni2GoInfo;
	}
	
	double identities=0;
	/**
	 * 初值为0;
	 * @param identities
	 */
	public void setIdentities(double identities) 
	{
		this.identities=identities;
	}
	/**
	 * 初值为0;
	 */
	public double getIdentities() {
		return this.identities;
	}
	
	double evalue=100;
	/**
	 * 初值为100
	 * @param evalue
	 */
	public void setEvalue(double evalue) 
	{
		this.evalue=evalue;
	}
	/**
	 * 初值为100
	 */
	public double getEvalue() {
		return this.evalue;
	}
	
	String blastDate;
	public void setBlastDate(String blastDate) 
	{
		this.blastDate=blastDate;
	}
	public String getBlastDate() {
		return this.blastDate;
	}
	
	/**
	 * 比对到的基因的详细信息
	 */
	Gene2GoInfo subjectGene2GoInfo;
	public void setSubjectGene2GoInfo(Gene2GoInfo subjectGene2GoInfo) 
	{
		this.subjectGene2GoInfo=subjectGene2GoInfo;
	}
	public Gene2GoInfo getSubjectGene2GoInfo() {
		return this.subjectGene2GoInfo;
	}
	
	/**
	 * 比对到的基因的详细信息
	 */
	Uni2GoInfo subjectUni2GoInfo;
	public void setSubjectUni2GoInfo(Uni2GoInfo subjectUni2GoInfo) 
	{
		this.subjectUni2GoInfo=subjectUni2GoInfo;
	}
	public Uni2GoInfo getSubjectUni2GoInfo() {
		return this.subjectUni2GoInfo;
	}
	
	
	
	
	
}
