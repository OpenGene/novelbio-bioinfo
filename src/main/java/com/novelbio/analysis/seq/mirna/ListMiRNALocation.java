package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.gffOperate.ListDetailBin;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ListHashBin;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.listOperate.ListBin;
import com.novelbio.base.dataStructure.listOperate.ListCodAbs;
import com.novelbio.base.dataStructure.listOperate.ListCodAbsDu;
import com.novelbio.base.fileOperate.FileOperate;
/**
 * ��ȡmiRNA.dat����Ϣ������listabs���������mirID��loc���Ӷ����ҵ�����5p����3p
 * @author zong0jie
 *
 */
public class ListMiRNALocation extends ListHashBin{
	Logger logger = Logger.getLogger(ListMiRNALocation.class);
	
	public static int TYPE_RNA_DATA = 10;
	public static int TYPE_MIREAP = 15;
	public static int TYPE_MIRDEEP = 25;
	public static void main(String[] args) {
		ListMiRNALocation tmpMiRNALocation = new ListMiRNALocation();
		tmpMiRNALocation.ReadGffarray("/media/winE/Bioinformatics/DataBase/sRNA/miRNA.dat");
		System.out.println(tmpMiRNALocation.searchMirName("hsa-mir-16-2", 50, 65));
	}
	/** taxID��ӦRNA.data�е�String */
	HashMap<Integer, String> hashSpecies = new HashMap<Integer, String>();
	int taxID = 9606;
	String species = "HSA";

	/**
	 * ΪmiRNA.dat�е�������
	 * �趨���֣�Ĭ��Ϊ���ࣺHSA
	 * ����Ҫ���RNA.data�ļ�
	 * @param species
	 */
	public void setSpecies(int taxID) {
		this.taxID = taxID;
	}
	int fileType = TYPE_RNA_DATA;
	/**
	 * �ļ���ʽ��������RNA.dat��Ҳ������miReap�Ľ��
	 * @param type TYPE_RNA_DATA �� TYPE_MIREAP
	 */
	public void setReadFileType(int type) {
		this.fileType = type;
	}
	/**
	 * ��ȡmiRNA.data�ļ���ͬʱ��ȡ����һ�������õ�taxID2species�ļ�
	 */
	protected void ReadGffarrayExcep(String rnadataFile) {
		if (fileType == TYPE_RNA_DATA) {
			String fileName = FileOperate.changeFileSuffix(rnadataFile, "_taxID2Species", "txt");
			readTaxID2Species(fileName);
			species = hashSpecies.get(taxID);
			ReadGffarrayExcepRNADat(rnadataFile);
		}
		else if (fileType == TYPE_MIREAP) {
			ReadGffarrayExcepMirReap(rnadataFile);
		}
		else if (fileType == TYPE_MIRDEEP) {
			ReadGffarrayExcepMirDeep(rnadataFile);
		}
	}
	/** ��ȡtaxID��ӦmiRNA.dat�е�������
	 * ���������� ���ࣺHSA ��
	 * ����Ҫ��� RNA.data �ļ�<br>
	 * ��һ�� taxID<br>
	 * �ڶ��� species
	 */
	private void readTaxID2Species(String rnaDataSpecies) {
		ArrayList<String[]> lsInfo = ExcelTxtRead.readLsExcelTxt(rnaDataSpecies, new int[]{1,2}, 1, -1);
		for (String[] strings : lsInfo) {
			if (strings == null || strings[0] == null || strings[0].trim().equals("")) {
				continue;
			}
			hashSpecies.put(Integer.parseInt(strings[0]), strings[1]);
		}
	}
	/**
	 * ��ȡRNA.dat�����ÿ��СRNA��������Ϣ
	 * ID   hsa-mir-1539      standard; RNA; HSA; 50 BP.
XX
AC   MI0007260;
XX
DE   Homo sapiens miR-1539 stem-loop
XX
RN   [1]
RX   PUBMED; 18524951.
RA   Azuma-Mukai A, Oguri H, Mituyama T, Qian ZR, Asai K, Siomi H, Siomi MC;
RT   "Characterization of endogenous human Argonautes and their miRNA partners
RT   in RNA silencing";
RL   Proc Natl Acad Sci U S A. 105:7964-7969(2008).
XX
DR   HGNC; 35383; MIR1539.
DR   ENTREZGENE; 100302257; MIR1539.
XX
FH   Key             Location/Qualifiers
FH
FT   miRNA           30..50
FT                   /accession="MIMAT0007401"
FT                   /product="hsa-miR-1539"
FT                   /evidence=experimental
FT                   /experiment="ChIP-seq [1]"
XX
SQ   Sequence 50 BP; 7 A; 18 C; 17 G; 0 T; 8 other;
     ggcucugcgg ccugcaggua gcgcgaaagu ccugcgcguc ccagaugccc                   50
//
	 */
	protected void ReadGffarrayExcepRNADat(String rnadataFile) {
		TxtReadandWrite txtRead = new TxtReadandWrite(rnadataFile, false);
		ListBin<ListDetailBin> lsMiRNA = null; ListDetailBin listDetailBin = null;
		super.locHashtable = new LinkedHashMap<String, ListDetailBin>();
		super.LOCIDList = new ArrayList<String>();
		boolean flagSpecies = false;//����Ƿ�Ϊ������Ҫ������
		boolean flagSQ = false;//����Ƿ���ȡ����
		for (String string : txtRead.readlines()) {
			if (string.startsWith("//")) {
				flagSpecies = false;
				flagSQ = false;
			}
			String[] sepInfo = string.split(" +");
			if (string.startsWith("ID") && sepInfo[4].contains(species)) {
				flagSpecies = true;
				lsMiRNA = new ListBin<ListDetailBin>();
				lsMiRNA.setName(sepInfo[1].toLowerCase());
				lsMiRNA.setCis5to3(true);
				//װ��chrHash
				getChrhash().put(lsMiRNA.getName(), lsMiRNA);
			}
			if (flagSpecies && sepInfo[0].equals("FT")) {
				if (sepInfo[1].equals("miRNA")) {
					listDetailBin = new ListDetailBin();
					listDetailBin.setCis5to3(true);
					//30..50
					String[] loc = sepInfo[2].split("\\.\\.");
					listDetailBin.setStartAbs(Integer.parseInt(loc[0]));
					listDetailBin.setEndAbs(Integer.parseInt(loc[1]));
					lsMiRNA.add(listDetailBin);
				}
				if (sepInfo[1].contains("product")) {
					String accID = sepInfo[1].split("=")[1];
					accID = accID.replace("\"", "");
					listDetailBin.setName(accID);
					locHashtable.put(listDetailBin.getName(), listDetailBin);
					LOCIDList.add(listDetailBin.getName());
				}
			}
			if (flagSpecies && sepInfo[0].equals("SQ")) {
				flagSQ = true;
			}
			if (flagSQ) {
				//TODO ��ȡ����
			}
		}
	}
	
