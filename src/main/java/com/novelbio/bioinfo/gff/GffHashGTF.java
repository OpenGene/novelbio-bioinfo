package com.novelbio.bioinfo.gff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.aspectj.util.LangUtil.ProcessController.Thrown;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.base.SepSign;
import com.novelbio.base.StringOperate;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.HttpFetch;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.modgeneid.GeneType;
import com.sun.tools.javac.util.Name;

public class GffHashGTF extends GffHashGeneAbs{
	private final static Logger logger = Logger.getLogger(GffHashGTF.class);
	final static String startCodeFlag = "start_codon";
	final static String stopCodeFlag = "stop_codon";
	GffHashGene gffHashRef;
	double likelyhood = 0.4;//相似度在0.4以内的转录本都算为同一个基因
	/** gene类似名 */
	private static Set<String> setIsGene = new HashSet<String>();
	/** 需要跳过contig等 */
	private static Set<String> setContig = new HashSet<String>();
	
	ArrayListMultimap<String, GffIso> mapID2Iso = ArrayListMultimap.create();
	private HashMap<String, Boolean> mapIso2IsHaveExon = new HashMap<String, Boolean>();
		
	List<String> lsGeneName = new ArrayList<>();
	List<String> lsTranscript = new ArrayList<>();	
	List<String> lsTranscriptType = new ArrayList<>();	

	/** geneName是哪一项，默认是 gene_name */
	public void addGeneNameFlag(String geneNameFlag) {
		lsGeneName.add(geneNameFlag);
	}
	/** geneName是哪一项，默认是 gene_name */
	public void addTranscriptNameFlag(String transcriptNameFlag) {
		lsTranscript.add(transcriptNameFlag);
	}
	private void initialLsGeneIds() {
		lsGeneName.add("gene_name");
		lsGeneName.add("gene_id");
		lsGeneName.add("Name");
		lsGeneName.add("Parent");
	}
	
	private void initialLsmiRNA() {
		lsTranscript.add("transcript_name");
		lsTranscript.add("transcript_id");
		lsTranscript.add("ID");
	}
	
