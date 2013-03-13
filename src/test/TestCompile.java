package test;

import java.util.List;

import org.faul.jql.core.Jql;
import org.junit.Test;
import junit.framework.TestCase;

public class TestCompile extends TestCase {

	@Test public void test() throws Exception {
		Jql jql = new Jql();
		jql.execute("load db './jstests/sampleDb.json';");
		jql.execute("use db;");

		List stmts = jql.compile("select * from xxx order by customer desc, amount desc;");

		long time1 = java.lang.System.currentTimeMillis();
		for (int i=0;i<10000;i++) {
			Object order = jql.execute("select * from xxx order by customer desc, amount desc;");
			//rets = jql.prettyPrint(order);
			//print("Asc: " + rets + "\n");
		}
		long time2 = java.lang.System.currentTimeMillis();
		long nonCompiled =  time2-time1;
		System.out.println("Done, Non-Compiled: " + nonCompiled);


		time1 = java.lang.System.currentTimeMillis();
		for (int i=0;i<10000;i++) {
			Object order = jql.execute(stmts);
		}
		time2 = java.lang.System.currentTimeMillis();
		long compiled =  time2-time1;
		System.out.println("Done, Compiled: " + compiled);
		assertTrue(compiled < nonCompiled);
	}

}
