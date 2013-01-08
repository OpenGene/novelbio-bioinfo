package com.novelbio.database.service.servgeneanno;

import java.util.ArrayList;
import java.util.HashMap;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.novelbio.database.domain.geneanno.Go2Term;
import com.novelbio.database.mapper.geneanno.MapGo2Term;
import com.novelbio.database.mapper.geneanno.MapGoIDconvert;
import com.novelbio.database.mapper.geneanno.MapNCBIID;
import com.novelbio.database.service.SpringFactory;
@Service
public class ServGo2Term implements MapGo2Term{
	@Inject
	private MapGo2Term mapGo2Term;
	@Inject
	private MapGoIDconvert mapGoIDconvert;
	/**
	 * 存储Go2Term的信息
	 * key:Go
	 * value:GoInfo
	 * 0:QueryGoID,1:GoID,2:GoTerm 3:GoFunction
	 */
	static HashMap<String, Go2Term> hashGo2Term = new HashMap<String, Go2Term>();
	
	public ServGo2Term() {
		mapGo2Term = (MapGo2Term) SpringFactory.getFactory().getBean("mapGo2Term");
		mapGoIDconvert = (MapGoIDconvert) SpringFactory.getFactory().getBean("mapGoIDconvert");
	}
	/**
	 * 全部读入内存后，hash访问。第一次速度慢，后面效率很高
	 */
	public Go2Term queryGo2Term(String goID) {
		return getHashGo2Term().get(goID);
	}
	/**
	 * 整合升级模式，将go2term拆成goconvert和go2term分别进行升级
	 * 如果已经有了就不升级，没有才升级
	 * 升级方式是简单的覆盖，不像geneInfo有add等方式
	 * 不过如果go2term中其他都没有，则不升级
	 * @param go2Term
	 */
	public boolean updateGo2TermComb(Go2Term go2Term) {
		//如果没有GOID信息，则返回
		if ((go2Term.getGoID() == null || go2Term.getGoID().equals("") )
				&& (go2Term.getGoIDQuery() == null || go2Term.getGoIDQuery().equals(""))) {
			return true;
		}
		if (go2Term.getGoID() != null && !go2Term.getGoID().equals("")) {
			updateGoIDconvert(go2Term);
		}
		//如果只有queryID，那么查找Convert数据库将GOID补上
		if (go2Term.getGoID() == null || go2Term.getGoID().equals("")) {
			Go2Term go2Term2 = mapGoIDconvert.queryGoIDconvert(go2Term);
			if (go2Term2 == null) {
				return false;
			}
			go2Term.setGoID(go2Term2.getGoID());
		}
		//查找Go2Term有没有信息，没有就插入
		Go2Term go2Term2 = mapGo2Term.queryGo2Term(go2Term);
		if ( (go2Term.getGoFunction() != null && go2Term.getGoTerm() != null)
				|| go2Term.getParent().size() > 0 || go2Term.getChild().size() > 0
				) {
			if (go2Term2 != null && !go2Term2.equalsAll(go2Term) ) {
				mapGo2Term.updateGo2Term(go2Term);
			}
			else if (go2Term2 == null) {
				mapGo2Term.insertGo2Term(go2Term);
			}
		}
		return true;
	}
	/**
	 * 用queryID搜索QueryID2GOID数据库，如果返回的GOID与go2Term中一致，则跳过，否则升级数据库的GOID项目
	 * @param go2Term
	 */
	private void updateGoIDconvert(Go2Term go2Term) {
		// 如果没有QueryID，那么将QueryID用GOID补上
		if (go2Term.getGoIDQuery() == null || go2Term.getGoIDQuery().equals("")) {
			go2Term.setGoIDQuery(go2Term.getGoID());
		}

		Go2Term go2TermQuery = new Go2Term();
		go2TermQuery.setGoIDQuery(go2Term.getGoIDQuery());
		Go2Term go2Term2 = mapGoIDconvert.queryGoIDconvert(go2TermQuery);
		if (go2Term2 == null) {
			mapGoIDconvert.insertGoIDconvert(go2Term);
		} 
		else if (!go2Term2.getGoID().equals(go2Term.getGoID())) {
			//升级数据库的GOID项目
			mapGoIDconvert.updateGoIDconvertWhereQueryGOID(go2Term);
		}
	}
	/**
	 * 通过访问数据库查询，效率相对低
	 * 注意最好能给定queryGOID
	 */
	@Override
	public Go2Term queryGo2Term(Go2Term queryGo2Term) {
		//如果没有GOID
		if ( (queryGo2Term.getGoID() == null || queryGo2Term.getGoID().equals("")) 
				&& queryGo2Term.getGoIDQuery() != null && !queryGo2Term.getGoIDQuery().equals("")
				) {
			Go2Term go2Term2 = mapGoIDconvert.queryGoIDconvert(queryGo2Term);
			//如果queryID没搜到，则将queryID直接装给GOID
			if (go2Term2 == null) {
				queryGo2Term.setGoID(queryGo2Term.getGoIDQuery());
			}
			//否则就给定新的GOID
			else {
				queryGo2Term.setGoID(go2Term2.getGoID());
			}
		}
		Go2Term go2Term2 = mapGo2Term.queryGo2Term(queryGo2Term);
		go2Term2.setGoIDQuery(queryGo2Term.getGoIDQuery());
		return go2Term2;
	}
	/**
	 * 根据需要获取GO信息
	 * 其中如果没有设定goID和goIDquery的话，就会返回全部信息，<br>
	 * <b>注意这些信息中没有GOIDquery</b>
	 * @param queryGo2Term
	 * @return
	 */
	@Override
	public ArrayList<Go2Term> queryLsGo2Term(Go2Term queryGo2Term) {
		//如果queryGOID和GOID中有一项存在，则返回相对应的信息
		if (queryGo2Term.getGoIDQuery() != null && queryGo2Term.getGoIDQuery().equals("")
			||
			queryGo2Term.getGoID() != null && queryGo2Term.getGoID().equals("")
				) {
			if ( (queryGo2Term.getGoID() == null || queryGo2Term.getGoID().equals("")) 
					&& queryGo2Term.getGoIDQuery() != null && !queryGo2Term.getGoIDQuery().equals("")
					) {
				queryGo2Term.setGoID(mapGoIDconvert.queryGoIDconvert(queryGo2Term).getGoID());
			}
			ArrayList<Go2Term> lsGo2Terms = mapGo2Term.queryLsGo2Term(queryGo2Term);
			if (queryGo2Term.getGoIDQuery() != null && !queryGo2Term.getGoIDQuery().equals("")  ) {
				for (Go2Term go2Term : lsGo2Terms) {
					go2Term.setGoIDQuery(queryGo2Term.getGoIDQuery());
				}
			}
			return lsGo2Terms;
		}
		//如果都不存在，则返回全部信息
		return mapGo2Term.queryLsGo2Term(queryGo2Term);
 	}
	/**
	 * 根据需要获取GO信息
	 * 其中如果没有设定goID和goIDquery的话，就会返回全部信息，<br>
	 * <b>注意这些信息中没有GOIDquery</b>
	 * @param queryGo2Term
	 * @return
	 */
	public HashMap<String, Go2Term> getHashGo2Term() {
		Go2Term queryGo2Term = new Go2Term();
		if (hashGo2Term.size() > 0) {
			return hashGo2Term;
		}
		HashMap<String, Go2Term> hashGOIDconvert = new HashMap<String, Go2Term>();
		ArrayList<Go2Term> lsAllConvert = mapGoIDconvert.queryLsGoIDconvert(queryGo2Term);
		ArrayList<Go2Term> lsAllGo2Term = mapGo2Term.queryLsGo2Term(queryGo2Term);
		for (Go2Term go2Term : lsAllGo2Term) {
			hashGOIDconvert.put(go2Term.getGoID(), go2Term);
		}
		for (Go2Term go2Term : lsAllConvert) {
			hashGo2Term.put(go2Term.getGoIDQuery(), hashGOIDconvert.get(go2Term.getGoID()));
		}
		return hashGo2Term;
	}

	@Override
	public void insertGo2Term(Go2Term Go2Term) {
		mapGo2Term.insertGo2Term(Go2Term);
	}

	@Override
	public void updateGo2Term(Go2Term Go2Term) {
		mapGo2Term.updateGo2Term(Go2Term);
	}

}
