import java.util.StringTokenizer;

public class KaliParser {
	
	public String parseQuery(String query) {
		
		System.out.println("Parsing query: "+query);
		
		String code = "";
		
		StringTokenizer st = new StringTokenizer(query," ");		
		
		while(st.hasMoreTokens()) {
			
			String token = st.nextToken();
			
			System.out.println("token: "+token);
			
			if(token.equals("create")) {
				
				String tabname = st.nextToken();
				
				code = code +"\n"+ "create_table "+tabname;
				
				int num_elements = Integer.parseInt(st.nextToken());
				
				//read each element
				while(num_elements>0) {
					
					String type = st.nextToken();					
					String attrname = st.nextToken();
					
					code = code + "\n" + "add_attr " + type + " " + attrname;
					
					num_elements--;
					
				}//while
				
				code = code + "\n" + "save_tab "+tabname;
				
				code = code + "\n" + "exists_tab "+tabname;
				
			}//if-create
			
			//the parsing can be a much better way, this is just to demonstrate the concept in dbms
			if(token.equals("insert")) {
				token = st.nextToken();
				if(token.equals("into")) {
					token = st.nextToken();
					String mytab = token;
					token = st.nextToken();
					if(token.equals("values")) {
						token = st.nextToken();
						code = code + "\n" + "insert_into "+mytab+" "+token;
					}
				}
			}
			
		}//while
		
		
		System.out.println("Returning code: "+code);
		
		return code;
	}

}
