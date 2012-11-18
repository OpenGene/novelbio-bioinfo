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
	 * �洢Go2Term����Ϣ
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
	 * ȫ�������ڴ��hash���ʡ���һ���ٶ���������Ч�ʺܸ�
	 */
	public Go2Term queryGo2Term(String goID) {
		return getHashGo2Term().get(goID);
	}
	/**
	 * ��������ģʽ����go2term���goconvert��go2term�ֱ��������
	 * ����Ѿ����˾Ͳ�������û�в�����
	 * ������ʽ�Ǽ򵥵ĸ��ǣ�����geneInfo��add�ȷ�ʽ
	 * �������go2term��������û�У�������
	 * @param go2Term
	 */
	public boolean updateGo2TermComb(Go2Term go2Term) {
		//���û��GOID��Ϣ���򷵻�
		if ((go2Term.getGoID() == null || go2Term.getGoID().equals("") )
				&& (go2Term.getGoIDQuery() == null || go2Term.getGoIDQuery().equals(""))) {
			return true;
		}
		if (go2Term.getGoID() != null && !go2Term.getGoID().equals("")) {
			updateGoIDconvert(go2Term);
		}
		//���ֻ��queryID����ô����Convert���ݿ⽫GOID����
		if (go2Term.getGoID() == null || go2Term.getGoID().equals("")) {
			Go2Term go2Term2 = mapGoIDconvert.queryGoIDconvert(go2Term);
			if (go2Term2 == null) {
				return false;
			}
			go2Term.setGoID(go2Term2.getGoID());
		}
		//����Go2Term��û����Ϣ��û�оͲ���
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
	 * ��queryID����QueryID2GOID���ݿ⣬������ص�GOID��go2Term��һ�£��������������������ݿ��GOID��Ŀ
	 * @param go2Term
	 */
	private void updateGoIDconvert(Go2Term go2Term) {
		// ���û��QueryID����ô��QueryID��GOID����
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
			//�������ݿ��GOID��Ŀ
			mapGoIDconvert.updateGoIDconvertWhereQueryGOID(go2Term);
		}
	}
	/**
	 * ͨ���������ݿ��ѯ��Ч����Ե�
	 * ע������ܸ���queryGOID
	 */
	@Override
	public Go2Term queryGo2Term(Go2Term queryGo2Term) {
		//���û��GOID
		if ( (queryGo2Term.getGoID() == null || queryGo2Term.getGoID().equals("")) 
				&& queryGo2Term.getGoIDQuery() != null && !queryGo2Term.getGoIDQuery().equals("")
				) {
			Go2Term go2Term2 = mapGoIDconvert.queryGoIDconvert(queryGo2Term);
			//���queryIDû�ѵ�����queryIDֱ��װ��GOID
			if (go2Term2 == null) {
				queryGo2Term.setGoID(queryGo2Term.getGoIDQuery());
			}
			//����͸����µ�GOID
			else {
				queryGo2Term.setGoID(go2Term2.getGoID());
			}
		}
		Go2Term go2Term2 = mapGo2Term.queryGo2Term(queryGo2Term);
		go2Term2.setGoIDQuery(queryGo2Term.getGoIDQuery());
		return go2Term2;
	}
	/**
	 * ������Ҫ��ȡGO��Ϣ
	 * �������û���趨goID��goIDquery�Ļ����ͻ᷵��ȫ����Ϣ��<br>
	 * <b>ע����Щ��Ϣ��û��GOIDquery</b>
	 * @param queryGo2Term
	 * @return
	 */
	@Override
	public ArrayList<Go2Term> queryLsGo2Term(Go2Term queryGo2Term) {
		//���queryGOID��GOID����һ����ڣ��򷵻����Ӧ����Ϣ
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
		//����������ڣ��򷵻�ȫ����Ϣ
		return mapGo2Term.queryLsGo2Term(queryGo2Term);
 	}
	/**
	 * ������Ҫ��ȡGO��Ϣ
	 * �������û���趨goID��goIDquery�Ļ����ͻ᷵��ȫ����Ϣ��<br>
	 * <b>ע����Щ��Ϣ��û��GOIDquery</b>
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
