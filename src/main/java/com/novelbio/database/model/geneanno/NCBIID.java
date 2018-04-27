package com.novelbio.database.model.geneanno;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import com.novelbio.database.domain.modgeneid.GeneID;

/**
 * 重写了equal，只要两个NCBIID初始化，并且他们的geneID都不为0，就比较geneID，一样的就认为两个一样
 * @author zong0jie
 *
 */
@Document(collection="ncbiid")
@CompoundIndexes({
    @CompoundIndex(unique = true, name = "gene_acc_tax_idx", def = "{'geneID': 1, 'accID': -1, 'taxID': 1}"),
    @CompoundIndex(unique = false, name = "acc_tax_idx", def = "{'accID': 1, 'taxID': -1}")
 })
public class NCBIID extends AgeneUniID{
	private long geneID;
	
	protected NCBIID() {}
	
	public long getGeneId() {
		return geneID;
	}
	public void setGeneId(long geneID) {
		this.geneID = geneID;
	}
	

	/**
	 * 只要两个ncbiid的geneID相同，就认为这两个NCBIID相同
	 * 但是如果geneID为0，也就是NCBIID根本没有初始化，那么直接返回false
	 * 	@Override
	 */
	public boolean equals(Object obj) {
		if (this == obj) return true;
		
		if (obj == null) return false;
		
		if (getClass() != obj.getClass()) return false;
		
		NCBIID otherObj = (NCBIID)obj;
		if (geneID == 0 || otherObj.getGeneId() == 0) {
			return false;
		}
		return geneID == otherObj.getGeneId();
	}
	/**
	 * 重写hashcode，也是仅针对geneID
	 */
	public int hashCode(){ 
		return Long.valueOf(geneID).hashCode(); 
	}
	@Override
	public String getGenUniID() {
		return geneID + "";
	}
	@Override
	public void setGenUniID(String genUniID) {
		try {
			setGeneId(Long.parseLong(genUniID));
		} catch (Exception e) {
			setGeneId(0);
		}
	}
	@Override
	public Integer getGeneIDtype() {
		return GeneID.IDTYPE_GENEID;
	}
	
}
