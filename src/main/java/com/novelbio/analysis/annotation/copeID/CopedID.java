package com.novelbio.analysis.annotation.copeID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import com.novelbio.analysis.annotation.pathway.kegg.pathEntity.KegEntity;
import com.novelbio.analysis.annotation.pathway.kegg.pathEntity.KeggInfo;
import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.database.DAO.FriceDAO.DaoFSNCBIID;
import com.novelbio.database.DAO.FriceDAO.DaoFSTaxID;
import com.novelbio.database.DAO.FriceDAO.DaoFSUniProtID;
import com.novelbio.database.entity.friceDB.BlastInfo;
import com.novelbio.database.entity.friceDB.NCBIID;
import com.novelbio.database.entity.friceDB.TaxInfo;
import com.novelbio.database.entity.friceDB.UniProtID;
import com.novelbio.database.entity.kegg.KGpathway;
import com.novelbio.database.service.ServAnno;

/**
 * 这只是一个代理类 专门对基因的ID做一些处理的类<br>
 * <b>如果基因的IDtype是accID，那么该基因很可能不存在，那么看下blast的相关信息，如果blast也没有，那么就不存在了</b><br>
 * 可以将输入的ID合并起来，并且将分散的ID存储在一个Hashmap中
 * 当genUniID存在时，类是否想等只比较genUniID、taxID，idType是否相等
 * 当genUniID不存在，accID存在时，比较accID、taxID，idType是否相等 当都不存在时，认为相同 HashCode的设定和这个类似
 * 
 * @author zong0jie
 */
public class CopedID implements CopedIDInt{
	public final static String IDTYPE_ACCID = "accID"; 
	public final static String IDTYPE_GENEID = "geneID";
	public final static String IDTYPE_UNIID = "uniID";

	private CopedIDAbs copedID;

	/**
	 * 设定初始值，不验证 如果在数据库中没有找到相应的geneUniID，则返回null 只能产生一个CopedID，此时accID = ""
	 * 
	 * @param idType
	 *            必须是IDTYPE中的一种
	 * @param genUniID
	 * @param taxID
	 *            物种ID
	 */
	public CopedID(String idType, String genUniID, int taxID) {
		if (idType.equals(IDTYPE_UNIID)) {
			copedID = new CopedIDuni("",idType, genUniID, taxID);
		}
		else if (idType.equals(IDTYPE_GENEID)) {
			copedID = new CopedIDgen("",idType, genUniID, taxID);
		}
		else if (idType.equals(IDTYPE_ACCID)) {
			copedID = new CopedIDacc("",idType, genUniID, taxID);
		}
	}

	/**
	 * 设定初始值，不验证 如果在数据库中没有找到相应的geneUniID，则返回null 只能产生一个CopedID，此时accID = ""
	 * 
	 * @param idType
	 *            必须是IDTYPE中的一种
	 * @param genUniID
	 * @param taxID
	 *            物种ID
	 */
	public CopedID(String accID,String idType, String genUniID, int taxID) {
		if (idType.equals(IDTYPE_UNIID)) {
			copedID = new CopedIDuni(accID,idType, genUniID, taxID);
		}
		else if (idType.equals(IDTYPE_GENEID)) {
			copedID = new CopedIDgen(accID,idType, genUniID, taxID);
		}
		else if (idType.equals(IDTYPE_ACCID)) {
			copedID = new CopedIDacc(accID,idType, genUniID, taxID);
		}
	}
	
	/**
	 * 设定初始值，会自动去数据库查找accID并，完成填充本类。
	 * <b>如果基因的IDtype是accID，那么该基因很可能不存在，那么看下blast的相关信息，如果blast也没有，那么就不存在了</b>
	 * 不过只能产生一个CopedID，如果觉得一个accID要产生多个geneID，那么可以选择getLsCopedID方法
	 * @param accID 如果类似XM_002121.1类型，那么将.1去除
	 * @param taxID
	 * @param blastType 具体的accID是否类似 blast的结果，如：dbj|AK240418.1|，那么获得AK240418，一般都是false
	 */
	public CopedID(String accID,int taxID,boolean blastType) {
		if (blastType)
			accID = getBlastAccID(accID);
		else
			accID = removeDot(accID);
		ArrayList<String> lsaccID = getNCBIUniTax(accID, taxID);
		String idType = lsaccID.get(0); taxID = Integer.parseInt(lsaccID.get(1));
		String tmpGenID = lsaccID.get(2);
		if (idType.equals(IDTYPE_UNIID)) {
			copedID = new CopedIDuni(accID, idType, tmpGenID, taxID);
		}
		else if (idType.equals(IDTYPE_GENEID)) {
			copedID = new CopedIDgen(accID,idType, tmpGenID, taxID);
		}
		else if (idType.equals(IDTYPE_ACCID)) {
			copedID = new CopedIDacc(accID,idType, tmpGenID, taxID);
		}
	}
	
