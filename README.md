# A Proof of Concept Ingesting Data From Postgresql

This program utilizes debezium.io. Most of this program inspired by https://github.com/sohangp/embedded-debezium, but changed  from Springboot to Dropwizard to make it lightweight.

# How to
- `make compile`. To compile the program
- `make run-dependencies` to run postgres locally using docker-swarm. It should be run only once.

Please prepare table (see the docker-compose.yaml to see the postgres config)
```sql
CREATE TABLE public.student
(
    id integer NOT NULL,
    address character varying(255),
    email character varying(255),
    name character varying(255),
    CONSTRAINT student_pkey PRIMARY KEY (id)
);

INSERT INTO STUDENT(ID, NAME, ADDRESS, EMAIL) VALUES('1','Jack','Dallas, TX','jack@gmail.com');
INSERT INTO STUDENT(ID, NAME, ADDRESS, EMAIL) VALUES('2','Foo','Slipi, JKT','foo@gmail.com');

UPDATE STUDENT SET EMAIL='jill@gmail.com', NAME='Jill' WHERE ID = 1; 
```

- `make run` to run. It will create a directory in `/etc/rds2fluentd/` to store the offset/checkpoint of ingestion.

# Explanation

See example of [config-test.yaml](./rds2fluentd/config-test.yaml) to see how the configuration is. 
**Note**: this is still not production ready, we should separate the config and the secret.
For now it can be run okay in local. 

# TODO
- [] FluentD in local via docker-compose
- [] Move secret to another configuration