package storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import config.Config;

public class KaliDbItem implements Serializable {
	
	public int getSize() {
		return getBytes().length;
	}
	
	public byte [] getBytes() {
		
		Serializable kaliObj = this;
		
		byte [] out_byte_array = null;
		
		try {
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			
			ObjectOutputStream oos = new ObjectOutputStream(baos); //link it to baos
			
			oos.writeObject(kaliObj);
			
			oos.close();
			
			out_byte_array = baos.toByteArray();
			
		}catch(IOException e) {
			e.printStackTrace();
		}
		
		return out_byte_array;
		
	}//convert object to bytes

}
