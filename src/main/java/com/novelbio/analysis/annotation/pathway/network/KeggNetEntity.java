package com.novelbio.analysis.annotation.pathway.network;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import com.novelbio.analysis.annotation.copeID.CopedID;
import com.novelbio.analysis.annotation.network.AbsNetEntity;
import com.novelbio.analysis.annotation.network.AbsNetRelate;
import com.novelbio.analysis.annotation.pathway.kegg.pathEntity.KegEntity;
import com.novelbio.database.DAO.KEGGDAO.DaoKEntry;
import com.novelbio.database.DAO.KEGGDAO.DaoKRealtion;
import com.novelbio.database.entity.friceDB.NCBIID;
import com.novelbio.database.entity.kegg.KGentry;
import com.novelbio.database.entity.kegg.KGrelation;

public class KeggNetEntity extends AbsNetEntity{
	String keggID = "";

	/**
	 * 
	 * �����ڵ㣬�ýڵ��е�ArrayList-geneID��ArrayList-UniID����KEGG���������Ӧ�Ĺ�ϵ
	 * @param blast �Ƿ����blast
	 * @param StaxID Ŀ�����ֵ�taxID��ֻ�е�blastΪtrueʱ�Ż�������
	 * @param evalue ֻ�е�blastΪtrueʱ�Ż�������
	 * @return
	 */
	@Override
	public ArrayList<AbsNetRelate> getRelate(boolean blast, int StaxID, double evalue) {
		ArrayList<AbsNetRelate> lsKegNetRelates = new ArrayList<AbsNetRelate>();
		for (CopedID copedID : hashCopedIDs) {
			ArrayList<KegEntity> lsKegEntities = copedID.getKegEntity(blast, StaxID, evalue);
			
			
			
			
			
		}
		return null;
	}
	
