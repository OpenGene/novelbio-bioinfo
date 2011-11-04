package com.novelbio.database.mapper.kegg;

import java.util.ArrayList;

import org.apache.ibatis.session.SqlSession;

import com.novelbio.database.domain.kegg.KGrelation;
import com.novelbio.database.util.Util;


public class MapKRealtion {

	
	/**
	 * 用pathname,entry1,entry2,type,sybtypeName中任意组合去查找relation表
	 * @param kGrelation
	 * @return
	 */
	public static ArrayList<KGrelation> queryLsKGrelations(KGrelation kGrelation){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
		ArrayList<KGrelation> lsKGrelations=null;
		try
		{
			lsKGrelations= (ArrayList<KGrelation>)session.selectList("KGMLSingle.selectRelation",kGrelation);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return lsKGrelations;
	}
	
	/**
	 * 用pathname,entry1,entry2,type,sybtypeName中任意组合去查找relation表
	 * @param kGrelation
	 * @return
	 */
	public static KGrelation queryKGrelation(KGrelation kGrelation){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
		KGrelation kGrelation2=null;
		try
		{
			kGrelation2= (KGrelation)session.selectOne("KGMLSingle.selectRelation",kGrelation);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return kGrelation2;
	}
	
	
	public static void InsertKGrelation(KGrelation kGrelation){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
 		try
		{
			session.insert("KGMLSingle.insertRelation", kGrelation);
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
	 * 目前的升级方式是:<br>
		update geneInfo set<br>
		pathname = #{pathname},<br>
		entry1ID = #{entry1},<br>
		entry2ID = #{entry2},<br>
		type = #{type},<br>
		sybtypeName = #{sybtypeName},<br>
		sybtypeValue = #{sybtypeValue}<br>
		where
			if test="pathname !=null"<br>
				 pathname=#{pathname}<br>
			/if<br>
			if test="entry1 !=null and entry1 != 0"<br>
				and entry1ID=#{entry1}<br>
			/if<br>
			if test="entry2 !=null and entry2 !=0"<br>
				and entry2ID=#{entry2}<br>
			/if<br>
			if test="type !=null "<br>
				and type=#{type}<br>
			/if<br>
			if test="sybtypeName !=null "<br>
				and sybtypeName=#{sybtypeName}<br>
			/if<br>
		/where<br>
	 * @param KGrelation
	 */
	public static void upDateKGrelation(KGrelation kGrelation){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
 		try
		{
			session.update("KGMLSingle.updateRelation", kGrelation);
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
