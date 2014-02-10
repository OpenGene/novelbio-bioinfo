package com.novelbio.analysis.seq.sam.pileup;

import java.util.LinkedList;
import java.util.List;

import net.sf.samtools.Cigar;
import net.sf.samtools.CigarElement;
import net.sf.samtools.CigarOperator;

import com.novelbio.analysis.seq.resequencing.SiteSnpIndelInfo.SnpIndelType;
import com.novelbio.analysis.seq.resequencing.SiteSnpIndelInfoFactory;
import com.novelbio.analysis.seq.sam.SamErrorException;
import com.novelbio.analysis.seq.sam.SamRecord;
import com.novelbio.base.dataStructure.ArrayOperate;


/**
 * pileup的一个单元
 * @author zomg0jie
 */
public class SamPileupUnit {
	int startRegion;
	int endRegion;
	String refseq;
	LinkedList<SamRecord> lsSamRecord = new LinkedList<>();
	
	/**
	 * 给定指定的位点，获得其pileup信息
	 * @param site
	 */
	public void getSite(int site) {
		checkRegion(site);
	}
	
	private void checkRegion(int site) {
		if (site < startRegion || site > endRegion) {
			throw new SamErrorException("site out of region:" + site + " " + startRegion + " " + endRegion);
		}
	}
	
	public void getBaseInfo() {
		
	}
	
}

/** 一个堆叠中，某个具体碱基的信息 */
class BaseInfo {
	char base;
	int quality;
	
	/** 是否为samrecord的第一个碱基 */
	boolean isStart;
	/** 是否为samrecord的最后一个碱基 */
	boolean isEnd;
	
	/** 如果是插入或缺失，则后面还会有相应的碱基，如+AT，-GGC等 */
	char[] baseFollow;
	/** 如果是插入，则会有插入序列的质量 */
	int[] qualityFollow;
	
	/** 这个位点的属性 */
	BaseType baseType;
	/** 在第几个位置 */
	int site;
	/** 所属的记录 */
	SamRecord samRecord;
	
	/**
	 * @param samRecord
	 * @param refSeq 除非含有deletion，否则不需要refseq
	 * 考虑弄成chr[]的形式
	 * @param site
	 */
	public BaseInfo(SamRecord samRecord, String refSeq, int site) {
		Cigar cigar = samRecord.getCigar();
		int start = samRecord.getStartAbs();
		if (site == start) {
			isStart = true;
		} else if (site == samRecord.getEndAbs()) {
			isEnd = true;
		}
		
		int baseNum = 0;
		//getCigarElements返回的是unmodified arraylist实现
		List<CigarElement> lsCigarElementrs = cigar.getCigarElements();
		for (int i = 0; i < lsCigarElementrs.size(); i++) {
			CigarElement cigarElement = lsCigarElementrs.get(i);
			CigarOperator cigarOperator = cigarElement.getOperator();
			//还没到所在的cigar
			if (start + cigarElement.getLength() < site || cigarOperator == CigarOperator.I) {
				if (cigarOperator != CigarOperator.I) start += cigarElement.getLength();
				
				if (cigarOperator != CigarOperator.D && cigarOperator != CigarOperator.N) {
					baseNum =+ cigarElement.getLength();
				}
				continue;
			}
			base = (char)samRecord.getReadBase()[baseNum + site - start];
			quality = (int)samRecord.getBaseQualities()[baseNum + site - start];
			baseType = BaseType.match;
			if (cigarOperator == CigarOperator.N) {
				if (samRecord.isCis5to3()) {
					base = '>';//TODO 需要确认
				} else {
					base = '<';//TODO 需要确认
				}
				
			} else if (cigarOperator == CigarOperator.D) {
				base = '*';
			}
			//如果该位点正好在一个indel的前面
			if (site - start == cigarElement.getLength() && i < lsCigarElementrs.size() - 1) {
				CigarElement cigarElementNext = lsCigarElementrs.get(i + 1);
				CigarOperator cigarOperatorNext = cigarElementNext.getOperator();
				if (cigarOperatorNext == CigarOperator.D) {
					baseType = BaseType.deletion;
					//TODO，看是否可以优化
					baseFollow = refSeq.substring(site - start, cigarElementNext.getLength()).toCharArray();
				} else if (cigarOperatorNext == CigarOperator.I) {
					baseType = BaseType.insert;
					baseFollow = subBase(samRecord, site + 1, cigarElementNext.getLength());
					qualityFollow = subQuality(samRecord, site + 1, cigarElementNext.getLength());
				}
			}
			break;
		}
	}
	
	private int[] subQuality(SamRecord samRecord, int start, int length) {
		int[] subQ = new int[length];
		for (int i = start; i < length + start; i++) {
			subQ[i - start] = (int)samRecord.getBaseQualities()[i];
		}
		return subQ;
	}
	
	private char[] subBase(SamRecord samRecord, int start, int length) {
		char[] subQ = new char[length];
		for (int i = start; i < length + start; i++) {
			subQ[i - start] = (char)samRecord.getReadBase()[i];
		}
		return subQ;
	}
	
}

enum BaseType {
	/** 比对上，不管是match还是mismatch */
	match,
	/** 插入 */
	insert,
	/** 缺失 */
	deletion
}