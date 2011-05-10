package com.novelbio.database.DAO.KEGGDAO;

import java.util.ArrayList;

import org.apache.ibatis.session.SqlSession;

import com.novelbio.database.entity.kegg.*;
import com.novelbio.database.entity.friceDB.*;
import com.novelbio.database.util.Util;

public class DaoKIDgen2Keg {

	/**
		where
			if test="geneID !=null and geneID !=0"
				 geneID=#{geneID}
			/if
			if test="keggID !=null"
				and keggID=#{keggID}
			/if
			if test="taxID !=null and taxID !=0"
				and taxID=#{taxID}
			/if
		/where
	 * @param KGIDgen2Keg
	 * @return
	 */
	public static ArrayList<KGIDgen2Keg> queryLsKGIDgen2Keg(KGIDgen2Keg kgIDgen2Keg){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
		ArrayList<KGIDgen2Keg> lsKgIDgen2Kegs=null;
		try
		{
			lsKgIDgen2Kegs= (ArrayList<KGIDgen2Keg>)session.selectList("KEGIDconvert.selectGen2Keg",kgIDgen2Keg);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return lsKgIDgen2Kegs;
	}
	
	/**
		where
			if test="geneID !=null and geneID !=0"
				 geneID=#{geneID}
			/if
			if test="keggID !=null"
				and keggID=#{keggID}
			/if
			if test="taxID !=null and id !=0"
				and taxID=#{taxID}
			/if
		/where
	 * @param KGIDgen2Keg
	 * @return
	 */
	public static KGIDgen2Keg queryKGIDgen2Keg(KGIDgen2Keg kGIDgen2Keg){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
		KGIDgen2Keg kGIDgen2Keg2=null;
		try
		{
			kGIDgen2Keg2= (KGIDgen2Keg)session.selectOne("KEGIDconvert.selectGen2Keg",kGIDgen2Keg);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return kGIDgen2Keg2;
	}
	
	
	public static void InsertKGIDgen2Keg(KGIDgen2Keg kGIDgen2Keg){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
 		try
		{
			session.insert("KEGIDconvert.insertGen2Keg", kGIDgen2Keg);
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
		update IDgen2Keg set
		geneID = #{geneID},
		keggID = #{keggID},
		taxID = #{taxID},
		where
			if test="geneID !=null and geneID !=0"
				 geneID=#{geneID}
			/if
			if test="keggID !=null"
				and keggID=#{keggID}
			/if
			if test="taxID !=null and id !=0"
				and taxID=#{taxID}
			/if
		/where
	 * @param kGIDgen2Keg
	 */
	public static void upDateKGIDgen2Keg(KGIDgen2Keg kGIDgen2Keg){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
 		try
		{
			session.update("KEGIDconvert.updateGen2Keg", kGIDgen2Keg);
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
