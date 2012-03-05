package com.novelbio.generalConf;

import java.util.HashMap;

import com.novelbio.base.dataOperate.TxtReadandWrite;

/**
 * ��·����صĳ����������ﱣ��
 * @author zong0jie
 *
 */
public class NovelBioPath {
	static String txtPathFile = "";
	static HashMap<String, String> hashPathName2Path = new HashMap<String, String>();
	static
	{
		setPath();
	}
	
	public static void setPath()
	{
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite(txtPathFile, false);
		for (String string : txtReadandWrite.readlines()) {
			string = string.trim();
			if (string.startsWith("#") || string.equals("")) {
				continue;
			}
			String[] info = string.split("\t");
			hashPathName2Path.put(info[0], info[1]);
		}
//		PEAKCALLING_SICER_PATH = hashPathName2Path.get("PEAKCALLING_SICER_PATH");
		 R_WORKSPACE = hashPathName2Path.get("R_WORKSPACE");
		 R_SCRIPT = hashPathName2Path.get("R_SCRIPT");
		 R_WORKSPACE_FISHER = hashPathName2Path.get("R_WORKSPACE_FISHER");
		 R_WORKSPACE_FISHER_INFO = hashPathName2Path.get("R_WORKSPACE_FISHER_INFO");
		 R_WORKSPACE_FISHER_SCRIPT = hashPathName2Path.get("R_WORKSPACE_FISHER_SCRIPT");
		 R_WORKSPACE_FISHER_RESULT = hashPathName2Path.get("R_WORKSPACE_FISHER_RESULT");
		 R_WORKSPACE_TOPGO = hashPathName2Path.get("R_WORKSPACE_TOPGO");
		 R_WORKSPACE_TOPGO_RSCRIPT = hashPathName2Path.get("R_WORKSPACE_TOPGO_RSCRIPT");
		 R_WORKSPACE_TOPGO_PARAM = hashPathName2Path.get("R_WORKSPACE_TOPGO_PARAM");
		 R_WORKSPACE_TOPGO_GENEGOINFO = hashPathName2Path.get("R_WORKSPACE_TOPGO_GENEGOINFO");
		 R_WORKSPACE_TOPGO_BGGeneGo = hashPathName2Path.get("R_WORKSPACE_TOPGO_BGGeneGo");
		 R_WORKSPACE_TOPGO_GOMAP = hashPathName2Path.get("R_WORKSPACE_TOPGO_GOMAP");
		 R_WORKSPACE_TOPGO_GOINFO = hashPathName2Path.get("R_WORKSPACE_TOPGO_GOINFO");
		 R_WORKSPACE_TOPGO_GENEID = hashPathName2Path.get("R_WORKSPACE_TOPGO_GENEID");
		 R_WORKSPACE_MICROARRAY = hashPathName2Path.get("R_WORKSPACE_MICROARRAY");
		 R_WORKSPACE_MICROARRAY_NORMLIZATION  = hashPathName2Path.get("R_WORKSPACE_MICROARRAY_NORMLIZATION");
		 R_WORKSPACE_MICROARRAY_COMPARE = hashPathName2Path.get("R_WORKSPACE_MICROARRAY_COMPARE");
		 R_WORKSPACE_MICROARRAY_NORMDATA_TMP = hashPathName2Path.get("R_WORKSPACE_MICROARRAY_NORMDATA_TMP");
	}

	//////////////////////////////////Rϵ��//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//	public static String R_SCRIPT = "Rscript ";
//
//	/**
//	 * "/media/winE/Bioinformatics/R/practice_script/platform/"
//	 */
//	public static String R_WORKSPACE = "/media/winE/Bioinformatics/R/practice_script/platform/";
	
	public static String R_WORKSPACE = "D:/Library/R//";
	public static String R_SCRIPT = "D:/tools/R2_14/bin/Rscript ";
	
