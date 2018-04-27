package com.novelbio.database.domain.species;

import java.util.ArrayList;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.analysis.seq.fasta.FastaDictMake;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaReader;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.GffChrSeq;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene.GeneStructure;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffType;
import com.novelbio.analysis.seq.sam.SamIndexRefsequence;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.modgeneid.GeneID;
import com.novelbio.database.domain.modgeneid.GeneType;
import com.novelbio.database.model.geneanno.EnumSpeciesFile;
import com.novelbio.database.model.geneanno.SpeciesFile;
import com.novelbio.generalConf.PathDetailNBC;

/** 初始化物种的时候，需要生成一系列文件，包括建索引，提取miRNA等工作。而这些都很费时 */
public class SpeciesFileExtract {
	private static final Logger logger = LoggerFactory.getLogger(SpeciesFileExtract.class);
	
	SpeciesFile speciesFile;
	
	private String rfamSeqFile = PathDetailNBC.getRfamSeq();
	
	public SpeciesFileExtract(SpeciesFile speciesFile) {
		this.speciesFile = speciesFile;
	}
	
	/** 仅用于测试 */
	protected void setRfamSeqFile(String rfamSeqFile) {
	    this.rfamSeqFile = rfamSeqFile;
    }
	
	/** step1, 给chrFile建索引 */
	public void indexChrFile() {
		if (StringOperate.isRealNull(speciesFile.getChromSeqFile())) {
			return;
		}
		//如果文件里面的染色体，或者说contig太多，就会将短的去掉
		String chromeSeq = speciesFile.getChromSeqFile();
		if (FileOperate.isFileExistAndBigThan0(chromeSeq)) {
			SamIndexRefsequence samIndexRefsequence = new SamIndexRefsequence();
			samIndexRefsequence.setRefsequence(chromeSeq);
			samIndexRefsequence.indexSequence();
		}
		String fastaDict = FileOperate.changeFileSuffix(chromeSeq, "", "dict");
		if (FileOperate.isFileExistAndBigThanSize(chromeSeq, 0) && !FileOperate.isFileExistAndBigThanSize(fastaDict, 0)) {
			FastaDictMake fastaDictMake = new FastaDictMake(chromeSeq, fastaDict);
			fastaDictMake.makeDict();
		}
	}
	
	/** step4，给refseq添加索引 */
	public void indexRefseqFile() {
		indexRefseqFile(true);
		indexRefseqFile(false);
	}
	
	/** step1, 给chrFile建索引 */
	private void indexRefseqFile(boolean isAllIso) {
		if (StringOperate.isRealNull(speciesFile.getRefSeqFile(isAllIso, false))) {
			return;
		}
		//如果文件里面的染色体，或者说contig太多，就会将短的去掉
		String refSeq = speciesFile.getRefSeqFile(isAllIso, false);
		if (FileOperate.isFileExistAndBigThanSize(refSeq, 0)) {
			SamIndexRefsequence samIndexRefsequence = new SamIndexRefsequence();
			samIndexRefsequence.setRefsequence(refSeq);
			samIndexRefsequence.indexSequence();
		}
	}
	
	/** step2 把gff文件转换为gtf，第三方的软件会用到 */
	public void extractGtf() {
		Set<String> setChrId = speciesFile.getMapChromInfo().keySet();
		for (String gffdb : speciesFile.getMapGffDB().keySet()) {
			GffType gffType = speciesFile.getGffType(gffdb);
			String gffFile = speciesFile.getGffFile(gffdb);
			GffHashGene gffHashGene = new GffHashGene(gffType, gffFile);
			gffHashGene.convertToFile(GffType.GTF, new ArrayList<>(setChrId));
        }
	}
	
	/** step3，提取refseq */
	public void extractRefSeq() {
		extractRefSeq(true, true);
		extractRefSeq(true, false);
		extractRefSeq(false, true);
		extractRefSeq(false, false);
	}
	
	private void extractRefSeq(boolean isAllIso, boolean isProtein) {
		String refseqFile = speciesFile.getRefSeqFile(isAllIso, isProtein);
		if (FileOperate.isFileExistAndBigThan0(refseqFile)) {
			if (!isProtein && !FileOperate.isFileExistAndBigThan0(getRefrna_Gene2Iso(refseqFile))) {
				GffHashGene gffHashGene = null;
				if (FileOperate.isFileExistAndBigThan0(speciesFile.getGffFile())) {
					gffHashGene = new GffHashGene();
                }
				generateGene2IsoForRefrna(gffHashGene, refseqFile);
            }
			return;
		}
		//说明没有序列
		if (!FileOperate.isFileExistAndBigThanSize(speciesFile.getGffFile(), 0) || !FileOperate.isFileExistAndBigThanSize(speciesFile.getChromSeqFile(), 0)) {
			return;
		}
		String fileName = getRefSeqRegularName(speciesFile.getVersion(), isAllIso, isProtein);
		String filePath = speciesFile.getRefFilePath(isAllIso, isProtein);
		FileOperate.createFolders(filePath);
		refseqFile = filePath + fileName;
		try {
			String chrFile = speciesFile.getChromSeqFile();
			
			GffChrAbs gffChrAbs = new GffChrAbs();
			gffChrAbs.setGffHash(new GffHashGene(speciesFile.getGffType(), speciesFile.getGffFile(), speciesFile.getTaxID() == 7227));
			gffChrAbs.setSeqHash(new SeqHash(chrFile, " "));

			GffChrSeq gffChrSeq = new GffChrSeq(gffChrAbs);
			if (isProtein) {
				gffChrSeq.setGeneStructure(GeneStructure.CDS);
				gffChrSeq.setGetAAseq(true);
			} else {
				gffChrSeq.setGeneStructure(GeneStructure.ALLLENGTH);
				gffChrSeq.setGetAAseq(false);
			}
			gffChrSeq.setGetAllIso(isAllIso);
			gffChrSeq.setGetIntron(false);
			gffChrSeq.setGetSeqGenomWide();
			gffChrSeq.setOutPutFile(refseqFile);
			gffChrSeq.run();
			gffChrAbs.close();
			
			if (!isProtein) {
				generateGene2IsoForRefrna(gffChrAbs.getGffHashGene(), refseqFile);
            }
			speciesFile.setRefSeqFileName(fileName, isAllIso, isProtein);
			speciesFile.save();
			gffChrAbs = null;
			gffChrSeq = null;
		} catch (Exception e) {
			logger.error("生成 RefRNA序列出错");
		}
	}
	
