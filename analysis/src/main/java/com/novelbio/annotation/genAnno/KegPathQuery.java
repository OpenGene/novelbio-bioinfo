package com.novelbio.annotation.genAnno;

import java.util.ArrayList;
import java.util.HashSet;

import com.novelbio.annotation.pathway.kegg.pathEntity.KGen2Path;
import com.novelbio.database.DAO.FriceDAO.DaoFSBlastInfo;
import com.novelbio.database.DAO.FriceDAO.DaoFSNCBIID;
import com.novelbio.database.DAO.KEGGDAO.DaoKCdetail;
import com.novelbio.database.DAO.KEGGDAO.DaoKEntry;
import com.novelbio.database.DAO.KEGGDAO.DaoKIDKeg2Ko;
import com.novelbio.database.DAO.KEGGDAO.DaoKPathway;
import com.novelbio.database.entity.friceDB.BlastInfo;
import com.novelbio.database.entity.friceDB.NCBIID;
import com.novelbio.database.entity.kegg.KGCgen2Entry;
import com.novelbio.database.entity.kegg.KGCgen2Ko;
import com.novelbio.database.entity.kegg.KGIDkeg2Ko;
import com.novelbio.database.entity.kegg.KGentry;
import com.novelbio.database.entity.kegg.KGpathway;

public class KegPathQuery {
	
	
	
	
	
	/**
	 * ����accID����ѯ��accID����Ӧ��pathway
	 * Ŀǰֻ����NCBIID�в�ѯ��������UniProt�в�ѯ
	 * @param accID accID ��Ҫȥ�ո����Լ��ж�accID�Ƿ�Ϊ��
	 * @param taxID
	 * @param blast
	 * @param subTaxID
	 * @param evalue
	 * @return ���û�鵽�򷵻�null
	 * ���blast��
	 * 0:accID
	 * 1:Symbol/AccID
	 * 2:PathID
	 * 3:PathName
	 * 4:evalue
	 * 5:SubjectSymbol
	 * 6:PathID
	 * 7:PathName
	 * ���û��blast
	 * 0:accID
	 * 1:Symbol/AccID
	 * 2:PathID
	 * 3:PathName
	 */
	public static ArrayList<String[]> getGenPath(String accID,int taxID,boolean blast,int subTaxID,double evalue)
	{
		//����ȥ�ظ��ģ���Ϊһ��keggID������һ��pathway�г��ֶ��
		HashSet<String> hashPahID = new HashSet<String>();
		ArrayList<String[]> lsPathInfo = new ArrayList<String[]>();
		
		String[] anno = AnnoQuery.getAnno(accID, taxID, blast, subTaxID, evalue);
		NCBIID ncbiid = new NCBIID();
		ncbiid.setAccID(accID); ncbiid.setTaxID(taxID);
		ArrayList<NCBIID> lsNcbiids = DaoFSNCBIID.queryLsNCBIID(ncbiid);
		if (lsNcbiids != null && lsNcbiids.size()>1) {
			ncbiid = lsNcbiids.get(0);
		}
		KGen2Path kGen2Path = qKegPath(ncbiid, blast, subTaxID, evalue);
		String[] resultsPath;
		//��������û���ҵ���pathway����ô����null
		if ((kGen2Path.getKGCgen2Entry() == null || kGen2Path.getKGCgen2Entry().getLsKGentries()==null || kGen2Path.getKGCgen2Entry().getLsKGentries().size()<1) && (kGen2Path.getLsBlastgen2Entry() == null || kGen2Path.getLsBlastgen2Entry().size()<1)) {
			return null;
		}
		if (blast)
		{
			//accID����pathway
			if (kGen2Path.getKGCgen2Entry() !=null && kGen2Path.getKGCgen2Entry().getLsKGentries() != null &&  kGen2Path.getKGCgen2Entry().getLsKGentries().size() >0) 
			{
				ArrayList<KGentry> lsKGentries = kGen2Path.getKGCgen2Entry().getLsKGentries();
				for (KGentry kGentry : lsKGentries) {
					resultsPath = new String[8];
					resultsPath[0] = accID;
					resultsPath[1] = anno[0];
					resultsPath[2] = kGentry.getPathName();
					resultsPath[3] = getPathTitle(resultsPath[2]);
					resultsPath[4] = "";
					resultsPath[5] = "";
					resultsPath[6] = "";
					resultsPath[7] = "";
					if (hashPahID.contains(resultsPath[2])) {
						continue;
					}
					hashPahID.add(resultsPath[2]);
					lsPathInfo.add(resultsPath);
					
				}
				return lsPathInfo;
			}
			else if (kGen2Path.getLsBlastgen2Entry() != null && kGen2Path.getLsBlastgen2Entry().size() >0) {
				ArrayList<KGentry> lsKGentries = kGen2Path.getLsBlastgen2Entry();
				for (KGentry kGentry : lsKGentries) {
					resultsPath = new String[8];
					resultsPath[0] = accID;
					resultsPath[1] = anno[0];
					resultsPath[2] = "";
					resultsPath[3] = "";
					resultsPath[4] = kGen2Path.getBlastInfo().getEvalue()+"";
					resultsPath[5] = anno[4];
					resultsPath[6] = kGentry.getPathName();
					resultsPath[7] = getPathTitle(resultsPath[6]);
					if (hashPahID.contains(resultsPath[6])) {
						continue;
					}
					hashPahID.add(resultsPath[6]);
					lsPathInfo.add(resultsPath);
				}
				return lsPathInfo;
			}
		}
		else
		{
			//accID����pathway
			if (kGen2Path.getKGCgen2Entry() !=null && kGen2Path.getKGCgen2Entry().getLsKGentries() != null &&  kGen2Path.getKGCgen2Entry().getLsKGentries().size() >0) 
			{
				resultsPath = new String[4];
				ArrayList<KGentry> lsKGentries = kGen2Path.getKGCgen2Entry().getLsKGentries();
				for (KGentry kGentry : lsKGentries) {
					resultsPath = new String[4];
					resultsPath[0] = accID;
					resultsPath[1] = anno[0];
					resultsPath[2] = kGentry.getPathName();
					resultsPath[3] = getPathTitle(resultsPath[2]);
					lsPathInfo.add(resultsPath);
				}
				return lsPathInfo;
			}
		}
		return null;
	}
	
