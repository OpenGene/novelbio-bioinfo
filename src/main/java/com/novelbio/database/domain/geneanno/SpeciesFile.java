package com.novelbio.database.domain.geneanno;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.fasta.format.NCBIchromFaChangeFormat;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.GffChrSeq;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene.GeneStructure;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffType;
import com.novelbio.analysis.seq.genome.gffOperate.ListDetailBin;
import com.novelbio.base.PathDetail;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.model.species.Species;
import com.novelbio.database.service.servgeneanno.ServSpeciesFile;

/**
 * 保存某个物种的各种文件信息，譬如mapping位置等等
 * 感觉可以用nosql进行存储，完全不能是一个结构化文件啊
 * @author zong0jie
 *
 */
@Document(collection="speciesfile")
@CompoundIndexes({
    @CompoundIndex(unique = true, name = "species_version_idx", def = "{'taxID': 1, 'version': -1}"),
 })
public class SpeciesFile {
	private static final Logger logger = Logger.getLogger(SpeciesFile.class);
	@Id
	String id;
	int taxID = 0;
	/** 文件版本 */
	String version = "";
	/** 该版本的年代，大概年代就行 */
	int publishYear = 0;
	/** 染色体所在文件夹、
	 * 格式 regex SepSign.SEP_ID chromPath
	 *  */
	String[] chromPath2Regx = new String[2];
	/** 染色体的单文件序列 */
	String chromSeq = "";
	/** 保存不同mapping软件所对应的索引
	 */
	Map<SoftWare, String> mapSoftware2IndexChrom = new HashMap<SoftWare, String>();

	/** gff的repeat文件，从ucsc下载 */
	String gffRepeatFile = "";
	/** refseq文件 */
	String refseqFile = "";
	/** 保存不同mapping软件所对应的索引
	 */
	Map<SoftWare, String> mapSoftware2IndexRef = new HashMap<SoftWare, String>();
	/** refseq中的NCRNA文件 */
	String refseqNCfile = "";
	/** key: chrID，为小写    value: chrLen */
	private Map<String, Long> mapChrID2ChrLen = new LinkedHashMap<String, Long>();

	/**
	 * key: DBname, 为小写<br>
	 * value: 0, GffType 1:GffFile
	 */
	Map<String, String[]> mapDB2GffTypeAndFile = new HashMap<String, String[]>();
	/** 用来将小写的DB转化为正常的DB，使得getDB获得的字符应该是正常的DB */
	@Transient
	Map<String, String> mapGffDB2DB;
	
