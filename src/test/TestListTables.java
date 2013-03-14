package test;

import java.util.List;
import java.util.Map;

import org.faul.jql.core.Jql;
import org.junit.Test;
import junit.framework.TestCase;

/**
 * Test the listTables() and execute("listTables;") extension to JQL.
 * @author Ben M. Faul
 *
 */
public class TestListTables extends TestCase {

	@Test public void test() throws Exception {
		Jql jql = new Jql();
		jql.execute("load db './jstests/sales.json';");
		jql.execute("use db;");

		List rets = (List)jql.listTables();
		assertNotNull(rets);
		assertTrue(rets.size()==1);
		Map m = (Map)rets.get(0);
		Integer count = (Integer)m.get("count");
		String name = (String)m.get("name");
		Boolean locked = (Boolean)m.get("locked");
		
		assertNotNull(count);
		assertNotNull(name);
		assertNotNull(locked);
		assertTrue(count == 10);
		assertTrue(name.equals("sales"));
		assertTrue(locked==false);
		
		rets = (List)jql.execute("listTables;");
		assertNotNull(rets);
		assertNotNull(rets);
		assertTrue(rets.size()==1);
		m = (Map)rets.get(0);
		count = (Integer)m.get("count");
		name = (String)m.get("name");
		locked = (Boolean)m.get("locked");
		
		assertNotNull(count);
		assertNotNull(name);
		assertNotNull(locked);
		assertTrue(count == 10);
		assertTrue(name.equals("sales"));
		assertTrue(locked==false);
		
		
		System.out.println(rets);
		
	}

}
