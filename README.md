# Демо проект к курсу "Domain Driven Design и Clean Architecture на языке Java"
📚 Подробнее о курсе: [microarch.ru/courses/ddd/languages/java](https://microarch.ru/courses/ddd/languages/java?utm_source=gitlab&utm_medium=repository)

---

## Условия использования

Вы можете использовать и модифицировать данный код **в образовательных целях**, при условии сохранения ссылки на курс и оригинального источника.

---

# Запросы к БД
```
SELECT * FROM public.couriers;
SELECT * public.storage_places;
SELECT * FROM public.orders;
SELECT * public.outbox;
```

# Очистка БД (все кроме справочников)
```
DELETE FROM public.orders;
DELETE FROM public.items;
DELETE FROM public.outbox;
```

# Генерация HTTP сервера
```
openapi-generator generate \
        -i <file path or url> \
        -g spring \
        -o src/main/java \
        --global-property apis,models \
        --package-name microarch.delivery.adapters.in.http \
        --skip-validate-spec \
        --additional-properties=interfaceOnly=true,useTags=true,skipDefaultInterface=false,useJakartaEe=true,useSpringBoot3=true,sourceFolder=src/main/java,modelPackage=microarch.delivery.adapters.in.http.openapi.model,apiPackage=microarch.delivery.adapters.in.http.openapi.api

```

# Генерация gRPC клиента из Protobuf
```
mvn clean compile
```
# Генерация интеграционных событий Kafka из Protobuf
```
mvn clean compile
```

## Лицензия

Код распространяется под лицензией [MIT](./LICENSE).  
© 2025 microarch.ru