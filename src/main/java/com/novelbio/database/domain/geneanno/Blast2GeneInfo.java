package com.novelbio.database.domain.geneanno;

import java.util.Set;

public class Blast2GeneInfo {
	/**
	 * �����ҵ�gene��ϸ��Ϣ
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
	 * �����ҵ�Unigene��ϸ��Ϣ
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
	 * ��ֵΪ0;
	 * @param identities
	 */
	public void setBlastInfo(BlastInfo blastInfo) 
	{
		this.blastInfo=blastInfo;
	}
	/**
	 * ��ֵΪ0;
	 */
	public BlastInfo getBlastInfo() {
		return this.blastInfo;
	}
	
	
	/**
	 * �ȶԵ��Ļ������ϸ��Ϣ
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
	 * �ȶԵ��Ļ������ϸ��Ϣ
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
