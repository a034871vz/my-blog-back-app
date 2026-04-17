# my-blog-back-app

## Технологии

- Java 21
- Spring Boot 3.4.1
- Spring Data JPA
- Hibernate
- PostgreSQL 15
- Gradle
- Docker & Docker Compose
- Lombok
- Jackson
- Hibernate Types (JSONB)

## Функциональность

- CRUD операций с постами
- Пагинация и поиск постов (по названию или тексту)
- Работа с комментариями (CRUD)
- Лайки постов (инкремент)
- Загрузка и получение картинок постов
- Хранение тегов в формате JSONB

## Локальный запуск

- ./gradlew bootJar
- docker-compose up -d