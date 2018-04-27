package com.novelbio.database.domain.modgeneid;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.database.DBAccIDSource;
import com.novelbio.database.domain.modgo.GOInfoAbs;
import com.novelbio.database.domain.modkegg.KeggInfo;
import com.novelbio.database.domain.modkegg.KeggInfoAbs;
import com.novelbio.database.model.geneanno.AGene2Go;
import com.novelbio.database.model.geneanno.AGeneInfo;
import com.novelbio.database.model.geneanno.AgeneUniID;
import com.novelbio.database.model.geneanno.BlastInfo;
import com.novelbio.database.model.geneanno.DBInfo;
import com.novelbio.database.model.geneanno.GOtype;
import com.novelbio.database.model.kegg.KGentry;
import com.novelbio.database.model.kegg.KGpathway;

public interface GeneIDInt {

	AgeneUniID getAgeneUniID();
	/**
	 * idType，必须是IDTYPE中的一种
	 */
	public int getIDtype();
	/**
	 * 具体的accID，如果没有则根据物种随机抓一个出来
	 */
	public String getAccID();

	/**
	 * 如果数据库中没有找到对应的accID，则设定时候输入的是什么taxID，返回的就是同样的taxID
	 * @return
	 */
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
	public String getSymbol();
	
	/**
	 * 获得该CopeID的List-KGentry,如果没有或为空，则返回null
	 * @param blast 是否blast到相应物种查看
	 * @param StaxID 如果blast为true，那么设定StaxID
	 * @return 如果没有就返回null
	 */
	public List<KGentry> getKegEntity(boolean blast) ;
	
	
	/**
	 * 	 * 指定一个dbInfo，返回该dbInfo所对应的accID，没有则返回null
	 * @param dbInfo
	 * @return
	 */
	public AgeneUniID getAccIDDBinfo(String dbInfo);
	
	/**
	 * 先设定blast的情况 如果blast * 0:symbol 1:description  2:subjectSpecies 3:evalue
	 * 4:symbol 5:description 如果不blast 0:symbol 1:description
	 * 
	 * @return
	 */
	public String[] getAnno( boolean blast ) ;
	/**
	 * 获得相应的KeggInfo信息
	 * @return
	 */
	public KeggInfoAbs getKeggInfo();
	/**
	 * blast多个物种 首先要设定blast的目标 用方法： setBlastInfo(double evalue, int... StaxID)
	 * 给定一系列的目标物种的taxID，获得CopedIDlist 如果没有结果，返回一个空的lsResult
	 * @param evalue
	 * @param StaxID
	 * @return
	 */
	public List<GeneID> getLsBlastGeneID();
 
	/**
	 * 设定多个物种进行blast
	 * @param evalue
	 * @param StaxID
	 */
	public void setBlastInfo(double evalue, int... StaxID);
	
	/**
	 * 设定多个物种进行blast
	 * @param evalue
	 * @param StaxID
	 */
	public void setBlastInfo(double evalue, List<Integer> lsStaxID);
	/**
	 * 返回该CopedID所对应的Gene2GOInfo <br>
	 * GO_BP<br>
	 * GO_CC<br>
	 * GO_MF<br>
	 * GO_ALL<br>
	 * @param GOType
	 * @return
	 */
	public List<AGene2Go> getGene2GO(GOtype GOType);

	
	//////////   GoInfo   ////////////////
	/**
	 * 	blast多个物种
	 * 首先设定blast的物种
	 * 用方法： setBlastInfo(double evalue, int... StaxID)
	 * 获得经过blast的GoInfo
	 */
	public List<AGene2Go> getGene2GOBlast(GOtype GOType);
	/**
	 * blast多个物种
	 * 首先要设定blast的目标
	 * 用方法： setBlastInfo(double evalue, int... StaxID)
	 * @return
	 * 返回blast的信息，包括evalue等，该list和getCopedIDLsBlast()得到的list是一一对应的
	 */
	public List<BlastInfo> getLsBlastInfos();

	/**
	 * 单个物种的blast 获得本copedID blast到对应物种的第一个copedID，没有就返回null
	 * 
	 * @param StaxID
	 * @param evalue
	 * @return
	 */
	GeneID getGeneIDBlast();
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
	void setUpdateGeneID(String geneUniID, int idType);
	