	public void setTaxID(int taxID) {
		this.taxID = taxID;
	}
	public int getTaxID() {
		return taxID;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getVersion() {
		return version;
	}
	/**
	 * @return
	 * key: chrID 小写
	 * value： length
	 */
	public Map<String, Long> getMapChromInfo() {
		return mapChrID2ChrLen;
	}
	public void setChromPath(String regx, String chromPath) {
		chromPath2Regx[0] = chromPath;
		chromPath2Regx[1] = regx;
	}
	/** 获得chromeFa的路径 */
	public String getChromFaPath() {
		return chromPath2Regx[0];
	}
	/** 获得chromeFa的正则 */
	public String getChromFaRegx() {
		return chromPath2Regx[1];
	}
	public void setChromSeq(String chromSeq) {
		this.chromSeq = chromSeq;
	}
	public String getChromSeqFile() {
		if (chromSeq == null || chromSeq.trim().equals("")) {
			chromSeq = FileOperate.addSep(getChromFaPath()) + "all/chrAll.fa";
		}
		
		if (!FileOperate.isFileExistAndBigThanSize(chromSeq, 10)) {
			String path = getChromFaPath();
			FileOperate.createFolders(FileOperate.getParentPathName(chromSeq));
			NCBIchromFaChangeFormat ncbIchromFaChangeFormat = new NCBIchromFaChangeFormat();
			ncbIchromFaChangeFormat.setChromFaPath(path, getChromFaRegx());
			ncbIchromFaChangeFormat.writeToSingleFile(chromSeq);
			//将自动生成的chromSeq导入数据库
			update();
		}
		return chromSeq;
	}

	/**
	 * 给GUI的下拉框用的，一般用不到
	 * @return GffFile
	 */
	public Map<String, String> getMapGffDB() {
		Map<String, String> mapStringDB = new LinkedHashMap<String, String>();
		if (mapGffDB2DB.size() == 0) {
			return mapStringDB;
		}
		for (String gffDB : mapGffDB2DB.values()) {
			mapStringDB.put(gffDB, gffDB);
		}
		return mapStringDB;
	}
	
	/**
	 * 获得某个Type的Gff文件，如果没有则返回null
	 * @param gffDB 指定gffDB 如果为null，表示不指定，则返回默认
	 * @return
	 */
	public String getGffFile(String gffDB) {
		if (gffDB == null) {
			return getGffFile();
		}
		return mapDB2GffTypeAndFile.get(gffDB.toLowerCase())[1];
	}
	/**
	 * 获得某个Type的GffType，如果没有则返回null
	 * @param GFFtype 指定gfftype 如果为null，表示不指定，则返回默认
	 * @return
	 */
	public GffType getGffType(String gffDB) {
		if (gffDB == null) {
			return getGffType();
		}
		String gffType = mapDB2GffTypeAndFile.get(gffDB.toLowerCase())[0];
		return GffType.getType(gffType);
	}
	
	/**
	 * 按照优先级返回gff文件，优先级由GFFtype来决定
	 * @return GffDB
	 */
	public String getGffDB() {
		String[] gffInfo = getGffDB2GffTypeFile();
		return gffInfo[0];
	}
	
	/**
	 * 按照优先级返回gff文件，优先级由GFFtype来决定
	 * @return GffFile
	 */
	public String getGffFile() {
		String[] gffInfo = getGffDB2GffTypeFile();
		return gffInfo[1].split(SepSign.SEP_INFO_SAMEDB)[1];
	}
	/**
	 * 按照优先级返回gff类型，优先级由GffDB来决定
	 * 公返回枚举
	 * @return  GffType
	 */
	public GffType getGffType() {
		String[] gffInfo = getGffDB2GffTypeFile();
		String gffType = gffInfo[1].split(SepSign.SEP_INFO_SAMEDB)[0];
		return GffType.getType(gffType);
	}
	/**
	 * 按照优先级返回gff文件，优先级由GFFDB来决定
	 * @return string[2]<br>
	 * 0: gffDB<br>
	 * 1: GffType<br>
	 * 2: GffFile
	 */
	private String[] getGffDB2GffTypeFile() {
		if (mapDB2GffTypeAndFile.size() == 0) {
			return new String[]{null, null};
		}
		Entry<String, String[]> entyGffDB2File = mapDB2GffTypeAndFile.entrySet().iterator().next();
		String gffDB = entyGffDB2File.getKey();
		gffDB = mapGffDB2DB.get(gffDB);
		String[] gffType2File = entyGffDB2File.getValue();
		return new String[]{gffDB, gffType2File[0], gffType2File[1]};
	}
	
	public void setGffRepeatFile(String gffRepeatFile) {
		this.gffRepeatFile = gffRepeatFile;
	}
	
	public String getGffRepeatFile() {
		return gffRepeatFile;
	}
	
	/** 返回该mapping软件所对应的index的文件
	 * 没有就新建一个
	 * 格式如下：
	 * softMapping.toString() + "_Chr_Index/"
	 */
	public String getIndexChromFa(SoftWare softMapping) {
		String indexChromFa = mapSoftware2IndexChrom.get(softMapping);
		if (!FileOperate.isFileExist(indexChromFa)) {
			indexChromFa = creatAndGetSeqIndex(false, softMapping, getChromSeqFile(), mapSoftware2IndexChrom);
			update();
		}
		return indexChromFa;
	}
	
	public void addIndexRefseq(SoftWare softWare, String indexRefseq) {
		mapSoftware2IndexRef.put(softWare, indexRefseq);
	}
	/** 返回该mapping软件所对应的index的文件
	 * 没有就新建一个
	 * 格式如下：softMapping.toString() + "_Ref_Index/"
	 */
	public String getIndexRefseq(SoftWare softMapping) {
		String indexRefseqThis =  mapSoftware2IndexRef.get(softMapping);
		if (!FileOperate.isFileExist(indexRefseqThis)) {
			indexRefseqThis = creatAndGetSeqIndex(true, softMapping, getRefRNAFile(), mapSoftware2IndexRef);
			update();
		}
		return indexRefseqThis;
	}
	
	/**
	 * 如果不存在该index，那么就新创建一个index并且保存入数据库 
	 * @param refseq 是否为refseq
	 * @param softMapping mapping的软件
	 * @param seqIndex 该index所对应的保存在数据库中的值，譬如indexChr
	 * @param seqFile 该index所对应的序列，用getChromSeq()获得
	 * @param mapSoftware2ChrIndexPath 该index所对应的hash表，如 mapSoftware2ChrIndexPath
	 * @return softMapping.toString() + "_Ref_Index/" 或 softMapping.toString() + "_Chr_Index/"
	 */
	private String creatAndGetSeqIndex(Boolean refseq, SoftWare softMapping, String seqFile, Map<SoftWare, String> mapSoftware2ChrIndexPath) {
		String indexChromFinal = null;
		String IndexPath = null;
		String seqName = null;
		String indexFinalPath = null;
		if (mapSoftware2ChrIndexPath.size() > 0) {
			//media/winE/Bioinformatics/GenomeData/mouse/ucsc_mm9/Index/bwa_Index/mm9.fasta
			String indexChromFaOther = mapSoftware2ChrIndexPath.entrySet().iterator().next().getValue();
			seqName = FileOperate.getFileName(indexChromFaOther);
			IndexPath = FileOperate.getParentPathName(FileOperate.getParentPathName(indexChromFaOther));
		}
		else {
			///media/winE/Bioinformatics/GenomeData/mouse/ucsc_mm9/ChromFa/all/mm9.fasta
			seqName = FileOperate.getFileName(seqFile);
			IndexPath = FileOperate.getParentPathName(getChromFaPath()) + "index/";
		}
		
		if (refseq)
			indexFinalPath = IndexPath + softMapping.toString() + "_Ref_Index/";
		else
			indexFinalPath = IndexPath + softMapping.toString() + "_Chr_Index/";
		
		indexChromFinal = indexFinalPath + seqName;
		if (!FileOperate.linkFile(seqFile, indexChromFinal, true)) {
			logger.error("创建链接出错：" + seqFile + " " + indexChromFinal);
			return null;
		}
		mapSoftware2ChrIndexPath.put(softMapping, indexChromFinal);
		
		return indexChromFinal;
	}

	public String getMiRNAmatureFile() {
		return getMiRNAseq()[0];
	}
	public String getMiRNAhairpinFile() {
		return getMiRNAseq()[1];
	}
	/**
	 * @return
	 * 0: miRNAfile<br>
	 * 1: miRNAhairpinFile
	 */
	private String[] getMiRNAseq() {
		String chromFaPath = getChromFaPath();
		String miRNAfile = FileOperate.getParentPathName(chromFaPath) + "miRNA/miRNA.fa";
		String miRNAhairpinFile = FileOperate.getParentPathName(chromFaPath) + "miRNA/miRNAhairpin.fa";
		if (!FileOperate.isFileExistAndBigThanSize(miRNAfile,10) || !FileOperate.isFileExistAndBigThanSize(miRNAhairpinFile,10)) {
			FileOperate.createFolders(FileOperate.getParentPathName(miRNAfile));
			ExtractSmallRNASeq extractSmallRNASeq = new ExtractSmallRNASeq();
			extractSmallRNASeq.setOutMatureRNA(miRNAfile);
			extractSmallRNASeq.setOutHairpinRNA(miRNAhairpinFile);
			Species species = new Species(taxID);
			extractSmallRNASeq.setRNAdata(PathDetail.getMiRNADat(), species.getAbbrName());
			extractSmallRNASeq.getSeq();
		}
		return new String[]{miRNAfile, miRNAhairpinFile};
	}
	public void setPublishYear(int publishYear) {
		this.publishYear = publishYear;
	}
	/** 获得年代 */
	public int getPublishYear() {
		return publishYear;
	}
	public void setRefseqFile(String refseqFile) {
		this.refseqFile = refseqFile;
	}
	public String getRefRNAFile() {
		if (refseqFile == null || refseqFile.trim().equals("")) {
			refseqFile = FileOperate.getParentPathName( getChromFaPath()) + "refrna/rna.fa";
		}
		if (FileOperate.isFileExistAndBigThanSize(refseqFile, 0.2)) {
			return refseqFile;
		}
		try {
			GffChrAbs gffChrAbs = new GffChrAbs();
			gffChrAbs.setGffHash(new GffHashGene(getGffType(), getGffFile()));
			gffChrAbs.setSeqHash(new SeqHash(getChromFaPath(), getChromFaRegx()));
			GffChrSeq gffChrSeq = new GffChrSeq(gffChrAbs);
			gffChrSeq.setGeneStructure(GeneStructure.ALLLENGTH);
			gffChrSeq.setGetAAseq(false);
			gffChrSeq.setGetAllIso(true);
			gffChrSeq.setGetIntron(false);
			gffChrSeq.setGetSeqIsoGenomWide();
			gffChrSeq.setOutPutFile(refseqFile);
			gffChrSeq.run();
			update();
		} catch (Exception e) {
			logger.error("生成 RefRNA序列出错");
		}
	
		return refseqFile;
	}
	public void setRefseqNCfile(String refseqNCfile) {
		this.refseqNCfile = refseqNCfile;
	}
	public String getRefseqNCfile() {
		return refseqNCfile;
	}
	/** 获取仅含有最长转录本的refseq文件，是核酸序列，没有就返回null */
	public String getRefseqLongestIsoNrFile() {
		String chromFaPath = getChromFaPath();
		String refseqLongestIsoFile = FileOperate.getParentPathName(chromFaPath) + "refrna/refseqLongestIsoNr.fa";
		if (!FileOperate.isFileExistAndBigThanSize(refseqLongestIsoFile,10)) {
			FileOperate.createFolders(FileOperate.getParentPathName(refseqLongestIsoFile));
			try {
				Species species = new Species(taxID);
				species.setVersion(version);
				GffChrSeq gffChrSeq = new GffChrSeq();
				gffChrSeq.setSpecies(species);
				gffChrSeq.setGeneStructure(GeneStructure.ALLLENGTH);
				gffChrSeq.setGetIntron(false);
				gffChrSeq.setGetAAseq(false);
				gffChrSeq.setGetAllIso(false);
				gffChrSeq.setIsGetOnlyMRNA(true);
				gffChrSeq.setGetSeqIsoGenomWide();
				gffChrSeq.setOutPutFile(refseqLongestIsoFile);
				gffChrSeq.run();
			} catch (Exception e) {
				return null;
			}
		}
		return refseqLongestIsoFile;
	}
	public String getRfamFile() {
		String chromFaPath = getChromFaPath();
		String rfamFile = FileOperate.getParentPathName(chromFaPath) + "rfam/rfamFile";
		if (!FileOperate.isFileExistAndBigThanSize(rfamFile,10)) {
			FileOperate.createFolders(FileOperate.getParentPathName(rfamFile));
			ExtractSmallRNASeq extractSmallRNASeq = new ExtractSmallRNASeq();
			extractSmallRNASeq.setRfamFile(PathDetail.getRfamSeq(), taxID);
			extractSmallRNASeq.setOutRfamFile(rfamFile);
			extractSmallRNASeq.getSeq();
		}
		return rfamFile;
	}
	
	public void update() {
		servSpeciesFile.update(this);
	}
	public Map<String, Long> getHashChrID2ChrLen() {
		return mapChrID2ChrLen;
	}
	/**
	 * 仔仔细细的全部比较一遍，方便用于数据库升级
	 * @param obj
	 * @return
	 */
	public boolean equalsDeep(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		
		if (getClass() != obj.getClass()) return false;
		SpeciesFile otherObj = (SpeciesFile)obj;
		if (mapChrID2ChrLen.equals(otherObj.mapChrID2ChrLen)
			&& mapDB2GffTypeAndFile.equals(otherObj.mapDB2GffTypeAndFile)
			&& mapGffDB2DB.equals(otherObj.mapGffDB2DB)
			&& mapSoftware2IndexChrom.equals(otherObj.mapSoftware2IndexChrom)
			&& mapSoftware2IndexRef.equals(otherObj.mapSoftware2IndexRef)
			&& ArrayOperate.compareString(chromPath2Regx[0], otherObj.chromPath2Regx[0])
			&& ArrayOperate.compareString(chromPath2Regx[1], otherObj.chromPath2Regx[1])
			&& ArrayOperate.compareString(chromSeq, otherObj.chromSeq)
			&& ArrayOperate.compareString(gffRepeatFile, otherObj.gffRepeatFile)
			&& this.publishYear == otherObj.publishYear
			&& ArrayOperate.compareString(this.refseqFile, otherObj.refseqFile)
			&&ArrayOperate.compareString( this.refseqNCfile, otherObj.refseqNCfile)
			&& this.taxID == otherObj.taxID
			&& ArrayOperate.compareString(this.version, otherObj.version)
			)
		{
			return true;
		}
		return false;
	}
	
	/** 提取小RNA的一系列序列 */
	static public class ExtractSmallRNASeq {
		public static void main(String[] args) {
			ExtractSmallRNASeq extractSmallRNASeq = new ExtractSmallRNASeq();
			extractSmallRNASeq.extractRfam("/media/winE/Bioinformatics/genome/sRNA/Rfam2_1.fasta", 
					"/media/winE/Bioinformatics/genome/sRNA/Rfam_test.fasta", 0);
			
		}
		
		String RNAdataFile = "";
		/** 类似 hsa */
		String RNAdataRegx = "";
		
		/** 提取ncRNA的正则表达式 */
		String regxNCrna  = "NR_\\d+|XR_\\d+";
		/** refseq的序列文件，要求是NCBI下载的文件 */
		String refseqFile = "";


		/** Rfam的正则 */
		int taxIDfram = 0;
		/** Rfam的名字regx */
		String regxRfamWrite = "(?<=\\>)\\S+";
		/** rfam的文件 */
		String rfamFile = "";
		
		/** 提取的rfam的文件 */
		String outRfamFile;
		/** 提取到的目标文件夹和前缀 */
		String outPathPrefix = "";
		String outHairpinRNA;
		String outMatureRNA;
		/** 从RefSeq中提取的ncRNA序列 */
		String outNcRNA;
		
		/**
		 * 设定输出文件夹和前缀，这个设定了就不用设定别的了
		 * @param outPathPrefix
		 */
		public void setOutPathPrefix(String outPathPrefix) {
			this.outPathPrefix = outPathPrefix;
		}
		
		public void setOutNcRNA(String outNcRNA) {
			this.outNcRNA = outNcRNA;
		}
		public void setOutHairpinRNA(String outHairpinRNA) {
			this.outHairpinRNA = outHairpinRNA;
		}
		public void setOutMatureRNA(String outMatureRNA) {
			this.outMatureRNA = outMatureRNA;
		}
		public void setOutRfamFile(String outRfamFile) {
			this.outRfamFile = outRfamFile;
		}
		/**
		 * @param rnaDataFile
		 * @param rnaDataRegx 自动转换为小写
		 */
		public void setRNAdata(String rnaDataFile, String rnaDataRegx) {
			this.RNAdataFile = rnaDataFile;
			this.RNAdataRegx = rnaDataRegx.toLowerCase();
		}
		/**
		 * 待提取的NCBI上下载的refseq文件
		 * @param refseqFile
		 */
		public void setRefseqFile(String refseqFile) {
			this.refseqFile = refseqFile;
		}
		/**
		 * 待提取某物中的rfam文件
		 * @param rfamFile
		 * @param regx rfam的物种名
		 */
		public void setRfamFile(String rfamFile, int taxIDrfam) {
			this.rfamFile = rfamFile;
			this.taxIDfram = taxIDrfam;//TODO 看这里是物种的什么名字
		}
		/**
		 * 提取序列
		 */
		public void getSeq() {
			if (FileOperate.isFileExist(refseqFile)) {
				if (outNcRNA == null)
					outNcRNA = outPathPrefix + "_ncRNA.fa";
				extractNCRNA(refseqFile, outNcRNA, regxNCrna);
			}
			
			if (FileOperate.isFileExist(RNAdataFile)) {
				if (outHairpinRNA == null)
					outHairpinRNA = outPathPrefix + "_hairpin.fa";
				if (outMatureRNA == null)
					outMatureRNA = outPathPrefix + "_mature.fa";
				extractMiRNASeqFromRNAdata(RNAdataFile, RNAdataRegx, outHairpinRNA, outMatureRNA);
			}
			
			if (FileOperate.isFileExist(rfamFile)) {
				if (outRfamFile == null)
					outRfamFile = outPathPrefix + "_rfam.fa"; 
				extractRfam(rfamFile, outRfamFile, taxIDfram);
			}
		}
		/**
		 * 从NCBI的refseq.fa文件中提取NCRNA
		 * @param refseqFile
		 * @param outNCRNA
		 * @param regx 类似 "NR_\\d+|XR_\\d+";
		 */
		private void extractNCRNA(String refseqFile, String outNCRNA, String regx) {
			 SeqFastaHash seqFastaHash = new SeqFastaHash(refseqFile,regx,false);
			 seqFastaHash.writeToFile( regx ,outNCRNA );
		}
		/**
		 * 从miRBase的RNAdata文件中提取miRNA序列
		 * @param hairpinFile
		 * @param outNCRNA
		 * @param regx 物种的英文，人类就是hsa
		 */
		private void extractMiRNASeqFromRNAdata(String rnaDataFile, String rnaDataRegx, String rnaHairpinOut, String rnaMatureOut) {
			TxtReadandWrite txtRead = new TxtReadandWrite(rnaDataFile, false);
			TxtReadandWrite txtHairpin = new TxtReadandWrite(rnaHairpinOut, true);
			TxtReadandWrite txtMature = new TxtReadandWrite(rnaMatureOut, true);

			StringBuilder block = new StringBuilder();
			for (String string : txtRead.readlines()) {
				if (string.startsWith("//")) {
					ArrayList<SeqFasta> lsseqFastas = getSeqFromRNAdata(block.toString(), rnaDataRegx);
					if (lsseqFastas.size() == 0) {
						block = new StringBuilder();
						continue;
					}
					txtHairpin.writefileln(lsseqFastas.get(0).toStringNRfasta());
					for (int i = 1; i < lsseqFastas.size(); i++) {
						txtMature.writefileln(lsseqFastas.get(i).toStringNRfasta());
					}
					block = new StringBuilder();
					continue;
				}
				block.append( string + TxtReadandWrite.ENTER_LINUX);
			}
			
			txtRead.close();
			txtHairpin.close();
			txtMature.close();
		}
		/**
		 * 给定RNAdata文件的一个block，将其中的序列提取出来
		 * @param rnaDataBlock
		 * @return regex 小写的kegg缩写，如hsa
		 * 后面为成熟体序列
		 */
		private ArrayList<SeqFasta> getSeqFromRNAdata(String rnaDataBlock, String regex) {
			ArrayList<SeqFasta> lSeqFastas = new ArrayList<SeqFasta>();
			
			String[] ss = rnaDataBlock.split(TxtReadandWrite.ENTER_LINUX);
			String[] ssID = ss[0].split(" +");
			if (!ss[0].startsWith("ID") || !ssID[4].toLowerCase().contains(regex)) {
				return lSeqFastas;
			}
			String miRNAhairpinName = ssID[1]; //ID   cel-lin-4         standard; RNA; CEL; 94 BP.
			ArrayList<ListDetailBin> lsSeqLocation = getLsMatureMirnaLocation(ss);
			String finalSeq = getHairpinSeq(ss);
			
			ArrayList<SeqFasta> lsResult = new ArrayList<SeqFasta>();
			SeqFasta seqFasta = new SeqFasta(miRNAhairpinName, finalSeq);
			seqFasta.setDNA(true);
			lsResult.add(seqFasta);
			for (ListDetailBin listDetailBin : lsSeqLocation) {
				SeqFasta seqFastaMature = new SeqFasta();
				seqFastaMature.setName(listDetailBin.getNameSingle());
				seqFastaMature.setSeq(finalSeq.substring(listDetailBin.getStartAbs()-1, listDetailBin.getEndAbs()));
				seqFastaMature.setDNA(true);
				lsResult.add(seqFastaMature);
			}
			return lsResult;
		}
		private ArrayList<ListDetailBin> getLsMatureMirnaLocation(String[] block) {
			ArrayList<ListDetailBin> lsResult = new ArrayList<ListDetailBin>();
			ListDetailBin lsMiRNAhairpin = null;
			for (String string : block) {
				String[] sepInfo = string.split(" +");
				if (sepInfo[0].equals("FT")) {
					if (sepInfo[1].equals("miRNA")) {
						lsMiRNAhairpin = new ListDetailBin();
						String[] loc = sepInfo[2].split("\\.\\.");
						lsMiRNAhairpin.setStartAbs(Integer.parseInt(loc[0]));
						lsMiRNAhairpin.setEndAbs(Integer.parseInt(loc[1]));
					}
					if (sepInfo[1].contains("product")) {
						String accID = sepInfo[1].split("=")[1];
						accID = accID.replace("\"", "");
						lsMiRNAhairpin.addItemName(accID);
						lsResult.add(lsMiRNAhairpin);
					}
				}
			}
			return lsResult;
		}
		private String getHairpinSeq(String[] block) {
			String finalSeq = "";
			boolean seqFlag = false;
			for (String string : block) {
				if (string.startsWith("SQ")) {
					seqFlag = true;
					continue;
				}
				if (seqFlag) {
					String[] ssA = string.trim().split(" +");
					finalSeq = finalSeq + string.replace(ssA[ssA.length - 1], "").replace(" ", "");
				}
			}
			return finalSeq;
		}
		/**
		 * 从rfam.txt文件中提取指定物种的ncRNA序列
		 * @param hairpinFile
		 * @param outNCRNA
		 * @param regx 物种的英文，人类就是Homo sapiens
		 */
		private void extractRfamOld(String rfamFile, String outRfam, int taxIDquery) {
			TxtReadandWrite txtOut = new TxtReadandWrite(outRfam, true);
			 SeqFastaHash seqFastaHash = new SeqFastaHash(rfamFile,null,false);
			 seqFastaHash.setDNAseq(true);
			 ArrayList<SeqFasta> lsSeqfasta = seqFastaHash.getSeqFastaAll();
			 for (SeqFasta seqFasta : lsSeqfasta) {
				 int taxID = 0;
				 try {
					 taxID = Integer.parseInt(seqFasta.getSeqName().trim().split(" +")[1].split(":")[0]);
				 } catch (Exception e) {
					 logger.error("本序列中找不到taxID：" + seqFasta.getSeqName());
				 }
				 
				 if (taxID == taxIDquery) {
					 SeqFasta seqFastaNew = seqFasta.clone();
					 String name = seqFasta.getSeqName().trim().split(" +")[0];
					 name = name.replace(";", "//");
					 seqFastaNew.setName(name);
					 txtOut.writefileln(seqFastaNew.toStringNRfasta());
				 }
			 }
			 txtOut.close();
		}
		
		/**
		 * 修正rfam.txt文件中提取指定物种的ncRNA序列
		 * @param hairpinFile
		 * @param outNCRNA
		 * @param regx 物种的英文，人类就是Homo sapiens
		 */
		private void extractRfam(String rfamFile, String outRfam, int taxIDquery) {
			TxtReadandWrite txtRead = new TxtReadandWrite(rfamFile, false);
			TxtReadandWrite txtWrite = new TxtReadandWrite(outRfam, true);

			HashMap<String, Integer> map = new HashMap<String, Integer>();
			for (String string : txtRead.readlines()) {
				if (string.startsWith(">")) {
					string = string.split(" +")[0].replace(";", "//");
					if (map.containsKey(string)) {
						int tmp = map.get(string) + 1;
						map.put(string, tmp);
					} else {
						map.put(string, 0);
					}
					int tmpNum = map.get(string);
					if (tmpNum == 0) {
						txtWrite.writefileln(string);
					} else {
						txtWrite.writefileln(string + "_new" + tmpNum);
					}
				} else {
					string = string.replace("U", "T");
					txtWrite.writefileln(string);
				}
			}
			txtRead.close();
			txtWrite.close();
		}
		
	}
	
}

