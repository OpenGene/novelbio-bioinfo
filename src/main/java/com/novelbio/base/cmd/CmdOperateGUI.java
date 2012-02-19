package com.novelbio.base.cmd;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JFrame;
import javax.swing.SwingWorker;


import com.google.common.base.Splitter;
import com.novelbio.analysis.annotation.genAnno.AnnoQuery;
import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;


/**
 * 输入cmd，执行完毕后可以将结果输出到界面，目前cmd只支持英文，否则会出错
 * 只要继承后重写process方法即可
 * 如果只是随便用用，那么调用doInBackground方法就好
 * @author zong0jie
 *
 */
public class CmdOperateGUI extends SwingWorker<ArrayList<ArrayList<String>>,ProgressData>
{
	String cmd = "";
	JFrame jFrame;
	/**
	 * 将gui界面传入java程序
	 * @param guiBlast
	 */
	public CmdOperateGUI(JFrame jFrame,String cmd) {
		this.jFrame =jFrame;
		this.cmd =cmd;
	}
	public CmdOperateGUI(String cmd) {
		this.cmd =cmd;
	}
	
	
	/**
	 * 返回两个arraylist-string 第一个是Info 第二个是error
	 * @param fileName
	 * @return
	 * @throws Exception 
	 */
	public ArrayList<ArrayList<String>> doInBackground() throws Exception 
	{
		ProgressData progressDataIn = new ProgressData();
		final ProgressData progressDataErr = new ProgressData();
		ArrayList<String> lsIn = new ArrayList<String>();
		final ArrayList<String> lsErr = new ArrayList<String>();
		
		    Process process = null;  
	     Runtime runtime = Runtime.getRuntime();  
		 process = runtime.exec(cmd);
		final InputStream is1 = process.getErrorStream();
		
		new Thread(new Runnable() {
			public void run() {
				BufferedReader br = new BufferedReader(new InputStreamReader(is1));
				String info = "";
				try {
					while((info =br.readLine()) != null)
					{
						progressDataErr.strcmdInfo = info; 
						progressDataErr.info = false;
						publish(progressDataErr);
						lsErr.add(info);
						
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start(); // 启动单独的线程来清空process.getInputStream()的缓冲区
		InputStream is2 = process.getInputStream();
		BufferedReader br2 = new BufferedReader(new InputStreamReader(is2));    
		String line = null;
		while((line = br2.readLine()) != null)
		{
			progressDataIn.strcmdInfo = line; 
			progressDataIn.info = true;
			publish(progressDataIn);
			lsIn.add(line);
		}
		ArrayList<ArrayList<String>> lsResult = new ArrayList<ArrayList<String>>();
		lsResult.add(lsIn); lsResult.add(lsErr);
		return lsResult;
	}
	
	
	/**
	 * 需要按需求添加
	 */
	@Override
	public void process(List<ProgressData> data)
	{
		if (isCancelled()) {
			return;
		}
		for (ProgressData progressData : data) {
			if (progressData.info) {
				jFrame.setTitle(progressData.strcmdInfo);
			}
		}
	}
}
class ProgressData
{
	public String strcmdInfo;
	/**
	 * true : info
	 * false : error
	 */
	public boolean info;
}


