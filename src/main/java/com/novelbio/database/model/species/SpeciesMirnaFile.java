package com.novelbio.database.model.species;

import com.novelbio.analysis.seq.mirna.ListMiRNAdat;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.TaxInfo;
import com.novelbio.database.service.servgeneanno.ManageSpecies;
import com.novelbio.generalConf.PathDetailNBC;

/** 
 * TODO 这里没有考虑miRNAdat的版本
 * 
 * @author novelbio
 *
 */
public class SpeciesMirnaFile {
	TaxInfo taxInfo;
	private static final String pathNode = "miRNA/";
	
	public SpeciesMirnaFile(TaxInfo taxInfo) {
		this.taxInfo = taxInfo;
	}
	
	/** 提取miRNA序列 */
	public void abstractMiRNA() {
		String miRNAfile = getMiRNAmatureFile();
		String miRNAhairpinFile = getMiRNAhairpinFile();
		boolean isHaveMirna = ListMiRNAdat.isContainMiRNA(taxInfo.getLatinName_2Word(), PathDetailNBC.getMiRNADat());
		if (!isHaveMirna) {
			taxInfo.setIsHaveMiRNA(false);
			ManageSpecies.getInstance().saveTaxInfo(taxInfo);
			return;
		}
		
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
		taxInfo.setIsHaveMiRNA(true);
		ManageSpecies.getInstance().saveTaxInfo(taxInfo);
	}
	
	/** 返回绝对路径 */
	public String getMiRNAmatureFile() {
		if (!taxInfo.isHaveMiRNA()) return null;
        
		String pathParent = PathDetailNBC.getGenomePath();
		String genomePath = pathNode + taxInfo.getTaxID() + FileOperate.getSepPath();
		String miRNAfile = pathParent + genomePath + "miRNA.fa";
		return miRNAfile;
	}
	
	/** 返回绝对路径 */
	public String getMiRNAhairpinFile() {
		if (!taxInfo.isHaveMiRNA()) return null;
		
		String pathParent = PathDetailNBC.getGenomePath();
		String genomePath = pathNode + taxInfo.getTaxID() + FileOperate.getSepPath();
		String miRNAhairpinFile = pathParent + genomePath + "miRNAhairpin.fa";
		return miRNAhairpinFile;
	}
}