	private void initialLsTranscriptType() {
		lsTranscriptType.add("gene_biotype");
		lsTranscriptType.add("transcript_biotype");
	}
	/**
	 * 设定参考基因的Gff文件
	 * @param gffHashRef
	 */
	public void setGffHashRef(GffHashGene gffHashRef) {
		this.gffHashRef = gffHashRef;
	}
	/** 设定mRNA和gene的类似名，在gff文件里面出现的 */
	private void setGeneName() {
		if (setIsGene.isEmpty()) {
			setIsGene.add("gene");
			setIsGene.add("transposable_element_gene");
			setIsGene.add("transposable_element");
			setIsGene.add("pseudogene");
		}
	}
	/** 设定mRNA和gene的类似名，在gff文件里面出现的 */
	private void setContig() {
		if (setContig.isEmpty()) {
			setContig.add("contig");
			setIsGene.add("chromosome");
		}
	}
	@Override
	protected void ReadGffarrayExcepTmp(String gfffilename) {
		setGeneName(); setContig();
		initialLsGeneIds();
		initialLsmiRNA();
		initialLsTranscriptType();
		
		mapChrID2ListGff = new LinkedHashMap<String, ListGff>();
		ArrayListMultimap<String, GffIso> mapChrID2LsIso = ArrayListMultimap.create();
		TxtReadandWrite txtgff = new TxtReadandWrite(gfffilename);
		
		GffIso gffGeneIsoInfo = null;
		String tmpChrID = "";
		String tmpTranscriptNameLast = "";
		int line = 0;
		for (String content : txtgff.readlines() ) {
			if (content.contains("BID-207")) {
				logger.info("stop");
			}
			
			try {
				line++;
				if (StringOperate.isRealNull(content) || content.charAt(0) == '#') continue;
				String[] ss = content.split("\t");// 按照tab分开
				ss[8] = StringOperate.decode(ss[8]);
				if (setContig.contains(ss[2].toLowerCase())) continue;
				
				int exonStart = Integer.parseInt(ss[3]), exonEnd = Integer.parseInt(ss[4]);
				Boolean cisExon = null;
				if (ss[6].equals("+")) {
					cisExon = true;
				} else if (ss[6].equals("-")) {
					cisExon = false;
				}
				
				// 新的染色体
				if (!tmpChrID.equals(ss[0]) ) {
					tmpChrID = ss[0];
				}
				String[] isoName2GeneName = null;
				try {
					isoName2GeneName = getIsoName2GeneName(ss[8]);
				} catch (Exception e) {
					isoName2GeneName = getIsoName2GeneName(ss[8]);
					throw new ExceptionNbcGFF("gff file error on line " + content);
				}
				if (isoName2GeneName == null) {
					txtgff.close();
					throw new ExceptionNbcGFF("line " + line + " error, no isoName exist: " + content);
				}
				String tmpTranscriptName = isoName2GeneName[0], tmpGeneName = isoName2GeneName[1];
				String geneTypeStr = isoName2GeneName[2];
				if (geneTypeStr == null) geneTypeStr = "";
				
				if (setIsGene.contains(ss[2].toLowerCase())) continue;
				
				
				/** 如果当前exon的转录本和上一个转录本不一样，可以先找一下是不是已经存在这个名字的iso */
				boolean getBeforeIso = false;
				if (!GeneType.getMapMRNA2GeneType().containsKey(ss[2].toLowerCase())
						&&
						!tmpTranscriptName.equals(tmpTranscriptNameLast)
					)
				{
					gffGeneIsoInfo = getGffIsoSimple(tmpGeneName, tmpTranscriptName, exonStart, exonEnd);
					if (gffGeneIsoInfo != null) {
						getBeforeIso = true;
					}
				}
				
				//出现新转录本有两种可能：
				//1： 单开一行标记新的transcript
				//2： 还是标记exon，只是后面的transcriptID 变了
				if (!getBeforeIso &&
					(
							GeneType.getMapMRNA2GeneType().containsKey(ss[2].toLowerCase())
							||
							(!tmpTranscriptName.equals(tmpTranscriptNameLast) 
							&& !isHaveIso(tmpGeneName, tmpTranscriptName, Integer.parseInt(ss[3]), Integer.parseInt(ss[4])))
						)
					) 
				{
					GeneType geneType = GeneType.getMapMRNA2GeneType().get(geneTypeStr.toLowerCase());
					if (geneType == null) {
						geneType = GeneType.mRNA;
					}
						
					boolean cis = getLocCis(ss[6], tmpChrID, exonStart, exonEnd);
					gffGeneIsoInfo = GffIso.createGffGeneIso(tmpTranscriptName, tmpGeneName, geneType, cis);
					addGffIso(tmpGeneName, gffGeneIsoInfo);
					mapChrID2LsIso.put(tmpChrID, gffGeneIsoInfo);
					tmpTranscriptNameLast = tmpTranscriptName;
					mapIso2IsHaveExon.put(tmpTranscriptName, false);
					if (GeneType.getMapMRNA2GeneType().containsKey(ss[2].toLowerCase())) {
						continue;
					}
				}
				if (gffGeneIsoInfo == null || !gffGeneIsoInfo.getName().equalsIgnoreCase(tmpTranscriptName)
						|| !gffGeneIsoInfo.getParentGeneName().equalsIgnoreCase(tmpGeneName)
						) {
					gffGeneIsoInfo = getGffIso(tmpGeneName, tmpTranscriptName, exonStart, exonEnd);
				}

				if (gffGeneIsoInfo == null && !ss[2].toLowerCase().contains("utr")) {
					logger.error("gtf record cannot find corresponding iso " + content);
					continue;
				}
				
				if (ss[2].equals("exon")) {
					if (mapIso2IsHaveExon.get(tmpTranscriptName) == false) {
						gffGeneIsoInfo.addExon(cisExon, exonStart, exonEnd);
						mapIso2IsHaveExon.put(tmpTranscriptName, true);
					} else {
						gffGeneIsoInfo.addExon(cisExon, exonStart, exonEnd);
					}	
				} else if (ss[2].toLowerCase().equals("cds")) {
					//TODO  ncbi上的gff3，cds的末尾是uag，而
					//ucsc上的GTF，cds的末尾不是uag，而是uag的前一位。
					//所以该方法在这里不适用，不过后面有个专门设定uag的方法，所以倒也无所谓了。
					gffGeneIsoInfo.setATGUAGauto(exonStart, exonEnd);
					if (mapIso2IsHaveExon.get(tmpTranscriptName) == null) {
						logger.error("gtf cannot find corresponding gene : " + tmpTranscriptName);
					}
					if (!mapIso2IsHaveExon.get(tmpTranscriptName)) {
						gffGeneIsoInfo.addCDS(cisExon, exonStart, exonEnd);
					}
				} else if (ss[2].toLowerCase().equals(startCodeFlag)) {
					gffGeneIsoInfo.addExon(cisExon, exonStart, exonEnd);
					if (cisExon == null || cisExon) {
						gffGeneIsoInfo.setATG(exonStart);
					} else {
						gffGeneIsoInfo.setATG(exonEnd);
					}
				} else if (ss[2].toLowerCase().equals(stopCodeFlag)) {
					gffGeneIsoInfo.addExon(cisExon, exonStart, exonEnd);
					if (cisExon == null || cisExon) {
						gffGeneIsoInfo.setUAG(exonEnd);
					} else {
						gffGeneIsoInfo.setUAG(exonStart);
					}
				}
			} catch (Exception e) {
				txtgff.close();
				if (e instanceof ExceptionNbcGFF) {
					throw e;
				}
				throw new ExceptionNbcGFF("line " + line + " error, no isoName exist: " + content, e);
			}

		}
		CopeChrIso(mapChrID2LsIso);
		txtgff.close();
		mapID2Iso = null;
	}

