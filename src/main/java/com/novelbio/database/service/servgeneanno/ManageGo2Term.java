package com.novelbio.database.service.servgeneanno;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.novelbio.database.domain.geneanno.Go2Term;
import com.novelbio.database.mongorepo.geneanno.RepoGo2Term;
import com.novelbio.database.service.SpringFactory;
//TODO 测试mongodb是否能对set中的元素建立索引，如果可以，则go2term可以设定为setQueryGOID和GOID两个
//并且只需要对setQueryGOID建立索引j
public class ManageGo2Term {
	static double[] lock = new double[0];
	/**
	 * 存储Go2Term的信息
	 * key:Go
	 * value:GoInfo
	 * 0:QueryGoID,1:GoID,2:GoTerm 3:GoFunction
	 */
	static Map<String, Go2Term> mapGoID2GOTerm = new HashMap<String, Go2Term>();
	
	@Autowired
	private RepoGo2Term repoGo2Term;

	public ManageGo2Term() {
		repoGo2Term = (RepoGo2Term) SpringFactory.getFactory().getBean("repoGo2Term");
		fillMap();
	}
	
	private void fillMap() {
		synchronized (lock) {
			if (mapGoID2GOTerm.size() > 0) {
				return;
			}
			for (Go2Term go2Term : repoGo2Term.findAll()) {
				for (String goID : go2Term.getGoIDQuery()) {
					mapGoID2GOTerm.put(goID, go2Term);
				}
			}
		}
	}
	/** 全部读入内存后，hash访问。第一次速度慢，后面效率很高 */
	public Go2Term queryGo2Term(String goID) {
		return mapGoID2GOTerm.get(goID);
	}
	
	/**
	 * 升级，没有就插入
	 * @param go2Term
	 */
	public void saveGo2Term(Go2Term go2Term) {
		synchronized (lock) {
			boolean update = false;
			Go2Term go2TermS = queryGo2Term(go2Term.getGoID());
			if (go2TermS == null) {
				go2TermS = go2Term;
				update = true;
			} else if (go2TermS.addInfo(go2Term)) {
				update = true;
			}
			
			if (update) {
				for (String goID : go2TermS.getGoIDQuery()) {
					mapGoID2GOTerm.put(goID, go2TermS);
				}
				repoGo2Term.save(go2Term);
			}
		}
	}
	
}
