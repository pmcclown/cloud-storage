## Резюме
Репозиторий содержит дипломный проект ["Облачное хранилище”](https://github.com/netology-code/jd-homeworks/blob/master/diploma/cloudservice.md) 
из курса ["Java-разработчик с нуля"](https://netology.ru/programs/java-developer#/) от [netology.ru](https://netology.ru/).
## Описание проекта
Задача — разработать REST-сервис. Сервис должен предоставить REST-интерфейс для загрузки файлов и вывода списка уже 
загруженных файлов пользователя.
Все запросы к сервису должны быть авторизованы. Заранее подготовленное веб-приложение [(FRONT)](https://github.com/netology-code/jd-homeworks/tree/master/diploma/netology-diplom-frontend) 
должно подключаться к разработанному сервису без доработок, а также использовать функционал FRONT для авторизации, 
загрузки и вывода списка файлов пользователя.

Детальные требования к заданию изложены [здесь](https://github.com/netology-code/jd-homeworks/blob/master/diploma/cloudservice.md)
## Описание решения
Приложение построено по стандартной трехслойной архитектуре:
- <b>Контроллеры.</b> Чаще всего, в самом контроллере не описывается логика обработки данных. Создаются дополнительные, 
сервисные классы, которые выполняют все основные задачи по обработке данных. Задача методов в контроллере - вызов 
сервисных методов в нужном порядке и возвращение результатов клиенту.
- <b>Сервисы</b>. Реализуют взаимодействие между слоями представления и бизнес-логики. Примерами реализаций сервисного 
слоя являются контроллеры, веб-сервисы и слушатели очередей сообщений.
- <b>Репозиторий</b>. Выполняет операции взаимодействия с базой данных, такие как извлечение и сохранение данных в БД.
  Реализация в интерфейсах Spring data JPA в пакете repository.<br>

  Дополнительно выделены компоненты, выполняющие аутентификацию и авторизацию входящих запросов через Spring Security. 
  В приложении реализована аутентификация и авторизация с помощью jwt токенов. Все энд-поинты приложения требуют 
  аутентификации за исключением /login, который доступен всем.<br>

  Обратившись к /login пользователь получает в ответ jwt 
  токен, который ему необходимо прилагать в заголовке каждого запроса (кроме /login), а в этот момент в БД создается
  пользовательская сессия с привязкой уникального authtoken к учетной записи пользователя.
  При выполенении /logout, сессия пользователя удаляется из БД.<br>

  Для выполнения операций с токенами использована библиотека io.jsonwebtoken. 
  Настройки для security находятся в config/SecurityConfig.<br>

  Для хранения данных использована база данных PostgreSQL.
  Структура БД описана в dump-файле /db/initdb.sql<br>
  Создание и администрирование пользователей приложения находится за рамками настоящего проекта. Одна учетная запись
  пользователя создается в БД при старте приложения, если его там еще нет.<br>
  Настройки приложения находятся в файлах application.yml. Текстовые сообщения в messages.properties.<br>

## Сборка и запуск
Для запуска приложения понадобится docker. Необходимо перейти в корень проекта и выполнить следующие инструкции:
- Собираем docker образ backend части приложения: выполняем команду `docker build -f DockerfileBACKEND -t backend .` 
- Собираем docker образ frontend части приложения: выполняем команду `docker build -f DockerfileFRONTEND -t frontend .` 
- Загружаем latest версию docker образа для postgres: выполняем команду `docker pull postgres` 
- Запускаем базу данных, backend и frontend с помощью docker-compose: выполняем команду `docker-compose -f docker-compose.yml up -d`
- Зайти на http://localhost:80 и залогиниться в приложение под пользователем `alex` c паролем `1234` 