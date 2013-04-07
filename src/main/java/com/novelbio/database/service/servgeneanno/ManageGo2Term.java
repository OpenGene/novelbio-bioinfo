package com.novelbio.database.service.servgeneanno;

import java.util.ArrayList;
import java.util.HashMap;

import javax.inject.Inject;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Service;

import com.novelbio.database.domain.geneanno.Go2Term;
import com.novelbio.database.domain.geneanno.UniGene2Go;
import com.novelbio.database.mapper.geneanno.MapGo2Term;
import com.novelbio.database.mapper.geneanno.MapGoIDconvert;
import com.novelbio.database.mapper.geneanno.MapNCBIID;
import com.novelbio.database.mongorepo.geneanno.RepoGo2Term;
import com.novelbio.database.service.SpringFactory;
//TODO 测试mongodb是否能对set中的元素建立索引，如果可以，则go2term可以设定为setQueryGOID和GOID两个
//并且只需要对setQueryGOID建立索引j
public class ManageGo2Term {
	@Inject
	private RepoGo2Term repoGo2Term;
	
	/**
	 * 存储Go2Term的信息
	 * key:Go
	 * value:GoInfo
	 * 0:QueryGoID,1:GoID,2:GoTerm 3:GoFunction
	 */
	static HashMap<String, Go2Term> mapGoID2GOTerm = new HashMap<String, Go2Term>();
	
	public ManageGo2Term() {
		repoGo2Term = (RepoGo2Term) SpringFactory.getFactory().getBean("repoGo2Term");
		for (Go2Term go2Term : repoGo2Term.findAll()) {
			for (String goID : go2Term.getGoIDQuery()) {
				mapGoID2GOTerm.put(goID, go2Term);
			}
		}
	}
	
	/** 全部读入内存后，hash访问。第一次速度慢，后面效率很高 */
	public Go2Term queryGo2Term(String goID) {
		return mapGoID2GOTerm.get(goID);
	}
	
	/**
	 * 整合升级模式，将go2term拆成goconvert和go2term分别进行升级
	 * 如果已经有了就不升级，没有才升级
	 * 升级方式是简单的覆盖，不像geneInfo有add等方式
	 * 不过如果go2term中其他都没有，则不升级
	 * @param go2Term
	 */
	public boolean updateGo2Term(Go2Term go2Term) {
		Go2Term go2TermS =  repoGo2Term.findByGoID(go2Term.getGoID());
		if (go2TermS == null) {
			go2Term = repoGo2Term.save(go2Term);
			for (String goID : go2Term.getGoIDQuery()) {
				mapGoID2GOTerm.put(goID, go2Term);
			}
		} else {
			go2TermS.addGoIDQuery(GoIDQuery);
		}
	}
	
}
