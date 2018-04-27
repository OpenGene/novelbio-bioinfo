package com.novelbio.database.service.servgeneanno;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.novelbio.database.dao.geneanno.RepoGo2Term;
import com.novelbio.database.model.geneanno.Go2Term;
import com.novelbio.database.service.SpringFactoryBioinfo;
//TODO 测试mongodb是否能对set中的元素建立索引，如果可以，则go2term可以设定为setQueryGOID和GOID两个
//并且只需要对setQueryGOID建立索引j
public class ManageGo2Term {
	private static final Logger logger = Logger.getLogger(ManageGo2Term.class);
	static double[] lock = new double[0];
	/**
	 * 存储Go2Term的信息
	 * key:Go 大写
	 * value:GoInfo
	 * 0:QueryGoID,1:GoID,2:GoTerm 3:GoFunction
	 */
	static Map<String, Go2Term> mapGoIDQuery2GOTerm = new HashMap<String, Go2Term>();
	
	@Autowired
	private RepoGo2Term repoGo2Term;

	private ManageGo2Term() {
		repoGo2Term = (RepoGo2Term) SpringFactoryBioinfo.getFactory().getBean("repoGo2Term");
		fillMap();
	}
	
	private void fillMap() {
		if (mapGoIDQuery2GOTerm.size() > 0) {
			return;
		}
		for (Go2Term go2Term : repoGo2Term.findAll()) {
			for (String goID : go2Term.getGoIDQuery()) {
				mapGoIDQuery2GOTerm.put(goID.toUpperCase(), go2Term);
			}
		}
		logger.info(this.getClass().getName() + "finish fill map");
	}
	/** 全部读入内存后，hash访问。第一次速度慢，后面效率很高 */
	public Go2Term queryGo2Term(String goID) {
		Go2Term go2Term = mapGoIDQuery2GOTerm.get(goID.toUpperCase());
		if (go2Term != null) {
			return go2Term.clone();
		} else {
			return repoGo2Term.findByQueryGoID(goID);
		}
	}
	
	public void saveGo2Term(Go2Term go2Term) {
		try {
			saveGo2TermExp(go2Term);
		} catch (Exception e) {
			logger.error("save GOterm error", e);
		}
	}
	
	/**
	 * 升级，先在map里面找，找到相同的就不升级，没找到才升级
	 * @param go2Term
	 */
	private void saveGo2TermExp(Go2Term go2Term) throws Exception {
		boolean update = false;
		Go2Term go2TermS = null;
		try {
			go2TermS = queryGo2Term(go2Term.getGoID());
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (go2TermS == null) {
			go2TermS = go2Term;
			update = true;
		} else if (go2TermS.addInfo(go2Term)) {
			update = true;
		}

		if (update) {
			for (String goID : go2TermS.getGoIDQuery()) {
				mapGoIDQuery2GOTerm.put(goID.toUpperCase(), go2TermS);
			}
			try {
				repoGo2Term.save(go2TermS);
			} catch (Exception e) {
				Thread.sleep(100);
				repoGo2Term.save(go2TermS);
			}
		}
	}
	
	static class ManageHolder {
		static ManageGo2Term manageGo2Term = new ManageGo2Term();
	}
	
	public static ManageGo2Term getInstance() {
		return ManageHolder.manageGo2Term;
	}
}
