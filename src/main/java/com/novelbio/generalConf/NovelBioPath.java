package com.novelbio.generalConf;

import java.util.HashMap;

import com.novelbio.base.dataOperate.TxtReadandWrite;

/**
 * 与路径相关的常量就在这里保存
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

	//////////////////////////////////R系列//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//	public static String R_SCRIPT = "Rscript ";
//
//	/**
//	 * "/media/winE/Bioinformatics/R/practice_script/platform/"
//	 */
//	public static String R_WORKSPACE = "/media/winE/Bioinformatics/R/practice_script/platform/";
	
	public static String R_WORKSPACE = "D:/Library/R//";
	public static String R_SCRIPT = "D:/tools/R2_14/bin/Rscript ";
	
	/////////////////////////////常规Fisher/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 常规Fisher的输入文件
	 */
	public static String R_WORKSPACE_FISHER = R_WORKSPACE + "Fisher/";
	/**
	 * 常规Fisher的输入文件
	 */
	public static String R_WORKSPACE_FISHER_INFO = R_WORKSPACE_FISHER + "Info.txt";
	/**
	 * 常规Fisher的计算脚本
	 */
	public static String R_WORKSPACE_FISHER_SCRIPT= R_WORKSPACE + "FisherBHfdr.R";
	/**
	 * 常规Fisher的结果文件
	 */
	public static String R_WORKSPACE_FISHER_RESULT = R_WORKSPACE_FISHER + "Analysis.txt";
	///////////////////////////TopGo的ElimFisher/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * topGo的文件夹
	 */
	public static String R_WORKSPACE_TOPGO = R_WORKSPACE + "topGO/";
	/**
	 * topGo的R脚本文件
	 */
	public static String R_WORKSPACE_TOPGO_RSCRIPT = R_WORKSPACE + "topGO.R";
	/**
	 * topGo的参数
	 * 参数文本，第一个记录选择BP、MF、CC，第二个记录写文件，默认是GoResult.txt
	 * 第三个是数字，表示显示多少个GOTerm ,第四个记录GOInfo，就是每个GO对应的基因文件名
	 */
	public static String R_WORKSPACE_TOPGO_PARAM = R_WORKSPACE_TOPGO + "parameter.txt";
	/**
	 * topGo的gene Go Info的结果文件，最后整合进入excel
	 */
	public static String R_WORKSPACE_TOPGO_GENEGOINFO = R_WORKSPACE_TOPGO + "GeneGOInfo.txt";
	/**
	 * topGo的gene go,go,go的结果文件
	 */
	public static String R_WORKSPACE_TOPGO_BGGeneGo = R_WORKSPACE_TOPGO + "BG2Go.txt";
	/**
	 * topGo的包含elimFisher的结果table文件
	 */
	public static String R_WORKSPACE_TOPGO_GORESULT = R_WORKSPACE_TOPGO + "GoResult.txt";
	/**
	 * topGo的包含elimFisher的图片
	 */
	public static String R_WORKSPACE_TOPGO_GOMAP = R_WORKSPACE_TOPGO + "tGOall_elim_10_def.pdf";
	/**
	 * topGo的包含elimFisher的结果table文件
	 * 每个GO里面所含有的背景基因
	 * 格式为<br>
	 * #GO:010101<br>
	 * NM_0110101
	 */
	public static String R_WORKSPACE_TOPGO_GOINFO = R_WORKSPACE_TOPGO + "GOInfo.txt";
	/**
	 * topGo计算需要的GeneID
	 */
	public static String R_WORKSPACE_TOPGO_GENEID = R_WORKSPACE_TOPGO + "GeneID.txt";
	/////////////////////////  limma microarray 的脚本  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * microarray的文件夹
	 */
	public static String R_WORKSPACE_MICROARRAY = R_WORKSPACE + "microarray/";
	/**
	 * 标准化的脚本路径
	 */
	public static String R_WORKSPACE_MICROARRAY_NORMLIZATION = R_WORKSPACE_MICROARRAY + "norm.R";
	/**
	 * limma筛选差异基因的脚本
	 */
	public static String R_WORKSPACE_MICROARRAY_COMPARE = R_WORKSPACE_MICROARRAY + "limma.txt";
	/**
	 * limma临时的标准化基因文件
	 */
	public static String R_WORKSPACE_MICROARRAY_NORMDATA_TMP = R_WORKSPACE_MICROARRAY + "tmpNormGene.txt";
	/////////////////////////基因结构的分析////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 保存基因结构文件的路径，用于保存ChIP-Seq的Peak在基因组上分布的统计分析
	 */
	public static String R_WORKSPACE_CHIP_GENESTRUCTURE = R_WORKSPACE + "GeneStructure/";
	/**
	 * 保存基因结构文件的路径，用于保存ChIP-Seq的Peak在基因组上分布的统计分析
	 */
	public static String R_WORKSPACE_CHIP_GENESTRUCTURE_FILE = R_WORKSPACE_CHIP_GENESTRUCTURE + "GeneStructureStatistics.txt";
	/**
	 * 保存基因结构文件的路径，用于保存ChIP-Seq的Peak在基因组上分布的统计分析
	 */
	public static String R_WORKSPACE_CHIP_GENESTRUCTURE_RSCRIPT = R_WORKSPACE + "MyBarPlotGeneStructure.R";
	/**
	 * 保存基因结构文件结果图
	 */
	public static String R_WORKSPACE_CHIP_GENESTRUCTURE_RESULT_PIC = R_WORKSPACE_CHIP_GENESTRUCTURE + "batPlot.jpg";
	/////////////////////////Reads在genome上的分布///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * reads在genome上分布的信息文件夹
	 */
	public static String R_WORKSPACE_CHIP_CHRREADS = R_WORKSPACE + "chrReads/";
	/**
	 * reads在genome上分布的信息，提供给R画图的参数文件
	 */
	public static String R_WORKSPACE_CHIP_CHRREADS_PARAM = R_WORKSPACE_CHIP_CHRREADS + "parameter";
	/**
	 * reads在genome上分布的输入数据，X轴
	 */
	public static String R_WORKSPACE_CHIP_CHRREADS_X = R_WORKSPACE_CHIP_CHRREADS + "readsx";
	/**
	 * reads在genome上分布的输入数据，Y轴,也就是实际reads的分布
	 */
	public static String R_WORKSPACE_CHIP_CHRREADS_Y = R_WORKSPACE_CHIP_CHRREADS + "readsy";
	/**
	 * reads在genome上分布的输入数据，如果分开正负链，也就是有第二个mapping文件，这是第二个文件的Y轴,也就是实际第二个文件的reads分布
	 */
	public static String R_WORKSPACE_CHIP_CHRREADS_2Y = R_WORKSPACE_CHIP_CHRREADS + "reads2y";
	/**
	 * reads在genome上分布的输入数据，如果分开正负链，也就是有第二个mapping文件，这是第二个文件的Y轴,也就是实际第二个文件的reads分布
	 */
	public static String R_WORKSPACE_CHIP_CHRREADS_RSCRIPT = R_WORKSPACE + "MyChrReads.R";
	/////////////////////////Reads在特定Region如Tss和GeneEnd上的分布///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * reads在特定Region如Tss和GeneEnd上的分布的文件夹
	 */
	public static String R_WORKSPACE_CHIP_READS_REGION = R_WORKSPACE + "regionReads/";
	/**
	 * reads在Tss上的分布信息文件，20个一行，用于R读取
	 */
	public static String R_WORKSPACE_CHIP_READS_REGION_TSS_R = R_WORKSPACE_CHIP_READS_REGION + "tss.txt";
	/**
	 * reads在geneEnd上的分布信息文件
	 */
	public static String R_WORKSPACE_CHIP_READS_REGION_GENEEND_R = R_WORKSPACE_CHIP_READS_REGION + "geneEnd.txt";
	/**
	 * reads在特定Region如Tss和GeneEnd上的分布，画图参数
	 */
	public static String R_WORKSPACE_CHIP_READS_REGION_PARAM = R_WORKSPACE_CHIP_READS_REGION + "parameter.txt";
	/**
	 * reads在TSS上的图
	 */
	public static String R_WORKSPACE_CHIP_READS_REGION_TSS_PIC = R_WORKSPACE_CHIP_READS_REGION + "TSSReads.jpg";	
	/**
	 * reads在GeneEnd上的图
	 */
	public static String R_WORKSPACE_CHIP_READS_REGION_GENEEND_PIC = R_WORKSPACE_CHIP_READS_REGION + "GeneEndReads.jpg";	
	/**
	 * reads在TSS上的分布信息文件，一列，用于excel使用
	 */
	public static String R_WORKSPACE_CHIP_READS_REGION_TSS_EXCEL = R_WORKSPACE_CHIP_READS_REGION + "tss2.txt";	
	/**
	 * reads在GeneEnd上的分布信息文件，一列，用于excel使用
	 */
	public static String R_WORKSPACE_CHIP_READS_REGION_GENEEND_EXCEL = R_WORKSPACE_CHIP_READS_REGION + "geneEnd2.txt";	
	/**
	 * reads在TSS上的分布信息文件，一列，用于excel使用
	 */
	public static String R_WORKSPACE_CHIP_READS_REGION_TSS_RSCRIPT = R_WORKSPACE + "MyTSSReads.R";	
	/**
	 * reads在GeneEnd上的分布信息文件，一列，用于excel使用
	 */
	public static String R_WORKSPACE_CHIP_READS_REGION_GENEEND_RSCRIPT = R_WORKSPACE + "MyGeneEndReads.R";	
	/////////////////////////R的density计算，可用于计算motif在指定序列上的分布///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * R的density计算的文件夹，可用于计算motif在指定序列上的分布
	 */
	public static String R_WORKSPACE_DENSITY = R_WORKSPACE + "NormalDensity/";
	/**
	 * R的density计算的参数文件，保存本次计算的数据来源和数量，可用于计算motif在指定序列上的分布和Peak在TSS分布<br>
	 * 第一行大标题第二行xtitle，第三行ytitle
	 */
	public static String R_WORKSPACE_DENSITY_PARAM = R_WORKSPACE_DENSITY + "param.txt";
	/**
	 * R的density计算的计算文件，保存本次计算的数据，可用于计算motif在指定序列上的分布<br>
	 * 数据每行一个，写成一列，没有标题
	 */
	public static String R_WORKSPACE_DENSITY_DATA = R_WORKSPACE_DENSITY + "data.txt";
	/**
	 * R的density计算的脚本
	 */
	public static String R_WORKSPACE_DENSITY_RSCRIPT = R_WORKSPACE + "NormalDensity.R";
	/**
	 * R的density计算的结果图片，保存本次计算的数据，可用于计算motif在指定序列上的分布
	 */
	public static String R_WORKSPACE_DENSITY_PIC = R_WORKSPACE_DENSITY + "density.jpeg";
	
	
}
