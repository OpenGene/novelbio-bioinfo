package com.novelbio.analysis.annotation.blast;

import java.util.HashMap;

import org.apache.ibatis.migration.commands.NewCommand;
import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.generalConf.NovelBioConst;

/**
 * �Զ�������blast�ķ���
 * ���ǽ�·���������ݿ���
 * ����NCBI��blast�������blast���������ǽ�����Զ��������ݿ�
 * @author zong0jie
 *
 */
public class BlastNBC {
	public static final String BLAST_TBLASTN_AA2NR_WITH_AA = "tblasn ";
	public static final String BLAST_TBLASTX_NR2NR_WITH_AA = "tblastx ";
	public static final String BLAST_BLASTN_NR2NR_WITH_NR = "blastn ";
	public static final String BLAST_BLASTX_NR2AA_WITH_AA = "blastx ";
	public static final String BLAST_BLASTP_AA2AA_WITH_AA = "blastp ";
	private static Logger logger = Logger.getLogger(BlastNBC.class);
	String blastAll = "blastall ";
	String formatDB = "formatdb ";
	String blastInputType = "-p ";
	String queryFasta = "";
	/**���ȶԵ����ݿ⣬�����fasta�ļ�������Զ�������*/
	String databaseSeq = "";
	String blastType = BLAST_TBLASTN_AA2NR_WITH_AA;
	/**
	 * @param QfastaType �����fasta���ͣ��Ǻ��ỹ�ǵ���
	 * @param SfastaType �����fasta���ͣ��Ǻ��ỹ�ǵ���
	 * @return blast���͵Ĺ�ϣ��
	 * key��˵��
	 * value�������blast���ͣ���BLAST_TBLASTN_AA2NR_WITH_AA�ȣ���ֱ���������ñ���Ĳ���
	 */
	public static HashMap<String, String> getHashBlast(int QfastaType, int SfastaType) {
		HashMap<String, String> hashBlastType = new HashMap<String, String>();
		if (QfastaType == SeqFasta.SEQ_PRO) {
			if (SfastaType == SeqFasta.SEQ_PRO) {
				hashBlastType.put("BLASTP_AA2AA_WITH_AA", BLAST_BLASTP_AA2AA_WITH_AA);
			}
			else if (SfastaType == SeqFasta.SEQ_DNA || SfastaType == SeqFasta.SEQ_RNA) {
				hashBlastType.put("TBLASTN_AA2NR_WITH_AA", BLAST_TBLASTN_AA2NR_WITH_AA);
			}
			else if (SfastaType == SeqFasta.SEQ_UNKNOWN) {
				hashBlastType.put("TBLASTN_AA2NR_WITH_AA", BLAST_TBLASTN_AA2NR_WITH_AA);
				hashBlastType.put("BLASTP_AA2AA_WITH_AA", BLAST_BLASTP_AA2AA_WITH_AA);
			}
		}
		else if (QfastaType == SeqFasta.SEQ_DNA || QfastaType == SeqFasta.SEQ_RNA) {
			if (SfastaType == SeqFasta.SEQ_PRO) {
				hashBlastType.put("BLASTX_NR2AA_WITH_AA", BLAST_BLASTX_NR2AA_WITH_AA);
			}
			else if (SfastaType == SeqFasta.SEQ_DNA || SfastaType == SeqFasta.SEQ_RNA) {
				hashBlastType.put("TBLASTX_NR2NR_WITH_AA", BLAST_TBLASTX_NR2NR_WITH_AA);
				hashBlastType.put("BLASTN_NR2NR_WITH_NR", BLAST_BLASTN_NR2NR_WITH_NR);
			}
			else if (SfastaType == SeqFasta.SEQ_UNKNOWN) {
				hashBlastType.put("TBLASTX_NR2NR_WITH_AA", BLAST_TBLASTX_NR2NR_WITH_AA);
				hashBlastType.put("BLASTN_NR2NR_WITH_NR", BLAST_BLASTN_NR2NR_WITH_NR);
				hashBlastType.put("BLASTX_NR2AA_WITH_AA", BLAST_BLASTX_NR2AA_WITH_AA);
			}
		}
		else if (QfastaType == SeqFasta.SEQ_UNKNOWN) {
			if (SfastaType == SeqFasta.SEQ_PRO) {
				hashBlastType.put("BLASTX_NR2AA_WITH_AA", BLAST_BLASTX_NR2AA_WITH_AA);
				hashBlastType.put("BLASTP_AA2AA_WITH_AA", BLAST_BLASTP_AA2AA_WITH_AA);
			}
			else if (SfastaType == SeqFasta.SEQ_DNA || SfastaType == SeqFasta.SEQ_RNA) {
				hashBlastType.put("TBLASTN_AA2NR_WITH_AA", BLAST_TBLASTN_AA2NR_WITH_AA);
				hashBlastType.put("TBLASTX_NR2NR_WITH_AA", BLAST_TBLASTX_NR2NR_WITH_AA);
				hashBlastType.put("BLASTN_NR2NR_WITH_NR", BLAST_BLASTN_NR2NR_WITH_NR);
			}
			else if (SfastaType == SeqFasta.SEQ_UNKNOWN) {
				hashBlastType.put("TBLASTN_AA2NR_WITH_AA", BLAST_TBLASTN_AA2NR_WITH_AA);
				hashBlastType.put("TBLASTX_NR2NR_WITH_AA", BLAST_TBLASTX_NR2NR_WITH_AA);
				hashBlastType.put("BLASTN_NR2NR_WITH_NR", BLAST_BLASTN_NR2NR_WITH_NR);
				hashBlastType.put("BLASTX_NR2AA_WITH_AA", BLAST_BLASTX_NR2AA_WITH_AA);
				hashBlastType.put("BLASTP_AA2AA_WITH_AA", BLAST_BLASTP_AA2AA_WITH_AA);
			}
		}
		return hashBlastType;
	}
	/**
	 * @return blast�������Ĺ�ϣ��
	 * key��˵��
	 * value�������������֣���0Ϊ������8Ϊ��д����ֱ���������ñ���Ĳ���
	 */
	public static HashMap<String, Integer> getHashResultType() {
		HashMap<String, Integer> hashBlastType = new HashMap<String, Integer>();
		hashBlastType.put("Normal Type With Alignment", 0);
		hashBlastType.put("Simple Table", 8);
		return hashBlastType;
	}	
	public static void main(String[] args) {
		String databaseSeq = "/media/winE/Bioinformatics/BLAST/DataBase/hsaProtein/protein.fa";
		String queryFasta = "/media/winE/Bioinformatics/GenomeData/Cow/Cow_refRNA.seq";
		BlastNBC blastNBC = new BlastNBC();
		blastNBC.setBlastType(BLAST_BLASTX_NR2AA_WITH_AA);
		blastNBC.setDatabaseSeq(databaseSeq);
		blastNBC.setQueryFastaFile(queryFasta);
		blastNBC.setResultFile(FileOperate.changeFileSuffix(queryFasta, "_Blast2Hum", null));
		blastNBC.setCpuNum(4);
		blastNBC.setResultSeqNum(1);
		blastNBC.setResultAlignNum(1);
		blastNBC.blast();
	}
	/**
	 * �趨blast��ģʽ
	 * @param blastType
	 * BLAST_TBLASTN��
	 */
	public void setBlastType(String blastType) {
		this.blastType = blastType;
	}
	/**
	 * ��query��fasta�ļ�
	 * @param queryFasta
	 */
	public void setQueryFastaFile(String queryFasta) {
		this.queryFasta = queryFasta;
	}	
	/**
	 * ���ȶԵ����ݿ⣬�����fasta�ļ�������Զ�������
	 * @param databaseSeq
	 */
	public void setDatabaseSeq(String databaseSeq) {
		this.databaseSeq = databaseSeq;
	}
	String resultFile = "";
	/**
	 * ����ļ�
	 * @param resultFile
	 */
	public void setResultFile(String resultFile) {
		this.resultFile = resultFile;
	}
	int cpuNum = 2;
	/**
	 * �趨cpuʹ���������о��趨��û��
	 * Ĭ��Ϊ2
	 * @param cpuNum
	 */
	public void setCpuNum(int cpuNum) {
		this.cpuNum = cpuNum;
	}
	int resultType = 8;
	/**
	 * ����ģʽΪ0
	 * ����ģʽΪ8
	 * ���忴�ĵ�
	 * @param resultType Ĭ��Ϊ8
	 */
	public void setResultType(int resultType) {
		this.resultType = resultType;
	}
	/**
	 * ��ʾ�������
	 */
	int resultSeqNum = 2;
	/**
	 * @param resultSeqNum ��ʾ�������
	 */
	public void setResultSeqNum(int resultSeqNum) {
		this.resultSeqNum = resultSeqNum;
	}
	/**
	 * ��ʾ�����ȶԽ��
	 */
	int resultAlignNum = 2;
	/**
	 * ��������ȶԽ������setResultTypeΪ8��ʱ�����������
	 * @param resultAlignNum
	 */
	public void setResultAlignNum(int resultAlignNum) {
		this.resultAlignNum = resultAlignNum;
	}
	/** @param evalue ������ƶȣ�ԽС���ƶ�Խ�ߡ������0��1֮�䣬Ĭ��0.1��һ�㲻�ø�  */
	double evalue = 0.1;
	/** @param evalue ������ƶȣ�ԽС���ƶ�Խ�ߡ������0��1֮�䣬Ĭ��0.1��һ�㲻�ø� */
	public void setEvalue(double evalue) {
		this.evalue = evalue;
	}
	/**
	 * ��ָ�������ж�Ŀ�����н���blast
	 * @return false blastʧ��
	 * true blast�ɹ�
	 */
	public boolean blast() {
		if (indexExisted() == null) {
			return false;
		}
		//�����Ƿ����
		if (!indexExisted()) {
			//�Ƿ�ɹ�����blast���ݿ�
			if (!formatDB()) {
				return false;
			}
		}
		String cmd = "perl " + NovelBioConst.BLAST_NCBI_SCRIPT + blastAll + blastInputType + blastType
				+ "-i " + CmdOperate.addQuot(queryFasta) + " -d " + CmdOperate.addQuot(databaseSeq) + " -o " + CmdOperate.addQuot(resultFile)
				+ " -a " + cpuNum + getFilter() + " -e " + evalue + " -m " + resultType + " -v "+resultSeqNum + " -b " + resultAlignNum + " --path " + NovelBioConst.BLAST_NCBI_PATH;
		CmdOperate cmdOperate = new CmdOperate(cmd,"blast");
		cmdOperate.run();
		return true;
	}
	/**
	 * database�����Ǻ��ỹ�ǵ���
	 * @return
	 */
	private String getSeqTypePro() {
		String seqTypePro = "";
		int seqTypeFlag = SeqHash.getSeqType(databaseSeq);
		
		if (seqTypeFlag == SeqFasta.SEQ_PRO)
			seqTypePro = "T ";
		else if(seqTypeFlag == SeqFasta.SEQ_DNA)
			seqTypePro = "F ";
		else {
			logger.error("databaseSeq ���г���δ֪�ַ�");
		}
		return seqTypePro;
	}
	/**
	 * ��Ŀ�����н�����
	 * ������������ڣ�һ����Զ���������
	 * ������������ˣ�����Ҫ�����ؽ�
	 */
	private boolean formatDB() {
		String seqTypePro = getSeqTypePro();
		if (seqTypePro == null) {
			return false;
		}
		String cmd = "perl " + NovelBioConst.BLAST_NCBI_SCRIPT + formatDB + " -i " + databaseSeq + " -p "+ seqTypePro + " -o T " 
				+ "--path " + NovelBioConst.BLAST_NCBI_PATH;
		CmdOperate cmdOperate = new CmdOperate(cmd,"blastFormatDB");
		cmdOperate.run();
		return true;
	}
	/**
	 * �������Ƿ����
	 * @return true ���ڣ� false ������ null ����������
	 */
	private Boolean indexExisted() {
		boolean indexExist = false;
		int seqType = SeqHash.getSeqType(databaseSeq);
		if (seqType == SeqFasta.SEQ_DNA || seqType == SeqFasta.SEQ_RNA) {
			indexExist = FileOperate.isFileExist(databaseSeq + ".nhr") && FileOperate.isFileExist(databaseSeq + ".nin") 
					&& FileOperate.isFileExist(databaseSeq + ".nog") && FileOperate.isFileExist(databaseSeq + ".nsd")
					 && FileOperate.isFileExist(databaseSeq + ".nsi") && FileOperate.isFileExist(databaseSeq + ".nsq");
		}
		else if (seqType == SeqFasta.SEQ_PRO) {
			indexExist = FileOperate.isFileExist(databaseSeq + ".phr") && FileOperate.isFileExist(databaseSeq + ".pin") 
					&& FileOperate.isFileExist(databaseSeq + ".pog") && FileOperate.isFileExist(databaseSeq + ".psd")
					 && FileOperate.isFileExist(databaseSeq + ".psi") && FileOperate.isFileExist(databaseSeq + ".psq");
		}
		else {
			logger.error("���г���δ֪�ַ�");
			return null;
		}
		return indexExist;
	}
	/**
	 * �������μ��ظ��͵͸��Ӷ����еĲ�������T��F����ѡ�ѡ��T������
		�����ڱȶԹ����л����ε�query�����еļ��ظ��͵͸��Ӷ����У�ѡ��
		��F���򲻻����Ρ�NCBI��blast����Ĭ��ֵ�ǡ�T��������Ĭ����F��Ҳ���Ƿ��� -F F
	 *����RNA��tblastx��NCBIĬ�ϣ�Ҳ���Ƿ���"",����ᱨ��
	 */
	private String getFilter() {
		if (this.blastType == BLAST_TBLASTX_NR2NR_WITH_AA) {
			return " ";
		}
		return " -F F ";
	}
	/**
	 * ����fasta��ʽ���ļ����趨NCBI��IDΪ������ʽ����Fasta��ʽ��ID��ѡ����
	 * ����һ���µ�fasta�ļ�
	 */
	public static void getFasta(String fastaFile) {
		SeqFastaHash seqFastaHash = new SeqFastaHash(fastaFile, "\\w+_\\d+", false, false, false);
		seqFastaHash.writeToFile(FileOperate.changeFileSuffix(fastaFile, "_cleanID", null));
	}
}
