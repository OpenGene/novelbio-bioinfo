package com.novelbio.database.updatedb.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.AGeneInfo;
import com.novelbio.database.domain.geneanno.Gene2Go;
import com.novelbio.database.domain.geneanno.GeneInfo;
import com.novelbio.database.domain.geneanno.Go2Term;
import com.novelbio.database.domain.geneanno.NCBIID;
import com.novelbio.database.domain.geneanno.UniGene2Go;
import com.novelbio.database.domain.geneanno.UniGeneInfo;
import com.novelbio.database.domain.geneanno.UniProtID;
import com.novelbio.database.mapper.geneanno.MapGene2GoOld;
import com.novelbio.database.mapper.geneanno.MapGeneInfoOld;
import com.novelbio.database.mapper.geneanno.MapGo2TermOld;
import com.novelbio.database.mapper.geneanno.MapNCBIID;
import com.novelbio.database.mapper.geneanno.MapUniGene2GoOld;
import com.novelbio.database.mapper.geneanno.MapUniGeneInfoOld;
import com.novelbio.database.mapper.geneanno.MapUniProtIDOld;
import com.novelbio.database.model.modcopeid.CopeID;
import com.novelbio.database.model.modcopeid.CopedID;
import com.novelbio.database.service.servgeneanno.ServNCBIID;


public class UpDateFriceDB {
	private static Logger logger = Logger.getLogger(UpDateFriceDB.class);
	 
