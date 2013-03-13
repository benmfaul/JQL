/*
 * This file is part of JQL.

 *
 * JQL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JQL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Zql.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gibello.zql;
/**
 * ZLockTable: an SQL LOCK TABLE statement
 */
public class ZSaveFile implements ZStatement {

  String fileName;
  String tableName;

  public ZSaveFile() {}

  public void addFileName(String f) { fileName = f; }
  public String getFileName() { return fileName; } 
  
  public void addTableName(String t) { tableName = t; }
  public String getTableName() { return tableName; } 
};