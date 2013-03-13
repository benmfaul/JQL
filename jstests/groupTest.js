Jql = Packages.org.faul.jql.core.Jql;
jql = new Jql();
jql.execute("load db 'sampleDb.json';");


all = jql.execute("select * from xxx;");
print();

print("All: " + all + "\n");


sum = jql.execute("select sum(amount) from xxx;");
print("DataOnly: " + sum + "\n");

nogroup = jql.execute("select customer, sum(amount) from xxx;");
print("No group: " + nogroup + "\n"); 


group = jql.execute("select customer, sum(amount) from xxx group by customer;");
print("Group: " + group + "\n");



