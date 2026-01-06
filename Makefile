.PHONY: start stop restart catalog-service build run clean test

start:
	docker compose up -d

stop:
	docker compose down --volumes

restart: stop start

migrate:
	flyway migrate

catalog-service:
	npx json-server --watch src/main/resources/json-server/db.json --port 3000

build:
	./mvnw clean package -DskipTests

run:
	./mvnw spring-boot:run

clean:
	./mvnw clean

test:
	./mvnw test

install:
	./mvnw clean install