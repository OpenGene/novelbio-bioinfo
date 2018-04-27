package com.novelbio.database.model.omim;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import bsh.This;

import com.novelbio.database.dao.omim.RepoMIMInfo;
import com.novelbio.database.service.SpringFactoryBioinfo;
import com.novelbio.test.mytest;

@Document(collection = "omimInfo")
public class MIMInfo implements Serializable {

	/** MIM ID */
	@Id
	private int mimId;
	/** MIM Title 号 */
	private String mimTitle;
	/** MIM Txt 信息*/
	private String mimTxt;
	/** description信息*/
	private String desc;
	/** 参考文献*/
	private List<String> listRef;
	/** 
	 * 用来记录omim.txt 表中的一条记录中的详细信息
	 * Key是description,clinical features，inheritance等，
	 * value是具体的详细描述信息
	 * 以上之外的其他信息*/
	private static HashMap<String, String> mapTitle2Info = new HashMap();
	
	/** type 使用不同符号表示不同的意义
	 * “*” 表示是一个基因；
	 * “#” 表示是一个描述的条目，通常是表型，且不表示是一个特定的位点。与表型相关的基因的相应描述，都在第一个条目中
	 *   "+" 表示这个条目包含了已知序列的基因以及表型的描述
	 *  “%” 表示该条目描述了已经确定的孟德尔表型或含有未知分子基础的表型位点
	 *    "^" 说明这个条目已经不存在了，它已经从数据库中移除，或者被移至其他条目中了。
	 *    “”若无符号，则说明能德尔遗传情况还未明确，或者它的性状分离的情况还不明确。
	 * */
	private char type;
	
