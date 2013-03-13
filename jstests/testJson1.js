Jql = Packages.org.faul.jql.core.Jql;
jql = new Jql();

jql.execute("load db 'c:/tapin/workspace/jsql/jsonTest1.json';");
jql.execute("use db;");

all = jql.execute("select * from xxx;");
print();

print("All: " + all + "\n");

dataOnly = jql.execute("select data from xxx;");
print("DataOnly: " + dataOnly + "\n");

constraint = jql.execute("select * from xxx where id>5 and id<40;");
print("Constraint: " + constraint + "\n");

sum = jql.execute("select sum(data), max(data) from xxx;");
print("Sum, Max: " + sum + "\n");

position = jql.execute("select * from xxx where id in(1,2,3);");
print("Position: " + position + "\n");

select = jql.execute("select data from xxx where id in(1,2,3);");
print("Select: " + select + "\n");

tween = jql.execute("select * from xxx where id between 1 and 3;");
print("Tween: " + tween + "\n");

nottween = jql.execute("select * from xxx where id not between 1 and 3;");
print("Not tween: " + nottween + "\n");

order = jql.execute("select * from xxx where id between 1 and 3 order by data asc;");
print("Asc: " + order + "\n");

selorder = jql.execute("select data from xxx where id between 1 and 3 order by data desc;");
print("Desc: " + selorder + "\n");