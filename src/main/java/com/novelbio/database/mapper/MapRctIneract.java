package com.novelbio.database.mapper;

import java.util.ArrayList;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.novelbio.database.domain.react.RctInteract;

public interface MapRctIneract {
 

	/**
	 * 用geneID,accessID,TaxID三个中的任意组合去查找NCBIID表
	 * 主要是来看本列是否已经存在了，返回单个NCBIID<br>
	 * if test="geneID !=0 and geneID !=null" <br>
				GeneID = #{geneID} <br>
			/if<br>
			if test="accessID !=null"<br>
				and accessID = #{accessID} <br>
			/if<br>
			if test="taxID !=0 and taxID !=null"<br>
				and TaxID = #{taxID} <br>
			/if<br>
	 * @param NCBIID
	 * @return
	 */
	public RctInteract qRctInteract(RctInteract rctInteract);
	/**
	 * 用geneID,accessID,TaxID三个中的任意组合去查找NCBIID表
	 * 主要是来看本列是否已经存在了，返回ListNCBIID
	 * if test="geneID !=0 and geneID !=null" <br>
				GeneID = #{geneID} <br>
			/if<br>
			if test="accessID !=null"<br>
				and accessID = #{accessID} <br>
			/if<br>
			if test="taxID !=0 and taxID !=null"<br>
				and TaxID = #{taxID} <br>
			/if<br>
	 * @param NCBIID
	 * @return
	 */
	public ArrayList<RctInteract> qLsRctInteracts(RctInteract rctInteract);
	
	public  void instRctInteract(RctInteract rctInteract);
	
	public  void updbRctInteract(RctInteract rctInteract);
}
