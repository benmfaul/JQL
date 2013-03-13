Jql = Packages.org.faul.jql.core.Jql;
jql = new Jql();
jql.addDatabase("website");
jql.execute("use website;");

jql.addTable("twitter");
jql.addTable("config");
jql.addTable("sentiments");
jql.addTable("stage");

all = jql.execute("insert into twitter (user,name,topic, dictionary,timeadded,raw,slope,rawarray,slopearray,tweets)" +
		" values('root','Newt Gingrich','Newt Gingrich','default',0,3,0,'json::[]','json::[]', 'json::[]');");
all = jql.execute("insert into twitter (user,name,topic, dictionary,timeadded,raw,slope,rawarray,slopearray,tweets)" +
		" values('root','Miley','Miley Cyrus','default',0,3,0,'json::[]','json::[]', 'json::[]');");
	all = jql.execute("insert into twitter (user,name,topic, dictionary,timeadded,raw,slope,rawarray,slopearray,tweets)" +
		" values('root','Jessica','Jessica Alba','default',0,3,0,'json::[]','json::[]', 'json::[]');");
		
all = jql.execute("insert into twitter (user,name,topic, dictionary,timeadded,raw,slope,rawarray,slopearray,tweets)" +
		" values('root','Mitt','Mitt Romney','default',0,3,0,'json::[]','json::[]', 'json::[]');");

print(jql.prettyPrint(all));

all = jql.execute("insert into config (turl,tuser,tpass,maxusers,maxtwits,wsport) values('https://stream.twitter.com/1/statuses/filter.json'," +
			"'bmfaul', 'fuzzy123', 32,10,8081);");
print(jql.prettyPrint(all));

jql.setCSVSeparator("\t");
data = jql.readFile("junk.txt");
jql.fromCSV("stage",data);
all = jql.execute("select * from stage;");
//print(jql.prettyFormat(all));

for (i=0;i<all.size();i++) {
	map = all.get(i);
	map.put("sfinn",0);
	s = jql.toJson(map,false)
	q = "insert into sentiments (sentiment) values " + 
		"('json::"+s+"');";
	print(q);
	jql.execute(q);
}
jql.execute("drop table stage;");

//q = "insert into sentiments (sentiment) values " + 
//	"('json::{\"word\":\"shit\",\"sfinn\":3,\"finn\":0}');";
//all = jql.execute(q);
//print(jql.prettyPrint(all));

jql.execute("save db 'webdb.json';");

print("Done!");