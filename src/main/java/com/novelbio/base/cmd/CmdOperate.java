package com.novelbio.base.cmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingWorker;

public class CmdOperate {

	/**
	 * 输入cmd，执行完毕后可以将结果输出到界面，目前cmd只支持英文，否则会出错
	 * 只要继承后重写process方法即可
	 * 如果只是随便用用，那么调用doInBackground方法就好
	 * @author zong0jie
	 *
	 */

		String cmd = "";
		public CmdOperate(String cmd) {
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
							System.out.println(info);
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
				System.out.println(line);
				lsIn.add(line);
			}
			ArrayList<ArrayList<String>> lsResult = new ArrayList<ArrayList<String>>();
			lsResult.add(lsIn); lsResult.add(lsErr);
			return lsResult;
		}
		
		
}




