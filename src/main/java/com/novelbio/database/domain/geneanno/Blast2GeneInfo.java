package com.novelbio.database.domain.geneanno;

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
	
	BlastInfo blastInfo;
	/**
	 * 初值为0;
	 * @param identities
	 */
	public void setBlastInfo(BlastInfo blastInfo) 
	{
		this.blastInfo=blastInfo;
	}
	/**
	 * 初值为0;
	 */
	public BlastInfo getBlastInfo() {
		return this.blastInfo;
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