	/**
	 * @param ss8
	 * @param gffGeneNameFlag
	 * @return 0 IsoName; 1 GeneName; 2 GeneType
	 */
	protected static String[] getIsoName2GeneName(String ss8, String gffGeneNameFlag) {
		ss8 = ss8.replace("\t", " ");
		String geneNameFlag = "gene_name";
		if (!StringOperate.isRealNull(gffGeneNameFlag)) {
			geneNameFlag = gffGeneNameFlag;
			if (!ss8.contains(geneNameFlag) && ss8.contains("gene_name")) {
				geneNameFlag = "gene_name";
			}
		}
		
		if (!ss8.contains(geneNameFlag) && ss8.contains("gene_id")) {
			geneNameFlag = "gene_id";
		} else if (!ss8.contains(geneNameFlag) && !ss8.contains("gene_id") && ss8.contains("Name")) {
			geneNameFlag = "Name";
		} else if (ss8.contains("Parent")) {
			geneNameFlag = "Parent";
		}
		
		String[] iso2geneName = new String[3];
		List<String> lsAnnoInfo = new ArrayList<>();
		StringBuilder stringBuilder = new StringBuilder();
		boolean isInQuote = false;
		for (char c : ss8.toCharArray()) {
			if (c == '"') isInQuote = !isInQuote;
			
			if ((c == ' ' || c == ';' || c == '"') && !isInQuote) {
				if (c=='"') stringBuilder.append('"');
				
				String info = stringBuilder.toString();
				stringBuilder = new StringBuilder();
				if (!StringOperate.isRealNull(info)) {
					lsAnnoInfo.add(info);
				}
			} else {
				stringBuilder.append(c);
			}
		}
		String info = stringBuilder.toString();
		stringBuilder = new StringBuilder();
		if (!StringOperate.isRealNull(info)) {
			lsAnnoInfo.add(info);
		}
		
		for (int i = 0; i < lsAnnoInfo.size(); i+=2) {
			String name = CmdOperate.removeQuot(lsAnnoInfo.get(i)) + SepSign.SEP_ID + CmdOperate.removeQuot(lsAnnoInfo.get(i+1));
			 name = name.trim();
				if (isKeyContainsValue(name, "transcript") && isKeyContainsValue(name	, "id")) {
					iso2geneName[0] = getLastValue(name);
				} else if (name.startsWith(geneNameFlag)) {
					iso2geneName[1] = getLastValue(name);
				} else if (name.startsWith("ID")) {
					iso2geneName[0] = getLastValue(name);
				} else if (isKeyContainsValue(name, "type")) {
					try {
						iso2geneName[2] = getLastValue(name);
					} catch (Exception e) {
						iso2geneName[2] = GeneType.mRNA.toString(); 
					}
				} else if (name.toLowerCase().startsWith("gene_biotype")) {
					try {
						iso2geneName[2] = getLastValue(name);
					} catch (Exception e) {
						iso2geneName[2] = GeneType.mRNA.toString(); 
					}
				}
		}
		
		 return iso2geneName;
	}
	
