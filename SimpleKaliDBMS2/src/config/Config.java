package config;

public interface Config {
	
	String configdir = "C:\\Users\\Admin\\Documents\\Academics\\CS3206\\Lab\\Lab2-OwnDBMS";
	String dbdir = configdir + "/db";
	String catalog = configdir + "/catalog.db";
	
	int block_size = 1024;

}
