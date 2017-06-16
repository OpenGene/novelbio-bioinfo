package com.novelbio.analysis.seq.genome.gffOperate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.genome.ExceptionNbcGFF;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.Alignment;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.modgeneid.GeneType;

/**
 * 应该是标准的gff3格式，仅用于NCBI的gff3文件
 * 
 * 获得Gff的基因数组信息,本类必须实例化才能使用<br/>
 * 输入Gff文件，最后获得两个哈希表和一个list表, 结构如下：<br/>
 * 1.hash（ChrID）--ChrList--GffDetail(GffDetail类,实际是GffDetailUCSCgene子类)<br/>
 * 其中ChrID为小写，代表染色体名字，因此用get来获取相应的ChrList的时候要输入小写的ChrID chr格式，全部小写
 * chr1,chr2,chr11<br/>
 * 
 * 2.hash（LOCID）--GffDetail，其中LOCID代表具体的基因编号 <br/>
 * 
 * 3.list（LOCID）--LOCList，按顺序保存LOCID<br/>
 * 
 * 每个基因的起点终点和CDS的起点终点保存在GffDetailList类中<br/>
 */
public class GffHashGeneNCBI extends GffHashGeneAbs {
	private static final Logger logger = LoggerFactory
			.getLogger(GffHashGeneNCBI.class);

	/** 基因名字的正则，可以改成识别人类或者其他,这里是拟南芥，默认 NCBI的ID */
	protected static String regGeneName = "(?<=gene\\=)[\\w\\-%\\:\\.\\{\\}\\(\\)]+";
	/** 可变剪接mRNA的正则，默认 NCBI的ID */
	protected static String regSplitmRNA = "(?<=transcript_id\\=)[\\w\\-\\.]+|(?<=stable_id\\=)[\\w\\-\\.]+";
	/** 可变剪接mRNA的产物的正则，默认 NCBI的symbol */
	protected static String regProduct = "(?<=product\\=)[\\w\\-%]+";
	/** geneID的正则 */
	protected static String regGeneID = "(?<=Dbxref\\=GeneID\\:)\\d+";
	/** Name的正则 */
	protected static String regName = "(?<=(\\W|^)Name\\=)[\\w\\-%\\.\\:\\{\\}\\(\\)]+";
	/** ID的正则 */
	protected static String regID = "(?<=ID\\=)[\\w\\.\\-%\\:\\{\\}]+";
	/** parentID的正则 */
	protected static String regParentID = "(?<=Parent\\=)[\\w\\.\\-%\\:\\{\\}\\(\\)]+";

	/** gene类似名 */
	private static Set<String> setIsGene = new HashSet<String>();
	/** gene类似名 */
	private static Set<String> setIsChromosome = new HashSet<String>();

	/** "(?<=gene\\=)\\w+" */
	PatternOperate patGeneName = null;
	/** "(?<=transcript_id\\=)\\w+" */
	PatternOperate patmRNAName = null;
	/** "(?<=Dbxref\\=GeneID\\:)\\d+" */
	PatternOperate patGeneID = null;
	/** "(?<=Name\\=)\\w+" */
	PatternOperate patName = null;
	/** "(?<=ID\\=)\\w+" */
	PatternOperate patID = null;
	/** "(?<=Parent\\=)\\w+" */
	PatternOperate patParentID = null;
	/** "(?<=product\\=)\\w+" */
	PatternOperate patProduct = null;

	private Map<String, String> mapRnaID2GeneID = new HashMap<String, String>();
	private Map<String, GffDetailGene> mapGenID2GffDetail = new LinkedHashMap<String, GffDetailGene>();

	/**
	 * 这两个是一对，一个是rnaID对应多个iso，常见于TRNA 另一个这个存储ISO对应的坐标
	 */
	private Map<String, GffGeneIsoInfo> mapRnaID2Iso = new HashMap<>();
	private ArrayListMultimap<String, ExonInfo> mapRnaID2LsIsoLocInfo = ArrayListMultimap.create();
	private Map<String, Align> mapGeneID2Region = new HashMap<>();
	private GffGetChrId gffGetChrId = new GffGetChrId();

	/** 发现重复的mRNA名字时，就换一个名字，专用于果蝇 */
	boolean isFilterDuplicateName = false;
	/** 发现重复的mRNA名字时，就换一个名字，专用于果蝇，里面的值都是小写 */
	private Set<String> setMrnaNameDuplicate = new HashSet<>();

