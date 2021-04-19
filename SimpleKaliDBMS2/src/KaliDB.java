import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import config.Config;

public class KaliDB {
	
	String configdir = Config.configdir;
	
	private String _getQuery(String progfile) throws IOException {
		
		System.out.println("Reading file: "+progfile);
		
		BufferedReader br = new BufferedReader(new FileReader(new File(progfile)));
		
		String query="";
		String line;
		
		while((line=br.readLine())!=null) {
			query = query + " " + line;
		}
		
		br.close();
		
		System.out.println("Returning query: "+query);
		
		return query;
		
	}
	

	
	public void run(int prognum) throws IOException {
		
		String progfile = configdir + "/prog_"+prognum+".txt";
		
		String query = _getQuery(progfile);
		
		KaliParser myparser = new KaliParser();
		KaliDBRuntimeEngine myruntime = new KaliDBRuntimeEngine();
		
		String code = myparser.parseQuery(query);
		
		myruntime.executeCode(code);
		
	}

}
