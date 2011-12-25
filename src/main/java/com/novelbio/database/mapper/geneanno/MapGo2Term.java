package com.novelbio.database.mapper.geneanno;

import java.util.ArrayList;

import org.apache.ibatis.session.SqlSession;

import com.novelbio.database.domain.geneanno.Go2Term;
import com.novelbio.database.mapper.MapperSql;
import com.novelbio.database.util.Util;

public interface MapGo2Term extends MapperSql{


	/**
	 * 用GoIDquery,GoID,GoFunction三个中的任意组合去查找Go2Term表
	 * 主要是来看本列是否已经存在了，返回单个Go2Term
	 * @param Go2Term
	 * @return
	 */
	public Go2Term queryGo2Term(Go2Term queryGo2Term);
	
	/**
	 * 用GoIDquery,GoID,GoFunction三个中的任意组合去查找Go2Term表
	 * 主要是来看本列是否已经存在了，返回单个ArrayList--Go2Term
	 * @param Go2Term
	 * @return
	 */
	public ArrayList<Go2Term> queryLsGo2Term(Go2Term queryGo2Term);
	
	public void insertGo2Term(Go2Term Go2Term);
	
	/**
	 * 目前的升级方式是
	update Go2Term <br>
		set<br>
			if test="GoIDquery !=null"<br>
				GoIDquery = #{GoIDquery},<br>
			/if<br>
			if test="GoID !=null"<br>
				GoID = #{GoID},<br>
			/if<br>
			if test="GoFunction !=null"<br>
				GoFunction = #{GoFunction}<br>
			/if<br>
			if test="GoTerm !=null"<br>
				GoTerm = #{GoTerm}<br>
			/if<br>
		/set<br>
		where<br>
			if test="GoIDquery !=null"<br>
				GoIDquery = #{GoIDquery} <br>
			/if<br>
			if test="GoID !=null"<br>
				and GoID = #{GoID} <br>
			/if<br>
			if test="GoFunction !=null"<br>
				and GoFunction = #{GoFunction} <br>
			/if<br>
	    /where<br>
	 * @param geneInfo
	 */
	public void updateGo2Term(Go2Term Go2Term);

}
