package com.novelbio.database.model.geneanno;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.google.common.annotations.VisibleForTesting;
import com.novelbio.analysis.seq.genome.gffOperate.GffType;
import com.novelbio.base.ExceptionNullParam;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.species.ExceptionNbcSpeciesNotExist;
import com.novelbio.database.service.servgeneanno.IManageSpecies;
import com.novelbio.database.service.servgeneanno.ManageSpecies;
import com.novelbio.generalConf.PathDetailNBC;

/**
 * 保存某个物种的各种文件信息，譬如mapping位置等等
 * 感觉可以用nosql进行存储，完全不能是一个结构化文件啊
 * @author zong0jie
 */
@Document(collection="speciesfile")
@CompoundIndexes({
    @CompoundIndex(unique = true, name = "species_version_idx", def = "{'taxID': 1, 'version': -1}"),
 })
public class SpeciesFile {
	private static final Logger logger = LoggerFactory.getLogger(SpeciesFile.class);
	
	/** 物种文件夹名称 */
	public static final String SPECIES_FOLDER = "species";
	/** 相对路径，类似 /media/hdfs/nbCloud/public/nbcplatform/ ，注意不要把genome写进去<br>
	 * 然后以后把speciesFile写在 /media/hdfs/nbCloud/public/nbcplatform/这个文件夹下就行
	 * 
	 * 仅用于测试
	 */
	@Transient
	public static String pathParent = PathDetailNBC.getGenomePath();
	
	@Id
	String id;
	@Indexed
	int taxID;
	
	/** 物种名，仅供桌面发行版使用 */
	@Transient
	transient String speciesName;
	
	/** 文件版本 */
	String version;
	/** 该版本的年代，大概年代就行 */
	int publishYear;
	/** 染色体的单文件序列 */
	String chromSeq;
	/** 相对路径 gff的repeat文件，从ucsc下载 */
	String gffRepeatFile;
	/** 相对路径 refseq文件，全体Iso */
	String refseqFileAllIso;
	/** 相对路径 refseq文件，一个gene一个Iso */
	String refseqFileOneIso;
	
	/** 相对路径 ref protein 文件，全体iso */
	String refProFileAllIso;
	/** 相对路径 ref protein 文件，一个gene一个Iso */
	String refProFileOneIso;
	
	/** refseq中的NCRNA文件 */
	String refseqNCfile;
	
	/**
	 * key: DBname, 为小写<br>
	 * value: 0, GffType 1:GffFile
	 */
	Map<String, String[]> mapDB2GffTypeAndFile = new LinkedHashMap<>();
	/** 用来将小写的DB转化为正常的DB，使得getDB获得的字符应该是正常的DB */
	Map<String, String> mapGffDBLowCase2DBNormal = new LinkedHashMap<>();
	

	/** key: chrID，为小写    value: chrLen */
	@Transient
	private Map<String, Long> mapChrID2ChrLen = new LinkedHashMap<String, Long>();

	
	/** mongodb的ID */
	public void setId(String id) {
		this.id = id;
	}
	/** mongodb的ID */
	public String getId() {
		return id;
	}
	
