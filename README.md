# CardManager API

Простой REST API для управления банковскими картами с JWT-аутентификацией.

## Быстрый старт

### Требования:
- Установленный Docker
- Порт 8080 свободен

### Запуск:
```bash
git clone https://github.com/NikitaDolgov44/cardmanager.git
cd cardmanager
docker-compose up

Доступ к API:
http://localhost:8080/swagger-ui.html

Тестовые пользователи
Роль	        Логин	              Пароль
Админ	        admin@example.com	  admin123
Пользователь	user@example.com	  user123
