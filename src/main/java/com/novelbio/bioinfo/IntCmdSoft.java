package com.novelbio.bioinfo;

import java.util.List;

/** 调用cmd命令所要获得的程序 */
public interface IntCmdSoft {
	/** 获得本次调用Cmd命令实际执行的命令行<br>
	 * 实际可能会执行多行，所以返回list
	 * @return
	 */
	List<String> getCmdExeStr();
}