	/**
	 * 设定初始值，会自动去数据库查找accID并完成填充本类。
	 * @param accID 如果类似XM_002121.1类型，那么将.1去除
	 * @param taxID
	 * @param blastType 具体的accID是否类似 blast的结果，如：dbj|AK240418.1|，那么获得AK240418，一般都是false
	 */
	public static ArrayList<CopedID> getLsCopedID(String accID,int taxID,boolean blastType) {
		ArrayList<CopedID> lsCopedIDs = new ArrayList<CopedID>();
		if (blastType) 
			accID = accID.split("\\|")[1];
		accID = removeDot(accID);
		
		ArrayList<String> lsaccID = getNCBIUniTax(accID, taxID);
		String idType = lsaccID.get(0); taxID = Integer.parseInt(lsaccID.get(1));
		 for (int i = 2 ; i < lsaccID.size(); i++) {
			 String tmpGenID = lsaccID.get(i);
			 CopedID copedID = new CopedID(accID, idType, tmpGenID, taxID);
			 lsCopedIDs.add(copedID);
		 }
		 return lsCopedIDs;
	}
	
	
	/**
	 * 设定初始值，会自动去数据库验证geneUniID并完成填充本类。
	 * 如果在数据库中没有找到相应的geneUniID，则返回null
	 * 只能产生一个CopedID，此时accID = ""
	 * @param idType 必须是IDTYPE中的一种
	 * @param genUniID
	 * @param taxID 物种ID
	 */
	public static CopedID validCopedID(String idType, String genUniID,int taxID) {
		CopedID copedID = null;
		if (idType.equals(IDTYPE_ACCID)) {
			return null;
		}
		else if (idType.equals(IDTYPE_GENEID)) {
			NCBIID ncbiid = ServAnno.getNCBIID(Integer.parseInt(genUniID), taxID);
			if (ncbiid != null) {
				String genUniID2 = ncbiid.getGeneId() + "";
				int taxID2 = ncbiid.getTaxID();
				copedID = new CopedID(idType, genUniID2, taxID2);
				return copedID;
			}
		}
		else if (idType.equals(IDTYPE_UNIID)) {
			UniProtID uniProtID = ServAnno.getUniProtID(genUniID, taxID);
			if (uniProtID != null) {
				String genUniID2 = uniProtID.getUniID();
				int taxID2 = uniProtID.getTaxID();
				copedID = new CopedID(idType, genUniID2, taxID2);
				return copedID;
			}
		}
		return null;
	}
	
	
	@Override
	public BlastInfo setBlastInfo(int StaxID, double evalue) {
		return copedID.setBlastInfo(StaxID, evalue);
	}

	@Override
	public CopedID getBlastCopedID(int StaxID, double evalue) {
		return copedID.getBlastCopedID(StaxID, evalue);
	}

	@Override
	public String getIDtype() {
		return copedID.getIDtype();
	}

	@Override
	public String getAccID() {
		return copedID.getAccID();
	}

	@Override
	public String getGenUniID() {
		return copedID.getGenUniID();
	}

	@Override
	public int getTaxID() {
		return copedID.getTaxID();
	}
	
	@Override
	public String getDescription() {
		return copedID.getDescription();
	}
	
	@Override
	public String getSymbo() {
		return copedID.getSymbo();
	}

	@Override
	public ArrayList<KegEntity> getKegEntity(boolean blast, int StaxID,
			double evalue) {
		return copedID.getKegEntity(blast, StaxID, evalue);
	}



	@Override
	public String getAccIDDBinfo(String dbInfo) {
		return copedID.getAccIDDBinfo(dbInfo);
	}

	@Override
	public String[] getAnno(boolean blast, int StaxID, double evalue) {
		return copedID.getAnno(blast, StaxID, evalue);
	}
	@Override
	public KeggInfo getKeggInfo() {
		return copedID.getKeggInfo();
	}

	@Override
	public ArrayList<CopedID> getBlastLsCopedID() {
		return copedID.getBlastLsCopedID();
	}

	@Override
	public ArrayList<KGpathway> getBlastKegPath() {
		return copedID.getBlastKegPath();
	}

	@Override
	public ArrayList<KGpathway> getBlastKegPath(CopedID copedID) {
		return copedID.getBlastKegPath(copedID);
	}

	@Override
	public ArrayList<KGpathway> getKegPath() {
		return copedID.getKegPath();
	}

