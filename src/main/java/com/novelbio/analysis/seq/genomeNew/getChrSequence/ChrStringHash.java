package com.novelbio.analysis.seq.genomeNew.getChrSequence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * ����������Ⱦɫ������֣�����װ��Ⱦɫ���࣬��������Hash����ʽ���� Ŀǰ�����н������о�̬���� ͬʱ������ȡĳ��λ�õ����� ����ȡ�����ظ�����
 * ���ߣ��ڽ� 20090617
 */
public class ChrStringHash {
	private static Logger logger = Logger.getLogger(ChrStringHash.class);
	String Chrpatten = "Chr\\w+";
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
	 * ����chrID��chrLength�Ķ�Ӧ��ϵ
	 */
	HashMap<String, Long> hashChrLength = new HashMap<String, Long>();

	/**
	 * ���������Ա�
	 */
	private HashMap<Character, Character> compMap;// ��������ϣ��

	/**
	 * ��������չ�ϣ��ֵ Ŀǰ��A-T�� G-C��N-N �Ķ�Ӧ��ϵ�������˴�Сд�Ķ�Ӧ�� ��������Ҫ����µ�
	 */
	private void compmapFill() {
		compMap = new HashMap<Character, Character>();// ��������ϣ��
		compMap.put(Character.valueOf('A'), Character.valueOf('T'));
		compMap.put(Character.valueOf('a'), Character.valueOf('t'));
		compMap.put(Character.valueOf('T'), Character.valueOf('A'));
		compMap.put(Character.valueOf('t'), Character.valueOf('a'));
		compMap.put(Character.valueOf('G'), Character.valueOf('C'));
		compMap.put(Character.valueOf('g'), Character.valueOf('c'));
		compMap.put(Character.valueOf('C'), Character.valueOf('G'));
		compMap.put(Character.valueOf('c'), Character.valueOf('g'));
		compMap.put(Character.valueOf(' '), Character.valueOf(' '));
		compMap.put(Character.valueOf('N'), Character.valueOf('N'));
		compMap.put(Character.valueOf('n'), Character.valueOf('n'));
	}

