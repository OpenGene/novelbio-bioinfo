package com.novelbio.analysis.seq.genome;

import java.io.Closeable;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.genome.gffoperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffoperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffoperate.GffHashGeneAbs;
import com.novelbio.analysis.seq.genome.gffoperate.GffType;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.species.Species;
/**
 * GffHashGene和SeqHash都是static，也就是一次只能对一个物种进行分析
 * MapReads不是static，也就是可以同时处理多个mapping文件
 * @author zong0jie
 *
 */
public class GffChrAbs implements Closeable {
	private static final Logger logger = Logger.getLogger(GffChrAbs.class);
	
	GffHashGene gffHashGene = null;
	SeqHash seqHash = null;
	Species species;

	public GffChrAbs() {}

	public GffChrAbs(Species species) {
		setSpecies(species);
	}
	
	/**
	 *  查找数据库的方法载入gff文件，占用内存少，但是效率稍低，不适用于大量search，
	 * 并且gffhash提取上一个和下一个gene也不能实现
	 * @param species
	 * @param searchDB 是否查找数据库
	 */
	public GffChrAbs(Species species, boolean searchDB) {
		//TODO 还没实现
		setSpecies(species);
	}
	
	public GffChrAbs(int taxID) {
		setTaxID(taxID);
	}

	public void setTaxID(int taxID) {
		this.species = new Species(taxID);
		setGffFile(species.getTaxID(), species.getVersion(), species.getGffDB(), species.getGffType(), species.getGffFile());
		setChrFile(species.getChromSeq(), " ");
	}
	
	/**
	 * 如果本GffChrAbs已经close过了，可以重置species来打开，并且效率较高
	 * @param species
	 */
	public void setSpecies(Species species) {

		if (this.species != null && this.species.equals(species) && this.species.getVersion().equals(species.getVersion()) 
				&& this.species.getGffDB().equals(species.getGffDB())) {
			return;
		}
		if (species == null || species.getTaxID() == 0) {
			return;
		}
		
		this.species = species.clone();
		if (species.getGffFile() != null) {
			setGffFile(species.getTaxID(), species.getVersion(), species.getGffDB(), species.getGffType(), species.getGffFile());
		}
		setChrFile(species.getChromSeq(), " ");
	}
	
	/**
	 * 仅初始化gff文件
	 * @param species
	 */
	public void setSpeciesGff(Species species) {
		if (species == null || species.getTaxID() == 0) {
			return;
		}

		this.species = species.clone();
		if (species.getGffFile() != null) {
			setGffFile(species.getTaxID(), species.getVersion(), species.getGffDB(), species.getGffType(), species.getGffFile());
		}
	}
	/**
	 * 仅初始化chr文件
	 * @param species
	 */
	public void setSpeciesChr(Species species) {
		if (species == null || species.getTaxID() == 0) {
			return;
		}

		this.species = species.clone();
		setChrFile(species.getChromSeq(), " ");
	}
	
	public void setGffHash(GffHashGene gffHashGene) {
		this.gffHashGene = gffHashGene;
	}
	public void setGffHash(GffHashGeneAbs gffHashGeneAbs) {
		GffHashGene gffHashGene = new GffHashGene();
		gffHashGene.setGffHashGene(gffHashGeneAbs);
		this.gffHashGene = gffHashGene;
	}
	public void setSeqHash(SeqHash seqHash) {
		close();
		this.seqHash = seqHash;
	}

	/** 如果没有设定species，就返回一个全新的species，并且其taxID == 0 */
	public Species getSpecies() {
		if (species == null) {
			return new Species();
		}
		return species;
	}

	public int getTaxID() {
		if (species == null) {
			return 0;
		}
		return species.getTaxID();
	}

	public GffHashGene getGffHashGene() {
		return gffHashGene;
	}

	public SeqHash getSeqHash() {
		return seqHash;
	}
	private void setGffFile(int taxID, String version, String dbinfo, GffType gffType, String gffFile) {
		if (FileOperate.isFileExistAndNotDir(gffFile)) {
			gffHashGene = new GffHashGene(taxID, version, dbinfo, gffType, gffFile, taxID == 7227);
		} else {
			throw new ExceptionNbcGFF(gffFile + " GffFile is not exist");
		}
	}
	
	/**
	 * chrFile 序列文件或序列文件夹 regx 如果是序列文件，则用该正则表达式提取每个序列的名字，如果是序列文件夹，
	 * 则用该正则表达式提取含有该文件名的文件 单文件默认为"";文件夹默认为"*"；
	 * 
	 * @param chrFile
	 * @param regx null和""都走默认 如果是" "，表示截取">chr1 mouse test" 为"chr1"
	 */
	public void setChrFile(String chrFile, String regx) {
		close();
		if (FileOperate.isFileExistAndNotDir(chrFile)
				|| FileOperate.isFileDirectory(chrFile)) {
			seqHash = new SeqHash(chrFile, regx);
		}
	}
	
	//TODO 没有考虑并发
	public String getGtfFile() {
		if (gffHashGene == null) {
			return null;
		}
		String pathGFF = gffHashGene.getGffFilename();
		String outGTF = GffHashGene.convertNameToOtherFile(pathGFF, GffType.GTF);
		if (!FileOperate.isFileExistAndBigThanSize(outGTF, 10)) {
			writeToFile(outGTF, GffType.GTF);
		}
		return outGTF;
	}
	//TODO 没有考虑并发
	public String getBedFile() {
		if (gffHashGene == null) {
			return null;
		}
		String pathGFF = gffHashGene.getGffFilename();
		String outGTF = GffHashGene.convertNameToOtherFile(pathGFF, GffType.BED);
		if (!FileOperate.isFileExistAndBigThanSize(outGTF, 10)) {
			writeToFile(outGTF, GffType.BED);
		}
		return outGTF;
	}
	/** 务必保证GffGene存在
	 * 
	 * @param outFile
	 * @return 是否成功写入
	 */
	private void writeToFile(String outFile, GffType gffType) {
		List<String> lsSeqName = null;
		if (seqHash != null) {
			lsSeqName = seqHash.getLsSeqName();
		}
		gffHashGene.writeToFile(gffType, lsSeqName, outFile);
	}
	
	/**
	 * 将seqHash关闭掉
	 */
	public void close() {
		try {
			if (seqHash != null) seqHash.close();
		} catch (Exception e) {
		}
	}
}
