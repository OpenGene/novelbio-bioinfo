package com.novelbio.database.service;

import java.util.ArrayList;

import com.novelbio.database.domain.react.RctInteract;

public class ServRctIntact {
	
	MapRctIneract mapRctIneract;
	/**
	 * 		where <br>
			if test="taxID !=0 and taxID !=null"<br>
				and taxID = #{taxID} <br>
			/if<br>
			if test="geneID1 !=null"<br>
				geneID1 = #{geneID1} <br>
			/if<br>
			if test="geneID2 !=null"<br>
				geneID2 = #{geneID2} <br>
			/if<br>
			if test="dbInfo1 !=null"<br>
				dbInfo1 = #{dbInfo1} <br>
			/if<br>
			if test="dbInfo2 !=null"<br>
				dbInfo2 = #{dbInfo2} <br>
			/if<br>
	    /where <br>

	 * @param rctInteract
	 * @return
	 */
	public static RctInteract qRctIntact(RctInteract rctInteract) {
		return mapRctIneract.qRctInteract(rctInteract);
	}
	
	/**
	 * 		where <br>
			if test="taxID !=0 and taxID !=null"<br>
				and taxID = #{taxID} <br>
			/if<br>
			if test="geneID1 !=null"<br>
				geneID1 = #{geneID1} <br>
			/if<br>
			if test="geneID2 !=null"<br>
				geneID2 = #{geneID2} <br>
			/if<br>
			if test="dbInfo1 !=null"<br>
				dbInfo1 = #{dbInfo1} <br>
			/if<br>
			if test="dbInfo2 !=null"<br>
				dbInfo2 = #{dbInfo2} <br>
			/if<br>
	    /where <br>

	 * @param rctInteract
	 * @return
	 */
	public static ArrayList<RctInteract> qLsRctIntacts(RctInteract rctInteract) {
		return mapRctIneract.qLsRctInteracts(rctInteract);
	}
	
	/**
	 * @param rctInteract
	 * @return
	 */
	public static void instRctInteract(RctInteract rctInteract) {
		 mapRctIneract.instRctInteract(rctInteract);
	}
	/**
		update rctinteract<br>
		set<br>
			if test="taxID !=0 and taxID !=null"<br>
				and taxID = #{taxID} <br>
			/if<br>
			if test="geneID1 !=null and geneID1 != '' "<br>
				geneID1 = #{geneID1} <br>
			/if<br>
			if test="geneID2 !=null and geneID2 != '' "<br>
				geneID2 = #{geneID2} <br>
			/if<br>
			if test="dbInfo1 !=null and dbInfo1 != '' "<br>
				dbInfo1 = #{dbInfo1} <br>
			/if<br>
			if test="dbInfo2 !=null and dbInfo2 != '' "<br>
				dbInfo2 = #{dbInfo2} <br>
			/if<br>
			if test="interaction !=null and interaction != '' "<br>
				interaction = #{interaction} <br>
			/if<br>
			if test="ictContext !=null and ictContext != '' "<br>
				ictContext = #{ictContext} <br>
			/if<br>
			if test="pubmed !=null and pubmed != '' "<br>
				pubmed = #{pubmed} <br>
			/if<br>
		/set<br>
		where<br>
			if test="taxID !=0 and taxID !=null"<br>
				and taxID = #{taxID} <br>
			/if<br>
			if test="geneID1 !=null and geneID1 != '' "<br>
				geneID1 = #{geneID1} <br>
			/if<br>
			if test="geneID2 !=null and geneID2 != '' "<br>
				geneID2 = #{geneID2} <br>
			/if<br>
			if test="dbInfo1 !=null and dbInfo1 != '' "<br>
				dbInfo1 = #{dbInfo1} <br>
			/if<br>
			if test="dbInfo2 !=null and dbInfo2 != '' "<br>
				dbInfo2 = #{dbInfo2} <br>
			/if<br>
	    /where <br>
	 * @param rctInteract
	 * @return
	 */
	public static void updbRctInteract(RctInteract rctInteract) {
		 mapRctIneract.updbRctInteract(rctInteract);
	}
	
	/**
	 * 
	 * 给定一个geneID，以及该基因的dbinfo，返回所有与该基因相关的关系
	 * @param genID
	 * @param dbInfo
	 * @return
	 */
	public static RctInteract getRctIntact(String genID,String dbInfo) {
		RctInteract rctInteract = new RctInteract();
		rctInteract.setGeneId1(genID); rctInteract.setDbInfo1(dbInfo);
		
		return null;
	}
	
	
}
