package com.novelbio.generalConf;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;

public class Species {
	private static Logger logger = Logger.getLogger(Species.class);
	/**
	 * 保存配置的文件，为tab分割的txt文本
	 */
	private final static String ConfigFile = "";
	
	/**
	 * 名字写法规范，物种名缩写--也就是KEGG的缩写方法，下划线，物种版本 
	 */
	/**
	 * 人类
	 */
	public final static String HUMAN = "human";
	/**
	 * 小鼠
	 */
	public final static String MOUSE = "mouse";

	/**
	 * 拟南芥
	 */
	public final static String ARABIDOPSIS = "arabidopsis";
	/**
	 * 水稻
	 */
	public final static String RICE = "rice";
	/**
	 * 大豆
	 */
	public final static String Gmax = "gmax";
	/**
	 * 人类第19版本
	 */
	public final static String HUMAN_HG19 = "human_ucsc_hg19";
	/**
	 * 人类第18版本
	 */
	public final static String HUMAN_HG18 = "human_ucsc_hg18";
	/**
	 * 小鼠第9版本
	 */
	public final static String MOUSE_MM9 = "mouse_mmu_mm9";
	/**
	 * 拟南芥第9版本
	 */
	public final static String ARABIDOPSIS_TAIR9 = "arabidopsis_tair_9";
	/**
	 * 拟南芥第10版本
	 */
	public final static String ARABIDOPSIS_TAIR10 = "arabidopsis_tair_10";
	/**
	 * 水稻TIGR6.1版本
	 */
	public final static String RICE_TIGR6_1 = "rice_tigr_6.1";
	
	/**
	 * key:物种名，来自上面定义的下划线前的缩写
	 * value：物种的类
	 */
	static HashMap<String, Species> hashSpecies = new HashMap<String, Species>();
	
	
	
	
	
	
	String species = null;
	String species_abbr = null;
	/**
	 * 本次设定的database
	 */
	String dbName = null;
	/**
	 * 本次设定的版本
	 */
	String dbVersion = null;
	/**
	 * key:数据库名
	 * value：数据库版本
	 * 多个数据库
	 * 每个数据库对应多个版本
	 */
	HashMap<String, HashSet<String>> hashDBInfo = new HashMap<String, HashSet<String>>(); 
	/**
	 * key:数据库名_数据库版本
	 * value：gff文件的路径
	 */
	HashMap<String, String> hashGff = new HashMap<String, String>();
	/**
	 * key:数据库名_数据库版本
	 * value：gtf文件的路径
	 */
	HashMap<String, String> hashGtf = new HashMap<String, String>();
	/**
	 * key:数据库名_数据库版本
	 * value：染色体文件夹路径，一个文件夹下多个文件
	 */
	HashMap<String, String> hashChrom_Path = new HashMap<String, String>();
	/**
	 * key:数据库名_数据库版本
	 * value：染色体文件路径，一个文件
	 */
	HashMap<String, String> hashChrom_File = new HashMap<String, String>();
	/**
	 * key:数据库名_数据库版本
	 * value：染色体文件路径，一个文件
	 */
	HashMap<String, String> hashStatistic_File = new HashMap<String, String>();
	/**
	 * 物种全名
	 * @return
	 */
	public String getSpecies() {
		return species;
	}
	/**
	 * 物种缩写
	 */
	public String getSpeciesAbbr() {
		return species_abbr;
	}
	
	public String getGFFfilePath() {
		String speciesDB = species+"_"+dbName+"_"+dbVersion;
		return hashGff.get(speciesDB);
	}
	
	public String getGTFfilePath() {
		String speciesDB = species+"_"+dbName+"_"+dbVersion;
		return hashGtf.get(speciesDB);
	}
	/**
	 * 输入物种和数据库，本类的常量里面选
	 * @param speciesInfo
	 */
	public Species getInstance(String speciesInfo) {
		String[] ss = speciesInfo.split("\t");
		HashMap<String, Species> hashSpecies = getHashSpecies();
		Species species = hashSpecies.get(ss[0]);
		species.dbName = ss[1];
		species.dbVersion = ss[2];
		return species;
	}
	
