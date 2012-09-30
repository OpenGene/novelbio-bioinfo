package com.novelbio.analysis.seq.genome;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.mappingOperate.MapInfo;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.species.Species;
/**
 * GffHashGene��SeqHash����static��Ҳ����һ��ֻ�ܶ�һ�����ֽ��з���
 * MapReads����static��Ҳ���ǿ���ͬʱ������mapping�ļ�
 * @author zong0jie
 *
 */
public class GffChrAbs {

	private static final Logger logger = Logger
			.getLogger(CopyOfGffChrAbs.class);
	private int distanceMapInfo = 3000;
	GffHashGene gffHashGene = null;
	SeqHash seqHash = null;
	Species species;

	public GffChrAbs() {
	}

	public GffChrAbs(Species species) {
		setSpecies(species);
	}

	public GffChrAbs(int taxID) {
		setTaxID(taxID);
	}

	public void setTaxID(int taxID) {
		this.species = new Species(taxID);
		setGffFile(species.getTaxID(), species.getGffFileType(),
				species.getGffFile());
		setChrFile(species.getChromFaPath(), species.getChromFaRegex());
	}

	public void setSpecies(Species species) {
		if (this.species != null && this.species.equals(species)) {
			return;
		}
		if (species == null || species.getTaxID() == 0) {
			return;
		}

		this.species = species;
		setGffFile(species.getTaxID(), species.getGffFileType(),
				species.getGffFile());
		setChrFile(species.getChromFaPath(), species.getChromFaRegex());
	}

	public void setGffHash(GffHashGene gffHashGene) {
		this.gffHashGene = gffHashGene;
	}

	public void setSeqHash(SeqHash seqHash) {
		this.seqHash = seqHash;
	}

	/** ���û���趨species���ͷ���һ��ȫ�µ�species��������taxID == 0 */
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

	public void setGffFile(int taxID, String gffType, String gffFile) {
		if (FileOperate.isFileExist(gffFile)) {
			gffHashGene = new GffHashGene(gffType, gffFile);
			gffHashGene.setTaxID(taxID);
		}
	}

	/**
	 * chrFile �����ļ��������ļ��� regx ����������ļ������ø�������ʽ��ȡÿ�����е����֣�����������ļ��У�
	 * ���ø�������ʽ��ȡ���и��ļ������ļ� ���ļ�Ĭ��Ϊ"";�ļ���Ĭ��Ϊ"\\bchr\\w*"��
	 * 
	 * @param chrFile
	 * @param regx
	 */
	public void setChrFile(String chrFile, String regx) {
		if (FileOperate.isFileExist(chrFile)
				|| FileOperate.isFileDirectory(chrFile)) {
			seqHash = new SeqHash(chrFile, regx);
		}
	}

	/**
	 * ���ָ���ļ��ڵ�������Ϣ �������λ���յ�ļ����distanceMapInfo���ڣ��ͻ�ɾ���Ǹ�Ȩ�ص͵�
	 * 
	 * @param txtExcel
	 * @param colChrID
	 * @param colStartLoc
	 * @param colEndLoc
	 * @param colScore
	 *            ��֣�Ҳ����Ȩ�أ�û�и��еĻ���������Ϊ <= 0
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
				logger.error("�����������⣺" + mapInfo.getRefID());
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
	 * ����reads���MapInfo ���summit���˸�region�������ܹ�����region*2+1������
	 * �������λ���յ�ļ����distanceMapInfo���ڣ��ͻ�ɾ���Ǹ�Ȩ�ص͵�
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
				logger.error("�����������⣺" + mapInfo.getRefID());
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

}
