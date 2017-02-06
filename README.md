# FixMyBug
Changelog

2/06/17(ish)
select distinct 
Master.event_id, Master.session_id, Master.id as master_id, Compile.success, Outputs.source_file_id Outputs.start_line, Master.event_id from (compile_events Compile join master_events Master on Compile.id = Master.event_id ) left join compile_outputs Outputs on Outputs.compile_event_id = Compile.id where Master.event_type = 'CompileEvent' limit 20;

1/13/17
select distinct MasterFail.id as Fail_id, MasterSuccess.id as Success_id, CO.source_file_id, CO.start_line from
((compile_outputs CO join compile_events CompileFail on CO.compile_event_id = CompileFail.id)
join master_events MasterFail on CompileFail.id = MasterFail.event_id) 
join (compile_events CompileSuccess join master_events MasterSuccess on CompileSuccess.id = MasterSuccess.event_id) on CompileFail.id = CompileSuccess.id - 1
where MasterFail.event_type = 'CompileEvent' and MasterSuccess.event_type = 'CompileEvent'
and CompileFail.success = 0 and CompileSuccess.success = 1
and MasterFail.session_id = MasterSuccess.session_id and MasterFail.id > 25000 limit 30000;

1/11/17
select CO1.compile_event_id, C1.id, C2.id, M1.session_id, M2.session_id, CO1.source_file_id from ((compile_outputs CO1 join compile_events C1 on CO1.compile_event_id = C1.id) join master_events M1 on C1.id = M1.event_id) join (compile_events C2 join master_events M2 on C2.id = M2.event_id) where M1.event_type = 'CompileEvent' and M2.event_type = 'CompileEvent' and C1.success = 0 and C1.id = C2.id-1 and C2.success = 1 and M1.session_id = M2.session_id limit 20;

1/8/17 - select C1.id, C1.success, C2.id, C2.success from compile_events C1 join compile_events C2 where C1.success = 1 and C2.id = C1.id-1 and C2.success = 0 limit 20;

^helpful sql query for grabbing success and previous error

select id, event_id, session_id, user_id, event_type from master_events where event_type = 'CompileEvent' order by session_id limit 20;

^ordering sessions in the master event table

select C2.id, C1.id, M1.session_id, M2.session_id from (compile_events C1 join master_events M1 on C1.id = M1.event_id) join (compile_events C2 join master_events M2 on C2.id = M2.event_id) where M1.event_type = 'CompileEvent' and M2.event_type = 'CompileEvent' and C1.success = 1 and C2.id = C1.id-1 and C2.success = 0 and M1.session_id = M2.session_id limit 20;

^grabs the event ids from consecutive compiles (error then success) of the same user and session


10/20/16 - Added setup notes - Joe

SETUP NOTES

The database lives in the root directory of the computer (in "/FixMyBugDB"), so everyone should have access to it. Trying to open an sqlite database that doesn't exist will create a new database, so there are a few TEST_DATABASEs running around; they are empty, and if you find one (especially in the git repo), go ahead and delete it.

Also in the root directory is the Maven archive. To get that up and running, all you need to do is edit your ~/.bash_profile folder and add the path "/FixMyBugDB/apache-maven-3.3.9/bin" to the PATH environment variable. You can verify that maven is installed by typing "mvn -v" and looking for output.

To "compile" the client or server programs, navigate to the correct folder (e.g. gs-rest-server) and type "mvn package". This should create a jar that you can then execute - "java -jar target/name-of-jar-file.jar".