	/**
	 * ָ��һЩID���ҳ����ǵ�geneID��UniID��
	 * ��Ҫ�Ƿ���upDateNCBIUniID��ʹ��
	 * @param taxID ����ID
	 * @param tmpID ����ID�����Ը���һϵ�У������������null��""�����Զ�����
	 * @return
	 * string-2 0:geneID, ==0 ˵��û�ҵ� 1: UniID == null ��"" ˵��û�ҵ�
	 */
	public static String[] getGeneUniID( int taxID,String... tmpID) {
		ServNCBIID servNCBIID = new ServNCBIID();
		long geneID = 0;
		String uniID = "";
		for (int i = 0; i < tmpID.length; i++) {
			if (tmpID[i] == null || tmpID[i].trim().equals("")) {
				continue;
			}
			NCBIID ncbiid = new NCBIID();
			ncbiid.setAccID(CopeID.removeDot(tmpID[i])); ncbiid.setTaxID(taxID);
			ArrayList<NCBIID> lsNcbiid = servNCBIID.queryLsNCBIID(ncbiid);
			if (lsNcbiid != null && lsNcbiid.size()>0)
			{
				geneID = lsNcbiid.get(0).getGeneId();
				break;
			}
		}
		if (geneID ==0 )
		{
			for (int i = 0; i < tmpID.length; i++) {
				if (tmpID[i] == null || tmpID[i].trim().equals("")) {
					continue;
				}
				UniProtID uniProtID = new UniProtID();
				uniProtID.setAccID(CopeID.removeDot(tmpID[i])); uniProtID.setTaxID(taxID);
				ArrayList<UniProtID> lsUniProtIDs = MapUniProtIDOld.queryLsUniProtID(uniProtID);
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
	 * @param lsAccIDInfo һ��accID����Ϣ list-string[2] 0:accID 1:DataBaseInfo ���accID == nul �� ""l��ֱ������
	 * @param arStrings ָ����databaseInfo
	 * @return ���뷵��true��û���ܹ����룬Ҳ����û�ҵ�������false
	 */
	public static boolean upDateNCBIUniID(long GeneID, String uniID,int taxID,boolean considerGeneID,Collection<String[]> lsAccIDInfo,String ...arStrings)
	{
		ServNCBIID servNCBIID = new ServNCBIID();
		HashSet<String> hashDBInfo = new HashSet<String>();
		for (String string : arStrings) {
			hashDBInfo.add(string);
		}
		
		if (GeneID != 0)
		{
			for (String[] strings : lsAccIDInfo) 
			{
				if (strings[0] == null || strings[0].trim().equals("")) {
					continue;
				}
				NCBIID ncbiid = new NCBIID();
				ncbiid.setAccID(CopedID.removeDot(strings[0]));
				ncbiid.setTaxID(taxID);
				if (considerGeneID) {
					ncbiid.setGeneId(GeneID);
				}
				ArrayList<NCBIID> lsNcbiid = servNCBIID.queryLsNCBIID(ncbiid);
				if (lsNcbiid != null && lsNcbiid.size()>0)
				{//������ݿ��е�--��Դ��Ϣ �ͱ��β�һ�£�������Ҫ��������
					if  (hashDBInfo.contains(strings[1]) && !lsNcbiid.get(0).getDBInfo().equals(strings[1])) {
						ncbiid.setGeneId(GeneID);ncbiid.setDBInfo(strings[1]);
						ncbiid.setTaxID(taxID);
						servNCBIID.updateNCBIID(ncbiid);
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
					try {
						servNCBIID.insertNCBIID(ncbiid);
					} catch (Exception e) {
						servNCBIID.updateNCBIID(ncbiid);
					}
					
				}
			}
			return true;
		}
		//װ��UniProtID����
		else if (GeneID == 0 && uniID != null && !uniID.trim().equals("")) {
			for (String[] strings : lsAccIDInfo) 
			{
				if (strings[0] == null || strings[0].trim().equals("")) {
					continue;
				}
				UniProtID uniProtID = new UniProtID();
				uniProtID.setAccID(CopedID.removeDot(strings[0])); uniProtID.setTaxID(taxID);
				if (considerGeneID) {
					uniProtID.setUniID(uniID);
				}
				ArrayList<UniProtID> lsUniProtIDs = MapUniProtIDOld.queryLsUniProtID(uniProtID);
				if (lsUniProtIDs != null && lsUniProtIDs.size()>0)
				{
						if (hashDBInfo.contains(strings[1]) && !lsUniProtIDs.get(0).getDBInfo().equals(strings[1]))
						{
							//���ﲻ��uniID��ԭ��uniID����geneIDһ������Ψһ�ԣ�һ����ͬ��gene�϶�һ��geneID��
							//������ָ����uniID��������Ϊ��NCBIID���Ҳ�����Ȼ������ָ����һ��ID��ΪuniID�������UniprotID�����ҵ��ˣ�����Uniprot���е�ID
							//���û���ҵ��������Լ���ID
							uniProtID.setDBInfo(strings[1]); uniProtID.setUniID(uniID);
							uniProtID.setTaxID(taxID);
							MapUniProtIDOld.upDateUniProt(uniProtID);
						}
				}
				else {
					uniProtID.setDBInfo(strings[1]); uniProtID.setUniID(uniID);
					uniProtID.setTaxID(taxID);
					try {
						MapUniProtIDOld.InsertUniProtID(uniProtID);
					} catch (Exception e) {
						MapUniProtIDOld.upDateUniProt(uniProtID);
					}
					
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
		Go2Term go2Term2 = MapGo2TermOld.queryGo2Term(go2Term);
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
			Gene2Go gene2Go2 = (Gene2Go) MapGene2GoOld.queryGene2Go(gene2Go);
			if (gene2Go2 != null)
			{
				return true;
			}
			else
			{
				MapGene2GoOld.InsertGene2Go(gene2Go);
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
			AGene2Go uniGene2Go2 = MapUniGene2GoOld.queryUniGene2Go(uniGene2Go);
			if (uniGene2Go2 != null) {
				return true;
			}
			else {
				MapUniGene2GoOld.InsertUniGene2Go(uniGene2Go);
				return true;
			}
		}
		else {
			return false;
		}
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
	public static boolean upDateGenGO(CopedID copedID, String tmpGOID,String DBINFO) {
		Go2Term go2Term = new Go2Term();
		go2Term.setGoIDQuery(tmpGOID);
		Go2Term go2Term2 = MapGo2TermOld.queryGo2Term(go2Term);
		String function = go2Term2.getGoFunction();
		String term = go2Term2.getGoTerm();
		String goID = go2Term2.getGoID();
		AGene2Go gene2Go = null;
		//��װGene2GO
		if (copedID.getIDtype().equals(CopedID.IDTYPE_GENEID)) {
			gene2Go = new Gene2Go();
			gene2Go.setGOID(goID);
			gene2Go.setDataBase(DBINFO);
			gene2Go.setFunction(function);
			gene2Go.setGeneUniID(copedID.getGenUniID());
			gene2Go.setGOTerm(term);
			
			Gene2Go gene2Go2 = (Gene2Go) MapGene2GoOld.queryGene2Go((Gene2Go)gene2Go);
			if (gene2Go2 != null)
			{
				return true;
			}
			else
			{
				MapGene2GoOld.InsertGene2Go((Gene2Go)gene2Go);
				return true;
			}
		}
		//��װuniGene2GO
		else if (copedID.getIDtype().equals(CopedID.IDTYPE_UNIID)) 
		{
			gene2Go = new UniGene2Go();
			gene2Go.setGOID(goID);
			gene2Go.setDataBase(DBINFO);
			gene2Go.setFunction(function);
			gene2Go.setGeneUniID(copedID.getGenUniID());
			gene2Go.setGOTerm(term);
			AGene2Go uniGene2Go2 = MapUniGene2GoOld.queryUniGene2Go((UniGene2Go)gene2Go);
			if (uniGene2Go2 != null) {
				return true;
			}
			else {
				MapUniGene2GoOld.InsertUniGene2Go((UniGene2Go)gene2Go);
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
		Go2Term go2Term2 = MapGo2TermOld.queryGo2Term(go2Term);
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
			Gene2Go gene2Go2 = (Gene2Go) MapGene2GoOld.queryGene2Go(gene2Go);
			if (gene2Go2 != null)
			{
				if (gene2Go2.getDataBase() != null && gene2Go.getDataBase() != null && !gene2Go2.getDataBase().contains( gene2Go.getDataBase())) 
					gene2Go2.setDataBase(gene2Go2.getDataBase().replace("-", "") + gene2Go.getDataBase());
				else if  (gene2Go2.getDataBase() == null)
					gene2Go2.setDataBase( gene2Go.getDataBase());
				
				if (gene2Go2.getEvidence() != null && gene2Go.getEvidence() != null && !gene2Go2.getEvidence().contains( gene2Go.getEvidence())) {
					gene2Go2.setEvidence(gene2Go2.getEvidence().replace("-", "") + gene2Go.getEvidence());
				}
				else if  (gene2Go2.getEvidence() == null) {
					gene2Go2.setEvidence(gene2Go.getEvidence());
				}
				if (gene2Go2.getReference() != null && gene2Go.getReference()!= null && !gene2Go2.getReference().contains( gene2Go.getReference())) {
					gene2Go2.setReference(gene2Go2.getReference().replace("-", "") + gene2Go.getReference());
				}
				else if  (gene2Go2.getReference() == null) {
					gene2Go2.setReference(gene2Go.getReference());
				}
				if (gene2Go2.getQualifier() != null&& gene2Go.getQualifier() != null && !gene2Go2.getQualifier().contains( gene2Go.getQualifier())) {
					gene2Go2.setQualifier(gene2Go2.getQualifier().replace("-", "") + gene2Go.getQualifier());
				}
				else if  (gene2Go2.getQualifier() == null) {
					gene2Go2.setQualifier(gene2Go.getQualifier());
				}
				
				MapGene2GoOld.upDateGene2Go(gene2Go2);
				return true;
			}
			else
			{
				MapGene2GoOld.InsertGene2Go(gene2Go);
				return true;
			}
		}
		//��װuniGene2GO
		else if (uniID != null && !uniID.equals("")) 
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
			AGene2Go uniGene2Go2 = MapUniGene2GoOld.queryUniGene2Go(uniGene2Go);
			if (uniGene2Go2 != null) {
				
				
				if (uniGene2Go2.getDataBase() != null &&uniGene2Go.getDataBase()!=null && !uniGene2Go2.getDataBase().contains( uniGene2Go.getDataBase())) 
					uniGene2Go2.setDataBase(uniGene2Go2.getDataBase().replace("-", "") + uniGene2Go.getDataBase());
				else if (uniGene2Go2.getDataBase() == null) {
					uniGene2Go2.setDataBase( uniGene2Go.getDataBase());
				}
					
				
				if (uniGene2Go2.getEvidence() != null && uniGene2Go.getEvidence() != null && !uniGene2Go2.getEvidence().contains( uniGene2Go.getEvidence())) {
					uniGene2Go2.setEvidence(uniGene2Go2.getEvidence().replace("-", "") + uniGene2Go.getEvidence());
				}
				else if(uniGene2Go2.getEvidence() == null){
					uniGene2Go2.setEvidence(uniGene2Go.getEvidence());
				}
				if (uniGene2Go2.getReference() != null && uniGene2Go.getReference() != null && !uniGene2Go2.getReference().contains( uniGene2Go.getReference())) {
					uniGene2Go2.setReference(uniGene2Go2.getReference().replace("-", "") + uniGene2Go.getReference());
				}
				else  if(uniGene2Go2.getReference() == null){
					uniGene2Go2.setReference(uniGene2Go.getReference());
				}
				if (uniGene2Go2.getQualifier() != null && uniGene2Go.getQualifier() != null && !uniGene2Go2.getQualifier().contains( uniGene2Go.getQualifier())) {
					uniGene2Go2.setQualifier(uniGene2Go2.getQualifier().replace("-", "") + uniGene2Go.getQualifier());
				}
				else  if(uniGene2Go2.getQualifier() == null){
					uniGene2Go2.setQualifier(uniGene2Go.getQualifier());
				}
				MapUniGene2GoOld.upDateUniGene2Go((UniGene2Go) uniGene2Go2);
				
				return true;
			}
			else {
				MapUniGene2GoOld.InsertUniGene2Go(uniGene2Go);
				return true;
			}
		}
		else {
			return false;
		}
	}
	
	
	/**
	 * Ĭ���滻�������ݿ��е�symbol
	 * @param aGeneInfo
	 */
	public static void upDateGenInfo(AGeneInfo aGeneInfo)
	{
		upDateGenInfo(aGeneInfo, true);
		
	}
	
	
	public static void upDateGenInfo(AGeneInfo aGeneInfo, boolean changeSymbol)
	{
		if(aGeneInfo.getIDType().equals(CopedID.IDTYPE_GENEID))
		{
			GeneInfo geneInfo = new GeneInfo();
			geneInfo.setGeneID(Long.parseLong(aGeneInfo.getGeneUniID()));
			GeneInfo geneInfoS = MapGeneInfoOld.queryGeneInfo(geneInfo);
			if (geneInfoS == null) {
				MapGeneInfoOld.InsertGeneInfo((GeneInfo) aGeneInfo);
			}
			else {
				if (!changeSymbol) {
					aGeneInfo.setSymbol(null);
				}
				MapGeneInfoOld.upDateGeneInfo((GeneInfo) aGeneInfo);
			}
		}
		else if (aGeneInfo.getIDType().equals(CopedID.IDTYPE_UNIID)) {
			UniGeneInfo geneInfo = new UniGeneInfo();
			geneInfo.setUniProtID(aGeneInfo.getGeneUniID());
			UniGeneInfo geneInfoS = MapUniGeneInfoOld.queryUniGeneInfo(geneInfo);
			if (geneInfoS == null) {
				MapUniGeneInfoOld.InsertUniGeneInfo((UniGeneInfo) aGeneInfo);
			}
			else {
				if (!changeSymbol) {
					aGeneInfo.setSymbol(null);
				}
				MapUniGeneInfoOld.upDateUniGeneInfo((UniGeneInfo) aGeneInfo);
			}
		}
		else {
			logger.error("����û������IDType");
		}
	}
}
