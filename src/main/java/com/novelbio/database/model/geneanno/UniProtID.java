package com.novelbio.database.model.geneanno;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import com.novelbio.database.domain.modgeneid.GeneID;

/** 重写了equal，只要两个UniProtID初始化，并且他们的UniID都不为null或""，就比较UniID，一样的就认为两个一样
 * 
 * @author zong0jie */
@Document(collection = "uniid")
@CompoundIndexes({
    @CompoundIndex(unique = true, name = "uni_acc_idx", def = "{'uniID': 1, 'accID': -1}"),
    @CompoundIndex(unique = false, name = "acc_tax_idx", def = "{'accID': 1, 'taxID': -1}")
 })
public class UniProtID extends AgeneUniID {
	private String uniID;

	protected UniProtID() {
	}

	public String getUniID() {
		return uniID;
	}
	
	/** 自动转成小写 */
	public void setUniID(String uniID) {
		this.uniID = uniID.toLowerCase();
	}

	/** 只要两个uniprotID的UniID相同，就认为这两个uniprotID相同
	 * 但是如果UniID为""或null，也就是uniprotID根本没有初始化，那么直接返回false
	 * 
	 * @Override */
	public boolean equals(Object obj) {
		if (this == obj) return true;

		if (obj == null) return false;

		if (getClass() != obj.getClass()) return false;

		UniProtID otherObj = (UniProtID) obj;
		if (uniID == null || uniID.trim().equals("") || otherObj.getUniID() == null || otherObj.getUniID().trim().equals("")) {
			return false;
		}
		return uniID.equals(otherObj.getUniID());
	}

	/** 重写hashcode，也是仅针对uniID */
	public int hashCode() {
		return uniID.hashCode();
	}
	
	/** 小写的 */
	@Override
	public String getGenUniID() {
		return uniID;
	}

	@Override
	public void setGenUniID(String genUniID) {
		setUniID(genUniID);
	}

	@Override
	public Integer getGeneIDtype() {
		return GeneID.IDTYPE_UNIID;
	}
}
