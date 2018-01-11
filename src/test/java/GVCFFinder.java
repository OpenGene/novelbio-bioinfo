import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.novelbio.analysis.seq.sam.SamIndexRefsequence;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;


public class GVCFFinder {
	List<String> lsGvcfFiles = new ArrayList<>();
	String chrFile;
	public static void main(String[] args) {
		GVCFFinder gvcfFinder = new GVCFFinder();
		String parentPath = "/home/novelbio/NBCresource/zdb/new/";
		gvcfFinder.setChrFile(parentPath + "K326_genomic_modify.fna");
		gvcfFinder.addVcfFile(parentPath + "NT-01_gvcf.vcf.gz");
		gvcfFinder.addVcfFile(parentPath + "NT-02_gvcf.vcf.gz");
		gvcfFinder.addVcfFile(parentPath + "NT-03_gvcf.vcf.gz");
		gvcfFinder.addVcfFile(parentPath + "NT-04_gvcf.vcf.gz");
		gvcfFinder.addVcfFile(parentPath + "NT-05_gvcf.vcf.gz");
		gvcfFinder.addVcfFile(parentPath + "NT-06_gvcf.vcf.gz");
		gvcfFinder.addVcfFile(parentPath + "NT-07_gvcf.vcf.gz");
		gvcfFinder.addVcfFile(parentPath + "NT-09_gvcf.vcf.gz");
		gvcfFinder.addVcfFile(parentPath + "NT-10_gvcf.vcf.gz");
		gvcfFinder.addVcfFile(parentPath + "NT-11_gvcf.vcf.gz");
		gvcfFinder.addVcfFile(parentPath + "NT-13_gvcf.vcf.gz");
		gvcfFinder.addVcfFile(parentPath + "NT-14_gvcf.vcf.gz");
		gvcfFinder.addVcfFile(parentPath + "NT-15_gvcf.vcf.gz");
		gvcfFinder.addVcfFile(parentPath + "NT-16_gvcf.vcf.gz");
		gvcfFinder.addVcfFile(parentPath + "NT-17_gvcf.vcf.gz");
		gvcfFinder.addVcfFile(parentPath + "NT-18_gvcf.vcf.gz");
		gvcfFinder.addVcfFile(parentPath + "NT-19_gvcf.vcf.gz");
		gvcfFinder.addVcfFile(parentPath + "NT-22_gvcf.vcf.gz");
		gvcfFinder.keepLongChrId();
		
//		Set<String> setChrId = new HashSet<>();
//		TxtReadandWrite txtRead = new TxtReadandWrite(parentPath + "NT-03_modify_gvcf.vcf.gz");
//		for (String content : txtRead.readlines()) {
//			if (!content.startsWith("#")) {
//				break;
//			}
//			if (content.startsWith("##contig=")) {
//				String chrId = content.split("ID=")[1].split(",length=")[0];
//				setChrId.add(chrId);
//            }
//        }
//		txtRead.close();
//		GVCFFinder gvcfFinder = new GVCFFinder();
//		String chrFile = "GCA_000715075.1_Ntab-K326_genomic_modify.fna";
//		gvcfFinder.removeChrFileChrId(setChrId, parentPath + chrFile, parentPath + "K326_genomic_modify.fna");

    }
	
	public void addVcfFile(String vcfFile) {
		lsGvcfFiles.add(vcfFile);
	}
	public void setChrFile(String chrFile) {
	    this.chrFile = chrFile;
    }
	
	public void keepLongChrId() {
		Set<String> setChrIdNoCover = getChrIdLong(chrFile);
		keepLongChr(setChrIdNoCover);
	}
	
	
	public void keepLongChr(Set<String> setChrIdKeeped) {
		for (String vcf : lsGvcfFiles) {
			String vcfNew = FileOperate.changeFileSuffix(vcf, "_long", "vcf.gz", null);
			keepVcfChrId(setChrIdKeeped, vcf, vcfNew);
        }
		
		String chrFileNew = FileOperate.changeFileSuffix(chrFile, "_long", null);
		keepChrFileChrId(setChrIdKeeped, chrFile, chrFileNew);
	}
	
	private Set<String> getChrIdLong(String chrFile) {
		Set<String> setChrId = new HashSet<>();
		Map<String, Long> mapChrId2Len = SamIndexRefsequence.generateIndexAndGetMapChrId2Len(chrFile);
		for (String chrId : mapChrId2Len.keySet()) {
			if (mapChrId2Len.get(chrId) >= 20000) {
	            	setChrId.add(chrId);
            }
        }
		return setChrId;
	}
	
	private void keepVcfChrId(Set<String> setChrId, String vcfFile, String vcfFileNew) {
		TxtReadandWrite txtRead = new TxtReadandWrite(vcfFile);
		TxtReadandWrite txtWrite = new TxtReadandWrite(vcfFileNew, true);
		for (String content : txtRead.readlines()) {
			if (content.startsWith("#")) {
				if (content.startsWith("##contig=")) {
					String chrId = content.split("ID=")[1].split(",length=")[0];
					if (!setChrId.contains(chrId.toLowerCase())) {
						continue;
					}
                }
			} else {
				String chrId = content.split("\t")[0];
				if (!setChrId.contains(chrId.toLowerCase())) {
					continue;
                }
			}
			txtWrite.writefileln(content);
        }
		txtRead.close();
		txtWrite.close();
	}
	
	private void keepChrFileChrId(Set<String> setChrIdKeep, String chrFile, String chrFileNew) {
		TxtReadandWrite txtRead = new TxtReadandWrite(chrFile);
		TxtReadandWrite txtWrite = new TxtReadandWrite(chrFileNew, true);
		boolean isWrite = true;
		for (String content : txtRead.readlines()) {
			if (content.startsWith(">")) {
				String chrId = content.split(" ")[0].replaceFirst(">", "");
				if (setChrIdKeep.contains(chrId.toLowerCase())) {
					isWrite = true;
				} else {
					isWrite = false;
				}
            }
			if (isWrite) {
				txtWrite.writefileln(content);
            }
        }
		txtRead.close();
		txtWrite.close();
	}
	
	
	private Set<String> getNoCoverChr(String file) {
		Set<String> setChrId = new HashSet<>();
		Map<String, Long> mapChr2Len = SamIndexRefsequence.generateIndexAndGetMapChrId2Len(chrFile);
		
		VCFFileReader vcfFileReader = new VCFFileReader(FileOperate.getFile(file), false);
		for (VariantContext variantContext : vcfFileReader) {
			if (variantContext.getStart() == 1 && variantContext.getEnd() == mapChr2Len.get(variantContext.getContig().toLowerCase())) {
				setChrId.add(variantContext.getContig());
			}
        }
		vcfFileReader.close();
		return setChrId;
	}
}
