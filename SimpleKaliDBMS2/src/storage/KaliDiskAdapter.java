package storage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import config.Config;

abstract class KaliDiskBlock extends KaliDbItem {
	
}

// this is not the right and best implementation, of course!
// there are problems with java class keeping some space for parent pointers and alignment information (for RAM word size)
// due to this, Kalidas is not able to fix the block size to exactly 1024 bytes!

// To teach the concept, roughly and approximately to convey the idea, the following header and data block classes are created

class KaliHeaderBlock extends KaliDiskBlock {
	long first_block_address; //to read from beginning
	long last_block_address; //to append new blocks
	
	@Override
	public String toString() {
		return first_block_address + " " + last_block_address;
	}
}

//this may not be the best data structure, but for simplicity I am using this
class KaliDataBlock extends KaliDiskBlock {
	long next_block_addr;
	ArrayList<KaliDbItem> db_item_list = new ArrayList<>(); 
	
	@Override
	public String toString() {
		return next_block_addr + " " + db_item_list.size();
	}
	
	//returns 0 if success and -1 if failure
	public int addDbItem(KaliDbItem dbitem) {
		
		//if there is an overflow, then return -1
		if(this.getSize() + dbitem.getSize() > Config.block_size) {
			return -1;
		}
		
		db_item_list.add(dbitem);
		
//		System.out.println("list size "+db_item_list.size()+" byte size "+this.getSize());
		
		return 0;
		
	}//addDbItem
	
}//KaliDiskBlock

public class KaliDiskAdapter {
	
	String dbfile;
	RandomAccessFile raf;
	
	int block_size = Config.block_size;
	
	long read_pointer; //to point to the last item to read
	KaliDataBlock read_data_block; //initially it will be null
	int read_db_item_count; //initially it is 0
	
//	byte [] buffer = new byte[block_size]; //this is no longer working. Kalidas found that, sticking to exactly 1024 bytes is a big head-ache in Java! Unlike C!!
	//due to issue with RAM word sizes, compiler to align data, keep pointers to parent classes, it is not easy to mimic C like
	// structure, get the exact bytes and type cast -- not easy in Java
	// I cannot just read 1024 bytes and type cast to a structure whose sum of all fields is exactly 1024 like in C
	// In Java, if I read 1024 bytes and try to type cast to an object, I am getting errors with EOFException in ObjectInputStream()
	// This is because, the actual object takes a bit more space. For instance, in my experiments, it took 1066 bytes, i.e. 42 bytes more!
	// So, after reading 1024 and trying to type cast to an object whose fields sum up to exactly 1024, the java runtime gives errors
	// Basically, A 'Java Class' is not identical to a 'Structure in C'.. Java class has a lot more implicit fields
	
	KaliHeaderBlock header_block;
	KaliDataBlock last_block;
	
	public KaliDiskAdapter(String dbfile, boolean forceInitialize) throws IOException {
		
		File db_file = new File(dbfile);
		
		if(forceInitialize) {
			
			//open file in write mode - to empty its contents
			FileWriter fw = new FileWriter(db_file);
			fw.close();
			
			raf = new RandomAccessFile(db_file, "rw");
			
			//create a new header
			header_block = new KaliHeaderBlock();
			
//			System.out.println("header block: "+header_block);
			
			_writeDiskBlock(header_block, 0);
			
			//append the first data block
			_appendDataBlock();
			
		}else {
			raf = new RandomAccessFile(db_file, "rw");
			header_block = (KaliHeaderBlock)_loadDiskBlock(0);
		}
		
//		System.out.println("before header block: "+header_block);
		
		header_block = (KaliHeaderBlock)_loadDiskBlock(0);
		
//		System.out.println("after header block: "+header_block);
		
		last_block = (KaliDataBlock)_loadDiskBlock(header_block.last_block_address);
		
	}//constructor	
	
	public void addDataItem(KaliDbItem dbitem) throws IOException {
		
		int ret_code = last_block.addDbItem(dbitem);
		
		//check if overflow event has occurred
		if(ret_code==-1) {			
//			System.out.println("overflow occurred...");
//			System.out.println("[during overflow..] status of the last block "+last_block);
			
//			System.out.println("old header (before append) "+header_block);
			
			_appendDataBlock();
			
//			System.out.println("new header (after append) "+header_block);
			last_block.addDbItem(dbitem);
		}
		
		
//		System.out.println("last_block item count "+last_block.db_item_list.size());
		
//		System.out.println("status of the last block "+last_block);
		
	}//add data item
	
	public void saveData() throws IOException {
//		System.out.println("saving data...");
		
//		System.out.println("last block being saved.. "+last_block);
//		System.out.println("header while saving.." + header_block);
		
		KaliDataBlock prev_block = (KaliDataBlock)_loadDiskBlock(header_block.first_block_address);
		
//		System.out.println("prev block "+prev_block);
		
		_writeDiskBlock(last_block, header_block.last_block_address);
		
	}//save data
	
	public void reset_read_pointer() {
		read_pointer = 0;
		read_data_block = null;
		read_db_item_count = 0;
	}
	