	/**
	 * 一般的转录本都会先出现exon，然后出现CDS，如下<br>
	 * hr3 RefSeq mRNA 59958839 59959481<br>
	 * chr3 RefSeq exon 59959427 59959481<br>
	 * chr3 RefSeq exon 59958839 59959233<br>
	 * chr3 RefSeq CDS 59959427 59959481<br>
	 * chr3 RefSeq CDS 59958839 59959233<br>
	 * 但是有些转录本不会出现exon，但是后面会出现CDS，如下<br>
	 * chr3 RefSeq gene 59962472 59963232<br>
	 * chr3 RefSeq V_gene_segment 59963181 59963232<br>
	 * chr3 RefSeq V_gene_segment 59962472 59962797<br>
	 * chr3 RefSeq CDS 59963181 59963229<br>
	 * chr3 RefSeq CDS 59962472 59962797<br>
	 * 那么本map就用来记录该转录本是否出现了exon，如果出现了exon，CDS就只用来设定ATG和UAG。
	 * 如果没有出现exon，CDS就要当exon来设定。
	 */
	private Map<String, Boolean> mapGeneName2IsHaveExon = new HashMap<String, Boolean>();

	int numCopedIDsearch = 0;// 查找taxID的次数最多10次

	/** 默认连上数据库 */
	boolean database = true;

	/** 发现重复的mRNA名字时，就换一个名字，专用于果蝇 */
	public void setFilterDuplicateName(boolean isFilterDuplicateName) {
		this.isFilterDuplicateName = isFilterDuplicateName;
	}

	/** 设定mRNA和gene的类似名，在gff文件里面出现的 */
	private void setGeneName() {
		if (setIsGene.isEmpty()) {
			setIsGene.add("gene");
			setIsGene.add("transposable_element_gene");
			setIsGene.add("protein_coding_gene");
			setIsGene.add("pseudogene");
			// setIsGene.add("tRNA");
		}
		if (setIsChromosome.isEmpty()) {
			setIsChromosome.add("chromosome");
		}
	}
	
	/**
	 * 是否把NCBI-Gff中的NC_1234修改为chr1
	 * @param isChangeChrId 默认为true
	 */
	public void setChangeChrId(boolean isChangeChrId) {
		gffGetChrId.setChangeChrId(isChangeChrId);
	}
	
	private void setPattern() {
		patGeneName = new PatternOperate(regGeneName, false);
		patmRNAName = new PatternOperate(regSplitmRNA, false);
		patProduct = new PatternOperate(regProduct, false);

		patGeneID = new PatternOperate(regGeneID, false);
		patID = new PatternOperate(regID, false);
		patName = new PatternOperate(regName, false);
		patParentID = new PatternOperate(regParentID, false);
	}

	/**
	 * 最底层读取gff的方法，本方法只能读取UCSCknown gene<br>
	 * 输入Gff文件，最后获得两个哈希表和一个list表<br/>
	 * 结构如下：<br/>
	 * 输入Gff文件，最后获得两个哈希表和一个list表, 结构如下：<br>
	 * <b>1.Chrhash</b><br>
	 * （ChrID）--ChrList-- GeneInforList(GffDetail类)
	 * 其中ChrID为小写，代表染色体名字，因此用get来获取相应的ChrList的时候要输入小写的ChrID, chr格式，全部小写
	 * chr1,chr2,chr11<br>
	 * <b>2.locHashtable</b><br>
	 * 其中LOCID代表具体的条目编号，在UCSCkonwn gene里面没有转录本一说，
	 * 只有两个LOCID共用一个区域的情况，所以只能够两个不同的LOCID指向同一个GffdetailUCSCgene
	 * <b>3.LOCIDList</b><br>
	 * （LOCID）--LOCIDList，按顺序保存LOCID,这里不考虑多个转录本，每一个转录本就是一个单独的LOCID <br>
	 * <b>4. LOCChrHashIDList </b><br>
	 * LOCChrHashIDList中保存LOCID代表具体的条目编号,与Chrhash里的名字一致，将同一基因的多个转录本放在一起：
	 * NM_XXXX/NM_XXXX...<br>
	 * 
	 * @throws Exception
	 */
	protected void ReadGffarrayExcepTmp(String gfffilename) {
		setGeneName();
		setPattern();
		TxtReadandWrite txtgff = new TxtReadandWrite(gfffilename, false);

		// 当前的geneID，主要是给tRNA和miRNA用的，因为别的mRNA都有parent geneID可以通过这个ID回溯geneName
		// 但是tRNA和miRNA就没有这个parent geneID，所以就记载下来给他们用
		String[] thisGeneIDandName = null;
		String[] thisRnaIDandName = null;
		if (isFilterDuplicateName) {
			fillDuplicateNameSet();
		}
		for (String content : txtgff.readlines()) {
			if (content.trim().equals("") || content.charAt(0) == '#')
				continue;

			String[] ss = content.split("\t");// 按照tab分开
			if (ss[2].equals("match")
					|| ss[2].toLowerCase().equals("chromosome")
					|| ss[2].toLowerCase().equals("intron")) {
				continue;
			}
			ss[8] = StringOperate.decode(ss[8]);
//			if (content.contains("Ifi202b")) {
//				logger.info("stop");
//			}
			ss[0] = gffGetChrId.getChrID(ss);
			if (ss[2].equals("region"))
				continue;

			// 读取到gene
			if (setIsGene.contains(ss[2])) {
				thisGeneIDandName = addNewGene(ss);
			}
			/**
			 * 当读取到mRNA时，就是说是可变剪接时，添加一个新的可变剪接list
			 * 不管怎么加都是从第一个cds开始加到最后一个cds，正向的话就是从小加到大，反向就是从大加到小。
			 * 一旦出现了mRNA，就要开始指定5UTR，3UTR，CDS的起点和终止
			 */
			else if (GeneType.getMapMRNA2GeneType().containsKey(
					ss[2].toLowerCase())) {
				Align alignRegion = new Align(ss[0], Integer.parseInt(ss[3]),
						Integer.parseInt(ss[4]));
				double[] compareRegion = null;
				if (thisGeneIDandName != null) {
					Align alignGeneRegion = mapGeneID2Region.get(thisGeneIDandName[0]);
					compareRegion = ArrayOperate.cmpArray(
							new double[] { alignRegion.getStartAbs(), alignRegion.getEndAbs() }, new double[] {
									alignGeneRegion.getStartAbs(),
									alignGeneRegion.getEndAbs() });

					if (!alignGeneRegion.getRefID().equalsIgnoreCase(
							alignRegion.getRefID())) {
						thisGeneIDandName = null;
					}
				}
				if (patParentID.getPatFirst(ss[8]) == null
						&& (thisGeneIDandName == null || compareRegion[2] < 0.5)) {
					thisGeneIDandName = addNewGene(ss);
				}

				thisRnaIDandName = addMRNA(thisGeneIDandName, ss);
			} else if (ss[2].equals("exon")) {
				if (!addExon(thisGeneIDandName, thisRnaIDandName, ss)) {
					continue;
				}
			} else if (ss[2].equals("CDS")) {
				addCDS(thisGeneIDandName, thisRnaIDandName, ss);
			} else if (ss[2].equals("STS") || ss[2].contains("gene_segment")
					|| ss[2].contains("contig") || ss[2].contains("match")) {
				continue;
			} else if (ss[2].equals("three_prime_UTR")
					|| ss[2].equals("five_prime_UTR")) {
				continue;
			} else {
				logger.debug("出现未知exon：" + ArrayOperate.cmbString(ss, "\t"));
			}

		}
		setGffList();
		txtgff.close();

		clear();
	}

