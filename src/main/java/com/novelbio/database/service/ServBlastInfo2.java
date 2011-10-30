package com.novelbio.database.service;

import java.util.ArrayList;

import com.novelbio.database.entity.friceDB.Blast2GeneInfo;
import com.novelbio.database.entity.friceDB.BlastInfo;
import com.novelbio.database.entity.friceDB.Gene2GoInfo;
import com.novelbio.database.entity.friceDB.NCBIID;
import com.novelbio.database.entity.friceDB.Uni2GoInfo;
import com.novelbio.database.entity.friceDB.UniProtID;
import com.novelbio.database.mapper.geneanno.MapBlastInfo;

public class ServBlastInfo2 {
	/**
	 * @param genInfo
	 * 0: ID类型："geneID"或"uniID"或"accID"<br>
	 * 1: accID<br>
	 * 2: 具体转换的ID<br>
	 * @param taxID
	 * @param sep
	 * @return
	 * 返回blast的信息，如果没有就返回null
	 */
	public static BlastInfo getBlastInfo(String[] genInfo,int QtaxID,int StaxID,double evalue)
	{
		BlastInfo blastInfo = null;
		if (genInfo[0].equals("geneID")) {
			NCBIID ncbiid = new NCBIID();
			 ncbiid.setGeneId(Long.parseLong(genInfo[2])); ncbiid.setTaxID(QtaxID);
			 blastInfo = getBlastInfo(ncbiid, evalue, StaxID);
		}
		else if (genInfo[0].equals("uniID")) {
			UniProtID uniProtID = new UniProtID();
			uniProtID.setUniID(genInfo[2]); uniProtID.setTaxID(QtaxID);
			blastInfo = getBlastInfo(uniProtID, evalue, StaxID);
		}
		else
		{
			String accID = genInfo[1];
			blastInfo = getBlastInfo(accID, evalue, StaxID);
		}
		return blastInfo;
	}
	
	/**
	 * @param ncbiid
	 * 注意ncbiid中必须含有geneID，因为是用这个去查找blast信息的
	 * @param evalue
	 * @param StaxID 需要比较到的物种ID，如果为0，则不考虑物种ID
	 * @return
	 */
	private static BlastInfo getBlastInfo(NCBIID ncbiid,double evalue,int StaxID) {
		//开始查询
		BlastInfo qblastInfo = new BlastInfo();
		qblastInfo.setEvalue(evalue);qblastInfo.setQueryID(ncbiid.getGeneId()+"");
		qblastInfo.setSubjectTax(StaxID);
		ArrayList<BlastInfo> lsBlastInfos = MapBlastInfo.queryLsBlastInfo(qblastInfo);
		if (lsBlastInfos != null && lsBlastInfos.size() > 0 && lsBlastInfos.get(0).getEvalue() <= evalue) 
		{
			return lsBlastInfos.get(0);
		}
		return null;
	}
	/**
	 * @param uniProtID
	 * 注意uniProtID中必须含有uniID，因为是用这个去查找blast信息的
	 * @param evalue
	 * @param StaxID 需要比较到的物种ID，如果为0，则不考虑物种ID
	 * @return
	 */
	private static BlastInfo getBlastInfo(UniProtID uniProtID,double evalue,int StaxID) {
		//开始查询
		BlastInfo qblastInfo = new BlastInfo();
		qblastInfo.setEvalue(evalue);qblastInfo.setQueryID(uniProtID.getUniID());
		qblastInfo.setSubjectTax(StaxID);
		ArrayList<BlastInfo> lsBlastInfos = MapBlastInfo.queryLsBlastInfo(qblastInfo);
		if (lsBlastInfos != null && lsBlastInfos.size() > 0 && lsBlastInfos.get(0).getEvalue() <= evalue) 
		{
			return lsBlastInfos.get(0);
		}
		return null;
	}
	
	/**
	 * 
	 * @param accID 直接用accID去做blast
	 * @param evalue
	 * @param StaxID 需要比较到的物种ID，如果为0，则不考虑物种ID
	 * @return
	 */
	private static BlastInfo getBlastInfo(String accID,double evalue,int StaxID) {
		//开始查询
		BlastInfo qblastInfo = new BlastInfo();
		qblastInfo.setEvalue(evalue);qblastInfo.setQueryID(accID);
		qblastInfo.setSubjectTax(StaxID);
		ArrayList<BlastInfo> lsBlastInfos = MapBlastInfo.queryLsBlastInfo(qblastInfo);
		if (lsBlastInfos != null && lsBlastInfos.size() > 0 && lsBlastInfos.get(0).getEvalue() <= evalue) 
		{
			return lsBlastInfos.get(0);
		}
		return null;
	}
	/**
	 * 
	 * 
	 * @param accID
	 * 	 * 0: ID类型："geneID"或"uniID"或"accID"<br>
	 * 1: accID<br>
	 * 2: 具体转换的ID<br>
	 * @param QtaxID
	 * @param blast 是否需要blast，如果不进行blast，那么blastInfo和SubjectGene2GoInfo和SubjectUni2GoInfo都为空
	 * @param StaxID
	 * @param evalue
	 * @return
	 */
	public static Blast2GeneInfo getBlastGen2Go(String[] accID, int QtaxID,boolean blast, int StaxID,double evalue) 
	{
		Blast2GeneInfo blast2GeneInfo = new Blast2GeneInfo();
		Gene2GoInfo Qgene2GoInfo =null;
		Uni2GoInfo Quni2GoInfo = null;
		
		if (accID[0].equals("geneID")) {
			Qgene2GoInfo = ServGo.getGen2GoInfo(accID, QtaxID);
		}
		else if(accID[0].equals("uniID"))
		{
			Quni2GoInfo = ServGo.getUni2GenGoInfo(accID, QtaxID);
		}
		else if(accID[0].equals("accID")) {
			Qgene2GoInfo = new Gene2GoInfo();
			Qgene2GoInfo.setQuaryID(accID[2]);
		}
		blast2GeneInfo.setQueryGene2GoInfo(Qgene2GoInfo);
		blast2GeneInfo.setQueryUni2GoInfo(Quni2GoInfo);
		
		if (!blast) {
			return blast2GeneInfo;
		}
		
		///////////////搜索blast信息/////////////////////////////////
		BlastInfo blastInfo = getBlastInfo(accID, QtaxID, StaxID, evalue);
		blast2GeneInfo.setBlastInfo(blastInfo);
		////////////////搜到blast信息后，再回去找geneInfo/////////////////////////////////////////
		if (blastInfo != null && blastInfo.getEvalue()<=evalue)
		{
			String tab = blastInfo.getSubjectTab();
			String[] subGenInfo = new String[3];
			subGenInfo[2] = blastInfo.getSubjectID();
			if (tab.equals("NCBIID")) 
			{
				subGenInfo[1] = ServAnno.getGenName(Long.parseLong(subGenInfo[2]));
				subGenInfo[0] = "geneID";
				blast2GeneInfo.setSubjectGene2GoInfo(ServGo.getGen2GoInfo(subGenInfo, StaxID));
			}
			else 
			{
				subGenInfo[1] = ServAnno.getUniGenName(subGenInfo[2]);
				subGenInfo[0] = "uniID";
				blast2GeneInfo.setSubjectUni2GoInfo(ServGo.getUni2GenGoInfo(subGenInfo, StaxID));
			}
		}
		return blast2GeneInfo;
	}
	
	
}
