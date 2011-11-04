package com.novelbio.database.model.modcopeid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import com.novelbio.analysis.annotation.pathway.kegg.pathEntity.KegEntity;
import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.domain.geneanno.NCBIID;
import com.novelbio.database.domain.geneanno.TaxInfo;
import com.novelbio.database.domain.geneanno.UniProtID;
import com.novelbio.database.domain.kegg.KGpathway;
import com.novelbio.database.mapper.geneanno.MapFSTaxID;
import com.novelbio.database.model.modgo.GOInfoAbs;
import com.novelbio.database.model.modkegg.KeggInfo;
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
	
	//////////////////  Blast setting  ///////////////////////////////////////////
	@Override
	public void setBlastLsInfo(double evalue, int... StaxID) {
		copedID.setBlastLsInfo(evalue,StaxID);
	}
	
	@Override
	public BlastInfo setBlastInfo(int StaxID, double evalue) {
		return copedID.setBlastInfo(StaxID, evalue);
	}
	public ArrayList<BlastInfo> getLsBlastInfos()
	{
		return copedID.getLsBlastInfos();
	}
///////////////////////   获得blast  copedID  ///////////////////////////////////////////////////////////////////
	
	@Override
	public CopedID getCopedIDBlast(int StaxID, double evalue) {
		return copedID.getCopedIDBlast(StaxID, evalue);
	}
	@Override
	public ArrayList<CopedID> getCopedIDLsBlast() {
		return copedID.getCopedIDLsBlast();
	}
/////////////////////////  常规信息  //////////////////////////////////////////////////////////////
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
	public String getAccIDDBinfo(String dbInfo) {
		return copedID.getAccIDDBinfo(dbInfo);
	}

	@Override
	public String[] getAnno(boolean blast, int StaxID, double evalue) {
		return copedID.getAnno(blast, StaxID, evalue);
	}
/////////////////////////////////////////////////////////////////////








