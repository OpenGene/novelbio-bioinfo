package com.novelbio.database.DAO.KEGGDAO;

import org.apache.ibatis.session.SqlSession;

import com.novelbio.database.entity.kegg.*;
import com.novelbio.database.entity.friceDB.*;
import com.novelbio.database.util.Util;

public class DaoKCdetail {
	/**
	 * geneID2KO是多对多的关系，
	 * KGCgen2Ko中包含了这个多对多，里面有个list
	 * 给定NCBIID对象，返回IDgen2Keg、IDkeg2Ko 表的信息<br>
	 * if test="geneID !=null and geneID !=0"<br>
				 geneID=#{geneID}<br>
			/if<br>
			if test="taxID !=null and taxID !=0"<br>
				and taxID=#{taxID}<br>
			/if<br>
	 * @param NCBIID<br>
	 * @return
	 */ 
	public static KGCgen2Ko queryGen2Ko(NCBIID ncbiid){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
		KGCgen2Ko kgCgen2Ko=null;
		try
		{
			kgCgen2Ko=(KGCgen2Ko) session.selectOne("KGCquery.selectGen2Ko",ncbiid);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return kgCgen2Ko;
	}
	
	/**
	 * 给定ncbiid对象，返回IDgen2Keg、entry 表的信息<br>
	 * if test="geneID !=null and geneID !=0"< br>
				 geneID=#{geneID}<br>
			/if<br>
			if test="taxID !=null and taxID !=0"<br>
				and taxID=#{taxID}<br>
			/if<br>
	 * @param NCBIID<br>
	 * @return
	 */
	public static KGCgen2Entry queryGen2entry(NCBIID ncbiid){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
		KGCgen2Entry kgCgen2Entry=null;
		try
		{
			kgCgen2Entry=(KGCgen2Entry) session.selectOne("KGCquery.selectGen2Entry",ncbiid);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return kgCgen2Entry;
	}
	
	/**
	 * <b>blast时，query物种只有KO，没有keggID，那么指定KO以及subject物种的taxID，返回subject物种的geneID</b>
	 * 给定kGentry对象，返回entry--ko2keg--genID 表的信息<br>
		where<br>
			if test="Ko !=null"<br>
				 Ko = #{Ko}<br>
			/if<br>
			if test="taxID !=null and taxID !=0"<br>
				and taxID=#{taxID}<br>
			/if<br>
			if test="keggID !=null"<br>
				and taxID=#{keggID}<br>
			/if<br>
		/where<br>
	 * @param NCBIID<br>
	 * @return
	 */
	public static KGCKo2Gen queryKo2Gen(KGIDkeg2Ko kgiDkeg2Ko){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
		KGCKo2Gen kgcKo2Gen=null;
		try
		{
			kgcKo2Gen=(KGCKo2Gen) session.selectOne("KGCquery.selectKo2Gen",kgiDkeg2Ko);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return kgcKo2Gen;
	}
	
	/**
	 * 给定kGentry对象，返回entry--genID 表的信息<br>
	 * where<br>
			if test="name !=null "<br>
				 name = #{name}<br>
			/if<br>
			if test="id !=null and id !=0"<br>
				and ID = #{id}<br>
			/if<br>
			if test="parentID !=null and parentID !=0"<br>
				and parentID = #{parentID}<br>
			/if<br>
			if test="pathName !=null"<br>
				and pathName = #{pathName}<br>
			/if<br>
			if test="taxID !=null and taxID != 0"<br>
				and taxID = #{taxID}<br>
			/if<br>
		/where<br>
	 * @param NCBIID<br>
	 * @return
	 */
	public static KGCentry2Gen queryEntry2Gen(KGentry kGentry){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
		KGCentry2Gen kgCentry2Gen=null;
		try
		{
			kgCentry2Gen=(KGCentry2Gen) session.selectOne("KGCquery.selectEntry2Gen",kGentry);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return kgCentry2Gen;
	}
}
