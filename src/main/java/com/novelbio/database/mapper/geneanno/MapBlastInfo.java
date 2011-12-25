package com.novelbio.database.mapper.geneanno;

import java.util.ArrayList;

import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.mapper.MapperSql;

public interface MapBlastInfo extends MapperSql {

	/**
		where<br>
			if test="queryID !=null"<br>
				queryID = #{queryID} <br>
			/if<br>
			if test="queryTax !=null or queryTax!=0"<br>
				and queryTax = #{queryTax} <br>
			/if<br>
			if test="queryDB !=null"<br>
				and queryDB = #{queryDB} <br>
			/if<br>
			if test="subjectID !=null"<br>
				and subjectID = #{subjectID} <br>
			/if<br>
			if test="subjectTax !=null or subjectTax!=0"<br>
				and subjectTax = #{subjectTax} <br>
			/if<br>
			if test="subjectDB !=null"<br>
				and subjectDB = #{subjectDB} <br>
			/if<br>
	    /where <br>
	 * @param BlastInfo
	 * @return
	 */
	public BlastInfo queryBlastInfo(BlastInfo qBlastInfo);
	
	/**
		where<br>
			if test="queryID !=null"<br>
				queryID = #{queryID} <br>
			/if<br>
			if test="queryTax !=null or queryTax!=0"<br>
				and queryTax = #{queryTax} <br>
			/if<br>
			if test="queryDB !=null"<br>
				and queryDB = #{queryDB} <br>
			/if<br>
			if test="subjectID !=null"<br>
				and subjectID = #{subjectID} <br>
			/if<br>
			if test="subjectTax !=null or subjectTax!=0"<br>
				and subjectTax = #{subjectTax} <br>
			/if<br>
			if test="subjectDB !=null"<br>
				and subjectDB = #{subjectDB} <br>
			/if<br>
	    /where <br>
	 * @param BlastInfo
	 * @return
	 */
	public ArrayList<BlastInfo> queryLsBlastInfo(BlastInfo qBlastInfo);
	
	public void insertBlastInfo(BlastInfo blastInfo);
	
	/**
update BlastInfo <br>
		set<br>
		/set<br>
		where<br>
			if test="queryID !=null"<br>
				queryID = #{queryID} <br>
			/if<br>
			if test="queryTax !=null or queryTax!=0"<br>
				and queryTax = #{queryTax} <br>
			/if<br>
			if test="subjectTax !=null or subjectTax!=0"<br>
				and subjectTax = #{subjectTax} <br>
			/if<br>
	    /where <br>
	 */
	public void updateBlastInfo(BlastInfo blastInfo);
}
