package test;

import java.util.List;

import org.faul.jql.core.Jql;
import org.junit.Test;
import junit.framework.TestCase;

public class TestSave extends TestCase {

	@Test public void test() throws Exception {
		Jql jql = new Jql();

		jql.execute("load db './jstests/webdb.json';");

		jql.execute("save db 'junk.json';");

	}

}
