package test;

import java.util.List;

import org.faul.jql.core.Jql;
import org.junit.Test;
import junit.framework.TestCase;

public class TestOrder extends TestCase {

	@Test public void test() throws Exception {
		Jql jql = new Jql();
		jql.execute("load db './jstests/sampleDb.json';");
		jql.execute("use db;");

		Object order = jql.execute("select * from xxx order by customer desc, amount desc;");
		Object rets = jql.prettyPrint(order);
		System.out.println("Asc: " + rets + "\n");

	}

}
