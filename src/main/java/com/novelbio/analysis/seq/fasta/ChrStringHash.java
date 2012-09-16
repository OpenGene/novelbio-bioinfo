package com.novelbio.analysis.seq.fasta;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fastq.FastQRecord;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * ����������Ⱦɫ������֣�����װ��Ⱦɫ���࣬��������Hash����ʽ���� Ŀǰ�����н������о�̬���� ͬʱ������ȡĳ��λ�õ����� ����ȡ�����ظ�����
 * ���ߣ��ڽ� 20090617
 */
public class ChrStringHash extends SeqHashAbs{
	public static void main(String[] args) {
		ChrStringHash chrStringHash = new ChrStringHash("/media/winE/Bioinformatics/genome/human/hg19_GRCh37/ChromFa", null);
		System.out.println(chrStringHash.getSeq("chrY", 69, 77));
	}
	private static Logger logger = Logger.getLogger(ChrStringHash.class);
	
	/** ���¹�ϣ��ļ���Ⱦɫ�����ƣ�����Сд����ʽ�磺chr1��chr2��chr10 */
	HashMap<String, RandomAccessFile> mapChrID2RandomFile;
	HashMap<String, BufferedReader> mapChrID2BufReader;
	HashMap<String, TxtReadandWrite> mapChrID2Txt;

	/**
	 * Seq�ļ��ڶ��еĳ��ȣ�Ҳ����ÿ�����еĳ���+1��1�ǻس� �����Ǽ���Seq�ļ���һ�ж���>ChrID,�ڶ��п�ʼ����Seq������Ϣ
	 * ����ÿһ�е����ж��ȳ�
	 */
	int lengthRow = 0;
	/**
	 * ���Ӳ�̶�ȡȾɫ���ļ��ķ�����ò�ƺ���Ӳ�̣������ù�̬Ӳ�� ע��
	 * ����һ���ļ��У�����ļ������汣����ĳ�����ֵ�����Ⱦɫ��������Ϣ��<b>�ļ����������ν�Ӳ���"/"��"\\"</b>
	 * һ���ı�����һ��Ⱦɫ�壬��fasta��ʽ���棬ÿ���ı���">"��ͷ��Ȼ�������ÿ�й̶��ļ����(��UCSCΪ50����TIGRRiceΪ60��)
	 * �ı��ļ���(�����Ǻ�׺������Ȼû�к�׺��Ҳ��)Ӧ���Ǵ����ҵ�chrID
	 * @param chrFilePath
	 * @param regx null��Ĭ�ϣ�Ĭ��Ϊ"\\bchr\\w*"�� �ø�������ʽȥ�����ļ����к���Chr���ļ���ÿһ���ļ�����Ϊ��һ��Ⱦɫ��
	 * @param CaseChange �Ƿ�������ת��ΪСд��һ��תΪСд
	 */
	public ChrStringHash(String chrFilePath,String regx) {
		super(chrFilePath, regx);
		setFile();
	}

	/**
	 * �趨�����ļ���
	 * @throws FileNotFoundException 
	 */
	protected void setChrFile() throws Exception {
		ArrayList<String> lsChrFile = initialAndGetFileList();
		RandomAccessFile chrRAseq = null;
		TxtReadandWrite txtChrTmp = null;
		BufferedReader bufChrSeq = null;
		
		for (int i = 0; i < lsChrFile.size(); i++) {
			String fileNam = lsChrFile.get(i);
			String[] chrFileName = FileOperate.getFileNameSep(fileNam);
			lsSeqName.add(chrFileName[0]);

			chrRAseq = new RandomAccessFile(fileNam, "r");
			txtChrTmp = new TxtReadandWrite(fileNam, false);
			bufChrSeq = txtChrTmp.readfile();
			// ����ÿһ���ļ���ÿһ��Seq�����
			if (i == 0) {
				String seqRow = txtChrTmp.readFirstLines(3).get(2);
				lengthRow = seqRow.length();// ÿ�м������
			}
			String chrID = chrFileName[0].toLowerCase();
			mapChrID2RandomFile.put(chrID, chrRAseq);
			mapChrID2BufReader.put(chrID, bufChrSeq);
			mapChrID2Txt.put(chrID, txtChrTmp);
		}
		setChrLength();
	}
	
