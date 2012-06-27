package com.novelbio.database.domain.geneanno;

import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqHash;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.service.servgeneanno.ServSpeciesFile;

/**
 * 保存某个物种的各种文件信息，譬如mapping位置等等
 * 感觉可以用nosql进行存储，完全不能是一个结构化文件啊
 * @author zong0jie
 *
 */
public class SpeciesFile {
	int taxID = 0;
	/** 文件版本 */
	String version = "";
	/** 该版本的年代，大概年代就行 */
	int publishYear = 0;
	/** 染色体所在文件夹
	 * 格式 regex SepSign.SEP_ID chromPath
	 *  */
	String chromPath = "";
	/** 染色体的单文件序列 */
	String chromSeq = "";
	/** 保存不同mapping软件所对应的索引，格式<br>
	 *  mappingSoftware SepSign.SEP_INFO  indexPath SepSign.SEP_ID mappingSoftware SepSign.SEP_INFO  indexPath
	 *  */
	String indexChr;
	/** 各种gffgene文件放在一起，有ucsc，ncbi，tigr，tair等等
	 * GffType SepSign.SEP_INFO Gfffile SepSign.SEP_ID GffType SepSign.SEP_INFO Gfffile
	 *  */
	String gffGeneFile;
	/** gff的repeat文件，从ucsc下载 */
	String gffRepeatFile = "";
	/** rfam的文件 */
	String rfamFile = "";
	/** refseq文件 */
	String refseqFile = "";
	/** 保存不同mapping软件所对应的索引，格式<br>
	 *  mappingSoftware SepSign.SEP_INFO  indexPath SepSign.SEP_ID mappingSoftware SepSign.SEP_INFO  indexPath
	 *  */
	String indexRefseq;
	/** refseq中的NCRNA文件 */
	String refseqNCfile = "";
	/** miRNA成熟体文件 */
	String miRNAfile = "";
	/** miRNA前体文件 */
	String miRNAhairpinFile = "";
	/** 染色体长度信息，包括总长度和每条染色体长度，格式<br>
	 * chrID SepSign.SEP_INFO  chrLen SepSign.SEP_ID chrID SepSign.SEP_INFO  chrLen
	 */
	private String chromInfo;
	/** 从chrominfo转化而来 key: chrID，为小写    value: chrLen */
	private HashMap<String, Integer> hashChrID2ChrLen = new HashMap<String, Integer>();
	/** 从indexSeq转化而来, key: 软件名，为小写 value：路径 */
	HashMap<String, String> hashSoftware2ChrIndexPath = new HashMap<String, String>();
	/** 从indexRefseq转化而来, key: 软件名，为小写 value：路径 */
	HashMap<String, String> hashSoftware2RefseqIndexPath = new HashMap<String, String>();
	/** 从gffGeneFile来，key：gffType  value：gffFile */
	HashMap<String, String> hashGffType2GffFile = new HashMap<String, String>();
	
	/** 是否已经查找过 */
	boolean searched = false;
	/** 查找的service层 */
	ServSpeciesFile servSpeciesFile = new ServSpeciesFile();
	
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
	public String getChromInfo() {
		return chromInfo;
	}
	public void setChromPath(String chromPath) {
		this.chromPath = chromPath;
	}
	/**
	 * 获得chromeFa的路径以及正则
	 * @return
	 * 0: regex
	 * 1: path
	 */
	public String[] getChromFaPath() {
		String[] ss = chromPath.split(SepSign.SEP_ID);
		return ss;
	}
	
	public void setChromSeq(String chromSeq) {
		this.chromSeq = chromSeq;
	}
	public String getChromSeq() {
		return chromSeq;
	}
	public void setGffGeneFile(String gffGeneFile) {
		this.gffGeneFile = gffGeneFile;
	}
	/**
	 * 获得某个Type的Gff文件，如果没有则返回null
	 * @param version 指定gfftype
	 * @return
	 */
	public String getGffFile(GFFtype gfFtype) {
		filledHashGffType2GffFile();
		return hashGffType2GffFile.get(gfFtype.toString().toLowerCase());
	}
	/**
	 * 按照优先级返回gff文件，优先级由GFFtype来决定
	 * @return 0: GffType<br>
	 * 1: GffFile
	 */
	public String[] getGffFile() {
		filledHashGffType2GffFile();
		if (hashGffType2GffFile.size() == 0) {
			return null;
		}
		for (GFFtype gfFtype : GFFtype.values()) {
			if (hashGffType2GffFile.containsKey(gfFtype.toString().toLowerCase())) {
				return new String[]{gfFtype.toString(), hashGffType2GffFile.get(gfFtype.toString().toLowerCase())};
			}
		}
		return null;
	}
	
	public void setGffRepeatFile(String gffRepeatFile) {
		this.gffRepeatFile = gffRepeatFile;
	}
	public String getGffRepeatFile() {
		return gffRepeatFile;
	}
	public void setIndexSeq(String indexSeq) {
		this.indexChr = indexSeq;
	}
	public String getIndexChromFa(SoftWare softMapping) {
		filledHashIndexPath(indexChr, hashSoftware2ChrIndexPath);
		return hashSoftware2ChrIndexPath.get(softMapping.toString());
	}

