package com.novelbio.generalConf;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.HttpFetch;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class PathNBCDetail {
	private static final Logger logger = Logger.getLogger(PathNBCDetail.class);
	static String workSpace;
	static Map<String, String> mapID2Path = new LinkedHashMap<String, String>();
	static {
		initial();
	}
	private static void initial() {
		String pathFile = getRworkspace() + "NBCPath.txt";
		if (!FileOperate.isFileExist(pathFile)) {
			logger.error(pathFile);
			return;
		}
		
		mapID2Path.clear();
		TxtReadandWrite txtRead = new TxtReadandWrite(getRworkspace() + "NBCPath.txt", false);		
		for (String string : txtRead.readlines()) {
			string = string.trim();
			if (string.startsWith("#")) {
				continue;
			}
			String[] ss = string.split("\t");
			if (ss.length < 2) {
				continue;
			}
			mapID2Path.put(ss[0], ss[1]);
		}
		if (mapID2Path.containsKey("TMPpath")) {
			setTmpDir(mapID2Path.get("TMPpath"));
		}
		txtRead.close();
	}
	
	public static void setWorkSpace(String workSpace) {
		PathNBCDetail.workSpace = workSpace;
		initial();
	}
	
	/** 返回jar所在的路径 */
	public static String getProjectPath() {
		if (workSpace == null || workSpace.equals("")) {
			java.net.URL url = PathNBCDetail.class.getProtectionDomain().getCodeSource().getLocation();
			String filePath = null;
			try {
				filePath = HttpFetch.decode(url.getPath());
			} catch (Exception e) {
				e.printStackTrace();
			}
			workSpace = FileOperate.getParentPathName(filePath);
		}
		workSpace = FileOperate.addSep(workSpace);
		return workSpace;
	}
	
	/** 返回jar内部路径 */
	public static String getProjectPathInside() {
		java.net.URL url = PathNBCDetail.class.getProtectionDomain().getCodeSource().getLocation();
		String filePath = null;
		try {
			filePath = HttpFetch.decode(url.getPath());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return FileOperate.addSep(filePath);
	}
	/** 返回jar所在的路径，路径分隔符都为"/" */
	public static String getProjectPathLinux() {
		java.net.URL url = PathNBCDetail.class.getProtectionDomain().getCodeSource().getLocation();
		String filePath = null;
		try {
			filePath = HttpFetch.decode(url.getPath());
		} catch (Exception e) {
			e.printStackTrace();
		}
		filePath = FileOperate.getParentPathName(filePath);
		return FileOperate.addSep(filePath).replace("\\", "/");
	}
	/** 零时文件的文件夹，没有就创建一个 */
	public static String getProjectConfPath() {
		String fold = PathNBCDetail.getProjectPath() + "ConfFold" + FileOperate.getSepPath();
		if (!FileOperate.isFileFoldExist(fold)) {
			FileOperate.createFolders(fold);
		}
		return fold;
	}
	
	/** 设定java的临时文件夹 */
	public static void setTmpDir(String filePath) {
		File f = new File(filePath);
        if (!f.exists()) f.mkdirs();
        f.setReadable(true, false);
        f.setWritable(true, false);
        System.setProperty("java.io.tmpdir", f.getAbsolutePath()); // in loop so that last one takes effect
	}
	
	public static String getRworkspace() {
		return getProjectPath() + "rscript"  + FileOperate.getSepPath();
	}

	public static String getRworkspaceTmp() {
		String fileName = getRworkspace() + "tmp"  + FileOperate.getSepPath();
		FileOperate.createFolders(fileName);
		return fileName;
	}
	
	/** 内部自动加空格 */
	public static String getRscript() {
		return mapID2Path.get("R_SCRIPT") + " ";
	}
	
	/** 内部自动加空格 */
	public static String getSpeciesFile() {
		return mapID2Path.get("SpeciesFile");
	}
	
	public static String getMiRNADat() {
		return mapID2Path.get("miRNAdat");
	}
	
	/** NCBI的基因互作表 */
	public static String getNCBIinteract() {
		return mapID2Path.get("NCBIinteract");
	}
	
	/** COGfasta文件的路径 */
	public static String getCOGfastaFile() {
		return mapID2Path.get("COGfasta");
	}
	
	/** 一个大的能容纳一些中间过程的文件夹，包含"/" */
	public static String getTmpPath() {
		return FileOperate.addSep(mapID2Path.get("TMPpath"));
	}
	
	/** rfam的对照表文件<br>
	 * 这个样子的<br>
	 * 1	1	RF00001	5S_rRNA	5S ribosomal RNA	Griffiths-Jones SR, Mifsud W, Gardner PP	Szymanski et al, 5S ribosomal database, PMID:11752286	\N	16.00	22.36	\N	5S ribosomal RNA (5S rRNA) is a component of the large ribosomal subunit in both prokaryotes and eukaryotes. In eukaryotes, it is synthesised by RNA polymerase III (the other eukaryotic rRNAs are cleaved from a 45S precursor synthesised by RNA polymerase I).  In Xenopus oocytes, it has been shown that fingers 4-7 of the nine-zinc finger transcription factor TFIIIA can bind to the central region of 5S RNA. Thus, in addition to positively regulating 5S rRNA transcription, TFIIIA also stabilises 5S rRNA until it is required for transcription.  	\N	cmbuild  -F CM SEED	cmcalibrate --mpi -s 1 CM	cmsearch  -Z 169604 -E 1000  --toponly  CM SEQDB	712	116760	Gene;rRNA;	Published; PMID:11283358	366	91	4365	Eukaryota; Bacteria; Viruses; Archaea;	Eukaryota; Bacteria; Viruses; Archaea;	<<<..<...<....<....<....<...<.......................................<..<..............<...<.....<....<....<.............................<..<.........................<..<...<..<...<.<..........................................................................................>.>..........>...>....>....>..................>..>.................................>..>...>......>....>..................>....>....<.......<...<...............<<............<............<.<..................................<..<.......................................................................>..>....................>.>................>............>>..............>..>....>...........>....>....>....>...>...>..>.>>.	<<<..<...<....<....<....<...<.......................................<..<..............<...<.....<....<....<.............................<..<.........................<..<...<..<...<.<..........................................................................................>.>..........>...>....>....>..................>..>.................................>..>...>......>....>..................>....>....<.......<...<...............<<............<............<.<..................................<..<.......................................................................>..>....................>.>................>............>>..............>..>....>...........>....>....>....>...>...>..>.>>.	RBY--U---R----Y----R----R---Y------C---------A--------U-------A-----C--C-------------AS---C-----M----Y----K--------A---A------------H---R--Y-------R-----------C-----C--S---G--A---U-C---Y-C-R-U-C-H----------------------------------------------------------------------------G-A--W--C----U---C----S----G-M-------A--------G--Y---U--A-----A----G---------------C--R---K------G----S--UY--------------G----G----G-------C---S--------------WGRKUA--------GU------A-----C-U---U-------G-----G-----A-------U--G--G--G-W-GA---------------------------------------------------------------C--C---WCY--U----R------G-G----A-----------A----KW------CYW-------------G--G----U-----------G----Y----U----G---Y---A--V-SCW	\N
	 *  */
	public static String getRfamTab() {
		return mapID2Path.get("rfamTab");
	}
	/** Rfam的序列文件<br>
	 * >RF00001;5S_rRNA;DQ397844.1/16860-16979 414005:Cenarchaeum symbiosum B<br>
	 * CAAGCCGGCCAUAGCGUCAGGGUGCGACCCAAUCCCAUUCCGAACUUGGAAGUCAAACCU
	 * @return
	 */
	public static String getRfamSeq() {
		return mapID2Path.get("rfamSeq");
	}
}
