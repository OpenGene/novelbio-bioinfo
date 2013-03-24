package com.novelbio.database.domain.geneanno;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.springframework.data.mongodb.core.index.Indexed;

import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.domain.geneanno.GOtype.GORelation;
import com.novelbio.database.service.servgeneanno.ServGo2Term;

/**
 * 重写了equal和hashcode
 * @author zong0jie
 *
 */
public class Go2Term implements Cloneable {
	
	private ServGo2Term servGo2Term = new ServGo2Term();
	@Indexed(unique = true)
    private String queryGoID;
	@Indexed
	private String GoID;
	private String GoTerm;
	private String GoFunction;
	private Map<String, GORelation> mapParentGO2Relate = new HashMap<String, GORelation>();;
	private Map<String, GORelation> mapChildGO2Relate = new HashMap<String, GORelation>();;
	private String Definition;

	/**
	 * 存储本GOID与别的GOID之间的关系
	 * 譬如RELATION_IS，RELATION_PARTOF等
	 * 必须是两个GO之间才会有该信息
	 */
	private GORelation gorelation;
	/**
	 * 存储本GOID与别的GOID之间的关系
	 * 譬如RELATION_IS，RELATION_PARTOF等
	 * 必须是两个GO之间才会有该信息
	 */
	private void setRelation(GORelation gorelation) {
		this.gorelation = gorelation;
	}
	/**
	 * go信息的具体定义
	 * @param definition
	 */
	public void setDefinition(String definition) {
		Definition = definition.replace("\"", "");
	}
	/**
	 * go信息的具体定义
	 * @param definition
	 */
	public String getDefinition() {
		return Definition;
	}
	/**
	 * <b>只有getParent或者getChild才有的flag，</b>表示与其他GOID之间的关系<br>
	 * 如果是直接从数据库获得，则返回GORelation.NONE<br><br>
	 * 存储本GOID与别的GOID之间的关系<br>
	 * 譬如RELATION_IS，RELATION_PARTOF等<br>
	 * 必须是两个GO之间才会有该信息<br>
	 */
	public GORelation getRelation() {
		return gorelation;
	}
	/**
	 * 设定其上游父类GO的信息
	 * @param go2Term
	 * @param relation 必须是RELATION中的一类
	 */
	public void addParent(Go2Term go2Term, GORelation relation ) {
		mapParentGO2Relate.put(go2Term.getGoID(), relation);
	}
	/**
	 * 设定其上游父类GO的信息，可以连续设定，新设定的会追加而不是覆盖
	 * @param go2Term
	 * @param relation 必须是RELATION中的一类
	 */
	public void addParent(String goid, GORelation relation) {
		mapParentGO2Relate.put(goid, relation);
	}
	/**
	 * 设定其下游子类GO的信息，可以连续设定，新设定的会追加而不是覆盖
	 * @param go2Term
	 * @param relation 必须是RELATION中的一类
	 */
	public void addChild(Go2Term go2Term, GORelation relation ) {
		mapChildGO2Relate.put(go2Term.getGoID(), relation);
	}
	/**
	 * 设定其下游子类GO的信息
	 * 后设定的会覆盖前设定的
	 * @param go2Term
	 * @param relation 必须是RELATION中的一类
	 */
	public void addChild(String goid, GORelation relation ) {
		mapChildGO2Relate.put(goid, relation);
	}
	
	public HashSet<Go2Term> getParent() {
		return getParentChild(mapParentGO2Relate);
	}
	public HashSet<Go2Term> getChild() {
		return getParentChild(mapChildGO2Relate);
	}
	/**
	 * 返回父类
	 * @return
	 */
	private HashSet<Go2Term> getParentChild(Map<String, GORelation> mapParentChildGO2Relation) {
		HashSet<Go2Term> hashResult = new HashSet<Go2Term>();
		if (mapParentChildGO2Relation == null) {
			return hashResult;
		}
		for (String goTerm : mapParentChildGO2Relation.keySet()) {
			Go2Term go2Term = servGo2Term.queryGo2Term(goTerm);
			Go2Term go2TermNew = go2Term.clone();
			go2TermNew.setRelation(mapParentChildGO2Relation.get(go2Term));
			hashResult.add(go2TermNew);
		}
		return hashResult;
	}
	
	public String getGoIDQuery() {
		return queryGoID;
	}
	public void setGoIDQuery(String GoIDQuery) {
		this.queryGoID = GoIDQuery;
	}

	public String getGoID() {
		return GoID;
	}
	/**
	 * 常规设定
	 * @param GoID
	 */
	public void setGoID(String GoID) {
		this.GoID = GoID;
	}
	
