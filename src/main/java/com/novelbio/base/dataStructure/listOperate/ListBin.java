package com.novelbio.base.dataStructure.listOperate;
/**
 * 一般的序列查找就用这个就行
 * @author zong0jie
 *
 */
public class ListBin<T extends ListDetailAbs> extends ListAbs<T, ListCodAbs<T>, ListCodAbsDu<T,ListCodAbs<T>>>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8632727637919902406L;

	@Override
	protected ListCodAbs<T> creatGffCod(String listName, int Coordinate) {
		ListCodAbs<T> result = new ListCodAbs<T>(listName, Coordinate);
		return result;
	}

	@Override
	protected ListCodAbsDu<T, ListCodAbs<T>> creatGffCodDu(
			ListCodAbs<T> gffCod1, ListCodAbs<T> gffCod2) {
		ListCodAbsDu<T, ListCodAbs<T>> result = new ListCodAbsDu<T, ListCodAbs<T>>(gffCod1, gffCod2);
		return null;
	}

}
