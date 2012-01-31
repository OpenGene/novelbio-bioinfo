package com.novelbio.database.mapper.geneanno;

import java.util.ArrayList;

import com.novelbio.database.domain.geneanno.Go2Term;

public interface MapGoIDconvert {



	/**
	 * ��GoIDquery,GoID�����е��������ȥ����GoIDconvety��
	 * ��Ҫ�����������Ƿ��Ѿ������ˣ����ص���Go2Term
	 * @param Go2Term
	 * @return
	 */
	public Go2Term queryGoIDconvert(Go2Term queryGo2Term);
	
	/**
	 * ��GoIDquery,GoID�����е��������ȥ����GoIDconvety��
	 * ��Ҫ�����������Ƿ��Ѿ������ˣ����ص���ArrayList--Go2Term
	 * @param Go2Term
	 * @return
	 */
	public ArrayList<Go2Term> queryLsGoIDconvert(Go2Term queryGo2Term);
	
	public void insertGoIDconvert(Go2Term Go2Term);
	
	/**
	 * Ŀǰ��������ʽ��
		update goidconvert 
		set
			if test="GoIDquery !=null"
				querygoid = #{GoIDquery},
			/if
			if test="GoID !=null"
				goid = #{GoID},
			/if
		/set
		where
			if test="GoIDquery !=null"
				querygoid = #{GoIDQuery} 
			/if
			if test="GoID !=null"
				and goid = #{GoID} 
			/if
	    /where>
	 * @param geneInfo
	 */
	public void updateGoIDconvert(Go2Term Go2Term);


}
