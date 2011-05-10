package DAO.KEGGDAO;

import java.util.ArrayList;

import org.apache.ibatis.session.SqlSession;

import util.Util;
import entity.kegg.KGIDkeg2Ko;
import entity.kegg.KGpathRelation;

public class DaoKIDKeg2Ko {
	/**
		where <br>
			if test="keggID !=null" <br>
				keggID = #{keggID} <br>
			/if<br>
			if test="KO !=null"<br>
				and KO = #{KO} <br>
			/if<br>
			if test="taxID !=null and taxID !=0"<br>
				and taxID = #{taxID} <br>
			/if<br>
	    /where<br>
	 * @param KGIDkeg2Ko
	 * @return
	 */
	public static ArrayList<KGIDkeg2Ko> queryLsKGIDkeg2Ko(KGIDkeg2Ko kgiDkeg2Ko){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
		ArrayList<KGIDkeg2Ko> lsKgiDkeg2Kos=null;
		try
		{
			lsKgiDkeg2Kos= (ArrayList<KGIDkeg2Ko>)session.selectList("KEGIDconvert.selectKeg2Ko",kgiDkeg2Ko);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return lsKgiDkeg2Kos;
	}
	
	/**
		where<br>
			if test="keggID !=null"<br>
				keggID = #{keggID} <br>
			/if<br>
			if test="KO !=null"<br>
				and KO = #{KO} <br>
			/if<br>
			if test="taxID !=null and taxID !=0"<br>
				and taxID = #{taxID} <br>
			/if<br>
	    /where<br>
	 * @param KGIDkeg2Ko
	 * @return
	 */
	public static KGIDkeg2Ko queryKGIDkeg2Ko(KGIDkeg2Ko kGIDkeg2Ko){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
		KGIDkeg2Ko kGIDkeg2Ko2=null;
		try
		{
			kGIDkeg2Ko2= (KGIDkeg2Ko)session.selectOne("KEGIDconvert.selectKeg2Ko",kGIDkeg2Ko);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return kGIDkeg2Ko2;
	}
	
	
	public static void InsertKGIDkeg2Ko(KGIDkeg2Ko kGIDkeg2Ko){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
 		try
		{
			session.insert("KEGIDconvert.insertKeg2Ko", kGIDkeg2Ko);
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
		update IDKeg2Ko set<br>
		keggID = #{keggID},<br>
		KO = #{KO},<br>
		taxID = #{taxID},<br>
 		where<br>
			if test="keggID !=null"<br>
				keggID = #{keggID} <br>
			/if<br>
			if test="KO !=null"<br>
				and KO = #{KO} <br>
			/if<br>
			if test="taxID !=null and taxID !=0"<br>
				and taxID = #{taxID} <br>
			/if<br>
	    /where<br>
	 * @param kGIDkeg2Ko
	 */
	public static void upDateKGIDkeg2Ko(KGIDkeg2Ko kGIDkeg2Ko){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
 		try
		{
			session.update("KEGIDconvert.updateKeg2Ko", kGIDkeg2Ko);
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
