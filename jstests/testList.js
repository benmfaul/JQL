Jql = Packages.org.faul.jql.core.Jql;
jql = new Jql();
jql.execute("load db 'jsonTest1.json';");
jql.execute("use db;");
jql.listTables();