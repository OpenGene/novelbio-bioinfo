package com.novelbio.database.updatedb.database;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.BlastFileInfo;
import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.species.Species;
import com.novelbio.database.service.servgeneanno.ManageBlastInfo;

public class BlastUp2DB extends ImportPerLine {
	private static final Logger logger = Logger.getLogger(BlastUp2DB.class);
	
	int queryIDType = GeneID.IDTYPE_ACCID;
	int blastIDType = GeneID.IDTYPE_ACCID;
	BlastFileInfo blastFileInfo = new BlastFileInfo();
	ManageBlastInfo manageBlastInfo = ManageBlastInfo.getInstance();
	
	String usrid;
	
	/** 检查行数超过15行有问题就可以报错了 */
	int linNum = 15;
	
	public  BlastUp2DB() {
		this.readFromLine = 1;
		setReadFromLine(1);
	}
	
	/** true 导入数据库，false导入缓存
	 * 默认false导入缓存
	 */
	public void setUpdate(boolean update) {
		blastFileInfo.setTmp(!update);
	}
	/**
	 * 为null表示不设定usrID
	 * @param usrid
	 */
	public void setUsrid(String usrid) {
		this.usrid = usrid;
	}
	/**
	 * 导入单个文件时，设定taxID
	 * @param taxID
	 */
	public void setTaxID(int taxID) {
		this.taxID = taxID;
		blastFileInfo.setQueryTaxID(taxID);
	}
	/**
	 * 设定物种名，taxID由物种名的hashcode获得
	 * @param taxName
	 */
	public void setTaxName(String taxName) {
		this.taxID = Math.abs(taxName.hashCode());
		blastFileInfo.setQueryTaxID(taxName);
	}
	/**
	 * blast到的物种ID
	 * @param subTaxID
	 */
	public void setSubTaxID(int subTaxID) {
		blastFileInfo.setSubjectTaxID(subTaxID);
	}
	
	/**
	 * 第一列，是accID还是geneID还是UniID
	 * @param IDtype 默认是CopedID.IDTYPE_ACCID
	 * @return
	 */
	public void setQueryIDType(int IDtypeQ) {
		this.queryIDType = IDtypeQ;
	}
	public void setUserID(String userID) {
		blastFileInfo.setUserID(userID);
	}
	/**
	 * blast到的ID是accID还是geneID还是UniID
	 * @param blastID 默认是CopedID.IDTYPE_ACCID
	 */
	public void setBlastIDType(int IDtypeS) {
		this.blastIDType = IDtypeS;
	}
	public boolean updateFile(String gene2AccFile) {
		blastFileInfo.setFileName(gene2AccFile);
		blastFileInfo.setUserID(usrid);
		manageBlastInfo.saveBlastFile(blastFileInfo);
		super.updateFile(gene2AccFile);
		return true;
	}
	
	/**
	 * 返回错误信息
	 * @param gene2AccFile
	 * @return
	 */
	public String checkFile(String gene2AccFile) {
		if (!FileOperate.isFileExistAndBigThanSize(gene2AccFile, 0)) {
			return "file is not exist:" + gene2AccFile;
		}
		
		String errorInfo = null;
		TxtReadandWrite txtRead = new TxtReadandWrite(gene2AccFile);
		List<String> lsBlastLines = txtRead.readFirstLines(100);
		txtRead.close();
		int notMatchQueryTaxID = 0, notMatchSubjectTaxID = 0;
		Set<Integer> setTaxIDquery = new HashSet<>();
		Set<Integer> setTaxIDsub = new HashSet<>();
		int taxIDS = blastFileInfo.getSubjectTaxID();
		for (String lineContent : lsBlastLines) {
			String[] ss = lineContent.split("\t");
			int idRealQ = getID(ss[0], taxID, false);
			int idRealS = getID(ss[1], taxIDS, ss[1].contains("|"));
			if (idRealQ != taxID) {
				setTaxIDquery.add(idRealQ);
				notMatchQueryTaxID++;
			}
			if (idRealS != taxIDS) {
				setTaxIDsub.add(idRealS);
				notMatchSubjectTaxID++;
			}
		}
		
		if (notMatchQueryTaxID > linNum && notMatchSubjectTaxID > linNum) {
			errorInfo = "query speciesID and subject speciesID may error\n " +
					"query speciesID:" + taxID + " but was:" + getSetSpeciesName(setTaxIDquery) +
							"\nsubject speciesID:" +  taxIDS + " but was:" + getSetSpeciesName(setTaxIDsub);
		} else if (notMatchQueryTaxID > linNum) {
			errorInfo = "query speciesID may error\n" +
					"query speciesID:" + taxID + " but was:" + getSetSpeciesName(setTaxIDquery);
		} else if (notMatchSubjectTaxID > linNum) {
			errorInfo = "subject speciesID may error\n" +
					"subject speciesID:" + taxIDS + " but was:" + getSetSpeciesName(setTaxIDsub);
		}
		return errorInfo;
	}
	
	private Set<String> getSetSpeciesName(Set<Integer> setTaxID) {
		Set<String> setSpeciesName = new HashSet<>();
		for (Integer taxID : setTaxID) {
			Species species = new Species(taxID);
			setSpeciesName.add(species.getCommonName() + ", " + species.getNameLatin());
		}
		return setSpeciesName;
	}
	
	/**
	 * @param ID
	 * @param taxID
	 * @param isBlastType
	 * @return 看输入的taxID是什么
	 */
	private int getID(String ID, int taxID, boolean isBlastType) {
		if (queryIDType == GeneID.IDTYPE_ACCID) {
			List<GeneID> lsGeneIDs = GeneID.createLsCopedID(ID, 0, isBlastType);
			if (lsGeneIDs.size() == 1 && lsGeneIDs.get(0).getTaxID() != 0 && lsGeneIDs.get(0).getTaxID() != taxID) {
				return lsGeneIDs.get(0).getTaxID();
			}
		} else {
			GeneID geneID = new GeneID(queryIDType, ID, 0);
			if (geneID.getTaxID() !=0 && geneID.getTaxID() != taxID) {
				return geneID.getTaxID();
			}
		}
		return taxID;
	}
	
	@Override
	boolean impPerLine(String lineContent) {
		BlastInfo blastInfo = new BlastInfo(true, taxID, queryIDType == GeneID.IDTYPE_ACCID, 
				blastFileInfo.getSubjectTaxID(), blastIDType == GeneID.IDTYPE_ACCID, lineContent);
		
		blastInfo.setBlastFileInfo(blastFileInfo);
		try {
			manageBlastInfo.updateBlast(blastInfo);
		} catch (Exception e) {
			logger.error("import db error", e);
			return false;
		}
		
		return true;
	}
	
	public static class BlastUpdateException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public BlastUpdateException(String info) {
			super(info);
		}
	}
	
}
