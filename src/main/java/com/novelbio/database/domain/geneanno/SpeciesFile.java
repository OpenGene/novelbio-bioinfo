package com.novelbio.database.domain.geneanno;

import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ListDetailBin;
import com.novelbio.base.PathDetail;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.model.species.Species;
import com.novelbio.database.service.servgeneanno.ServSpeciesFile;

/**
 * ����ĳ�����ֵĸ����ļ���Ϣ��Ʃ��mappingλ�õȵ�
 * �о�������nosql���д洢����ȫ������һ���ṹ���ļ���
 * @author zong0jie
 *
 */
public class SpeciesFile {
	int taxID = 0;
	/** �ļ��汾 */
	String version = "";
	/** �ð汾����������������� */
	int publishYear = 0;
	/** Ⱦɫ�������ļ���
	 * ��ʽ regex SepSign.SEP_ID chromPath
	 *  */
	String chromPath = "";
	/** Ⱦɫ��ĵ��ļ����� */
	String chromSeq = "";
	/** ���治ͬmapping�������Ӧ����������ʽ<br>
	 *  mappingSoftware SepSign.SEP_INFO  indexPath SepSign.SEP_ID mappingSoftware SepSign.SEP_INFO  indexPath
	 *  */
	String indexChr;
	/** ����gffgene�ļ�����һ����ucsc��ncbi��tigr��tair�ȵ�
	 * GffType SepSign.SEP_INFO Gfffile SepSign.SEP_ID GffType SepSign.SEP_INFO Gfffile
	 *  */
	String gffGeneFile;
	/** gff��repeat�ļ�����ucsc���� */
	String gffRepeatFile = "";
	/** refseq�ļ� */
	String refseqFile = "";
	/** ���治ͬmapping�������Ӧ����������ʽ<br>
	 *  mappingSoftware SepSign.SEP_INFO  indexPath SepSign.SEP_ID mappingSoftware SepSign.SEP_INFO  indexPath
	 *  */
	String indexRefseq;
	/** refseq�е�NCRNA�ļ� */
	String refseqNCfile = "";
	
	/** Ⱦɫ�峤����Ϣ�������ܳ��Ⱥ�ÿ��Ⱦɫ�峤�ȣ���ʽ<br>
	 * chrID SepSign.SEP_INFO  chrLen SepSign.SEP_ID chrID SepSign.SEP_INFO  chrLen
	 */
	private String chromInfo;
	/** ��chrominfoת������ key: chrID��ΪСд    value: chrLen */
	private HashMap<String, Integer> hashChrID2ChrLen = new HashMap<String, Integer>();
	/** ��indexSeqת������, key: �������ΪСд value��·�� */
	HashMap<String, String> hashSoftware2ChrIndexPath = new HashMap<String, String>();
	/** ��indexRefseqת������, key: �������ΪСд value��·�� */
	HashMap<String, String> hashSoftware2RefseqIndexPath = new HashMap<String, String>();
	/** ��gffGeneFile����key��gffType  value��gffFile */
	HashMap<String, String> hashGffType2GffFile = new HashMap<String, String>();
	