	private ArrayList<AbsNetRelate> getRelate(KegEntity kegEntity)
	{
		ArrayList<AbsNetRelate> lsKegNetRelates = new ArrayList<AbsNetRelate>();
		
		AbsNetRelate kegNetRelate = new KegNetRelate();
		/**
		 * �����������֮��ص�entry��Ϣ
		 */
//		Hashtable<String, KGpathScr2Trg> hashKGpathRelations = new Hashtable<String, KGpathScr2Trg>();
		//������entry����parentID�ģ�Ҳ����һ��component
		if (kegEntity.getParentID()>0)
		{
			////////����и�����Ȳ���entry�еĸ�������ø���������ิ���ﶼ�ҵ�//////////////////////////////////////////////////////////////////////////////////////////////////////////
			KGentry tmpqkGentry = new KGentry();//��ѯ�õ�KGentry
			tmpqkGentry.setPathName(kegEntity.getPathName());
			tmpqkGentry.setParentID(kegEntity.getParentID());
			//���ﶼ����queryEntry��component��entry,
			ArrayList<KegEntity> lsSubKGentries=KegEntity.getLsEntity(tmpqkGentry);
			for (int i = 0; i < lsSubKGentries.size(); i++) 
			{
				KegEntity tmpkgGentrySub=lsSubKGentries.get(i);
				//һ��pathway�ϵ�entry���ж��keggID��Ӧ��ȥ����ô��ʱ����ͬentry��keggID--Ҳ����ͬһ��entry�Ĳ�ͬ�Ļ���֮�䲻Ҫ����ϵ����������
				if (tmpkgGentrySub.getID() == kegEntity.getID())
				{
					continue;
				}
				KGpathScr2Trg kGpathRelation = new KGpathScr2Trg();
				kGpathRelation.setQKGentry(kegEntity);
				kGpathRelation.setSKGentry(tmpkgGentrySub);
				kGpathRelation.setType("component");
				kGpathRelation.setPathName(tmpkgGentrySub.getPathName());
				addHashKGpathRelation(hashKGpathRelations,kGpathRelation);
			}
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
			{
			////////////////�������ֱ���entryID��parentIDȥ����relation����ý����entryID////////////////////////////////////////////////////////////////////////////////////////////////////
			////////////////////////////////////////////////������entryIDȥ����//////////////////////////////////////////////////////////////
			KGrelation tmpQkGrelation=new KGrelation();
			tmpQkGrelation.setEntry1ID(kegEntity.getID()); tmpQkGrelation.setPathName(kegEntity.getPathName());
			ArrayList<KGrelation> lsKGrelations = DaoKRealtion.queryLsKGrelations(tmpQkGrelation);
			/////////////////////////////////////////��Ϊ��Щrelation�������������entry1��entry2��ȫһ�£�����subtype��һ�£�������ǽ�һ�µ�entry1��entry2ȥ�����////////////////////////////////////////////////////////////////
			Hashtable<String, KGrelation> hashPathID2KGRelation = removeRep(lsKGrelations);
			
			Enumeration keys=hashPathID2KGRelation.keys();
			while(keys.hasMoreElements())
			{
				String key=(String)keys.nextElement();
				KGrelation kGrelation = hashPathID2KGRelation.get(key);
			
				//������entry��Ӧ������ʵ��entry--Ҳ���ǲ�ͬ��keggID-entry
				ArrayList<KGentry> lskGentriesSub = getRelateEntry(kGrelation.getEntry2ID(), kGrelation.getPathName());
				if ( lskGentriesSub == null || lskGentriesSub.size() == 0) 
				{
					System.out.println("��entryID"+kGrelation.getEntry2ID()+"��pathway"+ kGrelation.getPathName()+"��û�ҵ���Ӧ��entry");
				}
				for (int j = 0; j < lskGentriesSub.size(); j++)
				{
					KGpathScr2Trg kGpathRelation = new KGpathScr2Trg();
					kGpathRelation.setQKGentry(kegEntity);
					kGpathRelation.setSKGentry(lskGentriesSub.get(j));
					kGpathRelation.setType(kGrelation.getType());
					String[] tmpSubInfo = new String[2];
					tmpSubInfo[0] = kGrelation.getSubtypeName();
					tmpSubInfo[1] = kGrelation.getSubtypeValue();
					kGpathRelation.setSubtypeInfo(tmpSubInfo);
					kGpathRelation.setPathName(lskGentriesSub.get(j).getPathName());
					addHashKGpathRelation(hashKGpathRelations,kGpathRelation);
					
				}
			}
			//////////////////////Ȼ����parentIDȥ��///////////////////////////////////////////////////////////////////////////////////////
			KGrelation tmpQkGrelation2=new KGrelation();
			tmpQkGrelation2.setEntry1ID(kegEntity.getParentID()); tmpQkGrelation2.setPathName(kegEntity.getPathName());
			ArrayList<KGrelation> lsKGrelations2 = DaoKRealtion.queryLsKGrelations(tmpQkGrelation2);
			/////////////////////////////////////////��Ϊ��Щrelation�������������entry1��entry2��ȫһ�£�����subtype��һ�£�������ǽ�һ�µ�entry1��entry2ȥ�����////////////////////////////////////////////////////////////////
			Hashtable<String, KGrelation> hashPathID2KGRelation2 = removeRep(lsKGrelations2);
			
			Enumeration keys2=hashPathID2KGRelation2.keys();
			while(keys.hasMoreElements())
			{
				String key2=(String)keys2.nextElement();
				KGrelation kGrelation2 = hashPathID2KGRelation.get(key2);
			
				//������entry��Ӧ������ʵ��entry--Ҳ���ǲ�ͬ��keggID-entry
				ArrayList<KGentry> lskGentriesSub = getRelateEntry(kGrelation2.getEntry2ID(), kGrelation2.getPathName());
				if ( lskGentriesSub == null || lskGentriesSub.size() == 0)
				{
					System.out.println("��entryID"+kGrelation2.getEntry2ID()+"��pathway"+ kGrelation2.getPathName()+"��û�ҵ���Ӧ��entry");
				}
				for (int j = 0; j < lskGentriesSub.size(); j++)
				{
					KGpathScr2Trg kGpathRelation = new KGpathScr2Trg();
					kGpathRelation.setQKGentry(kegEntity);
					kGpathRelation.setSKGentry(lskGentriesSub.get(j));
					kGpathRelation.setType(kGrelation2.getType());
					String[] tmpSubInfo = new String[2];
					tmpSubInfo[0] = kGrelation2.getSubtypeName();
					tmpSubInfo[1] = kGrelation2.getSubtypeValue();
					kGpathRelation.setSubtypeInfo(tmpSubInfo);
					kGpathRelation.setPathName(lskGentriesSub.get(j).getPathName());
					addHashKGpathRelation(hashKGpathRelations,kGpathRelation);
				}
			}
		}
		else
		{
			////////////////////////////////////////////////ֱ����entryIDȥ����//////////////////////////////////////////////////////////////
			KGrelation tmpQkGrelation=new KGrelation();
			tmpQkGrelation.setEntry1ID(kegEntity.getID()); tmpQkGrelation.setPathName(kegEntity.getPathName());
			ArrayList<KGrelation> lsKGrelations = DaoKRealtion.queryLsKGrelations(tmpQkGrelation);
			/////////////////////////////////////////��Ϊ��Щrelation�������������entry1��entry2��ȫһ�£�����subtype��һ��(Ҳ����һ��entry1-entry2���ж����ͬ��subtype)��������ǽ�һ�µ�entry1��entry2ȥ�����////////////////////////////////////////////////////////////////
			Hashtable<String, KGrelation> hashPathID2KGRelation = removeRep(lsKGrelations);
			Enumeration keys=hashPathID2KGRelation.keys();
			while(keys.hasMoreElements())
			{
				String key=(String)keys.nextElement();
				KGrelation kGrelation = hashPathID2KGRelation.get(key);
			
				//������entry��Ӧ������ʵ��entry--Ҳ���ǲ�ͬ��keggID-entry
				ArrayList<KGentry> lskGentriesSub = getRelateEntry(kGrelation.getEntry2ID(), kGrelation.getPathName());
				if ( lskGentriesSub == null || lskGentriesSub.size() == 0)
				{
					System.out.println("��entryID"+kGrelation.getEntry2ID()+"��pathway"+ kGrelation.getPathName()+"��û�ҵ���Ӧ��entry");
				}
				for (int j = 0; j < lskGentriesSub.size(); j++)
				{
					KGpathScr2Trg kGpathRelation = new KGpathScr2Trg();
					kGpathRelation.setQKGentry(kegEntity);
					kGpathRelation.setSKGentry(lskGentriesSub.get(j));
					kGpathRelation.setType(kGrelation.getType());
					String[] tmpSubInfo = new String[2];
					tmpSubInfo[0] = kGrelation.getSubtypeName();
					tmpSubInfo[1] = kGrelation.getSubtypeValue();
					kGpathRelation.setSubtypeInfo(tmpSubInfo);
					kGpathRelation.setPathName(lskGentriesSub.get(j).getPathName());
					addHashKGpathRelation(hashKGpathRelations,kGpathRelation);
				}
			}
		}
	}
	
}
