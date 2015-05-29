package com.novelbio.database.domain.geneanno;

import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.novelbio.analysis.seq.mirna.ListMiRNAdat;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.SpeciesFile.ExtractSmallRNASeq;
import com.novelbio.database.service.servgeneanno.IManageSpecies;
import com.novelbio.database.service.servgeneanno.ManageSpecies;
import com.novelbio.generalConf.PathDetailNBC;

/**
 * 有关taxID的表格
 * @author zong0jie
 */
@Document(collection = "taxinfo")
public class TaxInfo implements Cloneable {
	/** NCBI的物种ID */
	@Id
	private int taxID;
	/** KEGG上的缩写 */
	@Indexed
	private String abbr;
	/** 拉丁名 */
	private String latin;
	/** 常用名 */
	private String comName;
	/** 中文名 */
	@Indexed
	private String chnName;
	/** 是否有miRNA */
	private Boolean isHaveMiRNA;
	/** 核糖体rna的序列文件 */
	private String rrnaFile;
	
	public int getTaxID() {
		return taxID;
	}
	public void setTaxID(int taxID) {
		this.taxID = taxID;
	}
	public String getAbbr() {
		return abbr;
	}
	public void setAbbr(String abbr) {
		this.abbr = abbr;
	}
	public String getLatin() {
		return latin;
	}
	public void setLatin(String latin) {
		this.latin = latin;
	}
	public String getComName() {
		return comName;
	}
	public void setComName(String comName) {
		this.comName = comName;
	}
	public String getChnName() {
		return chnName;
	}
	public void setChnName(String chnName) {
		this.chnName = chnName;
	}
	public Boolean getIsHaveMiRNA() {
		return isHaveMiRNA;
	}
	public void setIsHaveMiRNA(Boolean isHaveMiRNA) {
		this.isHaveMiRNA = isHaveMiRNA;
	}
	public String getRrnaFile() {
		return rrnaFile;
	}
	public void setRrnaFile(String rrnaFile) {
		this.rrnaFile = rrnaFile;
	}
	
	@Override
	public TaxInfo clone()  {
		try {
			return (TaxInfo) super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * @param speciesName 物种的拉丁名
	 */
	public String getLatinName_2Word() {
		String result = null;
		String latin = getLatin();
		String[] names = latin.split(" ");
		if (names.length > 1) {
			result = names[0] + " " + names[1];
		} else {
			result = latin;
		}
		return result;
	}
	
	/**
	 * 返回绝对路径
	 * @return
	 * 0: miRNAfile<br>
	 * 1: miRNAhairpinFile
	 */
	public String[] fetchMiRNAseq() {
		String pathParent = PathDetailNBC.getGenomePath();
		String node = "miRNA/";
		String genomePath = node + taxID + FileOperate.getSepPath();
		String miRNAfile = pathParent + genomePath + "miRNA.fa";
		String miRNAhairpinFile = pathParent + genomePath + "miRNAhairpin.fa";
		if (!FileOperate.isFileExistAndBigThanSize(miRNAfile,10) || !FileOperate.isFileExistAndBigThanSize(miRNAhairpinFile,10)) {
			FileOperate.createFolders(FileOperate.getParentPathNameWithSep(miRNAfile));
			ExtractSmallRNASeq extractSmallRNASeq = new ExtractSmallRNASeq();
			extractSmallRNASeq.setOutMatureRNA(miRNAfile);
			extractSmallRNASeq.setOutHairpinRNA(miRNAhairpinFile);
			extractSmallRNASeq.setMiRNAdata(PathDetailNBC.getMiRNADat(), getLatinName_2Word());
			extractSmallRNASeq.getSeq();
		}
		if (!FileOperate.isFileExistAndBigThanSize(miRNAhairpinFile, 0)) {
			FileOperate.DeleteFileFolder(miRNAhairpinFile);
			FileOperate.DeleteFileFolder(miRNAfile);
			miRNAhairpinFile = null;
			miRNAfile = null;
		}
		return new String[]{miRNAfile, miRNAhairpinFile};
	}
}