	protected void ReadGffarrayExcepMirReap(String rnadataFile) {
		TxtReadandWrite txtRead = new TxtReadandWrite(rnadataFile, false);
		ListBin<ListDetailBin> lsMiRNA = null; ListDetailBin listDetailBin = null;
		super.locHashtable = new LinkedHashMap<String, ListDetailBin>();
		super.LOCIDList = new ArrayList<String>();
		int start = 0; int end = 0;
		boolean cis5to3 = true;
		for (String string : txtRead.readlines()) {
			String[] ss = string.split("\t");
			String name = ss[8].split(";")[0].split("=")[1].toLowerCase();
			if (ss[2].startsWith("precursor") ) {
				lsMiRNA = new ListBin<ListDetailBin>();
				lsMiRNA.setName(name);
				lsMiRNA.setCis5to3(true);
				cis5to3 = ss[6].equals("+");
				if (cis5to3) {
					start = Integer.parseInt(ss[3]);
					end = Integer.parseInt(ss[4]);
				}
				else {
					start = Integer.parseInt(ss[4]);
					end = Integer.parseInt(ss[3]);
				}
				//װ��chrHash
				getChrhash().put(lsMiRNA.getName(), lsMiRNA);
			}
			if (ss[2].startsWith("mature")) {
				listDetailBin = new ListDetailBin();
				listDetailBin.setCis5to3(true);
				//30..50
				listDetailBin.setName(name);
				if (cis5to3) {
					listDetailBin.setStartAbs(Integer.parseInt(ss[3]) - start);
					listDetailBin.setEndAbs(Integer.parseInt(ss[4]) - start);
				}
				else {
					listDetailBin.setStartAbs(start - Integer.parseInt(ss[4]));
					listDetailBin.setEndAbs(start - Integer.parseInt(ss[3]));
				}
				lsMiRNA.add(listDetailBin);
				locHashtable.put(listDetailBin.getName(), listDetailBin);
				LOCIDList.add(listDetailBin.getName());
			}
		}
	}
	/**
	 * ��ȡmirdeep�ļ����µ�run_26_06_2012_t_12_25_36/output.mrd
	 * @param rnadataFile
	 */
	protected void ReadGffarrayExcepMirDeep(String rnadataFile) {
		TxtReadandWrite txtRead = new TxtReadandWrite(rnadataFile, false);
		ListBin<ListDetailBin> lsMiRNA = null; ListDetailBin listDetailBin = null;
		super.locHashtable = new LinkedHashMap<String, ListDetailBin>();
		super.LOCIDList = new ArrayList<String>();
		for (String string : txtRead.readlines()) {
			if (string.startsWith(">") ) {
				lsMiRNA = new ListBin<ListDetailBin>();
				lsMiRNA.setName(string.substring(1).trim());
				lsMiRNA.setCis5to3(true);
				//װ��chrHash
				getChrhash().put(lsMiRNA.getName(), lsMiRNA);
			}
			if (string.startsWith("exp")) {
				String mirModel = string.replace("exp", "").trim();
				setMatureMiRNAdeep(lsMiRNA, mirModel);
			}
		}
	}
	/**
	 * fffffffffffffffffffffMMMMMMMMMMMMMMMMMMMMMMllllllllllllllllllllllllllllllllSSSSSSSSSSSSSSSSSSSSSSfffffffffffffffff
	 * M: mature mirna
	 * S: star mirna
	 */
	private void setMatureMiRNAdeep(ListBin<ListDetailBin> lsMirnaMautre, String mirModelString) {
		char[] mirModel = mirModelString.toCharArray();
		boolean MstartFlag = false;
		boolean SstartFlag = false;
		ListDetailBin listDetailBin = null;
		for (int i = 0; i < mirModel.length; i++) {
			if (mirModel[i] == 'f' || mirModel[i] == 'I') {
				if (MstartFlag) {
					listDetailBin.setEndAbs(i);
					MstartFlag = false;
				}
				if (SstartFlag) {
					listDetailBin.setEndAbs(i);
					SstartFlag = false;
				}
				continue;
			}
			else if (mirModel[i] == 'M') {
				if (!MstartFlag) {
					listDetailBin = new ListDetailBin();
					listDetailBin.setName(lsMirnaMautre.getName() + "_mature");
					listDetailBin.setCis5to3(true);
					listDetailBin.setStartAbs(i+1);
					lsMirnaMautre.add(listDetailBin);
					MstartFlag = true;
				}
			}
			else if (mirModel[i] == 'S') {
				if (!SstartFlag) {
					listDetailBin = new ListDetailBin();
					listDetailBin.setName(lsMirnaMautre.getName() + "_star");
					listDetailBin.setCis5to3(true);
					listDetailBin.setStartAbs(i+1);
					lsMirnaMautre.add(listDetailBin);
					SstartFlag = true;
				}
			}
		}
	}
	/**
	 * ���û���ҵ����򷵻�null
	 * @param mirName mir������
	 * @param start �����
	 * @param end
	 * @return
	 */
	public String searchMirName(String mirName, int start, int end) {
		ListCodAbsDu<ListDetailBin,ListCodAbs<ListDetailBin>> lsDu = searchLocation(mirName, start, end);
		if (lsDu == null) {
			logger.error("����δ֪miRNA���֣�" + mirName);
			return null;
		}
		ArrayList<ListDetailBin> lsResult = lsDu.getAllGffDetail();
		if (lsResult.size() == 0) {
			return null;
		}
		return lsResult.get(0).getName();
	}
	
	public static HashMap<String, Integer> getMapType2TypeID() {
		HashMap<String, Integer> mapType2TypID = new LinkedHashMap<String, Integer>();
		mapType2TypID.put("RNAdata", TYPE_RNA_DATA);
		mapType2TypID.put("mirDeep", TYPE_MIRDEEP);
		mapType2TypID.put("mirReap", TYPE_MIREAP);
		return mapType2TypID;
	}
	
}