	/**
	 * ����pathID������pathway��title
	 * @return
	 */
	public static String getPathTitle(String pathID) {
		KGpathway kGpathway = new KGpathway();
		kGpathway.setPathName(pathID.trim());
		kGpathway = DaoKPathway.queryKGpathway(kGpathway);
		return kGpathway.getTitle();
	}
	
	
	
	
	
	/**
	 * 
	 * ��ncbiidȥ�������ݿ⣬����øû����entry����Ҫ������pathway��enrichment
	 * if test="geneID !=null and geneID !=0"
geneID=#{geneID}
/if
if test="taxID !=null and taxID !=0"
and taxID=#{taxID}
/if
	 * �����趨�Ƿ���Ҫ����blast�����������趨��blast�������������pathway�ǻ��ǲ�����blast
	 * @param ncbiid �����ͬʱ���� accID��geneID����
	 * @param blast
	 * @param subTaxID ��Ҫ���ҵ�����
	 * @param evalue ֻ�е�blastΪtrueʱ�������ã���evalue<=�趨ֵʱ�Żῼ��blast��õ�KOֵ
	 */
	public static KGen2Path qKegPath(NCBIID ncbiid,boolean blast,int subTaxID,double evalue) 
	{
		KGen2Path kGen2Path = new KGen2Path();
		KGCgen2Entry kgCgen2Entry = null;
		if (ncbiid.getGeneId() != 0) {
			kgCgen2Entry=DaoKCdetail.queryGen2entry(ncbiid);
		}
		kGen2Path.setKGCgen2Entry(kgCgen2Entry);
		//�����������pathway�ǾͲ�����blast
		if (!blast ||   
				(kGen2Path.getKGCgen2Entry()!=null
						&&kGen2Path.getKGCgen2Entry().getLsKGentries()!=null
						     &&kGen2Path.getKGCgen2Entry().getLsKGentries().size()>0))
		{
			return kGen2Path;
		}
		else 
		{
			/////////////////////////////////////����geneID����blast���ݿ⣬���еĻ���accID���������Ѳ�����û����////////////////////////////
			BlastInfo blastInfo = new BlastInfo();
			BlastInfo blastInfo2 = null;
			if (ncbiid.getGeneId() != 0) {
				blastInfo.setQueryID(ncbiid.getGeneId()+"");blastInfo.setSubjectTax(subTaxID);
				blastInfo2=DaoFSBlastInfo.queryBlastInfo(blastInfo);
			}
			
			//��accID������һ��
			if (blastInfo2==null) {
				blastInfo.setQueryID(ncbiid.getAccID());
				blastInfo2=DaoFSBlastInfo.queryBlastInfo(blastInfo);
			}
			//�����������,����blast��evalueС���趨ֵ
			if(blastInfo2!=null&&blastInfo2.getEvalue()<=evalue)
			{
				int queryTaxID=blastInfo2.getQueryTax();
				kGen2Path.setBlastInfo(blastInfo2);
				//��blast����geneIDȥ����kegg���ݿ�,���subject��KO��Ϣ
				NCBIID ncbiidSubject = new NCBIID();
				ncbiidSubject.setGeneId(Integer.parseInt(blastInfo2.getSubjectID()));
				KGCgen2Ko kgCgen2Ko = DaoKCdetail.queryGen2Ko(ncbiidSubject);
				ArrayList<KGentry> lsKGentriesSubject = null;
				//����ҵ�ko��
				if (kgCgen2Ko!=null
						&&kgCgen2Ko.getLsKgiDkeg2Kos()!=null
						&&kgCgen2Ko.getLsKgiDkeg2Kos().size()>0)
				{
					kGen2Path.setKegIDSubject(kgCgen2Ko.getKegID());
					//�����汣����һ��keggID��Ӧ������ko
					ArrayList<KGIDkeg2Ko> lsKgiDkeg2Kos = kgCgen2Ko.getLsKgiDkeg2Kos();
					//�洢����������溬�ж���kegg��entry��Ϣ��Ҳ���൱��pathway
					lsKGentriesSubject=new ArrayList<KGentry>();
					for (int i = 0; i < lsKgiDkeg2Kos.size(); i++)
					{
						KGentry kGentry=new KGentry();
						String ko = lsKgiDkeg2Kos.get(i).getKo();
						////////////////���geneBlast�������࣬���ҵõ�����Ӧ��KO����ô��ø�KO����Ӧ�����ֵ�KeggID������KeggIDֱ��mapping�ر�����////////////////////////////////////////////////////////////////
						KGIDkeg2Ko kgiDkeg2Ko = new KGIDkeg2Ko();
						kgiDkeg2Ko.setKo(ko);kgiDkeg2Ko.setTaxID(queryTaxID);
						//��KOȥ����KeggID
						ArrayList<KGIDkeg2Ko> lsKgiDkeg2Kos2 = DaoKIDKeg2Ko.queryLsKGIDkeg2Ko(kgiDkeg2Ko);
						//����ѵ���KeggID����ô��KeggIDȥ��entry
						if (lsKgiDkeg2Kos2 != null && lsKgiDkeg2Kos2.size()>0) 
						{
							//��Ȼһ��ko��Ӧ���keggID�����Ƕ���pathway��˵��һ��ko�Ͷ�Ӧ��һ��pathway�ϣ�����һ��ko�͹���
							String keggID = lsKgiDkeg2Kos2.get(0).getKeggID();//����Ǳ����е�KeggID�������KeggIDֱ�ӿ���������Ӧ��pathway
							kGentry.setEntryName(keggID);
							kGentry.setTaxID(queryTaxID);
							//�ڸ���ko��taxID������£�һ��ko���Բ�����pathway����һ��pathway��Ķ��entry
							ArrayList<KGentry> lskGentries=DaoKEntry.queryLsKGentries(kGentry);
							lsKGentriesSubject.addAll(lskGentries);
						}
						/////////////���geneBlast�������࣬���ҵõ�����Ӧ��KO����ô��ø�KO����Ӧ�����ֵ�KeggID�����û��KeggID������KOȥ�ұ������entry//////////////////////////////////////////////////////////////////
						else
						{
							kGentry.setEntryName(ko);
							kGentry.setTaxID(queryTaxID);
							//�ڸ���ko��taxID������£�һ��ko���Բ�����pathway����һ��pathway��Ķ��entry
							ArrayList<KGentry> lskGentries=DaoKEntry.queryLsKGentries(kGentry);
							lsKGentriesSubject.addAll(lskGentries);
						}
					}
				}
				kGen2Path.setLsBlastgen2Entry(lsKGentriesSubject);
			}
		}
		return kGen2Path;
	}
	
}
