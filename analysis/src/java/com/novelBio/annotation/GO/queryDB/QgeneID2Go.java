package com.novelBio.annotation.GO.queryDB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import org.apache.poi.hdf.extractor.SEP;

import com.novelBio.annotation.genAnno.AnnoQuery;
import com.novelBio.annotation.genAnno.GOQuery;
import com.novelBio.annotation.pathway.kegg.prepare.KGprepare;
import com.novelBio.base.dataOperate.TxtReadandWrite;

import DAO.FriceDAO.DaoFCGene2GoInfo;
import DAO.FriceDAO.DaoFSGo2Term;

import entity.friceDB.BlastInfo;
import entity.friceDB.Gene2Go;
import entity.friceDB.Gene2GoInfo;
import entity.friceDB.GeneInfo;
import entity.friceDB.Go2Term;
import entity.friceDB.NCBIID;
import entity.friceDB.Uni2GoInfo;
import entity.friceDB.UniGene2Go;
import entity.friceDB.UniProtID;

/**
 * ����CopeID��������geneID��һ����������IDԤ�����Լ��ϲ��ȣ��õ���list��������ģ����GO�Ĳ�ѯ<br>
 * �����listΪ��
 * 	 * arrayList-string[3] :<br>
	 * 0: ID���ͣ�"geneID"��"uniID"��"accID"<br>
	 * 1: accID<br>
	 * 2: ����ת����ID<br>
 * @author zong0jie
 *
 */
public class QgeneID2Go {

	/**
	 * ���ҵ���geneID����Ϣ����ʱû�õ�
	 * @param ncbiid
	 * @return ��������arrayList,
	 * �����ncbiid<b>������geneID��Ŀ</b>��<br>
	 * �����<b>�ֿ�accID</b>�ģ���ô�����ncbiid����<b>��accID��Ŀ</b>����ô<br>
	 * ��һ��geneInfo��0��accID  1:geneSymbol/accID  2:description<br>
	 * �����<b>�ϲ�accID</b>����ô�����ncbiid����<b>û��accID��Ŀ</b>����ô<br>
	 * ��һ��geneInfo��0��geneID  1:geneSymbol/accID  2:description<br>
	 * ���û�鵽Go����ô�����Ϊֹ�����涼��������<br>
	 * �ڶ���goID��goID goID <br>
	 * ������evidence����Ӧÿ��goID��evidence Ʃ��IEA��<br>
	 * ������Լ�������������Ϣ
	 */
	public static ArrayList<String[]> getGenGoInfo(NCBIID ncbiid)
	{
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		String[] genInfo = new String[3];
		String[] geneAnoInfo = AnnoQuery.getGenInfo(ncbiid.getGeneId());
		if (ncbiid.getAccID() == null || ncbiid.getAccID().equals("")) {
			genInfo[0] = ncbiid.getGeneId()+"";
		}
		else {
			genInfo[0] = ncbiid.getAccID();
		}
		genInfo[1] = geneAnoInfo[0];genInfo[2] = geneAnoInfo[1];
		lsResult.add(genInfo);
		///////////////////////////////////////////////////////////////////////////////////////////////////
		ArrayList<Gene2Go> lsGene2Gos = GOQuery.getGen2Go(ncbiid);
		
		if (lsGene2Gos != null && lsGene2Gos.size()>0) {
			String[] goID = new String[lsGene2Gos.size()];
			String[] goEvidence = new String[lsGene2Gos.size()];
			for (int i = 0; i<lsGene2Gos.size() ; i++) {
				goID[i] = lsGene2Gos.get(i).getGOID();
				goEvidence[i] = lsGene2Gos.get(i).getEvidence();
			}
			lsResult.add(goID);lsResult.add(goEvidence);
		}
		return lsResult;
	}
	