	public void setMimId(int mimId) {
		this.mimId = mimId;
	}
	public int getMimId() {
		return mimId;
	}
	public void setMimTitle(String mimTitle) {
		this.mimTitle = mimTitle;
	}
	public String getMimTitle() {
		return mimTitle;
	}
	public void setType(char type) {
		this.type = type;
	}
	public char getType() {
		return type;
	}
	public void setMimTxt(String mimTxt) {
		this.mimTxt = mimTxt;
	}
	public String getMimTxt() {
		return mimTxt;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getDesc() {
		return desc;
	}
	public List<String> getListRef() {
		return listRef;
	}
	
	public void addRef(String ref) {
		if (listRef == null) {
			listRef = new ArrayList<String>();
		}
		this.listRef.add(ref);
	}
	
	public void setMapTitle2Info(HashMap<String, String> mapTitle2Info) {
		this.mapTitle2Info = mapTitle2Info;
	}
	public Map<String, String> getMapTitle2Info() {
		return mapTitle2Info;
	}
	
	
	/**
	 * 输入一个list，包含了Omim的一个record，用该record来填充本类
	 * 
	 * *RECORD*
*FIELD* NO
100050
*FIELD* TI
100050 AARSKOG SYNDROME, AUTOSOMAL DOMINANT
*FIELD* TX

DESCRIPTION

Aarskog syndrome is characterized by short stature and facial, limb, and
genital anomalies. One form of the disorder is X-linked (see 305400),
but there is also evidence for autosomal dominant and autosomal
recessive (227330) inheritance (summary by Grier et al., 1983).

CLINICAL FEATURES

Grier et al. (1983) reported father and 2 sons with typical Aarskog
syndrome, including short stature, hypertelorism, and shawl scrotum.
Stretchable skin was present in these patients.

INHERITANCE

Grier et al. (1983) tabulated the findings in 82 previously reported
cases of Aarskog syndrome and noted that X-linked recessive inheritance
was repeatedly suggested. However, their family had father-to-son
transmission, and a family reported by Welch (1974) had affected males
in 3 consecutive generations. Grier et al. (1983) suggested autosomal
dominant inheritance with strong sex-influence and possibly
ascertainment bias resulting from use of the shawl scrotum as a main
criterion.

Van de Vooren et al. (1983) studied a large family in which Aarskog
syndrome was segregating with variable expression in 3 generations and
with male-to-male transmission. Because 3 daughters of affected males
had no features of Aarskog syndrome and 2 sons of an affected male had
several features of the syndrome, van de Vooren et al. (1983) suggested
sex-influenced autosomal dominant inheritance.

*FIELD* RF
1. Grier, R. E.; Farrington, F. H.; Kendig, R.; Mamunes, P.: Autosomal
dominant inheritance of the Aarskog syndrome. Am. J. Med. Genet. 15:
39-46, 1983.

2. van de Vooren, M. J.; Niermeijer, M. F.; Hoogeboom, A. J. M.:
The Aarskog syndrome in a large family, suggestive for autosomal dominant
inheritance. Clin. Genet. 24: 439-445, 1983.

3. Welch, J. P.: Elucidation of a 'new' pleiotropic connective tissue
disorder. Birth Defects Orig. Art. Ser. X(10): 138-146, 1974.

*FIELD* CS

Growth:
   Mild to moderate short stature

Head:
   Normocephaly

Hair:
   Widow's peak

Facies:
   Maxillary hypoplasia;
   Broad nasal bridge;
   Anteverted nostrils;
   Long philtrum;
   Broad upper lip;
   Curved linear dimple below the lower lip

Eyes:
   Hypertelorism;
   Ptosis;
   Down-slanted palpebral fissures;
   Ophthalmoplegia;
   Strabismus;
   Hyperopic astigmatism;
   Large cornea

Ears:
   Floppy ears;
   Lop-ears

Mouth:
   Cleft lip/palate

GU:
   Shawl scrotum;
   Saddle-bag scrotum;
   Cryptorchidism

Limbs:
   Brachydactyly;
   Digital contractures;
   Clinodactyly;
   Mild syndactyly;
   Transverse palmar crease;
   Lymphedema of the feet

Joints:
   Ligamentous laxity;
   Osteochondritis dissecans;
   Proximal finger joint hyperextensibility;
   Flexed distal finger joints;
   Genu recurvatum;
   Flat feet

Skin:
   Stretchable skin

Spine:
   Cervical spine hypermobility;
   Odontoid anomaly

Heme:
   Macrocytic anemia;
   Hemochromatosis

GI:
   Hepatomegaly;
   Portal cirrhosis;
   Imperforate anus;
   Rectoperineal fistula

Pulmonary:
   Interstitial pulmonary disease

Thorax:
   Sternal deformity

Inheritance:
   Sex-influenced autosomal dominant form;
   also X-linked form

*FIELD* CN
Nara Sobreira - updated: 4/22/2013

*FIELD* CD
Victor A. McKusick: 6/4/1986

*FIELD* ED
carol: 04/24/2013
carol: 4/22/2013
carol: 2/16/2011
alopez: 6/3/1997
mimadm: 3/11/1994
carol: 7/7/1993
supermim: 3/16/1992
supermim: 3/20/1990
ddp: 10/26/1989
marie: 3/25/1988

	 * @param lsOmimunit
	 * @return
	 */
	public static MIMInfo getInstanceFromOmimUnit(List<String> lsOmimunit) {
		if (lsOmimunit.isEmpty()) {
			return null;
		}
		MIMInfo mimInfo = new MIMInfo();
		HashMap<String, String> mapMimUni = MIMInfo.getMimUni(lsOmimunit);
		for (String key : mapMimUni.keySet()) {
			if (key.equals("NO")) {
				String mimID = mapMimUni.get(key).trim();
				mimInfo.setMimId(Integer.parseInt(mimID));
			} else if (key.equals("TI")) {
				String[] typeAndTitle = MIMInfo.getTypeAndTitle(mapMimUni.get(key).toString());
				char[] type= typeAndTitle[0].toCharArray();
				mimInfo.setType(type[0]);
				mimInfo.setMimTitle(typeAndTitle[1].trim());
			} else if (key.equals("TX")) {
				mimInfo.createTitle2Info(mapMimUni.get(key));
				mimInfo.setMimTxt(mapTitle2Info.get(key).trim());
				if (mapTitle2Info.containsKey("DESCRIPTION")) {
					mimInfo.setDesc(mapTitle2Info.get("DESCRIPTION").trim());
				} else {
					mimInfo.setDesc("");
				}
				
			} else if (key.equals("RF")) {
				String reftestString = mapMimUni.get(key);
				String[] arrRef = mapMimUni.get(key).split("\n\n");
				for (String ref : arrRef) {
					if ((!ref.trim().equals(""))) {
						mimInfo.addRef(ref.replace("\n", " ").trim());
					}
				}
			} 
		}
		mapMimUni.clear();
		return mimInfo;
	}
	
	/**
	 * omim.txt中的每一个RECORD是lsOmimunit中的一条记录
	 * maMimUni 中记录该记录中每一项的详细信息
	 * 
	 * @param lsOmimunit
	 * @return
	 */
	private static HashMap<String, String> getMimUni(List<String> lsOmimunit) {
		String fieldTitle = "";
		String fieldTxt = "";
		HashMap<String, String> maMimUni = new HashMap<String, String>();
		for (String content : lsOmimunit) {
			if (content.startsWith("*RECORD*")) {
				continue;
			}
			if (content.startsWith("*FIELD*")) {	
				if (!(fieldTitle.equals(""))) {
					maMimUni.put(fieldTitle, fieldTxt);
					fieldTxt = "";
				}
				fieldTitle = content.split("\\s")[1];
			} else if (content.matches("\\d+\\.+\\s.*?$")) {
				fieldTxt = fieldTxt.concat("\n" + content + " ");
			} else {
				fieldTxt = fieldTxt.concat(content + "\n");
			} 
		}
		if (!fieldTitle.equals("")) {
			maMimUni.put(fieldTitle, fieldTxt);
		}
		return maMimUni;
	}
	/**
	 * 	提取 *FIELD* TI  中MIM ID前面不同符号代表的不同类型信息
	 * 如#100300 ADAMS-OLIVER SYNDROME 1; AOS1    提取该行中最前面的#，
	 * “#” 表示是一个描述的条目，通常是表型，且不表示是一个特定的位点。与表型相关的基因的相应描述，都在第一个条目中
	 * @param txt
	 * @return
	 */
	private static String[] getTypeAndTitle(String title) {
		title = title.replaceAll("\n", " ");
		String[] typeAndTitle = new String[2];
		if (title.trim().matches("[#+^*%]\\d{6}.*?$")) {
			typeAndTitle[0] = title.substring(0,1);
			typeAndTitle[1] = title.substring(8);
		} else if (title.trim().matches("\\d{6}.*?$")) {
			typeAndTitle[0] = " ";
			typeAndTitle[1] = title.substring(6);
		}	
		return typeAndTitle;
	}
	
	/**
	 * 将RECODE中 TX中的信息，存在Map中
	 * Key是description,clinical features，inheritance等，
	 * value是每一项的具体的详细描述信息
	 * @param txt
	 */
	private void createTitle2Info(String txt) {
		mapTitle2Info.clear();
		String[] arrDescInfo = txt.split("\n");
		String key = "TX";
		String value = arrDescInfo[0];
		for (int i = 1; i < arrDescInfo.length - 1; i++) {
			if ((arrDescInfo[i - 1].length()==0) && ((arrDescInfo[i].matches("[A-Z]+\\s*[A-Z]*")) || (arrDescInfo[i].matches("[A-Z]+\\s*[A-Z]*\\/*[A-Z]+\\s*[A-Z]*"))) && (arrDescInfo[i + 1].length()==0)) {
				mapTitle2Info.put(key,value.trim());
				key = arrDescInfo[i];
				value = "";
			} else {
				value = value.concat(arrDescInfo[i] + " ");
				continue;
			}			
		}
		if (arrDescInfo.length > 1) {
			value = value.concat(arrDescInfo[arrDescInfo.length - 1]);
		}
		mapTitle2Info.put(key,value.trim());
		value = "";
	}
	
	 private static RepoMIMInfo repo() {
		 return SpringFactoryBioinfo.getBean(RepoMIMInfo.class);
		 
	 }
	 
	 public boolean remove() {
		 try {
			 repo().delete(mimId+"");
		 } catch (Exception e) {
			 return false;
		 }
		 return true;
	 }	  
}