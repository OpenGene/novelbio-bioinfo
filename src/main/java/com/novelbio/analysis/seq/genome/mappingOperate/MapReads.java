package com.novelbio.analysis.seq.genome.mappingOperate;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReadsAbs.MapReadsProcessInfo;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.dataStructure.listOperate.ListAbs;
import com.novelbio.database.model.species.Species;
/**
 * �����mapping����Ѿ�����ã�����Ⱦɫ���Ѿ��ֿ��á�
 * �������ڴ����Ƶı�
 * 
 * @author zong0jie
 * 
 */
public class MapReads extends MapReadsAbs{
	private static Logger logger = Logger.getLogger(MapReads.class);

	 boolean uniqReads = false;
	 int startCod = -1;
	 boolean booUniqueMapping = true;
	 /** ��ѡȡĳ�������reads */
	 Boolean FilteredStrand = null;
	 Species species;
	 
	 long readsSize = 0;
	 
	 long allReadsNum = 0;
	 
	 /** ����������������reads */
	 MapReadsAddAlignRecord mapReadsAddAlignRecord;
	 
	 public void setSpecies(Species species) {
		mapChrID2Len = species.getMapChromInfo();
	}
	
	 /** ��ʱ����Ҫ�ò���������һ��������reads��������׼��
	  * <b>�ڶ�ȡ�������趨</b>
	  * @param allReadsNum
	  */
	 public void setAllReadsNum(long allReadsNum) {
		this.allReadsNum = allReadsNum;
	}
	 /** �ܹ��ж���reads������mapping�������ReadMapFile���ܵõ��� */
	public long getAllReadsNum() {
		if (allReadsNum > 0) {
			return allReadsNum;
		}
		for (ChrMapReadsInfo chrMapReadsInfo : mapChrID2ReadsInfo.values()) {
			allReadsNum = allReadsNum + chrMapReadsInfo.getReadsChrNum();
		}
		return allReadsNum;
	}
	
	/**
	 * @param uniqReads ��reads mapping��ͬһ��λ��ʱ���Ƿ������һ��reads
	 * @param startCod ����㿪ʼ��ȡ��reads�ļ���bp�������õ� С��0��ʾȫ����ȡ ����reads���ȵ�����Ըò���
	 * @param colUnique  Unique��reads����һ�� novelbio�ı���ڵ����У���1��ʼ����
	 * @param booUniqueMapping �ظ���reads�Ƿ�ֻѡ��һ��
	 * @param FilteredStrand �Ƿ��ѡȡĳһ�����reads��null������
	 */
	public void setFilter(boolean uniqReads, int startCod, boolean booUniqueMapping, Boolean FilteredStrand) {
		this.uniqReads = uniqReads;
		this.startCod = startCod;
		this.booUniqueMapping = booUniqueMapping;
		this.FilteredStrand = FilteredStrand;
	}
	/**
	 * ������õ���ʵ��ĳ��Ⱦɫ����������reads��Ŀ
	 */
	public long getChrReadsNum(String chrID) {
		return mapChrID2ReadsInfo.get(chrID.toLowerCase()).getReadsChrNum();
	}
	/**
	 * ������õ���ʵ��ĳ��Ⱦɫ��ĸ߶��ܺ�
	 */
	public long getChrReadsPipNum(String chrID) {
		return mapChrID2ReadsInfo.get(chrID.toLowerCase()).getReadsPipNum();
	}
	/**
	 * ������õ���ʵ��ĳ��Ⱦɫ��߶ȵ�ƽ��ֵ
	 */
	public double getChrReadsPipMean(String chrID) {
		ChrMapReadsInfo chrMapReadsInfo = mapChrID2ReadsInfo.get(chrID.toLowerCase());
		return chrMapReadsInfo.getReadsPipNum()/chrMapReadsInfo.chrLength;
	}
	
	protected boolean isUniqueMapping() {
		return booUniqueMapping;
	}
	 /**
	  * �趨��׼��������������ʱ�趨����һ��Ҫ�ڶ�ȡ�ļ�ǰ
	  * Ĭ����NORMALIZATION_ALL_READS
	  * @param normalType
	  */
	 public void setNormalType(int normalType) {
		NormalType = normalType;
	}
	 
