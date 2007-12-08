create table job_status (
              job_id varchar(32) not null primary key,
       	      code integer not null,
       	      message text not null,
       	      base_url varchar(128) not null,
	      start_time varchar(128),
	      last_update varchar(128),
       	      client_dn varchar(128),
       	      client_ip varchar(128),
       	      service_name varchar(128));

create table job_output (
              job_id varchar(32) not null primary key,
       	      std_out varchar(128) not null,
       	      std_err varchar(128) not null,
       	      foreign key(job_id) references job_status(job_id));

create table output_file (
              job_id varchar(32) not null,
       	      name varchar(128) not null,
       	      url varchar(128) not null,
       	      foreign key(job_id) references job_status(job_id));
