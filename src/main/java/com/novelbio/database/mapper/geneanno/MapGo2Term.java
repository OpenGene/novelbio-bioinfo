package com.novelbio.database.mapper.geneanno;

import java.util.ArrayList;

import org.apache.ibatis.session.SqlSession;

import com.novelbio.database.domain.geneanno.Go2Term;
import com.novelbio.database.mapper.MapperSql;
import com.novelbio.database.util.Util;

public interface MapGo2Term extends MapperSql{


	/**
	 * ��GoIDquery,GoID,GoFunction�����е��������ȥ����Go2Term��
	 * ��Ҫ�����������Ƿ��Ѿ������ˣ����ص���Go2Term
	 * @param Go2Term
	 * @return
	 */
	public Go2Term queryGo2Term(Go2Term queryGo2Term);
	
	/**
	 * ��GoIDquery,GoID,GoFunction�����е��������ȥ����Go2Term��
	 * ��Ҫ�����������Ƿ��Ѿ������ˣ����ص���ArrayList--Go2Term
	 * @param Go2Term
	 * @return
	 */
	public ArrayList<Go2Term> queryLsGo2Term(Go2Term queryGo2Term);
	
	public void insertGo2Term(Go2Term Go2Term);
	
	/**
	 * Ŀǰ��������ʽ��
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
