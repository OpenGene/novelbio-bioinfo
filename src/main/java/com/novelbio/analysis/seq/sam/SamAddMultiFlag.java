package com.novelbio.analysis.seq.sam;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.log4j.Logger;

/** sam文件添加非unique mapping的标签，待添加的文件不能被排序 */
public class SamAddMultiFlag {
	private static final Logger logger = Logger.getLogger(SamAddMultiFlag.class);
	
	int capacity = 50000;
	/** 读完的reads放在这里 */
	ArrayBlockingQueue<SamRecord> queueSamRecords = new ArrayBlockingQueue<>(capacity);
	boolean isFinished = false;
	
	boolean isPairend;
	Set<String> setTmp = new HashSet<>();
	/** 相同名字的序列 */
	Map<String, List<SamRecord>> mapMateInfo2pairReads = new LinkedHashMap<>();
	long i = 0;
	
	String lastSeqName = "";
	
	/** 是否为双端 */
	public void setPairend(boolean isPairend) {
		this.isPairend = isPairend;
	}
	
	public void addSamRecord(SamRecord samRecord) {
		try {
			if (!samRecord.isMapped()) {
				logger.debug("unmapped");
			}
			if (i++%1000000 == 0) {
				logger.info("read lines: " + i);
				logger.info("mapMateInfo2pairReads.size: " + mapMateInfo2pairReads.size());
			}
			if ((!isPairend) || (isPairend && !lastSeqName.equals(samRecord.getName()))
					) {
				String samName = samRecord.getName();
				if (!setTmp.contains(samName)) {
					addMapSamRecord(mapMateInfo2pairReads);
					setTmp.clear();
					setTmp.add(samName);
					mapMateInfo2pairReads.clear();
				}
				setTmp.add(samRecord.getName());
			}
			addSamRecordToMap(isPairend, samRecord, mapMateInfo2pairReads);
			lastSeqName = samRecord.getName();
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	/**
	 * @param lsSamRecords
	 * @param mapHitNum 小于0表示不需要调整flag
	 */
	private void addMapSamRecord(Map<String, List<SamRecord>> mapMateInfo2pairReads) {
		int multiHitNum = mapMateInfo2pairReads.size();
		Set<SamRecord> setSamRecordsSingle = new HashSet<>();
		
		int numSingle = 0;
		for (List<SamRecord> lsSamRecords : mapMateInfo2pairReads.values()) {
			if (lsSamRecords.size() < 2) {
				numSingle ++;
				setSamRecordsSingle.addAll(lsSamRecords);
				continue;
			}
			if (lsSamRecords.size() == 2) {
				continue;
			}
			
			for (int j = 2; j < lsSamRecords.size(); j++) {
				setSamRecordsSingle.add(lsSamRecords.get(j));
			}
		}
		multiHitNum = mapMateInfo2pairReads.size() - numSingle + setSamRecordsSingle.size();
		int i = 1;
		for (List<SamRecord> lsSamRecords : mapMateInfo2pairReads.values()) {
			if (lsSamRecords.size() < 2) continue;
			
			lsSamRecords.get(0).setMultiHitNum(multiHitNum);
			lsSamRecords.get(0).setMapIndexNum(i);
			lsSamRecords.get(1).setMultiHitNum(multiHitNum);
			lsSamRecords.get(1).setMapIndexNum(i);
			
			i++;

			while (queueSamRecords.remainingCapacity() < capacity/200) {
				try {
					Thread.sleep(200);
					logger.debug(queueSamRecords.size());
				} catch (Exception e) {
					// TODO: handle exception
				}	
			}
			queueSamRecords.add(lsSamRecords.get(0));
			queueSamRecords.add(lsSamRecords.get(1));
			
		}
		for (SamRecord samRecord : setSamRecordsSingle) {
			samRecord.setMultiHitNum(multiHitNum);
			samRecord.setMapIndexNum(i);
			queueSamRecords.add(samRecord);
			i++;
		}
	}
	
	private void addSamRecordToMap(boolean isPairend, SamRecord samRecord, 
			Map<String, List<SamRecord>> mapMateInfo2pairReads) {
		String pairInfo = getFirstNameAndSite(samRecord);
		if (isPairend) {
			//首先看第一端是否出现，出现了就获取第一端，然后放到第二端
			if (mapMateInfo2pairReads.containsKey(pairInfo)) {
				 List<SamRecord> lsRecords = mapMateInfo2pairReads.get(pairInfo);
				if (lsRecords.size() > 1) {
					SamRecord mate = findCloseSamRecord(lsRecords.get(0), lsRecords.get(1), samRecord);
					if (mate != null) {
						lsRecords.set(1, mate);
						//将多的全部清掉
						if (lsRecords.size() > 2) {
							SamRecord[] samRecords = new SamRecord[]{lsRecords.get(0), lsRecords.get(1)};
							lsRecords.clear();
							for (SamRecord samRecord2 : samRecords) {
								lsRecords.add(samRecord2);
							}
						}
					} else {
						lsRecords.add(samRecord);
					}
				} else {
					lsRecords.add(samRecord);
				}
			} else {
				addNewRecordInMap(samRecord, mapMateInfo2pairReads);
			}
		} else {
			addNewRecordInMap(samRecord, mapMateInfo2pairReads);
		}
	}
	
	private SamRecord findCloseSamRecord(SamRecord record1, SamRecord record2_1, SamRecord record2_2) {
		if (!record1.isMapped()) {
			return null;
		}
		if (record2_1.isMapped() && !record2_2.isMapped()) {
			return record2_1;
		} else if (!record2_1.isMapped() && record2_2.isMapped()) {
			return record2_2;
		}
		//两个都比上了
		if (record1.getRefID().equals(record2_1.getRefID()) ) {
			if (record1.getRefID().equals(record2_2.getRefID())) {
				int start1 = record1.getStartAbs(), end1 = record1.getEndAbs();
				int start2 = record2_1.getStartAbs(), end2 = record2_1.getEndAbs();
				int start3 = record2_2.getStartAbs(), end3 = record2_2.getEndAbs();
				int distance1 = (start1 < start2)? start2 - end1 : start1 - end2;
				int distance2 = (start1 < start3)? start3 - end1 : start1 - end3;
				return (distance1 < distance2) ? record2_1 : record2_2;
			} else {
				return record2_1;
			}
		} else {
			if (record1.getRefID().equals(record2_2.getRefID())) {
				return record2_2;
			} else {
				return null;
			}
		}
	}
	
	private void addNewRecordInMap(SamRecord samRecord, Map<String, List<SamRecord>> mapMateInfo2pairReads) {
		List<SamRecord> lsRecords = new ArrayList<SamRecord>();
		lsRecords.add(samRecord);
		String pairMateInfo = getFirstNameAndSite(samRecord);
		mapMateInfo2pairReads.put(pairMateInfo, lsRecords);
	}
	
	/**
	 * 如果是单端，则直接返回名字和比对位点
	 * 如果是双端，则返回第一条比对上的名字和位点
	 * @param samRecord
	 * @return
	 */
	private String getFirstNameAndSite(SamRecord samRecord) {
		if (!isPairend) {
			return samRecord.getName() + samRecord.getRefID() + samRecord.getStartAbs();
		} else {
			String cis = samRecord.getName() + samRecord.getRefID() + samRecord.getStartAbs();
			String trans = samRecord.getName() + samRecord.getMateRefID() + samRecord.getMateAlignmentStart();
			if (samRecord.isMapped() && samRecord.isMateMapped()) {
				return samRecord.isFirstRead()? cis : trans;
			} else if (samRecord.isMapped() && !samRecord.isMateMapped()) {
				return cis;
			} else if (!samRecord.isMapped() && samRecord.isMateMapped()) {
				return trans;
			} else if (!samRecord.isMapped() && !samRecord.isMateMapped()) {
				return cis;
			}
			return null;
		}
	}
	
	/** 完成添加tags的过程
	 * 在结束{@link #addSamRecord(SamRecord)} 操作后，调用该方法结束任务
	 */
	public void finish() {
		addMapSamRecord(mapMateInfo2pairReads);
		isFinished = true;
	}
	
	/**
	 * 从第几行开始读，是实际行
	 * @param lines 如果lines小于等于1，则从头开始读取
	 * @return
	 */
	public Iterable<SamRecord> readlines() {
		return new Iterable<SamRecord>() {
			public Iterator<SamRecord> iterator() {
				//在最后输出日志时使用
				final boolean[] flag = new boolean[]{false};
				
				return new Iterator<SamRecord>() {
					public boolean hasNext() {
						return line != null;
					}
					public SamRecord next() {
						SamRecord retval = line;
						line = getLine();
						return retval;
					}
					public void remove() {
						throw new UnsupportedOperationException();
					}
					SamRecord getLine() {
						SamRecord record = null;
						//如果从队列中拿不到record
						//此时如果isFinished为false，表示queueSamRecords还有可能会加入新的record
						//因此等待50ms，再从queueSamRecords中获取record
						//如果isFinished为true，则当queueSamRecords为空，就返回null然后结束提取序列
						do {
							record = queueSamRecords.poll();
							if (record != null) {
								break;
							}
							try {
								Thread.sleep(50);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						} while (!isFinished || !queueSamRecords.isEmpty());
						
						if (isFinished && !flag[0]) {
							flag[0] = true;
							logger.info("finish reading and queue still have " + queueSamRecords.size() + " reads");
						}
						return record;
					}
					SamRecord line = getLine();
				};
			}
		};
	
	}
}
