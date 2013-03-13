package test;

import java.util.List;

import org.faul.jql.core.Jql;
import org.junit.Test;
import junit.framework.TestCase;

public class TestFunction extends TestCase {

	@Test public void test() throws Exception {
		Jql jql = new Jql();
		jql.execute("load db './jstests/sampleDb.json';");


		Object sum = jql.execute("select sum(amount) from xxx;");
		System.out.println("DataOnly: " + sum + "\n");
	}

}