	/**
	 * ���ҵ���UniprotID����Ϣ����ʱû�õ�
	 * @param ncbiid
	 * @return ��������arrayList,
	 * �����ncbiid<b>������geneID��Ŀ</b>��<br>
	 * �����<b>�ֿ�accID</b>�ģ���ô�����uniProtID����<b>��accID��Ŀ</b>����ô<br>
	 * ��һ��geneInfo��0��accID  1:geneSymbol/accID  2:description<br>
	 * �����<b>�ϲ�accID</b>����ô�����ncbiid����<b>û��accID��Ŀ</b>����ô<br>
	 * ��һ��geneInfo��0��uniID  1:geneSymbol/accID  2:description<br>
	 * ���û�鵽Go����ô�����Ϊֹ�����涼��������<br>
	 * �ڶ���goID��goID goID <br>
	 * ������evidence����Ӧÿ��goID��evidence Ʃ��IEA��<br>
	 * ������Լ�������������Ϣ
	 */
	public static ArrayList<String[]> getUniGenGoInfo(UniProtID uniProtID)
	{
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		String[] genInfo = new String[3];
		String[] geneAnoInfo = AnnoQuery.getUniGenInfo(uniProtID.getUniID());
		if (uniProtID.getAccID() == null || uniProtID.getAccID().equals("")) {
			genInfo[0] = uniProtID.getUniID();
		}
		else {
			genInfo[0] = uniProtID.getAccID();
		}
		genInfo[1] = geneAnoInfo[0];genInfo[2] = geneAnoInfo[1];
		lsResult.add(genInfo);
		///////////////////////////////////////////////////////////////////////////////////////////////////
		ArrayList<UniGene2Go> lsuniGene2Gos = GOQuery.getUniGen2Go(uniProtID);
		
		if (lsuniGene2Gos != null && lsuniGene2Gos.size()>0) {
			String[] goID = new String[lsuniGene2Gos.size()];
			String[] goEvidence = new String[lsuniGene2Gos.size()];
			for (int i = 0; i<lsuniGene2Gos.size() ; i++) {
				goID[i] = lsuniGene2Gos.get(i).getGOID();
				goEvidence[i] = lsuniGene2Gos.get(i).getEvidence();
			}
			lsResult.add(goID);lsResult.add(goEvidence);
		}
		return lsResult;
	}

	/**
	 * @param genInfo
	 * 0: ID���ͣ�"geneID"��"uniID"��"accID"<br>
	 * 1: accID<br>
	 * 2: ����ת����ID<br>
	 * @param taxID
	 * @param sep
	 * @return
	 */
	public static Gene2GoInfo getGen2GoInfo(String[] genInfo,int taxID)
	{
			NCBIID ncbiid = new NCBIID();
			ncbiid.setAccID(genInfo[1]);
			ncbiid.setGeneId(Long.parseLong(genInfo[2]));
			ncbiid.setTaxID(taxID);
			return DaoFCGene2GoInfo.queryGeneDetail(ncbiid);
	}
	
	/**
	 * @param genInfo
	 * 0: ID���ͣ�"geneID"��"uniID"��"accID"<br>
	 * 1: accID<br>
	 * 2: ����ת����ID<br>
	 * @param taxID
	 * @param sep
	 * @return
	 */
	public static Uni2GoInfo getUni2GenGoInfo(String[] genInfo,int taxID)
	{
		UniProtID uniProtID = new UniProtID();
		uniProtID.setAccID(genInfo[1]);
		uniProtID.setUniID(genInfo[2]);
		uniProtID.setTaxID(taxID);
		return DaoFCGene2GoInfo.queryUniDetail(uniProtID);
	}
	
	/**
	 * @param genInfo
	 * 0: ID���ͣ�"geneID"��"uniID"��"accID"<br>
	 * 1: accID<br>
	 * 2: ����ת����ID<br>
	 * @param taxID
	 * @param sep
	 * @return
	 * ��������arrayList,
	 * �����<b>�ֿ�accID</b>�ģ���ô<br>
	 * ��һ��geneInfo��0��accID  1:geneSymbol/accID  2:description<br>
	 * �����<b>�ϲ�accID</b>����ô<br>
	 * ��һ��geneInfo��0��uniID  1:geneSymbol/accID  2:description<br>
	 * ���û�鵽Go����ô�����Ϊֹ�����涼��������<br>
	 * �ڶ���goID��goID goID <br>
	 * ������evidence����Ӧÿ��goID��evidence Ʃ��IEA��<br>
	 * ������Լ�������������Ϣ
	 */
	public static ArrayList<String[]> getGenGoInfo(String[] genInfo,int taxID,boolean sep) {
		ArrayList<String[]> lsGenGoInfo = null;
			if (genInfo[0].equals("geneID"))
			{
				NCBIID ncbiid = new NCBIID();
				if (sep) {
					ncbiid.setAccID(genInfo[1]); 
				}
				ncbiid.setGeneId(Long.parseLong(genInfo[2])); ncbiid.setTaxID(taxID);
				lsGenGoInfo = getGenGoInfo(ncbiid);
			}
			else if (genInfo[0].equals("uniID")) 
			{
				UniProtID uniProtID = new UniProtID();
				if (sep) {
					uniProtID.setAccID(genInfo[1]); 
				}
				uniProtID.setUniID(genInfo[2]); uniProtID.setTaxID(taxID);
				lsGenGoInfo = getUniGenGoInfo(uniProtID);
			}
			else
			{
				if (!genInfo[0].equals("accID"))
				{
					System.out.println("error ����ID�����˴�����QgeneID2Go �� getGenGoInfo����");
				}
			}
		return lsGenGoInfo;
	}
	