	/** �Ƿ��Ѿ����ҹ� */
	boolean searched = false;
	/** ���ҵ�service�� */
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
	/**
	 * @return
	 * key: chrID Сд
	 * value�� length
	 */
	public HashMap<String, Long> getMapChromInfo() {
		HashMap<String, Long> mapChrID2Len = new HashMap<String, Long>(); 
		String[] chrID2Lens = chromInfo.split(SepSign.SEP_ID);
		for (String chrid2Len : chrID2Lens) {
			String[] ss = chrid2Len.split(SepSign.SEP_INFO);
			mapChrID2Len.put(ss[0].toLowerCase(), Long.parseLong(ss[1]));
		}
		return mapChrID2Len;
	}
	public void setChromPath(String chromPath) {
		this.chromPath = chromPath;
	}
	/** ���chromeFa��·�� */
	public String getChromFaPath() {
		String[] ss = chromPath.split(SepSign.SEP_ID);
		return ss[1];
	}
	/** ���chromeFa������ */
	public String getChromFaRegx() {
		String[] ss = chromPath.split(SepSign.SEP_ID);
		return ss[0];
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
	 * ���ĳ��Type��Gff�ļ������û���򷵻�null
	 * @param version ָ��gfftype
	 * @return
	 */
	public String getGffFile(GFFtype gfFtype) {
		filledHashGffType2GffFile();
		return hashGffType2GffFile.get(gfFtype.toString().toLowerCase());
	}
	/**
	 * �������ȼ�����gff�ļ������ȼ���GFFtype������
	 * @return GffFile
	 */
	private String[] getGffFileAndType() {
		filledHashGffType2GffFile();
		if (hashGffType2GffFile.size() == 0) {
			return new String[]{null, null};
		}
		for (GFFtype gfFtype : GFFtype.values()) {
			if (hashGffType2GffFile.containsKey(gfFtype.toString().toLowerCase())) {
				return new String[]{gfFtype.toString(), hashGffType2GffFile.get(gfFtype.toString().toLowerCase())};
			}
		}
		return new String[]{null, null};
	}
	/**
	 * �������ȼ�����gff�ļ������ȼ���GFFtype������
	 * @return 0: GffType<br>
	 * 1: GffFile
	 */
	public String getGffFile() {
		String[] gffInfo = getGffFileAndType();
		return gffInfo[1];
	}
	/**
	 * �������ȼ�����gff�ļ������ȼ���GFFtype������
	 * @return  GffType<br>
	 */
	public String getGffFileType() {
		String[] gffInfo = getGffFileAndType();
		return gffInfo[0];
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
	/** ������ */
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
	/** ��chromInfo���Ⱦɫ�峤��hash�� */
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
	/** ��indexSeq���mapping����·��hash�� 
	 * @param indexSeq �����indexSeq�ı�
	 * @param hashSoftware2ChrIndexPath ������hashSoft2Index key�������  value��·��
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
	/** ��gffGeneFile���GffType2GffFile����·��hash�� */
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
	 * �趨����
	 */
	private void setChromLenInfo() {
		if (chromInfo != null && !chromInfo.equals("")) {
			return;
		}
		SeqHash seqHash = new SeqHash(getChromFaPath(), getChromFaRegx());
		ArrayList<String[]> lsChrLen = seqHash.getChrLengthInfo();
		chromInfo = lsChrLen.get(0)[0] + SepSign.SEP_INFO + lsChrLen.get(0)[1];
		for (int i = 0; i < lsChrLen.size(); i++) {
			String[] tmpLen = lsChrLen.get(i);
			chromInfo = chromInfo + SepSign.SEP_ID + tmpLen[0] + SepSign.SEP_INFO + tmpLen[1];
		}
	}
	/**
	 * ����ϸϸ��ȫ���Ƚ�һ�飬�����������ݿ�����
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
				&& this.publishYear == otherObj.publishYear
				&& ArrayOperate.compareString(this.refseqFile, otherObj.refseqFile)
				&&ArrayOperate.compareString( this.refseqNCfile, otherObj.refseqNCfile)
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
	
	/** ��ȡСRNA��һϵ������ */
	static public class ExtractSmallRNASeq {
		public static void main(String[] args) {
			ExtractSmallRNASeq extractSmallRNASeq = new ExtractSmallRNASeq();
			extractSmallRNASeq.setRNAdata("/media/winE/Bioinformatics/DataBase/sRNA/miRNA.dat", "HSA");
			extractSmallRNASeq.setOutPathPrefix("/media/winE/Bioinformatics/DataBase/sRNA/test");
			extractSmallRNASeq.getSeq();
			
		}
		
		String RNAdataFile = "";
		/** ���� hsa */
		String RNAdataRegx = "";
		
		/** ��ȡncRNA��������ʽ */
		String regxNCrna  = "NR_\\d+|XR_\\d+";
		/** refseq�������ļ���Ҫ����NCBI���ص��ļ� */
		String refseqFile = "";


		/** Rfam������ */
		int taxIDfram = 0;
		/** Rfam������regx */
		String regxRfamWrite = "(?<=\\>)\\S+";
		/** rfam���ļ� */
		String rfamFile = "";
		
		/** ��ȡ��rfam���ļ� */
		String outRfamFile;
		/** ��ȡ����Ŀ���ļ��к�ǰ׺ */
		String outPathPrefix = "";
		String outHairpinRNA;
		String outMatureRNA;
		/** ��RefSeq����ȡ��ncRNA���� */
		String outNcRNA;
		
		/**
		 * �趨����ļ��к�ǰ׺������趨�˾Ͳ����趨�����
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
		 * @param rnaDataRegx �Զ�ת��ΪСд
		 */
		public void setRNAdata(String rnaDataFile, String rnaDataRegx) {
			this.RNAdataFile = rnaDataFile;
			this.RNAdataRegx = rnaDataRegx.toLowerCase();
		}
		/**
		 * ����ȡ��NCBI�����ص�refseq�ļ�
		 * @param refseqFile
		 */
		public void setRefseqFile(String refseqFile) {
			this.refseqFile = refseqFile;
		}
		/**
		 * ����ȡĳ���е�rfam�ļ�
		 * @param rfamFile
		 * @param regx rfam��������
		 */
		public void setRfamFile(String rfamFile, int taxIDrfam) {
			this.rfamFile = rfamFile;
			this.taxIDfram = taxIDrfam;//TODO �����������ֵ�ʲô����
		}
		/**
		 * ��ȡ����
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
		 * ��NCBI��refseq.fa�ļ�����ȡNCRNA
		 * @param refseqFile
		 * @param outNCRNA
		 * @param regx ���� "NR_\\d+|XR_\\d+";
		 */
		private void extractNCRNA(String refseqFile, String outNCRNA, String regx) {
			 SeqFastaHash seqFastaHash = new SeqFastaHash(refseqFile,regx,false, false);
			 seqFastaHash.writeToFile( regx ,outNCRNA );
		}
		/**
		 * ��miRBase��RNAdata�ļ�����ȡmiRNA����
		 * @param hairpinFile
		 * @param outNCRNA
		 * @param regx ���ֵ�Ӣ�ģ��������hsa
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
		 * ����RNAdata�ļ���һ��block�������е�������ȡ����
		 * @param rnaDataBlock
		 * @return regex Сд��kegg��д����hsa
		 * ����Ϊ����������
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
		 * ��miRBase��hairpinFile�ļ�����ȡmiRNA����
		 * @param hairpinFile
		 * @param outNCRNA
		 * @param regx ���ֵ�Ӣ�ģ��������Homo sapiens
		 */
		private void extractRfam(String rfamFile, String outRfam, int taxIDquery) {
			TxtReadandWrite txtOut = new TxtReadandWrite(outRfam, true);
			 SeqFastaHash seqFastaHash = new SeqFastaHash(rfamFile,null,false, false);
			 seqFastaHash.setDNAseq(true);
			 ArrayList<SeqFasta> lsSeqfasta = seqFastaHash.getSeqFastaAll();
			 for (SeqFasta seqFasta : lsSeqfasta) {
				 int taxID = 0;
				 try {
					 taxID = Integer.parseInt(seqFasta.getSeqName().trim().split(" +")[1].split(":")[0]);
				} catch (Exception e) {
					System.out.println(seqFasta.getSeqName());
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
	}

}