	public void setTaxID(int taxID) {
		this.taxID = taxID;
	}
	public int getTaxID() {
		return taxID;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getVersion() {
		return version;
	}
	
	public void setSpeciesName(String speciesName) {
		this.speciesName = speciesName;
	}
	public String getSpeciesName() {
		return speciesName;
	}
	
	/**
	 * @return
	 * key: chrID 没有小写
	 * value： length
	 */
	public Map<String, Long> getMapChromInfo() {
		if (!mapChrID2ChrLen.isEmpty()) {
			return mapChrID2ChrLen;
		}
		synchronized (this) {
			String chrFile = getChromSeqFile();
			if (FileOperate.isFileExistAndBigThanSize(chrFile + ".fai", 0)) {
				mapChrID2ChrLen = new LinkedHashMap<>();
				TxtReadandWrite txtRead = new TxtReadandWrite(chrFile + ".fai");
				for (String content : txtRead.readlines()) {
					String[] ss = content.split("\t");
					mapChrID2ChrLen.put(ss[0], Long.parseLong(ss[1]));
				}
				txtRead.close();
			}
        }
		
		return mapChrID2ChrLen;
	}
	
	/**
	 * 物种版本对应的文件夹
	 * @return
	 */
	public String getSpeciesVersionPath() {
		if(taxID == 0 || StringOperate.isRealNull(version))
			return null;
		String basePath = FileOperate.addSep(pathParent) + SpeciesFile.SPECIES_FOLDER + FileOperate.getSepPath()
				+ taxID + FileOperate.getSepPath() + version + FileOperate.getSepPath();
		return basePath;
	}

	/** 物种版本的相对路径，到版本为止
	 * 如 9606/GRCh38/<br>
	 * 包含最后的"/"
	 */
	public String getPathToVersion() {
		if(taxID == 0 || StringOperate.isRealNull(version))
			throw new ExceptionNbcSpeciesNotExist("species taxId cannot be 0, and version must exist");
		return taxID + FileOperate.getSepPath() + version + FileOperate.getSepPath();
	}
	
	/** 相对路径，或者说文件名 */
	public void setChromSeq(String chromSeq) {
		this.chromSeq = chromSeq;
	}
	
	/** 返回文件名 */
	public String getChromSeq() {
		return chromSeq;
	}
	
	/** 返回染色体文件名，包含路径 */
	public String getChromSeqFile() {
		if (StringOperate.isRealNull(chromSeq)) {
			return null;
		}
		return EnumSpeciesFile.chromSeqFile.getSavePath(taxID, this) + chromSeq;
	}
	
	/**
	 * 给GUI的下拉框用的，一般用不到
	 * @return GffFile
	 */
	public Map<String, String> getMapGffDB() {
		Map<String, String> mapStringDB = new LinkedHashMap<String, String>();
		
		if (mapGffDBLowCase2DBNormal.size() == 0) {
			return mapStringDB;
		}
		for (String gffDB : mapGffDBLowCase2DBNormal.values()) {
			mapStringDB.put(gffDB, gffDB);
		}
		return mapStringDB;
	}
	
	/** 
	 * @param gffDB
	 * @param gffType
	 * @param gffFile 输入相对路径，仅包含文件名
	 */
	public void addGffDB2TypeFile(String gffDB, GffType gffType, String gffFileName) {
		if (StringOperate.isRealNull(gffDB)) {
			throw new ExceptionNullParam("No Param gffDB");
		}
		mapDB2GffTypeAndFile.put(gffDB.toLowerCase(), new String[]{gffType.toString(), gffFileName});
		mapGffDBLowCase2DBNormal.put(gffDB.toLowerCase(), gffDB);
	}
	
	/** 
	 * 更新gffDB
	 * @param gffDB
	 * @param newGffDB
	 */
	public void updGffDB(String gffDB, String newGffDB) {
		if (StringOperate.isRealNull(gffDB)) {
			throw new ExceptionNullParam("No Param gffDB");
		}
		
		String[] gff = mapDB2GffTypeAndFile.remove(gffDB.toLowerCase());
		mapDB2GffTypeAndFile.put(newGffDB.toLowerCase(), gff);
		mapGffDBLowCase2DBNormal.remove(gffDB.toLowerCase());
		mapGffDBLowCase2DBNormal.put(newGffDB.toLowerCase(), newGffDB);
	}
	
	/** 
	 * 更新gffType
	 * @param gffDB
	 * @param gffType
	 */
	public void updGffType(String gffDB, String gffType) {
		if (StringOperate.isRealNull(gffDB)) {
			throw new ExceptionNullParam("No Param gffDB");
		}
		
		String[] gff = mapDB2GffTypeAndFile.get(gffDB.toLowerCase());
		gff[0] = gffType;
	}
	
	/**
	 * 获得某个Type的GffType，如果没有则返回null
	 * @param GFFtype 指定gfftype 如果为null，表示不指定，则返回默认
	 * @return
	 */
	public GffType getGffType(String gffDB) {
		if (StringOperate.isRealNull(gffDB)) {
			throw new ExceptionNullParam("No Param gffDB");
		}
		String gffType = mapDB2GffTypeAndFile.get(gffDB.toLowerCase())[0];
		return GffType.getType(gffType);
	}
	
	/**
	 * 按照优先级返回gff文件，优先级由GFFtype来决定
	 * @return GffDB
	 */
	public String getGffDB() {
		String[] gffInfo = getGffDB2GffTypeFile();
		return gffInfo[0];
	}
	
	/**
	 * 按照优先级返回gff文件，优先级由GFFtype来决定
	 * @return GffFile
	 */
	public String getGffFile() {
		String[] gffInfo = getGffDB2GffTypeFile();
		if (gffInfo[2] == null) {
			return null;
		}
		return EnumSpeciesFile.gffGeneFile.getSavePath(taxID, this) + gffInfo[2];
	}
	
	/**
	 * 获得某个Type的Gff文件，如果没有则返回null
	 * @param gffDB 指定gffDB 如果为null，表示不指定，则返回默认
	 * @return
	 */
	public String getGffFile(String gffDB) {
		if (gffDB == null) {
			return getGffFile();
		}
		if (ArrayOperate.isEmpty(mapDB2GffTypeAndFile)) {
			return null;
		}
		return EnumSpeciesFile.gffGeneFile.getSavePath(taxID, this) + mapDB2GffTypeAndFile.get(gffDB.toLowerCase())[1];
	}
	
	/**
	 * 按照优先级返回gff类型，优先级由GffDB来决定
	 * 公返回枚举
	 * @return  GffType
	 */
	public GffType getGffType() {
		String[] gffInfo = getGffDB2GffTypeFile();
		String gffType = gffInfo[1];
		if (gffType == null) return null;
        
		return GffType.getType(gffType);
	}
	/**
	 * 按照优先级返回gff文件，优先级由GFFDB来决定
	 * @return string[2]<br>
	 * 0: gffDB<br>
	 * 1: GffType<br>
	 * 2: GffFile 相对文件名
	 */
	private String[] getGffDB2GffTypeFile() {
		if (mapDB2GffTypeAndFile.size() == 0) {
			return new String[]{null, null, null};
		}
		Entry<String, String[]> entyGffDB2File = mapDB2GffTypeAndFile.entrySet().iterator().next();
		String gffDB = entyGffDB2File.getKey();
		gffDB = mapGffDBLowCase2DBNormal.get(gffDB);
		String[] gffType2File = entyGffDB2File.getValue();
		return new String[]{gffDB, gffType2File[0], gffType2File[1]};
	}
	
	/**
	 * 返回gffGene的map
	 * 里面都是相对路径
	 * @return
	 */
	public Map<String, String[]> getGffDB2GffTypeFileMap() {
		Map<String, String[]> mapDB2GffTypeAndFile = new LinkedHashMap<>();
		for (String gffDBlowcase : this.mapDB2GffTypeAndFile.keySet()) {
			String gffDB = mapGffDBLowCase2DBNormal.get(gffDBlowcase);
			mapDB2GffTypeAndFile.put(gffDB, this.mapDB2GffTypeAndFile.get(gffDBlowcase));
		}
		return Collections.unmodifiableMap(mapDB2GffTypeAndFile);
	}
	
	public boolean isHaveGffDB(String gffDB) {
		if (mapGffDBLowCase2DBNormal == null) return false;
		return mapGffDBLowCase2DBNormal.containsKey(gffDB.toLowerCase());
	}
	
	/** 从map中删除某个gffDB，不保存数据库
	 * @param gffDb 内部会转为小写
	 */
	public void removeGffDB(String gffDb) {
		if (StringOperate.isRealNull(gffDb)) {
			return;
		}
		gffDb = gffDb.toLowerCase();
		mapDB2GffTypeAndFile.remove(gffDb.toLowerCase());
		mapGffDBLowCase2DBNormal.remove(gffDb.toLowerCase());
	}
	
	/** 输入相对路径 */
	public void setGffRepeatFile(String gffRepeatFile) {
		this.gffRepeatFile = gffRepeatFile;
	}
	
	public String getGffRepeatFile() {
		if (StringOperate.isRealNull(gffRepeatFile)) {
			return "";
		}
		return EnumSpeciesFile.gffRepeatFile.getSavePath(taxID, this) + gffRepeatFile;
	}

	public void setPublishYear(int publishYear) {
		this.publishYear = publishYear;
	}
	/** 获得年代 */
	public int getPublishYear() {
		return publishYear;
	}
	
	/** 获得全路径的文件名 */
	public String getRefSeqFile(boolean isAllIso, boolean isProtein) {
		String fileName = getRefSeqFileName(isAllIso, isProtein);
		if (!StringOperate.isRealNull(fileName)) {
	        	return getRefFilePath(isAllIso, isProtein) + fileName;
        }
		return null;
	}
	
	/** 获得refseq的文件名，不含路径

	 * @param fileName 文件名，不含路径
	 * @param isAllIso 是否为全iso文件，false：一个基因仅有一条序列，true：一个基因有多个转录本
	 * @param isProtein 是否为蛋白序列
	 * @return
	 */
	public String getRefSeqFileName(boolean isAllIso, boolean isProtein) {
		if (isAllIso) {
			return isProtein? refProFileAllIso : refseqFileAllIso;
		} else {
			return isProtein? refProFileOneIso : refseqFileOneIso;
		}
	}
	/** 设定refseq的文件名
	 * 
	 * @param fileName 文件名，不含路径
	 * @param isAllIso 是否为全iso文件，false：一个基因仅有一条序列，true：一个基因有多个转录本
	 * @param isProtein 是否为蛋白序列
	 * @return
	 */
	public void setRefSeqFileName(String fileName, boolean isAllIso, boolean isProtein) {
		if (isAllIso) {
			if (isProtein) {
				this.refProFileAllIso = fileName;
			} else {
				this.refseqFileAllIso = fileName;
			}
		} else {
			if (isProtein) {
				this.refProFileOneIso = fileName;
			} else {
				this.refseqFileOneIso = fileName;
			}
		}
	}
	
	/** 返回全路径，以"/"结尾 */
	public String getRefFilePath(boolean isAllIso, boolean isProtein) {
		String refseqPath = null, refseqFile = null;
		if (!isProtein) {
			refseqPath = isAllIso? EnumSpeciesFile.refseqAllIsoRNA.getSavePath(taxID, this) : EnumSpeciesFile.refseqOneIsoRNA.getSavePath(taxID, this);
		} else {
			refseqPath = isAllIso? EnumSpeciesFile.refseqAllIsoPro.getSavePath(taxID, this) : EnumSpeciesFile.refseqOneIsoPro.getSavePath(taxID, this);
		}
		refseqFile = refseqPath;
		return refseqFile;
	}
		
	/** 设定相对路径 */
	public void setRefseqNCfile(String refseqNCfile) {
		this.refseqNCfile = refseqNCfile;
	}
	
	public String getRefseqNCfile() {
		if (StringOperate.isRealNull(refseqNCfile)) {
			return null;
		}
		return EnumSpeciesFile.refseqNCfile.getSavePath(taxID, this) + refseqNCfile;
	}
	
	/**
	 * 是否物种特异性的提取，获取绝对路径
	 * @param speciesSpecific
	 * @return
	 */
	public String getRfamFile(boolean speciesSpecific) {
		String node = "rfam/";
		if (speciesSpecific) {
			return getRfamAll();
		} else {
			return pathParent + node + getPathToVersion() + "rfamFile";
		}
	}
	
	public static String getRfamAll() {
		String node = "rfam/";
		return pathParent + node + 0 + "/rfamFileAll";
	}
	/**
	 * 仔仔细细的全部比较一遍，方便用于数据库升级
	 * @param obj
	 * @return
	 */
	public boolean equalsDeep(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		
		if (getClass() != obj.getClass()) return false;
		SpeciesFile otherObj = (SpeciesFile)obj;
		if (mapChrID2ChrLen.equals(otherObj.mapChrID2ChrLen)
			&& ArrayOperate.compareMapStrArray(mapDB2GffTypeAndFile, otherObj.mapDB2GffTypeAndFile)
			&& mapGffDBLowCase2DBNormal.equals(otherObj.mapGffDBLowCase2DBNormal)
			&& ArrayOperate.compareString(chromSeq, otherObj.chromSeq)
			&& ArrayOperate.compareString(gffRepeatFile, otherObj.gffRepeatFile)
			&& this.publishYear == otherObj.publishYear
			&& ArrayOperate.compareString(this.refseqFileOneIso, otherObj.refseqFileOneIso)
			&& ArrayOperate.compareString(this.refseqFileAllIso, otherObj.refseqFileAllIso)
			&&ArrayOperate.compareString( this.refseqNCfile, otherObj.refseqNCfile)
			&& this.taxID == otherObj.taxID
			&& ArrayOperate.compareString(this.version, otherObj.version)
			)
		{
			return true;
		}
		return false;
	}
	
	/**
	 * @param taxID
	 * @return
	 */
	public static List<SpeciesFile> findByTaxID(int taxID) {
		IManageSpecies manageSpecies = ManageSpecies.getInstance();
		return manageSpecies.queryLsSpeciesFile(taxID);
	}
	
	/**
	 * 添加需要保存的路径信息但不保存
	 * @param fileType 物种文件类型
	 * @param fileName 文件名，不包含路径
	 * @param gffType 只有当文件类型是gffGeneFile时，才有用
	 * @param gffDB 只有当文件类型是gffGeneFile时，才有用
	 */
	public void addPathInfo(EnumSpeciesFile fileType, String fileName,GffType gffType,String gffDB) {
		switch (fileType) {
		case chromSeqFile: {
			setChromSeq(fileName);
			break;
		}
		case gffGeneFile: {
			addGffDB2TypeFile(gffDB, gffType, fileName);
			break;
		}
		case gffRepeatFile: {
			setGffRepeatFile(fileName);
			break;
		}
		case refseqAllIsoRNA: {
			setRefSeqFileName(fileName, true, false);
			break;
		}
		case refseqOneIsoRNA: {
			setRefSeqFileName(fileName, false, false);
			break;
		}
		case refseqNCfile: {
			setRefseqNCfile(fileName);
			break;
		}
		default:
			break;
		}
	}
	
	/** 保存，保存之前验证version是否改变，如果改变了，那么就不能保存成功 */
	public boolean save() {
		try {
			if(!StringOperate.isRealNull(this.id)) {
				SpeciesFile speciesFileOld = SpeciesFile.findById(id);
				if(speciesFileOld == null)
					return false;
				if(!speciesFileOld.getVersion().equals(this.version))
					return false;
			}
			repo().saveSpeciesFile(this);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	/**
	 * 根据物种编号以及版本号查找物种
	 * @param taxId
	 * @param version
	 * @return
	 */
	public static SpeciesFile findByTaxIDVersion(int taxId,String version) {
		return repo().querySpeciesFile(taxId, version);
	}
	public static SpeciesFile findById(String speciesFileId) {
		return repo().findOne(speciesFileId);
	}
	
	/**
	 * 查询物种的所有版本
	 * @param pageable
	 * @return
	 */
	public static List<SpeciesFile> queryLsSpeciesFile(int taxID){
		return repo().queryLsSpeciesFile(taxID);
	}
	
	/**
	 * 根据物种的版本的id删除对应的版本
	 * @param speciesFileId
	 * @return
	 */
	public static boolean deleteById(String speciesFileId) {
		SpeciesFile speciesFileOld = SpeciesFile.findById(speciesFileId);
		try {
			repo().deleteSpeciesFile(speciesFileId);
			FileOperate.deleteFileFolder(speciesFileOld.getSpeciesVersionPath());
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	
	/** 数据库操作类 */
	private static IManageSpecies repo() {
		return ManageSpecies.getInstance();
	}
	
	@VisibleForTesting
	public static void setPathParent(String pathParent) {
		SpeciesFile.pathParent = pathParent;
	}
}

/** 比较GFFDB的比较器 */
class CompGffDB implements Comparator<String> {
	public int compare(String o1, String o2) {
		if (o1.equalsIgnoreCase(o2)) {
			return 0;
		}
		if (o1.equalsIgnoreCase("ucsc")) {
			return 1;
		} else if (o2.equalsIgnoreCase("ucsc")) {
			return -1;
		} else if (o1.equalsIgnoreCase("ncbi") && !o2.equalsIgnoreCase("ucsc")) {
			return 1;
		} else if (!o1.equalsIgnoreCase("ucsc") && o2.equalsIgnoreCase("ncbi")) {
			return -1;
		}
		return o1.compareTo(o2);
	}
}
