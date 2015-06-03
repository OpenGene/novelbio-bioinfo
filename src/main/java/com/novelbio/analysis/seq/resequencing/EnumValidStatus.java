package com.novelbio.analysis.seq.resequencing;

import java.util.LinkedHashMap;
import java.util.Map;

/**用于MAF文件中的表示Mutation验证情况
 * 
 */
public enum EnumValidStatus {

	/**没有测试*/
	Untested,
	/**不确定的*/
	Inconclusive,
	/**已验证*/
	Valid,
	/**未验证*/
	Invalid
	
}
