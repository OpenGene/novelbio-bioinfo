package novelbiotest;


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
import com.novelBio.base.fileOperate.FileOperate;
import com.novelBio.base.genome.getChrSequence.ChrStringHash;
import com.novelBio.base.genome.gffOperate.GffHashPlantGene;
import com.novelBio.chIPSeq.preprocess.Comb;
import com.novelBio.chIPSeq.preprocess.MapPeak;
import com.novelBio.chIPSeq.preprocess.QualityCol;
import com.novelBio.tools.formatConvert.bedFormat.Soap2Bed;



public class myTest {
	public static void main(String[] args) throws Exception 
	{
		String thisPath = "/media/winD/fedora/workspace/NBCplatform/"; String prix = "CSA";
		String outFilePath = "/media/winE/bioinformaticsTools/soapTmpFile/bedFile/peakCalling";
		System.out.println(thisPath);
		File fileOld = new File("/media/winD/fedora/workspace/NBCplatform/CSA_peaks.xls");
		File filenew = new File("/media/winE/bioinformaticsTools/soapTmpFile/bedFile/peakCalling/CSA_peaks.xls");
		fileOld.renameTo(filenew);
		FileOperate.moveFile(thisPath+"/"+prix+"_peaks.xls", outFilePath);
		FileOperate.moveFile(thisPath+"/"+prix+"_peaks.bed", outFilePath+"/TmpPeakInfo/");
		FileOperate.moveFile(thisPath+"/"+prix+"_negative_peaks.xls", outFilePath+"/TmpPeakInfo/");
		FileOperate.moveFile(thisPath+"/"+prix+"_model.r", outFilePath+"/TmpPeakInfo/");
		FileOperate.moveFile(thisPath+"/"+prix+"_diag.xls", outFilePath+"/TmpPeakInfo/");
		FileOperate.moveFile(thisPath+"/"+prix+"_summits.bed", outFilePath+"/TmpPeakInfo/");
		FileOperate.moveFolder(thisPath+"/"+prix+"_MACS_wiggle", outFilePath+"/TmpPeakInfo/"+prix+"_MACS_wiggle");
//		FileOperate.delFile(outFilePath+"/macs.sh");
		CmdOperate cm = new CmdOperate("pwd");
		cm.doInBackground();
//		MapPeak.peakCalMacs(outPutTreat, outPutCol, "os", outFilePath, "CSA");
	}
	
	
	
	
	
	 public static String getProjectPath() {
		 java.net.URL url = Comb.class.getProtectionDomain().getCodeSource().getLocation();
		 String filePath = null;
		 try {
		 filePath = java.net.URLDecoder.decode(url.getPath(), "utf-8");
		 } catch (Exception e) {
		 e.printStackTrace();
		 }
		 if (filePath.endsWith(".jar"))
		 filePath = filePath.substring(0, filePath.lastIndexOf("/") + 1);
		 java.io.File file = new java.io.File(filePath);
		 filePath = file.getAbsolutePath();
		 return filePath;
		 }
	 
}
