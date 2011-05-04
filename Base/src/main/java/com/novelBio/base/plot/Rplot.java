package com.novelBio.base.plot;

import java.io.IOException;

import com.novelBio.base.dataOperate.TxtReadandWrite;
import com.novelBio.base.fileOperate.FileOperate;
import com.novelBio.generalConf.NovelBioConst;

public class Rplot {
	/**
	 * 
	 * ��R��ֱ��ͼ,��ͼ����ͼ����
	 * @param data
	 * @param min ���ݵ���Сֵ �����Сֵ���ڵ������ֵ����ô�Ͳ����й���
	 * @param max ���ݵ����ֵ  �����Сֵ���ڵ������ֵ����ô�Ͳ����й���
	 * @param mainTitle ������
	 * @param xTitle x����
	 * @param yTitle y����
	 * @param resultPath ���·��
	 * @param resultPrix ����ļ���ǰ׺���������density
	 * @throws Exception
	 */
	public static void plotHist(double[] data,double min,double max,String mainTitle,String xTitle,String yTitle,String resultPath,String resultPrix) throws Exception {
		//д������
		TxtReadandWrite txtR = new TxtReadandWrite();
		txtR.setParameter(NovelBioConst.R_WORKSPACE_DENSITY_DATA, true, false);
		if (min < max) {
			for (double d : data) {
				if (d>=min && d<=max) {
					txtR.writefile(d+"\n");
				}
			}
		}
		else {
			for (double d : data) {
				txtR.writefile(d+"\n");
			}
		}
		txtR.close();
		TxtReadandWrite txtTitle = new TxtReadandWrite();
		txtTitle.setParameter(NovelBioConst.R_WORKSPACE_DENSITY_PARAM, true, false);
		txtTitle.writefile(mainTitle+"\n");
		txtTitle.writefile(xTitle+"\n");
		txtTitle.writefile(yTitle);
		txtTitle.close();
		rscript(NovelBioConst.R_WORKSPACE_DENSITY_RSCRIPT);
		FileOperate.moveFoldFile(NovelBioConst.R_WORKSPACE_DENSITY, resultPath, resultPrix,true);
	}
	
	/**
	 * ִ��R����ֱ��R��������ٷ���
	 * @return
	 * @throws IOException 
	 * @throws IOException 
	 * @throws InterruptedException 
	 * @throws Exception
	 */
	private static int rscript(String scriptPath) throws IOException, InterruptedException  
	{
		//����������·���������ڵ�ǰ�ļ���������
		String command="Rscript "+scriptPath;
		Runtime   r=Runtime.getRuntime();
		Process p = r.exec(command);
		p.waitFor();
		return 1;
	}
}