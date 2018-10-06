package com.novelbio.database.service.servgff;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.novelbio.bioinfo.gff.GffFile;
import com.novelbio.bioinfo.gff.GffFileUnit;
import com.novelbio.bioinfo.gff.GffGene;
import com.novelbio.bioinfo.gff.GffHashGene;
import com.novelbio.bioinfo.gff.GffIso;
import com.novelbio.bioinfo.gff.GffType;
import com.novelbio.database.dao.geneanno.RepoGffFile;
import com.novelbio.database.dao.geneanno.RepoGffFileUnit;
import com.novelbio.database.dao.geneanno.RepoGffGene;
import com.novelbio.database.dao.geneanno.RepoGffIso;
import com.novelbio.database.domain.modgeneid.GeneID;
import com.novelbio.database.service.SpringFactoryBioinfo;

@Deprecated
public class MgmtGffDetailGene {
	private static final Logger logger = Logger.getLogger(MgmtGffDetailGene.class);
	static double[] lock = new double[0];
	
	RepoGffFile repoGffFile;
	RepoGffGene repoGffGene;
	RepoGffIso repoGffIso;
	RepoGffFileUnit repoGffFileUnit;
	MongoTemplate mongoTemplate;
	private MgmtGffDetailGene() {
		repoGffFile = (RepoGffFile)SpringFactoryBioinfo.getFactory().getBean("repoGffFile");
		repoGffGene = (RepoGffGene)SpringFactoryBioinfo.getFactory().getBean("repoGffGene");
		repoGffIso = (RepoGffIso)SpringFactoryBioinfo.getFactory().getBean("repoGffIso");
		repoGffFileUnit = (RepoGffFileUnit)SpringFactoryBioinfo.getFactory().getBean("repoGffFileUnit");
		mongoTemplate = (MongoTemplate)SpringFactoryBioinfo.getFactory().getBean("mongoTemplate");
	}
	
	/**
	 * 给定物种，版本，数据库，以及对应的gff文件，将gff文件导入数据库，这个是给jbrowse用的
	 * @param taxId
	 * @param version
	 * @param dbInfo
	 * @param gff_File
	 */
	public void saveGffFile(int taxId, String version, String dbInfo, String gff_File) {
		GffFile gffFile = repoGffFile.findByTaxIdAndVersionAndDbinfo(taxId, version, dbInfo);
		if (gffFile != null) {
			return;
		}
		gffFile = generateGffFile(taxId, version, dbInfo, gff_File);
		repoGffFile.save(gffFile);
		GffHashGene gffHashGene = new GffHashGene(gff_File, taxId == 7227);
		saveGffHashGene(gffFile, gffHashGene);
	}
	
	public GffFile findGffFileDefault() {
		PageModel pageModel = new PageModel();
		pageModel.setRows(1);
		Pageable pageable = pageModel.bePageable();
		Page<GffFile> pageGffFile = repoGffFile.findAll(pageable);
		if (pageGffFile.getSize() > 0) {
			return pageGffFile.iterator().next();
		}
		return null;
	}
	
	/**
	 * 给定物种，版本，数据库，以及对应的gff文件，将gff文件导入数据库，这个是给jbrowse用的
	 * @param taxId
	 * @param version
	 * @param dbInfo
	 * @param gff_File gff文件路径
	 * @param gffType gff的类型
	 */
	public void saveGffFile(int taxId, String version, String dbInfo, String gff_File, GffType gffType) {
		GffFile gffFile = repoGffFile.findByTaxIdAndVersionAndDbinfo(taxId, version, dbInfo);
		if (gffFile != null) {
			return;
		}
		gffFile = generateGffFile(taxId, version, dbInfo, gff_File);
		repoGffFile.save(gffFile);
		GffHashGene gffHashGene = new GffHashGene(taxId, version, dbInfo, gffType, gff_File, taxId == 7227);
		saveGffHashGene(gffFile, gffHashGene);
	}
	
	private GffFile generateGffFile(int taxId, String version, String dbInfo, String gff_File) {
		GffFile gffFile = new GffFile();
		gffFile.setFileName(gff_File);
		gffFile.setTaxID(taxId);
		gffFile.setVersion(version);
		gffFile.setDbinfo(dbInfo);
		return gffFile;
	}
	
