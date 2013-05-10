package com.novelbio.database.service.servgeneanno;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.novelbio.database.domain.geneanno.Go2Term;
import com.novelbio.database.mongorepo.geneanno.RepoGo2Term;
import com.novelbio.database.service.SpringFactory;
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

	public ManageGo2Term() {
		repoGo2Term = (RepoGo2Term) SpringFactory.getFactory().getBean("repoGo2Term");
		fillMap();
	}
	
	private void fillMap() {
		synchronized (lock) {
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
	}
	/** 全部读入内存后，hash访问。第一次速度慢，后面效率很高 */
	public Go2Term queryGo2Term(String goID) {
		return mapGoIDQuery2GOTerm.get(goID.toUpperCase()).clone();
//		return repoGo2Term.findByQueryGoID(goID);
	}
	
	/**
	 * 升级，先在map里面找，找到相同的就不升级，没找到才升级
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
					mapGoIDQuery2GOTerm.put(goID.toUpperCase(), go2TermS);
				}
				try {
					repoGo2Term.save(go2TermS);
				} catch (Exception e) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					try {
						repoGo2Term.save(go2TermS);
					} catch (Exception e2) {
						e2.printStackTrace();
					}
					
				}
			}
		}
	}
	
}
