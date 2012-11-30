package com.novelbio.database.model.modgeneid;

import java.util.ArrayList;

import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.AGeneInfo;
import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.domain.kegg.KGentry;
import com.novelbio.database.domain.kegg.KGpathway;
import com.novelbio.database.model.modgo.GOInfoAbs;
import com.novelbio.database.model.modkegg.KeggInfo;

public interface GeneIDInt{
 
	/**
	 * idType，必须是IDTYPE中的一种
	 */
	public String getIDtype();
	
	/**
	 * 具体的accID，如果没有则根据物种随机抓一个出来
	 */
	public String getAccID();


	/**
	 * 获得geneID
	 * 如果是accID就返回-1
	 * @return
	 */
	public String getGenUniID();
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
	public ArrayList<KGentry> getKegEntity(boolean blast) ;
	
	
	/**
	 * 	 * 指定一个dbInfo，返回该dbInfo所对应的accID，没有则返回null
	 * @param dbInfo
	 * @return
	 */
	public String getAccIDDBinfo(String dbInfo);
	
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
	public KeggInfo getKeggInfo();
	/**
	 * blast多个物种 首先要设定blast的目标 用方法： setBlastInfo(double evalue, int... StaxID)
	 * 给定一系列的目标物种的taxID，获得CopedIDlist 如果没有结果，返回一个空的lsResult
	 * @param evalue
	 * @param StaxID
	 * @return
	 */
	public ArrayList<GeneID> getLsBlastGeneID();
 
	/**
	 * 设定多个物种进行blast
	 * @param evalue
	 * @param StaxID
	 */
	public void setBlastInfo(double evalue, int... StaxID);
	/**
	 * 返回该CopedID所对应的Gene2GOInfo <br>
	 * GO_BP<br>
	 * GO_CC<br>
	 * GO_MF<br>
	 * GO_ALL<br>
	 * @param GOType
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
	void setUpdateGeneID(String geneUniID, String idType);
	/**
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
	void setUpdateGO(String GOID, String GOdatabase, String GOevidence,
			String GORef, String gOQualifiy);
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
	void setUpdateRefAccID(ArrayList<String> lsRefAccID);
	/**
	 * 如果没有QueryID, SubjectID, taxID中的任何一项，就不升级 如果evalue>50 或 evalue<0，就不升级
	 * 可以连续不断的添加
	 * @param SubAccID 目标物种的accID
	 * @param subDBInfo 目标物种的数据库来源
	 * @param SubTaxID 目标物种的物种ID
	 * @param evalue 相似度evalue
	 * @param identities 相似度信息
	 */
	void setUpdateBlastInfo(String SubAccID, String subDBInfo, int SubTaxID,
			double evalue, double identities);
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
	/**
	 * <b>全新加入ID，以前的ID会被清空</b>
	 * 在采用refaccID作为参照进行升级ID的时候，是否必须是uniqID
	 * uniqID：用给定的参考ID能找到数据库中的唯一基因
	 * true：只有当uniqID时才升级
	 * null：默认参数--非uniqID也升级，不过只升级第一个基因
	 * false：非uniqID也升级，升级搜索到的全部ID，该功能尚未实现
	 * @param uniqID
	 */
	void setUpdateRefAccIDClear(Boolean uniqID);
	/**<b>添加ID</b>
	 * 在采用refaccID作为参照进行升级ID的时候，是否必须是uniqID
	 * uniqID：用给定的参考ID能找到数据库中的唯一基因
	 * true：只有当uniqID时才升级
	 * null：默认参数--非uniqID也升级，不过只升级第一个基因
	 * false：非uniqID也升级，升级搜索到的全部ID，该功能尚未实现
	 * @param uniqID
	 */
	void addUpdateRefAccID(String... refAccID);
	/**
	 * 如果blast到geneUniID上去
	 * @param SubGenUniID
	 * @param subIDtype
	 * @param subDBInfo
	 * @param SubTaxID
	 * @param evalue
	 * @param identities
	 */
	void setUpdateBlastInfo(String SubGenUniID, String subIDtype,
			String subDBInfo, int SubTaxID, double evalue, double identities);
	/**
	 * 如果输入的是accID，那么返回该accID对应的数据库
	 * 如果没有则返回null
	 * @return
	 */
	String getDBinfo();
	/**
	 * 具体的accID，根据数据库情况抓一个出来
	 */
	String getAccIDDBinfo();




}
