Jql = Packages.org.faul.jql.core.Jql;
jql = new Jql();
jql.execute("load db 'sampleDb.json';");
jql.execute("use db;")

//f = select = jql.execute("select distinct customer, product from xxx where customer='ben';");
f = jql.execute("select distinct product, jql('payment,paytype'), customer from xxx where customer ='ben';");
// f = jql.execute("select payment from xxx;");
//all = jql.execute("select * from xxx;");
//select = jql.execute("select distinct customer, product from xxx;");
//unique = jql.execute("select distinct customer from xxx;",file);

//f = jql.execute("select * from xxx where payment_mode_='visa';");