	private void fillDuplicateNameSet() {
		TxtReadandWrite txtgff = new TxtReadandWrite(gfffilename, false);
		Set<String> setMrnaAll = new HashSet<>();
		for (String content : txtgff.readlines()) {
			if (content.startsWith("#")) {
				continue;
			}
			String[] ss = content.split("\t");
			boolean isMrna = GeneType.getMapMRNA2GeneType().containsKey(
					ss[2].toLowerCase());
			if (isMrna) {
				GeneType mRNAtype = getMrnaName(ss);
				String rnaName = getRnaName(ss, mRNAtype);
				if (rnaName == null)
					continue;
				rnaName = rnaName.toLowerCase();
				if (setMrnaAll.contains(rnaName)) {
					setMrnaNameDuplicate.add(rnaName);
				}
				setMrnaAll.add(rnaName);
			}
		}
		txtgff.close();
	}

	/**
	 * 当读取到gene时，就是读到了一个新的基因，那么新建一个基因 并且返回string[2]<br>
	 * 0: geneID<br>
	 * 1: geneName
	 */
	private String[] addNewGene(String[] ss) {
		// when read the # and the line contains gene, it means the new LOC
		String geneID = ss[0] + patID.getPatFirst(ss[8]);
		String geneName = getGeneName(ss[8]);
		setTaxID(ss, geneName);
		GffDetailGene gffDetailLOC = mapGenID2GffDetail.get(geneID);
		if (gffDetailLOC == null) {
			gffDetailLOC = new GffDetailGene(ss[0], geneName, ss[6].equals("+")
					|| ss[6].equals("."));// 新建一个基因类
		}
		gffDetailLOC.setTaxID(taxID);
		gffDetailLOC.setStartAbs(Integer.parseInt(ss[3]));
		gffDetailLOC.setEndAbs(Integer.parseInt(ss[4]));// 基因起止
		mapGenID2GffDetail.put(geneID, gffDetailLOC);

		mapGeneName2IsHaveExon.put(geneID, false);
		mapGeneID2Region.put(geneID, new Align(ss[0], Integer.parseInt(ss[3]),
				Integer.parseInt(ss[4])));

		return new String[] { geneID, geneName };
	}

