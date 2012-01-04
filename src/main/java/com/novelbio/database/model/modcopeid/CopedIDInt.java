package com.novelbio.database.model.modcopeid;

import java.util.ArrayList;

import com.novelbio.analysis.annotation.pathway.kegg.pathEntity.KegEntity;
import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.AGeneInfo;
import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.domain.kegg.KGpathway;
import com.novelbio.database.model.modgo.GOInfoAbs;
import com.novelbio.database.model.modkegg.KeggInfo;

public interface CopedIDInt{
 
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
	public ArrayList<KegEntity> getKegEntity(boolean blast) ;
	
	
	/**
	 * 	 * 指定一个dbInfo，返回该dbInfo所对应的accID，没有则返回null
	 * @param dbInfo
	 * @return
	 */
	public String getAccIDDBinfo(String dbInfo);
	
	/**
	 * 如果blast 选择blast的第一个物种<br>
	 * 0:symbol 1:description 2:subjectTaxID 3:evalue 4:symbol 5:description 如果不blast 0:symbol 1:description
	 * @return
	 */
	public String[] getAnno( boolean blast ) ;
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
	public ArrayList<CopedID> getCopedIDLsBlast();
 
	/**
	 * 设定多个物种进行blast
	 * @param evalue
	 * @param StaxID
	 */
	public void setBlastInfo(double evalue, int... StaxID);
	/**
	 * 返回该CopeID所对应的GO信息
	 * @return
	 */
	public ArrayList<AGene2Go> getGene2GO(String GOType);

	
	//////////   GoInfo   ////////////////
	/**
	 * 	blast多个物种
	 * 首先设定blast的物种
	 * 用方法： setBlastInfo(double evalue, int... StaxID)
	 * 获得经过blast的GoInfo
	 */
	public ArrayList<AGene2Go> getGene2GOBlast(String GOType);
	/**
	 * blast多个物种
	 * 首先要设定blast的目标
	 * 用方法： setBlastInfo(double evalue, int... StaxID)
	 * @return
	 * 返回blast的信息，包括evalue等，该list和getCopedIDLsBlast()得到的list是一一对应的
	 */
	public ArrayList<BlastInfo> getLsBlastInfos();

	/**
	 * 返回第一个比对到的物种
	 * @return
	 */
	CopedID getCopedIDBlast();
	/**
	 * 	blast多个物种
	 * 首先设定blast的物种
	 * 用方法： setBlastInfo(double evalue, int... StaxID)
	 * 获得经过blast的KegPath
	 */
	ArrayList<KGpathway> getKegPath(boolean blast);
	/**
	 * 输入已知的geneUniID和IDtype
	 * @param geneUniID
	 * @param idType 必须是CopedID.IDTYPE_GENEID等，可以不输入
	 */
	void setUpdateGeneID(String geneUniID, String idType);
	/**
	 * 依次输入需要升级的GO信息，最后升级
	 * 这里只是先获取GO的信息，最后调用升级method的时候再升级
	 * @param GOID
	 * @param GOdatabase
	 * @param GOevidence
	 * @param GORef
	 * @param gOQualifiy
	 */
	void setUpdateGO(String GOID, String GOdatabase, String GOevidence,
			String GORef, String gOQualifiy);
	/**
	 * 输入需要update的geneInfo，注意不需要设定geneUniID
	 * @param geneInfo
	 */
	void setUpdateGeneInfo(AGeneInfo geneInfo);
	/**
	 * 如果新的ID不加入UniID，那么就写入指定的文件中
	 * 文件需要最开始用set指定
	 * @param updateUniID
	 */
	void update(boolean updateUniID);
	/**
	 * 如果新的ID不加入UniID，那么就写入指定的文件中
	 * 文件需要最开始用set指定
	 * @param updateUniID
	 */
	void setUpdateDBinfo(String DBInfo, boolean overlapDBinfo);
	/**
	 * 如果新的ID不加入UniID，那么就写入指定的文件中
	 * 文件需要最开始用set指定
	 * @param updateUniID
	 */
	void setUpdateRefAccID(int taxID, String DBInfo, String... refAccID);
}
