package com.novelbio.database.mapper;

import java.util.ArrayList;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.novelbio.database.domain.react.RctInteract;

public interface MapRctIneract {
 

	/**
	 * ��geneID,accessID,TaxID�����е��������ȥ����NCBIID��
	 * ��Ҫ�����������Ƿ��Ѿ������ˣ����ص���NCBIID<br>
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
	 * ��geneID,accessID,TaxID�����е��������ȥ����NCBIID��
	 * ��Ҫ�����������Ƿ��Ѿ������ˣ�����ListNCBIID
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
