package com.novelbio.database.domain.omim;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.novelbio.database.domain.geneanno.GeneInfo;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.modgeneid.GeneIDInt;
import com.novelbio.database.mongorepo.omim.RepoGeneMIMInfo;
import com.novelbio.database.service.SpringFactoryBioinfo;

@Document(collection = "omimGene")
public class GeneMIM implements Serializable {
	private static final long serialVersionUID = -2516785092527973673L;
	
	@Id
	private String id;
	/** gene MIM 号 */
	@Indexed
	private int geneMimId;
	/** gene ID */
	private int geneId;
	/** Cytogenetic location */
	private String cytLoc;
	
	/** 数据库中记载的最新的，有记录的MIMID */
	private int uniMimId;
	
	
	public void setGeneId(int geneId) {
		this.geneId = geneId;
	}
	public int getGeneId() {
		return geneId;
	}
	public void setGeneMimId(int geneMimId) {
		this.geneMimId = geneMimId;
	}
	public int getGeneMimId() {
		return geneMimId;
	}
	public void setUniMimId(int uniMimId) {
		this.uniMimId = uniMimId;
	}
	public int getUniMimId() {
		return uniMimId;
	}
	public void setCytLoc(String cytLoc) {
		this.cytLoc = cytLoc;
	}
	public String getCytLoc() {
		return cytLoc;
	}

	public static GeneMIM getInstanceFromGeneOmim(String content) {
		if (content.equals("")) {
			return null;
		}
		String[] arrGeneOmimLine = content.split("\\|");
		GeneMIM geneMIM = new GeneMIM();
		
		geneMIM.setGeneId(0);
		geneMIM.setCytLoc(arrGeneOmimLine[4]);
		geneMIM.setGeneMimId(Integer.parseInt(arrGeneOmimLine[8]));
		geneMIM.setUniMimId(Integer.parseInt(arrGeneOmimLine[8]));			
		return geneMIM;
	}
	
	private static RepoGeneMIMInfo repo() {
		 return SpringFactoryBioinfo.getBean(RepoGeneMIMInfo.class);
	 }
	 
	 public static GeneMIM findGeneInfByMimId(String id) {
		 return repo().findOne(id);
	 }

	 public boolean remove() {
		 try {
			 repo().delete(id);
		 } catch (Exception e) {
			 return false;
		 }
		 return true;
	 }
}
