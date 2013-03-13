package test;

import java.util.List;

import org.faul.jql.core.Jql;
import org.junit.Test;
import junit.framework.TestCase;

public class TestDrop extends TestCase {

	@Test public void test() throws Exception {
		Jql jql = new Jql();
		jql.execute("load db './jstests/sampleDb.json';");
		jql.execute("use db;");

		String mq = "select customer, product, alsoOwns, payment as empty from xxx;" +
		     "select customer, product, alsoOwns, payment from intermediate where paytype='visa' and payment>100;";

		Object result1 = jql.execute(mq);
		System.out.println("Result1 = " + result1);

		jql.execute("drop table xxx, yyy;");

		result1 = jql.execute(mq);
	}

}
