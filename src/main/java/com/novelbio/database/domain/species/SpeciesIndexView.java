package com.novelbio.database.domain.species;

import java.util.ArrayList;
import java.util.List;

/**
 * 物种索引视图
 * @author luwei
 */
public class SpeciesIndexView {
	private int taxID = 0;
	private String speciesFileId = null;
	private String versionName = null;
	private String indexType = null;
	private String faFileName = null;
	private String faFileType = null;
	private long extractTime = 0L;
	private List<IndexFileView> lsIndexFileViews = new ArrayList<>();

	public int getTaxID() {
		return taxID;
	}

	public void setTaxID(int taxID) {
		this.taxID = taxID;
	}

	public String getSpeciesFileId() {
		return speciesFileId;
	}

	public void setSpeciesFileId(String speciesFileId) {
		this.speciesFileId = speciesFileId;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public String getIndexType() {
		return indexType;
	}

	public void setIndexType(String indexType) {
		this.indexType = indexType;
	}

	public String getFaFileName() {
		return faFileName;
	}

	public void setFaFileName(String faFileName) {
		this.faFileName = faFileName;
	}

	public String getFaFileType() {
		return faFileType;
	}

	public void setFaFileType(String faFileType) {
		this.faFileType = faFileType;
	}

	public long getExtractTime() {
		return extractTime;
	}

	public void setExtractTime(long extractTime) {
		this.extractTime = extractTime;
	}

	public List<IndexFileView> getLsIndexFileViews() {
		return lsIndexFileViews;
	}

	public void setLsIndexFileViews(List<IndexFileView> lsIndexFileViews) {
		this.lsIndexFileViews = lsIndexFileViews;
	}

	public void addIndexFileView(String indexTool, String path, long createTime) {
		IndexFileView indexFileView = new IndexFileView(indexTool, path, createTime);
		this.lsIndexFileViews.add(indexFileView);
	}
	
	/**
	 * 物种索引文件视图
	 * @author luwei
	 */
	public class IndexFileView {
		private String indexTool = null;
		private String path = null;
		private long createTime = 0L;
		
		public IndexFileView(String indexTool, String path, long createTime) {
			this.indexTool = indexTool;
			this.path = path;
			this.createTime = createTime;
		}

		public String getIndexTool() {
			return indexTool;
		}

		public void setIndexTool(String indexTool) {
			this.indexTool = indexTool;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public long getCreateTime() {
			return createTime;
		}

		public void setCreateTime(long createTime) {
			this.createTime = createTime;
		}

	}
}
