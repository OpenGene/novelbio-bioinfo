package com.novelbio.database.service;

import java.util.ArrayList;

import com.novelbio.analysis.annotation.copeID.CopedID;
import com.novelbio.analysis.annotation.pathway.kegg.pathEntity.KGen2Path;
import com.novelbio.analysis.annotation.pathway.kegg.pathEntity.KGng2Path;
import com.novelbio.database.DAO.KEGGDAO.MapKCdetail;
import com.novelbio.database.entity.friceDB.BlastInfo;
import com.novelbio.database.entity.friceDB.NCBIID;
import com.novelbio.database.entity.kegg.KGCgen2Entry;
import com.novelbio.database.entity.kegg.KGCgen2Ko;
import com.novelbio.database.entity.kegg.KGIDkeg2Ko;
import com.novelbio.database.entity.kegg.KGentry;
import com.novelbio.database.entity.kegg.noGene.KGNCompInfo;
import com.novelbio.database.entity.kegg.noGene.KGNIdKeg;
import com.novelbio.database.mapper.geneanno.MapBlastInfo;
import com.novelbio.database.mapper.kegg.MapKEntry;
import com.novelbio.database.mapper.kegg.MapKIDKeg2Ko;
import com.novelbio.database.mapper.kegg.MapKNCompInfo;

public class ServKegg {

	/**
	 * 
	 * ��ncbiidȥ�������ݿ⣬����øû����entry����Ҫ������pathway��enrichment
	 * <b>���ncbiid��geneID == 0</b>��˵����NCBIIDû��ֵ����ֱ�ӽ���blast����
	 * if test="geneID !=null and geneID !=0"
geneID=#{geneID}
/if
if test="taxID !=null and taxID !=0"
and taxID=#{taxID}
/if
	 * �����趨�Ƿ���Ҫ����blast�����������趨��blast�������������pathway�ǻ��ǲ�����blast
	 * @param ncbiid
	 * @param blast
	 * @param subTaxID ��Ҫ���ҵ�����
	 * @param evalue ֻ�е�blastΪtrueʱ�������ã���evalue<=�趨ֵʱ�Żῼ��blast��õ�KOֵ
	 */
	public static KGen2Path qKegPath(NCBIID ncbiid,boolean blast,int subTaxID,double evalue) 
	{
		KGen2Path kGen2Path = new KGen2Path();
		KGCgen2Entry kgCgen2Entry = null;
		if (ncbiid.getGeneId() != 0) {
			kgCgen2Entry=MapKCdetail.queryGen2entry(ncbiid);
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
			BlastInfo blastInfo=new BlastInfo(); 
			BlastInfo blastInfo2 = null;
			if (ncbiid.getGeneId() != 0) {
				blastInfo.setQueryID(ncbiid.getGeneId()+"");blastInfo.setSubjectTax(subTaxID);
				blastInfo2=MapBlastInfo.queryBlastInfo(blastInfo);
			}
			//��accID������һ��
			if (blastInfo2==null) {
				blastInfo.setQueryID(ncbiid.getAccID());
				blastInfo2=MapBlastInfo.queryBlastInfo(blastInfo);
			}
			//�����������,����blast��evalueС���趨ֵ
			if(blastInfo2!=null&&blastInfo2.getEvalue()<=evalue)
			{
				int queryTaxID=blastInfo2.getQueryTax();
				kGen2Path.setBlastInfo(blastInfo2);
				//��blast����geneIDȥ����kegg���ݿ�,���subject��KO��Ϣ
				NCBIID ncbiidSubject = new NCBIID();
				ncbiidSubject.setGeneId(Integer.parseInt(blastInfo2.getSubjectID()));
				KGCgen2Ko kgCgen2Ko = MapKCdetail.queryGen2Ko(ncbiidSubject);
				ArrayList<KGentry> lsKGentriesSubject=null;
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
						ArrayList<KGIDkeg2Ko> lsKgiDkeg2Kos2 = MapKIDKeg2Ko.queryLsKGIDkeg2Ko(kgiDkeg2Ko);
						if (lsKgiDkeg2Kos2 != null && lsKgiDkeg2Kos2.size()>0) 
						{
							//��Ȼһ��ko��Ӧ���keggID�����Ƕ���pathway��˵��һ��ko�Ͷ�Ӧ��һ��pathway�ϣ�����һ��ko�͹���
							String keggID = lsKgiDkeg2Kos2.get(0).getKeggID();//����Ǳ����е�KeggID�������KeggIDֱ�ӿ���������Ӧ��pathway
							kGentry.setEntryName(keggID);
							kGentry.setTaxID(queryTaxID);
							//�ڸ���ko��taxID������£�һ��ko���Բ�����pathway����һ��pathway��Ķ��entry
							ArrayList<KGentry> lskGentries=MapKEntry.queryLsKGentries(kGentry);
							for (int j = 0; j < lskGentries.size(); j++) {
								lsKGentriesSubject.add(lskGentries.get(j));
							}
						}
						/////////////���geneBlast�������࣬���ҵõ�����Ӧ��KO����ô��ø�KO����Ӧ�����ֵ�KeggID�����û��KeggID������KOmapping�ر�����//////////////////////////////////////////////////////////////////
						else
						{
							kGentry.setEntryName(ko);
							kGentry.setTaxID(queryTaxID);
							//�ڸ���ko��taxID������£�һ��ko���Բ�����pathway����һ��pathway��Ķ��entry
							ArrayList<KGentry> lskGentries=MapKEntry.queryLsKGentries(kGentry);
							for (int j = 0; j < lskGentries.size(); j++) 
							{
								lsKGentriesSubject.add(lskGentries.get(j));
							}
						}
					}
				}
				kGen2Path.setLsBlastgen2Entry(lsKGentriesSubject);
			}
		}
		return kGen2Path;
	}
	

	/**
	 * 
	 * ��kgnIdKegȥ�������ݿ⣬����øû����entry����Ҫ������pathway��enrichment
	 * kgnIdKeg�����ȷ���Ѿ����ڵ�KGNIdKeg
	 * �����趨�Ƿ���Ҫ����blast�����������趨��blast�������������pathway�ǻ��ǲ�����blast
	 * @param ncbiid
	 * @param blast
	 * @param subTaxID ��Ҫ���ҵ�����
	 * @param evalue ֻ�е�blastΪtrueʱ�������ã���evalue<=�趨ֵʱ�Żῼ��blast��õ�KOֵ
	 */
	public static KGng2Path qKegPath(int queryTax, KGNIdKeg kgnIdKeg) 
	{
		KGng2Path kGng2Path = new KGng2Path();
		kGng2Path.setKGNIdKeg(kgnIdKeg);
		KGNCompInfo kgnCompInfo = MapKNCompInfo.queryKGNCompInfo(kgnIdKeg);
		if (kgnCompInfo != null) {
			kGng2Path.setKgnCompInfo(kgnCompInfo);
		}
		KGentry kGentryq = new KGentry();
		////////���ݿ������⣬entry������ cpd:C00229����compound������C00229//////////////////////////////////////////////////////////
		kGentryq.setEntryName("cpd:"+kgnIdKeg.getKegID()); kGentryq.setTaxID(queryTax);
		ArrayList<KGentry> lsKGentryS = MapKEntry.queryLsKGentries(kGentryq);
		kGng2Path.setLsKGentry(lsKGentryS);
		return kGng2Path;
	}
	
}
