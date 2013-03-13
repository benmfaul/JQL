package test;

import java.util.List;

import org.faul.jql.core.Jql;
import org.junit.Test;
import junit.framework.TestCase;

public class TestCSV extends TestCase {

	@Test public void test() throws Exception {
		Jql jql = new Jql();
		jql.execute("load db './jstests/sales.json';");
		jql.execute("use db;");

		List data = (List)jql.execute("select * from sales order by orderdate;");
		System.out.println(jql.toCSV(data));

		jql.addTable("csv");
		String sdata = "N::Date,JSON::Apples,N::Oranges,N::Tomatoes\n" +
			"0,10,20,30\n" +
			"1,100,200,300\n" +
			"2,1000,2000,3000\n" +
			"3,\"{\"red\":10, \"green\":20}\", 20, 30";
			
		jql.fromCSV("csv",sdata);
		List csv = jql.getTable("csv");

		System.out.println("From CSV:\n" + jql.format(csv));

		sdata = jql.toCSV(csv);
		System.out.println("\nTo CSV:" + sdata);
	}

}
