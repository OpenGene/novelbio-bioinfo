package com.novelbio.analysis.seq.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.GffChrAnno;
import com.novelbio.analysis.seq.genome.GffChrSeq;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.HashMapLsValue;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.species.Species;
/**
 * ��û���ؽ����bam�ļ�
 * @author zong0jie
 *
 */
public class MapRsem implements MapRNA{
	public static void main(String[] args) {
		MapRsem mapRsem = new MapRsem();
		Species species = new Species(9606);
		GffChrAbs gffChrAbs = new GffChrAbs(species);
		mapRsem.setExePath("", "");
		mapRsem.setGffChrAbs(gffChrAbs);
		mapRsem.createGene2IsoAndRefSeq();
	}
	private static Logger logger = Logger.getLogger(MapRsem.class);
	
	Species species;
	GffChrSeq gffChrSeq = null;
	GffChrAbs gffChrAbs = null;
	/** ��GffFile�Զ����� */
	String gene2isoFile = "";
	String refFile = "";
	/** �Զ����� */
	String rsemIndex = "";
	
	/** rsem��·�� */
	String exePathRsem = "";
	/** bowtie��·�� */
	String exePathBowtie = "";
	
	/** �߳��� */
	int threadNum = 4;
	
	List<FastQ> lsLeftFq;
	List<FastQ> lsRightFq;
	
	boolean pairend = false;
	/** ����ļ����Լ�ǰ׺ */
	String outPathPrefix = "";
	
	/**����֮���gene���ֵ������������� */
	HashMapLsValue<String, Double> mapGeneID2LsExp; 
	/** rsem �� rpkm��������10^6 �� */
	int foldRsem2RPKM = 1000000;
	
	public MapRsem() {
		SoftWareInfo softWareInfoRsem = new SoftWareInfo();
		softWareInfoRsem.setName(SoftWare.rsem);
		SoftWareInfo softWareInfoBowtie = new SoftWareInfo();
		softWareInfoBowtie.setName(SoftWare.bowtie);
		setExePath(softWareInfoRsem.getExePath(), softWareInfoBowtie.getExePath());
	}

