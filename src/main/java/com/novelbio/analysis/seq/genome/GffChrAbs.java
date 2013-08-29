package com.novelbio.analysis.seq.genome;

import java.io.Closeable;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGeneAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffType;
import com.novelbio.analysis.seq.genome.mappingOperate.MapInfo;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.species.Species;
/**
 * GffHashGene和SeqHash都是static，也就是一次只能对一个物种进行分析
 * MapReads不是static，也就是可以同时处理多个mapping文件
 * @author zong0jie
 *
 */
public class GffChrAbs implements Closeable {
	private static final Logger logger = Logger.getLogger(GffChrAbs.class);
	
	private int distanceMapInfo = 3000;
	GffHashGene gffHashGene = null;
	SeqHash seqHash = null;
	Species species;

	public GffChrAbs() {}

	public GffChrAbs(Species species) {
		setSpecies(species);
	}

	public GffChrAbs(int taxID) {
		setTaxID(taxID);
	}

	public void setTaxID(int taxID) {
		this.species = new Species(taxID);
		setGffFile(species.getTaxID(), species.getGffType(), species.getGffFile());
		setChrFile(species.getChromFaPath(), species.getChromFaRegex());
	}
	
	/**
	 * 如果本GffChrAbs已经close过了，可以重置species来打开，并且效率较高
	 * @param species
	 */
	public void setSpecies(Species species) {
		close();
		if (this.species != null && this.species.equals(species) && this.species.getGffDB().equals(species.getGffDB())) {
			if (FileOperate.isFileDirectory(species.getChromFaPath())) {
				setChrFile(species.getChromFaPath(), species.getChromFaRegex());
			}
			return;
		}
		if (species == null || species.getTaxID() == 0) {
			return;
		}

		this.species = species;
		setGffFile(species.getTaxID(), species.getGffType(), species.getGffFile());
		setChrFile(species.getChromFaPath(), species.getChromFaRegex());
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
	public void setGffFile(int taxID, GffType gffType, String gffFile) {
		if (FileOperate.isFileExist(gffFile)) {
			gffHashGene = new GffHashGene(gffType, gffFile);
			gffHashGene.setTaxID(taxID);
		}
	}
	
	/**
	 * chrFile 序列文件或序列文件夹 regx 如果是序列文件，则用该正则表达式提取每个序列的名字，如果是序列文件夹，
	 * 则用该正则表达式提取含有该文件名的文件 单文件默认为"";文件夹默认为"\\bchr\\w*"；
	 * 
	 * @param chrFile
	 * @param regx null和""都走默认
	 */
	public void setChrFile(String chrFile, String regx) {
		close();
		if (FileOperate.isFileExist(chrFile)
				|| FileOperate.isFileDirectory(chrFile)) {
			seqHash = new SeqHash(chrFile, regx);
		}
	}

	/**
	 * 获得指定文件内的坐标信息 如果两个位点终点的间距在distanceMapInfo以内，就会删除那个权重低的
	 * 
	 * @param txtExcel
	 * @param colChrID
	 * @param colStartLoc
	 * @param colEndLoc
	 * @param colScore
	 *            打分，也就是权重，没有该列的话，就设置为 <= 0
	 * @param rowStart
	 */
	public ArrayList<MapInfo> readFileRegionMapInfo(String txtExcel,
			int colChrID, int colStartLoc, int colEndLoc, int colScore,
			int rowStart) {
		int[] columnID = null;
		if (colScore <= 0) {
			columnID = new int[] { colChrID, colStartLoc, colEndLoc };
		} else {
			columnID = new int[] { colChrID, colStartLoc, colEndLoc, colScore };
		}
		ArrayList<String[]> lstmp = ExcelTxtRead.readLsExcelTxt(txtExcel,
				columnID, rowStart, 0);
		ArrayList<MapInfo> lsMapInfos = new ArrayList<MapInfo>();
		for (String[] strings : lstmp) {
			MapInfo mapInfo = new MapInfo(strings[0]);
			try {
				mapInfo.setStartEndLoc(Integer.parseInt(strings[1]),
						Integer.parseInt(strings[2]));
			} catch (Exception e) {
				logger.error("该坐标有问题：" + mapInfo.getRefID());
				continue;
			}
			if (colScore > 0) {
				mapInfo.setScore(Double.parseDouble(strings[3]));
			}
			lsMapInfos.add(mapInfo);
		}
		MapInfo.sortLsMapInfo(lsMapInfos, distanceMapInfo);
		return lsMapInfos;
	}

	/**
	 * 不用reads填充MapInfo 获得summit两端各region的区域，总共就是region*2+1的区域
	 * 如果两个位点终点的间距在distanceMapInfo以内，就会删除那个权重低的
	 * 
	 * @param txtExcel
	 * @param region
	 * @param colChrID
	 * @param colSummit
	 * @param rowStart
	 */
	public ArrayList<MapInfo> readFileSiteMapInfo(String txtExcel, int region,
			int colChrID, int colSummit, int colScore, int rowStart) {
		int[] columnID = null;
		if (colScore <= 0) {
			columnID = new int[] { colChrID, colSummit, colScore };
		} else {
			columnID = new int[] { colChrID, colSummit, colScore };
		}
		ArrayList<String[]> lstmp = ExcelTxtRead.readLsExcelTxt(txtExcel,
				columnID, rowStart, 0);
		ArrayList<MapInfo> lsMapInfos = new ArrayList<MapInfo>();
		for (String[] strings : lstmp) {
			MapInfo mapInfo = new MapInfo(strings[0]);
			try {
				mapInfo.setFlagLoc(Integer.parseInt(strings[1]));
			} catch (Exception e) {
				logger.error("该坐标有问题：" + mapInfo.getRefID());
				continue;
			}
			mapInfo.setStartEndLoc(mapInfo.getFlagSite() - region,
					mapInfo.getFlagSite() + region);
			if (colScore > 0) {
				mapInfo.setScore(Double.parseDouble(strings[2]));
			}
			lsMapInfos.add(mapInfo);
		}
		MapInfo.sortLsMapInfo(lsMapInfos, distanceMapInfo);
		return lsMapInfos;
	}
	
	/**
	 * 将seqHash关闭掉
	 */
	public void close() {
		try {
			seqHash.close();
		} catch (Exception e) {
		}
	}
}