	private void setChrLenFromReadBed() {
		if (mapChrID2Len.size() > 0)
			return;
		
		String chrID = ""; AlignRecord lastAlignRecord = null;
		for (AlignRecord alignRecord : alignSeqReader.readLines()) {
			if (!alignRecord.getRefID().equals(chrID)) {
				if (lastAlignRecord != null) {
					mapChrID2Len.put(chrID.toLowerCase(), (long)lastAlignRecord.getEndAbs());
				}
				chrID = alignRecord.getRefID();
			}
			lastAlignRecord = alignRecord;
		}
		mapChrID2Len.put(lastAlignRecord.getRefID().toLowerCase(), (long)lastAlignRecord.getEndAbs());
	}
	/**
	 * ������׼������equations����
	 * ����Ⱦɫ�壬�������յ㣬���ظ�Ⱦɫ����tag���ܶȷֲ��������Ⱦɫ����mappingʱ�򲻴��ڣ��򷵻�null
	 * @param chrID Сд
	 * @param startLoc ������꣬Ϊʵ����� ���startNum<=0 ����endNum<=0���򷵻�ȫ����Ϣ
	 * @param endLoc 
	 * @param binNum ���ָ�Ŀ���
	 * @return
	 */
	public  double[] getReadsDensity(String chrID, int startLoc, int endLoc, int binNum ) {
		int tagBinLength=(int)(tagLength*1.5);
		double[] tmpReadsNum = getRangeInfo(tagBinLength, chrID, startLoc, endLoc,1);
		if (tmpReadsNum==null) {
			return null;
		}
		double[] resultTagDensityNum=MathComput.mySpline(tmpReadsNum, binNum, 0, 0, 2);
		return resultTagDensityNum;
	}
	
	/**
	 * ������Ҫ���������װ��ArrayList-ExonInfo���棬���ؽ���������Щ����Ļ�����ֲ��ܶ�ͼ<br>
	 * ������Ǳߵ�����ǿ����ġ�������뿴ȫ��������tss����ļ׻����ֲ������exon����ļ׻����ֲ������<br>
	 * ����˼·����һ�����ȵ�slide window����������Ȼ�󿴸�λ�����м׻����Ļ���ı�������<br>
	 * ��ô�ҵ��������ǳ���tss������������ļ׻���ȫ���趨Ϊ0��Ҳ���ǽ�����ָ��lsExonInfos�ڵļ׻�����Ȼ������߳��沽��<br>
	 * @param lsExonInfos
	 * @param chrID
	 * @param startLoc
	 * @param endLoc
	 * @param binNum
	 * @return
	 */
	public  double[] getReadsDensity(ListAbs<ExonInfo> lsExonInfos, String chrID, int startLoc, int endLoc, int binNum ) {
		//���Ƚ�reads��׼��Ϊһ��400-500bp��Ĵ�飬ÿһ������Ӧ���Ǹ���������tags���������������������������ֵ
		//Ȼ�����ڴ������ͳ�ƣ�
		//��Ź�����һ�£������Ͽ����һ��tag��1.5����ʱ�������ȽϺ���
		int tagBinLength=(int)(tagLength*1.5);
		double[] tmpReadsNum = getRangeInfo(tagBinLength, chrID, startLoc, endLoc,1);
		if (tmpReadsNum==null) {
			return null;
		}
		double[] resultTagDensityNum=MathComput.mySpline(tmpReadsNum, binNum, 0, 0, 2);
		return resultTagDensityNum;
	}
	
	/**
	 * ������Ϊmacs��bed�ļ�ʱ���Զ�<b>����chrm��Ŀ</b><br>
	 * ����chr��Ŀ��Сд
	 * ��ȡMapping�ļ���������Ӧ��һά�������飬��󱣴���һ����ϣ���С�ע�⣬mapping�ļ��е�chrID��chrLengthFile�е�chrIDҪһ�£���������
	 * @throws Exception
	 */
	protected void ReadMapFileExp() throws Exception {
		allReadsNum = 0;
		setChrLenFromReadBed();
		AlignRecord alignRecordFirst = alignSeqReader.readFirstLine();
		if (!prepareAlignRecord(alignRecordFirst)) {
			return;
		}
		
		for (AlignRecord alignRecord : alignSeqReader.readLines()) {
			allReadsNum++;
			mapReadsAddAlignRecord.addAlignRecord(alignRecord);
			
			suspendCheck();
			if (flagStop) {
				break;
			}
			readsSize = readsSize + alignRecord.getRawStringInfo().getBytes().length;
			if (allReadsNum%1000 == 0) {
				MapReadsProcessInfo mapReadsProcessInfo = new MapReadsProcessInfo(readsSize);
				setRunInfo(mapReadsProcessInfo);
			}
		}
		mapReadsAddAlignRecord.summary();
	}
	
