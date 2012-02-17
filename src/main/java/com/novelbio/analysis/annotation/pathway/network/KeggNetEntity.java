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
	 * 给定节点，用节点中的ArrayList-geneID和ArrayList-UniID查找KEGG，并获得相应的关系
	 * @param blast 是否进行blast
	 * @param StaxID 目标物种的taxID，只有当blast为true时才会起作用
	 * @param evalue 只有当blast为true时才会起作用
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
		 * 保存最后获得与之相关的entry信息
		 */
//		Hashtable<String, KGpathScr2Trg> hashKGpathRelations = new Hashtable<String, KGpathScr2Trg>();
		//如果这个entry是有parentID的，也就是一个component
		if (kegEntity.getParentID()>0)
		{
			////////如果有复合物，先查找entry中的复合物，将该复合物的其余复合物都找到//////////////////////////////////////////////////////////////////////////////////////////////////////////
			KGentry tmpqkGentry = new KGentry();//查询用的KGentry
			tmpqkGentry.setPathName(kegEntity.getPathName());
			tmpqkGentry.setParentID(kegEntity.getParentID());
			//这里都是与queryEntry是component的entry,
			ArrayList<KegEntity> lsSubKGentries=KegEntity.getLsEntity(tmpqkGentry);
			for (int i = 0; i < lsSubKGentries.size(); i++) 
			{
				KegEntity tmpkgGentrySub=lsSubKGentries.get(i);
				//一个pathway上的entry会有多个keggID对应上去，那么这时候相同entry的keggID--也就是同一个entry的不同的基因，之间不要有联系，所以跳过
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
			////////////////接下来分别用entryID和parentID去查找relation表，获得结果的entryID////////////////////////////////////////////////////////////////////////////////////////////////////
			////////////////////////////////////////////////首先用entryID去查找//////////////////////////////////////////////////////////////
			KGrelation tmpQkGrelation=new KGrelation();
			tmpQkGrelation.setEntry1ID(kegEntity.getID()); tmpQkGrelation.setPathName(kegEntity.getPathName());
			ArrayList<KGrelation> lsKGrelations = DaoKRealtion.queryLsKGrelations(tmpQkGrelation);
			/////////////////////////////////////////因为有些relation中有这种情况：entry1、entry2完全一致，就是subtype不一致，这个就是将一致的entry1、entry2去冗余的////////////////////////////////////////////////////////////////
			Hashtable<String, KGrelation> hashPathID2KGRelation = removeRep(lsKGrelations);
			
			Enumeration keys=hashPathID2KGRelation.keys();
			while(keys.hasMoreElements())
			{
				String key=(String)keys.nextElement();
				KGrelation kGrelation = hashPathID2KGRelation.get(key);
			
				//搜索该entry对应的所有实际entry--也就是不同的keggID-entry
				ArrayList<KGentry> lskGentriesSub = getRelateEntry(kGrelation.getEntry2ID(), kGrelation.getPathName());
				if ( lskGentriesSub == null || lskGentriesSub.size() == 0) 
				{
					System.out.println("用entryID"+kGrelation.getEntry2ID()+"在pathway"+ kGrelation.getPathName()+"中没找到对应的entry");
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
			//////////////////////然后用parentID去查///////////////////////////////////////////////////////////////////////////////////////
			KGrelation tmpQkGrelation2=new KGrelation();
			tmpQkGrelation2.setEntry1ID(kegEntity.getParentID()); tmpQkGrelation2.setPathName(kegEntity.getPathName());
			ArrayList<KGrelation> lsKGrelations2 = DaoKRealtion.queryLsKGrelations(tmpQkGrelation2);
			/////////////////////////////////////////因为有些relation中有这种情况：entry1、entry2完全一致，就是subtype不一致，这个就是将一致的entry1、entry2去冗余的////////////////////////////////////////////////////////////////
			Hashtable<String, KGrelation> hashPathID2KGRelation2 = removeRep(lsKGrelations2);
			
			Enumeration keys2=hashPathID2KGRelation2.keys();
			while(keys.hasMoreElements())
			{
				String key2=(String)keys2.nextElement();
				KGrelation kGrelation2 = hashPathID2KGRelation.get(key2);
			
				//搜索该entry对应的所有实际entry--也就是不同的keggID-entry
				ArrayList<KGentry> lskGentriesSub = getRelateEntry(kGrelation2.getEntry2ID(), kGrelation2.getPathName());
				if ( lskGentriesSub == null || lskGentriesSub.size() == 0)
				{
					System.out.println("用entryID"+kGrelation2.getEntry2ID()+"在pathway"+ kGrelation2.getPathName()+"中没找到对应的entry");
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
			////////////////////////////////////////////////直接用entryID去查找//////////////////////////////////////////////////////////////
			KGrelation tmpQkGrelation=new KGrelation();
			tmpQkGrelation.setEntry1ID(kegEntity.getID()); tmpQkGrelation.setPathName(kegEntity.getPathName());
			ArrayList<KGrelation> lsKGrelations = DaoKRealtion.queryLsKGrelations(tmpQkGrelation);
			/////////////////////////////////////////因为有些relation中有这种情况：entry1、entry2完全一致，就是subtype不一致(也就是一对entry1-entry2含有多个不同的subtype)，这个就是将一致的entry1、entry2去冗余的////////////////////////////////////////////////////////////////
			Hashtable<String, KGrelation> hashPathID2KGRelation = removeRep(lsKGrelations);
			Enumeration keys=hashPathID2KGRelation.keys();
			while(keys.hasMoreElements())
			{
				String key=(String)keys.nextElement();
				KGrelation kGrelation = hashPathID2KGRelation.get(key);
			
				//搜索该entry对应的所有实际entry--也就是不同的keggID-entry
				ArrayList<KGentry> lskGentriesSub = getRelateEntry(kGrelation.getEntry2ID(), kGrelation.getPathName());
				if ( lskGentriesSub == null || lskGentriesSub.size() == 0)
				{
					System.out.println("用entryID"+kGrelation.getEntry2ID()+"在pathway"+ kGrelation.getPathName()+"中没找到对应的entry");
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
