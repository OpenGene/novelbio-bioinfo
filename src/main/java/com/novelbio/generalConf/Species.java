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
	 * �������õ��ļ���Ϊtab�ָ��txt�ı�
	 */
	private final static String ConfigFile = "";
	
	/**
	 * ����д���淶����������д--Ҳ����KEGG����д�������»��ߣ����ְ汾 
	 */
	/**
	 * ����
	 */
	public final static String HUMAN = "human";
	/**
	 * С��
	 */
	public final static String MOUSE = "mouse";

	/**
	 * ���Ͻ�
	 */
	public final static String ARABIDOPSIS = "arabidopsis";
	/**
	 * ˮ��
	 */
	public final static String RICE = "rice";
	/**
	 * ��
	 */
	public final static String Gmax = "gmax";
	/**
	 * �����19�汾
	 */
	public final static String HUMAN_HG19 = "human_ucsc_hg19";
	/**
	 * �����18�汾
	 */
	public final static String HUMAN_HG18 = "human_ucsc_hg18";
	/**
	 * С���9�汾
	 */
	public final static String MOUSE_MM9 = "mouse_mmu_mm9";
	/**
	 * ���Ͻ��9�汾
	 */
	public final static String ARABIDOPSIS_TAIR9 = "arabidopsis_tair_9";
	/**
	 * ���Ͻ��10�汾
	 */
	public final static String ARABIDOPSIS_TAIR10 = "arabidopsis_tair_10";
	/**
	 * ˮ��TIGR6.1�汾
	 */
	public final static String RICE_TIGR6_1 = "rice_tigr_6.1";
	
	/**
	 * key:���������������涨����»���ǰ����д
	 * value�����ֵ���
	 */
	static HashMap<String, Species> hashSpecies = new HashMap<String, Species>();
	
	
	
	
	
	
	String species = null;
	String species_abbr = null;
	/**
	 * �����趨��database
	 */
	String dbName = null;
	/**
	 * �����趨�İ汾
	 */
	String dbVersion = null;
	/**
	 * key:���ݿ���
	 * value�����ݿ�汾
	 * ������ݿ�
	 * ÿ�����ݿ��Ӧ����汾
	 */
	HashMap<String, HashSet<String>> hashDBInfo = new HashMap<String, HashSet<String>>(); 
	/**
	 * key:���ݿ���_���ݿ�汾
	 * value��gff�ļ���·��
	 */
	HashMap<String, String> hashGff = new HashMap<String, String>();
	/**
	 * key:���ݿ���_���ݿ�汾
	 * value��gtf�ļ���·��
	 */
	HashMap<String, String> hashGtf = new HashMap<String, String>();
	/**
	 * key:���ݿ���_���ݿ�汾
	 * value��Ⱦɫ���ļ���·����һ���ļ����¶���ļ�
	 */
	HashMap<String, String> hashChrom_Path = new HashMap<String, String>();
	/**
	 * key:���ݿ���_���ݿ�汾
	 * value��Ⱦɫ���ļ�·����һ���ļ�
	 */
	HashMap<String, String> hashChrom_File = new HashMap<String, String>();
	/**
	 * key:���ݿ���_���ݿ�汾
	 * value��Ⱦɫ���ļ�·����һ���ļ�
	 */
	HashMap<String, String> hashStatistic_File = new HashMap<String, String>();
	/**
	 * ����ȫ��
	 * @return
	 */
	public String getSpecies() {
		return species;
	}
	/**
	 * ������д
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
	 * �������ֺ����ݿ⣬����ĳ�������ѡ
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
	 * ��ȡSpecies�������ļ�
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
			//�ǲ��Ƕ�����һ��head��
			int flag = 0;
			//����ÿ�е���������
			ArrayList<String> lsAttribute = new ArrayList<String>();
			
			while ((content = reader.readLine()) != null) {
				if (content.trim().startsWith("#")||content.trim().equals("")) {
					continue;
				}
				content.toLowerCase();
				flag++ ;
				//���������head��
				if (flag == 1 ) {
					String[] ss = content.trim().split("\t");
					for (String string : ss) {
						lsAttribute.add(string.trim().toLowerCase());//תΪСд
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
			logger.error("���������ļ�������鿴�ļ� "+ConfigFile);
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