	public String getGoTerm() {
		return GoTerm;
	}
	/**
	 * 全部读入内存后，hash访问。第一次速度慢，后面效率很高
	 * @param GOID
	 * @return
	 */
	public static Go2Term queryGo2Term(String GOID) {
		ServGo2Term servGo2Term = new ServGo2Term();
		return servGo2Term.queryGo2Term(GOID);
	}
	/**
	 * 通过访问数据库查询，效率相对低
	 * @param GOID
	 * @return
	 */
	public static Go2Term queryGo2TermDB(String GOID) {
		ServGo2Term servGo2Term = new ServGo2Term();
		Go2Term go2Term = new Go2Term();
		go2Term.setGoIDQuery(GOID);
		return servGo2Term.queryGo2Term(go2Term);
	}
	/**
	 * 通过访问数据库查询，效率相对低
	 * @param go2Term
	 * @return
	 */
	public static Go2Term queryGo2TermDB(Go2Term go2Term) {
		ServGo2Term servGo2Term = new ServGo2Term();
		return servGo2Term.queryGo2Term(go2Term);
	}
	public void setGoTerm(String GoTerm) {
		this.GoTerm = GoTerm;
	}  
	/**
	 * FUN_SHORT_BIO_P<br>
	 * FUN_SHORT_CEL_C<br>
	 * FUN_SHORT_MOL_F<br>
	 * @return
	 */
	public GOtype getGOtype() {
		return GOtype.getMapStrShort2Gotype().get(GoFunction);
	}
	public void setGOtype(GOtype gotype) {
		this.GoFunction = gotype.getOneWord();
	}
	/**
	 * 仅比较GOID
	 * 如果两个都是null，则返回false
	 * @param other
	 * @return
	 */
	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		
		if (other == null) return false;
		
		if (getClass() != other.getClass()) return false;
		Go2Term otherObj = (Go2Term)other;
		if (GoID == null) {
			return false;
		}
		if (otherObj.getGoID() == null) {
			return false;
		}
		if (GoID.trim().equals(otherObj.getGoID())) {
			return true;
		}
		return false;
	}
	/**
	 * 除了queryGOID和flag外，全部比较
	 * 如果两个都是null，则返回false
	 * @param other
	 * @return
	 */
	public boolean equalsAll(Object other) {
		if (this == other) return true;
		if (other == null) return false;
		if (getClass() != other.getClass()) return false;
		Go2Term otherObj = (Go2Term)other;
		
		if (cmpString(GoFunction, otherObj.GoFunction)
			&&
			cmpString(GoID, otherObj.getGoID())
			&&
			cmpString(GoTerm, otherObj.getGoTerm())	
			&&
			cmpString(Definition, otherObj.getDefinition())
			&&
			mapChildGO2Relate.equals(otherObj.mapChildGO2Relate)
			&&
			mapParentGO2Relate.equals(otherObj.mapParentGO2Relate)
				) {
			return true;
		}
		return false;
	}
	
	private boolean cmpString(String a, String b) {
		if (a == b) {
			return true;
		}
		if ((a == null && b != null) || (a != null && b == null)) {
			return false;
		}
		if (a != null && b != null) {
			return a.trim().equals(b.trim());
		}
		return false;
	}
	
	/**
	 * 	仅比较GOID
	 */
	@Override
	public int hashCode() {
		String result = GoID;
		return result.hashCode();
	}
	/**
	 * 整合升级模式，将go2term拆成goconvert和go2term分别进行升级 如果已经有了就不升级，
	 * 没有才升级 升级方式是简单的覆盖，
	 * 不像geneInfo有add等方式 不过如果go2term中其他都没有，则不升级
	 * @return
	 */
	public boolean update() {
		return servGo2Term.updateGo2TermComb(this);
	}
	
	public Go2Term clone() {
		Go2Term go2Term = null;
		try {
			go2Term = (Go2Term) super.clone();
			go2Term.mapChildGO2Relate = new HashMap<String, GOtype.GORelation>();
			for (String goID : mapChildGO2Relate.keySet()) {
				go2Term.mapChildGO2Relate.put(goID, mapChildGO2Relate.get(goID));
			}
			go2Term.mapParentGO2Relate = new HashMap<String, GOtype.GORelation>();
			for (String goID : mapParentGO2Relate.keySet()) {
				go2Term.mapParentGO2Relate.put(goID, mapParentGO2Relate.get(goID));
			}
			go2Term.Definition = Definition;
			go2Term.GoFunction = GoFunction;
			go2Term.GoID = GoID;
			go2Term.gorelation = gorelation;
			go2Term.GoTerm = GoTerm;
			go2Term.queryGoID = queryGoID;
			go2Term.servGo2Term = servGo2Term;
			
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return go2Term;
	}
}
