package com.novelbio.database.domain.omim;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.mongorepo.omim.RepoMorbidMap;
import com.novelbio.database.service.SpringFactoryBioinfo;


@Document(collection = "morbidMap")
public class MorbidMap implements Serializable {
	private static final int taxID = 9606;
	
	@Id
	private String id;
	/** gene MIM 号 */
	@Indexed
	private int geneId;
	/** gene ID */
	private int geneMimId;
	/** 遗传学位置 */
	private String cytLoc;
	/** 孟德尔表型MIM ID */
	private int phenMimId;
	/** 疾病信息 */
	private List<String> listDis;
	/** 疾病信息 类型
	 * 1 the disorder was positioned by mapping of the wildtype gene;
	 * 2 the disease phenotype itself was mapped;
	 * 3 the molecular basis of the disorder is known; 
	 * 4 the disorder is a chromosome deletion or duplication syndrome;
	 * */
	private int disType;
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
	public int getPhenMimId() {
		return phenMimId;
	}
	public void setPhenMimId(int phenMimId) {
		this.phenMimId = phenMimId;
	}
	public void setCytLoc(String cytLoc) {
		this.cytLoc = cytLoc;
	}
	public String getCytLoc() {
		return cytLoc;
	}
	public List<String> getListDis() {
		return listDis;
	}
	public void setListDis(List<String> listDis) {
		this.listDis = listDis;
	}
	public void setDisType(int disType) {
		this.disType = disType;
	}
	public int getDisType() {
		return disType;
	}
	
	public static MorbidMap getInstanceFromOmimRecord(String content) {
		if (content.equals("")) {
			return null;
		}
		MorbidMap morbidMap = new MorbidMap();
		String[] arrMorbidMapLine = content.split("\\|");
		String geneName = "";
		if (arrMorbidMapLine[1].contains(",")) {
			geneName = arrMorbidMapLine[1].split(",")[0];
		} else {
			geneName = arrMorbidMapLine[1];
		}
		GeneID copedID = new GeneID(geneName, taxID, false);
		GeneMIM geneMIM = new GeneMIM();
		String geneID = copedID.getGeneUniID();
		if (!geneID.matches("[0-9]+")) {
			return null;
		}
		morbidMap.setGeneId(Integer.parseInt(geneID));
		int geneMimId = Integer.parseInt(arrMorbidMapLine[2]);
		morbidMap.setGeneMimId(geneMimId);
		morbidMap.setCytLoc(arrMorbidMapLine[3]);
		String[] arrDisease = arrMorbidMapLine[0].split(",");
		//疾病信息添加到morbidMap的疾病list中
		for (int i = 0; i < arrDisease.length - 1; i++) {
			morbidMap.addDis(arrDisease[i].trim());
		}
		//以下获取phenotype MIM ID
		String phenInfo = arrDisease[arrDisease.length - 1].trim();
		String[] arrPhen = phenInfo.split("\\s+");
		int phenMimId = 0;
		//如果该疾病信息中含有phenotype MIM ID则，提取phenotype MIM ID号，如果没有含有phenotype MIM ID号,则将最后一行疾病信息添加到morbidMap的疾病list中
		if (phenInfo.matches("^\\d{6}\\s+\\(\\d+\\)")) {
			phenMimId = Integer.parseInt(arrPhen[0]);
		} else {
			phenInfo = phenInfo.substring(0, phenInfo.length()-3);
			morbidMap.addDis(phenInfo.trim());
		}
		String disType = arrPhen[arrPhen.length - 1].replaceAll("[()]", "");
		morbidMap.setDisType(Integer.parseInt(disType));
		morbidMap.setPhenMimId(phenMimId);
		return morbidMap;
	}
	
	public void addDis(String dis) {
		if (listDis == null) {
			listDis = new ArrayList<String>();
		}
		this.listDis.add(dis);
	}
	private static RepoMorbidMap repo() {
		 return SpringFactoryBioinfo.getBean(RepoMorbidMap.class);
	 }
	 public static MorbidMap findInfByGeneId(int id) {
		 return repo().findOne(id);
	 }
		/** 根据完成情况查询分配的任务 */
		public static List<MorbidMap> findInfByDisease(String disease) {
			return repo().findInfByDisease(disease);
		}	
}


