import java.io.IOException;
import java.util.StringTokenizer;

import datastructures.KaliAttribute;
import datastructures.KaliTable;
import storage.KaliFileStorage;

public class KaliDBRuntimeEngine {
	
	KaliFileStorage kfs = new KaliFileStorage();
	
	private KaliTable _createTable(String token) throws IOException {
		String tabname = token.split(" ")[1];
		System.out.println("_createTable is invoked: "+tabname);
		
		KaliTable mytab = new KaliTable();
		mytab.tabname = tabname;
		
		System.out.println("table object: "+mytab);
		
		return mytab;
	}
	
	private void _saveTable(KaliTable mytab) throws IOException {
		System.out.println("saving table");
		kfs.storeTable(mytab);
	}
	
	private boolean _existsTable(String tabname) throws IOException {
		
		boolean is_exists = false;
		
		if(kfs.retrieveTable(tabname)!=null)
			is_exists = true;
		
		System.out.println("table "+tabname+" is_exists "+is_exists);
		
		return is_exists;
		
	}
	
	private KaliTable _loadTable(String tablename) throws IOException  {
		System.out.println("Load table: "+tablename);
		KaliTable mytab = kfs.retrieveTable(tablename);
		System.out.println("retrieved table "+mytab);
		return mytab;
	}
	
	private void _insertValues(KaliTable mytab, String valstr) {
		System.out.println("Inserting values: "+mytab+" "+valstr);
	}
	
	private void _addAttribute(KaliTable mytab, String token) {
		System.out.println("Adding attribute: "+mytab.tabname+" "+token);
		String [] _arr = token.split(" ");
		String type = _arr[1];
		String attrname = _arr[2];
		
		KaliAttribute myattr = new KaliAttribute();
		
		if(type.equals("int")) {
			myattr.attrtype = Integer.class;
		}
		
		if(type.equals("string")) {
			myattr.attrtype = String.class;
		}
		
		myattr.attrname = attrname;
		
		//add to the table object
		mytab.attr_list.add(myattr);
		
		System.out.println("attribute object: "+myattr);
	}
	
	public void executeCode(String code) throws IOException {
		
		System.out.println("Executing code: "+code);
		
		StringTokenizer st = new StringTokenizer(code,"\n");
		
		KaliTable mytab=null;
		
		while(st.hasMoreTokens()) {
			
			String token = st.nextToken();
			
			System.out.println("token: "+token);
			
			if(token.startsWith("create_table")) {				
				mytab = _createTable(token);
			}
			
			if(token.startsWith("add_attr")) {
				_addAttribute(mytab, token);
			}
			
			if(token.startsWith("save_tab")) {
				_saveTable(mytab);
			}
			
			if(token.startsWith("exists_tab")) {
				String [] _arr = token.split(" ");
				_existsTable(_arr[1]);
			}
			
			//This parsing code may look complex, but it only to illustrate
			//You should imagine that there is an improvised system used, but theoretically it is same 
			//(or as simple or as complex) as you see here!
			
			if(token.startsWith("insert_into")) {
				
				String [] _arr = token.split(" ");
				String tablename = _arr[1];
				String valstr = _arr[2];
				
				mytab = _loadTable(tablename);
				
				_insertValues(mytab,valstr);
			}
			
		}//while
		
	}

}
