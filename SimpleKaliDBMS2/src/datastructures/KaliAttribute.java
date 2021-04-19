package datastructures;

import java.io.Serializable;

import storage.KaliDbItem;

public class KaliAttribute extends KaliDbItem {
	 public Class attrtype;
	 public String attrname;
	 
	 @Override
	 public String toString() {
		 return "(" + attrtype + " " + attrname +")";
	 }
}

