package com.novelbio.database.mapper.kegg;

import java.util.ArrayList;

import org.apache.ibatis.session.SqlSession;

import com.novelbio.database.entity.kegg.*;
import com.novelbio.database.entity.kegg.noGene.KGNCompInfo;
import com.novelbio.database.entity.kegg.noGene.KGNIdKeg;
import com.novelbio.database.entity.friceDB.*;
import com.novelbio.database.util.Util;

public class MapKNCompInfo {

	/**
		select *
		from kgComp
		where kegID = #{kegID}
	 * @param KGNIdKeg<br>
	 * @return
	 */
	public static ArrayList<KGNCompInfo> queryLsKGNCompInfo(KGNIdKeg kgnIdKeg){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
		ArrayList<KGNCompInfo> lsKgnCompInfos=null;
		try
		{
			lsKgnCompInfos= (ArrayList<KGNCompInfo>)session.selectList("KGNoGen.selectCompInfo",kgnIdKeg);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return lsKgnCompInfos;
	}
	
	/**
	select *
	from kgComp
	where kegID = #{kegID}
 * @param KGNIdKeg<br>
 * @return
 */
	public static KGNCompInfo queryKGNCompInfo(KGNIdKeg kgnIdKeg){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
		KGNCompInfo kgnCompInfos=null;
		try
		{
			kgnCompInfos= (KGNCompInfo) session.selectOne("KGNoGen.selectCompInfo",kgnIdKeg);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return kgnCompInfos;
	}
	
	
	public static void InsertKGNCompInfo(KGNCompInfo kgnCompInfo){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
 		try
		{
			session.insert("KGNoGen.insertCompInfo", kgnCompInfo);
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
	 * 目前的升级方式是<br>
	update kgComp set<br>
		set<br>
			if test="kegID !=null"<br>
				 kegID=#{kegID},<br>
			/if<br>
			if test="usualName !=null"<br>
				usualName = #{usualName},<br>
			/if<br>
			if test="formula !=null"<br>
				formula=#{formula},<br>
			/if<br>
			if test="mass !=null and mass != 0"<br>
				mass = #{mass},<br>
			/if<br>
			if test="remark !=null "<br>
				remark = #{remark},<br>
			/if<br>
			if test="comment !=null "<br>
				comment = #{comment}<br>
			/if<br>
		/set<br>
		where kegID = #{kegID}<br>
	 * @param kgnCompInfo
	 */
	public static void upDateKGNIdKeg(KGNCompInfo kgnCompInfo){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
 		try
		{
			session.update("KEGIDconvert.updateCompInfo", kgnCompInfo);
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
