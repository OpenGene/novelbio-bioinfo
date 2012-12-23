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
import javax.swing.WindowConstants;

import org.apache.log4j.Logger;

import com.novelbio.base.PathDetail;
import com.novelbio.base.dataOperate.DateTime;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.gui.GUIInfo;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.generalConf.NovelBioConst;
/**
 * ����cmd��ִ����Ϻ���Խ������������棬Ŀǰcmdֻ֧��Ӣ�ģ��������� ֻҪ�̳к���дprocess��������
 * ���ֻ��������ã���ô����doInBackground�����ͺ�
 * @author zong0jie
 */
public class CmdOperate extends RunProcess<String> {
	public static void main(String[] args) {
		try {
			test();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void test() throws InterruptedException {
		String cmd = "Rscript /media/winD/fedora/gitNovelbio/Novelbio-Bioinformatics-Analysis-Platform/target/rscript/tmp/TopGO_2012-09-09040524123.R";
		CmdOperate cmdOperate = new CmdOperate(cmd);
		Thread thread = new Thread(cmdOperate);
		thread.start();
		while (!cmdOperate.isFinished()) {
			Thread.sleep(100);
		}
		System.out.println("stop");
	}
	private static Logger logger = Logger.getLogger(CmdOperate.class);

	/** �Ƿ�pid��2�������д���ı�Ȼ��shִ�У�����Ҫ����2 */
	boolean shPID = false;
	
	/** ���� */
	Process process = null;
	/** �����е����� */
	String cmd = "";
	/** ��ʱ�ļ����ļ��� */
	String scriptFold = "";
	
	GUIInfo guIcmd;
	
	/**
	 * ֱ�����У���д���ı�
	 * @param cmd
	 */
	public CmdOperate(String cmd) {
		this.cmd = cmd;
		shPID = false;
	}
	/** 
	 * �Ƿ�չʾGUI��Ĭ�ϲ�չʾ
	 */
	public void setDisplayGUI(boolean displayGUI) {
		if (displayGUI) {
			guIcmd = new GUIInfo(this);
		}
		else {
			guIcmd = null;
		}
	}
	/**
	 * ��ʼ����ֱ�ӿ����̼߳���
	 * @param cmd ��������
	 * @param cmdWriteInFileName ������д����ı�
	 */
	public CmdOperate(String cmd, String cmdWriteInFileName) {
		this.cmd = cmd;
		setCmdFile(cmdWriteInFileName);
	}
	/**
	 * ���е�������
	 * @param lsCmd
	 */
	public CmdOperate(ArrayList<String> lsCmd) {
		for (String string : lsCmd) {
			cmd = cmd + string + "\n";
		}
		shPID = false;
	}

	/** �趨��Ҫ���е����� */
	public void setCmd(String cmd) {
		this.cmd = cmd;
		shPID = false;
	}
	/**
	 * ��cmdд���ĸ��ı���Ȼ��ִ�У������ʼ��������cmdWriteInFileName, �Ͳ���Ҫ�����
	 * @param cmd
	 */
	public void setCmdFile(String cmdWriteInFileName) {
		shPID = true;
		logger.info(cmd);
		String cmd1SH = PathDetail.getProjectConfPath() + cmdWriteInFileName.replace("\\", "/") + DateTime.getDateAndRandom() + ".sh";
		TxtReadandWrite txtCmd1 = new TxtReadandWrite(cmd1SH, true);
		txtCmd1.writefile(cmd);
		txtCmd1.close();
		cmd = "sh " + cmd1SH;
	}
	/**
	 * ֱ������cmd�����ܻ���� ��������arraylist-string ��һ����Info �ڶ�����error
	 * @param fileName
	 * @return
	 * @throws Exception
	 * @throws Exception
	 */
	private void doInBackgroundB() throws Exception {
		try {
			Thread thread = new Thread(guIcmd);
			thread.start();
		} catch (Exception e) {
			e.printStackTrace();
		}


		Runtime runtime = Runtime.getRuntime();
		process = runtime.exec(cmd);	
        // any error message?
        StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), "ERROR", guIcmd);            
        // any output?
        StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), "OUTPUT", guIcmd);
            
        // kick them off
        errorGobbler.start();
        outputGobbler.start();
        
		int info = process.waitFor();
		finishAndCloseCmd(info);
	}
	private void finishAndCloseCmd(int info) {
		if (guIcmd != null) {
			if (info == 0) {
				guIcmd.closeWindow();
			}
			else {
				guIcmd.appendTxtInfo("error");
			}
		}
	}

	@Override
	protected void running() {
		logger.info(cmd);
		try {
			doInBackgroundB();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("cmd cannot executed correctly: " + cmd);
		}
	}
	/** ����ʵ�� */
	@Deprecated
	public void threadSuspend() {
	}
	/** 
	 * ����ʵ�� 
	 * */
	@Deprecated
	public synchronized void threadResume() {
	}
	/** ��ֹ�̣߳���ѭ������� */
	public void threadStop() {
		int pid = -10;
		try {
			pid = getUnixPID(process);
			if (pid > 0) {
				if (shPID) {
					pid = pid + 2;
				}
				System.out.println(pid);
				Runtime.getRuntime().exec("kill -9 " + pid).waitFor();
				process.destroy();// �޷�ɱ���߳�
				process = null;
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
	/** ������ţ�һ�����ļ�·����Ҫ������� **/
	public static String addQuot(String pathName) {
		return "\"" + pathName + "\"";
	}
}

class StreamGobbler extends Thread {
	Logger logger = Logger.getLogger(StreamGobbler.class);
    InputStream is;
    String type;
    GUIInfo guiCmd;
    StreamGobbler(InputStream is, String type, GUIInfo guicmd) {
        this.is = is;
        this.type = type;
        this.guiCmd = guicmd;
    }
    
	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				logger.info(line);
				if (guiCmd != null) {
					guiCmd.appendTxtInfo(line);
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}


class ProgressData {
	public String strcmdInfo;
	/**
	 * true : info
	 * false : error
	 */
	public boolean info;
}
