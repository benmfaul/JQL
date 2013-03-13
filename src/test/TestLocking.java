package test;

import java.util.List;

import org.faul.jql.core.Jql;
import org.junit.Test;
import junit.framework.TestCase;

public class TestLocking extends TestCase {

/*	@Test public void testSingleLock() throws Exception {
		Jql jql = new Jql();
		jql.execute("load db './jstests/sales.json';");
		jql.execute("use db;");

		try {
			jql.execute("lock table sales in exclusive mode;");
			System.out.println("Got the lock");
		} catch (Exception e) {
			System.out.println("Did not get the lock: " + e);
			fail("Could not get lock as expected");
		}
	}
	@Test public void testNoTable() throws Exception {
		Jql jql = new Jql();
		jql.execute("load db './jstests/sales.json';");
		jql.execute("use db;");

		try {
			jql.execute("lock table xxx in exclusive mode;");
			fail("Should not have gotten a lock on un defined table");
		} catch (Exception e) {
			System.out.println("Did not get the lock. this is expected");
		}
	}
	
	@Test public void testDuplicateLock() throws Exception {
		Jql jql = new Jql();
		jql.execute("load db './jstests/sales.json';");
		jql.execute("use db;");

		try {
			jql.execute("lock table sales in exclusive mode;");
			System.out.println("Got the lock");
		} catch (Exception e) {
			System.out.println("Did not get the lock: " + e);
			fail("Could not get lock as expected");
		}
		
		try {
			jql.execute("lock table sales in exclusive mode;");
			System.out.println("Got the lock");
		} catch (Exception e) {
			System.out.println("Did not get the lock: " + e);
			fail("Could not get lock as expected");
		}
	} */
	
	@Test public void testConcurrentLock() throws Exception {
		Jql jql = new Jql();
		jql.execute("load db './jstests/sales.json';");
		jql.execute("use db;");
		
		System.out.println("------------------");
		Runner r = new Runner(jql);
		Thread.sleep(1000);
		assertTrue(r.locked);
		Runner rr = new Runner(jql);
		Thread.sleep(1000);
		assertFalse(rr.locked);
		r.halt();

	}
	

}

class Runner implements Runnable {
	Jql jql;
	Thread me;
	boolean halt = false;
	boolean locked = false;
	public Runner(Jql jql) {
		this.jql = jql;
		me = new Thread(this);
		me.start();
	}
	
	public void halt() {
		halt = true;
	}
	
	public void run() {
		try {
			jql.execute("lock table sales in exclusive mode;");
			System.out.println("" + this + " Got the lock");
			locked = true;
		} catch (Exception e) {
			System.out.println("" + this + " Did not the lock");
			locked = false;
			return;
		}
		while(halt == false) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
