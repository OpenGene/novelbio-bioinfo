package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.mapping.StrandSpecific;
import com.novelbio.listoperate.HistBin;
import com.novelbio.listoperate.HistList;

/** 将RNAseq的数据进行打乱处理 */
public class ShuffleReads {
	public static void main(String[] args) {
		ShuffleReads shuffleReads = new ShuffleReads();
		shuffleReads.setBlockSize(5);
		for (int i = 0; i < 100; i++) {
			String chrId = "chr" + (i/20 + 1);
			Align align = new Align(chrId, i%20, i%20+10);
			shuffleReads.addAlign(align, true, StrandSpecific.NONE);
		}
		shuffleReads.summary();
		
		for (Align align : shuffleReads.readlines()) {
			System.out.println(align.toStringNoStrand());
		}
	}
    Random rnd;    
	Map<String, int[]> mapChrId2ReadsNum = new HashMap<>();
	
	int blockSize = 200000;//每个单元的数量
	/** 每条染色体所对应的reads数量<br>
	 * long reads数量<br>
	 * 0: 总reads数量<br>
	 * 1: 当前读取到的reads数量
	 */
	HistList lsChrId2ReadsNum = HistList.creatHistList("", true);
	/** 总reads量，应该是mapChrId2ReadsNum中reads的总和 */
	int allReadsNum;
	/**
	 * 每条染色体所对应的reads<br>
	 * int[200000][2]<br>
	 * 200000 每个int单元<br>
	 * 0: start<br>
	 * 1: end<br>
	 * start < end 表示cis<br>
	 * start > end 表示trans
	 */
	Map<String, List<int[][]>> mapChrId2StartEnd = new HashMap<>();
	
	
	int[][] chrIdStartEndArray = new int[200000][3];
	
	/** 默认是200000一个单元 */
	public void setBlockSize(int blockSize) {
		this.blockSize = blockSize;
	}
	public int getAllReadsNum() {
		return allReadsNum;
	}
	/**
	 * 
	 * 添加单个align，可以是剪接位点，也可以是reads
	 * @param align 本align不考虑方向
	 * @param cis5to3Record samRecord的方向
	 * @param specific
	 */
	public void addAlign(Align align, boolean cis5to3Record, StrandSpecific specific) {
		boolean isCis = true;
		if( (specific == StrandSpecific.FIRST_READ_TRANSCRIPTION_STRAND && !cis5to3Record 
				|| specific == StrandSpecific.SECOND_READ_TRANSCRIPTION_STRAND && cis5to3Record) ) {
			isCis = false;
		}
		align.setCis5to3(isCis);
		
		int[] readsNum = mapChrId2ReadsNum.get(align.getRefID());
		if (readsNum == null) {
			readsNum = new int[1];
			mapChrId2ReadsNum.put(align.getRefID(), readsNum);
		}
		readsNum[0]++;
		List<int[][]> lsBlocks = mapChrId2StartEnd.get(align.getRefID());
		if (lsBlocks == null) {
			lsBlocks = new ArrayList<>();
			mapChrId2StartEnd.put(align.getRefID(), lsBlocks);
		}
		addLsBlocksNum(readsNum[0], align, lsBlocks);
	}
	
	/** 添加一条junction reads，考虑了链特异性 */
	private void addLsBlocksNum(int readsNum, Align align, List<int[][]> lsBlocks) {
		int[] readsIndex = getReadsIndex(blockSize, readsNum-1);
		int[][] num = null;
		if (readsIndex[1] == 0) {
			num = new int[blockSize][2];
			lsBlocks.add(num);
		} else {
			num = lsBlocks.get(lsBlocks.size() - 1);
		}
		num[readsIndex[1]] = new int[]{align.getStartCis(), align.getEndCis()};
	}
	
	/** 添加结束之后的总结 */
	public void summary() {
		shuffle();
		initialLsChrId2readsNum();		
	}
	
	/** 混排某一条染色体的reads */
    private void shuffle() {
    	for (String chrId : mapChrId2StartEnd.keySet()) {
    		List<int[][]> lsStartEnd = mapChrId2StartEnd.get(chrId);
    		int size = mapChrId2ReadsNum.get(chrId)[0];
    		rnd = new Random();
    		for (int i = size; i > 1; i--) {
    			swap(blockSize, lsStartEnd, i-1, rnd.nextInt(i));
    		}
		}
    }
	