	/**
	 * 当读取到mRNA时，就是说是可变剪接时，添加一个新的可变剪接list
	 * 不管怎么加都是从第一个cds开始加到最后一个cds，正向的话就是从小加到大，反向就是从大加到小。
	 * 一旦出现了mRNA，就要开始指定5UTR，3UTR，CDS的起点和终止 并且返回string[2]<br>
	 * 0: rnaID<br>
	 * 1: rnaName
	 */
	private String[] addMRNA(String[] lastGeneIDandName, String[] ss) {
		String rnaID = ss[0] + patID.getPatFirst(ss[8]);
		GeneType mRNAtype = getMrnaName(ss);
		String rnaName = add_MapRnaID2RnaName_And_MapRnaID2GeneID(
				lastGeneIDandName, rnaID, ss, mRNAtype);
		GffDetailGene gffDetailGene = getGffDetailRnaID(rnaID);
		try {
			GffGeneIsoInfo iso= mapRnaID2Iso.get(rnaID);
			if (iso == null) {
				iso = gffDetailGene.addsplitlist(rnaName,
						gffDetailGene.getNameSingle(), mRNAtype, ss[6].equals("+")
								|| ss[6].equals("."));// 每遇到一个mRNA就添加一个可变剪接,先要类型转换为子类
				mapRnaID2Iso.put(rnaID, iso);
			}
	
			ExonInfo exonInfo = new ExonInfo(true, Integer.parseInt(ss[3]),
					Integer.parseInt(ss[4]));
			mapRnaID2LsIsoLocInfo.put(rnaID, exonInfo);
		} catch (Exception e) {
			logger.error("error, need check: " + rnaName, e);
		}
		return new String[] { rnaID, rnaName };

	}

	/**
	 * @param lastGeneIDandName
	 * @param rnaID
	 * @param ss
	 * @return 返回加入的rna名字
	 */
	private String add_MapRnaID2RnaName_And_MapRnaID2GeneID(
			String[] lastGeneIDandName, String rnaID, String[] ss,
			GeneType geneType) {
		String rnaName = getRnaName(ss, geneType);
		if (rnaName != null
				&& setMrnaNameDuplicate.contains(rnaName.toLowerCase())) {
			rnaName = getRnaNameDifToRnaName(rnaName, ss, geneType);
		}
		if (rnaName == null) {
			rnaName = lastGeneIDandName[1];
		}
		// tRNA这种里面是没有parentID的，所以就将其上一行的geneID抓过来就行了
		String geneID = patParentID.getPatFirst(ss[8]);
		if (geneID == null) {
			geneID = lastGeneIDandName[0];
		} else {
			geneID = ss[0] + geneID;
		}
		mapRnaID2GeneID.put(rnaID, geneID);
		return rnaName;
	}

	/**
	 * @param lastGeneIDandName
	 * @param rnaID
	 * @param ss
	 * @return 返回加入的rna名字
	 */
	private String getRnaName(String[] ss, GeneType geneType) {
		// 保证不能出现重复id
		String rnaName = null;
		if (geneType == GeneType.miRNA) {
			rnaName = patProduct.getPatFirst(ss[8]);
			if (rnaName == null)
				rnaName = patmRNAName.getPatFirst(ss[8]);
		} else {
			rnaName = patmRNAName.getPatFirst(ss[8]);
		}

		if (rnaName == null) {
			rnaName = patName.getPatFirst(ss[8]);
		}
		return rnaName;
	}

	/**
	 * @param lastGeneIDandName
	 * @param rnaID
	 * @param ss
	 * @return 返回加入的rna名字
	 */
	private String getRnaNameDifToRnaName(String rnaNameExist, String[] ss,
			GeneType geneType) {
		// 保证不能出现重复id
		String rnaName = null;
		if (geneType == GeneType.miRNA) {
			rnaName = patProduct.getPatFirst(ss[8]);
			if (rnaName == null || rnaNameExist.equals(rnaName))
				rnaName = patmRNAName.getPatFirst(ss[8]);
		} else {
			rnaName = patmRNAName.getPatFirst(ss[8]);
		}

		if (rnaName == null || rnaNameExist.equals(rnaName)) {
			rnaName = patName.getPatFirst(ss[8]);
		}
		if (rnaName == null || rnaNameExist.equals(rnaName)) {
			rnaName = patID.getPatFirst(ss[8]);
		}
		return rnaName;
	}

	private boolean addExon(String[] lastGeneID2Name, String[] lastRnaID2Name,
			String[] ss) {
		String rnaID = getRNAID(lastGeneID2Name, lastRnaID2Name, ss);

		GffGeneIsoInfo gffGeneIsoInfo = null;
		int exonStart = Integer.parseInt(ss[3]);
		int exonEnd = Integer.parseInt(ss[4]);

		try {
			gffGeneIsoInfo = getGffIso(rnaID, exonStart, exonEnd,
					GeneType.ncRNA);// TODO
		} catch (Exception e) {
			logger.error("出现未知exon：" + ArrayOperate.cmbString(ss, "\t"), e);
			return false;
		}
		if (gffGeneIsoInfo == null) {
			return false;
		}
		
		String geneID = getGeneID(rnaID);
		if (mapGeneName2IsHaveExon.get(geneID) == null) {
			logger.error("没有找到相应的GeneID:" + geneID);
		}
		if (mapGeneName2IsHaveExon.get(geneID) == false) {
			gffGeneIsoInfo.addFirstExon(ss[6].equals("+") || ss[6].equals("."),
					exonStart, exonEnd);
			mapGeneName2IsHaveExon.put(geneID, true);
		} else {
			gffGeneIsoInfo.addExon(ss[6].equals("+") || ss[6].equals("."),
					exonStart, exonEnd);
		}

		return true;
	}

