package org.gibello.zql;

import java.util.List;

/**
 * Linkage between the production in ZqljjParser.java to the Jql method listTables()
 * @author Ben Faul
 *
 */
public class ZListTables implements ZStatement {

	  List returns_;

	  public ZListTables() {}
	  public void addTablesInfo(List tables) { returns_ = tables; }
	  public List getTableInfo() { return returns_; } 
}
