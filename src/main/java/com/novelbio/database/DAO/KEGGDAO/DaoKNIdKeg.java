package com.novelbio.database.DAO.KEGGDAO;

import java.util.ArrayList;

import org.apache.ibatis.session.SqlSession;

import com.novelbio.database.entity.kegg.noGene.KGNIdKeg;
import com.novelbio.database.util.Util;

 
public class DaoKNIdKeg {

	/**
      where <br>
			if test="kegID != null and kegID != '' "<br>
				 kegID = #{kegID}<br>
			/if<br>
			if test="usualName !=null and usualName != '' "<br>
				and usualName = #{usualName}<br>
			/if<br>
		/where<br>
	 * @param KGNIdKeg<br>
	 * @return
	 */
	public static ArrayList<KGNIdKeg> queryLsKGNIdKeg(KGNIdKeg kgnIdKeg){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
		ArrayList<KGNIdKeg> lsKgnIdKegs=null;
		try
		{
			lsKgnIdKegs= (ArrayList<KGNIdKeg>)session.selectList("KGNoGen.selectIdKeg",kgnIdKeg);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return lsKgnIdKegs;
	}
	
	/**
    where <br>
			if test="kegID != null and kegID != '' "<br>
				 kegID = #{kegID}<br>
			/if<br>
			if test="usualName !=null and usualName != '' "<br>
				and usualName = #{usualName}<br>
			/if<br>
		/where<br>
	 * @param KGNIdKeg<br>
	 * @return
	 */
	public static KGNIdKeg queryKGNIdKeg(KGNIdKeg kgnIdKeg){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
		KGNIdKeg kgnIdKeg2=null;
		try
		{
			kgnIdKeg2= (KGNIdKeg) session.selectOne("KGNoGen.selectIdKeg",kgnIdKeg);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return kgnIdKeg2;
	}
	
	
	public static void InsertKGNIdKeg(KGNIdKeg kgnIdKeg){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
 		try
		{
			session.insert("KGNoGen.insertIdKeg", kgnIdKeg);
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
		update idKeg set<br>
		set<br>
			if test="kegID !=null"<br>
				 kegID = #{kegID},<br>
			/if<br>
			if test="usualName !=null"<br>
				usualName = #{usualName},<br>
			/if<br>
			if test="attribute !=null"<br>
				attribute = #{attribute}<br>
			/if<br>
		/set<br>
		where<br>
			if test="kegID != null and kegID != '' "<br>
				 kegID = #{kegID}<br>
			/if<br>
			if test="usualName !=null and usualName != '' "<br>
				and usualName = #{usualName}<br>
			/if<br>
		/where<br>
	 * @param kgnIdKeg
	 */
	public static void upDateKGNIdKeg(KGNIdKeg kgnIdKeg){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
 		try
		{
			session.update("KEGIDconvert.updateIdKeg", kgnIdKeg);
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