	private void addCDS(String[] lastGeneID2Name, String[] lastRnaID2Name,
			String[] ss) {
		int cdsStart = Integer.parseInt(ss[3]);
		int cdsEnd = Integer.parseInt(ss[4]);
		String rnaID = getRNAID(lastGeneID2Name, lastRnaID2Name, ss);
		String geneID = getGeneID(rnaID);
		GffGeneIsoInfo gffGeneIsoInfo = getGffIso(rnaID, cdsStart, cdsEnd, null);
		gffGeneIsoInfo.setATGUAGauto(cdsStart, cdsEnd);
		if (mapGeneName2IsHaveExon.get(geneID) == null) {
			logger.error("没有找到相应的GeneID:" + geneID);
		}
		if (!mapGeneName2IsHaveExon.get(geneID)) {
			gffGeneIsoInfo.addCDS(ss[6].equals("+") || ss[6].equals("."),
					Integer.parseInt(ss[3]), Integer.parseInt(ss[4]));
		}
	}

	/**
	 * 首先查找ss8，看能否找到RNAID，找不到就返回上一个RNAID，再为null就返回上一个geneID
	 * 
	 * @param lastGeneID
	 * @param lastRNAID
	 * @param
	 */
	private String getRNAID(String[] lastGeneID2Name, String[] lastRNAID2Name,
			String[] ss) {
		String rnaID = patParentID.getPatFirst(ss[8]);
		if (rnaID == null) {
			rnaID = lastRNAID2Name[0];
			if (rnaID == null) {
				rnaID = lastGeneID2Name[0];
			}
		} else {
			rnaID = ss[0] + rnaID;
		}
		return rnaID;
	}

	private String getGeneName(String content) {
		String geneName = patGeneName.getPatFirst(content);// 查找基因名字
		String geneID = null;
		String ID = patID.getPatFirst(content);
		if (geneName == null) {
			geneID = patGeneID.getPatFirst(content);
			if (database) {
				try {
					GeneID copedID = new GeneID(GeneID.IDTYPE_GENEID, geneID,
							taxID);
					geneName = copedID.getAccID();
				} catch (Exception e) {
					database = false;
				}
			}
		}
		if (geneName == null) {
			geneName = patName.getPatFirst(content);
		}
		if (geneName == null) {
			geneName = patProduct.getPatFirst(content);
		}
		if (geneName == null) {
			if (geneID == null && ID == null) {
				logger.error("GffHashNCBI: 文件  " + getGffFilename()
						+ "  在本行可能没有指定的基因ID  " + content);
			} else {
				if (geneID != null) {
					geneName = geneID;
				} else {
					geneName = ID;
				}

			}
		}
		return geneName;
	}

	/**
	 * @param content
	 *            相关的某一行
	 * @return string[2] 0: geneName 1: NCBI读取的type
	 */
	private GeneType getMrnaName(String[] content) {
		String result = content[2];// 每遇到一个mRNA就添加一个可变剪接,先要类型转换为子类
		Map<String, String> mapID2value = new HashMap<>();
		for (String string : content[8].split(";")) {
			if (!string.contains("=")) {
				continue;
			}
			String[] info = string.trim().split("=");
			try {
				mapID2value.put(info[0], info[1]);
			} catch (Exception e) {
				mapID2value.put(info[0], info[1]);
			}

		}
		String gbkey = mapID2value.get("gbkey");
		boolean ncRNA = false;
		if (gbkey != null) {
			String ncRNAclass = null;
			if (gbkey.equals("ncRNA")) {
				ncRNA = true;
				ncRNAclass = mapID2value.get("ncrna_class");
			} else if (gbkey.equals("precursor_RNA") || gbkey.equals("RNA")) {
				String product = mapID2value.get("product");
				if (product != null && product.contains("microRNA")) {
					gbkey = GeneType.Precursor_miRNA.toString();
				}
			}

			if (ncRNAclass != null) {
				result = ncRNAclass;
			} else if (gbkey != null) {
				result = gbkey;
			}
		}

		GeneType geneType = GeneType.getGeneType(result);
		if (geneType == null) {
			if (ncRNA) {
				geneType = GeneType.ncRNA;
			} else {
				logger.error("UnKnown RNA Type: please check: "
						+ ArrayOperate.cmbString(content, "\t"));
			}
		}
		return geneType;
	}

