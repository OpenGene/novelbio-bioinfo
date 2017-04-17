package com.novelbio.analysis.seq.genome.gffOperate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;

import com.novelbio.base.SepSign;
import com.novelbio.database.service.servgff.MgmtGffDetailGene;

@CompoundIndexes({
	@CompoundIndex(unique = true, name = "fileid_chr_trunknum_idx", def = "{'gffFileId': 1, 'chrId': 1, 'trunkNum': 1}"),
    @CompoundIndex(unique = true, name = "taxVsDb_chr_trunknum_idx", def = "{'taxVsDb': 1, 'chrId': 1, 'trunkNum': 1}")
})
public class GffFileUnit {
	@Id
	String id;
	@Indexed
	String gffFileId;

	/** TaxId Version DbInfo */
	String taxVsDb;
	
	String chrId;
	int trunkNum;
	
	int[] trunkDetail;
	/** 染色体ID都小写 */
	Map<String, List<int[]>> mapChrID2LsInterval;
	
	public void setChrId(String chrId) {
		this.chrId = chrId;
	}
	public void setGffFileId(String gffFileId) {
		this.gffFileId = gffFileId;
	}
	public String getChrId() {
		return chrId;
	}
	public String getGffFileId() {
		return gffFileId;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getId() {
		return id;
	}
	
	public void setTaxVsDb(int taxId, String version ,String dbInfo) {
		this.taxVsDb = getTaxVsDb(taxId, version, dbInfo);
	}
	
	public void setTrunkNum(int trunkNum) {
		this.trunkNum = trunkNum;
	}
	public int getTrunkNum() {
		return trunkNum;
	}
	public void setTrunkDetail(int[] trunkDetail) {
		this.trunkDetail = trunkDetail;
	}
	public int[] getTrunkDetail() {
		return trunkDetail;
	}
	
	public static String getTaxVsDb(int taxId, String version ,String dbInfo) {
		return taxId + SepSign.SEP_INFO + version + SepSign.SEP_INFO_SIMPLE + dbInfo;
	}
	
	/** 直接查询GffFileUnit */
	public static GffFileUnit findGffFileSimple(int taxId, String version, String dbinfo, String chrId, int truncNum) {
		return MgmtGffDetailGene.getInstance().findGffFileUnit(taxId, version, dbinfo, chrId, truncNum);
	}
	/** 先查寻GffFile，获得GffFileId后再查询GffFileUnit*/
	public static GffFileUnit findGffFileUnit(int taxId, String version, String dbinfo, String chrId, int truncNum) {
		MgmtGffDetailGene manageGffDetailGene = MgmtGffDetailGene.getInstance();
		GffFile gffFile = manageGffDetailGene.findGffFile(taxId, version, dbinfo);
		return manageGffDetailGene.findGffFileUnit(gffFile.getId(), chrId, truncNum);
	}
	public static GffFileUnit findGffFileUnit(String gffFileId, String chrId, int truncNum) {
		return MgmtGffDetailGene.getInstance().findGffFileUnit(gffFileId, chrId, truncNum);
	}
	public static Map<String, List<int[]>> findGffFileUnit(String gffFileId) {
		Map<String, List<int[]>> mapChrId2LsInterval = new HashMap<>();
		List<GffFileUnit> lsGffFileUnits = MgmtGffDetailGene.getInstance().findLsGffFileUnit(gffFileId);
		for (GffFileUnit gffFileUnit : lsGffFileUnits) {
			String chrId = gffFileUnit.getChrId();
			List<int[]> lsInterval = mapChrId2LsInterval.get(chrId);
			if (lsInterval == null) lsInterval = new ArrayList<>();
			lsInterval.add(gffFileUnit.getTrunkDetail());
		}
		for (String chrId : mapChrId2LsInterval.keySet()) {
			List<int[]> lsInterval = mapChrId2LsInterval.get(chrId);
			Collections.sort(lsInterval, new Comparator<int[]>() {
				public int compare(int[] o1, int[] o2) {
					Integer o1start = o1[0];
					Integer o2start = o2[0];
					return o1start.compareTo(o2start);
				}
			});
		}
		return mapChrId2LsInterval;
	}
	/** 直接查询GffFileUnit 输出结果按照坐标顺序排列 */
	public static List<int[]> findGffFileUnit(String gffFileId, String chrId) {
		List<GffFileUnit> lsGffFileUnits = MgmtGffDetailGene.getInstance().findLsGffFileUnit(gffFileId, chrId);
		List<int[]> lsInterval = new ArrayList<>();
		for (GffFileUnit gffFileUnit : lsGffFileUnits) {
			lsInterval.add(gffFileUnit.getTrunkDetail());
		}
		Collections.sort(lsInterval, new Comparator<int[]>() {
			public int compare(int[] o1, int[] o2) {
				Integer o1start = o1[0];
				Integer o2start = o2[0];
				return o1start.compareTo(o2start);
			}
		});
		return lsInterval;
	}
	/** 直接查询GffFileUnit 输出结果按照坐标顺序排列 */
	public static List<int[]> findGffFileUnit(int taxId, String version, String dbinfo, String chrId) {
		List<GffFileUnit> lsGffFileUnits = MgmtGffDetailGene.getInstance().findLsGffFileUnit(taxId, version, dbinfo, chrId);
		List<int[]> lsInterval = new ArrayList<>();
		for (GffFileUnit gffFileUnit : lsGffFileUnits) {
			lsInterval.add(gffFileUnit.getTrunkDetail());
		}
		Collections.sort(lsInterval, new Comparator<int[]>() {
			public int compare(int[] o1, int[] o2) {
				Integer o1start = o1[0];
				Integer o2start = o2[0];
				return o1start.compareTo(o2start);
			}
		});
		return lsInterval;
	}
}
