package com.novelbio.database.domain.geneanno;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.domain.geneanno.GOtype.GORelation;
import com.novelbio.database.service.servgeneanno.ManageGo2Term;

/**
 * 重写了equal和hashcode
 * @author zong0jie
 *
 */
@Document(collection = "go2term")
public class Go2Term implements Cloneable {
	
	@Indexed(unique = true)
    private Set<String> setQueryGoID = new HashSet<String>();
	@Indexed
	private String GoID;
	private String GoTerm;
	private String GoType;
	private Map<String, GORelation> mapParentGO2Relate = new HashMap<String, GORelation>();;
	private Map<String, GORelation> mapChildGO2Relate = new HashMap<String, GORelation>();;
	private String Definition;

	/**
	 * 存储本GOID与别的GOID之间的关系
	 * 譬如RELATION_IS，RELATION_PARTOF等
	 * 必须是两个GO之间才会有该信息
	 */
	@Transient
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
	public void addParent(String goID, GORelation relation ) {
		mapParentGO2Relate.put(goID, relation);
	}
	/**
	 * 设定其下游子类GO的信息，可以连续设定，新设定的会追加而不是覆盖
	 * @param go2Term
	 * @param relation 必须是RELATION中的一类
	 */
	public void addChild(String goID, GORelation relation ) {
		mapChildGO2Relate.put(goID, relation);
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
		ManageGo2Term manageGo2Term = new ManageGo2Term();
		HashSet<Go2Term> hashResult = new HashSet<Go2Term>();
		if (mapParentChildGO2Relation == null) {
			return hashResult;
		}
		for (String goID : mapParentChildGO2Relation.keySet()) {
			Go2Term go2TermNew = manageGo2Term.queryGo2Term(goID).clone();
			go2TermNew.setRelation(mapParentChildGO2Relation.get(goID));
			hashResult.add(go2TermNew);
		}
		return hashResult;
	}
	
	public Set<String> getGoIDQuery() {
		return setQueryGoID;
	}
	public void addGoIDQuery(String GoIDQuery) {
		if (GoIDQuery != null && !GoIDQuery.equals("")) {
			setQueryGoID.add(GoIDQuery);
		}
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
		setQueryGoID.add(GoID);
	}
	
	public String getGoTerm() {
		return GoTerm;
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
		return GOtype.getMapStrShort2Gotype().get(GoType);
	}
	public void setGOtype(GOtype gotype) {
		this.GoType = gotype.getOneWord();
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
		if (GoID.trim().equalsIgnoreCase(otherObj.getGoID().trim())) {
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
		
		if (ArrayOperate.compareString(GoType, otherObj.GoType)
			&&
			ArrayOperate.compareString(GoID, otherObj.getGoID())
			&&
			ArrayOperate.compareString(GoTerm, otherObj.getGoTerm())	
			&&
			ArrayOperate.compareString(Definition, otherObj.getDefinition())
			&&
			mapChildGO2Relate.equals(otherObj.mapChildGO2Relate)
			&&
			mapParentGO2Relate.equals(otherObj.mapParentGO2Relate)
				) {
			return true;
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
	 * 添加信息并返回是否需要更新
	 * @param go2Term
	 * @return
	 */
	public boolean addInfo(Go2Term go2Term) {
		boolean update = false;
		update = update || AGeneInfo.addInfo(setQueryGoID, go2Term.setQueryGoID);
		update = update || AGeneInfo.addInfo(mapChildGO2Relate.keySet(), go2Term.mapChildGO2Relate.keySet());
		update = update || AGeneInfo.addInfo(mapParentGO2Relate.keySet(), go2Term.mapParentGO2Relate.keySet());
		
		if (go2Term.GoType != null && !go2Term.GoType.equals(GoType)) {
			GoType = go2Term.GoType;
			update = true;
		}
		
		if (go2Term.Definition != null && !go2Term.Definition.equals(Definition)) {
			Definition = go2Term.Definition;
			update = true;
		}
		if (go2Term.GoID != null && !go2Term.GoID.equals(GoID)) {
			GoID = go2Term.GoID;
			update = true;
		}
		
		return update;
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
			go2Term.GoType = GoType;
			go2Term.GoID = GoID;
			go2Term.gorelation = gorelation;
			go2Term.GoTerm = GoTerm;
			go2Term.setQueryGoID = new HashSet<String>();
			for (String goID : setQueryGoID) {
				go2Term.setQueryGoID.add(goID);
			}
			
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return go2Term;
	}
}
