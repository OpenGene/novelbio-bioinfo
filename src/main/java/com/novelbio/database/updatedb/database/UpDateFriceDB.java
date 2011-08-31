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
	 * ָ��һЩID���ҳ����ǵ�geneID��UniID��
	 * ��Ҫ�Ƿ���upDateNCBIUniID��ʹ��
	 * @param taxID ����ID
	 * @param tmpID ����ID�����Ը���һϵ��
	 * @return
	 * string-2 0:geneID, ==0 ˵��û�ҵ� 1: UniID == null ��"" ˵��û�ҵ�
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
	 * ����һ��accID��DBinfo��list����һ��accID����ͬһ��gene�Ĳ�ͬAccID�Լ���ID���ڵ����ݿ⣬
	 * ͬʱָ��һЩ���ݿ������--Ҳ����DBinfo��Ϣ����ô���ϸ����Ƶ�����accID�ᱻ��������ʵ������ǰLOC_Os01g01110��DBinfo������NCBIID�����ڸĳ�TIGRrice��
	 * ���ݿ���û�еĻᱻ���롣
	 * @param geneID ���geneID��Ϊ0������NCBIID���ݿ�
	 * @param uniID ���uniID��Ϊnull������uniProtID���ݿ�
	 * ע�����uniID��geneID���棬��ô���ȿ���geneID
	 * @param taxID
	 * @param considerGeneID �Ƿ�geneID�����ѯ����Ϊ��Щgene���ϲ�ֹһ��geneID�����еĲ��ǣ��������ֻ�ǳ���Ļ���Ͳ��ܿ���geneID
	 * ��Ⱦɫ���accID����Ҫ����geneID
	 * @param lsAccIDInfo һ��accID����Ϣ list-string[2] 0:accID 1:DataBaseInfo
	 * @param arStrings ָ����databaseInfo
	 * @return ���뷵��true��û���ܹ����룬Ҳ����û�ҵ�������false
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
				{//������ݿ��е�--��Դ��Ϣ �ͱ��β�һ�£�������Ҫ��������
					if  (hashDBInfo.contains(strings[1]) && !lsNcbiid.get(0).getDBInfo().equals(strings[1])) {
						ncbiid.setGeneId(GeneID);ncbiid.setDBInfo(strings[1]);
						ncbiid.setTaxID(taxID);
						DaoFSNCBIID.upDateNCBIID(ncbiid);
					}
				}
				else
				{
//					//������rapdb��gffʱ�����ȥ��ע��
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
		//װ��UniProtID����
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
							//���ﲻ��uniID��ԭ��uniID����geneIDһ������Ψһ�ԣ�һ����ͬ��gene�϶�һ��geneID��
							//������ָ����uniID��������Ϊ��NCBIID���Ҳ�����Ȼ������ָ����һ��ID��ΪuniID�������UniprotID�����ҵ��ˣ�����Uniprot���е�ID
							//���û���ҵ��������Լ���ID
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
	 * ����һ��GOterm���Լ���Ӧ��GeneID��UniID�������GoTerm������Ӧ�ı�.
	 * ��һ��geneID����GOterm����ʱ�������
	 * ͬʱָ��GO���ݿ����Դ�����ݿ���û�еĻᱻ���롣
	 * @param geneID ���geneID��Ϊ0������NCBIID���ݿ�
	 * @param uniID ���uniID��Ϊnull������uniProtID���ݿ�
	 * ע�����uniID��geneID���棬��ô���ȿ���geneID
	 * @param lsAccIDInfo һ��accID����Ϣ list-string[2] 0:accID 1:DataBaseInfo
	 * @param arStrings ָ����databaseInfo
	 * @return ���뷵��true��û���ܹ����룬Ҳ����û�ҵ�������false
	 */
	public static boolean upDateGenGO(long GeneID,String uniID,String tmpGOID,String DBINFO) {
		Go2Term go2Term = new Go2Term();
		go2Term.setGoIDQuery(tmpGOID);
		Go2Term go2Term2 = DaoFSGo2Term.queryGo2Term(go2Term);
		String function = go2Term2.getGoFunction();
		String term = go2Term2.getGoTerm();
		String goID = go2Term2.getGoID();
		//��װGene2GO
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
		//��װuniGene2GO
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
	 * ����һ��Gene2Go���Լ���Ӧ��GeneID��UniID�������GoTerm������Ӧ�ı�.
	 * ��һ��geneID����GOterm����ʱ�������
	 * ͬʱָ��GO���ݿ����Դ�����ݿ���û�еĻᱻ���롣
	 * @param geneID ���geneID��Ϊ0������NCBIID���ݿ�
	 * @param uniID ���uniID��Ϊnull������uniProtID���ݿ�
	 * ע�����uniID��geneID���棬��ô���ȿ���geneID
	 * @param gene2Go ��Ϣ������gene2Go����
	 * @return ���뷵��true��û���ܹ����룬Ҳ����û�ҵ�������false
	 */
	public static boolean upDateGenGO(long GeneID,String uniID,Gene2Go gene2GoTmp) {
		Go2Term go2Term = new Go2Term();
		go2Term.setGoIDQuery(gene2GoTmp.getGOID());
		Go2Term go2Term2 = DaoFSGo2Term.queryGo2Term(go2Term);
		if (go2Term2 == null) {
			logger.error("û�и�GOTerm��"+ gene2GoTmp.getGOID());
			return false;
		}
		String function = go2Term2.getGoFunction();
		String term = go2Term2.getGoTerm();
		String goID = go2Term2.getGoID();
		//��װGene2GO
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
		//��װuniGene2GO
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
