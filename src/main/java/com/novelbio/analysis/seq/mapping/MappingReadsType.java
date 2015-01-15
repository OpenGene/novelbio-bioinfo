package com.novelbio.analysis.seq.mapping;

import java.util.ArrayList;
import java.util.List;


public enum MappingReadsType {
		All, 
		Mapped, 
		UnMapped, 
		UniqueMapped, 
		MappedRate,
		UniqueMappedRate,
		/** 非unique Mapping */
		RepeatMapped,
		JunctionUniqueMapped, 
		JunctionAllMapped;
		
//		public static void main(String[] args) {
//			for (MappingReadsType type : MappingReadsType.values()) {
//				System.out.println(type);
//			}
//		}
		
		public static List<String> getLsReadsInfoType() {
			List<String> lsReadsInfoType = new ArrayList<String>();
			for (MappingReadsType type : MappingReadsType.values()) {
				lsReadsInfoType.add(type.toString());
			}
			return lsReadsInfoType;
		}
}
	