	/**
	 * ׼�����reads��Ϣ����Ҫ�ǳ�ʼ��mapReadsAddAlignRecord
	 * ��������ж�һ��startCod�Ƿ�����
	 * @param alignRecordFirst
	 * @return �����setFilter�ж��� startCod > 0 ����readsû�з���
	 * �򷵻�false
	 */
	public boolean prepareAlignRecord(AlignRecord alignRecordFirst) {
		mapReadsAddAlignRecord = new MapReadsAddAlignRecord(this);
		if (startCod > 0 && alignRecordFirst.isCis5to3() == null) {
			logger.error("�����趨startCod����Ϊû���趨������");
			return false;
		}
		return true;
	}
	
	/**���samBam���ļ����������Ϣ
	 * ע�������֮ǰҪ��ִ��{@link #prepareAlignRecord(AlignRecord)}
	 */
	public void addAlignRecord(AlignRecord alignRecord) {
		mapReadsAddAlignRecord.addAlignRecord(alignRecord);
	}
}

/**
 * ��������þ��ǽ�reads����Ϣװ��MapReads��mapChrID2ReadsInfo��ȥ��
 * �����ģ������������Է���һ�ζ�ȡbam�ļ���Ȼ�����ö�����
 * @author zong0jie
 *
 */
class MapReadsAddAlignRecord {
	private static final Logger logger = Logger.getLogger(MapReadsAddAlignRecord.class);
	MapReads mapReads;
	int[] chrBpReads = null;//����ÿ��bp��reads�ۼ���
	String lastChr="";
	boolean flag = true;// ��û�и�Ⱦɫ��ʱ���Ϊfalse�����������и�Ⱦɫ���ϵ�����
	ChrMapReadsInfo chrMapReadsInfo = null;
	int[] tmpOld = new int[2];//���� tmpOld
	
	public MapReadsAddAlignRecord(MapReads mapReads) {
		this.mapReads = mapReads;
	}
	
	public void addAlignRecord(AlignRecord alignRecord) {
		String tmpChrID = alignRecord.getRefID().toLowerCase();
		if (!tmpChrID.equals(lastChr)) {
			tmpOld = new int[2];//���� tmpOld
			summary();
			lastChr = tmpChrID;// ʵ�������³��ֵ�ChrID
			logger.error(lastChr);
			
			Long chrLength = mapReads.mapChrID2Len.get(lastChr.toLowerCase());
			flag = true;
			if (chrLength == null) {
				logger.error("����δ֪chrID "+lastChr);
				flag = false; return;
			}

			chrBpReads = new int[(int) (chrLength + 1)];// ͬ��Ϊ���㣬0λ��¼�ܳ��ȡ�����ʵ��bp����ʵ�ʳ���
			chrBpReads[0] = chrLength.intValue();
			chrMapReadsInfo = new ChrMapReadsInfo(lastChr, mapReads);
			mapReads.mapChrID2ReadsInfo.put(lastChr, chrMapReadsInfo);
		}
		//û�и�Ⱦɫ��������
		if (flag == false) return;
		tmpOld = addLoc(alignRecord, tmpOld, chrBpReads, chrMapReadsInfo);
		chrMapReadsInfo.addReadsAllNum(1);
	}
	
