package test;

import java.util.List;
import java.util.Map;

import org.faul.jql.core.Jql;
import org.junit.Test;
import junit.framework.TestCase;

public class TestUpdate extends TestCase {

	@Test public void test() throws Exception {
		Jql jql = new Jql();
		jql.execute("load db './jstests/sampleDb.json';");
		jql.execute("use db;");
		
		List test = (List)jql.execute("select customer from xxx where acctnum=483284;");
		assertEquals(2,test.size());
		Map m = (Map)test.get(0);
		String v =(String) m.get("customer");
		assertTrue(v.equals("jt"));

		Object order = jql.execute("update xxx set customer=1+1 where acctnum=483284;"); 
		String rets = jql.prettyPrint(order);
		System.out.println("Asc: " + rets + "\n");
		
		test = (List)jql.execute("select customer from xxx where acctnum=483284;");
		System.out.println("Test: " + test);
		m = (Map)test.get(0);
		Double d =  (Double)m.get("customer");
		assertTrue(d==2.0);
	}

}