    /**
     * 交换
     * @param blockSize
     * @param list
     * @param from 从0开始计数
     * @param to 从0开始计数
     */
    private static void swap(int blockSize, List<int[][]> list, int from, int to) {
    	int[] formIndex = getReadsIndex(blockSize, from);
    	int[] toIndex = getReadsIndex(blockSize, to);
    	int[] tmpInfo = list.get(toIndex[0])[toIndex[1]] ;
    	list.get(toIndex[0])[toIndex[1]] = list.get(formIndex[0])[formIndex[1]];
    	list.get(formIndex[0])[formIndex[1]] = tmpInfo;
    }
	
    /** 给定值，返回该值所在list的block数，以及block的index
     * 
     * @param blocSize
     * @param num 从0开始计数
     * @return int[2]
     * 0: blockNum 从0开始计数
     * 1: resideNum 从0开始计数
     */
    private static int[] getReadsIndex(int blockSize, int num) {
    	int blockNum = (num)/blockSize;
    	int resideNum = num%blockSize;
    	return new int[]{blockNum, resideNum};
    }
    
    /** 初始化随机查找染色体的类 */
    private void initialLsChrId2readsNum() {
    	allReadsNum = 0;
    	int i = 0;
    	for (String chrId : mapChrId2ReadsNum.keySet()) {
    		allReadsNum += mapChrId2ReadsNum.get(chrId)[0];
			if (i == 0) {
				lsChrId2ReadsNum.setStartBin(0, chrId, 0, allReadsNum);
			} else {
				lsChrId2ReadsNum.addHistBin(0, chrId, allReadsNum);
			}
			i++;
		}
    }


	public Iterable<Align> readlines() {
		final int[] readNum = new int[]{1};
		final int[] allReadsNumTmp = new int[]{allReadsNum};
		return new Iterable<Align>() {
			public Iterator<Align> iterator() {
				return new Iterator<Align>() {
					Align align = getChrIdRandom();
				    /** 按照比例随机获得chrId */
				    private Align getChrIdRandom() {
				    	Align align = null;
				    	do {
				    		if (readNum[0] > allReadsNumTmp[0]) {
				    			break;
							}
				    		double num = rnd.nextDouble();
				        	int chrNum = (int) (num*allReadsNumTmp[0]);
				        	HistBin histBin = lsChrId2ReadsNum.searchHistBin(chrNum);
				        	if (histBin.getCountNumber() < histBin.getLength()) {
				    			String chrId = histBin.getNameSingle();
				    			align = getAlign(chrId, (int) histBin.getCountNumber());
				    			
				    			histBin.addNumber();
								readNum[0]++;
				    		} else {
				    			allReadsNumTmp[0] = removeChrId(histBin.getNameSingle());
							}
						} while (align == null);
				    	return align;
				    }
					@Override
					public boolean hasNext() {
						return align != null;
					}
					@Override
					public Align next() {
						Align retval = align;
						align = getChrIdRandom();
						return retval;
					}
					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
		
	}
	
	/** 返回减去该chrId后剩下的reads总数 */
	private int removeChrId(String chrId) {
		int i = 0;
		for (HistBin histBin : lsChrId2ReadsNum) {
			if (histBin.getNameSingle().equals(chrId)) {
				lsChrId2ReadsNum.remove(i);
				break;
			}
			i++;
		}
		HistList lsChrId2ReadsNumTmp = HistList.creatHistList("", true);
		int allReads = 0;
		for (int j = 0; j < lsChrId2ReadsNum.size(); j++) {
			HistBin histBin = lsChrId2ReadsNum.get(j);
			allReads = allReads +  (int)(histBin.getEndAbs() - histBin.getStartAbs() - histBin.getCountNumber());
			if (j == 0) {
				lsChrId2ReadsNumTmp.setStartBin(0, histBin.getNameSingle(), 0, allReads);
			} else {
				lsChrId2ReadsNumTmp.addHistBin(0, histBin.getNameSingle(), allReads);
			}
		}
		lsChrId2ReadsNum = lsChrId2ReadsNumTmp;
		return allReads;
	}
	
	
    /** 
     * 从0开始计数
     * @param chrId
     * @param index
     * @return
     */
    private Align getAlign(String chrId, int index) {
		List<int[][]> lsAlign = mapChrId2StartEnd.get(chrId);
		int[] indexNum = getReadsIndex(blockSize, index);
		int[] startEnd = null;
		startEnd = lsAlign.get(indexNum[0])[indexNum[1]];
		
		Align align = new Align(chrId, startEnd[0], startEnd[1]);
		return align;
	}
    

}
