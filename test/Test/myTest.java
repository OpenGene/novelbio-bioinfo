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



public class myTest {
	public static void main(String[] args) throws Exception 
	{
		CmdOperate cmdOperate = new CmdOperate("sh /media/winE/NBC/tmpPaper/aaa.sh");
		cmdOperate.doInBackground();
	}
}
