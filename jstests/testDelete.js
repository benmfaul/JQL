Jql = Packages.org.faul.jql.core.Jql;
jql = new Jql();
jql.execute("load db 'sampleDb.json';");
jql.execute("use db;")

all = jql.execute("delete from xxx where customer='ben';");

print(jql.prettyPrint(all));