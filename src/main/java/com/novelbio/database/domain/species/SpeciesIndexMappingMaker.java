package com.novelbio.database.domain.species;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.base.StringOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.bioinfo.gff.GffHashGene;
import com.novelbio.bioinfo.gff.GffType;
import com.novelbio.bioinfo.sam.SamIndexRefsequence;
import com.novelbio.database.domain.species.IndexMappingMaker.ExceptionNbcMappingSoftNotSupport;
import com.novelbio.database.domain.species.IndexMappingMaker.IndexMapSplice;
import com.novelbio.database.domain.species.IndexMappingMaker.IndexRsem;
import com.novelbio.database.domain.species.IndexMappingMaker.IndexTophat;
import com.novelbio.database.model.geneanno.EnumSpeciesFile;
import com.novelbio.database.model.geneanno.SpeciesFile;
import com.novelbio.database.model.information.SoftWareInfo.SoftWare;
import com.novelbio.generalconf.PathDetailNBC;

/** 为某个物种创建mapping相关的索引 */
public class SpeciesIndexMappingMaker {
	private static final Logger logger = LoggerFactory.getLogger(SpeciesIndexMappingMaker.class);
	
	/** 仅用于测试 */
	protected static final String indexPath = "index/";
	/**
	 * 仅用于测试<br>
	 * 可以建索引的序列
	 * key: 基因序列
	 * value:序列索引所在的文件夹名
	 */
	protected static final Map<EnumSpeciesFile, String> mapFile2IndexPath = new HashMap<>();
	static {
		mapFile2IndexPath.put(EnumSpeciesFile.chromSeqFile, "Chr_Index");
		mapFile2IndexPath.put(EnumSpeciesFile.refseqOneIsoRNA, "Ref_OneIso_Index");
		mapFile2IndexPath.put(EnumSpeciesFile.refseqAllIsoRNA, "Ref_AllIso_Index");
	}
	
	private SpeciesFile speciesFile;
	
	/** 保存index的文件夹路径，默认从配置文件走 */
	private String genomePath = PathDetailNBC.getGenomePath();
	/** 是否需要分布式锁 */
	private boolean isLock = true;
	
	public SpeciesIndexMappingMaker(SpeciesFile speciesFile) {
		this.speciesFile = speciesFile;
	}
	/** 是否需要分布式锁 */
	public void setLock(boolean isLock) {
	    this.isLock = isLock;
    }
	
	/** 仅用于测试，保存index的文件夹路径，默认从配置文件走 */
	protected void setGenomePath(String genomePath) {
	    this.genomePath = FileOperate.addSep(genomePath);
    }
	
	/**
	 * 返回索引所在的文件夹，绝对路径，以"/"结尾<br>
	 * 保存在  index/bwa/9606/GRCh38/Chr_Index/
	 * @param indexSeq
	 * @param softWare
	 * @return
	 */
	public String getSequenceIndex(EnumSpeciesFile indexSeq, String softWare) {
		if (StringOperate.isEqual(softWare, SoftWare.bwa_aln.toString())) softWare = SoftWare.bwa_mem.toString();
	      
		if (!mapFile2IndexPath.containsKey(indexSeq)) {
			throw new ExceptionNbcMappingSoftNotSupport(indexSeq + " is not a validate sequence file");
        }
		
//		Set<String> setIndex = IndexMappingMaker.getLsIndexDNA();
//		setIndex.addAll(IndexMappingMaker.getLsIndexRNA());
//		if (!setIndex.contains(softWare)) {
//			throw new ExceptionNbcMappingSoftNotSupport("cannot find mapping software " + softWare.toString());
//        }
		
		String path = getParentPathIndex(indexSeq, softWare.toString());
		if (indexSeq == EnumSpeciesFile.chromSeqFile) {
			path = path + FileOperate.getFileName(speciesFile.getChromSeqFile());
		} else if (indexSeq == EnumSpeciesFile.refseqAllIsoRNA) {
			path = path + speciesFile.getRefSeqFileName(true, false);
		} else if (indexSeq == EnumSpeciesFile.refseqOneIsoRNA) {
			path = path + speciesFile.getRefSeqFileName(false, false);
		} else {
			throw new ExceptionNbcSpeciesFile("do not support file type " + indexSeq.toString());
		}
		return path;
	}
	