	public void summary() {
		if (!lastChr.equals("") && flag) {
			chrMapReadsInfo.sumChrBp(chrBpReads);
			chrBpReads = null;
		}
	}
	/**
	 * ����Ӻ͵Ĵ�����
	 * ����һ����Ϣ�����������ݼӵ���Ӧ��������
	 * @param alignRecord reads��Ϣ
	 * @param tmpOld ��һ�������յ㣬�����ж��Ƿ�����ͬһλ�����
	 * @param chrBpReads ������Ҫ���ӵ�Ⱦɫ����Ϣ
	 * @param chrMapReadsInfo ��¼�ܹ�mapping��reads������Ϊ���ܹ�������ȥ���������鷽ʽ
	 * @return
	 * ��λ�����Ϣ��������һ���ж��Ƿ���ͬһλ��
	 */
	protected int[] addLoc(AlignRecord alignRecord, int[] tmpOld, int[] chrBpReads, ChrMapReadsInfo chrMapReadsInfo) {
		boolean cis5to3This = alignRecord.isCis5to3();
		if ((mapReads.FilteredStrand != null && alignRecord.isCis5to3() != mapReads.FilteredStrand)
				|| (mapReads.isUniqueMapping() && alignRecord.getMappingNum() > 1)
				) {
			return tmpOld;
		}
		
		int[] tmpStartEnd = new int[2];
		tmpStartEnd[0] = alignRecord.getStartAbs();
		tmpStartEnd[1] = alignRecord.getEndAbs();

		//�����reads����һ��reads��ͬ������Ϊ����������������
		if (mapReads.uniqReads && tmpStartEnd[0] == tmpOld[0] && tmpStartEnd[1] == tmpOld[1] ) {
			return tmpOld;
		}

		ArrayList<? extends Alignment> lsadd = null;
		//���û�пɱ����
		lsadd = alignRecord.getAlignmentBlocks();
		lsadd = setStartCod(lsadd, mapReads.startCod, cis5to3This);

		addChrLoc(chrBpReads, lsadd);
		chrMapReadsInfo.readsAllNum = chrMapReadsInfo.readsAllNum + 1;
		return tmpStartEnd;
	}
	/**
	 * �����������ȡ��Ӧ��������󷵻���Ҫ�ۼӵ�ArrayList<int[]>
	 * @param lsStartEnd
	 * @param cis5to3
	 * @return ���cis5to3 = True����ô���Ž�ȡstartCod���ȵ�����
	 * ���cis5to3 = False����ô���Ž�ȡstartCod���ȵ�����
	 */
	private ArrayList<? extends Alignment> setStartCod(ArrayList<? extends Alignment> lsStartEnd, int StartCodLen, boolean cis5to3) {
		if (StartCodLen <= 0) {
			return lsStartEnd;
		}
		ArrayList<Align> lsResult = new ArrayList<Align>();
		
		if (cis5to3) {
			for (int i = 0; i < lsStartEnd.size(); i++) {
				Alignment alignment = lsStartEnd.get(i);
				if (StartCodLen - lsStartEnd.get(i).Length() > 0) {
					Align align = new Align(alignment.getRefID(), alignment.getStartAbs(), alignment.getEndAbs());
					align.setCis5to3(alignment.isCis5to3());
					lsResult.add(align);
					StartCodLen = StartCodLen - alignment.Length();
				}
				else {
					Align lastAlign = new Align(alignment.getRefID(), alignment.getStartAbs(), alignment.getStartAbs() + StartCodLen - 1);
					lsResult.add(lastAlign);
					break;
				}
			}
		}
		else {
			for (int i = lsStartEnd.size() - 1; i >= 0; i--) {
				Alignment alignment = lsStartEnd.get(i);
				if (StartCodLen - alignment.Length() > 0) {
					Align align = new Align(alignment.getRefID(), alignment.getStartAbs(), alignment.getEndAbs());
					align.setCis5to3(alignment.isCis5to3());
					
					lsResult.add(0,align);
					StartCodLen = StartCodLen - alignment.Length();
				}
				else {
					Align align = new Align(alignment.getRefID(), alignment.getEndAbs() - StartCodLen + 1, alignment.getEndAbs());
					align.setCis5to3(alignment.isCis5to3());
					lsResult.add(0,align);
				}
			}
		}
		return lsResult;
	}
	/**
	 * ����һ�����е�������Ϣ���Լ�������Ҫ�ۼӵ���������
	 * ��������������ۼӵ�Ŀ��������ȥ
	 * @param chrLoc ����λ�㣬0Ϊ���곤�ȣ�1��ʼΪ�������꣬����chrLoc[123] ����ʵ��123λ������
	 * @param lsAddLoc ��ϵ���������Ϊint[2] ��list��Ʃ�� 100-250��280-300�����ӣ�ע���ṩ�����궼�Ǳ����䣬������λ���˶�Ҫ����
	 */
	private void addChrLoc(int[] chrLoc, ArrayList<? extends Alignment> lsAddLoc) {
		for (Alignment is : lsAddLoc) {
			for (int i = is.getStartAbs(); i <=is.getEndAbs(); i++) {
				if (i >= chrLoc.length) {
					logger.info("������Χ��"+ i);
					break;
				}
				if (i < 0) {
					logger.info("������Χ��"+ i);
					continue;
				}
				chrLoc[i]++;
			}
		}
	}
}