	/** 生成文件名，没有路径 */
	protected static String getRefSeqRegularName(String version, boolean isAllIso, boolean isProtein) {
		String refseq;
		if (!isProtein) {
			refseq = isAllIso?  "rnaAllIso_" + version + ".fa" : "rnaOneIso_" + version + ".fa";
		} else {
			refseq = isAllIso?  "proteinAllIso_" + version + ".fa" : "proteinOneIso_" + version + ".fa";
		}
		return refseq;
	}
	
	private void generateGene2IsoForRefrna(GffHashGene gffHashGene, String refseqFile) {
		String gene2isoFile = getRefrna_Gene2Iso(refseqFile);
		TxtReadandWrite txtGene2Iso = new TxtReadandWrite(gene2isoFile, true);
		SeqFastaReader seqFastaReader = new SeqFastaReader(refseqFile);
		for (SeqFasta seqFasta : seqFastaReader.readlines()) {
			String isoName = seqFasta.getSeqName();
			String symbol = null;
			if (gffHashGene != null) {
				GffGeneIsoInfo gffGeneIsoInfo = gffHashGene.searchISO(isoName);
				if (gffGeneIsoInfo != null) {
					symbol = gffGeneIsoInfo.getParentGeneName();
				}
            }
			if (symbol == null) {
				GeneID geneID = new GeneID(isoName, speciesFile.getTaxID());
				symbol = geneID.getSymbol();
            }
			if (symbol == null || symbol.equals("")) {
				symbol = isoName;
			}
			txtGene2Iso.writefileln(symbol + "\t" + isoName);
        }
		txtGene2Iso.close();
	}
	
	/** 提取rfam相关的序列，主要用于miRNA */
	public void extractRfamFile() {
		extractRfamFile(true);
		extractRfamFile(false);
	}
	
	/** 提取rfam相关的序列，主要用于miRNA
	 * @param speciesSpecific 是否按照物种特异性提取
	 * @return
	 */
	private void extractRfamFile(boolean speciesSpecific) {
		String rfamFile = speciesFile.getRfamFile(speciesSpecific);
		if (!FileOperate.isFileExistAndBigThanSize(rfamFile,10)) {
			FileOperate.createFolders(FileOperate.getParentPathNameWithSep(rfamFile));
			ExtractSmallRNASeq extractSmallRNASeq = new ExtractSmallRNASeq();
			if (speciesSpecific) {
				extractSmallRNASeq.setRfamFile(rfamSeqFile, speciesFile.getTaxID());
			} else {
				extractSmallRNASeq.setRfamFile(rfamSeqFile, 0);
			}
			extractSmallRNASeq.setOutRfamFile(rfamFile);
			extractSmallRNASeq.getSeq();
		}
		return;
	}
	
	/** 数据库里是否记载了ncRNA，没记载就从Gff中提取 */
	public void generaetRefseqNCfile() {
		String ncFile = speciesFile.getRefseqNCfile();
		if (FileOperate.isFileExistAndBigThanSize(ncFile, 0)) {
			return;
		}
		String ncFileName = "ncRNA_" + speciesFile.getVersion() + ".fa";
		ncFile = EnumSpeciesFile.refseqNCfile.getSavePath(speciesFile) + ncFileName;
		GffHashGene gffHash = new GffHashGene(speciesFile.getGffType(), speciesFile.getGffFile());
		if (!gffHash.isContainNcRNA()) return;
		
		GffChrAbs gffChrAbs = new GffChrAbs();
		gffChrAbs.setGffHash(gffHash);
		gffChrAbs.setSeqHash(new SeqHash(speciesFile.getChromSeqFile(), " "));
		GffChrSeq gffChrSeq = new GffChrSeq(gffChrAbs);
		gffChrSeq.setGeneStructure(GeneStructure.EXON);
		gffChrSeq.setGeneType(GeneType.ncRNA);
		gffChrSeq.setGetAAseq(false);
		gffChrSeq.setGeneStructure(GeneStructure.ALLLENGTH);
		gffChrSeq.setGetAllIso(true);
		gffChrSeq.setGetIntron(false);
		gffChrSeq.setGetSeqGenomWide();
		gffChrSeq.setOutPutFile(ncFile);
		gffChrSeq.run();
		gffChrAbs.close();
		speciesFile.setRefseqNCfile(ncFileName);
		speciesFile.save();
		gffChrAbs = null;
		gffChrSeq = null;
	}
	
	/** 给定refseq的rna，返回该rna所对应的gene2iso文件 */
	public static String getRefrna_Gene2Iso(String refrna) {
		String gene2isoFile = FileOperate.changeFileSuffix(refrna, "_gene2Iso", "txt");
		return gene2isoFile;
	}
	
}

