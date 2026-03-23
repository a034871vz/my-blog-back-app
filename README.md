# my-blog-back-app

## Технологии

- Java 21
- Spring Framework 6.2.1
- Spring Data JPA 3.4.1
- Hibernate ORM 6.6
- PostgreSQL 15
- Maven
- Docker & Docker Compose
- Tomcat 10.1
- Lombok
- Jackson (JSON)
- Hibernate Types (для JSONB)

## Функциональность

- CRUD операций с постами
- Пагинация и поиск постов (по названию или тексту)
- Работа с комментариями (CRUD)
- Лайки постов (инкремент)
- Загрузка и получение картинок постов
- Хранение тегов в формате JSONB

## Локальный запуск

- mvn clean package
- docker-compose up -d