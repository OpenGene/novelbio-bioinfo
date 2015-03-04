package com.novelbio.database.updatedb.database;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.analysis.annotation.blast.BlastType;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.BlastFileInfo;
import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.species.Species;
import com.novelbio.database.service.servgeneanno.ManageBlastInfo;

public class BlastUp2DB {
	private static final Logger logger = Logger.getLogger(BlastUp2DB.class);
	
	int queryIDType = GeneID.IDTYPE_ACCID;
	int blastIDType = GeneID.IDTYPE_ACCID;
	BlastFileInfo blastFileInfo = new BlastFileInfo();
	ManageBlastInfo manageBlastInfo = ManageBlastInfo.getInstance();
		
	/** 出错行数超过15行有问题就可以报错了 */
	int linNum = 15;
	
	public static void main(String[] args) {
		BlastUp2DB blastUp2DB = new BlastUp2DB();
		
		BlastFileInfo blastFileInfo = new BlastFileInfo();
		blastFileInfo.setTmp(false);
		
		blastFileInfo.setQueryTaxID(508771);
		blastFileInfo.setSubjectTaxID(7227);
		blastFileInfo.setBlastType(BlastType.blastx);
		blastFileInfo.setFileName("/media/winE/sssss/apicomplexans2fruitfly_2014-08-11-11-44-2030283.2014-08-11-11-44-2030283");
		try {
			blastFileInfo.importAndSave();
		} catch (BlastFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		blastUp2DB.setBlastFileInfo(blastFileInfo, GeneID.IDTYPE_ACCID, GeneID.IDTYPE_ACCID);
		try {
			blastUp2DB.updateFile(false);
		} catch (BlastFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/** 这就是一步到位的设定 */
	public void setBlastFileInfo(BlastFileInfo blastFileInfo, int queryIdType, int blastIdType) {
		this.blastFileInfo = blastFileInfo;
		this.queryIDType = queryIdType;
		this.blastIDType = blastIdType;
	}
	
	public boolean updateFile() throws BlastFileException, IOException {
		return updateFile(true);
	}
	
	public boolean updateFile(boolean isSaveDb) throws BlastFileException, IOException {
		checkFile();
		blastFileInfo.save(isSaveDb);
		updateBlastInfo();
		return true;
	}

	
	/**
	 * 将指定的文件导入数据库，必须是每一行都能单独导入的表
	 * 如果需要导入多行，譬如amiGO的信息，请覆盖该方法
	 */
	private boolean updateBlastInfo() {
		TxtReadandWrite txtGene2Acc;
		txtGene2Acc = new TxtReadandWrite(blastFileInfo.getFileName(), false);
		
		//从第二行开始读取
		int num = 0;
		for (String content : txtGene2Acc.readlines()) {
			if (content.startsWith("#") || content.startsWith("!")) {
				continue;
			}
			try {
				impPerLine(content);
			} catch (Exception e) {
				e.printStackTrace();
			}
	
			num++;
			if (num%300000 == 0) {
				logger.info(blastFileInfo.getFileName() + " import line number:" + num);
			}
		}
		txtGene2Acc.close();
		logger.info("finished import file " + blastFileInfo.getFileName());
		return true;
	}
	
	/**
	 * 返回错误信息
	 * @param gene2AccFile
	 * @return
	 * @throws BlastFileException 
	 */
	public void checkFile() throws BlastFileException {
		String gene2AccFile = blastFileInfo.getFileName();
		if (!FileOperate.isFileExistAndBigThanSize(gene2AccFile, 0)) {
			FileOperate.DeleteFileFolder(gene2AccFile);
			throw new BlastFileException("file is not exist:" + gene2AccFile);
		}
		int taxIdQ = blastFileInfo.getQueryTaxID();
		int taxIdS = blastFileInfo.getSubjectTaxID();
		String errorInfo = null;
		TxtReadandWrite txtRead = new TxtReadandWrite(gene2AccFile);
		List<String> lsBlastLines = txtRead.readFirstLines(100);
		txtRead.close();
		int notMatchQueryTaxID = 0, notMatchSubjectTaxID = 0;
		Set<Integer> setTaxIDquery = new HashSet<>();
		Set<Integer> setTaxIDsub = new HashSet<>();
		int taxIDS = blastFileInfo.getSubjectTaxID();
		for (String lineContent : lsBlastLines) {
			if (lineContent.startsWith("#")) {
				continue;
			}
			String[] ss = lineContent.split("\t");
			if (ss.length != 12) {
				errorInfo = "file format is not correct";
				break;
			}
			int idRealQ = getID(ss[0], taxIdQ, false);
			int idRealS = getID(ss[1], taxIdS, ss[1].contains("|"));
			if (idRealQ != taxIdQ) {
				setTaxIDquery.add(idRealQ);
				notMatchQueryTaxID++;
			}
			if (idRealS != taxIDS) {
				setTaxIDsub.add(idRealS);
				notMatchSubjectTaxID++;
			}
		}
		
		if (errorInfo == null) {
			if (notMatchQueryTaxID > linNum && notMatchSubjectTaxID > linNum) {
				errorInfo = "query speciesID and subject speciesID may error\n " +
						"query speciesID:" + taxIdQ + " but was:" + getSetSpeciesName(setTaxIDquery) +
								"\nsubject speciesID:" +  taxIDS + " but was:" + getSetSpeciesName(setTaxIDsub);
			} else if (notMatchQueryTaxID > linNum) {
				errorInfo = "query speciesID may error\n" +
						"query speciesID:" + taxIdQ + " but was:" + getSetSpeciesName(setTaxIDquery);
			} else if (notMatchSubjectTaxID > linNum) {
				errorInfo = "subject speciesID may error\n" +
						"subject speciesID:" + taxIDS + " but was:" + getSetSpeciesName(setTaxIDsub);
			}
		}
		
		
		if (errorInfo != null) {
			FileOperate.DeleteFileFolder(blastFileInfo.realFileAndName());
			throw new BlastFileException(errorInfo);
		}
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
	
	boolean impPerLine(String lineContent) {
		BlastInfo blastInfo = new BlastInfo(true, blastFileInfo.getQueryTaxID(), queryIDType == GeneID.IDTYPE_ACCID, 
				blastFileInfo.getSubjectTaxID(), blastIDType == GeneID.IDTYPE_ACCID, lineContent);
		
		if (blastInfo.getQueryID().equals("TGME49218790")) {
			logger.debug("stop");
		}
		blastInfo.setBlastFileId(blastFileInfo.getId());
		try {
			manageBlastInfo.updateBlast(blastInfo);
		} catch (Exception e) {
			logger.error("import db error", e);
			return false;
		}
		
		return true;
	}
	
	public static class BlastFileException extends Exception {
		private static final long serialVersionUID = 1L;

		public BlastFileException(String info) {
			super(info);
		}
	}
	
}