	@Override
	public void setBlastLsInfo(double evalue, int... StaxID) {
		copedID.setBlastLsInfo(evalue,StaxID);
	}

	
	
	
	/////////////////////////////  static 方法  ////////////////////////////////////
	/**
	 * blast的结果可能类似dbj|AK240418.1|
	 * 将里面的AK240418抓出来并返回
	 * @return
	 */
	public static String getBlastAccID(String blastGenID) {
		String[] ss = blastGenID.split("\\|");
		return removeDot(ss[1]);
	}

	/**
	 *  首先除去空格
	 * 如果类似XM_002121.1类型，那么将.1去除
	 * @param accID
	 * @return accID without .1
	 */
	public static String removeDot(String accID)
	{
		String tmpGeneID = accID.trim();
		int dotIndex = tmpGeneID.lastIndexOf(".");
		//如果类似XM_002121.1类型
		if (dotIndex>0 && tmpGeneID.length() - dotIndex == 2) {
			tmpGeneID = tmpGeneID.substring(0,dotIndex);
		}
		return tmpGeneID;
	}
	/**
	 * @param collectionAccID 常规的accIDlist，可以先对accID去一次重复
	 * @param taxID
	 * @param combineID
	 * @return
	 */
	public static ArrayList<CopedID> getLsCopedID(Collection<String> collectionAccID, int taxID, boolean combineID) {
		ArrayList<CopedID> lsResult = new ArrayList<CopedID>();
		HashSet<CopedID> hashCopedIDs = null; //用hash表来去重复，因为已经重写过hash了
		if (combineID) {
			hashCopedIDs = new HashSet<CopedID>();
		}
		for (String string : collectionAccID) {
			CopedID copedID = new CopedID(string, taxID, false);
			if (combineID) {
				hashCopedIDs.add(copedID);
			}
			else {
				lsResult.add(copedID);
			}
		}
		
		if (combineID) {
			lsResult = new ArrayList<CopedID>();
			for (CopedID copedID : hashCopedIDs) {
				lsResult.add(copedID);
			}
		}
		return lsResult;
	}
	
	/**
	 * @param collectionAccID 常规的accIDlist，可以先对accID去一次重复
	 * @param taxID
	 * @return
	 * HashMap-CopedID, ArrayList-String
	 * key CopedID
	 * value 相同copedID对应的不同accID的list
	 */
	public static HashMap<CopedID, ArrayList<String>> getHashCopedID(Collection<String> collectionAccID, int taxID) {
		HashMap<CopedID, ArrayList<String>> hashResult = new HashMap<CopedID, ArrayList<String>>();
		for (String string : collectionAccID) {
			CopedID copedID = new CopedID(string, taxID, false);
			if (hashResult.containsKey(copedID) && !hashResult.get(copedID).contains(string) ) {
				hashResult.get(copedID).add(string);
			}
			else if (!hashResult.containsKey(copedID)){
				ArrayList<String> lsAccID = new ArrayList<String>();
				lsAccID.add(string);
				hashResult.put(copedID, lsAccID);
			}
		}
		return hashResult;
	}
	
/////////////////////// 私有 static 方法 /////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 给定一个accessID，如果该access是NCBIID，则返回NCBIID的geneID
	 * 如果是uniprotID，则返回Uniprot的UniID
	 * 如果输入得到了两个以上的accID，那么跳过数据库为DBINFO_SYNONYMS的项目
	 * @param accID 输入的accID,没有内置去空格去点
	 * @param taxID 物种ID，如果不知道就设置为0，只要不是symbol都可以为0
	 * @return arraylist-string:0:为"geneID"或"uniID"或"accID"，1:taxID，2-之后：具体的geneID 或 UniID或accID<br>
	 * 没查到就返回accID-accID
	 */
	public static ArrayList<String> getNCBIUniTax(String accID,int taxID) {
		ArrayList<String> lsResult = new ArrayList<String>();
		NCBIID ncbiid = new NCBIID();
		ncbiid.setAccID(accID); ncbiid.setTaxID(taxID);
		ArrayList<NCBIID> lsNcbiids = DaoFSNCBIID.queryLsNCBIID(ncbiid);
		ArrayList<UniProtID> lsUniProtIDs = null;
		//先查ncbiid
		if (lsNcbiids != null && lsNcbiids.size() > 0)
		{
			lsResult.add(CopedID.IDTYPE_GENEID); lsResult.add(lsNcbiids.get(0).getTaxID()+"");
			for (NCBIID ncbiid2 : lsNcbiids) {
				if (ncbiid2.getDBInfo().equals(NovelBioConst.DBINFO_SYNONYMS)) {
					continue;
				}
				lsResult.add(ncbiid2.getGeneId()+"");
			}
			if (lsResult.size() <= 2) {
				lsResult.add(lsNcbiids.get(0).getGeneId()+"");
			}
			return lsResult;
		}
		//查不到查uniprotID
		else 
		{
			UniProtID uniProtID = new UniProtID();
			uniProtID.setAccID(accID); uniProtID.setTaxID(taxID);
			lsUniProtIDs = DaoFSUniProtID.queryLsUniProtID(uniProtID);
			if (lsUniProtIDs != null && lsUniProtIDs.size() > 0) 
			{
				lsResult.add(CopedID.IDTYPE_UNIID);lsResult.add(lsUniProtIDs.get(0).getTaxID()+"");
				for (UniProtID uniProtID2 : lsUniProtIDs) {
					if (uniProtID2.getDBInfo().equals(NovelBioConst.DBINFO_SYNONYMS)) {
						continue;
					}
					lsResult.add(uniProtID2.getUniID());
				}
				if (lsResult.size() <= 2) {
					lsResult.add(lsUniProtIDs.get(0).getUniID()+"");
				}
				return lsResult;
			}
		}
		lsResult.add(CopedID.IDTYPE_ACCID); lsResult.add(taxID+"");
		lsResult.add(accID);
		return lsResult;
	}

	
	/**
	 * 读取数据库中的taxID表，将其中的species读取出来并保存为taxID,speciesInfo
	 * @return
	 * HashMap - key:Integer taxID
	 * value: 0: Kegg缩写 1：拉丁名
	 */
	public static HashMap<Integer, String[]> getSpecies() 
	{
		TaxInfo taxInfo = new TaxInfo();
		ArrayList<TaxInfo> lsTaxID = DaoFSTaxID.queryLsTaxInfo(taxInfo);
		HashMap<Integer,String[]> hashTaxID = new HashMap<Integer, String[]>();
		for (TaxInfo taxInfo2 : lsTaxID) {
			if (taxInfo2.getAbbr() == null || taxInfo2.getAbbr().trim().equals("")) {
				continue;
			}
			
			hashTaxID.put( taxInfo2.getTaxID(),new String[]{taxInfo2.getAbbr(),taxInfo2.getLatin()});
		}
		return hashTaxID;
	}

