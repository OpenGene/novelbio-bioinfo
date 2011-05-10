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
	 * 给定accID，查询该accID所对应的pathway
	 * 目前只能在NCBIID中查询，不能在UniProt中查询
	 * @param accID accID 需要去空格处理以及判断accID是否为空
	 * @param taxID
	 * @param blast
	 * @param subTaxID
	 * @param evalue
	 * @return 如果没查到则返回null
	 * 如果blast：
	 * 0:accID
	 * 1:Symbol/AccID
	 * 2:PathID
	 * 3:PathName
	 * 4:evalue
	 * 5:SubjectSymbol
	 * 6:PathID
	 * 7:PathName
	 * 如果没有blast
	 * 0:accID
	 * 1:Symbol/AccID
	 * 2:PathID
	 * 3:PathName
	 */
	public static ArrayList<String[]> getGenPath(String accID,int taxID,boolean blast,int subTaxID,double evalue)
	{
		//用来去重复的，因为一个keggID可能在一个pathway中出现多次
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
		//如果结果中没有找到的pathway，那么返回null
		if ((kGen2Path.getKGCgen2Entry() == null || kGen2Path.getKGCgen2Entry().getLsKGentries()==null || kGen2Path.getKGCgen2Entry().getLsKGentries().size()<1) && (kGen2Path.getLsBlastgen2Entry() == null || kGen2Path.getLsBlastgen2Entry().size()<1)) {
			return null;
		}
		if (blast)
		{
			//accID存在pathway
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
			//accID存在pathway
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
	 * 给定pathID，返回pathway的title
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
	 * 用ncbiid去查找数据库，最后获得该基因的entry，主要用于做pathway的enrichment
	 * if test="geneID !=null and geneID !=0"
geneID=#{geneID}
/if
if test="taxID !=null and taxID !=0"
and taxID=#{taxID}
/if
	 * 可以设定是否需要进行blast，不过就算设定了blast，如果本基因含有pathway那还是不进行blast
	 * @param ncbiid 最好能同时含有 accID和geneID两项
	 * @param blast
	 * @param subTaxID 需要查找的物种
	 * @param evalue 只有当blast为true时才起作用，当evalue<=设定值时才会考虑blast获得的KO值
	 */
	public static KGen2Path qKegPath(NCBIID ncbiid,boolean blast,int subTaxID,double evalue) 
	{
		KGen2Path kGen2Path = new KGen2Path();
		KGCgen2Entry kgCgen2Entry = null;
		if (ncbiid.getGeneId() != 0) {
			kgCgen2Entry=DaoKCdetail.queryGen2entry(ncbiid);
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
			BlastInfo blastInfo = new BlastInfo();
			BlastInfo blastInfo2 = null;
			if (ncbiid.getGeneId() != 0) {
				blastInfo.setQueryID(ncbiid.getGeneId()+"");blastInfo.setSubjectTax(subTaxID);
				blastInfo2=DaoFSBlastInfo.queryBlastInfo(blastInfo);
			}
			
			//用accID再搜索一次
			if (blastInfo2==null) {
				blastInfo.setQueryID(ncbiid.getAccID());
				blastInfo2=DaoFSBlastInfo.queryBlastInfo(blastInfo);
			}
			//如果搜索到了,并且blast的evalue小于设定值
			if(blastInfo2!=null&&blastInfo2.getEvalue()<=evalue)
			{
				int queryTaxID=blastInfo2.getQueryTax();
				kGen2Path.setBlastInfo(blastInfo2);
				//用blast到的geneID去搜索kegg数据库,获得subject的KO信息
				NCBIID ncbiidSubject = new NCBIID();
				ncbiidSubject.setGeneId(Integer.parseInt(blastInfo2.getSubjectID()));
				KGCgen2Ko kgCgen2Ko = DaoKCdetail.queryGen2Ko(ncbiidSubject);
				ArrayList<KGentry> lsKGentriesSubject = null;
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
						//用KO去搜索KeggID
						ArrayList<KGIDkeg2Ko> lsKgiDkeg2Kos2 = DaoKIDKeg2Ko.queryLsKGIDkeg2Ko(kgiDkeg2Ko);
						//如果搜到了KeggID，那么将KeggID去找entry
						if (lsKgiDkeg2Kos2 != null && lsKgiDkeg2Kos2.size()>0) 
						{
							//虽然一个ko对应多个keggID，但是对于pathway来说，一个ko就对应到一个pathway上，所以一个ko就够了
							String keggID = lsKgiDkeg2Kos2.get(0).getKeggID();//这就是本物中的KeggID，用这个KeggID直接可以搜索相应的pathway
							kGentry.setEntryName(keggID);
							kGentry.setTaxID(queryTaxID);
							//在给定ko和taxID的情况下，一个ko可以参与多个pathway，和一个pathway里的多个entry
							ArrayList<KGentry> lskGentries=DaoKEntry.queryLsKGentries(kGentry);
							lsKGentriesSubject.addAll(lskGentries);
						}
						/////////////如果geneBlast到了人类，并且得到了相应的KO，那么获得该KO所对应本物种的KeggID，如果没有KeggID，则用KO去找本基因的entry//////////////////////////////////////////////////////////////////
						else
						{
							kGentry.setEntryName(ko);
							kGentry.setTaxID(queryTaxID);
							//在给定ko和taxID的情况下，一个ko可以参与多个pathway，和一个pathway里的多个entry
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
