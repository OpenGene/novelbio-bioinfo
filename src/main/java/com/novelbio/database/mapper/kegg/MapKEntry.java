package com.novelbio.database.mapper.kegg;

import java.util.ArrayList;

import org.apache.ibatis.session.SqlSession;

import com.novelbio.database.entity.kegg.*;
import com.novelbio.database.entity.friceDB.*;
import com.novelbio.database.util.Util;

public class MapKEntry{
	
	/**
	 		where<br>
			if test="name !=null"<br>
				 name=#{name}<br>
			/if<br>
			if test="pathName !=null"<br>
				and pathName=#{pathName}<br>
			/if<br>
			if test="id !=null and id !=0"<br>
				and ID=#{id}<br>
			/if<br>
			if test="reactionName !=null"<br>
				and reactionName=#{reactionName}<br>
			/if<br>
			if test="taxID !=null or taxID !=0"<br>
				and taxID=#{taxID}<br>
			/if<br>
			if test="parentID !=null or parentID !=0"<br>
				and parentID=#{parentID}<br>
			/if<br>
		/where<br>
	 * @param KGentry
	 * @return
	 */
	public static ArrayList<KGentry> queryLsKGentries(KGentry kGentry){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
		ArrayList<KGentry> lsKGentries=null;
		try
		{
			lsKGentries= (ArrayList<KGentry>)session.selectList("KGMLSingle.selectEntry",kGentry);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return lsKGentries;
	}
	
	/**
	 		where<br>
			if test="name !=null"<br>
				 name=#{name}<br>
			/if<br>
			if test="pathName !=null"<br>
				and pathName=#{pathName}<br>
			/if<br>
			if test="id !=null and id !=0"<br>
				and ID=#{id}<br>
			/if<br>
			if test="reactionName !=null"<br>
				and reactionName=#{reactionName}<br>
			/if<br>
			if test="taxID !=null or taxID !=0"<br>
				and taxID=#{taxID}<br>
			/if<br>
			if test="parentID !=null or parentID !=0"<br>
				and parentID=#{parentID}<br>
			/if<br>
		/where<br>
	 * @param KGentry
	 * @return
	 */
	public static KGentry queryKGentry(KGentry kGentry){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
		KGentry kGentry2=null;
		try
		{
			kGentry2= (KGentry)session.selectOne("KGMLSingle.selectEntry",kGentry);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return kGentry2;
	}
	
	
	public static void InsertKGentry(KGentry kGentry){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
 		try
		{
			session.insert("KGMLSingle.insertEntry", kGentry);
		}
		catch(Exception e)
		{
			System.out.println(kGentry.getPathName()+"      "+kGentry.getLinkEntry());
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
	}
	
	/**
	 * 目前的升级方式是
	    update entry set <br>
		name = #{name},<br>
		pathName = #{pathName},<br>
		ID = #{id},<br>
		type = #{type},<br>
		reactionName = #{reactionName},<br>
		link = #{linkEntry},<br>
		compNum=#{compNum},<br>
		compID=#{compID},<br>
		parentID=#{parentID}<br>
		<b>where</b> name=#{name}<br>
		and pathName=#{pathName}<br>
		and ID=#{id}<br>
	 * @param kGentry
	 */
	public static void upDateKGentry(KGentry kGentry){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
 		try
		{
			session.update("KGMLSingle.updateEntry", kGentry);
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