	private Species()
	{}
	/**
	 * 读取Species的配置文件
	 */
	public HashMap<String, Species> getHashSpecies()
	{
		if (hashSpecies != null) {
			return hashSpecies;
		}
		TxtReadandWrite txtSpecies = new TxtReadandWrite();
		try {
			BufferedReader reader = txtSpecies.readfile();
			String content = ""; 
			//是不是读到第一行head行
			int flag = 0;
			//保存每列的名字属性
			ArrayList<String> lsAttribute = new ArrayList<String>();
			
			while ((content = reader.readLine()) != null) {
				if (content.trim().startsWith("#")||content.trim().equals("")) {
					continue;
				}
				content.toLowerCase();
				flag++ ;
				//如果读到了head行
				if (flag == 1 ) {
					String[] ss = content.trim().split("\t");
					for (String string : ss) {
						lsAttribute.add(string.trim().toLowerCase());//转为小写
					}
					continue;
				}
				
				String[] ss = content.trim().split("\t");
				Species mySpecies = null; String dbName = ""; String dbVersion = "";
				for (int i = 0; i < ss.length; i++) {
					if (lsAttribute.get(i).equals("species")) {
						if (!hashSpecies.containsKey(ss[i])) {
							mySpecies = hashSpecies.get(ss[i]);
						}
						else {
							mySpecies = new Species();
							hashSpecies.put(ss[i], mySpecies);
						}
						mySpecies.setSpecies(ss[i]);
					}
					if (lsAttribute.get(i).equals("database")) {
						dbName = ss[i];
					}
					if (lsAttribute.get(i).equals("version")) {
						dbVersion = ss[i];
					}
				}
				String dbInfo = dbName+"_"+dbVersion;
				mySpecies.setDB(dbName, dbVersion);
				for (int i = 0; i < ss.length; i++) {
					
					if (lsAttribute.get(i).equals("species_abbr")) {
						mySpecies.setSpeciesAbbr(ss[i]);
					}
					if (lsAttribute.get(i).equals("chrom_file")) {
						mySpecies.setChrFile(dbInfo, ss[i]);
					}
					if (lsAttribute.get(i).equals("chrom_path")) {
						mySpecies.setChrPath(dbInfo, ss[i]);
					}
					if (lsAttribute.get(i).equals("gff_file")) {
						mySpecies.setGFF(dbInfo, ss[i]);
					}
					if (lsAttribute.get(i).equals("gtf_file")) {
						mySpecies.setGTF(dbInfo, ss[i]);
					}
					if (lsAttribute.get(i).equals("statisticInfo")) {
						mySpecies.setStatisticFile(dbInfo, ss[i]);
					}
				}
			}
		} catch (Exception e) {
			logger.error("物种配置文件出错，请查看文件 "+ConfigFile);
			e.printStackTrace();
		}
		return hashSpecies;
	}
	
	
	
	private void setSpecies(String spName) {
		this.species = spName;
	}
	private void setSpeciesAbbr( String spNameAbbr) {
		this.species_abbr = spNameAbbr;
	}
	private void setDB(String dbName, String dbVersion) {
		if (dbName == null) {
			dbName = dbName;
			hashDBInfo.put(dbName, new HashSet<String>());
		}
		if (dbVersion == null) {
			this.dbVersion = dbVersion;
			HashSet<String> dbVersionSet = hashDBInfo.get(dbName);
			dbVersionSet.add(dbVersion);
		}
	}
	private void setGFF( String dbInfo, String GffFile) {
		hashGff.put(dbInfo,GffFile);
	}
	private void setGTF(String dbInfo, String GtfFile) {
		hashGtf.put(dbInfo,GtfFile);
	}
	private void setChrFile( String dbInfo, String ChrFile) {
		hashChrom_File.put(dbInfo,ChrFile);
	}
	private void setChrPath(String dbInfo, String ChrPath) {
		hashChrom_Path.put(dbInfo,ChrPath);
	}
	private void setStatisticFile(String dbInfo, String StatisticFile) {
		hashStatistic_File.put(dbInfo,StatisticFile);
	}
}
