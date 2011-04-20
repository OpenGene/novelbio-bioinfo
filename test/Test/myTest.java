package Test;


import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
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
import com.novelBio.base.dataOperate.TxtReadandWrite;
import com.novelBio.base.genome.gffOperate.GffHashPlantGene;



public class myTest {
	public static void main(String[] args) throws Exception 
	{ 
		GffHashPlantGene gffHashPlantGene = new GffHashPlantGene();
		gffHashPlantGene.GeneName = "LOC_Os\\d{2}g\\d{5}";
		gffHashPlantGene.splitmRNA = "(?<=LOC_Os\\d{2}g\\d{5}\\.)\\d";
		gffHashPlantGene.ReadGffarray("/media/winE/Bioinformatics/GenomeData/Rice/TIGRRice/Noname1test.txt");
		gffHashPlantGene.getGeneStructureLength();
		System.out.println("ok");
	}
	
}