	private String[] getIsoName2GeneName(String ss8) {
		String[] iso2Gene = new String[3];
		if (lsGeneName.isEmpty() && lsTranscript.isEmpty()) {
			return iso2Gene;
		}
		Map<String, String> mapId2Value = getMapId2ValueSS8(ss8);

		if (!lsGeneName.isEmpty()) {
			for (String geneName : lsGeneName) {
				if (mapId2Value.containsKey(geneName)) {
					iso2Gene[1] = mapId2Value.get(geneName);
					break;
				}
			}
		}
		if (!lsTranscript.isEmpty()) {
			for (String transcript : lsTranscript) {
				if (mapId2Value.containsKey(transcript)) {
					iso2Gene[0] = mapId2Value.get(transcript);
					break;
				}
			}
		}
		if (!lsTranscriptType.isEmpty()) {
			for (String transcript : lsTranscriptType) {
				if (mapId2Value.containsKey(transcript)) {
					iso2Gene[2] = mapId2Value.get(transcript);
					break;
				}
			}
		}
		return iso2Gene;
	}
	
	protected static Map<String, String> getMapId2ValueSS8(String ss8) {
		Map<String, String> mapId2Value = null;
		if (ss8.contains("\"\"")) {
			try {
				mapId2Value = getMapId2ValueSS8Commo(ss8);
			} catch (Exception e) {
				mapId2Value = getMapId2ValueSS8Quote(ss8);
			}
		} else {
			try {
				mapId2Value = getMapId2ValueSS8Quote(ss8);
			} catch (Exception e) {
				mapId2Value = getMapId2ValueSS8Commo(ss8);
			}
		}
		return mapId2Value;
	}
	/**  第八列整理成id2value的形式 */
	protected static Map<String, String> getMapId2ValueSS8Quote(String ss8) {
		Map<String, String> mapId2Value = new HashMap<>();
		StringBuilder stringBuilder = new StringBuilder();
		boolean isInQuote = false;
		List<String> lsAnnoInfo = new ArrayList<>();
		for (char c : ss8.toCharArray()) {
			if (c == '"') isInQuote = !isInQuote;
			
			if ((c == ' ' || c == ';' || c == '"') && !isInQuote) {
//				if (c=='"') stringBuilder.append('"');
				
				String info = stringBuilder.toString();
				stringBuilder = new StringBuilder();
				if (!StringOperate.isRealNull(info)) {
					lsAnnoInfo.add(info.trim());
				}
			} else {
				if (c == '"') {
					continue;
				}
				stringBuilder.append(c);
			}
		}
		String info = stringBuilder.toString();
		stringBuilder = new StringBuilder();
		if (!StringOperate.isRealNull(info)) {
			lsAnnoInfo.add(info.trim());
		}
		for (int i = 0; i < lsAnnoInfo.size(); i+=2) {
			mapId2Value.put(lsAnnoInfo.get(i), lsAnnoInfo.get(i+1));
		}
		return mapId2Value;
	}
	
	/**  第八列整理成id2value的形式 */
	protected static Map<String, String> getMapId2ValueSS8Commo(String ss8) {
		if (ss8.endsWith(";")) {
			ss8 = ss8+" ";
		}
		Map<String, String> mapId2Value = new HashMap<>();
		String[] ss = ss8.split("; ");
		for (String unit : ss) {
			if (StringOperate.isRealNull(unit)) {
				continue;
			}
			String[] tmp = unit.split(" ");
			String key = CmdOperate.removeQuot(tmp[0]);
			String value = cmbStringWithoutFirst(tmp);
			mapId2Value.put(key, CmdOperate.removeQuot(value));
		}
		return mapId2Value;
	}
	
