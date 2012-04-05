package com.novelbio.analysis.seq.mapping;

import java.io.BufferedReader;
import java.util.ArrayList;

import javax.swing.text.rtf.RTFEditorKit;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.impl.piccolo.xml.Piccolo;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.SeqComb;
import com.novelbio.analysis.seq.chipseq.repeatMask.repeatRun;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.PatternOperate;

/**
 * 处理sam文件的工具
 * @author zong0jie
 *
 */
public class SAMtools extends SeqComb{
	private static final Logger logger = Logger.getLogger(SAMtools.class);

	String samFile = "";
	boolean paired = false;
	/**
	 * 序列质量小于10的就不要了
	 */
	private int mapQuality = 10;
	/**
	 * 提取SAM中的mapping信息用的
	 */
	private PatternOperate patternlocation = null;
	
	
	/**
	 * 设定单双端
	 * @param samFile sam文件
	 * @param paired 不知道的话就设定false
	 * @param mapQuality mapping质量，大于30说明不错。<=0则用默认值，为10
	 */
	public SAMtools(String samFile, boolean paired, int mapQuality) {
		super(samFile, 1);
		if (paired) {
			setBlock(2);
		}
		this.samFile = samFile;
		this.paired = paired;
		if (mapQuality > 0) {
			this.mapQuality = mapQuality;
		}
		
		patternlocation = new PatternOperate("\\d+(?=M|N)", true); 
		
	}
 
	
	private void setInfo(String samFile, boolean paired, int mapQuality) {
		this.samFile = samFile;
		this.paired = paired;
		if (mapQuality > 0) {
			this.mapQuality = mapQuality;
		}
		
		patternlocation = new PatternOperate("\\d+(?=M|N)", true); 
	}
	/**
	 * 
	 * 将sam文件改为bed文件，根据mapping质量和正反向进行筛选
	 * <b>不能挑选跨染色体的融合基因<b>
	 * @param bedFileCompType bed文件的压缩格式，TxtReadandWrite.TXT等设定
	 * @param bedFile 最后产生的bedFile
	 * @param uniqMapping 是否为uniqmapping
	 * 如果不是uniqmapping，那么mapping数量在第七列
	 * @return
	 */
	public BedSeq sam2bed(String bedFileCompType, String bedFile, boolean uniqMapping) {
		TxtReadandWrite txtSam = new TxtReadandWrite(samFile, false);
		
		TxtReadandWrite txtBed = new TxtReadandWrite(bedFileCompType, bedFile, true);

//		以下为同一行
//		HWUSI-EAS1734:0007:3:1:1430:16645:0     163     NC_009443       1523922 60      70M     =       
//		1524079 227     ACAGGTCAGAGATACATTTTGATAATCAATCATCATCTTCTCCTTTTAAAATAGCCATGAGACTCTGACT  
//		BD:==EDE?DFFBFFFFFEFFFBFAFFFBFFFDFFDEEFFFDDDFFDFFF=EEEECFF5FDEFDDFEB?5  XT:A:U 
//		NM:i:0  SM:i:37 AM:i:37 X0:i:1  X1:i:0  XM:i:0  XO:i:0  XG:i:0  MD:Z:70
		try {
			BufferedReader readerSam = txtSam.readfile();
			String content = ""; String preContent = null;//保存上一行
			String[] tmpResult = null;
			while ((content = readerSam.readLine()) != null) {
				if (content.trim().startsWith("@")) {
					continue;
				}
				//双端判断下两个是否都存在
				if (paired) {
					String[] preResult = tmpResult;
					tmpResult = getBedFormat(content);
					if (preContent == null || preResult == null || tmpResult == null) {
						preContent = content;
						continue;
					}
					//是否为同一个序列的单双端
					String[] splitPre = preContent.split("\t"); String[] splitTmp = content.split("\t");
					//不是同一个探针或不在同一个染色体上
					if (!splitPre[0].equals(splitTmp[0]) || !splitPre[2].equals(splitTmp[2])) {
						preContent = content;
						continue;
					}
					//solexa测序的时候，两条序列必须是一个正向一个反向，不是的说明不对
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
				//单端直接将结果写入文本
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
	
	boolean getSeqName = false;
	/**
	 * 是否在bed文件的最后一列加上seq的名字
	 * @param getSeqName
	 */
	public void setGetSeqName(boolean getSeqName) {
		this.getSeqName = getSeqName;
	}
	/**
	 * 将一行的信息提取为bed文件的格式
	 * 起点从0开始，默认mapping数目为1
	 * @return
	 * 0:chrID
	 * 1:start
	 * 2:end
	 * 3:Mismatching positions/bases
	 * 4:CIGAR  M 0 alignment match (can be a sequence match or mismatch)
I   1 insertion to the reference 
D 2 deletion from the reference
N 3 skipped region from the reference
S 4 soft clipping (clipped sequences present in SEQ)
H  5 hard clipping (clipped sequences NOT present in SEQ)
P  6 padding (silent deletion from padded reference)
=  7 sequence match 
X 8 sequence mismatch

5: strand
6: 

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
		//方向
		String strand = "+";
		if (flag[4] == 1 ) {
			strand = "-";
		}
		//长度
		int length = getMnum(ss[5]);
		if (length <= 0 ) {
			return null;
		}
		//起点
		int start = Integer.parseInt(ss[3]) - 1;
		int end = start + length;
		String[] result = null;
		if (getSeqName) {
			result = new String[8];
		}
		else {
			result = new String[7];
		}
		result[0] = ss[2]; result[1] = start+""; result[2] = end+""; 
		try {
			//Mismatching positions/bases
			result[3] = ss[18];
		} catch (Exception e) {
			result[3] = "none";
		}
		 result[4] = ss[5]; result[5] = strand;
		 result[6] = "1";
		 if (getSeqName) {
			result[7] = ss[0];
		}
		return result;
	}
	
	/**
	 * 非unique mapping的时候使用
	 * 将一行的信息提取为bed文件的格式
	 * 起点从0开始
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
		/////////////// 装 载 第 一 个 mapping上的 ///////////////////////////
		//方向
		String strand = "+";
		if (flag[4] == 1 ) {
			strand = "-";
		}
		//长度
		int length = getMnum(ss[5]);
		if (length <= 0 ) {
			return null;
		}
		//起点
		int start = Integer.parseInt(ss[3]) - 1;
		int end = start + length;
		String[] result = null;
		if (getSeqName) {
			result = new String[8];
		}
		else {
			result = new String[7];
		}
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
			if (getSeqName) {
				result[7] = ss[0];
			}
			return lsResult;
		}
		 
		 
		 String[] tmpInfo = tagXA[1].split("\t")[0].split(";");
		//这里修改的是已经添加入lsResult的result的信息
		 result[6] = tmpInfo.length + 1 + "";
		 if (getSeqName) {
			 result[7] = ss[0];
		 }
		 //添加新的信息
		 for (String string : tmpInfo) {
			String[] tmpResult = null;
			if (getSeqName) {
				tmpResult = new String[8];
			}
			else {
				tmpResult = new String[7];
			}
			
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
			 if (getSeqName) {
				 tmpResult[7] = ss[0];
			 }
			lsResult.add(tmpResult);
		}
		 if (lsResult.size() == 0) {
			 return null;
		}
		return lsResult;
	}
	
	
	/**
	 * 返回M+N的个数，说明该序列的长度<br>
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
	 * 没有长度的话，返回-1
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
	 * 将flag分解为具体的flag
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
		int[] result = new int[10]; int m = 0; //另一个计数器，为result计数
		for (int i = tmpResult.length-1; i >= 0; i --) {
			result[m] = tmpResult[i] - 48; m++;
		}
		return result;
	}
}
