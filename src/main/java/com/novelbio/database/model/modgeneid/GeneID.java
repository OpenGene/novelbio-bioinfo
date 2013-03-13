package com.novelbio.database.model.modgeneid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.DBAccIDSource;
import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.AGeneInfo;
import com.novelbio.database.domain.geneanno.AgeneUniID;
import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.domain.geneanno.GOtype;
import com.novelbio.database.domain.geneanno.NCBIID;
import com.novelbio.database.domain.geneanno.SepSign;
import com.novelbio.database.domain.geneanno.UniProtID;
import com.novelbio.database.domain.kegg.KGentry;
import com.novelbio.database.domain.kegg.KGpathway;
import com.novelbio.database.model.modgo.GOInfoAbs;
import com.novelbio.database.model.modkegg.KeggInfo;

/**
 * <b>注意blastInfo中的SubjectTab和QueryTab有问题，需要重写</b><br>
 * 
 * 这只是一个代理类 专门对基因的ID做一些处理的类<br>
 * <b>如果基因的IDtype是accID，那么该基因很可能不存在，那么看下blast的相关信息，如果blast也没有，那么就不存在了</b><br>
 * 可以将输入的ID合并起来，并且将分散的ID存储在一个Hashmap中
 * 当genUniID存在时，类是否想等只比较genUniID、taxID，idType是否相等
 * 当genUniID不存在，accID存在时，比较accID、taxID，idType是否相等 当都不存在时，认为相同 HashCode的设定和这个类似
 * 
 * @author zong0jie
 */
public class GeneID implements GeneIDInt{
	public final static String IDTYPE_ACCID = "accID"; 
	public final static String IDTYPE_GENEID = "NCBIID";
	public final static String IDTYPE_UNIID = "UniprotID";
	
	private GeneIDInt geneID;
	
	GeneIDfactoryInt geneIDfactoryInt = new GeneIDfactory();
	
	public void setGeneIDfactoryInt(GeneIDfactoryInt geneIDfactoryInt) {
		this.geneIDfactoryInt = geneIDfactoryInt;
	}
	