	private static String cmbStringWithoutFirst(String[] tmp) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(tmp[1]);
		for (int i = 2; i < tmp.length; i++) {
			stringBuilder.append(" " + tmp[i]);
		}
		return stringBuilder.toString();
	}
	
	/**
	 * @param ss8
	 * @param gffGeneNameFlag
	 * @return 0 IsoName; 1 GeneName; 2 GeneType
	 * 注意2 暂时不用
	 */
	protected static String[] getIsoName2GeneNameStatic(String ss8) {
		ss8 = ss8.replace("\t", " ");
		String geneNameFlag = "gene_name";
		
		if (!ss8.contains(geneNameFlag) && ss8.contains("gene_id")) {
			geneNameFlag = "gene_id";
		} else if (!ss8.contains(geneNameFlag) && !ss8.contains("gene_id") && ss8.contains("Name")) {
			geneNameFlag = "Name";
		} else if (ss8.contains("Parent")) {
			geneNameFlag = "Parent";
		}
		 String[] iso2geneName = new String[3];
		Map<String, String> mapId2Value = getMapId2ValueSS8(ss8);
		for (String key : mapId2Value.keySet()) {
			String value = mapId2Value.get(key);
			String name = key + SepSign.SEP_ID + value;
			 name = name.trim();
			 if (isKeyContainsValue(name, "transcript") && isKeyContainsValue(name	, "id")) {
					iso2geneName[0] = getLastValue(name);
				} else if (name.startsWith(geneNameFlag)) {
					iso2geneName[1] = getLastValue(name);
				} else if (name.startsWith("ID")) {
					iso2geneName[0] = getLastValue(name);
				} else if (isKeyContainsValue(name, "type")) {
					try {
						iso2geneName[2] = getLastValue(name);
					} catch (Exception e) {
						iso2geneName[2] = GeneType.mRNA.toString(); 
					}
				} else if (name.toLowerCase().startsWith("gene_biotype")) {
					try {
						iso2geneName[2] = getLastValue(name);
					} catch (Exception e) {
						iso2geneName[2] = GeneType.mRNA.toString(); 
					}
				}
		}
		 return iso2geneName;
	}

	/**
	 * 给定 transcript_id "R2_19_1" 这种，返回 
	 * @param keyvalue
	 * @return
	 */
	private static String getLastValue(String keyValue) {
		String[] ss = keyValue.replace("=", SepSign.SEP_ID).trim().split(SepSign.SEP_ID);
		String value = ss[ss.length-1];
		value = value.replace("\"", "").trim();
		return value;
	}
	
	/**
	 * 给定 transcript_id "R2_19_1" 这种，
	 * 判定 transcript_id 字段是否含有id片段
	 * @param keyvalue
	 * @return
	 */
	private static boolean isKeyContainsValue(String info, String value) {
		String key = info.trim().replace("=", " ").split(" ")[0].toLowerCase();
		return key.contains(value.toLowerCase());
	}
	
	private void addGffIso(String geneName, GffIso gffGeneIsoInfo) {
		mapID2Iso.put(geneName, gffGeneIsoInfo);
	}
	
	
	/** 如果一个iso的exon之间隔了超过这个数字，并且中间还有别的转录本，那么就认为是一个全新的iso了 */
	static int isoDistance = 500000;
	
	private boolean isHaveIso(String geneName, String isoName, int start, int end) {
		return false;
//		List<GffGeneIsoInfo> lsIsos = mapID2Iso.get(geneName);
//		if (lsIsos == null || lsIsos.size() == 0) {
//			return false;
//		}
//		for (GffGeneIsoInfo gffGeneIsoInfo : lsIsos) {
//			if (gffGeneIsoInfo.getName().toLowerCase().equals(isoName.toLowerCase())) {
//				if (gffGeneIsoInfo.isEmpty()) {
//					return true;
//				}
//				
//				if (start <= gffGeneIsoInfo.getEndAbs() && end >= gffGeneIsoInfo.getStartAbs()) {
//					return true;
//				}
//				int distance = Math.min(Math.abs(start - gffGeneIsoInfo.getEndAbs()), Math.abs(gffGeneIsoInfo.getStartAbs() - end));
//
//				if (distance < isoDistance) {
//					return true;
//				}
//	
//			}
//		}
//		return false;
	}

	/**
	    * 从hashRnaID2RnaName中获得该RNA的GffGeneIsoInfo
	    * 这里的genID不是我们数据库里面的geneID，而是NCBI gff所特有的ID
	    * @param rnaID 输入的rnaID
	    * @param startExon 输入exon的起点和终点，查找lsGffISO，只有当ISO cover 这对坐标时，才会返回相应的ISO
	    * 主要用于这种情况：<br>
	    * NC_000001.10	RefSeq	gene	94313129	94313213<br>
	    * NC_000001.10	RefSeq	tRNA	94313129	94313165<br>
	    * NC_000001.10	RefSeq	tRNA	94313178	94313213<br>
	    * NC_000001.10	RefSeq	exon	94313129	94313165<br>
	    * NC_000001.10	RefSeq	exon	94313178	94313213<br>
	    * 这时候两个tRNA的rnaID是一样的，但是这两个tRNA确实是两个不同的iso，所以就要根据坐标将两个exon分别装入两个iso中
	    * @param endExon 如果startExon和endExon中有一个小于0，则直接返回listIso的第一个ISO
	    * @param geneType 如果没有找到iso，则新建的iso是什么类型
	    * @return
	    */  
	private GffIso getGffIso(String geneName, String isoName, int startExon, int endExon) {
		int start = Math.min(startExon, endExon), end = Math.max(startExon, startExon);
		List<GffIso> lsIsos = mapID2Iso.get(geneName);
		if (lsIsos == null || lsIsos.size() == 0) {
			return null;
		}
		for (GffIso gffGeneIsoInfo : lsIsos) {
			if (gffGeneIsoInfo.getName().equalsIgnoreCase(isoName)) {
				if (gffGeneIsoInfo.isEmpty()) {
					return gffGeneIsoInfo;
				}
				if (start <= gffGeneIsoInfo.getEndAbs() && end >= gffGeneIsoInfo.getStartAbs()) {
					return gffGeneIsoInfo;
				}
				int distance = Math.min(Math.abs(start - gffGeneIsoInfo.getEndAbs()), Math.abs(gffGeneIsoInfo.getStartAbs() - end));

				if (distance < isoDistance) {
					return gffGeneIsoInfo;
				}
			}
		}
		for (GffIso gffGeneIsoInfo : lsIsos) {
			if (gffGeneIsoInfo.size() == 0 || start <= gffGeneIsoInfo.getEndAbs() && end >= gffGeneIsoInfo.getStartAbs()) {
				return gffGeneIsoInfo;
			}
		}
		int maxDistance = isoDistance;
		GffIso gffGeneIsoInfoFinal = null;
		for (GffIso gffGeneIsoInfo : lsIsos) {
			int distance = Math.min(Math.abs(start - gffGeneIsoInfo.getEndAbs()), Math.abs(gffGeneIsoInfo.getStartAbs() - end));
			if (distance < maxDistance) {
				maxDistance = distance;
				gffGeneIsoInfoFinal = gffGeneIsoInfo;
			}
		}
		return gffGeneIsoInfoFinal;
	}

	/**
	    * 从hashRnaID2RnaName中获得该RNA的GffGeneIsoInfo
	    * @return
	    */  
	private GffIso getGffIsoSimple(String geneName, String isoName, int startExon, int endExon) {
		int start = Math.min(startExon, endExon), end = Math.max(startExon, startExon);
		List<GffIso> lsIsos = mapID2Iso.get(geneName);
		if (lsIsos == null || lsIsos.size() == 0) {
			return null;
		}
		for (GffIso gffGeneIsoInfo : lsIsos) {
			if (gffGeneIsoInfo.getName().equalsIgnoreCase(isoName)) {
				if (gffGeneIsoInfo.isEmpty()) {
					return gffGeneIsoInfo;
				}
				if (start <= gffGeneIsoInfo.getEndAbs() && end >= gffGeneIsoInfo.getStartAbs()) {
					return gffGeneIsoInfo;
				}
				int distance = Math.min(Math.abs(start - gffGeneIsoInfo.getEndAbs()), Math.abs(gffGeneIsoInfo.getStartAbs() - end));

				if (distance < isoDistance) {
					return gffGeneIsoInfo;
				}
			}
		}
		
		return null;
	}
	private void CopeChrIso(ArrayListMultimap<String, GffIso> hashChrIso) {
		for (String chrID : hashChrIso.keySet()) {
			List<GffIso> listIso = hashChrIso.get(chrID);
			copeChrInfo(chrID, listIso);
		}
	}
	
	/**
	 * 整理某条染色体的信息
	 * 将重叠的Iso放到一个gffDetail基因里面
	 */
	private void copeChrInfo(String chrID, List<GffIso> lsGeneIsoInfos) {
		ListGff lsResult = new ListGff();
		lsResult.setName(chrID);
		
		if (lsGeneIsoInfos == null)// 如果已经存在了LOCList，也就是前一个LOCList，那么截短并装入LOCChrHashIDList
			return;
		//排序
		Collections.sort(lsGeneIsoInfos, new Comparator<GffIso>() {
			public int compare(GffIso o1, GffIso o2) {
				Integer o1Start = 0;
				try {
					o1Start = o1.getStartAbs();
				} catch (Exception e) {
					o1Start = o1.getStartAbs();
				}
				
				Integer o2Start = 0;
				o2Start = o2.getStartAbs();

				return o1Start.compareTo(o2Start);
			}
		});
		//依次装入gffdetailGene中
		GffGene gffDetailGene = null;
		for (GffIso gffGeneIsoInfo : lsGeneIsoInfos) {
			gffGeneIsoInfo.sortOnly();
			if (gffDetailGene == null) {
				gffDetailGene = createGffDetailGene(lsResult, gffGeneIsoInfo);
			}
			
			double[] gffIsoRange = new double[]{gffGeneIsoInfo.getStartAbs(), gffGeneIsoInfo.getEndAbs()};
			double[] gffGeneRange = new double[]{gffDetailGene.getStartAbs(), gffDetailGene.getEndAbs()};
			double[] compResult = ArrayOperate.cmpArray(gffIsoRange, gffGeneRange);
			if (compResult[2] > GffGene.OVERLAP_RATIO || compResult[3] > GffGene.OVERLAP_RATIO
					||
					gffDetailGene.getSimilarIso(gffGeneIsoInfo, likelyhood) != null
					) {
				gffDetailGene.addIso(gffGeneIsoInfo);
			}
			else {
				gffDetailGene = createGffDetailGene(lsResult, gffGeneIsoInfo);
				continue;
			}
		}
		mapChrID2ListGff.put(chrID.toLowerCase(), lsResult);
	}
	
	private GffGene createGffDetailGene(ListGff lsParent, GffIso gffGeneIsoInfo) {
		String geneName = gffGeneIsoInfo.getParentGeneName();
		GffGene gffDetailGene = new GffGene(lsParent, geneName, gffGeneIsoInfo.isCis5to3());
		gffDetailGene.addIso(gffGeneIsoInfo);
		lsParent.add(gffDetailGene);
		return gffDetailGene;
	}
	
	
	/**
	 * 给定chrID和坐标，返回该点应该是正链还是负链
	 * 如果不清楚正负链且没有给定相关的refGff，则直接返回true
	 * @param chrID
	 * @param LocID
	 * @return
	 */
	private boolean getLocCis(String ss, String chrID, int LocIDStart, int LocIDEnd) {
		if (ss.equals("+")) {
			return true;
		}
		else if (ss.equals("-")) {
			return false;
		}
		else {
			if (gffHashRef == null) {
				return true;
			}
			int LocID = (LocIDStart + LocIDEnd )/2;
			GffCodGene gffCodGene = gffHashRef.searchLocation(chrID, LocID);
			if (gffCodGene == null) {
				return true;
			}
			if (gffCodGene.isInsideLoc()) {
				return gffCodGene.getGffDetailThis().isCis5to3();
			} else {
				int a = Integer.MAX_VALUE;
				int b = Integer.MAX_VALUE;
				if (gffCodGene.getGffDetailUp() != null) {
					a = gffCodGene.getGffDetailUp().getCod2End(LocID);
				}
				if (gffCodGene.getGffDetailDown() != null) {
					b = gffCodGene.getGffDetailDown().getCod2Start(LocID);
				}
				if (a < b) {
					return gffCodGene.getGffDetailUp().isCis5to3();
				}
				else {
					return gffCodGene.getGffDetailDown().isCis5to3();
				}
			}
		}
	}
	
	/**
	 * 修正ensembl的GTF文件，主要是将一些没用的行，譬如H开头，G开头的删除
	 * 同时将染色体名字从 1 改为 chr1
	 */
	public static void modifyEnsemblGTF(String ensemblGTF) {
		TxtReadandWrite txtRead = new TxtReadandWrite(ensemblGTF, false);
		String fileName = FileOperate.changeFileSuffix(ensemblGTF, "_modify", null);
		fileName = FileOperate.changeFilePrefix(fileName, "Ensembl_", null);
		TxtReadandWrite txtWrite = new TxtReadandWrite(fileName, true);
		for (String string : txtRead.readlines()) {
			if (string.startsWith("H") || string.startsWith("G") || string.startsWith("N")) {
				continue;
			}
			txtWrite.writefileln("chr" + string);
		}
		txtRead.close();
		txtWrite.close();
	}
}