	/**
	 * �趨Gff�ļ���chrFile
	 * @param gffFile
	 */
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
		gffChrSeq = new GffChrSeq(gffChrAbs);
		this.species = gffChrAbs.getSpecies();
		setFileRef(species.getRefseqFile());
	}
	/**
	 * �趨bwa���ڵ��ļ����Լ����ȶԵ�·��
	 * @param exePathRsem rsem��·�� ����ڸ�Ŀ¼��������Ϊ""��null
	 * @param exePathBowtie bowtie��·����ʲôʱ��rsem֧��bowtie2�ˣ���ô������
	 */
	public void setExePath(String exePathRsem, String exePathBowtie) {
		if (exePathRsem == null || exePathRsem.trim().equals(""))
			this.exePathRsem = "";
		else
			this.exePathRsem = FileOperate.addSep(exePathRsem);
		
		if (exePathBowtie == null || exePathBowtie.trim().equals(""))
			this.exePathBowtie = "";
		else
			this.exePathBowtie = FileOperate.addSep(exePathBowtie);
	}
	/**
	 * �趨Gff�ļ���refFile
	 * @param gffFile
	 */
	public void setFileRef(String refFile) {
		this.refFile = refFile;
	}
	public void setThreadNum(int threadNum) {
		if (threadNum > 0 && threadNum < 24) {
			this.threadNum = threadNum;
		}
	}
	/** û�� */
	public void setIndelLen(int indelLen) {}
	/** û�� */
	public void setStrandSpecifictype(StrandSpecific strandSpecifictype) {}

	/** û�� */
	public void setInsert(int insert) {}
	/** û�� */
	public void setMismatch(int mismatch) {}
	
	public SoftWare getBowtieVersion() {
		return SoftWare.bowtie;
	}
	/** mapping����ý��
	 * Ϊ��ֹһ��geneID��Ӧ���exp��value��������list������value
	 *  */
	public HashMapLsValue<String, Double> getMapGeneID2LsExp() {
		return mapGeneID2LsExp;
	}
	/** ����ȫ�µ�reference */
	private void createGene2IsoAndRefSeq() {
		String pathRsemIndex = FileOperate.getParentPathName(gffChrAbs.getSeqHash().getChrFile()) + "index/rsemRef_Index_" + species.getVersion().replace(" ", "") + FileOperate.getSepPath();
		String refFileRsem = pathRsemIndex +  "RefGene.fa";
		gene2isoFile = pathRsemIndex +  "RefGene_gene2iso.txt";

		if (!FileOperate.isFileExist(refFileRsem)) {
			FileOperate.createFolders(pathRsemIndex);
			if (!FileOperate.isFileExist(refFile)) {
				gffChrSeq.writeIsoFasta(refFileRsem);
				gffChrAbs.getGffHashGene().writeGene2Iso(gene2isoFile);
			} else {
				FileOperate.copyFile(refFile, refFileRsem, true);
			}
		}
		refFile = refFileRsem;//��rsem��reffile�滻��reffile����Ϊ���涼����reffile��������
		
		if (!FileOperate.isFileExist(gene2isoFile)) {
			TxtReadandWrite txtGene2Iso = new TxtReadandWrite(gene2isoFile, true);
			SeqFastaHash seqFastaHash = new SeqFastaHash(refFile, null, false);
			for (String geneIDstr : seqFastaHash.getLsSeqName()) {
				GeneID geneID = new GeneID(geneIDstr, species.getTaxID());
				String symbol = geneID.getSymbol();
				if (symbol == null || symbol.equals("")) {
					symbol = geneIDstr;
				}
				txtGene2Iso.writefileln(symbol + "\t" + geneIDstr);
			}
			txtGene2Iso.close();
		}
	}
	private String getThreadNum() {
		return "-p " + threadNum + " ";
	}
	/**
	 * ������˵����У����û����ǰ�����
	 * @param fqFile
	 */
	public void setLeftFq(List<FastQ> lsLeftFastQs) {
		this.lsLeftFq = lsLeftFastQs;
	}
	/**
	 * �����Ҷ˵����У����û����ǰ�����
	 * @param fqFile
	 */
	public void setRightFq(List<FastQ> lsRightFastQs) {
		this.lsRightFq = lsRightFastQs;
	}
	
	public void setOutPathPrefix(String outPathPrefix) {
		this.outPathPrefix = outPathPrefix;
	}
	/**
	 * ������������������bowtie1����bowtie2������
	 * @param bowtie2
	 */
	private void IndexMakeBowtie() {
		createGene2IsoAndRefSeq();
		rsemIndex = FileOperate.changeFileSuffix(refFile, "_rsemIndex", "");
		if (FileOperate.isFileExist(rsemIndex + ".3.ebwt") == true)
			return;
		String cmd = exePathRsem + "rsem-prepare-reference  --transcript-to-gene-map ";
		//TODO :�����Ƿ��Զ��ж�Ϊsolid
		cmd = cmd + gene2isoFile + " " + refFile + " " + rsemIndex;
		CmdOperate cmdOperate = new CmdOperate(cmd,"RsemMakeIndex");
		cmdOperate.run();
	}
	private String getOffset() {
		if (lsLeftFq.get(0).getOffset() == FastQ.FASTQ_ILLUMINA_OFFSET) {
			return " --phred64-quals ";
		}
		return " --phred33-quals ";
	}
	private String getBowtiePath() {
		if (exePathBowtie != null && !exePathBowtie.equals("")) {
			return "--bowtie-path " + exePathBowtie + "bowtie ";
		}
		return "";
	}
	private String getPairend() {
		if (lsLeftFq.size() > 0 && lsRightFq.size() > 0)
			pairend = true;
		else
			pairend = false;
		
		if (pairend) {
			return "--paired-end ";
		}
		return "";
	}
	/**
	 * �ȶ����в�������
	 * @return
	 */
	public void mapReads() {
		IndexMakeBowtie();
		String cmd = exePathRsem + "rsem-calculate-expression " + getBowtiePath();
		cmd = cmd + getOffset() + getPairend() + getThreadNum();
		
		cmd = cmd + " " + CmdOperate.addQuot(lsLeftFq.get(0).getReadFileName());
		for (int i = 1; i < lsLeftFq.size(); i++) {
			cmd = cmd + "," + CmdOperate.addQuot(lsLeftFq.get(i).getReadFileName());
		}
		if (lsRightFq.size() > 0) {
			cmd = cmd + " " + CmdOperate.addQuot(lsRightFq.get(0).getReadFileName()) ;
			for (int i = 1; i < lsRightFq.size(); i++) {
				cmd = cmd + "," + CmdOperate.addQuot(lsRightFq.get(i).getReadFileName());
			}
		}
		cmd = cmd + " " + rsemIndex + " " + CmdOperate.addQuot(outPathPrefix);
		logger.info(cmd);
		CmdOperate cmdOperate = new CmdOperate(cmd,"bwaMapping");
		cmdOperate.run();
		copeResult();
	}
	
	/** �������ļ�����Ҫ������gene.result,�����gene list */
	private void copeResult() {
		mapGeneID2LsExp = new HashMapLsValue<String, Double>();
		TxtReadandWrite txtReadGeneExp = new TxtReadandWrite(outPathPrefix+".genes.results", false);
		for (String geneInfo : txtReadGeneExp.readlines()) {
			String[] ss = geneInfo.split("\t");
			double value = 0;
			try {
				value = Double.parseDouble(ss[2]);
			} catch (Exception e) {
				continue;
			}
			mapGeneID2LsExp.put(ss[0], value * foldRsem2RPKM);
		}
	}
	
	/** û�ã���tophat�õ� */
	@Override
	public void setGtfFile(String gtfFile) { }
	
}
