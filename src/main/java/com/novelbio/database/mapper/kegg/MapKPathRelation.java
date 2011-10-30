package com.novelbio.database.mapper.kegg;

import java.util.ArrayList;

import org.apache.ibatis.session.SqlSession;

import com.novelbio.database.entity.kegg.KGpathRelation;
import com.novelbio.database.util.Util;

 
public class MapKPathRelation {
	/**
	 * if test="pathName !=null" <br>
				 pathName=#{pathName}<br>
			/if<br>
			if test="scrPath !=null"<br>
				and scrPath=#{scrPath}<br>
			/if<br>
			if test="trgPath !=null"<br>
				and trgPath=#{trgPath}<br>
			/if<br>
	 * @param kGpathRelation
	 * @return
	 */
	public static ArrayList<KGpathRelation> queryLskGpathRelations(KGpathRelation kGpathRelation){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
		ArrayList<KGpathRelation> lsKGpathRelations=null;
		try
		{
			lsKGpathRelations= (ArrayList<KGpathRelation>)session.selectList("KGMLSingle.selectPathRelation",kGpathRelation);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return lsKGpathRelations;
	}
	
	/**
	 * if test="pathName !=null" <br>
				 pathName=#{pathName}<br>
			/if<br>
			if test="scrPath !=null"<br>
				and scrPath=#{scrPath}<br>
			/if<br>
			if test="trgPath !=null"<br>
				and trgPath=#{trgPath}<br>
			/if<br>
	 * @param kGpathRelation
	 * @return
	 */
	public static KGpathRelation queryKGpathRelation(KGpathRelation kGpathRelation){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
		KGpathRelation kGpathRelation2=null;
		try
		{
			kGpathRelation2= (KGpathRelation)session.selectOne("KGMLSingle.selectPathRelation",kGpathRelation);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return kGpathRelation2;
	}
	
	
	public static void InsertKGpathRelation(KGpathRelation kGpathRelation){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
 		try
		{
			session.insert("KGMLSingle.insertPathRelation", kGpathRelation);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
	}
	
	/**
	 * 目前的升级方式是
		update substrate <br>
		set<br>
			if test="pathName !=null"<br>
				 pathName=#{pathName},<br>
			/if<br>
			if test="scrPath !=null"<br>
				scrPath=#{scrPath},<br>
			/if<br>
			if test="trgPath !=null"<br>
				trgPath=#{trgPath}<br>
			/if<br>
			if test="type !=null"<br>
				type=#{type}<br>
			/if<br>
		/set<br>
		where<br>
			if test="pathName !=null"<br>
				 pathName=#{pathName}<br>
			/if<br>
			if test="scrPath !=null"<br>
				and scrPath=#{scrPath}<br>
			/if<br>
			if test="trgPath !=null"<br>
				and trgPath=#{trgPath}<br>
			/if<br>
		/where<br>
	 * @param kGentry
	 */
	public static void upDateKGentry(KGpathRelation kGpathRelation){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
 		try
		{
			session.update("KGMLSingle.updatePathRelation", kGpathRelation);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
	}
}