	/**
	 * @param genInfo
	 * 0: ID���ͣ�"geneID"��"uniID"��"accID"<br>
	 * 1: accID<br>
	 * 2: ����ת����ID<br>
	 * @param taxID
	 * @param sep
	 * @return
	 * ����blast����Ϣ
	 */
	public static BlastInfo getBlastInfo(String[] genInfo,int QtaxID,int StaxID,double evalue)
	{
		BlastInfo blastInfo = null;
		if (genInfo[0].equals("geneID")) {
			NCBIID ncbiid = new NCBIID();
			 ncbiid.setGeneId(Long.parseLong(genInfo[2])); ncbiid.setTaxID(QtaxID);
			 blastInfo = AnnoQuery.getBlastInfo(ncbiid, evalue, StaxID);
		}
		else if (genInfo[0].equals("uniID")) {
			UniProtID uniProtID = new UniProtID();
			uniProtID.setUniID(genInfo[2]); uniProtID.setTaxID(QtaxID);
			blastInfo = AnnoQuery.getBlastInfo(uniProtID, evalue, StaxID);
		}
		else
		{
			String accID = genInfo[1];
			blastInfo =AnnoQuery.getBlastInfo(accID, evalue, StaxID);
		}
		return blastInfo;
	}
	
	/**
	 *  ����CopeID��������geneID��һ����������IDԤ�����Լ��ϲ��ȣ��õ���list�������÷�����GO�Ĳ�ѯ<br>
	 *  @param lsAccID 	 
	 *  * arraylist-string[3]<br>
	 * 0: ID���ͣ�"geneID"��"uniID"��"accID"<br>
	 * 1: accID<br>
	 * 2: ����ת����ID<br>
	 * @param QtaxID
	 * @param GOClass Go������ P: biological Process F:molecular Function C: cellular Component ���GOClassΪ""��ô��ѡ��ȫ��
	 * @param sepID �Ƿ�ֿ�ID��true�ֿ� false�ϲ�ID
	 * @param blast �Ƿ�blast
	 * @param evalue
	 * @param StaxID
	 * @return 
	 * <b>���blast</b>����������arrayList-string[]<br>
	 * ��һ����string[15]
	  * 0: queryID<br>
	  * 1: queryGeneID<br>
	  * 2: querySymbol<br>
	  * 3: Description<br>
	  * 4: GOID<br>
	  * 5: GOTerm<br>
	  * 6: GO���Ŷ�<br>
	  * 7: blastEvalue<br>
	  * 8: taxID<br>
	  * 9: subjectGeneID<br>
	  * 10: subjectSymbol<br>
	  * 11: Description<br>
	  * 12: GOID<br>
	  * 13: GOTerm<br>
	  * 14: GO���Ŷ�<br>
	 * �ڶ�����string[2]
	 * lsGene2Go ����һ�� lsGene2Go-Stirng[]������String[]�ĽṹΪ<br>
	 * 0��sepID-true: accID sepID-false: geneID<br>
	 * 1��GOID,GOID,GOID...<br>
	 * ��������string[6],Ҳ����GO2Gene�����ֻ����NBCfisher�вŻ��õ�<br>
	 * 0: GOID<br>
	 * 1: GOTerm<br>
	 * 2: GO���Ŷ�<br>
	 * 3: queryID<br>
	 * 4: queryGeneID<br>
	 * 5: querySymbol<br>
	 * 6: subjectSymbol<br>>
	 * <b>�����blast</b>����������arrayList-string[]<br>
	 * ��һ��string[7]
	 * 0: queryID<br>
	 * 1: geneID<br>
	 * 2: symbol<br>
	 * 3: description<br>
	 * 4: GOID<br>
	 * 5: GOTerm<br>
	 * 6: Evidence<br>
	 * �ڶ�����string[2]
	 * lsGene2Go ����һ�� lsGene2Go-Stirng[]������String[]�ĽṹΪ<br>
	 * 0��sepID-true: accID sepID-false: geneID<br>
	 * 1��GOID,GOID,GOID...<br>
	 * 
	 * 
	 */
	public static ArrayList<ArrayList<String[]>> getGenGoInfo(ArrayList<String[]> lsAccID, int QtaxID,String GOClass,boolean sepID,boolean blast,double evalue, int StaxID) 
	{
		/**
		 * ���汾�����blast����Ϣ
		 * �����blast
		 * ���У�0: queryID<br>
		 * 1: geneID<br>
		 * 2: symbol<br>
		 * 3: description<br>
		 * 4: GOID<br>
		 * 5: GOTerm<br>
		 * 6: Evidence<br>
		 * ���blast
		 * 0: queryID<br>
		 * 1: queryGeneID<br>
		 * 2: querySymbol<br>
		 * 3: Description<br>
		 * 4: GOID<br>
		 * 5: GOTerm<br>
		 * 6: GO���Ŷ�<br>
		 * 7: blastEvalue<br>
		 * 8: taxID<br>
		 * 9: subjectGeneID<br>
		 * 10: subjectSymbol<br>
		 * 11: Description<br>
		 * 12: GOID<br>
		 * 13: GOTerm<br>
		 * 14: GO���Ŷ�<br>
		 */
		ArrayList<String[]> lsGene2GoInfo = new ArrayList<String[]>();
		/**
		 * ֻ��blast�Ż��������Ŀ
		 * 0: GOID
		 * 1: GOTerm
		 * 2: GO���Ŷ�
		 * 3: queryID
		 * 4: querySymbol
		 * 5: subjectSymbol
		 */
		ArrayList<String[]> lsGo2Gene = new ArrayList<String[]>();
		//װ��0��sepID-true: accID sepID-false: geneID
		//1��GOID,GOID,GOID...
		ArrayList<String[]> lsGene2Go = new ArrayList<String[]>();
		///////////ֱ�Ӳ���///////////////////////////////////
		for (String[] strings : lsAccID)
		{
			if (strings[2].equals("LOC_Os01g54230")) {
				System.out.println("test");
			}
			Gene2GoInfo Qgene2GoInfo =null;
			Uni2GoInfo Quni2GoInfo = null;
			
			if (strings[0].equals("geneID")) {
				Qgene2GoInfo = getGen2GoInfo(strings, QtaxID);
			}
			else if(strings[0].equals("uniID"))
			{
				Quni2GoInfo = getUni2GenGoInfo(strings, QtaxID);
			}
			else if(strings[0].equals("accID")) {
				Qgene2GoInfo = new Gene2GoInfo();
				Qgene2GoInfo.setQuaryID(strings[2]);
			}
			Gene2GoInfo QBlastGene2GoInfo =null;
			Uni2GoInfo QBlastUni2GoInfo = null;
			
			BlastInfo blastInfo = null;
			if (blast)
			{
				ArrayList<String[]> lsBlastGenGoInfo = null;
				///////////////����blast��Ϣ/////////////////////////////////
				blastInfo = getBlastInfo(strings, QtaxID, StaxID, evalue);
				////////////////�ѵ�blast��Ϣ���ٻ�ȥ��geneInfo/////////////////////////////////////////
				if (blastInfo != null && blastInfo.getEvalue()<=evalue)
				{
					String tab = blastInfo.getSubjectTab();
					String[] genInfo = new String[3];
					genInfo[2] = blastInfo.getSubjectID();
					if (tab.equals("NCBIID")) 
					{
						genInfo[1] = AnnoQuery.getGenName(Long.parseLong(genInfo[2]));
						genInfo[0] = "geneID";
						QBlastGene2GoInfo = getGen2GoInfo(genInfo, StaxID);
					}
					else 
					{
						genInfo[1] = AnnoQuery.getUniGenName(genInfo[2]);
						genInfo[0] = "uniID";
						QBlastUni2GoInfo = getUni2GenGoInfo(genInfo, StaxID);
					}
				}
			}

			if (blast) {
				ArrayList<String[]> tmpInfo = GOQuery.copeBlastInfo(Qgene2GoInfo, Quni2GoInfo, QBlastGene2GoInfo, QBlastUni2GoInfo, blastInfo, evalue, GOClass, sepID, lsGene2Go, true);
				if (tmpInfo != null) {
					lsGene2GoInfo.addAll(tmpInfo);
					lsGo2Gene.addAll(GOQuery.copeBlastInfoSimple(Qgene2GoInfo, Quni2GoInfo, QBlastGene2GoInfo, QBlastUni2GoInfo, blastInfo, evalue, GOClass, sepID, null, true));
				}
			}
			else
			{
				ArrayList<String[]> tmpInfo = null;
				if (strings[0].equals("geneID")) 
				{
					tmpInfo = GOQuery.getGene2GoInfo(Qgene2GoInfo, lsGene2Go, sepID, GOClass);
				}
				else if(strings[0].equals("uniID"))
				{
					tmpInfo = GOQuery.getUni2GoInfo(Quni2GoInfo, lsGene2Go, sepID, GOClass);
				}
				if (tmpInfo != null) 
					lsGene2GoInfo.addAll(tmpInfo);
			}
		}
		ArrayList<ArrayList<String[]>> lsResult = new ArrayList<ArrayList<String[]>>();
		if (blast) {
			lsResult.add(lsGene2GoInfo);
			lsResult.add(lsGene2Go);
			lsResult.add(lsGo2Gene);
		}
		else {
			lsResult.add(lsGene2GoInfo);
			lsResult.add(lsGene2Go);
		}
		////////////////////////////////////////////////////////////////////////////////////////
		return lsResult;
	}
	
}
