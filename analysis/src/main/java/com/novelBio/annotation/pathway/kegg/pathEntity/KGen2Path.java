package com.novelBio.annotation.pathway.kegg.pathEntity;

import java.util.ArrayList;

import entity.friceDB.BlastInfo;
import entity.kegg.KGCgen2Entry;
import entity.kegg.KGentry;

/**
 * ����Gen2��pathway����Ϣ
 * @author zong0jie
 *
 */
public class KGen2Path {
	/**
	 * ���汾�����Kegg��Path��Ϣ
	 */
	KGCgen2Entry kgCgen2Entry;
	/**
	 * ���汾�����Kegg��Path��Ϣ
	 */
	public void setKGCgen2Entry(KGCgen2Entry kgCgen2Entry)
	{
		this.kgCgen2Entry = kgCgen2Entry;
	}
	/**
	 * ���汾�����Kegg��Path��Ϣ
	 */
	public KGCgen2Entry getKGCgen2Entry() 
	{
		return this.kgCgen2Entry;
	}
	
	/**
	 * ����subject���ֵ�keggID
	 */
	String kegIDSubject;
	/**
	 * ����subject���ֵ�keggID
	 */
	public void setKegIDSubject(String kegIDSubject)
	{
		this.kegIDSubject=kegIDSubject;
	}
	/**
	 * ����subject���ֵ�keggID
	 */
	public String getKegIDSubject()
	{
		return this.kegIDSubject;
	}
	
	/**
	 * blast�õ��Ļ�������KO�󣬽�KOmapping��query���ֵ�pathway�����query���ֵ�ArrayList-entry
	 */
	ArrayList<KGentry> blastgen2Entry;
	/**
	 * blast�õ��Ļ�������KO�󣬽�KOmapping��query���ֵ�pathway�����query���ֵ�ArrayList-entry
	 */
	public void setLsBlastgen2Entry(ArrayList<KGentry> blastgen2Entry)
	{
		this.blastgen2Entry = blastgen2Entry;
	}
	/**
	 * ����blast�õ��Ļ����Kegg��Path��Ϣ
	 */
	public ArrayList<KGentry> getLsBlastgen2Entry() 
	{
		return this.blastgen2Entry;
	}
	
	BlastInfo blastInfo;
	public void setBlastInfo(BlastInfo blastInfo)
	{
		this.blastInfo = blastInfo;
	}
	public BlastInfo getBlastInfo() 
	{
		return this.blastInfo;
	}
	
}
