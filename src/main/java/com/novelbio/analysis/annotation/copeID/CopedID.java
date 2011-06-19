package com.novelbio.analysis.annotation.copeID;


import java.util.ArrayList;

import com.novelbio.analysis.annotation.pathway.kegg.pathEntity.KegEntity;
import com.novelbio.analysis.annotation.pathway.kegg.pathEntity.KegGenEntryKO;
import com.novelbio.database.entity.friceDB.BlastInfo;
import com.novelbio.database.entity.friceDB.NCBIID;
import com.novelbio.database.entity.friceDB.UniProtID;
import com.novelbio.database.entity.kegg.KGentry;
import com.novelbio.database.service.ServAnno;
import com.novelbio.database.service.ServBlastInfo;

/**
 * 专门对基因的ID做一些处理的类<br>
 * <b>如果基因的IDtype是accID，那么该基因很可能不存在，那么看下blast的相关信息，如果blast也没有，那么就不存在了</b><br>
 * 可以将输入的ID合并起来，并且将分散的ID存储在一个Hashmap中
 * 当genUniID存在时，类是否想等只比较genUniID、taxID，idType是否相等
 * 当genUniID不存在，accID存在时，比较accID、taxID，idType是否相等
 * 当都不存在时，认为相同
 * HashCode的设定和这个类似
 * @author zong0jie
 *
 */
public class CopedID {
	public static String IDTYPE_ACCID = "accID"; 
	public static String IDTYPE_GENEID = "geneID"; 
	public static String IDTYPE_UNIID = "uniID"; 

	/**
	 * 物种id
	 */
	int taxID = 0;
	/**
	 * idType，必须是IDTYPE中的一种
	 */
	String idType = IDTYPE_ACCID;
	
	/**
	 * 具体的accID
	 */
	String accID = "";

	String genUniID = "";
	
	String description = null;
	
	String symbol = null;
	
	BlastInfo blastInfo = null;
	
	KegGenEntryKO kegGenEntryKO = null;
	/**
	 * 外部不要通过该方法new
	 */
	protected CopedID() {
	}
	/**
	 * 设定初始值，会自动去数据库查找accID并，完成填充本类。
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
		
		ArrayList<String> lsaccID = ServAnno.getNCBIUniTax(accID, taxID);
		String idType = lsaccID.get(0); taxID = Integer.parseInt(lsaccID.get(1));
		String tmpGenID = lsaccID.get(2);
		setInfo(accID, idType, taxID, tmpGenID);
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
	public CopedID(String idType, String genUniID, int taxID) {
		this.accID = "";
		this.genUniID = genUniID;
		this.idType = idType;
		this.taxID = taxID;
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
		
		ArrayList<String> lsaccID = ServAnno.getNCBIUniTax(accID, taxID);
		String idType = lsaccID.get(0); taxID = Integer.parseInt(lsaccID.get(1));
		 for (int i = 2 ; i < lsaccID.size(); i++) {
			 CopedID copedID = new CopedID();
			 String tmpGenID = lsaccID.get(i);
			 copedID.setInfo(accID, idType, taxID, tmpGenID);
			 lsCopedIDs.add(copedID);
		 }
		 return lsCopedIDs;
	}
	
	/**
	 * 获得本copedID blast到对应物种的blastInfo信息，没有就返回null
	 * @param StaxID
	 * @param evalue
	 * @return
	 */
	public BlastInfo getBlastInfo(int StaxID,double evalue) {
		if (blastInfo == null) {
			String[] genInfo = new String[3];
			genInfo[0] = getIDtype();
			genInfo[1] = getAccID();
			genInfo[2] = getGenUniID();
			blastInfo = ServBlastInfo.getBlastInfo(genInfo, getTaxID(), StaxID, evalue);
		}
		return blastInfo;
	}
	/**
	 * 获得本copedID blast到对应物种的copedID，没有就返回null
	 * @param StaxID
	 * @param evalue
	 * @return
	 */
	public CopedID getBlastCopedID(int StaxID,double evalue) {
		BlastInfo blastInfo = getBlastInfo(StaxID, evalue);
		if (blastInfo == null) {
			return null;
		}
		String idType = "";
		if (blastInfo.getSubjectTab().equals(BlastInfo.SUBJECT_TAB_NCBIID)) {
			idType = CopedID.IDTYPE_GENEID;
		}
		else if (blastInfo.getSubjectTab().equals(BlastInfo.SUBJECT_TAB_UNIPROTID)) {
			idType = CopedID.IDTYPE_UNIID;
		}
		CopedID copedID = new CopedID(idType,blastInfo.getSubjectID(), StaxID);
		return copedID;
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
		CopedID copedID = new CopedID();
		copedID.accID = "";
		if (idType.equals(IDTYPE_ACCID)) {
			return null;
		}
		else if (idType.equals(IDTYPE_GENEID)) {
			NCBIID ncbiid = ServAnno.getNCBIID(Integer.parseInt(genUniID), taxID);
			if (ncbiid != null) {
				copedID.genUniID = ncbiid.getGeneId() + "";
				copedID.idType = IDTYPE_GENEID;
				copedID.taxID = ncbiid.getTaxID();
				return copedID;
			}
		}
		else if (idType.equals(IDTYPE_UNIID)) {
			UniProtID uniProtID = ServAnno.getUniProtID(genUniID, taxID);
			if (uniProtID != null) {
				copedID.genUniID = uniProtID.getUniID();
				copedID.idType = IDTYPE_UNIID;
				copedID.taxID = uniProtID.getTaxID();
				return copedID;
			}
		}
		return null;
	}
	/**
	 * 设定各种参数的，不需要外部设定
	 * @param accID
	 * @param idType
	 * @param taxID
	 * @param genUniID
	 */
	protected void setInfo(String accID,String idType, int taxID, String genUniID) {
		this.accID = accID;
		this.taxID = taxID;
		this.genUniID = genUniID;
		this.idType = idType;
	}
	