	private void saveGffHashGene(GffFile gffFile, GffHashGene gffHashGene) {
		Map<String, List<int[]>> mapChrID2LsInterval = gffHashGene.getMapChrID2LsInterval(10);
		for (String chrId : mapChrID2LsInterval.keySet()) {
			List<int[]> lsTrunkNum = mapChrID2LsInterval.get(chrId);
			for (int i = 0; i < lsTrunkNum.size(); i++) {
				GffFileUnit gffFileUnit = new GffFileUnit();
				gffFileUnit.setGffFileId(gffFile.getId());
				gffFileUnit.setTaxVsDb(gffFile.getTaxID(), gffFile.getVersion(), gffFile.getDbinfo());
				gffFileUnit.setChrId(chrId);
				gffFileUnit.setTrunkNum(i);
				gffFileUnit.setTrunkDetail(lsTrunkNum.get(i));
				repoGffFileUnit.save(gffFileUnit);
			}
		}
		
		for (GffGene gffDetailGene : gffHashGene.getLsGffDetailGenes()) {
			for (GffIso gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
//				gffGeneIsoInfo.setGffFileId(gffFile.getId());
				repoGffIso.save(gffGeneIsoInfo);
			}
			repoGffGene.save(gffDetailGene);
		}
	}
	
	public void saveGffFile(GffFile gffFile) {
		repoGffFile.save(gffFile);
	}
	public void deleteGff(int taxId, String version, String dbInfo) {
		GffFile gffFile = repoGffFile.findByTaxIdAndVersionAndDbinfo(taxId, version, dbInfo);
		if (gffFile == null) {
			return;
		}
		deleteGff(gffFile);
	}
	public void deleteGff(GffFile gffFile) {
		Query query = new Query(Criteria.where("gffFileId").is(gffFile.getId()));
		mongoTemplate.remove(query, GffIso.class);
		mongoTemplate.remove(query, GffGene.class);
		mongoTemplate.remove(query, GffFileUnit.class);
		mongoTemplate.remove(gffFile);
	}
	
	public List<GffFile> getLsGffFileAll() {
		return repoGffFile.findAll();
	}
	
	public GffFile findGffFile(int taxId, String version, String dbinfo) {
		return repoGffFile.findByTaxIdAndVersionAndDbinfo(taxId, version, dbinfo);
	}
	
