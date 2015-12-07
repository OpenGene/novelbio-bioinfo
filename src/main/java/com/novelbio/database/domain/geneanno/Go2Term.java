package com.novelbio.database.domain.geneanno;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.data.annotation.Id;
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
	private static final Logger logger = Logger.getLogger(Go2Term.class);
	
	@Id
	String id;
	@Indexed(unique = true)
    private Set<String> setQueryGoID = new HashSet<String>();
	@Indexed
	private String goID;
	private String goTerm;
	private String goType;
	private Map<String, GORelation> mapParentGO2Relate = new HashMap<String, GORelation>();
	private Map<String, GORelation> mapChildGO2Relate = new HashMap<String, GORelation>();
	private String definition;

	/**
	 * 存储本GOID与别的GOID之间的关系
	 * 譬如RELATION_IS，RELATION_PARTOF等
	 * 必须是两个GO之间才会有该信息
	 */
	@Transient
	private GORelation goRelation;
	/**
	 * 存储本GOID与别的GOID之间的关系
	 * 譬如RELATION_IS，RELATION_PARTOF等
	 * 必须是两个GO之间才会有该信息
	 */
	private void setRelation(GORelation gorelation) {
		this.goRelation = gorelation;
	}
	/**
	 * go信息的具体定义
	 * @param definition
	 */
	public void setDefinition(String definition) {
		definition = definition.replace("\"", "");
	}
	/**
	 * go信息的具体定义
	 * @param definition
	 */
	public String getDefinition() {
		return definition;
	}
	/**
	 * <b>只有getParent或者getChild才有的flag，</b>表示与其他GOID之间的关系<br>
	 * 如果是直接从数据库获得，则返回GORelation.NONE<br><br>
	 * 存储本GOID与别的GOID之间的关系<br>
	 * 譬如RELATION_IS，RELATION_PARTOF等<br>
	 * 必须是两个GO之间才会有该信息<br>
	 */
	public GORelation getRelation() {
		return goRelation;
	}
	/**
	 * 设定其上游父类GO的信息
	 * @param go2Term
	 * @param relation 必须是RELATION中的一类
	 */
	public void addParent(String goID, GORelation relation ) {
		mapParentGO2Relate.put(goID.toUpperCase(), relation);
	}
	/**
	 * 设定其下游子类GO的信息，可以连续设定，新设定的会追加而不是覆盖
	 * @param go2Term
	 * @param relation 必须是RELATION中的一类
	 */
	public void addChild(String goID, GORelation relation ) {
		mapChildGO2Relate.put(goID.toUpperCase(), relation);
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
		ManageGo2Term manageGo2Term = ManageGo2Term.getInstance();
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
		return goID;
	}
	/**
	 * 常规设定
	 * @param GoID
	 */
	public void setGoID(String GoID) {
		this.goID = GoID.trim().toUpperCase();
		setQueryGoID.add(this.goID);
	}
	
	public String getGoTerm() {
		return goTerm;
	}

	public void setGoTerm(String GoTerm) {
		this.goTerm = GoTerm;
	}  
	/**
	 * FUN_SHORT_BIO_P<br>
	 * FUN_SHORT_CEL_C<br>
	 * FUN_SHORT_MOL_F<br>
	 * @return
	 */
	public GOtype getGOtype() {
		return GOtype.getMapStrShort2Gotype().get(goType);
	}
	public void setGOtype(GOtype gotype) {
		this.goType = gotype.getOneWord();
	}
	
	/**
	 * 返回第几级GO
	 * @param level 如果level小于等于0，则返回自身
	 * @return
	 */
	public Go2Term getGOlevel(int level) {
		if (level <= 0) {
			return this;
		}
		Queue<Go2Term> queueGo2Terms = new LinkedList<Go2Term>();
		Go2Term goTermParent = this;
		while ((goTermParent = goTermParent.getOneParentGo2Term()) != null) {
			queueGo2Terms.add(goTermParent);
			if (queueGo2Terms.size() > level) {
				queueGo2Terms.poll();
			}
		}
		
		if (queueGo2Terms.size() == level) {
			return queueGo2Terms.peek();
		} else {
			return null;
		}
	}
	
	/**获取一个父级的Go，根据和当前Go的关系选择，优先is，其次REGULATE，再次REGULATE_NEG或者REGULATE_POS，最后PART_OF，没有返回null；*/
	private Go2Term getOneParentGo2Term() {
		Set<Go2Term> setGo2Terms = getParent();
		if (setGo2Terms.size() == 0) {
			return null;
		}
		for (Go2Term go2Term2 : setGo2Terms) {
			if (go2Term2.getRelation() == GORelation.IS) {
				return go2Term2;
			}
		}
		for (Go2Term go2Term2 : setGo2Terms) {
			if (go2Term2.getRelation() == GORelation.REGULATE) {
				return go2Term2;
			}
		}
		for (Go2Term go2Term2 : setGo2Terms) {
			if (go2Term2.getRelation() == GORelation.REGULATE_NEG
					|| go2Term2.getRelation() == GORelation.REGULATE_POS) {
				return go2Term2;
			}
		}
		for (Go2Term go2Term2 : setGo2Terms) {
			if (go2Term2.getRelation() == GORelation.PART_OF) {
				return go2Term2;
			}
		}
		logger.error("该Term的父级出现新的Relation" + getGoID());
		return null;
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
		if (goID == null) {
			return false;
		}
		if (otherObj.getGoID() == null) {
			return false;
		}
		if (goID.equals(otherObj.goID)) {
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
		
		if (ArrayOperate.compareString(goType, otherObj.goType)
			&&
			ArrayOperate.compareString(goID, otherObj.goID)
			&&
			ArrayOperate.compareString(goTerm, otherObj.goTerm)	
			&&
			ArrayOperate.compareString(definition, otherObj.definition)
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
		String result = goID;
		return result.hashCode();
	}

	/**
	 * 添加信息并返回是否需要更新
	 * @param go2Term
	 * @return
	 */
	public boolean addInfo(Go2Term go2Term) {
		boolean update = false;
		update = AGeneInfo.addInfo(setQueryGoID, go2Term.setQueryGoID) || update;
		update = AGeneInfo.addInfo(mapChildGO2Relate, go2Term.mapChildGO2Relate) || update;
		update = AGeneInfo.addInfo(mapParentGO2Relate, go2Term.mapParentGO2Relate) || update;
		
		if (go2Term.goType != null && !go2Term.goType.equals(goType)) {
			goType = go2Term.goType;
			update = true;
		}
		
		if (go2Term.definition != null && !go2Term.definition.equals(definition)) {
			definition = go2Term.definition;
			update = true;
		}
		if (go2Term.goID != null && !go2Term.goID.equals(goID)) {
			goID = go2Term.goID;
			update = true;
		}
		
		return update;
	}
	
	/**
	 * ID也一起clone
	 */
	public Go2Term clone() {
		Go2Term go2Term = null;
		try {
			go2Term = (Go2Term) super.clone();
			go2Term.id = id;
			go2Term.mapChildGO2Relate = new HashMap<String, GOtype.GORelation>();
			for (String goID : mapChildGO2Relate.keySet()) {
				go2Term.mapChildGO2Relate.put(goID, mapChildGO2Relate.get(goID));
			}
			go2Term.mapParentGO2Relate = new HashMap<String, GOtype.GORelation>();
			for (String goID : mapParentGO2Relate.keySet()) {
				go2Term.mapParentGO2Relate.put(goID, mapParentGO2Relate.get(goID));
			}
			go2Term.definition = definition;
			go2Term.goType = goType;
			go2Term.goID = goID;
			go2Term.goRelation = goRelation;
			go2Term.goTerm = goTerm;
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
