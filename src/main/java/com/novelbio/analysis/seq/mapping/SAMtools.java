package com.novelbio.analysis.seq.mapping;

import java.io.BufferedReader;
import java.util.ArrayList;

import javax.swing.text.rtf.RTFEditorKit;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.impl.piccolo.xml.Piccolo;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.chipseq.repeatMask.repeatRun;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.Patternlocation;

/**
 * ����sam�ļ��Ĺ���
 * @author zong0jie
 *
 */
public class SAMtools{
	private static final Logger logger = Logger.getLogger(SAMtools.class);

	String samFile = "";
	boolean paired = false;
	/**
	 * ��������С��25�ľͲ�Ҫ��
	 */
	private int mapQuality = 25;
	/**
	 * ��ȡSAM�е�mapping��Ϣ�õ�
	 */
	private Patternlocation patternlocation = null;
	
	
	/**
	 * �趨��˫��
	 * @param samFile sam�ļ�
	 * @param paired ��֪���Ļ����趨false
	 * @param mapQuality mapping����������30˵����������25˵����mapping�ϡ�<=0����Ĭ��ֵ��Ϊ25
	 */
	public SAMtools(String samFile, boolean paired, int mapQuality) {
		this.samFile = samFile;
		this.paired = paired;
		if (mapQuality > 0) {
			this.mapQuality = mapQuality;
		}
		
		patternlocation = new Patternlocation("\\d+(?=M|N)", true); 
		
	}
	/**
	 * ��sam�ļ���Ϊbed�ļ�������mapping���������������ɸѡ
	 * <b>������ѡ��Ⱦɫ����ںϻ���<b>
	 * @param bedFile ��������bedFile
	 * @param uniqMapping �Ƿ�Ϊuniqmapping
	 * �������uniqmapping����ômapping�����ڵ�����
	 * @return
	 */
	public BedSeq sam2bed(String bedFile, boolean uniqMapping) {
		TxtReadandWrite txtSam = new TxtReadandWrite(samFile, false);
		
		TxtReadandWrite txtBed = new TxtReadandWrite(bedFile, true);

//		����Ϊͬһ��
//		HWUSI-EAS1734:0007:3:1:1430:16645:0     163     NC_009443       1523922 60      70M     =       
//		1524079 227     ACAGGTCAGAGATACATTTTGATAATCAATCATCATCTTCTCCTTTTAAAATAGCCATGAGACTCTGACT  
//		BD:==EDE?DFFBFFFFFEFFFBFAFFFBFFFDFFDEEFFFDDDFFDFFF=EEEECFF5FDEFDDFEB?5  XT:A:U 
//		NM:i:0  SM:i:37 AM:i:37 X0:i:1  X1:i:0  XM:i:0  XO:i:0  XG:i:0  MD:Z:70
		try {
			BufferedReader readerSam = txtSam.readfile();
			String content = ""; String preContent = null;//������һ��
			String[] tmpResult = null;
			while ((content = readerSam.readLine()) != null) {
				if (content.trim().startsWith("@")) {
					continue;
				}
				//˫���ж��������Ƿ񶼴���
				if (paired) {
					String[] preResult = tmpResult;
					tmpResult = getBedFormat(content);
					if (preContent == null || preResult == null || tmpResult == null) {
						preContent = content;
						continue;
					}
					//�Ƿ�Ϊͬһ�����еĵ�˫��
					String[] splitPre = preContent.split("\t"); String[] splitTmp = content.split("\t");
					//����ͬһ��̽�����ͬһ��Ⱦɫ����
					if (!splitPre[0].equals(splitTmp[0]) || !splitPre[2].equals(splitTmp[2])) {
						preContent = content;
						continue;
					}
					//solexa�����ʱ���������б�����һ������һ�����򣬲��ǵ�˵������
					if (preResult[5].equals(tmpResult[5])) {
						preContent = content;
						continue;
					}
					int start = Math.min(Integer.parseInt(preResult[1]),Integer.parseInt( tmpResult[1]));
					int end = Math.max(Integer.parseInt(preResult[2]),Integer.parseInt( tmpResult[2]));
					preResult[1] = start + "";
					preResult[2] = end + "";
					String result = "";
					for (String string : preResult) {
						result = result + "\t" + string;
					}
					txtBed.writefileln(result.trim());
				}
				//����ֱ�ӽ����д���ı�
				else {
					//Uniqmapping
					if (uniqMapping) {
						tmpResult = getBedFormat(content);
						if (tmpResult == null) {
							preContent = content;
							continue;
						}
						String result = "";
						for (String string : tmpResult) {
							result = result + "\t" + string;
						}
						txtBed.writefileln(result.trim());
					}
					else {
						ArrayList<String[]> lsTmpResult = getBedFormatMulti(content);
						if (lsTmpResult == null) {
							preContent = content;
							continue;
						}
						for (String[] strings : lsTmpResult) {
							String result = "";
							for (String string : strings) {
								result = result + "\t" + string;
							}
							txtBed.writefileln(result.trim());
						}
					}
				}
				preContent = content;
			}
			txtBed.close();
			txtSam.close();
		} catch (Exception e) {
			logger.error("SamFile error:"+ samFile);
			e.printStackTrace();
		}
		BedSeq bedSeq = new BedSeq(bedFile);
		return bedSeq;
	}
	
	
	/**
	 * ��һ�е���Ϣ��ȡΪbed�ļ��ĸ�ʽ
	 * ����0��ʼ��Ĭ��mapping��ĿΪ1
	 * @return
	 */
	private String[] getBedFormat(String content)
	{
		String[] ss = content.split("\t");
		int[] flag = getFlag(Integer.parseInt(ss[1]));
		if (flag[2] == 1) {
			return null;
		}
		if (Integer.parseInt(ss[4]) < mapQuality) {
			return null;
		}
		//����
		String strand = "+";
		if (flag[4] == 1 ) {
			strand = "-";
		}
		//����
		int length = getMnum(ss[5]);
		if (length <= 0 ) {
			return null;
		}
		//���
		int start = Integer.parseInt(ss[3]) - 1;
		int end = start + length;
		String[] result = new String[7];
		result[0] = ss[2]; result[1] = start+""; result[2] = end+""; 
		try {
			result[3] = ss[18];
		} catch (Exception e) {
			result[3] = "none";
		}
		 result[4] = ss[5]; result[5] = strand;
		 result[6] = "1";
		return result;
	}
	
