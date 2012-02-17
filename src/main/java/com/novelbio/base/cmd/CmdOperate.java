package com.novelbio.base.cmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.base.dataOperate.DateTime;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class CmdOperate {
	private static Logger logger = Logger.getLogger(CmdOperate.class);
	/**
	 * ����cmd��ִ����Ϻ���Խ������������棬Ŀǰcmdֻ֧��Ӣ�ģ���������
	 * ֻҪ�̳к���дprocess��������
	 * ���ֻ��������ã���ô����doInBackground�����ͺ�
	 * @author zong0jie
	 *
	 */

		String cmd = "";
		public CmdOperate(String cmd) {
			this.cmd =cmd;
		}
		
		public ArrayList<ArrayList<String>> doInBackground() {
			try {
				logger.info(cmd);
				return doInBackgroundB();
			} catch (Exception e) {
				logger.error("cmd cannot executed correctly: "+cmd);
				return null;
			}
			
		}
		
		/**
		 * ��������arraylist-string ��һ����Info �ڶ�����error
		 * @param fileName
		 * @return
		 * @throws Exception 
		 * @throws Exception 
		 */
		public ArrayList<ArrayList<String>> doInBackgroundB() throws Exception
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
							logger.error(info);
							System.out.println(info);
							lsErr.add(info);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).start(); // �����������߳������process.getInputStream()�Ļ�����
//			InputStream is2 = process.getInputStream();
//			BufferedReader br2 = new BufferedReader(new InputStreamReader(is2));    
//			String line = null;
//			while((line = br2.readLine()) != null)
//			{
//				progressDataIn.strcmdInfo = line; 
//				progressDataIn.info = true;
//				System.out.println(line);
//				lsIn.add(line);
//			}
			
			BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(process.getInputStream()));
			String ls_1 = "";
			while ( (ls_1=bufferedReader.readLine()) != null)
			{
				logger.info(ls_1);
				System.out.println(ls_1);
				lsIn.add(ls_1);
			}
			process.waitFor();
			ArrayList<ArrayList<String>> lsResult = new ArrayList<ArrayList<String>>();
			lsResult.add(lsIn); lsResult.add(lsErr);
			return lsResult;
		}
		
		
		/**
		 * д���ı������õ���sh�ķ�������
		 * @param cmdFileName �ļ����������õģ�����Ҫ·��
		 */
		public void doInBackground(String cmdFileName) {
			String cmd1SH = NovelBioConst.PATH_POSITION_RELATE + cmdFileName+ DateTime.getDate() + ".sh";
			TxtReadandWrite txtCmd1 = new TxtReadandWrite(cmd1SH, true);
			txtCmd1.writefile(cmd);
			txtCmd1.close();
			cmd = "sh "+cmd1SH;
			try {
				logger.info(cmd);
				doInBackgroundB();
			} catch (Exception e) {
				logger.error("cmd cannot executed correctly: "+cmd);
			}
		}
		
}