	/**
	 * 设定taxID
	 * 
	 * @param geneName
	 */
	private void setTaxID(String[] ss, String geneName) {
		if (taxID != 0)
			return;

		if (ss[2].equals("region")) {
			// 把ID=id0;Dbxref=taxon:9823;breed=mixed;chromosome=1;gbkey=Src;genom
			// 里面的9823抓出来
			try {
				taxID = Integer.parseInt(PatternOperate.getPatLoc(ss[8],
						"(?<=Dbxref\\=taxon\\:)\\w+", false).get(0)[0]);
			} catch (Exception e) {
			}
			return;
		}
		if (taxID == 0 && numCopedIDsearch < 20) {
			// ArrayList<GeneID> lsCopedIDs = null;
			// try { lsCopedIDs = GeneID.createLsCopedID(geneName, taxID,
			// false); } catch (Exception e) { }

			// if (lsCopedIDs != null && lsCopedIDs.size() == 1) {
			// taxID = lsCopedIDs.get(0).getTaxID();
			// }
			numCopedIDsearch++;
		}
	}

	/**
	 * 从hashGenID2GffDetail中获得该GffDetailGene 这里的genID不是我们数据库里面的geneID，而是NCBI
	 * gff所特有的ID
	 * 
	 * @param genID
	 * @return null 表示没有找到相应的GffDetail信息
	 */
	private GffDetailGene getGffDetailGenID(String genID) {
		return mapGenID2GffDetail.get(genID);
	}

	/**
	 * 从hashRnaID2GeneID中获得该GffDetailGene 这里的genID不是我们数据库里面的geneID，而是NCBI
	 * gff所特有的ID
	 * 
	 * @param genID
	 * @return null 表示没有找到相应的GffDetail信息
	 */
	private GffDetailGene getGffDetailRnaID(String rnaID) {
		String genID = mapRnaID2GeneID.get(rnaID);
		return getGffDetailGenID(genID);
	}

	private String getGeneID(String rnaID) {
		String geneID = mapRnaID2GeneID.get(rnaID);
		if (geneID == null) {
			geneID = rnaID;
		}
		return geneID;
	}

	/**
	 * 
	 * 从hashRnaID2RnaName中获得该RNA的GffGeneIsoInfo 这里的genID不是我们数据库里面的geneID，而是NCBI
	 * gff所特有的ID
	 * 
	 * @param rnaID
	 *            输入的rnaID
	 * @param startExon
	 *            输入exon的起点和终点，查找lsGffISO，只有当ISO cover 这对坐标时，才会返回相应的ISO
	 *            主要用于这种情况：<br>
	 *            NC_000001.10 RefSeq gene 94313129 94313213<br>
	 *            NC_000001.10 RefSeq tRNA 94313129 94313165<br>
	 *            NC_000001.10 RefSeq tRNA 94313178 94313213<br>
	 *            NC_000001.10 RefSeq exon 94313129 94313165<br>
	 *            NC_000001.10 RefSeq exon 94313178 94313213<br>
	 *            这时候两个tRNA的rnaID是一样的，但是这两个tRNA确实是两个不同的iso，
	 *            所以就要根据坐标将两个exon分别装入两个iso中
	 * @param endExon
	 *            如果startExon和endExon中有一个小于0，则直接返回listIso的第一个ISO
	 * @param geneType
	 *            如果没有找到iso，则新建的iso是什么类型
	 * @return
	 */
	private GffGeneIsoInfo getGffIso(String rnaID, int startExon, int endExon,
			GeneType geneType) {
		GffGeneIsoInfo iso = mapRnaID2Iso.get(rnaID);
		List<ExonInfo> lsGffLoc = mapRnaID2LsIsoLocInfo.get(rnaID);
		if (iso == null) {
			mapRnaID2GeneID.put(rnaID, rnaID);
			GffDetailGene gffDetailGene = getGffDetailGenID(rnaID);
			if (gffDetailGene == null) {
				logger.error("cannot find rnaId " + rnaID);
				return null;
			}
			GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.addsplitlist(
					gffDetailGene.getNameSingle(),
					gffDetailGene.getNameSingle(), geneType);

			mapRnaID2Iso.put(rnaID, gffGeneIsoInfo);
			iso = mapRnaID2Iso.get(rnaID);
		}
		return iso;
	}
	
	private int minDistance(Alignment align1, Alignment align2) {
		if (align1.getStartAbs() <= align2.getEndAbs() && align1.getEndAbs() >= align2.getStartAbs()) {
			return 0;
		}
		int distance = align1.getStartAbs() < align2.getStartAbs()? 
				align2.getStartAbs() - align1.getEndAbs() : align1.getStartAbs() - align2.getEndAbs();
				
		if (distance < 0) {
			throw new ExceptionNbcGFF("align site error " + align1.toString() + " " + align2.toString());
		}
		return distance;
	}

