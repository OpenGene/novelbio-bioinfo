package com.novelbio.analysis.tools.snvintersection;

import org.apache.commons.lang.StringUtils;

public interface EqualStrategy {
	public boolean equals(String o1, String o2);

	public static class DefaultEqualStrategy implements EqualStrategy {
		@Override
		public boolean equals(String o1, String o2) {
			return StringUtils.equals(o1, o2);
		}
	}
}