	public void setIndexRefseq(String indexRefseq) {
		this.indexRefseq = indexRefseq;
	}
	public String getIndexRefseq() {
		return indexRefseq;
	}
	public void setMiRNAfile(String miRNAfile) {
		this.miRNAfile = miRNAfile;
	}
	public String getMiRNAmatureFile() {
		return miRNAfile;
	}
	public void setMiRNAhairpinFile(String miRNAhairpinFile) {
		this.miRNAhairpinFile = miRNAhairpinFile;
	}
	public String getMiRNAhairpinFile() {
		return miRNAhairpinFile;
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
	public String getRefseqFile() {
		return refseqFile;
	}
	public void setRefseqNCfile(String refseqNCfile) {
		this.refseqNCfile = refseqNCfile;
	}
	public String getRefseqNCfile() {
		return refseqNCfile;
	}
	public void setRfamFile(String rfamFile) {
		this.rfamFile = rfamFile;
	}
	public String getRfamFile() {
		return rfamFile;
	}
	/** 用chromInfo填充染色体长度hash表 */
	private void filledHashChrLen() {
		if (chromInfo == null) return;
		if (hashChrID2ChrLen.size() > 0) {
			return;
		}
		String[] chrLen = chromInfo.split(SepSign.SEP_ID);
		for (String string : chrLen) {
			String[] chrLenDetail = string.split(SepSign.SEP_INFO);
			hashChrID2ChrLen.put(chrLenDetail[0].toLowerCase(), Integer.parseInt(chrLenDetail[1]));
		}
	}
	/** 用indexSeq填充mapping所需路径hash表 
	 * @param indexSeq 输入的indexSeq文本
	 * @param hashSoftware2ChrIndexPath 待填充的hashSoft2Index key：软件名  value：路径
	 * */
	private void filledHashIndexPath(String indexSeq, HashMap<String, String> hashSoft2index) {
		if (indexSeq == null) return;
		if (hashSoft2index.size() > 0) {
			return;
		}
		String[] indexInfo = indexSeq.split(SepSign.SEP_ID);
		for (String string : indexInfo) {
			String[] indexDetail = string.split(SepSign.SEP_INFO);
			hashSoft2index.put(indexDetail[0].toLowerCase(), indexDetail[1]);
		}
	}
	/** 用gffGeneFile填充GffType2GffFile所需路径hash表 */
	private void filledHashGffType2GffFile() {
		if (gffGeneFile == null) return;
		if (hashGffType2GffFile.size() > 0) {
			return;
		}
		String[] gffType2File = gffGeneFile.split(SepSign.SEP_ID);
		for (String string : gffType2File) {
			String[] gffDetail = string.split(SepSign.SEP_INFO);
			hashGffType2GffFile.put(gffDetail[0].toLowerCase(), gffDetail[1]);
		}
	}
	
	public void update() {
		servSpeciesFile.update(this);
	}
	public HashMap<String, Integer> getHashChrID2ChrLen() {
		setChromLenInfo();
		filledHashChrLen();
		return hashChrID2ChrLen;
	}
	/**
	 * 设定长度
	 */
	private void setChromLenInfo() {
		if (chromInfo != null && !chromInfo.equals("")) {
			return;
		}
		String[] chrPath = getChromFaPath();
		SeqHash seqHash = new SeqHash(chrPath[1], chrPath[0]);
		ArrayList<String[]> lsChrLen = seqHash.getChrLengthInfo();
		chromInfo = lsChrLen.get(0)[0] + SepSign.SEP_INFO + lsChrLen.get(0)[1];
		for (int i = 0; i < lsChrLen.size(); i++) {
			String[] tmpLen = lsChrLen.get(i);
			chromInfo = chromInfo + SepSign.SEP_ID + tmpLen[0] + SepSign.SEP_INFO + tmpLen[1];
		}
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
		
		if (ArrayOperate.compareString(this.chromInfo, otherObj.chromInfo)
				&& ArrayOperate.compareString(this.chromPath, otherObj.chromPath)
				&& ArrayOperate.compareString(this.chromSeq, otherObj.chromSeq)
				&& ArrayOperate.compareString(this.gffGeneFile, otherObj.gffGeneFile)
				&& ArrayOperate.compareString(this.gffRepeatFile, otherObj.gffRepeatFile)
				&& ArrayOperate.compareString(this.indexChr, otherObj.indexChr)	
				&& ArrayOperate.compareString(this.miRNAfile, otherObj.miRNAfile)
				&& ArrayOperate.compareString(this.miRNAhairpinFile, otherObj.miRNAhairpinFile)
				&& this.publishYear == otherObj.publishYear
				&& ArrayOperate.compareString(this.refseqFile, otherObj.refseqFile)
				&&ArrayOperate.compareString( this.refseqNCfile, otherObj.refseqNCfile)
				&& ArrayOperate.compareString(this.rfamFile, otherObj.rfamFile)
				&& this.taxID == otherObj.taxID
				&& ArrayOperate.compareString(this.version, otherObj.version)
				&& ArrayOperate.compareString(this.indexRefseq, otherObj.indexRefseq)
				)
		{
			return true;
		}
		return false;
	
	}
	public static enum GFFtype {
		GFF_NCBI , GFF_UCSC,GFF_PLANT,GFF_TIGR,GFF_CUFFLINKS
	}
}