	/**
	 * idType，必须是IDTYPE中的一种
	 */
	public String getIDtype() {
		return this.idType;
	}
	
	/**
	 * 具体的accID
	 */
	public String getAccID() {
		return this.accID;
	}


	/**
	 * 获得geneID
	 * @return
	 */
	public String getGenUniID()
	{
		return this.genUniID;
	}
	
	public int getTaxID() {
		return taxID;
	}
	
	/**
	 * 获得该基因的description
	 * @return
	 */
	public String getDescription() {
		setSymbDesp();
		return description;
	}
	/**
	 * 获得该基因的symbol
	 * @return
	 */
	public String getSymbo() {
		setSymbDesp();
		return symbol;
	}
	
	private void setSymbDesp() {
		if (description != null && symbol != null) {
			return;
		}
		if (idType.equals(IDTYPE_ACCID)) {
			description = "";
			symbol = "";
		}
		else if(idType.equals(IDTYPE_GENEID)){
			String[]  symbDesp = ServAnno.getGenInfo(Integer.parseInt(getGenUniID()));
			description = symbDesp[1];
			symbol = symbDesp[0];
		}
		else if (idType.equals(IDTYPE_UNIID)) {
			String[]  symbDesp = ServAnno.getUniGenInfo(getGenUniID());
			description = symbDesp[1];
			symbol = symbDesp[0];
		}
	}
	/**
	 * 获得该CopeID的List-KGentry,如果没有或为空，则返回null
	 * @param blast 是否blast到相应物种查看
	 * @param StaxID 如果blast为true，那么设定StaxID
	 * @return 如果没有就返回null
	 */
	public ArrayList<KegEntity> getKegEntity(boolean blast,int StaxID,double evalue) {
		if (!blast) {
			return getKegGenEntryKO().getLsKGentries();
		}
		else {
			//如果本基因能找到keggID就不进行blast
			if ( getKegGenEntryKO().getLsKGentries() != null) {
				return getKegGenEntryKO().getLsKGentries();
			}
			CopedID ScopedID = getBlastCopedID(StaxID,evalue);
			return ScopedID.getKegEntity(false, 0, 0);
		}
	}
	
	private KegGenEntryKO getKegGenEntryKO()
	{
		if (kegGenEntryKO == null) {
			kegGenEntryKO = new KegGenEntryKO(idType, genUniID, taxID);
		}
		return kegGenEntryKO;
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
				(!genUniID.trim().equals("")
				&& !otherObj.getGenUniID().trim().equals("") 
				&& genUniID.trim().equals(otherObj.getGenUniID().trim())	
				&& idType.equals(otherObj.getIDtype())
				&& taxID == otherObj.getTaxID()
				)
				||//geneID都为""，那么如果两个accID相同且不为""，也可认为两个基因相同
				(genUniID.trim().equals("")
				&& otherObj.getGenUniID().trim().equals("") 
				&& !accID.equals("") && !otherObj.getAccID().equals("")
				&& accID.equals(otherObj.getAccID())
				&& idType.equals(otherObj.getIDtype())
				&& taxID == otherObj.getTaxID()
				)
				||
				(genUniID.trim().equals("")
				&& otherObj.getGenUniID().trim().equals("") 
				&& accID.equals("") 
				&& otherObj.getAccID().equals("")						
				)
		)
		{
			return true;
		}
		return false;
	}

	/**
	 * 如果blast * 0:symbol 1:description 2:subjectTaxID 3:evalue 4:symbol 5:description 如果不blast 0:symbol 1:description
	 * @return
	 */
	public String[] getAnno( boolean blast, int StaxID, double evalue) {
		String[] tmpAnno = null;
		if (blast) {
			tmpAnno = new String[6];
			for (int i = 0; i < tmpAnno.length; i++) {
				tmpAnno[i] = "";
			}
			
			tmpAnno[0] = getSymbo(); tmpAnno[1] = getDescription(); tmpAnno[2] = StaxID + ""; 
			CopedID copedIDBlast = getBlastCopedID(StaxID, evalue);
			if (copedIDBlast != null) {
				tmpAnno[3] = evalue + "";
				tmpAnno[4] = copedIDBlast.getSymbo();
				tmpAnno[5] = copedIDBlast.getDescription();
			}
		}
		else {
			tmpAnno = new String[2];
			for (int i = 0; i < tmpAnno.length; i++) {
				tmpAnno[i] = "";
			}
			tmpAnno[0] = getSymbo(); tmpAnno[1] = getDescription(); 
		}
		
		
		
		return tmpAnno;
	}
	
	
	
	
	/**
	 * 重写hashcode
	 */
	public int hashCode(){
		String hash = "";
		if (!genUniID.trim().equals("")) {
			hash = genUniID.trim()+"sep_@@_genUni_"+idType.trim()+"@@"+taxID;
		}
		else if (genUniID.trim().equals("") && !accID.trim().equals("")) {
			hash = accID.trim()+"@@accID"+idType.trim()+"@@"+taxID;
		}
		else if ( genUniID.trim().equals("") && accID.trim().equals("")) {
			hash = "";
		}
		return hash.hashCode();
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
	
}
