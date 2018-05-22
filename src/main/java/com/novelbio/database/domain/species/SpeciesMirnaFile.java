package com.novelbio.database.domain.species;

import com.novelbio.analysis.seq.mirna.ListMiRNAdat;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.species.TaxInfo;
import com.novelbio.database.service.servgeneanno.ManageSpecies;
import com.novelbio.generalconf.PathDetailNBC;

/** 
 * TODO 这里没有考虑miRNAdat的版本
 * 
 * @author novelbio
 *
 */
public class SpeciesMirnaFile {
	TaxInfo taxInfo;
	private static final String pathNode = "miRNA/";
	
	private String parentPath = PathDetailNBC.getGenomePath();
	private String miRnaDatFile = PathDetailNBC.getMiRNADat();
	public SpeciesMirnaFile(TaxInfo taxInfo) {
		this.taxInfo = taxInfo;
	}
	
	/** 仅用于测试 */
	protected void setParentPathAndMirnaFile(String parentPath, String miRnaDatFile) {
		this.parentPath = parentPath;
		this.miRnaDatFile = miRnaDatFile;
    }
	
	/** 提取miRNA序列 */
	public void extractMiRNA() {
		boolean isHaveMirna = isHaveAndExtractMiRNA();
		taxInfo.setIsHaveMiRNA(isHaveMirna);
		ManageSpecies.getInstance().saveTaxInfo(taxInfo);
	}
	
	/** 是否成功提取
	 * 暴露出来用于测试
	 * @return
	 */
	protected boolean isHaveAndExtractMiRNA() {
		boolean isHaveMirna = ListMiRNAdat.isContainMiRNA(taxInfo.getLatinName_2Word(), miRnaDatFile);
		if (!isHaveMirna) {
			return false;
		}
		
		taxInfo.setIsHaveMiRNA(true);
		String miRNAfile = getMiRNAmatureFile();
		String miRNAhairpinFile = getMiRNAhairpinFile();

		
		if (!FileOperate.isFileExistAndBigThanSize(miRNAfile,10) || !FileOperate.isFileExistAndBigThanSize(miRNAhairpinFile,10)) {
			FileOperate.createFolders(FileOperate.getParentPathNameWithSep(miRNAfile));
			ExtractSmallRNASeq extractSmallRNASeq = new ExtractSmallRNASeq();
			extractSmallRNASeq.setOutMatureRNA(miRNAfile);
			extractSmallRNASeq.setOutHairpinRNA(miRNAhairpinFile);
			extractSmallRNASeq.setMiRNAdata(PathDetailNBC.getMiRNADat(), taxInfo.getLatinName_2Word());
			extractSmallRNASeq.getSeq();
		}
		if (!FileOperate.isFileExistAndBigThanSize(miRNAhairpinFile, 0)) {
			throw new ExceptionNbcSpeciesFileAbstract("cannot abstract mirna for species " + taxInfo.getLatinName_2Word());
		}
		return true;
	}
	
	/** 返回绝对路径 */
	public String getMiRNAmatureFile() {
		if (!taxInfo.isHaveMiRNA()) return null;
        
		String pathParent = parentPath;
		String genomePath = pathNode + taxInfo.getTaxID() + FileOperate.getSepPath();
		String miRNAfile = pathParent + genomePath + "miRNA.fa";
		return miRNAfile;
	}
	
	/** 返回绝对路径 */
	public String getMiRNAhairpinFile() {
		if (!taxInfo.isHaveMiRNA()) return null;
		
		String pathParent = parentPath;
		String genomePath = pathNode + taxInfo.getTaxID() + FileOperate.getSepPath();
		String miRNAhairpinFile = pathParent + genomePath + "miRNAhairpin.fa";
		return miRNAhairpinFile;
	}
}