	// TODO 考虑将该方法放到超类中
	/**
	 * 将locGff中的信息整理然后装入ChrHash中
	 */
	private void setGffList() {
		mapChrID2ListGff = new LinkedHashMap<String, ListGff>();
		ArrayListMultimap<String, GffDetailGene> mapName2LsGene = ArrayListMultimap.create();
		
		//====================================================
		//有些基因是很长的转录本，但是gff记录的时候记录成了两个，所以需要把他们合并为一个iso
		for (GffDetailGene gene : mapGenID2GffDetail.values()) {
			if (!mapName2LsGene.containsKey(gene.getNameSingle())) {
				mapName2LsGene.put(gene.getNameSingle(), gene);
				continue;
			}
			if (gene.getNameSingle().equals("Dock2")) {
				logger.info("stop");
			}
			List<GffDetailGene> lsGenes = mapName2LsGene.get(gene.getNameSingle());
			
			boolean isFinish = false;
			for (GffDetailGene gffDetailGene : lsGenes) {
				if (isFinish) break;
				
				if (minDistance(gene, gffDetailGene) > 500_000) {
					continue;
				}
				for (GffGeneIsoInfo isoOld : gffDetailGene.getLsCodSplit()) {
					if (gene.getLsCodSplit().isEmpty()) {
						isFinish = true;
						break;
					}
					GffGeneIsoInfo iso = gene.pollIsoByName(isoOld.getName());
					if (iso == null) continue;
					
					if (isoOld.isCis5to3() != iso.isCis5to3() || GeneType.getSetSmallRNA().contains(iso.getGeneType())) {
						continue;
					}
					if (isoOld.getStartAbs() < iso.getEndAbs() && isoOld.getEndAbs() > iso.getStartAbs()) {
						continue;
					}
					isoOld.addAll(iso.getLsElement());
					if (isoOld.isCis5to3()) {
						isoOld.setATGUAGauto(Math.min(iso.getATGsite(), isoOld.getATGsite()), Math.max(iso.getUAGsite(), isoOld.getUAGsite()));
					} else {
						isoOld.setATGUAGauto(Math.max(iso.getATGsite(), isoOld.getATGsite()), Math.min(iso.getUAGsite(), isoOld.getUAGsite()));
					}
					isoOld.sort();
				}
				gffDetailGene.resetStartEnd();
			}
			if (!gene.getLsCodSplit().isEmpty()) {
				mapName2LsGene.put(gene.getNameSingle(), gene);
			}
		}
		//====================================================
		
		ListGff LOCList = null;
		for (GffDetailGene gffDetailGene : mapName2LsGene.values()) {
			String chrID = gffDetailGene.getRefID();
			// 新的染色体
			if (!mapChrID2ListGff.containsKey(chrID.toLowerCase())) { // 新的染色体
				LOCList = new ListGff();// 新建一个LOCList并放入Chrhash
				LOCList.setName(chrID);
				mapChrID2ListGff.put(chrID.toLowerCase(), LOCList);
			} else {
				LOCList = mapChrID2ListGff.get(chrID.toLowerCase());
			}

			if (gffDetailGene.getLsCodSplit().size() == 0) {
				gffDetailGene.addsplitlist(gffDetailGene.getNameSingle(),
						gffDetailGene.getNameSingle(), GeneType.ncRNA);
				gffDetailGene.addExon(null, gffDetailGene.getStartAbs(),
						gffDetailGene.getEndAbs());
			}
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				if (gffGeneIsoInfo.size() == 0) {
					gffGeneIsoInfo.addExon(null, gffDetailGene.getStartCis(),
							gffDetailGene.getEndCis());
				}
			}
			LOCList.add(gffDetailGene);
		}
	}

	/**
	 * 读取完毕后清空一些变量
	 */
	private void clear() {
		patGeneName = null;
		patmRNAName = null;
		patGeneID = null;
		patName = null;
		patID = null;
		patParentID = null;
		patProduct = null;

		mapRnaID2GeneID.clear();
		mapGenID2GffDetail.clear();

		mapRnaID2Iso.clear();
		mapRnaID2LsIsoLocInfo.clear();
		gffGetChrId.clear();
		mapGeneName2IsHaveExon.clear();
		setMrnaNameDuplicate.clear();

		mapRnaID2GeneID = null;
		mapGenID2GffDetail = null;
		setMrnaNameDuplicate = null;

		mapRnaID2GeneID = null;
		mapGenID2GffDetail = null;

		mapRnaID2Iso = null;
		mapRnaID2LsIsoLocInfo = null;
		gffGetChrId = null;
		mapGeneName2IsHaveExon = null;

		mapRnaID2GeneID = null;
		mapGenID2GffDetail = null;
	}

	/**
	 * 将NCBIgff中的chrID转换为标准ChrID，然后将其中的scaffold删除 同时修正tRNA的问题
	 * 
	 * @param NCBIgff
	 *            /media/winE/Bioinformatics/GenomeData/pig/gff/ref_Sscrofa10
	 *            .2_gnomon_top_level.gff3
	 */
	public static void modifyNCBIgffFile(String NCBIgff) {
		String regxChrID = "(?<=chromosome\\=)\\w+";
		TxtReadandWrite txtGff = new TxtReadandWrite(NCBIgff, false);
		TxtReadandWrite txtGffOut = new TxtReadandWrite(
				FileOperate.changeFileSuffix(NCBIgff, "_modify", null), true);
		/** 将不同的chrID表也写入对照表中 */
		HashMap<String, String> mapAccID2ChrID = new HashMap<String, String>();
		TxtReadandWrite txtGffOutConvertTab = new TxtReadandWrite(
				FileOperate
						.changeFileSuffix(NCBIgff, "_modify_ChrID_Tab", null),
				true);

		String chrID = "";
		boolean tRNAflag = false;
		String[] tRNAtmp = null;
		for (String string : txtGff.readlines()) {
			if (string.startsWith("#")) {
				continue;
			}
			String[] ss = string.split("\t");
			if (ss[2].equals("match") || ss[0].startsWith("NW_")
					|| ss[0].startsWith("NT_")) {
				continue;
			}

			if (ss[2].equals("region")) {
				if (ss[8].contains("genome=genomic")) {
					continue;
				} else if (ss[8].contains("genome=mitochondrion")) {
					chrID = "chrM";
				} else if (ss[8].contains("genome=chloroplast")) {
					chrID = "chrC";
				} else {
					try {
						String chrName = PatternOperate.getPatLoc(ss[8],
								regxChrID, false).get(0)[0];
						if (chrName.startsWith("NC_")) {
							chrID = chrName.toLowerCase();
						} else {
							chrID = "chr" + chrName;
						}
					} catch (Exception e) {
						logger.error("本位置出错，错误的region，本来一个region应该是一个染色体，这里不知道是什么 "
								+ string);
						chrID = "unkonwn";
					}
				}
				mapAccID2ChrID.put(ss[0], chrID);
			}
			ss[0] = chrID;
			if (chrID.equals("unknown")) {
				continue;
			}
			if (tRNAflag) {
				if (!ss[2].equals("tRNA")) {
					txtGffOut.writefileln(tRNAtmp);
					txtGffOut.writefileln(ss);
				} else {
					int start = minmax(true, tRNAtmp[3], tRNAtmp[4], ss[3],
							ss[4]);
					int end = minmax(false, tRNAtmp[3], tRNAtmp[4], ss[3],
							ss[4]);
					tRNAtmp[3] = start + "";
					tRNAtmp[4] = end + "";
					txtGffOut.writefileln(tRNAtmp);
				}
				tRNAflag = false;
				continue;
			} else {
				if (ss[2].equals("tRNA")) {
					tRNAflag = true;
					tRNAtmp = ss;
					continue;
				}
			}
			txtGffOut.writefileln(ss);

		}
		for (Entry<String, String> entry : mapAccID2ChrID.entrySet()) {
			txtGffOutConvertTab.writefileln(entry.getKey() + "\t"
					+ entry.getValue());
		}
		txtGff.close();
		txtGffOut.close();
		txtGffOutConvertTab.close();
	}

	/**
	 * 获得tRNA的两行的最小和最大值，作为tRNA的起点和终点
	 * 
	 * @param min
	 * @param is
	 * @return
	 */
	private static int minmax(boolean min, String... is) {
		int[] intis = new int[is.length];
		for (int i = 0; i < is.length; i++) {
			intis[i] = Integer.parseInt(is[i]);
		}
		MathComput.sort(intis, min);
		return intis[0];
	}

	/** 修正果蝇的gff文件 */
	public static void ModifyDrosophylia(String input, String output) {
		String parentRegx = "Parent=([\\w\\-%\\,\\.]+)?;";
		String idRegx = "ID=([\\w\\-%\\,\\.]+)?;";
		PatternOperate patParent = new PatternOperate(parentRegx, false);
		PatternOperate patId = new PatternOperate(idRegx, false);

		// 保存已经出现过的id
		Set<String> setId = new HashSet<String>();

		TxtReadandWrite txtRead = new TxtReadandWrite(input);
		TxtReadandWrite txtWrite = new TxtReadandWrite(output, true);
		for (String content : txtRead.readlines()) {
			if (content.startsWith("#") || content.trim().equals("")) {
				txtWrite.writefileln(content);
				continue;
			}
			String[] ss = content.split("\t");
			if (ss[2].equals("golden_path_region")
					|| ss[2].equals("exon_junction") || ss[2].equals("intron")) {
				continue;
			}
			String id = patId.getPatFirst(ss[8], 1);
			String parentId = patParent.getPatFirst(ss[8], 1);

			if (id != null) {
				if (id.contains(",")) {
					logger.debug("find id contains \",\" :" + id);
				}
				setId.add(id);
			}
			if (parentId != null) {
				List<String> lsParentId = new ArrayList<String>();
				for (String string : parentId.split(",")) {
					lsParentId.add(string);
				}
				String detailInfo = ss[8];
				for (String string : lsParentId) {
					if (setId.contains(string)) {
						ss[8] = detailInfo.replaceFirst(parentId, string);
						txtWrite.writefileln(ss);
					}
				}
				continue;
			}
			txtWrite.writefileln(ss);
		}
		txtRead.close();
		txtWrite.close();

	}
}
