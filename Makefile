setup:
	sudo mkdir -p /etc/rds2fluentd/
	sudo chown -R ${USER} /etc/rds2fluentd/

run-dependencies:
	cd rds2fluentd && docker-compose up

compile:
	cd rds2fluentd && mvn clean package

run: setup
	cd rds2fluentd && java -jar target/rds2fluentd-1.0-SNAPSHOT.jar server config-test.yaml