	/**
	 * ��unique mapping��ʱ��ʹ��
	 * ��һ�е���Ϣ��ȡΪbed�ļ��ĸ�ʽ
	 * ����0��ʼ
	 * @return
	 */
	private ArrayList<String[]> getBedFormatMulti(String content)
	{
		String[] ss = content.split("\t");
		int[] flag = getFlag(Integer.parseInt(ss[1]));
		if (flag[2] == 1) {
			return null;
		}
		String[] tagXA = content.split("XA:Z:");
		if (Integer.parseInt(ss[4]) < mapQuality && tagXA.length < 2) {
			return null;
		}
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		/////////////// װ �� �� һ �� mapping�ϵ� ///////////////////////////
		//����
		String strand = "+";
		if (flag[4] == 1 ) {
			strand = "-";
		}
		//����
		int length = getMnum(ss[5]);
		if (length <= 0 ) {
			return null;
		}
		//���
		int start = Integer.parseInt(ss[3]) - 1;
		int end = start + length;
		String[] result = new String[7];
		result[0] = ss[2]; result[1] = start+""; result[2] = end+""; 
		try {
			result[3] = ss[18];
		} catch (Exception e) {
			result[3] = "none";
		}
		 result[4] = ss[5]; result[5] = strand;
		 
		 lsResult.add(result);
		 
		 if (tagXA.length < 2) {
			result[6] = "1";
			return lsResult;
		}
		 
		 
		 String[] tmpInfo = tagXA[1].split("\t")[0].split(";");
		 result[6] = tmpInfo.length + 1 + "";
		 for (String string : tmpInfo) {
			String[] tmpResult = new String[7];
			String[] info = string.split(",");
			tmpResult[0] = info[0];
			int start1 = Integer.parseInt(info[1].substring(1)) -1;
			tmpResult[1] = start1 + "";
			tmpResult[2] =  start1 + length + "";
			try {
				tmpResult[3] = ss[18];
			} catch (Exception e) {
				tmpResult[3] = "none";
			}
			tmpResult[4] = info[2];
			tmpResult[5] = info[1].charAt(0)+"";
			tmpResult[6] = result[6];
			lsResult.add(tmpResult);
		}
		 if (lsResult.size() == 0) {
			 return null;
		}
		return lsResult;
	}
	
	
	/**
	 * ����M+N�ĸ�����˵�������еĳ���<br>
	 *
	 * M 0 alignment match (can be a sequence match or mismatch) <br>
 	 * I 1 insertion to the reference <br>
 	 * D 2 deletion from the reference<br>
	 * N 3 skipped region from the reference<br>
	 * S 4 soft clipping (clipped sequences present in SEQ) <br>
	 * H 5 hard clipping (clipped sequences NOT present in SEQ) <br>
	 * P 6 padding (silent deletion from padded reference) <br>
	 * = 7 sequence match <br>
	 * X 8 sequence mismatch<br>
	 * @param cigar
	 * @return
	 * û�г��ȵĻ�������-1
	 */
	private int getMnum(String cigar)
	{
		ArrayList<String> lsNum = patternlocation.getPat(cigar);
		int result = 0;
		if (lsNum == null) {
			return -1;
		}
		for (String string : lsNum) {
			result = result + Integer.parseInt(string);
		}
		
		return result;
	}
	
	/**
	 * ��flag�ֽ�Ϊ�����flag
	 * int[10]: <br>
	 * 0	0x1 template having multiple fragments in sequencing <br>
     * 1	0x2 each fragment properly aligned according to the aligner <br>
	 * 2	0x4 fragment unmapped <br>
	 * 3	0x8 next fragment in the template unmapped <br>
	 * 4	0x10 SEQ being reverse complemented <br>
	 * 5	0x20 SEQ of the next fragment in the template being reversed <br>
	 * 6	0x40 the first fragment in the template <br>
	 * 7	0x80 the last fragment in the template <br>
	 * 8	0x100 secondary alignment <br>
	 * 9	0x200 not passing quality controls <br>
	 * 10 PCR or optical duplicate<br>
	 * @param flag
	 * @return
	 */
	private static int[] getFlag(int flag) {
		String myflag = Integer.toBinaryString(flag);
		char[] tmpResult = myflag.toCharArray();
		int[] result = new int[10]; int m = 0; //��һ����������Ϊresult����
		for (int i = tmpResult.length-1; i >= 0; i --) {
			result[m] = tmpResult[i] - 48; m++;
		}
		return result;
	}
}