	/////////////////////////////����Fisher/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * ����Fisher�������ļ�
	 */
	public static String R_WORKSPACE_FISHER = R_WORKSPACE + "Fisher/";
	/**
	 * ����Fisher�������ļ�
	 */
	public static String R_WORKSPACE_FISHER_INFO = R_WORKSPACE_FISHER + "Info.txt";
	/**
	 * ����Fisher�ļ���ű�
	 */
	public static String R_WORKSPACE_FISHER_SCRIPT= R_WORKSPACE + "FisherBHfdr.R";
	/**
	 * ����Fisher�Ľ���ļ�
	 */
	public static String R_WORKSPACE_FISHER_RESULT = R_WORKSPACE_FISHER + "Analysis.txt";
	///////////////////////////TopGo��ElimFisher/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * topGo���ļ���
	 */
	public static String R_WORKSPACE_TOPGO = R_WORKSPACE + "topGO/";
	/**
	 * topGo��R�ű��ļ�
	 */
	public static String R_WORKSPACE_TOPGO_RSCRIPT = R_WORKSPACE + "topGO.R";
	/**
	 * topGo�Ĳ���
	 * �����ı�����һ����¼ѡ��BP��MF��CC���ڶ�����¼д�ļ���Ĭ����GoResult.txt
	 * �����������֣���ʾ��ʾ���ٸ�GOTerm ,���ĸ���¼GOInfo������ÿ��GO��Ӧ�Ļ����ļ���
	 */
	public static String R_WORKSPACE_TOPGO_PARAM = R_WORKSPACE_TOPGO + "parameter.txt";
	/**
	 * topGo��gene Go Info�Ľ���ļ���������Ͻ���excel
	 */
	public static String R_WORKSPACE_TOPGO_GENEGOINFO = R_WORKSPACE_TOPGO + "GeneGOInfo.txt";
	/**
	 * topGo��gene go,go,go�Ľ���ļ�
	 */
	public static String R_WORKSPACE_TOPGO_BGGeneGo = R_WORKSPACE_TOPGO + "BG2Go.txt";
	/**
	 * topGo�İ���elimFisher�Ľ��table�ļ�
	 */
	public static String R_WORKSPACE_TOPGO_GORESULT = R_WORKSPACE_TOPGO + "GoResult.txt";
	/**
	 * topGo�İ���elimFisher��ͼƬ
	 */
	public static String R_WORKSPACE_TOPGO_GOMAP = R_WORKSPACE_TOPGO + "tGOall_elim_10_def.pdf";
	/**
	 * topGo�İ���elimFisher�Ľ��table�ļ�
	 * ÿ��GO���������еı�������
	 * ��ʽΪ<br>
	 * #GO:010101<br>
	 * NM_0110101
	 */
	public static String R_WORKSPACE_TOPGO_GOINFO = R_WORKSPACE_TOPGO + "GOInfo.txt";
	/**
	 * topGo������Ҫ��GeneID
	 */
	public static String R_WORKSPACE_TOPGO_GENEID = R_WORKSPACE_TOPGO + "GeneID.txt";
	/////////////////////////  limma microarray �Ľű�  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * microarray���ļ���
	 */
	public static String R_WORKSPACE_MICROARRAY = R_WORKSPACE + "microarray/";
	/**
	 * ��׼���Ľű�·��
	 */
	public static String R_WORKSPACE_MICROARRAY_NORMLIZATION = R_WORKSPACE_MICROARRAY + "norm.R";
	/**
	 * limmaɸѡ�������Ľű�
	 */
	public static String R_WORKSPACE_MICROARRAY_COMPARE = R_WORKSPACE_MICROARRAY + "limma.txt";
	/**
	 * limma��ʱ�ı�׼�������ļ�
	 */
	public static String R_WORKSPACE_MICROARRAY_NORMDATA_TMP = R_WORKSPACE_MICROARRAY + "tmpNormGene.txt";
	/////////////////////////����ṹ�ķ���////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * �������ṹ�ļ���·�������ڱ���ChIP-Seq��Peak�ڻ������Ϸֲ���ͳ�Ʒ���
	 */
	public static String R_WORKSPACE_CHIP_GENESTRUCTURE = R_WORKSPACE + "GeneStructure/";
	/**
	 * �������ṹ�ļ���·�������ڱ���ChIP-Seq��Peak�ڻ������Ϸֲ���ͳ�Ʒ���
	 */
	public static String R_WORKSPACE_CHIP_GENESTRUCTURE_FILE = R_WORKSPACE_CHIP_GENESTRUCTURE + "GeneStructureStatistics.txt";
	/**
	 * �������ṹ�ļ���·�������ڱ���ChIP-Seq��Peak�ڻ������Ϸֲ���ͳ�Ʒ���
	 */
	public static String R_WORKSPACE_CHIP_GENESTRUCTURE_RSCRIPT = R_WORKSPACE + "MyBarPlotGeneStructure.R";
	/**
	 * �������ṹ�ļ����ͼ
	 */
	public static String R_WORKSPACE_CHIP_GENESTRUCTURE_RESULT_PIC = R_WORKSPACE_CHIP_GENESTRUCTURE + "batPlot.jpg";
	/////////////////////////Reads��genome�ϵķֲ�///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * reads��genome�Ϸֲ�����Ϣ�ļ���
	 */
	public static String R_WORKSPACE_CHIP_CHRREADS = R_WORKSPACE + "chrReads/";
	/**
	 * reads��genome�Ϸֲ�����Ϣ���ṩ��R��ͼ�Ĳ����ļ�
	 */
	public static String R_WORKSPACE_CHIP_CHRREADS_PARAM = R_WORKSPACE_CHIP_CHRREADS + "parameter";
	/**
	 * reads��genome�Ϸֲ����������ݣ�X��
	 */
	public static String R_WORKSPACE_CHIP_CHRREADS_X = R_WORKSPACE_CHIP_CHRREADS + "readsx";
	/**
	 * reads��genome�Ϸֲ����������ݣ�Y��,Ҳ����ʵ��reads�ķֲ�
	 */
	public static String R_WORKSPACE_CHIP_CHRREADS_Y = R_WORKSPACE_CHIP_CHRREADS + "readsy";
	/**
	 * reads��genome�Ϸֲ����������ݣ�����ֿ���������Ҳ�����еڶ���mapping�ļ������ǵڶ����ļ���Y��,Ҳ����ʵ�ʵڶ����ļ���reads�ֲ�
	 */
	public static String R_WORKSPACE_CHIP_CHRREADS_2Y = R_WORKSPACE_CHIP_CHRREADS + "reads2y";
	/**
	 * reads��genome�Ϸֲ����������ݣ�����ֿ���������Ҳ�����еڶ���mapping�ļ������ǵڶ����ļ���Y��,Ҳ����ʵ�ʵڶ����ļ���reads�ֲ�
	 */
	public static String R_WORKSPACE_CHIP_CHRREADS_RSCRIPT = R_WORKSPACE + "MyChrReads.R";
	/////////////////////////Reads���ض�Region��Tss��GeneEnd�ϵķֲ�///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * reads���ض�Region��Tss��GeneEnd�ϵķֲ����ļ���
	 */
	public static String R_WORKSPACE_CHIP_READS_REGION = R_WORKSPACE + "regionReads/";
	/**
	 * reads��Tss�ϵķֲ���Ϣ�ļ���20��һ�У�����R��ȡ
	 */
	public static String R_WORKSPACE_CHIP_READS_REGION_TSS_R = R_WORKSPACE_CHIP_READS_REGION + "tss.txt";
	/**
	 * reads��geneEnd�ϵķֲ���Ϣ�ļ�
	 */
	public static String R_WORKSPACE_CHIP_READS_REGION_GENEEND_R = R_WORKSPACE_CHIP_READS_REGION + "geneEnd.txt";
	/**
	 * reads���ض�Region��Tss��GeneEnd�ϵķֲ�����ͼ����
	 */
	public static String R_WORKSPACE_CHIP_READS_REGION_PARAM = R_WORKSPACE_CHIP_READS_REGION + "parameter.txt";
	/**
	 * reads��TSS�ϵ�ͼ
	 */
	public static String R_WORKSPACE_CHIP_READS_REGION_TSS_PIC = R_WORKSPACE_CHIP_READS_REGION + "TSSReads.jpg";	
	/**
	 * reads��GeneEnd�ϵ�ͼ
	 */
	public static String R_WORKSPACE_CHIP_READS_REGION_GENEEND_PIC = R_WORKSPACE_CHIP_READS_REGION + "GeneEndReads.jpg";	
	/**
	 * reads��TSS�ϵķֲ���Ϣ�ļ���һ�У�����excelʹ��
	 */
	public static String R_WORKSPACE_CHIP_READS_REGION_TSS_EXCEL = R_WORKSPACE_CHIP_READS_REGION + "tss2.txt";	
	/**
	 * reads��GeneEnd�ϵķֲ���Ϣ�ļ���һ�У�����excelʹ��
	 */
	public static String R_WORKSPACE_CHIP_READS_REGION_GENEEND_EXCEL = R_WORKSPACE_CHIP_READS_REGION + "geneEnd2.txt";	
	/**
	 * reads��TSS�ϵķֲ���Ϣ�ļ���һ�У�����excelʹ��
	 */
	public static String R_WORKSPACE_CHIP_READS_REGION_TSS_RSCRIPT = R_WORKSPACE + "MyTSSReads.R";	
	/**
	 * reads��GeneEnd�ϵķֲ���Ϣ�ļ���һ�У�����excelʹ��
	 */
	public static String R_WORKSPACE_CHIP_READS_REGION_GENEEND_RSCRIPT = R_WORKSPACE + "MyGeneEndReads.R";	
	/////////////////////////R��density���㣬�����ڼ���motif��ָ�������ϵķֲ�///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * R��density������ļ��У������ڼ���motif��ָ�������ϵķֲ�
	 */
	public static String R_WORKSPACE_DENSITY = R_WORKSPACE + "NormalDensity/";
	/**
	 * R��density����Ĳ����ļ������汾�μ����������Դ�������������ڼ���motif��ָ�������ϵķֲ���Peak��TSS�ֲ�<br>
	 * ��һ�д����ڶ���xtitle��������ytitle
	 */
	public static String R_WORKSPACE_DENSITY_PARAM = R_WORKSPACE_DENSITY + "param.txt";
	/**
	 * R��density����ļ����ļ������汾�μ�������ݣ������ڼ���motif��ָ�������ϵķֲ�<br>
	 * ����ÿ��һ����д��һ�У�û�б���
	 */
	public static String R_WORKSPACE_DENSITY_DATA = R_WORKSPACE_DENSITY + "data.txt";
	/**
	 * R��density����Ľű�
	 */
	public static String R_WORKSPACE_DENSITY_RSCRIPT = R_WORKSPACE + "NormalDensity.R";
	/**
	 * R��density����Ľ��ͼƬ�����汾�μ�������ݣ������ڼ���motif��ָ�������ϵķֲ�
	 */
	public static String R_WORKSPACE_DENSITY_PIC = R_WORKSPACE_DENSITY + "density.jpeg";
	
	
}
