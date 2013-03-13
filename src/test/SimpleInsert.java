package test;

import java.util.List;

import org.faul.jql.core.Jql;
import org.junit.Test;
import junit.framework.TestCase;

public class SimpleInsert extends TestCase {

	@Test public void test() throws Exception {
		Jql jql = new Jql();
		jql.execute("load db './jstests/sampleDb.json';use db;");

		List all = (List)jql.execute("insert into xxx (id,data) values(666,999);");
		assertNotNull(all);
		java.io.File f = new java.io.File(".");
		jql.addRecord("xxx","id",1000,"data",f);
		System.out.println(jql.prettyPrint(all));
	}

}