	public KaliDbItem readNextDataItem() throws IOException {
		
		header_block = (KaliHeaderBlock)_loadDiskBlock(0);
		last_block = (KaliDataBlock)_loadDiskBlock(header_block.first_block_address);
		
//		System.out.println("sanity test last block"+last_block);
		
		KaliDbItem dbitem = null;
		
		if(read_pointer==0) {
			
			header_block = (KaliHeaderBlock)_loadDiskBlock(0);
			
//			System.out.println("read: header block "+header_block +" size "+header_block.getSize());
			
			read_pointer = header_block.first_block_address;
			
			read_data_block = (KaliDataBlock)_loadDiskBlock(read_pointer);
			
			read_db_item_count = 0;
			
//			System.out.println("read: pointing to first data block: "+read_pointer+"  read block "+read_data_block);
			
		}
		
		//when already some reads happened earlier
		if(read_data_block!=null) {
			
//			System.out.println("num items "+read_data_block.db_item_list.size());
			
			//if already all items are read in the present block,
			//then point to the next block
			if ( read_db_item_count >= read_data_block.db_item_list.size()) {
				
				if(read_data_block.next_block_addr!=0) {
					read_pointer = read_data_block.next_block_addr;
					read_data_block = (KaliDataBlock)_loadDiskBlock(read_pointer);
					read_db_item_count = 0;
					
					System.out.println("read: pointing to first data item of the next block");	
				}else {
					read_data_block = null;
				}
			}
			
			if(read_data_block!=null) {
				dbitem = read_data_block.db_item_list.get(read_db_item_count);
				read_db_item_count++;
			}//fi
		}
		
		return dbitem;
		
	}// function - read next data item
	
	public void close() throws IOException {
		raf.close();
	}
	
	//load disk block from specified address
	private KaliDiskBlock _loadDiskBlock(long diskaddress) throws IOException {
		
		//First read the size of the disk block... All I can say is, it is approx 1024. Read my comments in other places regarding this issue!
		raf.seek(diskaddress);
		
		int rough_block_size = raf.readInt();
		
//		System.out.println("reading: "+rough_block_size);
		
		byte [] b_arr = new byte[rough_block_size]; //exclude first few bytes
		
		raf.read(b_arr); //there will be one additional read, unfortunately :-(  ... cannot help it for now!
		
		
//		raf.seek(diskaddress);
//		raf.read(buffer);	
		
//		System.out.println("read buffer len "+buffer.length);
		
		KaliDiskBlock block = (KaliDiskBlock) _convertBytesToObject(b_arr);
		
//		System.out.println("converted bytes to disk block "+block);
		
		return block;
		
	}//_load disk block
	
	//write disk block to specified address
	private void _writeDiskBlock(KaliDiskBlock block, long diskaddress) throws IOException {
		
		raf.seek(diskaddress);
		
		byte [] b_arr = block.getBytes(); //This size will not be exactly 1024... see my comments elsewhere!
		
//		System.out.println("writing: "+b_arr.length);
		
		raf.writeInt(b_arr.length);
		
		raf.write(b_arr);
	}
	
	private void _appendDataBlock() throws IOException {
		
//		System.out.println("appending new block");
		
		//re-load the header
		header_block = (KaliHeaderBlock)_loadDiskBlock(0);
		
		//determine the address for the new block
//		long new_block_addr = header_block.last_block_address + Config.block_size;	
		
		//Adding the FIRST data block
		if(header_block.first_block_address==0) {
			
//			System.out.println("first address is zero");
			
			int header_size = header_block.getSize();
			
//			System.out.println("header size "+header_size);
			
			header_block.first_block_address = Integer.SIZE + header_size;
			header_block.last_block_address = Integer.SIZE + header_size;
			
			//write the updated header
			_writeDiskBlock(header_block,0);
			
//			System.out.println("updated header "+header_block+" size "+header_block.getSize());
			
			KaliDataBlock new_data_block = new KaliDataBlock();
			_writeDiskBlock(new_data_block, Integer.SIZE + header_size);
			
			last_block = new_data_block;
			
//			System.out.println("added the first data block "+last_block);
			
//			System.out.println("just after initialization "+last_block);
			
			return;
			
		}//fi
		
//		System.out.println("adding subsequent data block");
		
		long new_address = header_block.last_block_address + Integer.SIZE + last_block.getSize();
		
		//write updated last_block
		last_block.next_block_addr = new_address;
		_writeDiskBlock(last_block, header_block.last_block_address);
		
//		System.out.println("updated last block "+last_block);
		
		//write updated header
		header_block.last_block_address = new_address;
		_writeDiskBlock(header_block, 0);
		
//		System.out.println("updated header "+header_block);
		
		//create a new block
		KaliDataBlock new_block = new KaliDataBlock();
		_writeDiskBlock(new_block, new_address);
		
		last_block = new_block;
		
//		System.out.println("new last block "+last_block);
		
	}//append data block	
	
	public static Object _convertBytesToObject(byte [] byte_arr) {
		Object ret_obj = null;
		
		try {
			
			ByteArrayInputStream bais = new ByteArrayInputStream(byte_arr);
			
			ObjectInputStream ois = new ObjectInputStream(bais);
			
			ret_obj = ois.readObject();
			
			ois.close();
			bais.close();
			
		}catch(IOException e) {
			e.printStackTrace();
		}catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return ret_obj;
	}
	
}