	public void makeIndex() {
		Set<String> setIndexDNA = IndexMappingMaker.getLsIndexDNA();
		Set<String> setIndexRNA = IndexMappingMaker.getLsIndexRNA();

		for (String softWare : setIndexDNA) {
			makeIndexChr(softWare);
			makeIndexRef(softWare, true);
			makeIndexRef(softWare, false);
        }
		
		for (String softWare : setIndexRNA) {
			if (StringOperate.isEqual(softWare, SoftWare.rsem.toString())) {
				makeIndexRefRsem(softWare, true);
				makeIndexRefRsem(softWare, false);
	            	continue;
            }
			makeIndexChr(softWare);
        }
	}
	
	public void makeIndexRef(String softWare, boolean isAllIso) {
		String refAllIso = speciesFile.getRefSeqFile(isAllIso, false);
		EnumSpeciesFile refFile = isAllIso? EnumSpeciesFile.refseqAllIsoRNA: EnumSpeciesFile.refseqOneIsoRNA;
		String refAllIndex = getSequenceIndex(refFile, softWare);
		
		copyFileAndIndex(refAllIso, refAllIndex);
		
		makeIndexNormal(softWare, refAllIndex);
		String softwareName = softWare.toString();
		if (softwareName.startsWith("bwa_")) {
			softwareName = "bwa";
        }
		logger.info("sucessfully finish {} index on {}, with version " + speciesFile.getVersion() + " " + FileOperate.getFileName(refAllIndex), softwareName, speciesFile.getTaxID());
	}
	
	private void makeIndexRefRsem(String softWare, boolean isAllIso) {
		String refIso = speciesFile.getRefSeqFile(isAllIso, false);
		EnumSpeciesFile enumFileType = isAllIso? EnumSpeciesFile.refseqAllIsoRNA: EnumSpeciesFile.refseqOneIsoRNA;
		String refIsoIndex = getSequenceIndex(enumFileType, softWare);
		String gene2Iso = SpeciesFileExtract.getRefrna_Gene2Iso(refIso);
		String gene2IsoIndex = SpeciesFileExtract.getRefrna_Gene2Iso(refIsoIndex);
		
		copyFileAndIndex(refIso, refIsoIndex);
		FileOperate.copyFile(gene2Iso, gene2IsoIndex, true);
		makeRsemIndex(refIsoIndex, gene2IsoIndex);
		String softwareName = softWare.toString();
		logger.info("sucessfully finish {} index on {}, with version " + speciesFile.getVersion() + " " + FileOperate.getFileName(refIsoIndex), softwareName, speciesFile.getTaxID());
	}
	
	/** 对染色体文件建立索引 */
	public void makeIndexChr(String softWare) {
		String chrFile = speciesFile.getChromSeqFile();
		SamIndexRefsequence.generateIndexAndGetMapChrId2Len(chrFile);

		String chrFolder = null;
		String chrFileIndex = getSequenceIndex(EnumSpeciesFile.chromSeqFile, softWare);
		if (StringOperate.isEqual(softWare, SoftWare.mapsplice.toString())) {
			SpeciesFileSepChr sepChr = new SpeciesFileSepChr();
			sepChr.setSpeciesFile(speciesFile);
			sepChr.setLock(isLock);
			sepChr.setGenomePath(genomePath);
			sepChr.generateChrSepFiles();
			chrFile = sepChr.getChrSepFileOne();
			chrFolder = sepChr.getChrSepFolder();
			chrFileIndex = IndexMapSplice.modifyChrSeqName(chrFileIndex);
		}
		copyFileAndIndex(chrFile, chrFileIndex);

		if (StringOperate.isEqual(softWare, SoftWare.tophat.toString())) {
			makeTophatIndexBowtie(SoftWare.bowtie, null, chrFileIndex);
			makeTophatIndexBowtie(SoftWare.bowtie2, null, chrFileIndex);

			for (String gffdb : speciesFile.getMapGffDB().keySet()) {
				makeTophatIndexBowtie(SoftWare.bowtie, gffdb, chrFileIndex);
				logger.info("sucessfully finish tophat index using bowtie on {}, with version {} and gffdb " + gffdb, speciesFile.getTaxID(), speciesFile.getVersion());
				makeTophatIndexBowtie(SoftWare.bowtie2, gffdb, chrFileIndex);
				logger.info("sucessfully finish tophat index using bowtie2 on {}, with version {} and gffdb " + gffdb, speciesFile.getTaxID(), speciesFile.getVersion());
			}
		} else if (StringOperate.isEqual(softWare, SoftWare.mapsplice.toString())) {
			makeMapspliceIndex(chrFileIndex, chrFolder);
			logger.info("sucessfully finish mapsplice index on {}, with version {}", speciesFile.getTaxID(), speciesFile.getVersion());
		} else {
			makeIndexNormal(softWare, chrFileIndex);
			String softwareName = softWare.toString();
			if (softwareName.startsWith("bwa_")) {
				softwareName = "bwa";
            }
			logger.info("sucessfully finish {} index on {}, with version " + speciesFile.getVersion(), softwareName, speciesFile.getTaxID());
		}
	}
	
