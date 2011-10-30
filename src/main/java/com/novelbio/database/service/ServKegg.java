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
	 * 用ncbiid去查找数据库，最后获得该基因的entry，主要用于做pathway的enrichment
	 * <b>如果ncbiid中geneID == 0</b>，说明该NCBIID没有值，会直接进入blast步骤
	 * if test="geneID !=null and geneID !=0"
geneID=#{geneID}
/if
if test="taxID !=null and taxID !=0"
and taxID=#{taxID}
/if
	 * 可以设定是否需要进行blast，不过就算设定了blast，如果本基因含有pathway那还是不进行blast
	 * @param ncbiid
	 * @param blast
	 * @param subTaxID 需要查找的物种
	 * @param evalue 只有当blast为true时才起作用，当evalue<=设定值时才会考虑blast获得的KO值
	 */
	public static KGen2Path qKegPath(NCBIID ncbiid,boolean blast,int subTaxID,double evalue) 
	{
		KGen2Path kGen2Path = new KGen2Path();
		KGCgen2Entry kgCgen2Entry = null;
		if (ncbiid.getGeneId() != 0) {
			kgCgen2Entry=MapKCdetail.queryGen2entry(ncbiid);
		}
		kGen2Path.setKGCgen2Entry(kgCgen2Entry);
		//如果本基因含有pathway那就不进行blast
		if (!blast ||   
				(kGen2Path.getKGCgen2Entry()!=null
						&&kGen2Path.getKGCgen2Entry().getLsKGentries()!=null
						     &&kGen2Path.getKGCgen2Entry().getLsKGentries().size()>0))
		{
			return kGen2Path;
		}
		else 
		{
			/////////////////////////////////////先用geneID搜索blast数据库，不行的话用accID搜索，还搜不到就没有了////////////////////////////
			BlastInfo blastInfo=new BlastInfo(); 
			BlastInfo blastInfo2 = null;
			if (ncbiid.getGeneId() != 0) {
				blastInfo.setQueryID(ncbiid.getGeneId()+"");blastInfo.setSubjectTax(subTaxID);
				blastInfo2=MapBlastInfo.queryBlastInfo(blastInfo);
			}
			//用accID再搜索一次
			if (blastInfo2==null) {
				blastInfo.setQueryID(ncbiid.getAccID());
				blastInfo2=MapBlastInfo.queryBlastInfo(blastInfo);
			}
			//如果搜索到了,并且blast的evalue小于设定值
			if(blastInfo2!=null&&blastInfo2.getEvalue()<=evalue)
			{
				int queryTaxID=blastInfo2.getQueryTax();
				kGen2Path.setBlastInfo(blastInfo2);
				//用blast到的geneID去搜索kegg数据库,获得subject的KO信息
				NCBIID ncbiidSubject = new NCBIID();
				ncbiidSubject.setGeneId(Integer.parseInt(blastInfo2.getSubjectID()));
				KGCgen2Ko kgCgen2Ko = MapKCdetail.queryGen2Ko(ncbiidSubject);
				ArrayList<KGentry> lsKGentriesSubject=null;
				//如果找到ko了
				if (kgCgen2Ko!=null
						&&kgCgen2Ko.getLsKgiDkeg2Kos()!=null
						&&kgCgen2Ko.getLsKgiDkeg2Kos().size()>0)
				{
					kGen2Path.setKegIDSubject(kgCgen2Ko.getKegID());
					//这里面保存了一个keggID对应的所有ko
					ArrayList<KGIDkeg2Ko> lsKgiDkeg2Kos = kgCgen2Ko.getLsKgiDkeg2Kos();
					//存储结果基因里面含有多少kegg的entry信息，也就相当于pathway
					lsKGentriesSubject=new ArrayList<KGentry>();
					for (int i = 0; i < lsKgiDkeg2Kos.size(); i++)
					{
						KGentry kGentry=new KGentry();
						String ko = lsKgiDkeg2Kos.get(i).getKo();
						////////////////如果geneBlast到了人类，并且得到了相应的KO，那么获得该KO所对应本物种的KeggID，并用KeggID直接mapping回本基因////////////////////////////////////////////////////////////////
						KGIDkeg2Ko kgiDkeg2Ko = new KGIDkeg2Ko();
						kgiDkeg2Ko.setKo(ko);kgiDkeg2Ko.setTaxID(queryTaxID);
						ArrayList<KGIDkeg2Ko> lsKgiDkeg2Kos2 = MapKIDKeg2Ko.queryLsKGIDkeg2Ko(kgiDkeg2Ko);
						if (lsKgiDkeg2Kos2 != null && lsKgiDkeg2Kos2.size()>0) 
						{
							//虽然一个ko对应多个keggID，但是对于pathway来说，一个ko就对应到一个pathway上，所以一个ko就够了
							String keggID = lsKgiDkeg2Kos2.get(0).getKeggID();//这就是本物中的KeggID，用这个KeggID直接可以搜索相应的pathway
							kGentry.setEntryName(keggID);
							kGentry.setTaxID(queryTaxID);
							//在给定ko和taxID的情况下，一个ko可以参与多个pathway，和一个pathway里的多个entry
							ArrayList<KGentry> lskGentries=MapKEntry.queryLsKGentries(kGentry);
							for (int j = 0; j < lskGentries.size(); j++) {
								lsKGentriesSubject.add(lskGentries.get(j));
							}
						}
						/////////////如果geneBlast到了人类，并且得到了相应的KO，那么获得该KO所对应本物种的KeggID，如果没有KeggID，则用KOmapping回本基因//////////////////////////////////////////////////////////////////
						else
						{
							kGentry.setEntryName(ko);
							kGentry.setTaxID(queryTaxID);
							//在给定ko和taxID的情况下，一个ko可以参与多个pathway，和一个pathway里的多个entry
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
	 * 用kgnIdKeg去查找数据库，最后获得该基因的entry，主要用于做pathway的enrichment
	 * kgnIdKeg最好是确定已经存在的KGNIdKeg
	 * 可以设定是否需要进行blast，不过就算设定了blast，如果本基因含有pathway那还是不进行blast
	 * @param ncbiid
	 * @param blast
	 * @param subTaxID 需要查找的物种
	 * @param evalue 只有当blast为true时才起作用，当evalue<=设定值时才会考虑blast获得的KO值
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
		////////数据库有问题，entry里面是 cpd:C00229，而compound里面是C00229//////////////////////////////////////////////////////////
		kGentryq.setEntryName("cpd:"+kgnIdKeg.getKegID()); kGentryq.setTaxID(queryTax);
		ArrayList<KGentry> lsKGentryS = MapKEntry.queryLsKGentries(kGentryq);
		kGng2Path.setLsKGentry(lsKGentryS);
		return kGng2Path;
	}
	
}
