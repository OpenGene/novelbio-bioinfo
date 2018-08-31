package com.novelbio.bioinfo.sam;

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
		JunctionAllMapped,
		
		AllBase,
		MappedBase,
		UnMappedBase,
		UniqueMappedBase,
		RepeatMappedBase;
		
		
		public static List<String> getLsReadsInfoType() {
			List<String> lsReadsInfoType = new ArrayList<String>();
			for (MappingReadsType type : MappingReadsType.values()) {
				lsReadsInfoType.add(type.toString());
			}
			return lsReadsInfoType;
		}
}
	