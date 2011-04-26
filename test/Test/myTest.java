package Test;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.color.CMMException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tc33.jheatchart.HeatChart;

import com.google.common.base.Splitter;
import com.google.common.io.Files;
import com.novelBio.base.cmd.CmdOperate;
import com.novelBio.base.cmd.CmdOperateGUI;
import com.novelBio.base.dataOperate.TxtReadandWrite;
import com.novelBio.base.genome.getChrSequence.ChrStringHash;
import com.novelBio.base.genome.gffOperate.GffHashPlantGene;
import com.novelBio.chIPSeq.preprocess.MapPeak;
import com.novelBio.chIPSeq.preprocess.QualityCol;
import com.novelBio.tools.formatConvert.bedFormat.Soap2Bed;



public class myTest {
	public static void main(String[] args) throws Exception 
	{
		MapPeak.sortBedFile("/media/winE/bioinformaticsTools/soapTmpFile/test","/media/winE/bioinformaticsTools/soapTmpFile/test/K0_comb", 1, "/media/winE/bioinformaticsTools/soapTmpFile/test/K0_combSort", 2,3);
		long readsNum = 0;
		TxtReadandWrite txt = new TxtReadandWrite();
		txt.setParameter("/media/winE/bioinformaticsTools/soapTmpFile/K0.fq/K0.fq", false, true);
		BufferedReader reader = txt.readfile();
		while (reader.readLine()!=null) {
			readsNum++;
		}
		readsNum = readsNum/4;
		ArrayList<String[]> lsResult = QualityCol.calCover("/media/winE/bioinformaticsTools/soapTmpFile/test/K0_combSort", "/media/winE/Bioinformatics/GenomeData/mouse/ucsc_mm9/statisticInfo/chrLengthInfo.txt", readsNum, false, false, "");
		txt.setParameter("/media/winE/bioinformaticsTools/soapTmpFile/test/K0report.txt", true, false);
		txt.ExcelWrite(lsResult, "\t", 1, 1);
		txt.close();
	}
}
