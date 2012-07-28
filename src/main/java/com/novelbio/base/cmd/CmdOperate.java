package com.novelbio.base.cmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

import com.novelbio.base.RunProcess;
import com.novelbio.base.dataOperate.DateTime;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.generalConf.NovelBioConst;
/**
 * 输入cmd，执行完毕后可以将结果输出到界面，目前cmd只支持英文，否则会出错 只要继承后重写process方法即可
 * 如果只是随便用用，那么调用doInBackground方法就好
 * @author zong0jie
 */
public class CmdOperate extends RunProcess<String>{
	private static Logger logger = Logger.getLogger(CmdOperate.class);
	
	public static void main(String[] args) throws InterruptedException {
		//执行
		String command=NovelBioConst.R_SCRIPT + NovelBioConst.R_WORKSPACE_TOPGO_RSCRIPT;
		CmdOperate cmdOperate = new CmdOperate(command, "Rtest");
//		CmdOperate cmdOperate = new CmdOperate("sort /media/winF/NBC/Project/Project_Invitrogen/sRNA/TG_miRNA.bed > /media/winF/NBC/Project/Project_Invitrogen/sRNA/TG_miRNA_sorted.bed", "test");
		Thread thread = new Thread(cmdOperate);
		thread.start();
		int count = 0;
		while (!cmdOperate.isFinished()) {
			Thread.sleep(100);
			System.out.println(count ++ );
			if (count == 20) {
				cmdOperate.threadStop();
			}
		}
		System.out.println(cmdOperate.isFinished());
	}
	/** 常规 */
	public static final int CMD_TYPE_NORMAL = 2;
	/** R语言 */
	public static final int CMD_TYPE_R = 4;
	/** 是否将pid加2，如果是写入文本然后sh执行，则需要加上2 */
	boolean shPID = false;

	/** 进程 */
	Process process = null;
	/** 待运行的命令 */
	String cmd = "";
	/**
	 * 直接运行，不写入文本
	 * @param cmd
	 */
	public CmdOperate(String cmd) {
		this.cmd = cmd;
		shPID = false;
	}
	/**
	 * 初始化后直接开新线程即可
	 * @param cmd 输入命令
	 * @param cmdWriteInFileName 将命令写入的文本
	 */
	public CmdOperate(String cmd, String cmdWriteInFileName) {
		this.cmd = cmd;
		setCmdFile(cmdWriteInFileName);
	}
	/**
	 * 多行的命令行
	 * @param lsCmd
	 */
	public CmdOperate(ArrayList<String> lsCmd) {
		for (String string : lsCmd) {
			cmd = cmd + string + "\n";
		}
		shPID = false;
	}
	/** 设定需要运行的命令 */
	public void setCmd(String cmd) {
		this.cmd = cmd;
		shPID = false;
	}

	/**
	 * 直接运行cmd，可能会出错 返回两个arraylist-string 第一个是Info 第二个是error
	 * 
	 * @param fileName
	 * @return
	 * @throws Exception
	 * @throws Exception
	 */
	private ArrayList<ArrayList<String>> doInBackgroundB() throws Exception {
//		ProgressData progressDataIn = new ProgressData();
		final ProgressData progressDataErr = new ProgressData();
		ArrayList<String> lsIn = new ArrayList<String>();
		final ArrayList<String> lsErr = new ArrayList<String>();
		
		Runtime runtime = Runtime.getRuntime();
	        
		process = runtime.exec(cmd);
		final InputStream is1 = process.getErrorStream();
		new Thread(new Runnable() {
			public void run() {
				BufferedReader br = new BufferedReader(new InputStreamReader(is1));
				String info = "";
				try {
					while ((info = br.readLine()) != null) {
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
		}).start(); // 启动单独的线程来清空process.getInputStream()的缓冲区
		// InputStream is2 = process.getInputStream();
		// BufferedReader br2 = new BufferedReader(new InputStreamReader(is2));
		// String line = null;
		// while((line = br2.readLine()) != null)
		// {
		// progressDataIn.strcmdInfo = line;
		// progressDataIn.info = true;
		// System.out.println(line);
		// lsIn.add(line);
		// }

		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(process.getInputStream()));
		String ls_1 = "";
		while ((ls_1 = bufferedReader.readLine()) != null) {
			logger.info(ls_1);
			System.out.println(ls_1);
			lsIn.add(ls_1);
		}		
		process.waitFor();
		
		ArrayList<ArrayList<String>> lsResult = new ArrayList<ArrayList<String>>();
		lsResult.add(lsIn);
		lsResult.add(lsErr);
		return lsResult;
	}
	/**
	 * 将cmd写入哪个文本，然后执行，如果初始化输入了cmdWriteInFileName, 就不需要这个了
	 * @param cmd
	 */
	public void setCmdFile(String cmdWriteInFileName) {
		shPID = true;
		logger.info(cmd);
		String cmd1SH = NovelBioConst.PATH_POSITION_RELATE + cmdWriteInFileName
				+ DateTime.getDate() + ".sh";
		TxtReadandWrite txtCmd1 = new TxtReadandWrite(cmd1SH, true);
		txtCmd1.writefile(cmd);
		txtCmd1.close();
		cmd = "sh " + cmd1SH;
	}
	@Override
	protected void running() {
		logger.info(cmd);
		try {
			doInBackgroundB();
		} catch (Exception e) {
			logger.error("cmd cannot executed correctly: " + cmd);
		}
	}
	/** 不能实现 */
	@Deprecated
	public void threadSuspend() {
	}
	/** 
	 * 不能实现 
	 * */
	@Deprecated
	public synchronized void threadResume() {
	}
	/** 终止线程，在循环中添加 */
	public void threadStop() {
//		process.destroy() 无法杀死线程
		int pid = -10;
		try {
			pid = getUnixPID(process);
			if (pid > 0) {
				if (shPID) {
					pid = pid + 2;
				}
				Runtime.getRuntime().exec("kill -9 " + pid).waitFor();
			}
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	private static int getUnixPID(Process process) throws Exception {
//	    System.out.println(process.getClass().getName());
	    if (process.getClass().getName().equals("java.lang.UNIXProcess")) {
	        Class cl = process.getClass();
	        Field field = cl.getDeclaredField("pid");
	        field.setAccessible(true);
	        Object pidObject = field.get(process);
	        return (Integer) pidObject;
	    } else {
	        throw new IllegalArgumentException("Needs to be a UNIXProcess");
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
