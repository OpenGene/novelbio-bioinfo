package com.novelbio.database.updatedb.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.analysis.annotation.copeID.CopeID;
import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.database.DAO.FriceDAO.DaoFSGene2Go;
import com.novelbio.database.DAO.FriceDAO.DaoFSGo2Term;
import com.novelbio.database.DAO.FriceDAO.DaoFSNCBIID;
import com.novelbio.database.DAO.FriceDAO.DaoFSUniGene2Go;
import com.novelbio.database.DAO.FriceDAO.DaoFSUniProtID;
import com.novelbio.database.entity.friceDB.AGene2Go;
import com.novelbio.database.entity.friceDB.Gene2Go;
import com.novelbio.database.entity.friceDB.Go2Term;
import com.novelbio.database.entity.friceDB.NCBIID;
import com.novelbio.database.entity.friceDB.UniGene2Go;
import com.novelbio.database.entity.friceDB.UniProtID;


public class UpDateFriceDB {
	private static Logger logger = Logger.getLogger(UpDateFriceDB.class);
	/**
	 * 指定一些ID，找出他们的geneID或UniID、
	 * 主要是方便upDateNCBIUniID的使用
	 * @param taxID 物种ID
	 * @param tmpID 给定ID，可以给定一系列
	 * @return
	 * string-2 0:geneID, ==0 说明没找到 1: UniID == null 或"" 说明没找到
	 */
	public static String[] getGeneUniID( int taxID,String... tmpID) {
		long geneID = 0;
		String uniID = "";
		for (int i = 1; i < tmpID.length; i++) {
			NCBIID ncbiid = new NCBIID();
			ncbiid.setAccID(CopeID.removeDot(tmpID[i])); ncbiid.setTaxID(taxID);
			ArrayList<NCBIID> lsNcbiid = DaoFSNCBIID.queryLsNCBIID(ncbiid);
			if (lsNcbiid != null && lsNcbiid.size()>0)
			{
				geneID = lsNcbiid.get(0).getGeneId();
				break;
			}
		}
		if (geneID ==0 )
		{
			for (int i = 1; i < tmpID.length; i++) {
				UniProtID uniProtID = new UniProtID();
				uniProtID.setAccID(CopeID.removeDot(tmpID[i])); uniProtID.setTaxID(taxID);
				ArrayList<UniProtID> lsUniProtIDs = DaoFSUniProtID.queryLsUniProtID(uniProtID);
				if (lsUniProtIDs != null && lsUniProtIDs.size()>0)
				{
					uniID = lsUniProtIDs.get(0).getUniID();
					break;
				}
			}
		}
		String[] result = new String[2];
		result[0] = geneID + "";
		result[1] = uniID;
		return result;
	}
	
	
	/**
	 * 
	 * 给定一组accID与DBinfo的list，这一组accID都是同一个gene的不同AccID以及该ID所在的数据库，
	 * 同时指定一些数据库的名称--也就是DBinfo信息，那么符合该名称的数据accID会被升级。其实就是以前LOC_Os01g01110的DBinfo可能是NCBIID，现在改成TIGRrice。
	 * 数据库中没有的会被插入。
	 * @param geneID 如果geneID不为0，插入NCBIID数据库
	 * @param uniID 如果uniID不为null，插入uniProtID数据库
	 * 注意如果uniID和geneID并存，那么优先考虑geneID
	 * @param taxID
	 * @param considerGeneID 是否将geneID进入查询，因为有些gene对上不止一个geneID，而有的不是，所以如果只是常规的基因就不能考虑geneID
	 * 而染色体的accID就需要考虑geneID
	 * @param lsAccIDInfo 一组accID的信息 list-string[2] 0:accID 1:DataBaseInfo
	 * @param arStrings 指定的databaseInfo
	 * @return 插入返回true，没有能够插入，也就是没找到，返回false
	 */
	public static boolean upDateNCBIUniID(long GeneID, String uniID,int taxID,boolean considerGeneID,Collection<String[]> lsAccIDInfo,String ...arStrings)
	{
		HashSet<String> hashDBInfo = new HashSet<String>();
		for (String string : arStrings) {
			hashDBInfo.add(string);
		}
		
		if (GeneID != 0)
		{
			for (String[] strings : lsAccIDInfo) 
			{
				NCBIID ncbiid = new NCBIID();
				ncbiid.setAccID(strings[0]);
				ncbiid.setTaxID(taxID);
				if (considerGeneID) {
					ncbiid.setGeneId(GeneID);
				}
				ArrayList<NCBIID> lsNcbiid = DaoFSNCBIID.queryLsNCBIID(ncbiid);
				if (lsNcbiid != null && lsNcbiid.size()>0)
				{//如果数据库中的--来源信息 和本次不一致，并且需要更换名称
					if  (hashDBInfo.contains(strings[1]) && !lsNcbiid.get(0).getDBInfo().equals(strings[1])) {
						ncbiid.setGeneId(GeneID);ncbiid.setDBInfo(strings[1]);
						ncbiid.setTaxID(taxID);
						DaoFSNCBIID.upDateNCBIID(ncbiid);
					}
				}
				else
				{
//					//当输入rapdb的gff时候将这个去除注释
//					if (strings[1].equals(NovelBioConst.DBINFO_UNIPROT_GenralID)) {
//						continue;
//					}
					
					ncbiid.setGeneId(GeneID);ncbiid.setDBInfo(strings[1]);
					ncbiid.setTaxID(taxID);
					DaoFSNCBIID.InsertNCBIID(ncbiid);
				}
			}
			return true;
		}
		//装入UniProtID表中
		else if (GeneID == 0 && uniID != null && !uniID.trim().equals("")) {
			for (String[] strings : lsAccIDInfo) 
			{
				UniProtID uniProtID = new UniProtID();
				uniProtID.setAccID(strings[0]);uniProtID.setTaxID(taxID);
				if (considerGeneID) {
					uniProtID.setUniID(uniID);
				}
				ArrayList<UniProtID> lsUniProtIDs = DaoFSUniProtID.queryLsUniProtID(uniProtID);
				if (lsUniProtIDs != null && lsUniProtIDs.size()>0)
				{
						if (hashDBInfo.contains(strings[1]) && !lsUniProtIDs.get(0).getDBInfo().equals(strings[1]))
						{
							//这里不用uniID的原因：uniID不像geneID一样具有唯一性，一组相同的gene肯定一个geneID，
							//而这里指定的uniID仅仅是因为在NCBIID中找不到，然后随意指定了一个ID作为uniID，如果在UniprotID表中找到了，就用Uniprot表中的ID
							//如果没有找到，才用自己的ID
							uniProtID.setDBInfo(strings[1]); uniProtID.setUniID(uniID);
							uniProtID.setTaxID(taxID);
							DaoFSUniProtID.upDateUniProt(uniProtID);
						}
				}
				else {
					uniProtID.setDBInfo(strings[1]); uniProtID.setUniID(uniID);
					uniProtID.setTaxID(taxID);
					DaoFSUniProtID.InsertUniProtID(uniProtID);
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * 给定一个GOterm，以及相应的GeneID或UniID，将这个GoTerm插入相应的表.
	 * 当一个geneID仅有GOterm存在时会最合适
	 * 同时指定GO数据库的来源，数据库中没有的会被插入。
	 * @param geneID 如果geneID不为0，插入NCBIID数据库
	 * @param uniID 如果uniID不为null，插入uniProtID数据库
	 * 注意如果uniID和geneID并存，那么优先考虑geneID
	 * @param lsAccIDInfo 一组accID的信息 list-string[2] 0:accID 1:DataBaseInfo
	 * @param arStrings 指定的databaseInfo
	 * @return 插入返回true，没有能够插入，也就是没找到，返回false
	 */
	public static boolean upDateGenGO(long GeneID,String uniID,String tmpGOID,String DBINFO) {
		Go2Term go2Term = new Go2Term();
		go2Term.setGoIDQuery(tmpGOID);
		Go2Term go2Term2 = DaoFSGo2Term.queryGo2Term(go2Term);
		String function = go2Term2.getGoFunction();
		String term = go2Term2.getGoTerm();
		String goID = go2Term2.getGoID();
		//先装Gene2GO
		if (GeneID != 0) {
			Gene2Go gene2Go = new Gene2Go();
			gene2Go.setGOID(goID);
			gene2Go.setDataBase(DBINFO);
			gene2Go.setFunction(function);
			gene2Go.setGeneId(GeneID);
			gene2Go.setGOTerm(term);
			Gene2Go gene2Go2 = (Gene2Go) DaoFSGene2Go.queryGene2Go(gene2Go);
			if (gene2Go2 != null)
			{
				return true;
			}
			else
			{
				DaoFSGene2Go.InsertGene2Go(gene2Go);
				return true;
			}
		}
		//再装uniGene2GO
		else if (uniID != null) 
		{
			UniGene2Go uniGene2Go = new UniGene2Go();
			uniGene2Go.setGOID(goID);
			uniGene2Go.setDataBase(DBINFO);
			uniGene2Go.setFunction(function);
			uniGene2Go.setUniProtID(uniID);
			uniGene2Go.setGOTerm(term);
			AGene2Go uniGene2Go2 = DaoFSUniGene2Go.queryUniGene2Go(uniGene2Go);
			if (uniGene2Go2 != null) {
				return true;
			}
			else {
				DaoFSUniGene2Go.InsertUniGene2Go(uniGene2Go);
				return true;
			}
		}
		else {
			return false;
		}
	}
	
	/**
	 * 给定一个Gene2Go，以及相应的GeneID或UniID，将这个GoTerm插入相应的表.
	 * 当一个geneID仅有GOterm存在时会最合适
	 * 同时指定GO数据库的来源，数据库中没有的会被插入。
	 * @param geneID 如果geneID不为0，插入NCBIID数据库
	 * @param uniID 如果uniID不为null，插入uniProtID数据库
	 * 注意如果uniID和geneID并存，那么优先考虑geneID
	 * @param gene2Go 信息保存在gene2Go里面
	 * @return 插入返回true，没有能够插入，也就是没找到，返回false
	 */
	public static boolean upDateGenGO(long GeneID,String uniID,Gene2Go gene2GoTmp) {
		Go2Term go2Term = new Go2Term();
		go2Term.setGoIDQuery(gene2GoTmp.getGOID());
		Go2Term go2Term2 = DaoFSGo2Term.queryGo2Term(go2Term);
		if (go2Term2 == null) {
			logger.error("没有该GOTerm："+ gene2GoTmp.getGOID());
			return false;
		}
		String function = go2Term2.getGoFunction();
		String term = go2Term2.getGoTerm();
		String goID = go2Term2.getGoID();
		//先装Gene2GO
		if (GeneID != 0) {
			Gene2Go gene2Go = new Gene2Go();
			gene2Go.setGOID(goID);
			
			gene2Go.setDataBase(gene2GoTmp.getDataBase());
			gene2Go.setEvidence(gene2GoTmp.getEvidence());
			gene2Go.setReference(gene2GoTmp.getReference());
			gene2Go.setQualifier(gene2GoTmp.getQualifier());
			
			gene2Go.setFunction(function);
			gene2Go.setGeneId(GeneID);
			gene2Go.setGOTerm(term);
			Gene2Go gene2Go2 = (Gene2Go) DaoFSGene2Go.queryGene2Go(gene2Go);
			if (gene2Go2 != null)
			{
				if (gene2Go2.getDataBase() != null && !gene2Go2.getDataBase().contains( gene2Go.getDataBase())) 
					gene2Go2.setDataBase(gene2Go2.getDataBase().replace("-", "") + gene2Go.getDataBase());
				else 
					gene2Go2.setDataBase( gene2Go.getDataBase());
				
				if (gene2Go2.getEvidence() != null && !gene2Go2.getEvidence().contains( gene2Go.getEvidence())) {
					gene2Go2.setEvidence(gene2Go2.getEvidence().replace("-", "") + gene2Go.getEvidence());
				}
				else {
					gene2Go2.setEvidence(gene2Go.getEvidence());
				}
				if (gene2Go2.getReference() != null&& !gene2Go2.getReference().contains( gene2Go.getReference())) {
					gene2Go2.setReference(gene2Go2.getReference().replace("-", "") + gene2Go.getReference());
				}
				else {
					gene2Go2.setReference(gene2Go.getReference());
				}
				if (gene2Go2.getQualifier() != null&& !gene2Go2.getQualifier().contains( gene2Go.getQualifier())) {
					gene2Go2.setQualifier(gene2Go2.getQualifier().replace("-", "") + gene2Go.getQualifier());
				}
				else {
					gene2Go2.setQualifier(gene2Go.getQualifier());
				}
				
				DaoFSGene2Go.upDateGene2Go(gene2Go2);
				return true;
			}
			else
			{
				DaoFSGene2Go.InsertGene2Go(gene2Go);
				return true;
			}
		}
		//再装uniGene2GO
		else if (uniID != null) 
		{
			UniGene2Go uniGene2Go = new UniGene2Go();
			uniGene2Go.setGOID(goID);
			
			uniGene2Go.setDataBase(gene2GoTmp.getDataBase());
			uniGene2Go.setEvidence(gene2GoTmp.getEvidence());
			uniGene2Go.setReference(gene2GoTmp.getReference());
			uniGene2Go.setQualifier(gene2GoTmp.getQualifier());
			
			uniGene2Go.setFunction(function);
			uniGene2Go.setUniProtID(uniID);
			uniGene2Go.setGOTerm(term);
			AGene2Go uniGene2Go2 = DaoFSUniGene2Go.queryUniGene2Go(uniGene2Go);
			if (uniGene2Go2 != null) {
				
				
				if (uniGene2Go2.getDataBase() != null && !uniGene2Go2.getDataBase().contains( uniGene2Go.getDataBase())) 
					uniGene2Go2.setDataBase(uniGene2Go2.getDataBase().replace("-", "") + uniGene2Go.getDataBase());
				else 
					uniGene2Go2.setDataBase( uniGene2Go.getDataBase());
				
				if (uniGene2Go2.getEvidence() != null && !uniGene2Go2.getEvidence().contains( uniGene2Go.getEvidence())) {
					uniGene2Go2.setEvidence(uniGene2Go2.getEvidence().replace("-", "") + uniGene2Go.getEvidence());
				}
				else {
					uniGene2Go2.setEvidence(uniGene2Go.getEvidence());
				}
				if (uniGene2Go2.getReference() != null && !uniGene2Go2.getReference().contains( uniGene2Go.getReference())) {
					uniGene2Go2.setReference(uniGene2Go2.getReference().replace("-", "") + uniGene2Go.getReference());
				}
				else {
					uniGene2Go2.setReference(uniGene2Go.getReference());
				}
				if (uniGene2Go2.getQualifier() != null && !uniGene2Go2.getQualifier().contains( uniGene2Go.getQualifier())) {
					uniGene2Go2.setQualifier(uniGene2Go2.getQualifier().replace("-", "") + uniGene2Go.getQualifier());
				}
				else {
					uniGene2Go2.setQualifier(uniGene2Go.getQualifier());
				}
				DaoFSUniGene2Go.upDateUniGene2Go((UniGene2Go) uniGene2Go2);
				
				return true;
			}
			else {
				DaoFSUniGene2Go.InsertUniGene2Go(uniGene2Go);
				return true;
			}
		}
		else {
			return false;
		}
	}
	
	
	
	
	
	
	
	
	
	
}
