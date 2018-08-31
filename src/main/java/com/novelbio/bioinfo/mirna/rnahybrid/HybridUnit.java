package com.novelbio.bioinfo.mirna.rnahybrid;

import com.novelbio.base.SepSign;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.bioinfo.fasta.SeqFasta;

/** 具体的一对pair */
public abstract class HybridUnit {
	String qName;
	String sName;
	int alignLen;
	int startQ;
	int endQ;
	int startS;
	int endS;
	
	double energy;
	
	/** 小写表示前缀或后缀，都不计入start和end中
	 * 是反向序列，因为方向是从3‘-5’的
	 */
	String qSeq;
	/** 小写表示前缀或后缀，都不计入start和end中 */
	String sSeq;
	/** 连配的竖线 */
	String align;
	/** 连配中开头的空格 */
	int startSpaceNum;

	protected String getKey() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(qName);
		stringBuilder.append(SepSign.SEP_ID);
		stringBuilder.append(sName);
		stringBuilder.append(SepSign.SEP_ID);
		stringBuilder.append(startQ);
		stringBuilder.append(SepSign.SEP_ID);
		stringBuilder.append(endQ);
		stringBuilder.append(SepSign.SEP_ID);
		stringBuilder.append(startS);
		stringBuilder.append(SepSign.SEP_ID);
		stringBuilder.append(endS);
		return stringBuilder.toString();
	}
	
	/** 返回连配的结果*/
	public String getAlign() {
		int lenQname = qName.length();
		int lenSname = sName.length();
		//名字长度不同用空格补齐
		int QminusS = lenQname - lenSname;
		String space = getSpace(QminusS);
		//设定名字
		String qNameDesply;
		String sNameDesply;
		if (QminusS > 0) {
			qNameDesply = qName + ":";
			sNameDesply = sName + new String(space) + ":";
		} else {
			qNameDesply = qName + new String(space) + ":";
			sNameDesply = sName + ":";
		}
		//设定alignment
		int alingSpaceNum = startSpaceNum + Math.max(lenQname, lenSname) + 1;
		String alignDesply = getSpace(alingSpaceNum) + align;
		return qNameDesply + qSeq + TxtReadandWrite.ENTER_LINUX + alignDesply + TxtReadandWrite.ENTER_LINUX + sNameDesply + sSeq;
	}
	
	/**
	 * 开头0-5个碱基开始，是否和mRNA有6个以上的配对
	 * @return
	 */
	public boolean isSeedPerfectMatch() {
		int startMaxNum = 5;//最多开头5个碱基
		int seedLen = 6;
		String mirSeq = SeqFasta.reverse(qSeq);
		//seq开头有几个-
		int barNum = 0;
		for (char chr : mirSeq.toCharArray()) {
			if (chr == '-') {
				barNum++;
			} else {
				break;
			}
		}
		int startNum = startSpaceNum - barNum;
		String alignSeq = SeqFasta.reverse(align);
		int matchNum = 0;
		for (char bar : alignSeq.toCharArray()) {
			if (bar == '|') {
				matchNum++;
			} else {
				if (startNum <=startMaxNum && matchNum < seedLen) {
					matchNum = 0;
					continue;
				} else if (matchNum >= seedLen) {
					break;
				} else if (startNum > startMaxNum) {
					break;
				}
			}
			startNum++;
		}
		if (matchNum >= seedLen) {
			return true;
		}
		return false;
	}
	
	/**
	 * 返回若干空格
	 * @param spaceNum 无所谓正负号，会返回该绝对值个数的空格<br>
	 * 譬如 3 和 -3 都返回 3个空格
	 * @return
	 */
	private String getSpace(int spaceNum) {
		if (spaceNum == 0) {
			return "";
		}
		char[] space = new char[Math.abs(spaceNum)];
		for (int i = 0; i < space.length; i++) {
			space[i] = ' ';
		}
		return new String(space);
	}

	/** 整理清楚的结果 */
	public abstract String toResultTab();
	/** 获得title */
	public abstract String getTitle();
}