	public GffFile findGffFileById(String gffFileId) {
		return repoGffFile.findOne(gffFileId);
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/** 直接查GffFileUnit */
	public GffFileUnit findGffFileUnit(int taxId, String version, String dbinfo, String chrId, int trunkNum) {
		String taxVsDb = GffFileUnit.getTaxVsDb(taxId, version, dbinfo);
		return repoGffFileUnit.findByTaxVsDbAndChrIdAndTruncNum(taxVsDb, chrId, trunkNum);
	}
	public GffFileUnit findGffFileUnit(String gffFileId, String chrId, int trunkNum) {
		return repoGffFileUnit.findByFileIdAndChrIdAndTruncNum(gffFileId, chrId, trunkNum);
	}
	public List<GffFileUnit> findLsGffFileUnit(String gffFileId) {
		return repoGffFileUnit.findByFileId(gffFileId);
	}
	public List<GffFileUnit> findLsGffFileUnit(int taxId, String version, String dbinfo) {
		String taxVsDb = GffFileUnit.getTaxVsDb(taxId, version, dbinfo);
		return repoGffFileUnit.findByTaxVsDb(taxVsDb);
	}
	public List<GffFileUnit> findLsGffFileUnit(String gffFileId, String chrId) {
		return repoGffFileUnit.findByFileIdAndChrId(gffFileId, chrId);
	}
	public List<GffFileUnit> findLsGffFileUnit(int taxId, String version, String dbinfo, String chrId) {
		String taxVsDb = GffFileUnit.getTaxVsDb(taxId, version, dbinfo);
		return repoGffFileUnit.findByTaxVsDbAndChrId(taxVsDb, chrId);
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public List<GffGene> searchRegionOverlap(String gffFileId, String chrID, int start, int end) {
		int startAbs = Math.min(start, end);
		int endAbs = Math.max(start, end);
		List<GffGene> lsGffDetailGenes = repoGffGene.findByFileId_ChrId_RegionOverlap
				(gffFileId, chrID, startAbs, endAbs, new Sort(new Sort.Order(Sort.Direction.ASC, "numberstart")));
		for (GffGene gffDetailGene : lsGffDetailGenes) {
			for (GffIso gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				gffGeneIsoInfo.setGffDetailGeneParent(gffDetailGene);
			}
		}
		return lsGffDetailGenes;
	}
	
	public List<GffGene> searchRegionIn(String gffFileId, String chrID, int start, int end) {
		int startAbs = Math.min(start, end);
		int endAbs = Math.max(start, end);
		List<GffGene> lsGffDetailGenes = repoGffGene.findByFileId_ChrId_RegionCover
				(gffFileId, chrID, startAbs, endAbs, new Sort(new Sort.Order(Sort.Direction.ASC, "numberstart")));
		for (GffGene gffDetailGene : lsGffDetailGenes) {
			for (GffIso gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				gffGeneIsoInfo.setGffDetailGeneParent(gffDetailGene);
			}
		}
		return lsGffDetailGenes;
	}
//	/** 模糊查找 */
//	public List<GffGene> searchGene(GffFile gffFile, String geneNameRegex) {
//		List<GffGene> lsGffDetailGenes = searchGeneExact(gffFile.getId(), geneNameRegex);
//		if (lsGffDetailGenes.isEmpty()) {
//			GeneID geneID = new GeneID(geneNameRegex, gffFile.getTaxID());
//			if (geneID.getTaxID() == 0) {
//				return new ArrayList<>();
//			}
//			String geneId = geneID.getGeneUniID();
//			lsGffDetailGenes = searchGeneExact(gffFile.getId(), geneId);
//		}
//		
//		if (lsGffDetailGenes.isEmpty()) {
//			if (geneNameRegex.length() <= 1) {
//				return new ArrayList<>();
//			}	
//			lsGffDetailGenes = searchGeneRegex(gffFile.getId(), geneNameRegex);
//		}
//
//		
//		for (GffGene gffDetailGene : lsGffDetailGenes) {
//			for (GffIso gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
//				gffGeneIsoInfo.setGffDetailGeneParent(gffDetailGene);
//			}
//		}
//		return lsGffDetailGenes;
//	}
	
//	private List<GffGene> searchGeneRegex(String gffFileId, String geneNameRegex) {
//		geneNameRegex = geneNameRegex.toLowerCase();
//		return repoGffGene.findByFileId_Name_Regex(gffFileId, geneNameRegex);
//	}
//	private List<GffGene> searchGeneExact(String gffFileId, String geneNameExact) {
//		geneNameExact = geneNameExact.toLowerCase();
//		return repoGffGene.findByFileId_Name_Exact(gffFileId, geneNameExact);
//	}

	
	static class ManageGffDetailGeneHolder {
		static MgmtGffDetailGene manageGffDetailGene = new MgmtGffDetailGene();
	}
	
	public static MgmtGffDetailGene getInstance() {
		return ManageGffDetailGeneHolder.manageGffDetailGene;
	}
	
}
/**
 * easyui datagrid 页面返回模型
 * @author novelbio
 *
 */
class PageModel {
	
	/** 升序 */
	public static final String ORDER_ASC = Direction.ASC.toString();
	/** 降序 */
	public static final String ORDER_DESC = Direction.DESC.toString();
	
	/** 当前页 */
	private int page = 1;
	/** 每页条数 */
	private int rows = 20;
	/** 按什么属性排序 */
	private String sort;
	/** 按多个属性排序 */
	private String[] sorts;
	/** 排序方式，默认升序 desc asc */
	private String order = Direction.ASC.toString();
	
	/** 排序方式 sort=id order=asc rows=Integer.MAX_VALUE */
	public final static int ID_ASC_ALL = 1;
	/** 排序方式 sort=createTime order=desc rows=Integer.MAX_VALUE */
	public final static int CREATETIME_DESC_ALL = 5;
	/** 排序方式 sort=createTime order=desc rows=20 */
	public final static int CREATETIME_DESC_PAGE = 6;
	
	public PageModel(final int orderType){
		switch (orderType) {
		case ID_ASC_ALL:
			setSort("_id");
			setRows(Integer.MAX_VALUE);
			break;
		case CREATETIME_DESC_ALL:
			setSort("createTime");
			setOrder(Sort.Direction.DESC.toString());
			setRows(Integer.MAX_VALUE);
			break;
		case CREATETIME_DESC_PAGE:
			setSort("createTime");
			setOrder(Sort.Direction.DESC.toString());
			break;
		default:
			;
		}
	}
	
	public PageModel() {

	}
	
	/** 返回当前页，默认为1 */
	public int getPage() {
		return page;
	}
	
	/** 返回上一页*/
	public int getPrevPage() {
		return (page - 1) < 1 ? 1 : page-1;
	}
	
	/** 返回下一页*/
	public int getNextPage() {
		return page + 1;
	}
	
	public void setPage(int page) {
		this.page = page;
	}
	
	
	public void addPage() {
		page++;
	}
	/** 每条页数，默认为20 */
	public int getRows() {
		return rows;
	}
	/** 每条页数，默认为20 */
	public void setRows(int rows) {
		this.rows = rows;
	}
	/** 按什么属性排序 */
	public String getSort() {
		return sort;
	}
	/** 按什么属性排序 */
	@Deprecated
	public void setSort(String sort) {
		this.sort = sort;
	}
	
	public String[] getSorts() {
		return sorts;
	}
	/** 按多个属性排序 */
	@Deprecated
	public void setSorts(String[] sorts) {
		this.sorts = sorts;
	}

	/** 排序方式 desc asc */
	public String getOrder() {
		return order;
	}
	/** 排序方式 desc asc */
	@Deprecated
	public void setOrder(String order) {
		this.order = order;
	}
	
	/** 设置按ASC升序排序的字段名称 */
	public void setOrderByASC(String fieldForSort) {
		this.order = ORDER_ASC;
		this.sort = fieldForSort;
	}
	
	/** 设置按DESC降序排序的字段名称 */
	public void setOrderByDESC(String fieldForSort) {
		this.order = ORDER_DESC;
		this.sort = fieldForSort;
	}
	
	/** 设置按ASC升序排序的字段名称 */
	public void setOrderByASC(String[] fieldsForSort) {
		this.order = ORDER_ASC;
		this.sorts = fieldsForSort;
	}
	
	/** 设置按DESC降序排序的字段名称 */
	public void setOrderByDESC(String[] fieldsForSort) {
		this.order = ORDER_DESC;
		this.sorts = fieldsForSort;
	}
	
	/**
	 * 获取某list的指定分页对象
	 * @param lsResult
	 * @return Page<T>
	 */
	public <T> Page<T> getPage(List<T> lsResult) {
		return new PageImpl<T>(getSliceForPage(lsResult), bePageable(), lsResult.size());
	}
	
	/**
	 * 获取某list的一个分页片段
	 * @param lsResult
	 * @return List<T>
	 */
	public <T> List<T> getSliceForPage(List<T> lsResult) {
		if (null == lsResult)
			return null;
		if (lsResult.size() <= 0)
			return lsResult;
		int fromIndex = 0;
		int toIndex = rows;
		
		if(page > 1){
			fromIndex = (page - 1) * rows;
			toIndex = fromIndex + rows;
		}
		if (fromIndex >= lsResult.size()){
			fromIndex = lsResult.size() - 1;
		}
		if (toIndex > lsResult.size())
			toIndex = lsResult.size();

		return lsResult.subList(fromIndex, toIndex);
	}
	
	/**
	 * 生成一个Pageable对象供mongoDB的分页查询使用
	 */
	public Pageable bePageable() {
		Pageable pageable = null;
		if (sort != null && order != null) {
			pageable = new PageRequest(page - 1, rows, Direction.fromString(this.order), this.sort);
		} else if (sorts != null && order != null) {
			pageable = new PageRequest(page - 1, rows, Direction.fromString(this.order), this.sorts);
		} else {
			pageable = new PageRequest(page - 1, rows);
		}
		return pageable;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("PageModel:");
		sb.append(" page is " + page);
		sb.append(", rows is " + rows);
		if (sort != null) {
			sb.append(", sort " + sort + " by " + order);
		}
		if (sorts != null) {
			sb.append(", sort " + sorts + " by " + order);
		}
		return sb.toString();
	}
	
}