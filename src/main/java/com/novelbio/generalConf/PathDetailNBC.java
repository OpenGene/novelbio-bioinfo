package com.novelbio.generalConf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.assertj.core.util.VisibleForTesting;

import com.novelbio.analysis.annotation.cog.EnumCogType;
import com.novelbio.base.PathDetail;
import com.novelbio.base.fileOperate.FileOperate;

public class PathDetailNBC {
	private static final Logger logger = Logger.getLogger(PathDetailNBC.class);
	static Properties properties;
	static {
		initial();
	}
	
	/** 仅用于测试 */
	@VisibleForTesting
	public static void setProperties(String pathOfPropertiesFile) throws Exception {
		File f = new File(pathOfPropertiesFile);
		InputStream in = new FileInputStream(f);
		properties = new Properties();
		try {
			properties.load(in);
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new RuntimeException(e1);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void initial() {
		String configPath = "Genome.Info";
		InputStream in = PathDetailNBC.class.getClassLoader().getResourceAsStream(configPath);
		properties = new Properties();
		try {
			properties.load(in);
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new RuntimeException(e1);
		} finally{
			try {
				if(in != null){
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/** 有最后的"/" */
	public static String getGenomePath() {
		String genomePath = properties.getProperty("GenomePath");
		if (!genomePath.startsWith("/") && !genomePath.startsWith("\\")) {
			if (!FileOperate.isFileDirectory(genomePath)) {
				genomePath = PathDetail.getProjectPathLinux() + genomePath;
			}
		}
		
		return FileOperate.addSep(genomePath);
	}
	
	/** 内部自动加空格 */
	public static String getSpeciesFile() {
		String speciesFile = properties.getProperty("SpeciesFile");
		if (!speciesFile.startsWith("/") && !speciesFile.startsWith("\\")) {
			if (!FileOperate.isFileExistAndBigThanSize(speciesFile, 0)) {
				speciesFile = PathDetail.getProjectPathLinux() + speciesFile;
			}
		}
		return speciesFile;
	}
	
	public static String getMiRNADat() {
		return properties.getProperty("miRNAdat");
	}
	
	/** NCBI的基因互作表 */
	public static String getNCBIinteract() {
		return properties.getProperty("NCBIinteract");
	}
	
	/** COGfasta文件的路径 */
	public static String getCogFasta(EnumCogType cogType) {
		String cogFile = null;
		if (cogType == EnumCogType.COG) {
			cogFile = properties.getProperty("COGfasta");
		} else if (cogType == EnumCogType.KOG) {
			cogFile = properties.getProperty("KOGfasta");
		}
		return cogFile;
	}
	/** COGfasta文件的路径 */
	public static String getCogId2Anno(EnumCogType cogType) {
		String cogFile = null;
		if (cogType == EnumCogType.COG) {
			cogFile = properties.getProperty("cogId2Anno");
		} else if (cogType == EnumCogType.KOG) {
			cogFile = properties.getProperty("kogId2Anno");
		}
		return cogFile;
	}
	/** COGfasta文件的路径 */
	public static String getCogPro2CogId(EnumCogType cogType) {
		String cogFile = null;
		if (cogType == EnumCogType.COG) {
			cogFile = properties.getProperty("pro2cog");
		} else if (cogType == EnumCogType.KOG) {
			cogFile = properties.getProperty("pro2kog");
		}
		return cogFile;
	}
	/** COGfasta文件的路径 */
	public static String getCogAbbr2Fun(EnumCogType cogType) {
		String cogFile = null;
		if (cogType == EnumCogType.COG) {
			cogFile = properties.getProperty("cogAbbr2Fun");
		} else if (cogType == EnumCogType.KOG) {
			cogFile = properties.getProperty("kogAbbr2Fun");
		}
		return cogFile;
	}
	
	/** software配置文件的路径 */
	public static String getSoftwareInfo() {
		if (!properties.contains("Software")) {
			return null;
		}
		String software = properties.getProperty("Software");
		if (!software.startsWith("/") && !software.startsWith("\\")) {
			if (!FileOperate.isFileExistAndBigThanSize(software, 0)) {
				software = PathDetail.getProjectPathLinux() + software;
			}
		}
		return software;
	}
	
	/** rfam的对照表文件<br>
	 * 这个样子的<br>
	 * 1	1	RF00001	5S_rRNA	5S ribosomal RNA	Griffiths-Jones SR, Mifsud W, Gardner PP	Szymanski et al, 5S ribosomal database, PMID:11752286	\N	16.00	22.36	\N	5S ribosomal RNA (5S rRNA) is a component of the large ribosomal subunit in both prokaryotes and eukaryotes. In eukaryotes, it is synthesised by RNA polymerase III (the other eukaryotic rRNAs are cleaved from a 45S precursor synthesised by RNA polymerase I).  In Xenopus oocytes, it has been shown that fingers 4-7 of the nine-zinc finger transcription factor TFIIIA can bind to the central region of 5S RNA. Thus, in addition to positively regulating 5S rRNA transcription, TFIIIA also stabilises 5S rRNA until it is required for transcription.  	\N	cmbuild  -F CM SEED	cmcalibrate --mpi -s 1 CM	cmsearch  -Z 169604 -E 1000  --toponly  CM SEQDB	712	116760	Gene;rRNA;	Published; PMID:11283358	366	91	4365	Eukaryota; Bacteria; Viruses; Archaea;	Eukaryota; Bacteria; Viruses; Archaea;	<<<..<...<....<....<....<...<.......................................<..<..............<...<.....<....<....<.............................<..<.........................<..<...<..<...<.<..........................................................................................>.>..........>...>....>....>..................>..>.................................>..>...>......>....>..................>....>....<.......<...<...............<<............<............<.<..................................<..<.......................................................................>..>....................>.>................>............>>..............>..>....>...........>....>....>....>...>...>..>.>>.	<<<..<...<....<....<....<...<.......................................<..<..............<...<.....<....<....<.............................<..<.........................<..<...<..<...<.<..........................................................................................>.>..........>...>....>....>..................>..>.................................>..>...>......>....>..................>....>....<.......<...<...............<<............<............<.<..................................<..<.......................................................................>..>....................>.>................>............>>..............>..>....>...........>....>....>....>...>...>..>.>>.	RBY--U---R----Y----R----R---Y------C---------A--------U-------A-----C--C-------------AS---C-----M----Y----K--------A---A------------H---R--Y-------R-----------C-----C--S---G--A---U-C---Y-C-R-U-C-H----------------------------------------------------------------------------G-A--W--C----U---C----S----G-M-------A--------G--Y---U--A-----A----G---------------C--R---K------G----S--UY--------------G----G----G-------C---S--------------WGRKUA--------GU------A-----C-U---U-------G-----G-----A-------U--G--G--G-W-GA---------------------------------------------------------------C--C---WCY--U----R------G-G----A-----------A----KW------CYW-------------G--G----U-----------G----Y----U----G---Y---A--V-SCW	\N
	 *  */
	public static String getRfamTab() {
		return properties.getProperty("rfamTab");
	}
	/** Rfam的序列文件<br>
	 * >RF00001;5S_rRNA;DQ397844.1/16860-16979 414005:Cenarchaeum symbiosum B<br>
	 * CAAGCCGGCCAUAGCGUCAGGGUGCGACCCAAUCCCAUUCCGAACUUGGAAGUCAAACCU
	 * @return
	 */
	public static String getRfamSeq() {
		return properties.getProperty("rfamSeq");
	}
	
	/**
	 * 获取物种索引文件的根路径<br>
	 * 以"/"结尾
	 */
	public static String getIndexPath() {
		return getGenomePath() + "index/";
	}
}