////////////////////   KEGG    /////////////////////////////////////////////////////////
	@Override
	public KeggInfo getKeggInfo() {
		return copedID.getKeggInfo();
	}
	@Override
	public ArrayList<KGpathway> getKegPathBlast() {
		return copedID.getKegPathBlast();
	}

	@Override
	public ArrayList<KGpathway> getKegPath() {
		return copedID.getKegPath();
	}

	@Override
	public ArrayList<KegEntity> getKegEntity(boolean blast, int StaxID,
			double evalue) {
		return copedID.getKegEntity(blast, StaxID, evalue);
	}

	//////////////  GO 方法  ///////////////////////
	@Override
	public ArrayList<AGene2Go> getGene2GO(String GOType) {
		return copedID.getGene2GO(GOType);
	}

	protected GOInfoAbs getGOInfo() {
		return copedID.getGOInfo();
	}

	@Override
	public ArrayList<AGene2Go> getGene2GOBlast(String GOType) {
		return copedID.getGene2GOBlast(GOType);
	}

	
	/**
	 * 获得该copedID的annotation信息
	 * @param copedID
	 * @param blast
	 * @return
	 * 	 * blast：<br>
	 * 			title2[0]="QueryID";title2[1]="QuerySymbol";title2[2]="Description";<br>
	 * title2[3]="Evalue";
	 * title2[4]="subjectSymbol";
			title2[5]="Description";<br>
			不blast：<br>
						title2[0]="QueryID";title2[1]="QuerySymbol";title2[2]="Description";<br>
	 */
	public String[] getAnnoInfo(boolean blast) {
		String[] tmpresult = null;
		if (blast)
			tmpresult = new String[6];
		else
			tmpresult = new String[3];
		tmpresult[0] = copedID.getAccID(); tmpresult[1] = copedID.getSymbo(); tmpresult[2] = copedID.getDescription();
		if (blast) {
			if (copedID.getCopedIDLsBlast() != null && copedID.getLsBlastInfos() != null && copedID.getLsBlastInfos().size() > 0) {
				for (int i = 0; i < copedID.getLsBlastInfos().size(); i++) {
					if (tmpresult[3] == null || tmpresult[3].trim().equals("")) {
						tmpresult[3] = copedID.getLsBlastInfos().get(i).getEvalue() + "";
						tmpresult[4] = copedID.getCopedIDLsBlast().get(i).getSymbo();
						tmpresult[5] = copedID.getCopedIDLsBlast().get(i).getDescription();
					}
					else {
						tmpresult[3] = tmpresult[3] + "//" + copedID.getLsBlastInfos().get(i).getEvalue();
						tmpresult[4] = tmpresult[4] + "//" + copedID.getCopedIDLsBlast().get(i).getSymbo();
						tmpresult[5] = tmpresult[5] + "//" + copedID.getCopedIDLsBlast().get(i).getDescription();
					}
				}
			}
		}
		return tmpresult;
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
	
////////   公有 GO 的处理   /////////////////////////////////////
	/**
	 * 将一系列CopedID中的GO整理成 genUniID goID,goID,goID.....的样式
	 * 内部根据genUniID去重复
	 * @param lsCopedIDs 一系列的copedID
	 * @param GOType GOInfoAbs中的信息
	 * @param blast 注意lsCopedID里面的copedID必须要先设定过setBlast才有用
	 * @reture 没有则返回null
	 */
	public static ArrayList<String[]> getLsGoInfo(ArrayList<CopedID> lsCopedIDs, String GOType, boolean blast) {
		if (lsCopedIDs == null || lsCopedIDs.size() == 0) {
			return null;
		}
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		HashSet<String> hashUniGenID = new HashSet<String>();
		for (CopedID copedID : lsCopedIDs) {
			///////    去重复     //////////////////////////////
			if (hashUniGenID.contains(copedID.getGenUniID())) {
				continue;
			}
			hashUniGenID.add(copedID.getGenUniID());
			////////////////////////////////////////////////////////////
			ArrayList<AGene2Go> lstmpgo;
			if (blast) {
				lstmpgo = copedID.getGene2GOBlast(GOType);
			}
			else {
				lstmpgo = copedID.getGene2GO(GOType);
			}
			if (lstmpgo == null || lstmpgo.size() == 0) {
				continue;
			}
			String[] strGene2Go = new String[2];
			strGene2Go[0] = copedID.getGenUniID();
			for (AGene2Go aGene2Go : lstmpgo) {
				if (strGene2Go[1].equals("")) {
					strGene2Go[1] = aGene2Go.getGOID();
				}
				else {
					strGene2Go[1] = strGene2Go[1] + ","+aGene2Go.getGOID();
				}
			}
			lsResult.add(strGene2Go);
		}
		return lsResult;
	}
	
////////公有 KEGG 的处理   /////////////////////////////////////
	/**
	 * 将一系列CopedID中的KEGG整理成 genUniID PathID,pathID,pathID.....的样式
	 * 内部根据genUniID去重复
	 * @param lsCopedIDs 一系列的copedID
	 * @param GoType GOInfoAbs中的信息
	 * @param blast 注意lsCopedID里面的copedID必须要先设定过setBlast才有用
	 * @reture 没有则返回null
	 */
	public static ArrayList<String[]> getLsPathInfo(ArrayList<CopedID> lsCopedIDs, boolean blast) {
		if (lsCopedIDs == null || lsCopedIDs.size() == 0) {
			return null;
		}
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		HashSet<String> hashUniGenID = new HashSet<String>();
		for (CopedID copedID : lsCopedIDs) {
			///////    去重复     //////////////////////////////
			if (hashUniGenID.contains(copedID.getGenUniID())) {
				continue;
			}
			hashUniGenID.add(copedID.getGenUniID());
			////////////////////////////////////////////////////////////
			
			ArrayList<KGpathway> lstmpgo;
			if (blast) {
				lstmpgo = copedID.getKegPathBlast();
			}
			else {
				lstmpgo = copedID.getKegPath();
			}
			if (lstmpgo == null || lstmpgo.size() == 0) {
				continue;
			}
			String[] strGene2Path = new String[2];
			strGene2Path[0] = copedID.getGenUniID();
			for (KGpathway kGpathway : lstmpgo) {
				if (strGene2Path[1].equals("")) {
					strGene2Path[1] = kGpathway.getPathName();
				}
				else {
					strGene2Path[1] = strGene2Path[1] + ","+kGpathway.getPathName();
				}
			}
			lsResult.add(strGene2Path);
		}
		return lsResult;
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
		return CopedIDAbs.getNCBIUniTax(accID, taxID);
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
		ArrayList<TaxInfo> lsTaxID = MapFSTaxID.queryLsTaxInfo(taxInfo);
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