	/**
	 * ���Ӳ�̶�ȡȾɫ���ļ��ķ��� ע��
	 * ����һ���ļ��У�����ļ������汣����ĳ�����ֵ�����Ⱦɫ��������Ϣ��<b>�ļ����������ν�Ӳ���"/"��"\\"</b>
	 * һ���ı�����һ��Ⱦɫ�壬��fasta��ʽ���棬ÿ���ı���">"��ͷ��Ȼ�������ÿ�й̶��ļ����(��UCSCΪ50����TIGRRiceΪ60��)
	 * �ı��ļ���(�����Ǻ�׺������Ȼû�к�׺��Ҳ��)Ӧ���Ǵ����ҵ�chrID
	 * ���������һ��Hashtable--chrID(String)---SeqFile(RandomAccessFile)��<br>
	 * ��һ��Hashtable--chrID(String)---SeqFile(BufferedReader)��<br>
	 * ����chrIDһֱΪСд
	 * 
	 * @param chrFilePath
	 * @throws Exception 
	 * @throws IOException
	 */
	public ChrStringHash(String chrFilePath) 
	{
		try {
			setChrFile(chrFilePath);
		} catch (Exception e) {
			logger.error("��ȡ�����ļ����� "+chrFilePath);
			e.printStackTrace();
		}
	}
	/**
	 * �趨�����ļ���
	 * @throws FileNotFoundException 
	 */
	private void setChrFile(String chrFilePath) throws Exception
	{

		if (compMap == null) {
			compmapFill();
		}
		if (!chrFilePath.endsWith(File.separator)) {
			chrFilePath = chrFilePath + File.separator;
		}
		ArrayList<String[]> chrFile = FileOperate.getFoldFileName(chrFilePath,
				"\\bchr\\w*", "*");
		hashChrSeqFile = new HashMap<String, RandomAccessFile>();
		hashBufChrSeqFile = new HashMap<String, BufferedReader>();

		for (int i = 0; i < chrFile.size(); i++) {
			RandomAccessFile chrRAseq = null;
			TxtReadandWrite txtChrTmp = new TxtReadandWrite();
			BufferedReader bufChrSeq = null;
			String[] chrFileName = chrFile.get(i);
			String fileNam = "";

			if (chrFileName[1].equals(""))
				fileNam = chrFilePath + chrFileName[0];
			else
				fileNam = chrFilePath + chrFileName[0] + "." + chrFileName[1];

			chrRAseq = new RandomAccessFile(fileNam, "r");
			txtChrTmp.setParameter(fileNam, false, true);
			bufChrSeq = txtChrTmp.readfile();

			if (i == 0) // ����ÿһ���ļ���ÿһ��Seq�����
			{
				chrRAseq.seek(0);
				chrRAseq.readLine();
				String seqRow = chrRAseq.readLine();
				lengthRow = seqRow.length();// ÿ�м������
			}
			hashChrSeqFile.put(chrFileName[0].toLowerCase(), chrRAseq);
			hashBufChrSeqFile.put(chrFileName[0].toLowerCase(), bufChrSeq);
		}
		getChrLength();
	
	}
	private void getChrLength() throws IOException {
		Iterator iter = hashChrSeqFile.entrySet().iterator();
		ArrayList<String[]> lsResult = new ArrayList<String[]>();// ��������
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String chrID = (String) entry.getKey();
			RandomAccessFile chrRAfile = (RandomAccessFile) entry.getValue();
			// �趨��0λ
			chrRAfile.seek(0);
			// ���ÿ��Ⱦɫ��ĳ��ȣ��ļ�����-��һ�е�
			String fastaID = chrRAfile.readLine();
			int lengthChrID = -1;
			if (fastaID.contains(">"))
				lengthChrID = fastaID.length();// ��һ�У���>�ŵĳ���

			long lengthChrSeq = chrRAfile.length();
			long tmpChrLength = (lengthChrSeq - lengthChrID - 1)
					/ (lengthRow + 1) * lengthRow
					+ (lengthChrSeq - lengthChrID - 1) % (lengthRow + 1);
			hashChrLength.put(chrID, tmpChrLength);
		}
	}

	/**
	 * ���趨Chr�ļ��󣬿��Խ����г���������ļ� ����ļ�Ϊ chrID(Сд)+��\t��+chrLength+���� ����˳�����
	 * 
	 * @param outFile
	 *            ��������ļ���������ȫ��·��
	 * @throws IOException
	 */
	public void saveChrLengthToFile(String outFile) {
		Iterator iter = hashChrLength.entrySet().iterator();
		ArrayList<String[]> lsResult = new ArrayList<String[]>();// ��������
		while (iter.hasNext()) {
			String[] tmpResult = new String[2];
			Map.Entry entry = (Map.Entry) iter.next();
			String chrID = (String) entry.getKey();
			long lengthChrSeq = (Long) entry.getValue();
			tmpResult[0] = chrID;
			tmpResult[1] = lengthChrSeq + "";
			lsResult.add(tmpResult);
		}
		TxtReadandWrite txtChrLength = new TxtReadandWrite();
		txtChrLength.setParameter(outFile, true, false);
		try {
			txtChrLength.ExcelWrite(lsResult, "\t", 1, 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ����chrID,chrID���Զ�ת��ΪСд���Ͷ�ȡ������Լ��յ㣬���ض�ȡ������
	 * startNum=204;�ӵڼ��������ʼ��ȡ����1��ʼ������ע��234�Ļ���ʵ��Ϊ��234��ʼ��ȡ������substring���� long
	 * endNum=254;//�����ڼ����������1��ʼ������ʵ�ʶ�����endNum������� ������ȡ����
	 * 
	 * @throws IOException
	 */
	private String getSeq(String chrID, long startlocation, long endlocation)
			throws IOException {
		startlocation--;
		RandomAccessFile chrRASeqFile = hashChrSeqFile.get(chrID.toLowerCase());// �ж��ļ��Ƿ����
		if (chrRASeqFile == null) {
			logger.error( "�޸�Ⱦɫ��: "+ chrID);
			return null;
		}
		int startrowBias = 0;
		int endrowBias = 0;
		// �趨��0λ
		chrRASeqFile.seek(0);
		String fastaID = chrRASeqFile.readLine();
		int lengthChrID = -1;
		if (fastaID.contains(">"))
			lengthChrID = fastaID.length();// ��һ�У���>�ŵĳ���
		long lengthChrSeq = chrRASeqFile.length();
		long rowstartNum = startlocation / lengthRow;
		startrowBias = (int) (startlocation % lengthRow);
		long rowendNum = endlocation / lengthRow;
		endrowBias = (int) (endlocation % lengthRow);
		// ʵ���������ļ��е����
		long startRealCod = (lengthChrID + 1) + (lengthRow + 1) * rowstartNum
				+ startrowBias;
		long endRealCod = (lengthChrID + 1) + (lengthRow + 1) * rowendNum
				+ endrowBias;
		/**
		 * ���λ�㳬���˷�Χ����ô����λ��
		 */
		if (startlocation < 0 || startRealCod >= lengthChrSeq
				|| endlocation < 1 || endRealCod >= lengthChrSeq) {
			logger.error(chrID + " " + startlocation + " " + endlocation + " Ⱦɫ���������");
			return null;
		}

		if (endlocation <= startlocation) {
			logger.error(chrID + " "+ startlocation + " " + endlocation + " ���������");
			return null;
			}
		if (endlocation - startlocation > 200000) {
			logger.error(chrID + " " + startlocation + " " + endlocation
					+ " �����ȡ20000bp");
			return null;
		}
		// ����Ŀ������
		StringBuilder sequence = new StringBuilder();
		chrRASeqFile.seek(startRealCod);

		if (rowendNum - rowstartNum == 0) {
			String seqResult = chrRASeqFile.readLine();
			seqResult = seqResult.substring(0, endrowBias - startrowBias);
			return seqResult;
		} else {
			for (int i = 0; i < rowendNum - rowstartNum; i++) {
				sequence.append(chrRASeqFile.readLine());
			}
			String endline = chrRASeqFile.readLine();
			endline = endline.substring(0, endrowBias);
			sequence.append(endline);
			String seqResult = sequence.toString();
			return seqResult;
		}
	}

	/**
	 * * ����Ⱦɫ��list��Ϣ �������������Լ��Ƿ�Ϊ���򻥲�,����ChrIDΪ chr1��chr2��chr10���� ��������
	 * ��ȡ����Ϊ�����䣬�������ȡ30-40bp��ôʵ����ȡ���Ǵ�30��ʼ��40������11�����
	 * @param cisseq
	 *            ������
	 * @param chrID
	 *            Ŀ��Ⱦɫ�����ƣ������ڹ�ϣ���в��Ҿ���ĳ��Ⱦɫ��
	 * @param startlocation
	 *            �������
	 * @param endlocation
	 *            �����յ�
	 * @return
	 */
	public String getSeq(boolean cisseq, String chrID, long startlocation,
			long endlocation) {
		String sequence = null;
		try {
			sequence = getSeq(chrID, startlocation, endlocation);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (sequence == null) {
			return null;
		}
		if (cisseq ) {
			return sequence;
		} else {
			return resCompSeq(sequence, compMap);
		}
	}

	/**
	 * ����Ⱦɫ����λ�úͷ��򷵻�����<br>
	 * ��ȡ����Ϊ�����䣬�������ȡ30-40bp��ôʵ����ȡ���Ǵ�30��ʼ��40������11�����
	 * @param chrlocationȾɫ���ŷ�����
	 *            ��Chr:1000-2000,�Զ���chrIDСд,chrID����������ʽץȡ������ν��Сд�����Զ�ת��ΪСд
	 * @param cisseq����
	 *            ��true:���� false:���򻥲�
	 */
	public String getSeq(String chrlocation, boolean cisseq) {
		/**
		 * �ж�Chr��ʽ�Ƿ���ȷ���Ƿ�����Ч��Ⱦɫ��
		 */
		Pattern pattern = Pattern.compile(Chrpatten, Pattern.CASE_INSENSITIVE);
		Matcher matcher;
		matcher = pattern.matcher(chrlocation);
		if (!matcher.find()) {
			logger.error("ReadSiteȾɫ���ʽ����"+ chrlocation);
			return null;
		}
		String chr = matcher.group();

		/**
		 * ��ȡ��ʼλ�����ֹλ��
		 */
		Pattern patternnumber = Pattern.compile("(?<!\\w)\\d+(?!\\w)",
				Pattern.CASE_INSENSITIVE);
		Matcher matchernumber;
		matchernumber = patternnumber.matcher(chrlocation);
		int[] location = new int[2];
		int i = 0;
		while (matchernumber.find()) {
			location[i] = Integer.parseInt(matchernumber.group());
			i++;
		}
		if (i > 2 || location[1] <= location[0]) {
			logger.error(chrlocation + " " + cisseq + " Ⱦɫ��λ�ô���");
			return null;
		}
		return getSeq(cisseq, chr.toLowerCase(), location[0], location[1]);
	}

	/**
	 * ����peakλ�㣬����ָ����Χ��sequence,chr����������ʽץȡ������ν��Сд�����Զ�ת��ΪСд
	 * <br>
	 * ��ȡ����Ϊ�����䣬�������ȡ30-40bp��ôʵ����ȡ���Ǵ�30��ʼ��40������11�����
	 * @param chr
	 *            ,
	 * @param peaklocation
	 *            peak summit������
	 * @param region
	 *            peak���ҵķ�Χ
	 * @param cisseq
	 *            true:������ false�����򻥲���
	 */

	public String getSeq(String chr, int peaklocation, int region,
			boolean cisseq) {
		/**
		 * �ж�Chr��ʽ�Ƿ���ȷ���Ƿ�����Ч��Ⱦɫ��
		 */
		Pattern pattern = Pattern.compile(Chrpatten, Pattern.CASE_INSENSITIVE);
		Matcher matcher; // matcher.groupCount() ���ش�ƥ����ģʽ�еĲ���������
		matcher = pattern.matcher(chr);
		if (!matcher.find()) {
			logger.error(chr + " " + peaklocation + " " + region + " Ⱦɫ���ʽ����");
			return "ReadSiteȾɫ���ʽ����";
		} else {
			chr = matcher.group().toLowerCase();
		}
		int startnum = peaklocation - region;
		int endnum = peaklocation + region;
		return getSeq(cisseq, chr, startnum, endnum);
	}

	/**
	 * �������У��������ձ� ��÷��򻥲�����
	 */
	public String resCompSeq(String sequence,
			HashMap<Character, Character> complementmap) {
		StringBuilder recomseq = new StringBuilder();
		int length = sequence.length();
		Character base;
		for (int i = length - 1; i >= 0; i--) {
			base = complementmap.get(sequence.charAt(i));
			if (base != null) {
				recomseq.append(complementmap.get(sequence.charAt(i)));
			} else {
				logger.error(sequence + " ����δ֪���");

				return "����δ֪��� " + sequence.charAt(i);
			}
		}
		return recomseq.toString();
	}

	/**
	 * ���ÿ��Ⱦɫ���Ӧ��bufferedreader�࣬�����ͷ��ȡ
	 * 
	 * @param chrID
	 * @return
	 */
	public BufferedReader getBufChrSeq(String chrID) {
		return hashBufChrSeqFile.get(chrID.toLowerCase());
	}

	/**
	 * ���ÿ��Ⱦɫ���Ӧ��bufferedreader�࣬�����ͷ��ȡ
	 * 
	 * @param chrID
	 * @return
	 */
	public HashMap<String, BufferedReader> getBufChrSeq() {
		return hashBufChrSeqFile;
	}

	public long getEffGenomeSize() throws IOException {
		long effGenomSize = 0;
		for (Map.Entry<String, BufferedReader> entry : hashBufChrSeqFile
				.entrySet()) {
			String chrID = entry.getKey();
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

	/**
	 * ��Chr�ļ�ÿһ�еĳ���
	 * 
	 * @return
	 */
	public int getChrLineLength() {
		return lengthRow;
	}

	/**
	 * ����chrID��chrLength�Ķ�Ӧ��ϵ
	 * chrIDͨͨСд
	 * @return
	 */
	public HashMap<String, Long> getHashChrLength() {
		return hashChrLength;
	}

	/**
	 * �ڶ�ȡchr�����ļ��󣬿���ͨ���˻������chr�ĳ�����Ϣ
	 * 
	 * @param chrID
	 * @return ArrayList<String[]> 0: chrID 1: chr���� ���Ұ���chr���ȴ�С��������
	 */
	public ArrayList<String[]> getChrLengthInfo() {
		Iterator iter = hashChrLength.entrySet().iterator();
		ArrayList<String[]> lsResult = new ArrayList<String[]>();// ��������
		while (iter.hasNext()) {
			String[] tmpResult = new String[2];
			Map.Entry entry = (Map.Entry) iter.next();
			String chrID = (String) entry.getKey();
			long lengthChrSeq = (Long) entry.getValue();
			tmpResult[0] = chrID;
			tmpResult[1] = lengthChrSeq + "";
			lsResult.add(tmpResult);
		}
		// //////////////////////////��lsChrLength����chrLen��С�����������/////////////////////////////////////////////////////////////////////////////
		Collections.sort(lsResult, new Comparator<String[]>() {
			public int compare(String[] arg0, String[] arg1) {
				if (Integer.parseInt(arg0[1]) < Integer.parseInt(arg1[1]))
					return -1;
				else if (Integer.parseInt(arg0[1]) == Integer.parseInt(arg1[1]))
					return 0;
				else
					return 1;
			}
		});
		// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		return lsResult;
	}

	/**
	 * ָ���Ⱦɫ���ֵ�����ذ�����ÿ��Ⱦɫ����Ӧֵ��Ⱦɫ�����������,resolution��int[resolution]�������ڻ�ͼ
	 * ��ôresolution���Ƿ��ص�int[]�ĳ���
	 * 
	 * @param chrID
	 * @param maxresolution
	 */
	public int[] getChrRes(String chrID, int maxresolution) throws Exception {
		ArrayList<String[]> chrLengthArrayList = getChrLengthInfo();
		int binLen = Integer.parseInt(chrLengthArrayList.get(chrLengthArrayList
				.size() - 1)[1]) / maxresolution;
		int resolution = (int) (hashChrLength.get(chrID) / binLen);

		Long chrLength = hashChrLength.get(chrID.toLowerCase());
		double binLength = (double) chrLength / resolution;
		int[] chrLengtharray = new int[resolution];
		for (int i = 0; i < resolution; i++) {
			chrLengtharray[i] = (int) ((i + 1) * binLength);
		}
		return chrLengtharray;
	}
	
	
	/**
	 * <br>
	 * ��ȡ����Ϊ�����䣬�������ȡ30-40bp��ôʵ����ȡ���Ǵ�30��ʼ��40������11�����
	 * @param cisseq ������
	 * @param lsInfo ArrayList-int[] ������ת¼����ÿһ����һ��������
	 * @param getIntron �Ƿ���ȡ�ں�������True���ں���Сд�������Ӵ�д��False��ֻ��ȡ������
	 */
	public String getSeq(boolean cisseq, String chrID,ArrayList<int[]> lsInfo, boolean getIntron) {
		if (!hashChrSeqFile.containsKey(chrID.toLowerCase())) {
			logger.error("û�и�Ⱦɫ�壺 "+chrID);
			return null;
		}
		
		String result = ""; boolean cis5to3 = true;
		int[] exon1 = lsInfo.get(0);
		if (exon1[0] > exon1[1]) {
			cis5to3 = false;
		}
		if (cis5to3) {
			for (int i = 0; i < lsInfo.size(); i++) {
				int[] exon = lsInfo.get(i);
				try {	
					result = result + getSeq(chrID, exon[0], exon[1]).toUpperCase(); 
					if (getIntron && i < lsInfo.size()-1) {
						result = result + getSeq(chrID,exon[1]+1, lsInfo.get(i+1)[0]-1).toLowerCase();
					}
				} catch (IOException e) {e.printStackTrace();}
			}
		}
		else {
			for (int i = lsInfo.size() - 1; i >= 0; i--) {
				int[] exon = lsInfo.get(i);
				try {	
					result = result + getSeq(chrID, exon[1], exon[0]).toUpperCase(); 
					if (getIntron && i > 0) {
						result = result + getSeq(chrID,exon[0] + 1, lsInfo.get(i-1)[1] - 1).toLowerCase();
					}
				} catch (IOException e) {e.printStackTrace();}
			}
		}
		if (!cisseq) {
			result = resCompSeq(result, compMap);
		}
		return result;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
