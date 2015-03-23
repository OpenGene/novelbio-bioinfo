package com.novelbio.generalConf;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import com.jhlabs.image.MapColorsFilter;
import com.novelbio.analysis.annotation.cog.EnumCogType;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.base.PathDetail;
import com.novelbio.base.fileOperate.FileOperate;

public class PathDetailNBC {
	private static final Logger logger = Logger.getLogger(PathDetailNBC.class);
	static Properties properties;
	static {
		initial();
	}
	private static void initial() {
		ClassPathResource resource = new ClassPathResource("path.properties", PathDetailNBC.class);
		try {
			properties = PropertiesLoaderUtils.loadProperties(resource);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

//		InputStream in = PathDetailNBC.class.getClassLoader().getResourceAsStream("path.properties");
//		properties = new Properties();
//		try {
//			properties.load(in);
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		} finally{
//			try {
//				in.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
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
	
	public static Map<String, String> getMapReadsQuality() {
		Map<String, String> mapReadsQualtiy = new LinkedHashMap<String, String>();
		String value = properties.getProperty("FastQ_Levels");
		for (String qualityInfo : value.split(";")) {
			mapReadsQualtiy.put(qualityInfo.replace("FastQ_QUALITY_", ""), qualityInfo);
		}
		return mapReadsQualtiy;
	}
	
	/** 从上面的那个map中获得value，当作key来查找具体的quality map<br>
	 * 格式类似如下：<br>
	 * FastQ_QUALITY_HIGH=10,0.07;13,0.07;20,0.15<br>
	 *  10以下不得超过0.07的碱基比例<br>
	 *  13以下不得超过0.07的碱基比例<br>
	 *  20以下不得超过0.15的碱基比例<br>
	 */
	public static Map<Integer, Double> getMapQuality2Num(String QUALITY) {
		Map<Integer, Double> mapQuality2CutoffNum = new HashMap<Integer, Double>();
		String value = properties.getProperty(QUALITY);
		if (value.equalsIgnoreCase("ChangeToBest") || value.equalsIgnoreCase("NotFilter")) {
			return mapQuality2CutoffNum;
		}
		String[] ss = value.split(";");
		for (String quality2property : ss) {
			String[] quality2propertyArray = quality2property.split(",");
			mapQuality2CutoffNum.put(Integer.parseInt(quality2propertyArray[0]), Double.parseDouble(quality2propertyArray[1]));
		}
		return mapQuality2CutoffNum;
	}
	
}
