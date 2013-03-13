Jql = Packages.org.faul.jql.core.Jql;
jql = new Jql();

tables = jql.execute("load db 'jsonTest1.json';");
print("Tables = " + tables);
jql.execute("use db;");

t = jql.execute("select * from xxx;");
jql.execute("save db 'c:/tapin/junk.json';");