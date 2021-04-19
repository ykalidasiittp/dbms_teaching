package datastructures;

import java.io.Serializable;
import java.util.ArrayList;

import storage.KaliDbItem;

public class KaliTable extends KaliDbItem {
	public String tabname;
	public ArrayList<KaliAttribute> attr_list = new ArrayList<>();
	
	@Override
	public String toString() {
		return tabname +" " + attr_list;
	}
}
