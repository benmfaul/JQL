Jql = Packages.org.faul.jql.core.Jql;
jql = new Jql();
jql.execute("load db 'sampleDb.json';use db;");

all = jql.execute("insert into xxx (id,data) values(666,999);");

f = new java.io.File(".");
jql.addRecord("xxx","id",1000,"data",f);
print(jql.prettyPrint(all));