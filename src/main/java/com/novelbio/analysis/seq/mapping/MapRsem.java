package com.novelbio.analysis.seq.mapping;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.FastQ;
import com.novelbio.analysis.seq.genomeNew.GffChrAbs;
import com.novelbio.analysis.seq.genomeNew.GffChrAnno;
import com.novelbio.analysis.seq.genomeNew.GffChrSeq;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;
/**
 * ��û���ؽ����bam�ļ�
 * @author zong0jie
 *
 */
public class MapRsem {
	private static Logger logger = Logger.getLogger(MapRsem.class);
	
	GffChrSeq gffChrSeq = null;
	GffChrAnno gffChrAnno = null;
	GffChrAbs gffChrAbs = null;
	/** ��GffFile�Զ����� */
	String gene2isoFile = "";
	/** refgene���ļ� FileOperate.changeFileSuffix(chrFile, "_RefGene", "fastq"); */
	String refFile = "";
	/** FileOperate.changeFileSuffix(refFile, "_rsemIndex", "") */
	String rsemIndex = "";
	/** rsem��·�� */
	String exePathRsem = "";
	/** bowtie��·�� */
	String exePathBowtie = "";
	/** �߳��� */
	int threadNum = 4;
	ArrayList<FastQ> lsLeftFq = new ArrayList<FastQ>();
	ArrayList<FastQ> lsRightFq = new ArrayList<FastQ>();
	boolean pairend = false;
	/** ����ļ����Լ�ǰ׺ */
	String outPathPrefix = "";
	/**
	 * �趨Gff�ļ���chrFile
	 * @param gffFile
	 */
	public void setFileChr(String gffType, String gffFile, String chrFile) {
		gffChrAbs = new GffChrAbs(gffType, gffFile, chrFile, null, 0);
		gene2isoFile = FileOperate.changeFileSuffix(gffFile,"_gene2iso","txt");
		if (!FileOperate.isFileExist(gene2isoFile)) {
			gffChrAbs.getGffHashGene().writeGene2Iso(gene2isoFile);
		}
		gffChrSeq = new GffChrSeq(gffChrAbs);
		refFile = FileOperate.changeFileSuffix(chrFile, "_RefGene", "fastq");
		if (!FileOperate.isFileExist(refFile)) {
			gffChrSeq.writeIsoFasta(refFile);
		}
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
	public void setFileRef(String gffType, String gffFile, String refFile) {
		gffChrAbs = new GffChrAbs(gffType, gffFile, null, null, 0);
		gene2isoFile = FileOperate.changeFileSuffix(gffFile,"_gene2iso","txt");
		if (!FileOperate.isFileExist(gene2isoFile)) {
			gffChrAbs.getGffHashGene().writeGene2Iso(gene2isoFile);
		}
		this.refFile = refFile;
	}
	public void setThreadNum(int threadNum) {
		if (threadNum > 0 && threadNum < 20) {
			this.threadNum = threadNum;
		}
	}
	
	private String getThreadNum() {
		return "-p " + threadNum + " ";
	}
	/**
	 * ������˵����У����û����ǰ�����
	 * @param fqFile
	 */
	public void setLeftFq(String... fqFile) {
		lsLeftFq.clear();
		for (String string : fqFile) {
			FastQ fastQ = new FastQ(string, FastQ.QUALITY_MIDIAN);
			lsLeftFq.add(fastQ);
		}
	}
	/**
	 * �����Ҷ˵����У����û����ǰ�����
	 * @param fqFile
	 */
	public void setRightFq(String... fqFile) {
		lsRightFq.clear();
		for (String string : fqFile) {
			FastQ fastQ = new FastQ(string, FastQ.QUALITY_MIDIAN);
			lsRightFq.add(fastQ);
		}
	}
	public void setOutPathPrefix(String outPathPrefix) {
		this.outPathPrefix = outPathPrefix;
	}
	/**
	 * ������������������bowtie1����bowtie2������
	 * @param bowtie2
	 */
	private void IndexMakeBowtie() {
		rsemIndex = FileOperate.changeFileSuffix(refFile, "_rsemIndex", "");
		if (FileOperate.isFileExist(rsemIndex + ".3.ebwt") == true)
			return;
		String cmd = exePathRsem + "rsem-prepare-reference  --transcript-to-gene-map ";
		//TODO :�����Ƿ��Զ��ж�Ϊsolid
		cmd = cmd + gene2isoFile + " " + refFile + " " + rsemIndex;
		CmdOperate cmdOperate = new CmdOperate(cmd);
		cmdOperate.doInBackground("RsemMakeIndex");
	}
	private String getOffset() {
		if (lsLeftFq.get(0).getOffset() == FastQ.FASTQ_ILLUMINA_OFFSET) {
			return " --phred64-quals ";
		}
		return "";
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
	public SamFile mapReads() {
		IndexMakeBowtie();
		String cmd = exePathRsem + "rsem-calculate-expression " + getBowtiePath();
		cmd = cmd + getOffset() + getPairend() + getThreadNum();
		
		cmd = cmd + " " + lsLeftFq.get(0).getFileName();
		for (int i = 1; i < lsLeftFq.size(); i++) {
			cmd = cmd + "," + lsLeftFq.get(i).getFileName();
		}
		if (lsRightFq.size() > 0) {
			cmd = cmd + " " + lsRightFq.get(0).getFileName();
			for (int i = 1; i < lsRightFq.size(); i++) {
				cmd = cmd + "," + lsRightFq.get(i).getFileName();
			}
		}
		cmd = cmd + " " + rsemIndex + " " + outPathPrefix;
		logger.info(cmd);
		CmdOperate cmdOperate = new CmdOperate(cmd);
		cmdOperate.doInBackground("bwaMapping");
		return null;//����Ƿ���һ��bam�ļ�
	}
	
	
	
}
