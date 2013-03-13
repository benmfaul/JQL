package org.faul.jql.test;
import java.util.ArrayList;
import java.util.List;


import org.faul.jql.core.Jql;
import org.faul.jql.utils.Timer;
import org.gibello.zql.ZStatement;


public class Main { 
	Jql  jql = new Jql();
	Timer t;
	double time = 0;
	
	public Main() throws Exception {
		ArrayList list = new ArrayList();
		System.out.println(list);
		create(100000);
		//modify();
		
		load();
		delete(400);
	}
	
	void create(double count) throws Exception {
		jql.addDatabase("db");
		jql.execute("use db;");
		jql.addTable("test");

		t = new Timer();
		StringBuilder sb = new StringBuilder();
		String leader = "insert into test (b,c,d,e,f,g,a) values(";
		String tail = ",'b','c','d','e','f','g');";
		List<List<ZStatement>> l = new ArrayList();
		for (int i=0;i<count;i++) {
			//sb.setLength(0);
			sb.append(leader);
			sb.append(Integer.toString(i));
			sb.append(tail);
			//jql.execute(sb.toString());
			jql.addRecord("test","a",i,"b","b","c","c","d","d","e","e","f","f","g","g");
		}
		//jql.execute(sb.toString());
	
		time = t.getElapsed();
		time = time/1000;
		System.out.println("Insert Elapsed: " + time + ", " + (count/time));
		
		t = new Timer();
		jql.execute("update test set g = 'xxx';");
		time = time/1000;
		System.out.println("Update Elapsed: " +  time + ", " + (count/time));
		
		
		jql.execute("save db 'c:/tapin/junk.json';");
		
	}
	
	void load() throws Exception  {
		t = new Timer();
		jql.execute("load db 'c:/tapin/junk.json';");
		time = t.getElapsed();
		time = time/1000;
		List list = jql.getTable("test");
		double count = list.size();
		System.out.println("Load Elapsed time = " + time + ", " + (count/time));
	}
	
	void delete(double count) throws Exception  {
		t = new Timer();
		StringBuilder sb = new StringBuilder();
		String leader = "delete from test where a=";
		for (int i=0;i<count;i++) {
			sb.setLength(0);
			sb.append(leader);
			sb.append(Integer.toString(i));
			jql.execute(sb.toString(),null);
			//System.out.println(sb.toString());
			
			//jql.statement("delete from test where a="+i+";",null);
		}
		time = t.getElapsed();
		time = time/1000;
		System.out.println("Delete Elapsed: " +  time + ", " + (count/time));
	}
	
	public void printStats() {
		
	}
	
	public static void main(String [] args) throws Exception {
		new Main();
	}
}