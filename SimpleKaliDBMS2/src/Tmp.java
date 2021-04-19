import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import config.Config;

public class Tmp {

	class Employee implements java.io.Serializable {
		   public String name;
		   public String address;
		   public int SSN; //public transient int SSN; if we do not want to store its value
		   public int number;
		   
		   public void mailCheck() {
		      System.out.println("Mailing a check to " + name + " " + address);
		   }
		   
		   @Override
		   public String toString() {
			   return name + " " + address + " " + SSN + " " + number;
		   }
		   
	}
		
		private void _serialize_test(String name) {
			Employee e = new Employee();
		      e.name = name;
		      e.address = "xx";
		      e.SSN = 11122333;
		      e.number = 101;
		      
		      try {	         
		    	  FileOutputStream fileOut =
		        		 new FileOutputStream(Config.configdir+"/employee.ser",true);	         
		         ObjectOutputStream out = new ObjectOutputStream(fileOut);	         
		         out.writeObject(e);	         
		         out.close();	         
		         fileOut.close();
		         
		         System.out.printf("Serialized data is saved in /tmp/employee.ser");
		         
		      } catch (IOException i) {
		         i.printStackTrace();
		      }
		}
		
		private void _deserialize_test() {
			Employee e = null;
		      try {
		         FileInputStream fileIn = new FileInputStream(Config.configdir+"/employee.ser");
		         ObjectInputStream in = new ObjectInputStream(fileIn);
		         
		         e = (Employee) in.readObject();
		         
		         while(e!=null) {
		        	 
					System.out.println("Deserialized Employee...");
					System.out.println("Name: " + e.name);
					System.out.println("Address: " + e.address);
					System.out.println("SSN: " + e.SSN);
					System.out.println("Number: " + e.number);
					
					e = (Employee) in.readObject();
		         }//while
		         
		         in.close();
		         
		         fileIn.close();
		         
		      } catch (IOException i) {
		         i.printStackTrace();
		         return;
		      } catch (ClassNotFoundException c) {
		         System.out.println("Employee class not found");
		         c.printStackTrace();
		         return;
		      }
		      
		}
		
		private  void main(String [] args) throws Exception {
			
			System.out.println("kali");
			
//			KaliDB kdb = new KaliDB();
			
//			kdb.run(1);
			
//			_serialize_test("kali");
//			_serialize_test("abcd");
			
//			_deserialize_test();
			
			
		}
	

}