	/** 拷贝chr文件和fai文件到索引文件夹下 */
	public static void copyFileAndIndex(String seqFile, String seqFileIndex) {
		String parentPath = FileOperate.getPathName(seqFileIndex);
		FileOperate.createFolders(parentPath);
		String seqFileFai = SamIndexRefsequence.getIndexFile(seqFile);
		String seqFileIndexFai = SamIndexRefsequence.getIndexFile(seqFileIndex);
		logger.info("copyfile from {} to {}", seqFile, seqFileIndex);
		FileOperate.copyFile(seqFile, seqFileIndex, false);
		logger.info("copyfile from {} to {}", seqFileFai, seqFileIndexFai);
		FileOperate.copyFile(seqFileFai, seqFileIndexFai, false);
	}
	
	/** 普通建索引 */
	private void makeIndexNormal(String softWare, String chrFile) {
		IndexMappingMaker indexMappingMaker = IndexMappingMaker.createIndexMaker(softWare);
		indexMappingMaker.setChrIndex(chrFile);
		indexMappingMaker.setLock(isLock);
		indexMappingMaker.IndexMake();
	}
	
	private void makeTophatIndexBowtie(SoftWare bowtieVersion, String gffdb, String chrFile) {
		String gffFile = speciesFile.getGffFile(gffdb);
		String gtfFile = null;
		if (gffFile != null) {
			gtfFile = GffHashGene.convertNameToOtherFile(gffFile, GffType.GTF);
        }
		
		IndexTophat indexMappingMaker = (IndexTophat)IndexMappingMaker.createIndexMaker(SoftWare.tophat);
		indexMappingMaker.setChrIndex(chrFile);
		indexMappingMaker.setBowtieVersion(bowtieVersion);
		indexMappingMaker.setGtfFile(gtfFile, true);
		indexMappingMaker.setLock(isLock);
		indexMappingMaker.IndexMake();
    }
	
	private void makeMapspliceIndex(String chrFile, String chrFolder) {
		Set<String> setChrId = null;
		if (speciesFile.getGffType() != null) {
			GffHashGene gffHashGene = new GffHashGene(speciesFile.getGffType(), speciesFile.getGffFile());
			setChrId = gffHashGene.getMapChrID2LsGff().keySet();
        }
		
		IndexMapSplice indexMappingMaker = (IndexMapSplice)IndexMappingMaker.createIndexMaker(SoftWare.mapsplice);
		
		indexMappingMaker.setChrIndex(chrFile);
		indexMappingMaker.setChrSepFolder(chrFolder);
		indexMappingMaker.setSetChrInclude(setChrId);
		indexMappingMaker.setLock(isLock);
		indexMappingMaker.IndexMake();
    }
	
	private void makeRsemIndex(String chrFile, String gene2IsoFile) {		
		IndexRsem indexMappingMaker = (IndexRsem)IndexMappingMaker.createIndexMaker(SoftWare.rsem);
		indexMappingMaker.setChrIndex(chrFile);
		indexMappingMaker.setGene2IsoFile(gene2IsoFile);
		indexMappingMaker.setLock(isLock);
		indexMappingMaker.IndexMake();
    }
	
	/**
	 * 
	 * 返回索引所在的文件夹，绝对路径，以"/"结尾<br>
	 * 保存在  index/bwa/9606/GRCh38/Chr_Index/
	 * @param refseq
	 * @param isAllIso
	 * @param softName
	 * @return
	 */
	private String getParentPathIndex(EnumSpeciesFile indexType, String softName) {
		if (softName.startsWith("bwa_")) {
			softName = "bwa";
		}
		String parentPath = genomePath + indexPath + softName + FileOperate.getSepPath() + getPathToVersion();
		return parentPath + FileOperate.addSep(mapFile2IndexPath.get(indexType));
	}
	
	/** 物种版本的相对路径，到版本为止
	 * 如 9606/GRCh38/<br>
	 * 包含最后的"/"
	 */
	private String getPathToVersion() {
		if(speciesFile.getTaxID() == 0 || StringOperate.isRealNull(speciesFile.getVersion()))
			throw new ExceptionNbcSpeciesNotExist("species taxId cannot be 0, and version must exist");
		return speciesFile.getTaxID() + FileOperate.getSepPath() + speciesFile.getVersion() + FileOperate.getSepPath();
	}
	

}
