package com.novelbio.analysis.seq.sam;

import java.util.ArrayList;
import java.util.Map;

import com.novelbio.analysis.seq.genome.mappingOperate.Alignment;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReadsAbs;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.dataStructure.MathComput;

/**
 * ����bam�ļ���MapReads���ʺ�С��Χ����ȡ��������
 * Ʃ����ȡtss��genebody�ȣ�
 * ���ʺ���ȡȫ���������Ϣ
 * ���ʺ���ȡRNA-Seq��ȫ������Ϣ
 * @author zong0jie
 *
 */
public class SamMapReads extends MapReadsAbs {
	public static void main(String[] args) {
		SamMapReads samMapReads = new SamMapReads(new SamFile("/media/winF/NBC/Project/Project_FY/paper/KOod.bam"));
		double[] info = samMapReads.getRangeInfo("chr4", 108013084, 108013173);
		for (int i = 0; i < info.length; i++) {
			if (i%10 == 0) {
				System.out.println();
			}
			System.out.print(info[i] + "\t");
		}
	}
	
	
	Map<String, Long> mapChrIDlowcase2Length;
	
	SamFile samFile;
	
	/** �����samFile�������������������� */
	public SamMapReads(SamFile samFile) {
		this.samFile = samFile;
		mapChrIDlowcase2Length = samFile.getChrID2LengthMap();
	}
	
	/**
	 * �趨Ⱦɫ�������볤�ȵĶ��ձ�ע��keyΪСд
	 * @param mapChrIDlowcase2Length
	 */
	public void setMapChrIDlowcase2Length(
			Map<String, Long> mapChrIDlowcase2Length) {
		this.mapChrIDlowcase2Length = mapChrIDlowcase2Length;
	}

	@Override
	protected long getAllReadsNum() {
		return allReadsNum;
	}

	@Override
	protected void ReadMapFileExp() throws Exception {
		//TODO ���Կ���ͨ����������bam�ļ���reads����
	}

	@Override
	public double[] getRangeInfo(int thisInvNum, String chrID, int startNum, int endNum, int type) {
		int[] startEndLoc = MapReadsAbs.correctStartEnd(mapChrIDlowcase2Length, chrID, startNum, endNum);
		if (startEndLoc == null) {
			return null;
		}
		if (thisInvNum <= 0) {
			thisInvNum = 1;
		}
		double binNum = (double)(startEndLoc[1] - startEndLoc[0] + 1) / thisInvNum;
		int binNumFinal = 0;
		if (binNum - (int)binNum >= 0.7) {
			binNumFinal = (int)binNum + 1;
		} else {
			binNumFinal = (int)binNum;
		}
		if (binNumFinal == 0) {
			binNumFinal = 1;
		}
		return getRangeInfo(chrID, startNum, endNum, binNumFinal, type);
	}

	@Override
	protected double[] getRangeInfo(String chrID, int startNum, int endNum, int binNum, int type) {
		double[] value = getRangeInfo(chrID, startNum, endNum);
		if (value == null) {
			return null;
		}
		double[] result = null;
		try {
			result = MathComput.mySpline(value, binNum, 0, 0, type);
		} catch (Exception e) {
			result = MathComput.mySpline(value, binNum, 0, 0, type);
		}
		normDouble(NormalType, result, allReadsNum);
		return result;
	}

	private double[] getRangeInfo(String chrID, int startNum, int endNum) {
		int[] startEnd = MapReadsAbs.correctStartEnd(mapChrIDlowcase2Length, chrID, startNum, endNum);
		if (startEnd == null) {
			return null;
		}
		double[] result = new double[startEnd[1] - startEnd[0] + 1];

		for (SamRecord samRecord : samFile.readLinesOverlap(chrID, startEnd[0], startEnd[1])) {
			try {
				addReadsInfo(samRecord, startEnd, result);
			} catch (Exception e) { }
		}
		
		return result;
	}
	/** ��samRecord����Ϣ����� result�� */
	private void addReadsInfo(SamRecord samRecord, int[] startEnd, double[] result) {
		if (booUniqueMapping && samRecord.getMappingNum() > 1) {
			return;
		}
		ArrayList<Align> lsAlign = samRecord.getAlignmentBlocks();
		for (Align align : lsAlign) {
			if (isInRegion(startEnd, align) == 1) {
				continue;
			} else if (isInRegion(startEnd, align) == 2) {
				break;
			}
			int[] startEndRegion = getStartEndLoc(startEnd, align);
			for (int i = startEndRegion[0]; i <= startEndRegion[1]; i++) {
				result[i] = result[i] + (double)1/samRecord.getMappingNum();
			}
		}
	}
	
	/**
	 * ��align�Ƿ���region��
	 * @param region ���䣬����  region[0] < region[1]
	 * @param align
	 * @return 0 inside
	 * 1 align before region
	 * 2 align after region
	 */
	private static int isInRegion(int[] region, Alignment align) {
		if (align.getEndAbs() < region[0]) {
			return 1;
		} else if (align.getStartAbs() > region[1]) {
			return 2;
		} else {
			return 0;
		}
	}
	
	/**
	 * <b>alignֻ��getStartAbs��getEndAbs</b><br>
	 * ����һ��alignment��ȷ��������� startEnd �����Χ���������
	 * ��0��ʼ����
	 * @param startEnd
	 * @param alignment ���alignmen������starend�ķ�Χ�����ͷ/β��ʼ���㡣Ʃ��С��0������Ϊ0
	 * @return ���ش�0��ʼ��������startLoc��endLoc����
	 * ����ֱ�ӵ����±�ʹ��
	 */
	private static int[] getStartEndLoc(int[] startEnd, Alignment align) {
		int startLoc = align.getStartAbs() - startEnd[0];
		int endLoc = align.getEndAbs() - startEnd[0];
		if (startLoc < 0) {
			startLoc = 0;
		}
		int length = startEnd[1] - startEnd[0];
		if (endLoc > length) {
			endLoc = length;
		}
		return new int[]{startLoc, endLoc};
	}
	
	/** 
	 * <b>alignֻ��getStartAbs��getEndAbs</b><br>
	 * ����һ��alignment��ȷ��������� startEnd �����Χ������ʹ�õ������յ�
	 * ��0��ʼ���㡣
	 * ���alignment��startEnd��overlap�ģ���ô��alignment�ĵڼ�λ��ʼ���𣬵��ڼ�λ����
	 * @param startEnd
	 * @param alignment
	 * @return ���ش�0��ʼ��������align�������յ�����
	 * ����ֱ�ӵ����±�ʹ��
	 */
	private static int[] getStartEndAlign(int[] startEnd, Alignment align) {
		int alignStart = 0;
		int alignEnd = align.Length() - 1;
		
		if (align.getStartAbs() < startEnd[0]) {
			alignStart = startEnd[0] - align.getStartAbs();
		}
		if (align.getEndAbs() > startEnd[1]) {
			alignEnd = startEnd[1] - align.getStartAbs();
		}
		return new int[]{alignStart, alignEnd};
	}

}
