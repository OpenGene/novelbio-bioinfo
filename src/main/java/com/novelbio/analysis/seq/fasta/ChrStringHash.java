package com.novelbio.analysis.seq.fasta;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * ����������Ⱦɫ������֣�����װ��Ⱦɫ���࣬��������Hash����ʽ���� Ŀǰ�����н������о�̬���� ͬʱ������ȡĳ��λ�õ����� ����ȡ�����ظ�����
 * ���ߣ��ڽ� 20090617
 */
public class ChrStringHash extends SeqHashAbs{
	private static Logger logger = Logger.getLogger(ChrStringHash.class);
	/**
	 * ���Ӳ�̶�ȡȾɫ���ļ��ķ�����ò�ƺ���Ӳ�̣������ù�̬Ӳ�� ע��
	 * ����һ���ļ��У�����ļ������汣����ĳ�����ֵ�����Ⱦɫ��������Ϣ��<b>�ļ����������ν�Ӳ���"/"��"\\"</b>
	 * һ���ı�����һ��Ⱦɫ�壬��fasta��ʽ���棬ÿ���ı���">"��ͷ��Ȼ�������ÿ�й̶��ļ����(��UCSCΪ50����TIGRRiceΪ60��)
	 * �ı��ļ���(�����Ǻ�׺������Ȼû�к�׺��Ҳ��)Ӧ���Ǵ����ҵ�chrID
	 * ���������һ��Hashtable--chrID(String)---SeqFile(RandomAccessFile)��<br>
	 * ��һ��Hashtable--chrID(String)---SeqFile(BufferedReader)��<br>
	 * @param chrFilePath
	 * @param regx һ��Ϊ"\\bchr\\w*"�� �ø�������ʽȥ�����ļ����к���Chr���ļ���ÿһ���ļ�����Ϊ��һ��Ⱦɫ��
	 * @param CaseChange �Ƿ�������ת��ΪСд��һ��תΪСд
	 */
	public ChrStringHash(String chrFilePath,String regx, boolean CaseChange) {
		super(chrFilePath, regx, CaseChange);
		setFile();
	}
	/**
	 * ��Ⱦɫ����Ϣ�����ϣ��,����RandomAccessFile���棬������ ��ϣ��ļ���Ⱦɫ�����ƣ�����Сд����ʽ�磺chr1��chr2��chr10
	 * ��ϣ���ֵ��Ⱦɫ������У������޿ո�
	 */
	HashMap<String, RandomAccessFile> hashChrSeqFile;
	/**
	 * ��Ⱦɫ����Ϣ�����ϣ��,����BufferedReader���棬������ ��ϣ��ļ���Ⱦɫ�����ƣ�����Сд����ʽ�磺chr1��chr2��chr10
	 * ��ϣ���ֵ��Ⱦɫ������У������޿ո�
	 */
	HashMap<String, BufferedReader> hashBufChrSeqFile;
	/**
	 * Seq�ļ��ڶ��еĳ��ȣ�Ҳ����ÿ�����еĳ���+1��1�ǻس� �����Ǽ���Seq�ļ���һ�ж���>ChrID,�ڶ��п�ʼ����Seq������Ϣ
	 * ����ÿһ�е����ж��ȳ�
	 */
	int lengthRow = 0;
	/**
	 * �趨�����ļ���
	 * @throws FileNotFoundException 
	 */
	protected void setChrFile() throws Exception {
		chrFile = FileOperate.addSep(chrFile);
		if (regx == null) {
			regx = "\\bchr\\w*";
		}
		ArrayList<String[]> lsChrFile = FileOperate.getFoldFileName(chrFile,regx, "*");
		hashChrSeqFile = new HashMap<String, RandomAccessFile>();
		hashBufChrSeqFile = new HashMap<String, BufferedReader>();
		lsSeqName = new ArrayList<String>();
		for (int i = 0; i < lsChrFile.size(); i++) {
			RandomAccessFile chrRAseq = null;
			TxtReadandWrite txtChrTmp = new TxtReadandWrite();
			BufferedReader bufChrSeq = null;
			String[] chrFileName = lsChrFile.get(i);
			String fileNam = "";
			lsSeqName.add(chrFileName[0]);
			if (chrFileName[1].equals(""))
				fileNam = chrFile + chrFileName[0];
			else
				fileNam = chrFile + chrFileName[0] + "." + chrFileName[1];

			chrRAseq = new RandomAccessFile(fileNam, "r");
			txtChrTmp.setParameter(fileNam, false, true);
			bufChrSeq = txtChrTmp.readfile();
			// ����ÿһ���ļ���ÿһ��Seq�����
			if (i == 0) {
				chrRAseq.seek(0);
				chrRAseq.readLine();
				String seqRow = chrRAseq.readLine();
				lengthRow = seqRow.length();// ÿ�м������
			}
			if (CaseChange) {
				hashChrSeqFile.put(chrFileName[0].toLowerCase(), chrRAseq);
				hashBufChrSeqFile.put(chrFileName[0].toLowerCase(), bufChrSeq);
			}
			else {
				hashChrSeqFile.put(chrFileName[0], chrRAseq);
				hashBufChrSeqFile.put(chrFileName[0], bufChrSeq);
			}
		}
		setChrLength();
	}
	/** �趨Ⱦɫ�峤�� */
	private void setChrLength() throws IOException {
		for (Entry<String, RandomAccessFile> entry : hashChrSeqFile.entrySet()) {
			String chrID = entry.getKey();
			RandomAccessFile chrRAfile = entry.getValue();
			// �趨��0λ
			chrRAfile.seek(0);
			// ���ÿ��Ⱦɫ��ĳ��ȣ��ļ�����-��һ�е�
			String fastaID = chrRAfile.readLine();
			int lengthChrID = -1;
			if (fastaID.contains(">"))
				lengthChrID = fastaID.length();// ��һ�У���>�ŵĳ���

			long lengthChrSeq = chrRAfile.length();
			long tmpChrLength = (lengthChrSeq - lengthChrID - 1) / (lengthRow + 1) * lengthRow + (lengthChrSeq - lengthChrID - 1) % (lengthRow + 1);
			hashChrLength.put(chrID, tmpChrLength);
		}
	}
	/**
	 * ����chrID,chrID���Զ�ת��ΪСд���Ͷ�ȡ������Լ��յ㣬���ض�ȡ������
	 * startNum=204;�ӵڼ��������ʼ��ȡ����1��ʼ������ע��234�Ļ���ʵ��Ϊ��234��ʼ��ȡ������substring���� long
	 * endNum=254;//�����ڼ����������1��ʼ������ʵ�ʶ�����endNum������� ������ȡ����
	 * @throws IOException
	 */
	protected SeqFasta getSeqInfo(String chrID, long startlocation, long endlocation) throws IOException {
		startlocation--;
		RandomAccessFile chrRASeqFile = hashChrSeqFile.get(chrID.toLowerCase());// �ж��ļ��Ƿ����
		if (chrRASeqFile == null) {
			logger.error( "�޸�Ⱦɫ��: "+ chrID);
			return null;
		}
		int startrowBias = 0, endrowBias = 0;
		// �趨��0λ
		chrRASeqFile.seek(0);
		String fastaID = chrRASeqFile.readLine();
		int lengthChrID = -1;
		if (fastaID.contains(">"))
			lengthChrID = fastaID.length();// ��һ�У���>�ŵĳ���
		else
			logger.error("���������fasta��ʽ��" + chrID);

		long lengthChrSeq = chrRASeqFile.length();
		long rowstartNum = startlocation / lengthRow;
		long rowendNum = endlocation / lengthRow;

		startrowBias = (int) (startlocation % lengthRow);
		endrowBias = (int) (endlocation % lengthRow);
		// ʵ���������ļ��е����
		long startRealCod = (lengthChrID + 1) + (lengthRow + 1) * rowstartNum + startrowBias;
		long endRealCod = (lengthChrID + 1) + (lengthRow + 1) * rowendNum + endrowBias;
		//���λ�㳬���˷�Χ����ô����λ��
		if (startlocation < 0 || startRealCod >= lengthChrSeq || endlocation < 1 || endRealCod >= lengthChrSeq || endlocation <= startlocation) {
			logger.error(chrID + " " + startlocation + " " + endlocation + " Ⱦɫ���������");
			return null;
		}
		if (endlocation - startlocation > 200000) {
			logger.error(chrID + " " + startlocation + " " + endlocation + " �����ȡ20000bp");
			return null;
		}

		SeqFasta seqFasta = new SeqFasta();
		seqFasta.setName(chrID + "_" + startlocation + "_" + endlocation);
		// ����Ŀ������
		StringBuilder sequence = new StringBuilder();
		chrRASeqFile.seek(startRealCod);
		
		if (rowendNum - rowstartNum == 0) {
			String seqResult = chrRASeqFile.readLine();
			seqResult = seqResult.substring(0, endrowBias - startrowBias);
			seqFasta.setSeq(seqResult);
		} else {
			for (int i = 0; i < rowendNum - rowstartNum; i++) {
				sequence.append(chrRASeqFile.readLine());
			}
			String endline = chrRASeqFile.readLine();
			endline = endline.substring(0, endrowBias);
			sequence.append(endline);
			seqFasta.setSeq(sequence.toString());
		}
		return seqFasta;
	}
	/**
	 * ���ÿ��Ⱦɫ���Ӧ��bufferedreader�࣬�����ͷ��ȡ
	 * @param chrID
	 * @return
	 */
	public BufferedReader getBufChrSeq(String chrID) {
		return hashBufChrSeqFile.get(chrID.toLowerCase());
	}
	/**
	 * ���ÿ��Ⱦɫ���Ӧ��bufferedreader�࣬�����ͷ��ȡ
	 * @param refID
	 * @return
	 */
	public HashMap<String, BufferedReader> getBufChrSeq() {
		return hashBufChrSeqFile;
	}
	/**
	 * ����������ļ������
	 * @return
	 * @throws IOException
	 */
	public long getEffGenomeSize() throws IOException {
		long effGenomSize = 0;
		for (Map.Entry<String, BufferedReader> entry : hashBufChrSeqFile.entrySet()) {
			BufferedReader chrReader = entry.getValue();
			String content = "";
			while ((content = chrReader.readLine()) != null) {
				if (content.startsWith(">")) {
					continue;
				}
				String tmp = content.trim().replace("N", "").replace("n", "");
				effGenomSize = effGenomSize + tmp.length();
			}
		}
		return effGenomSize;
	}
}