	/**
	 * 设定初始值，不验证 如果在数据库中没有找到相应的geneUniID，则返回null 只能产生一个CopedID，此时accID = ""
	 * @param idType  必须是IDTYPE中的一种
	 * @param genUniID
	 * @param taxID 物种ID
	 */
	public GeneID(String idType, String genUniID, int taxID) {
		geneID = geneIDfactoryInt.createGeneID(idType, genUniID, taxID);
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
	public GeneID(String accID,String idType, String genUniID, int taxID) {
		geneID = geneIDfactoryInt.createGeneID(accID, idType, genUniID, taxID);
	}
	
	
	/**
	 * 设定初始值，会自动去数据库查找accID并，完成填充本类。
	 * <b>如果基因的IDtype是accID，那么该基因很可能不存在，那么看下blast的相关信息，如果blast也没有，那么就不存在了</b>
	 * 不过只能产生一个CopedID，如果觉得一个accID要产生多个geneID，那么可以选择getLsCopedID方法
	 * @param accID 除去引号，然后如果类似XM_002121.1类型，那么将.1去除
	 * @param taxID
	 */
	public GeneID(String accID,int taxID) {
		this(accID,taxID, false);
	}
	/**
	 * 设定初始值，会自动去数据库查找accID并，完成填充本类。
	 * <b>如果基因的IDtype是accID，那么该基因很可能不存在，那么看下blast的相关信息，如果blast也没有，那么就不存在了</b>
	 * 不过只能产生一个CopedID，如果觉得一个accID要产生多个geneID，那么可以选择getLsCopedID方法
	 * @param accID 除去引号，然后如果类似XM_002121.1类型，那么将.1去除
	 * @param taxID
	 * @param blastType 具体的accID是否类似 blast的结果，如：dbj|AK240418.1|，那么获得AK240418，一般都是false
	 */
	public GeneID(String accID,int taxID,boolean blastType) {
		if (blastType) {
			accID = getBlastAccID(accID);
		} else {
			accID = removeDot(accID);
		}
		ArrayList<AgeneUniID> lsaccID = GeneIDabs.getNCBIUniTax(accID, taxID);
		if (lsaccID.size() == 0) {
			geneID = geneIDfactoryInt.createGeneID(accID, IDTYPE_ACCID, accID, taxID);
			return;
		}
		AgeneUniID geneUniID = lsaccID.get(0);
		geneID = geneIDfactoryInt.createGeneID(accID, geneUniID.getGeneIDtype(), geneUniID.getGenUniID(), taxID);
	}
	/**
	 * 设定初始值，会自动去数据库查找accID并完成填充本类。
	 * @param accID 如果类似XM_002121.1类型，那么将.1去除
	 * @param taxID
	 */
	public static ArrayList<GeneID> createLsCopedID(String accID,int taxID) {
		return createLsCopedID(accID, taxID, false);
	}
	/**
	 * 设定初始值，会自动去数据库查找accID并完成填充本类。
	 * @param accID 如果类似XM_002121.1类型，那么将.1去除
	 * @param taxID
	 * @param blastType 具体的accID是否类似 blast的结果，如：dbj|AK240418.1|，那么获得AK240418，一般都是false
	 */
	public static ArrayList<GeneID> createLsCopedID(String accID,int taxID,boolean blastType) {
		ArrayList<GeneID> lsCopedIDs = new ArrayList<GeneID>();
		if (blastType) 
			accID = accID.split("\\|")[1];
		accID = removeDot(accID);
		
		ArrayList<AgeneUniID> lsaccID = GeneIDabs.getNCBIUniTax(accID, taxID);
		for (AgeneUniID ageneUniID : lsaccID) {
			 GeneID copedID = new GeneID(accID, ageneUniID.getGeneIDtype(), ageneUniID.getGenUniID(), taxID);
			 lsCopedIDs.add(copedID);
		}
		return lsCopedIDs;
	}

	//////////////////  Blast setting  ///////////////////////////////////////////
	@Override
	public void setBlastInfo(double evalue, int... StaxID) {
		geneID.setBlastInfo(evalue,StaxID);
	}
	public ArrayList<BlastInfo> getLsBlastInfos() {
		return geneID.getLsBlastInfos();
	}
///////////////////////   获得blast  copedID  ///////////////////////////////////////////////////////////////////
	@Override
	public ArrayList<GeneID> getLsBlastGeneID() {
		return geneID.getLsBlastGeneID();
	}
	@Override
	public GeneID getGeneIDBlast() {
		return geneID.getGeneIDBlast();
	}
/////////////////////////  常规信息  //////////////////////////////////////////////////////////////
	@Override
	public String getIDtype() {
		return geneID.getIDtype();
	}
	@Override
	public String getAccID() {
		return geneID.getAccID();
	}
	@Override
	public String getAccID_With_DefaultDB() {
		return geneID.getAccID_With_DefaultDB();
	}
	@Override
	public String getGenUniID() {
		if (getIDtype().equals(GeneID.IDTYPE_ACCID)) {
			return "-1";
		}
		return geneID.getGenUniID();
	}
	@Override
	public int getTaxID() {
		return geneID.getTaxID();
	}
	@Override
	public String getDescription() {
		return geneID.getDescription();
	}
	@Override
	public String getSymbol() {
		return geneID.getSymbol();
	}
	@Override
	public String getAccIDDBinfo(DBAccIDSource dbInfo) {
		return geneID.getAccIDDBinfo(dbInfo);
	}
	/**
	 * 返回geneinfo信息
	 * @return
	 */
	@Override
	public AGeneInfo getGeneInfo() {
		return geneID.getGeneInfo();
	}
	/**
	 * 获得该copedID的annotation信息
	 * @param geneID
	 * @param blast
	 * @return
	 * 	 * blast：<br>
	 * 			blast * 0:symbol 1:description 2:subjectSpecies 3:evalue 4:symbol 5:description <br>
			不blast：<br>
						0:symbol 1:description<br>
	 */
	@Override
	public String[] getAnno(boolean blast) {
		return geneID.getAnno(blast);
	}
	public static String[] getTitleAnno(boolean blast) {
		String[] titleAnno = null;
		if (blast) {
			titleAnno = new String[6];
		}
		else {
			titleAnno = new String[2];
		}
		titleAnno[0] = "Symbol";
		titleAnno[1] = "Description";
		if (blast) {
			titleAnno[2] = "BLast_Species";
			titleAnno[3] = "Evalue";
			titleAnno[4] = "Blast_Symbol";
			titleAnno[5] = "Blast_Description";
		}
		return titleAnno;
	}
/////////////////////////////////////////////////////////////////////

////////////////////   KEGG    /////////////////////////////////////////////////////////
	@Override
	public KeggInfo getKeggInfo() {
		return geneID.getKeggInfo();
	}
	@Override
	public ArrayList<KGpathway> getKegPath(boolean blast) {
		return geneID.getKegPath(blast);
	}
	@Override
	public ArrayList<KGentry> getKegEntity(boolean blast) {
		return geneID.getKegEntity(blast);
	}
	//////////////  GO 方法  ///////////////////////
	@Override
	public ArrayList<AGene2Go> getGene2GO(GOtype GOType) {
		return geneID.getGene2GO(GOType);
 	}
	public GOInfoAbs getGOInfo() {
		return geneID.getGOInfo();
	}
	@Override
	public ArrayList<AGene2Go> getGene2GOBlast(GOtype GOType) {
		return geneID.getGene2GOBlast(GOType);
	}
	/////////////////////////////  static 方法  ////////////////////////////////////
	/**
	 * blast的结果可能类似dbj|AK240418.1|
	 * 将里面的AK240418抓出来并返回
	 * @return
	 */
	public static String getBlastAccID(String blastGenID) {
		if (blastGenID == null) {
			return null;
		}
		String[] ss = blastGenID.split("\\|");
		return removeDot(ss[1]);
	}
	/**
	 *  首先除去空格，如果为""或“-”
	 *  则返回null
	 * 如果类似XM_002121.1类型，那么将.1去除
	 * @param accID
	 * @return accID without .1
	 */
	public static String removeDot(String accID) {
		if (accID == null) {
			return null;
		}
		String tmpGeneID = accID.replace("\"", "").trim();
		if (tmpGeneID.equals("") || accID.equals("-")) {
			return null;
		}
		int dotIndex = tmpGeneID.lastIndexOf(".");
		//如果类似XM_002121.1类型
		if (dotIndex>0 && tmpGeneID.length() - dotIndex <= 3) {
			String subIndex = tmpGeneID.substring(dotIndex + 1, tmpGeneID.length());
			try {
				int num = Integer.parseInt(subIndex);
				tmpGeneID = tmpGeneID.substring(0,dotIndex);
			} catch (Exception e) { }
		}
		return tmpGeneID;
	}
	/**
	 * @param collectionAccID 常规的accIDlist，可以先对accID去一次重复
	 * @param taxID
	 * @param combineID
	 * @return
	 */
	public static ArrayList<GeneID> getLsGeneID(Collection<String> collectionAccID, int taxID, boolean combineID) {
		ArrayList<GeneID> lsGeneID = new ArrayList<GeneID>();
		for (String string : collectionAccID) {
			GeneID copedID = new GeneID(string, taxID, false);
			lsGeneID.add(copedID);
		}
		if (!combineID) {
			return lsGeneID;
		}
		HashSet<GeneID> setUniqueGeneID = ArrayOperate.removeDuplicate(lsGeneID);
		lsGeneID.clear();
		for (GeneID geneID : setUniqueGeneID) {
			lsGeneID.add(geneID);
		}
		return lsGeneID;
	}
	
	/**
	 * 返回一个geneID对应多个accID的表
	 * @param collectionAccID 常规的accIDlist，可以先对accID去一次重复
	 * @param taxID
	 * @return
	 * HashMap-geneID, ArrayList-String
	 * key geneID
	 * value 相同copedID对应的不同accID的list
	 */
	public static HashMap<GeneID, ArrayList<String>> getMapGeneID2LsAccID(Collection<String> collectionAccID, int taxID) {
		HashMap<GeneID, ArrayList<String>> hashResult = new HashMap<GeneID, ArrayList<String>>();
		for (String string : collectionAccID) {
			GeneID copedID = new GeneID(string, taxID, false);
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
	 * @param lsGeneID 一系列的copedID
	 * @param GOType GOInfoAbs中的信息
	 * @param blast 注意lsCopedID里面的copedID必须要先设定过setBlast才有用
	 * @reture 没有则返回null
	 */
	public static ArrayList<String[]> getLsGoInfo(ArrayList<GeneID> lsGeneID, GOtype GOType, boolean blast) {
		if (validateListIsEmpty(lsGeneID)) return null;
		
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		HashSet<GeneID> setUniqueGeneID = ArrayOperate.removeDuplicate(lsGeneID);
		for (GeneID geneID : setUniqueGeneID) {
			////////////////////////////////////////////////////////////
			ArrayList<AGene2Go> lstmpgo;
			if (blast) {
				lstmpgo = geneID.getGene2GOBlast(GOType);
			}
			else {
				lstmpgo = geneID.getGene2GO(GOType);
			}
			if (validateListIsEmpty(lstmpgo)) continue;
			
			String[] strGene2Go = new String[2];
			strGene2Go[0] = geneID.getGenUniID();
			
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
	 * @param lsGeneID 一系列的copedID
	 * @param GoType GOInfoAbs中的信息
	 * @param blast 注意lsCopedID里面的copedID必须要先设定过setBlast才有用
	 * @reture 没有则返回null
	 */
	public static ArrayList<String[]> getLsPathInfo(ArrayList<GeneID> lsGeneID, boolean blast) {
		if (validateListIsEmpty(lsGeneID)) return null;
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		LinkedHashSet<GeneID> hashGenID = ArrayOperate.removeDuplicate(lsGeneID);
		for (GeneID geneID : hashGenID) {
			ArrayList<KGpathway> lstmpgo = geneID.getKegPath(blast);
			if (validateListIsEmpty(lstmpgo))  continue;
			
			String[] strGene2Path = new String[2];
			strGene2Path[0] = geneID.getGenUniID();
			strGene2Path[1] = lstmpgo.get(0).getPathName();
			for (int i = 1; i < lstmpgo.size(); i++) {
				strGene2Path[1] = strGene2Path[1] + ","+lstmpgo.get(i).getPathName();
			}
			lsResult.add(strGene2Path);
		}
		return lsResult;
	}

/////////////////////// 私有 static 方法 /////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/** 查看该list是否有内容 */
	private static boolean validateListIsEmpty(Collection col) {
		if (col == null || col.size() == 0)
			return true;
		return false;
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
		GeneID otherObj = (GeneID)obj;
		if (
				//geneID相同且都不为“”，可以认为两个基因相同
				(!getIDtype().equals(GeneID.IDTYPE_ACCID) && !otherObj.getIDtype().equals(GeneID.IDTYPE_ACCID)
				&& getGenUniID().trim().equals(otherObj.getGenUniID().trim())	
				&& getIDtype().equals(otherObj.getIDtype())
				&& getTaxID() == otherObj.getTaxID()
				)
				||//geneID都为""，那么如果两个accID相同且不为""，也可认为两个基因相同
				(getIDtype().equals(GeneID.IDTYPE_ACCID) && otherObj.getIDtype().equals(GeneID.IDTYPE_ACCID)
				&&
				   ( !getAccID().equals("") && !otherObj.getAccID().equals("")
				      && getAccID().equals(otherObj.getAccID())
				      && getTaxID() == otherObj.getTaxID() )
				      ||
				      (getAccID().equals("") && otherObj.getAccID().equals(""))
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
		if (!getIDtype().equals(GeneID.IDTYPE_ACCID)) {
			hash = geneID.getGenUniID().trim() + SepSign.SEP_ID + getIDtype() + SepSign.SEP_INFO_SAMEDB + getTaxID();
		}
		else if ( getIDtype().equals(GeneID.IDTYPE_ACCID) && !getAccID().trim().equals("")) {
			hash = getAccID().trim() + "@@accID" + getIDtype() + "@@" + getTaxID();
		}
		else if ( getGenUniID().trim().equals("") && getAccID().trim().equals("")) {
			hash = "";
		}
		return hash.hashCode();
	}

	@Override
	public void setUpdateGeneID(String geneUniID, String idType) {
		geneID.setUpdateGeneID(geneUniID, idType);
	}

	@Override
	public void addUpdateGO(String GOID, DBAccIDSource GOdatabase, String GOevidence,
			String GORef, String gOQualifiy) {
		geneID.addUpdateGO(GOID, GOdatabase, GOevidence, GORef, gOQualifiy);
	}

	@Override
	public void setUpdateGeneInfo(AGeneInfo geneInfo) {
		geneID.setUpdateGeneInfo(geneInfo);
	}

	@Override
	public boolean update(boolean updateUniID) {
		return geneID.update(updateUniID);
	}

	@Override
	public void setUpdateDBinfo(DBAccIDSource DBInfo, boolean overlapDBinfo) {
		geneID.setUpdateDBinfo(DBInfo, overlapDBinfo);
	}
	public void setUpdateDBinfo(String DBInfo, boolean overlapDBinfo) {
		geneID.setUpdateDBinfo(DBInfo, overlapDBinfo);
	}
	@Override
	public void setUpdateRefAccID(String... refAccID) {
		geneID.setUpdateRefAccID(refAccID);
	}
	@Override
	public void addUpdateRefAccID(String... refAccID) {
		geneID.addUpdateRefAccID(refAccID);
	}
	@Override
	public void setUpdateRefAccID(ArrayList<String> lsRefAccID) {
		geneID.setUpdateRefAccID(lsRefAccID);
	}
	@Override
	public void setUpdateBlastInfo(String SubAccID, String subDBInfo,
			int SubTaxID, double evalue, double identities) {
		geneID.setUpdateBlastInfo(SubAccID, subDBInfo, SubTaxID, evalue, identities);
	}

	@Override
	public void setUpdateAccID(String accID) {
		geneID.setUpdateAccID(accID);
	}

	@Override
	public void setUpdateBlastInfo(String SubGenUniID, String subIDtype,
			String subDBInfo, int SubTaxID, double evalue, double identities) {
		geneID.setUpdateBlastInfo(SubGenUniID, subIDtype, subDBInfo, SubTaxID, evalue, identities);
	}

	@Override
	public void setUpdateAccIDNoCoped(String accID) {
		geneID.setUpdateAccIDNoCoped(accID);
	}

	@Override
	public String getDBinfo() {
		return geneID.getDBinfo();
	}
	
}
