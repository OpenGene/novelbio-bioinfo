import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

public class NumberOfSdAndNd {
	ArrayList<ArrayList<String>> vectorAll;

	public NumberOfSdAndNd() {
	}

	public Vector getCodonAndAA(File filename) {
		vectorAll = new ArrayList<ArrayList<String>>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			ArrayList<String> lsInfo = new ArrayList<String>();
			Vector codon = new Vector(1);
			String str;

			while ((str = in.readLine()) != null) {
				String[] temp = str.split("\\t");
				codon.add(temp[0]);
				aa.add(temp[1]);
			}
			all.add(aa);
			all.add(codon);
			in.close();
		} catch (IOException e) {
		}
		return all;
	}

	public static boolean isSynonymous(String codon1, String codon2,
			Vector aacodon) {
		boolean b = false;
		String aa1 = (String) ((Vector) (aacodon.get(0)))
				.get(((Vector) (aacodon.get(1))).indexOf(codon1));
		String aa2 = (String) ((Vector) (aacodon.get(0)))
				.get(((Vector) (aacodon.get(1))).indexOf(codon2));
		if (aa1.equals(aa2)) {
			b = true;
		} else {
			b = false;
		}
		return b;
	}

	public static boolean isTerminal(StringBuffer codon, Vector aacodon) {
		boolean b = false;
		if (((String) ((Vector) (aacodon.get(0)))
				.get(((Vector) (aacodon.get(1))).indexOf(codon.toString())))
				.equals("St")) {
			b = true;
		}
		return b;
	}
	/**
	 * 用atgc的任意一个去替换输入的codon，并计数为count。
	 * 如果发生了氨基酸变化，则s_temp+1
	 * 最后sn为 s_temp / count和 3-s_temp / count
	 * @param codon
	 * @param aacodon
	 * @return
	 */
	public static double[] getNumberOfSandNperCodon(String codon, Vector aacodon) {
		double[] sn = new double[2];// sn[0]0:S sn[1]:N
		char[] inputCodon = (codon.toUpperCase()).toCharArray();
		String aminoAcid = (String) ((Vector) aacodon.get(0)).get(((Vector) aacodon.get(1)).indexOf(codon.toUpperCase()));
		char[] atcg = { 'A', 'T', 'G', 'C' };
		for (int p = 0; p < inputCodon.length; p++) {
			double count = 0.0d;
			double s_temp = 0.0d;
			//用atgc的任意一个去替换输入的codon，并计数为count。
			//如果发生了氨基酸变化，则s_temp+1
			//最后sn为 s_temp / count和 3-s_temp / count
			for (char nr : atcg) {
				if (inputCodon[p] != nr) {
					StringBuffer sb = new StringBuffer();
					switch (p) {
					case 0:
						sb.append(nr);
						sb.append(inputCodon[1]);
						sb.append(inputCodon[2]);
						break;
					case 1:
						sb.append(inputCodon[0]);
						sb.append(nr);
						sb.append(inputCodon[2]);
						break;
					case 2:
						sb.append(inputCodon[0]);
						sb.append(inputCodon[1]);
						sb.append(nr);
						break;
					}
					if (((String) ((Vector) aacodon.get(0)).get(((Vector) aacodon.get(1)).indexOf(sb.toString()))).equals(aminoAcid)) {
						s_temp++;
						count++;
					} else {
						if (!((String) ((Vector) aacodon.get(0)).get(((Vector) aacodon.get(1)).indexOf(sb.toString()))).equals("St")) {
							count++;
						}
					}
				}
			
			}
			sn[0] = sn[0] + s_temp / count;
		}
		sn[1] = 3.0d - sn[0];
		System.out.println(codon + "	" + String.valueOf(sn[0]) + "	"
				+ String.valueOf(sn[1]));
		return sn;
	}

	public static void main(String[] args) {
		NumberOfSdAndNd nosan = new NumberOfSdAndNd();
		Vector aacodon = nosan.getCodonAndAA(new File(args[0]));
		double[] sdnd_all = new double[2];// sdnd[0]0:Sd sdnd[1]:Nd
		String refAAseq = args[1];
		String obvAAseq = args[2];
		double[] sAndN_plus = new double[2];
		double[] sAndN_minus = new double[2];
		//遍历每个三联密码子
		for (int i = 0; i < refAAseq.length(); i = i + 3) {
			double[] sdnd = new double[2];// sdnd[0]0:Sd sdnd[1]:Nd
			double[] sAndN_1 = NumberOfSdAndNd.getNumberOfSandNperCodon( refAAseq.substring(i, i + 3), aacodon);
			double[] sAndN_2 = NumberOfSdAndNd.getNumberOfSandNperCodon( obvAAseq.substring(i, i + 3), aacodon);
			sAndN_plus[0] = sAndN_plus[0] + sAndN_1[0];
			sAndN_plus[1] = sAndN_plus[1] + sAndN_1[1];
			sAndN_minus[0] = sAndN_minus[0] + sAndN_2[0];
			sAndN_minus[1] = sAndN_minus[1] + sAndN_2[1];
			if (!(refAAseq.substring(i, i + 3)).equals(obvAAseq.substring(i, i + 3))) {
				//如果密码子不同，则获得每个三联密码子
				char[] ref_codon_char = refAAseq.substring(i, i + 3).toCharArray();
				char[] obv_codon_char = obvAAseq.substring(i, i + 3).toCharArray();
				int dif_num = 0;
				
				//计算有几个nr是不同的
				for (int m = 0; m < ref_codon_char.length; m++) {
					if (ref_codon_char[m] != obv_codon_char[m]) {
						dif_num++;
					}
				}
				
				//差了一个碱基
				if (dif_num == 1) {
					if (NumberOfSdAndNd.isSynonymous(refAAseq.substring(i, i + 3), obvAAseq.substring(i, i + 3), aacodon)) {
						sdnd[0] = 1.0d;
						sdnd[1] = 0.0d;
					} else {
						sdnd[0] = 0.0d;
						sdnd[1] = 1.0d;
					}
				} 
				//差了两个碱基
				else if (dif_num == 2) {
					//前两个碱基不一样，那么将这两个碱基分别用ref和obv替换掉
					if (obv_codon_char[0] != ref_codon_char[0]
							&& obv_codon_char[1] != ref_codon_char[1]) {// 0,1
						StringBuffer transition_codon_1 = new StringBuffer();
						StringBuffer transition_codon_2 = new StringBuffer();
						transition_codon_1.append(ref_codon_char[0]);
						transition_codon_1.append(obv_codon_char[1]);
						transition_codon_1.append(obv_codon_char[2]);
						transition_codon_2.append(obv_codon_char[0]);
						transition_codon_2.append(ref_codon_char[1]);
						transition_codon_2.append(obv_codon_char[2]);

						boolean b1 = false;
						boolean b2 = false;
						double num_path = 0.0d;
						if (!NumberOfSdAndNd.isTerminal(transition_codon_1, aacodon)) {
							num_path++;
							b1 = true;
						}
						if (!NumberOfSdAndNd.isTerminal(transition_codon_2, aacodon)) {
							num_path++;
							b2 = true;
						}
						String refCod = refAAseq.substring(i, i + 3);
						String obvCod = obvAAseq.substring(i, i + 3);
						if (NumberOfSdAndNd.isSynonymous( refCod, transition_codon_1.toString(), aacodon) && b1) {
							sdnd[0]++;
						} else {
							sdnd[1]++;
						}
						if (NumberOfSdAndNd.isSynonymous( refCod, transition_codon_2.toString(), aacodon) && b2) {
							sdnd[0]++;
						} else {
							sdnd[1]++;
						}
						if (NumberOfSdAndNd.isSynonymous( obvCod, transition_codon_1.toString(), aacodon) && b1) {
							sdnd[0]++;
						} else {
							sdnd[1]++;
						}
						if (NumberOfSdAndNd.isSynonymous( obvCod, transition_codon_2.toString(), aacodon) && b2) {
							sdnd[0]++;
						} else {
							sdnd[1]++;
						}
						sdnd[0] = sdnd[0] / num_path;
						sdnd[1] = sdnd[1] / num_path;
					} else if (obv_codon_char[0] != ref_codon_char[0]
							&& obv_codon_char[2] != ref_codon_char[2]) {// 0,2
						StringBuffer transition_codon_1 = new StringBuffer();
						StringBuffer transition_codon_2 = new StringBuffer();
						transition_codon_1.append(ref_codon_char[0]);
						transition_codon_1.append(obv_codon_char[1]);
						transition_codon_1.append(obv_codon_char[2]);
						transition_codon_2.append(obv_codon_char[0]);
						transition_codon_2.append(obv_codon_char[1]);
						transition_codon_2.append(ref_codon_char[2]);

						boolean b1 = false;
						boolean b2 = false;
						double num_path = 0.0d;
						if (!NumberOfSdAndNd.isTerminal(transition_codon_1,
								aacodon)) {
							num_path++;
							b1 = true;
						}
						if (!NumberOfSdAndNd.isTerminal(transition_codon_2,
								aacodon)) {
							num_path++;
							b2 = true;
						}

						if (NumberOfSdAndNd.isSynonymous(
								refAAseq.substring(i, i + 3),
								transition_codon_1.toString(), aacodon) && b1) {
							sdnd[0]++;
						} else {
							sdnd[1]++;
						}
						if (NumberOfSdAndNd.isSynonymous(
								refAAseq.substring(i, i + 3),
								transition_codon_2.toString(), aacodon) && b2) {
							sdnd[0]++;
						} else {
							sdnd[1]++;
						}
						if (NumberOfSdAndNd.isSynonymous(
								obvAAseq.substring(i, i + 3),
								transition_codon_1.toString(), aacodon) && b1) {
							sdnd[0]++;
						} else {
							sdnd[1]++;
						}
						if (NumberOfSdAndNd.isSynonymous(
								obvAAseq.substring(i, i + 3),
								transition_codon_2.toString(), aacodon) && b2) {
							sdnd[0]++;
						} else {
							sdnd[1]++;
						}
						sdnd[0] = sdnd[0] / num_path;
						sdnd[1] = sdnd[1] / num_path;
					} else {// 1,2
						StringBuffer transition_codon_1 = new StringBuffer();
						StringBuffer transition_codon_2 = new StringBuffer();
						double num_path = 0.0d;
						transition_codon_1.append(obv_codon_char[0]);
						transition_codon_1.append(ref_codon_char[1]);
						transition_codon_1.append(obv_codon_char[2]);
						transition_codon_2.append(obv_codon_char[0]);
						transition_codon_2.append(obv_codon_char[1]);
						transition_codon_2.append(ref_codon_char[2]);
						boolean b1 = false;
						boolean b2 = false;
						if (!NumberOfSdAndNd.isTerminal(transition_codon_1,
								aacodon)) {
							num_path++;
							b1 = true;
						}
						if (!NumberOfSdAndNd.isTerminal(transition_codon_2,
								aacodon)) {
							num_path++;
							b2 = true;
						}
						if (NumberOfSdAndNd.isSynonymous(
								refAAseq.substring(i, i + 3),
								transition_codon_1.toString(), aacodon) && b1) {
							sdnd[0]++;
						} else {
							sdnd[1]++;
						}
						if (NumberOfSdAndNd.isSynonymous(
								refAAseq.substring(i, i + 3),
								transition_codon_2.toString(), aacodon) && b2) {
							sdnd[0]++;
						} else {
							sdnd[1]++;
						}
						if (NumberOfSdAndNd.isSynonymous(
								obvAAseq.substring(i, i + 3),
								transition_codon_1.toString(), aacodon) && b1) {
							sdnd[0]++;
						} else {
							sdnd[1]++;
						}
						if (NumberOfSdAndNd.isSynonymous(
								obvAAseq.substring(i, i + 3),
								transition_codon_2.toString(), aacodon) && b2) {
							sdnd[0]++;
						} else {
							sdnd[1]++;
						}
						sdnd[0] = sdnd[0] / num_path;
						sdnd[1] = sdnd[1] / num_path;
					}
				} else {
					StringBuffer transition_codon_1 = new StringBuffer();
					StringBuffer transition_codon_2 = new StringBuffer();
					StringBuffer transition_codon_3 = new StringBuffer();
					StringBuffer transition_codon_4 = new StringBuffer();
					StringBuffer transition_codon_5 = new StringBuffer();
					StringBuffer transition_codon_6 = new StringBuffer();
					StringBuffer transition_codon_7 = new StringBuffer();
					StringBuffer transition_codon_8 = new StringBuffer();
					StringBuffer transition_codon_9 = new StringBuffer();
					StringBuffer transition_codon_10 = new StringBuffer();
					StringBuffer transition_codon_11 = new StringBuffer();
					StringBuffer transition_codon_12 = new StringBuffer();
					double num_path = 0.0d;

					transition_codon_1.append(obv_codon_char[0]);
					transition_codon_1.append(ref_codon_char[1]);
					transition_codon_1.append(ref_codon_char[2]);
					transition_codon_2.append(obv_codon_char[0]);
					transition_codon_2.append(obv_codon_char[1]);
					transition_codon_2.append(ref_codon_char[2]);
					transition_codon_3.append(obv_codon_char[0]);
					transition_codon_3.append(ref_codon_char[1]);
					transition_codon_3.append(ref_codon_char[2]);
					transition_codon_4.append(obv_codon_char[0]);
					transition_codon_4.append(ref_codon_char[1]);
					transition_codon_4.append(obv_codon_char[2]);

					transition_codon_5.append(ref_codon_char[0]);
					transition_codon_5.append(obv_codon_char[1]);
					transition_codon_5.append(ref_codon_char[2]);
					transition_codon_6.append(obv_codon_char[0]);
					transition_codon_6.append(obv_codon_char[1]);
					transition_codon_6.append(ref_codon_char[2]);
					transition_codon_7.append(ref_codon_char[0]);
					transition_codon_7.append(obv_codon_char[1]);
					transition_codon_7.append(ref_codon_char[2]);
					transition_codon_8.append(ref_codon_char[0]);
					transition_codon_8.append(obv_codon_char[1]);
					transition_codon_8.append(obv_codon_char[2]);

					transition_codon_9.append(ref_codon_char[0]);
					transition_codon_9.append(ref_codon_char[1]);
					transition_codon_9.append(obv_codon_char[2]);
					transition_codon_10.append(obv_codon_char[0]);
					transition_codon_10.append(ref_codon_char[1]);
					transition_codon_10.append(obv_codon_char[2]);
					transition_codon_11.append(ref_codon_char[0]);
					transition_codon_11.append(ref_codon_char[1]);
					transition_codon_11.append(obv_codon_char[2]);
					transition_codon_12.append(ref_codon_char[0]);
					transition_codon_12.append(obv_codon_char[1]);
					transition_codon_12.append(obv_codon_char[2]);

					boolean b1 = false;
					boolean b2 = false;
					boolean b3 = false;
					boolean b4 = false;
					boolean b5 = false;
					boolean b6 = false;

					if (!NumberOfSdAndNd
							.isTerminal(transition_codon_1, aacodon)
							&& !NumberOfSdAndNd.isTerminal(transition_codon_2,
									aacodon)) {
						num_path++;
						b1 = true;
					}
					if (!NumberOfSdAndNd
							.isTerminal(transition_codon_3, aacodon)
							&& !NumberOfSdAndNd.isTerminal(transition_codon_4,
									aacodon)) {
						num_path++;
						b2 = true;
					}
					if (!NumberOfSdAndNd
							.isTerminal(transition_codon_5, aacodon)
							&& !NumberOfSdAndNd.isTerminal(transition_codon_6,
									aacodon)) {
						num_path++;
						b3 = true;
					}
					if (!NumberOfSdAndNd
							.isTerminal(transition_codon_7, aacodon)
							&& !NumberOfSdAndNd.isTerminal(transition_codon_8,
									aacodon)) {
						num_path++;
						b4 = true;
					}
					if (!NumberOfSdAndNd
							.isTerminal(transition_codon_9, aacodon)
							&& !NumberOfSdAndNd.isTerminal(transition_codon_10,
									aacodon)) {
						num_path++;
						b5 = true;
					}
					if (!NumberOfSdAndNd.isTerminal(transition_codon_11,
							aacodon)
							&& !NumberOfSdAndNd.isTerminal(transition_codon_12,
									aacodon)) {
						num_path++;
						b6 = true;
					}

					if (NumberOfSdAndNd.isSynonymous(
							refAAseq.substring(i, i + 3),
							transition_codon_1.toString(), aacodon)
							&& b1) {
						sdnd[0]++;
					} else {
						sdnd[1]++;
					}
					if (NumberOfSdAndNd.isSynonymous(
							transition_codon_1.toString(),
							transition_codon_2.toString(), aacodon)
							&& b1) {
						sdnd[0]++;
					} else {
						sdnd[1]++;
					}
					if (NumberOfSdAndNd.isSynonymous(
							transition_codon_2.toString(),
							obvAAseq.substring(i, i + 3), aacodon)
							&& b1) {
						sdnd[0]++;
					} else {
						sdnd[1]++;
					}

					if (NumberOfSdAndNd.isSynonymous(
							refAAseq.substring(i, i + 3),
							transition_codon_3.toString(), aacodon)
							&& b2) {
						sdnd[0]++;
					} else {
						sdnd[1]++;
					}
					if (NumberOfSdAndNd.isSynonymous(
							transition_codon_3.toString(),
							transition_codon_4.toString(), aacodon)
							&& b2) {
						sdnd[0]++;
					} else {
						sdnd[1]++;
					}
					if (NumberOfSdAndNd.isSynonymous(
							transition_codon_4.toString(),
							obvAAseq.substring(i, i + 3), aacodon)
							&& b2) {
						sdnd[0]++;
					} else {
						sdnd[1]++;
					}

					if (NumberOfSdAndNd.isSynonymous(
							refAAseq.substring(i, i + 3),
							transition_codon_5.toString(), aacodon)
							&& b3) {
						sdnd[0]++;
					} else {
						sdnd[1]++;
					}
					if (NumberOfSdAndNd.isSynonymous(
							transition_codon_5.toString(),
							transition_codon_6.toString(), aacodon)
							&& b3) {
						sdnd[0]++;
					} else {
						sdnd[1]++;
					}
					if (NumberOfSdAndNd.isSynonymous(
							transition_codon_6.toString(),
							obvAAseq.substring(i, i + 3), aacodon)
							&& b3) {
						sdnd[0]++;
					} else {
						sdnd[1]++;
					}

					if (NumberOfSdAndNd.isSynonymous(
							refAAseq.substring(i, i + 3),
							transition_codon_7.toString(), aacodon)
							&& b4) {
						sdnd[0]++;
					} else {
						sdnd[1]++;
					}
					if (NumberOfSdAndNd.isSynonymous(
							transition_codon_7.toString(),
							transition_codon_8.toString(), aacodon)
							&& b4) {
						sdnd[0]++;
					} else {
						sdnd[1]++;
					}
					if (NumberOfSdAndNd.isSynonymous(
							transition_codon_8.toString(),
							obvAAseq.substring(i, i + 3), aacodon)
							&& b4) {
						sdnd[0]++;
					} else {
						sdnd[1]++;
					}

					if (NumberOfSdAndNd.isSynonymous(
							refAAseq.substring(i, i + 3),
							transition_codon_9.toString(), aacodon)
							&& b5) {
						sdnd[0]++;
					} else {
						sdnd[1]++;
					}
					if (NumberOfSdAndNd.isSynonymous(
							transition_codon_9.toString(),
							transition_codon_10.toString(), aacodon)
							&& b5) {
						sdnd[0]++;
					} else {
						sdnd[1]++;
					}
					if (NumberOfSdAndNd.isSynonymous(
							transition_codon_10.toString(),
							obvAAseq.substring(i, i + 3), aacodon)
							&& b5) {
						sdnd[0]++;
					} else {
						sdnd[1]++;
					}

					if (NumberOfSdAndNd.isSynonymous(
							refAAseq.substring(i, i + 3),
							transition_codon_11.toString(), aacodon)
							&& b6) {
						sdnd[0]++;
					} else {
						sdnd[1]++;
					}
					if (NumberOfSdAndNd.isSynonymous(
							transition_codon_11.toString(),
							transition_codon_12.toString(), aacodon)
							&& b6) {
						sdnd[0]++;
					} else {
						sdnd[1]++;
					}
					if (NumberOfSdAndNd.isSynonymous(
							transition_codon_12.toString(),
							obvAAseq.substring(i, i + 3), aacodon)
							&& b6) {
						sdnd[0]++;
					} else {
						sdnd[1]++;
					}
					System.out.println(String.valueOf(sdnd[0]) + "	"
							+ String.valueOf(sdnd[1]));
					sdnd[0] = sdnd[0] / num_path;
					sdnd[1] = sdnd[1] / num_path;
				}
			} else {
				sdnd[0] = 0.0d;
				sdnd[1] = 0.0d;
			}
			sdnd_all[0] = sdnd_all[0] + sdnd[0];
			sdnd_all[1] = sdnd_all[1] + sdnd[1];
		}
		if (sAndN_plus[0] + sAndN_plus[1] != args[1].length()
				&& sAndN_minus[0] + sAndN_minus[1] != args[1].length()) {
			System.out.println("error");
		}
		double ps = 2.0d * sdnd_all[0] / (sAndN_plus[0] + sAndN_minus[0]);
		double pn = 2.0d * sdnd_all[1] / (sAndN_plus[1] + sAndN_minus[1]);
		double ks = (-3.0d / 4.0d) * Math.log(1.0d - 4.0d * ps / 3.0d);
		double ka = (-3.0d / 4.0d) * Math.log(1.0d - 4.0d * pn / 3.0d);
		System.out.println(String.valueOf(sAndN_plus[0]) + "	"
				+ String.valueOf(sAndN_plus[1]) + "	"
				+ String.valueOf(sAndN_minus[0]) + "	"
				+ String.valueOf(sAndN_minus[1]) + "	"
				+ String.valueOf(sdnd_all[0]) + "	"
				+ String.valueOf(sdnd_all[1]) + "	" + String.valueOf(ks) + "	"
				+ String.valueOf(ka) + "	" + String.valueOf(ka / ks));
	}
}
