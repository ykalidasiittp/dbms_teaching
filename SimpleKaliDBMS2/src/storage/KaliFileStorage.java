package storage;

import java.io.IOException;

import config.Config;
import datastructures.KaliTable;

public class KaliFileStorage {
	
	KaliDiskAdapter kda_cat;
	
	public KaliFileStorage() {
		
		try {
			kda_cat = new KaliDiskAdapter(Config.catalog, false);	
		}catch(IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void storeTable(KaliTable mytab) throws IOException {
		kda_cat.addDataItem(mytab);
		kda_cat.saveData();
	}
	
	public KaliTable retrieveTable(String tabname) throws IOException {
		kda_cat.reset_read_pointer();
		KaliTable mytab;
		
		while( (mytab = (KaliTable)kda_cat.readNextDataItem()) != null) {
			if(mytab.tabname.equals(tabname)) {
				return mytab;
			}
		}//while
		
		return null;
	}

}