	/**
	 * <b>务必在设定好lsRef等信息后再添加</b>
	 * 依次输入需要升级的GO信息，最后升级<br>
	 * 这里只是先获取GO的信息，最后调用升级method的时候再升级<br>
	 * 可以连续不断的添加
	 * @param GOID
	 * @param GOdatabase
	 * @param GOevidence
	 * @param GORef a GO_REF identifier. See section 8 and
http://www.geneontology.org/doc/GO.references<br>
for an explanation of the reference types used.<br>
Examples: PMID:9058808<br>
DOI:10.1046/j.1469-8137.2001.00150.x<br>
GO_REF:0000002<br>
GO_REF:0000020<br>
GO_REF:0000004<br>
	 * @param gOQualifiy
	 */
	void addUpdateGO(String GOID, DBAccIDSource GOdatabase, String GOevidence,
			List<String> lsGOref, String gOQualifiy);
	/**
	 * 依次输入需要升级的GO信息，最后升级<br>
	 * 这里只是先获取GO的信息，最后调用升级method的时候再升级<br>
	 * 可以连续不断的添加<br>
	 * <b>不需要设定genUniID和taxId</b>
	 */
	void addUpdateGO(AGene2Go aGene2Go);

	/**
	 * 输入需要update的geneInfo，注意不需要设定geneUniID，除非是单独升级pubmedID信息，否则
	 * <b>务必要设定geneinfo的dbinfo</b>，dbinfo是判定该geneinfo数据库来源的信息<br>
	 * 此外如果还需要设定synonme，并且synonme是被“|”等符号隔开<b>，则还需设定分隔符 "\\|"</b>
	 * @param geneInfo
	 */
	public void setUpdateGeneInfo(AGeneInfo geneInfo);
	/**
	 * 如果新的ID不加入UniID，那么就返回false
	 * 如果升级不成功，也返回false
	 * 文件需要最开始用set指定
	 * @param updateUniID 新的ID是否加入UniID
	 */
	boolean update(boolean updateUniID);
	/**
	 * 记录该ID的物种ID和数据库信息，用于修正以前的数据库
	 * 
	 * @param taxID
	 * @param DBInfo
	 * @param 是否用本DBInfo修正以前的DBInfo
	 * 不管是true还是false，geneinfo都会用其进行修正
	 */
	void setUpdateDBinfo(DBAccIDSource DBInfo, boolean overlapDBinfo);
	/**
	 * 记录该ID的物种ID和数据库信息，用于修正以前的数据库
	 * 
	 * @param taxID
	 * @param DBInfo
	 * @param 是否用本DBInfo修正以前的DBInfo
	 * 不管是true还是false，geneinfo都会用其进行修正
	 */
	void setUpdateDBinfo(String DBInfo, boolean overlapDBinfo);
	/**
	 * 记录可能用于升级数据库的ID 譬如获得一个ID与NCBI的别的ID有关联，就用别的ID来查找数据库，以便获得该accID所对应的genUniID
	 * <b>重新设定的时候会清空</b>
	 * @param updateUniID
	 */
	void setUpdateRefAccID(String... refAccID);
	/**
	 * 记录可能用于升级数据库的ID 譬如获得一个ID与NCBI的别的ID有关联，就用别的ID来查找数据库，以便获得该accID所对应的genUniID
	 */
	void setUpdateRefAccID(List<String> lsRefAccID);

	/**
	 * 设定accID，当用geneUniID进行new copedID工作时使用
	 * @param accID
	 */
	void setUpdateAccID(String accID);
	/**
	 * 设定该ID的accID，不经过处理的ID
	 */
	void setUpdateAccIDNoCoped(String accID);
	/**
	 * 返回geneinfo信息
	 * @return
	 */
	AGeneInfo getGeneInfo();

	/**<b>添加RefID</b>
	 * 在采用refaccID作为参照进行升级ID
	 */
	void addUpdateRefAccID(String... refAccID);
	
	/** <b>务必在设定好lsRef等信息后再添加</b> 
	 * 其中的queryID, queryTax, queryIDtype 信息会被GeneID中的信息替换
	 */
	void addUpdateBlastInfo(BlastInfo blastInfo);

	/**
	 * 如果输入的是accID，那么返回该accID对应的数据库
	 * 如果没有则返回null
	 * @return
	 */
	DBInfo getDBinfo();
	/**
	 * 具体的accID，根据数据库情况抓一个出来
	 * 没找到，也就是说数据库中没有该基因，则返回Null
	 */
	AgeneUniID getAccID_With_DefaultDB();
	
	public boolean equals(Object obj);
	
	public int hashCode();
	
	/** 有genUniID的返回genUniID,没有的返回accID
	 * @return
	 */
	String getGeneUniID();

	
}
