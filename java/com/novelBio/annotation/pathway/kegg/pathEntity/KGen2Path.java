package com.novelBio.annotation.pathway.kegg.pathEntity;

import java.util.ArrayList;

import entity.friceDB.BlastInfo;
import entity.kegg.KGCgen2Entry;
import entity.kegg.KGentry;

/**
 * 保存Gen2到pathway的信息
 * @author zong0jie
 *
 */
public class KGen2Path {
	/**
	 * 保存本基因的Kegg到Path信息
	 */
	KGCgen2Entry kgCgen2Entry;
	/**
	 * 保存本基因的Kegg到Path信息
	 */
	public void setKGCgen2Entry(KGCgen2Entry kgCgen2Entry)
	{
		this.kgCgen2Entry = kgCgen2Entry;
	}
	/**
	 * 保存本基因的Kegg到Path信息
	 */
	public KGCgen2Entry getKGCgen2Entry() 
	{
		return this.kgCgen2Entry;
	}
	
	/**
	 * 保存subject物种的keggID
	 */
	String kegIDSubject;
	/**
	 * 保存subject物种的keggID
	 */
	public void setKegIDSubject(String kegIDSubject)
	{
		this.kegIDSubject=kegIDSubject;
	}
	/**
	 * 保存subject物种的keggID
	 */
	public String getKegIDSubject()
	{
		return this.kegIDSubject;
	}
	
	/**
	 * blast得到的基因获得其KO后，将KOmapping回query物种的pathway，获得query物种的ArrayList-entry
	 */
	ArrayList<KGentry> blastgen2Entry;
	/**
	 * blast得到的基因获得其KO后，将KOmapping回query物种的pathway，获得query物种的ArrayList-entry
	 */
	public void setLsBlastgen2Entry(ArrayList<KGentry> blastgen2Entry)
	{
		this.blastgen2Entry = blastgen2Entry;
	}
	/**
	 * 保存blast得到的基因的Kegg到Path信息
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