	/////////////////////////////  重写equals等  ////////////////////////////////////
	/**
	 * 只要两个ncbiid的geneID相同，就认为这两个NCBIID相同
	 * 但是如果geneID为0，也就是NCBIID根本没有初始化，那么直接返回false
	 * 	@Override
	 */
	public boolean equals(Object obj) {
		if (this == obj) return true;
		
		if (obj == null) return false;
		
		if (getClass() != obj.getClass()) return false;
		CopedID otherObj = (CopedID)obj;
		
		if (
				//geneID相同且都不为“”，可以认为两个基因相同
				(!copedID.genUniID.trim().equals("")
				&& !otherObj.getGenUniID().trim().equals("") 
				&& copedID.genUniID.trim().equals(otherObj.getGenUniID().trim())	
				&& copedID.idType.equals(otherObj.getIDtype())
				&& copedID.taxID == otherObj.getTaxID()
				)
				||//geneID都为""，那么如果两个accID相同且不为""，也可认为两个基因相同
				(copedID.genUniID.trim().equals("")
				&& otherObj.getGenUniID().trim().equals("") 
				&& ( !copedID.accID.equals("") && !otherObj.getAccID().equals("") )
				&& copedID.accID.equals(otherObj.getAccID())
				&& copedID.idType.equals(otherObj.getIDtype())
				&& copedID.taxID == otherObj.getTaxID()
				)
				||
				//或者geneID和accID都为""也可以认为两个基因相同
				(copedID.genUniID.trim().equals("")
				&& otherObj.getGenUniID().trim().equals("") 
				&& copedID.accID.equals("") 
				&& otherObj.getAccID().equals("")						
				)
		)
		{
			return true;
		}
		return false;
	}

	/**
	 * 重写hashcode
	 */
	public int hashCode(){
		String hash = "";
		if (!copedID.genUniID.trim().equals("")) {
			hash = copedID.genUniID.trim() + "sep_@@_genUni_" + copedID.idType.trim() + "@@" + copedID.taxID;
		}
		else if (copedID.genUniID.trim().equals("") && !copedID.accID.trim().equals("")) {
			hash = copedID.accID.trim()+"@@accID"+copedID.idType.trim()+"@@"+copedID.taxID;
		}
		else if ( copedID.genUniID.trim().equals("") && copedID.accID.trim().equals("")) {
			hash = "";
		}
		return hash.hashCode();
	}
	
}
