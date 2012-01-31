package com.novelbio.database.mapper.geneanno;

import java.util.ArrayList;

import com.novelbio.database.domain.geneanno.Go2Term;

public interface MapGoIDconvert {



	/**
	 * 用GoIDquery,GoID两个中的任意组合去查找GoIDconvety表
	 * 主要是来看本列是否已经存在了，返回单个Go2Term
	 * @param Go2Term
	 * @return
	 */
	public Go2Term queryGoIDconvert(Go2Term queryGo2Term);
	
	/**
	 * 用GoIDquery,GoID两个中的任意组合去查找GoIDconvety表
	 * 主要是来看本列是否已经存在了，返回单个ArrayList--Go2Term
	 * @param Go2Term
	 * @return
	 */
	public ArrayList<Go2Term> queryLsGoIDconvert(Go2Term queryGo2Term);
	
	public void insertGoIDconvert(Go2Term Go2Term);
	
	/**
	 * 目前的升级方式是
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
