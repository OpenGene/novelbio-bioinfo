package com.novelbio.analysis.annotation.copeID;

import java.util.ArrayList;
import com.novelbio.analysis.annotation.pathway.kegg.pathEntity.KegEntity;
import com.novelbio.analysis.annotation.pathway.kegg.pathEntity.KeggInfo;
import com.novelbio.database.entity.friceDB.BlastInfo;
import com.novelbio.database.entity.kegg.KGpathway;

public interface CopedIDInt {
	
	
	/**
	 * 获得本copedID blast到对应物种的blastInfo信息，没有就返回null
	 * @param StaxID
	 * @param evalue
	 * @return
	 */
	public BlastInfo setBlastInfo(int StaxID, double evalue);
	
	
	/**
	 * 获得本copedID blast到对应物种的copedID，没有就返回null
	 * @param StaxID
	 * @param evalue
	 * @return
	 */
	public CopedID getBlastCopedID(int StaxID,double evalue) ;
	
	/**
	 * idType，必须是IDTYPE中的一种
	 */
	public String getIDtype();
	
	/**
	 * 具体的accID
	 */
	public String getAccID();


	/**
	 * 获得geneID
	 * @return
	 */
	public String getGenUniID();
	
	public int getTaxID() ;
	
	/**
	 * 获得该基因的description
	 * @return
	 */
	public String getDescription() ;
	/**
	 * 获得该基因的symbol
	 * @return
	 */
	public String getSymbo();
	
	/**
	 * 获得该CopeID的List-KGentry,如果没有或为空，则返回null
	 * @param blast 是否blast到相应物种查看
	 * @param StaxID 如果blast为true，那么设定StaxID
	 * @return 如果没有就返回null
	 */
	public ArrayList<KegEntity> getKegEntity(boolean blast,int StaxID,double evalue) ;
	
	
	/**
	 * 	 * 指定一个dbInfo，返回该dbInfo所对应的accID，没有则返回null
	 * @param dbInfo
	 * @return
	 */
	public String getAccIDDBinfo(String dbInfo);
	
	/**
	 * 如果blast * 0:symbol 1:description 2:subjectTaxID 3:evalue 4:symbol 5:description 如果不blast 0:symbol 1:description
	 * @return
	 */
	public String[] getAnno( boolean blast, int StaxID, double evalue) ;
	/**
	 * 获得相应的KeggInfo信息
	 * @return
	 */
	public KeggInfo getKeggInfo();
	/**
	 * blast多个物种
	 * 首先要设定blast的目标
	 * 用方法： setBlastInfo(double evalue, int... StaxID)
	 * 给定一系列的目标物种的taxID，获得CopedIDlist
	 * 如果没有结果，直接返回null
	 * @param evalue
	 * @param StaxID
	 * @return
	 */
	public ArrayList<CopedID> getBlastLsCopedID();
	/**
	 * 	blast多个物种
	 * 首先设定blast的物种
	 * 用方法： setBlastInfo(double evalue, int... StaxID)
	 * 获得经过blast的KegPath
	 */
	public ArrayList<KGpathway> getBlastKegPath();
	/**
	 * blast单个物种
	 * 给定blast到的copedID，用 getBlastCopedID(int StaxID,double evalue) 方法获得
	 * 用方法： setBlastInfo(double evalue, int... StaxID)
	 * 获得经过blast的KegPath
	 */
	public ArrayList<KGpathway> getBlastKegPath(CopedID copedID);
	/**
	 * 获得该copedID的KegPath
	 */
	public ArrayList<KGpathway> getKegPath();
	/**
	 * 设定多个物种进行blast
	 * @param evalue
	 * @param StaxID
	 */
	public void setBlastLsInfo(double evalue, int... StaxID);
}