	/** ��ʼ���������ļ����е����з���������ʽ���ı��� */
	private ArrayList<String> initialAndGetFileList() {
		chrFile = FileOperate.addSep(chrFile);
		if (regx == null)
			regx = "\\bchr\\w*";
		
		mapChrID2RandomFile = new HashMap<String, RandomAccessFile>();
		mapChrID2BufReader = new HashMap<String, BufferedReader>();
		mapChrID2Txt = new HashMap<String, TxtReadandWrite>();
		lsSeqName = new ArrayList<String>();
		return FileOperate.getFoldFileNameLs(chrFile,regx, "*");
	}
	/** �趨Ⱦɫ�峤�� */
	private void setChrLength() throws IOException {
		for (Entry<String, RandomAccessFile> entry : mapChrID2RandomFile.entrySet()) {
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
	protected SeqFasta getSeqInfo(String chrID, long startlocation, long endlocation) {
		try {
			return getSeqInfoExp(chrID, startlocation, endlocation);
		} catch (Exception e) {
			return null;
		}
	}
	/**
	 * ����chrID,chrID���Զ�ת��ΪСд���Ͷ�ȡ������Լ��յ㣬���ض�ȡ������
	 * startNum=204;�ӵڼ��������ʼ��ȡ����1��ʼ������ע��234�Ļ���ʵ��Ϊ��234��ʼ��ȡ������substring���� long
	 * endNum=254;//�����ڼ����������1��ʼ������ʵ�ʶ�����endNum������� ������ȡ����
	 * @throws IOException
	 */
	private SeqFasta getSeqInfoExp(String chrID, long startlocation, long endlocation) throws IOException {
		startlocation--;
		chrID = chrID.toLowerCase();
		RandomAccessFile chrRASeqFile = mapChrID2RandomFile.get(chrID);// �ж��ļ��Ƿ����
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
		if (startlocation < 0 || startRealCod >= lengthChrSeq || endlocation < 1 || endRealCod >= lengthChrSeq || endlocation < startlocation) {
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
		return mapChrID2BufReader.get(chrID.toLowerCase());
	}
	/**
	 * ���ÿ��Ⱦɫ���Ӧ��bufferedreader�࣬�����ͷ��ȡ
	 * @param refID
	 * @return
	 */
	public HashMap<String, BufferedReader> getBufChrSeq() {
		return mapChrID2BufReader;
	}
	/**
	 * ����������ļ������
	 * @return
	 * @throws IOException
	 */
	public long getEffGenomeSize() throws IOException {
		long effGenomSize = 0;
		for (Map.Entry<String, BufferedReader> entry : mapChrID2BufReader.entrySet()) {
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
	@Override
	public Iterable<Character> readBase(String refID) {
		final String myRefID = refID.toLowerCase();
		return new Iterable<Character>() {
			@Override
			public Iterator<Character> iterator() {
				IteratorBase iteratorBase = new IteratorBase();
				iteratorBase.setReader(mapChrID2Txt.get(myRefID));
				return iteratorBase;
			}
		};
	}
}


class IteratorBase implements Iterator<Character> {
	TxtReadandWrite txtReader;
	BufferedReader reader;
	
	char[] tmpSeq;
	int index = 0;
	
	Character base;
	
	public void setReader(TxtReadandWrite txtRead) {
		this.txtReader = txtRead;
		try { reader = txtReader.readfile(); } catch (Exception e) { e.printStackTrace(); }
		base = getBase();
	}
	@Override
	public boolean hasNext() {
		return base != null;
	}

	@Override
	public Character next() {
		Character retval = base;
		base = getBase();
		return retval;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	private Character getBase() {
		try {
			return getBaseWithExp();
		} catch (IOException e) {
			return null;
		}
	}
	
	private Character getBaseWithExp() throws IOException {
		Character base = null;
		if (tmpSeq == null || index >= tmpSeq.length) {
			String lineTmp = reader.readLine();
			if (lineTmp == null) {
				return null;
			}
			lineTmp = lineTmp.trim();
			/////skip blank lines
			while (lineTmp.startsWith(">") || lineTmp.length() == 0) {
				lineTmp = reader.readLine();
				if (lineTmp == null) {
					return null;
				}
				lineTmp = lineTmp.trim();
			}
			/////////////////
			tmpSeq = lineTmp.toCharArray();
			index = 0;
		}

		base = tmpSeq[index];
		index++;
		return base;
	}
}
