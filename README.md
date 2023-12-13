<h1 align="center">Итоговый проект по курсу Java-разработчик</h1>
<h2 align="center">Поисковый движок по сайтам</h2>

## Описание
Проект с подключенными библиотеками лемматизаторами.
Написан на языке Java и использует Spring Framework для реализации RESTful веб-сервиса.
Содержит несколько контроллеров, сервисов и репозиториев с подключением к бд MySQL.

Выполняет индексирование (сохраняет в базу данных страницы и найденные на них леммы) сайтов, указанных в файле настроек (application.yaml), а также отдельных страниц по запросу, а также производит поиск слов по найденным

## Технологии
- движок разработан на фреймворке Spring и сборщика Maven
- реализовано многопоточное приложение, которое обходит все страницы сайта начиная с главной
- использована с библиотека лемматизации слов
- реализована система индексации страниц сайта — систему, которая позволяет подсчитывать слова на страницах сайта и по поисковому запросу определяет наиболее релевантные (соответствующие поисковому запросу) страницы
- реализована система поиска информации с использованием созданного поискового индекса
- база данных MySQL

### Принципы работы поискового движка

- В конфигурационном файле перед запуском приложения задаются адреса сайтов, по которым движок должен осуществлять поиск.
- Поисковый движок самостоятельно обходит все страницы заданных сайтов и индексирует их (создавать так называемый индекс) так, чтобы потом находить наиболее релевантные страницы по любому поисковому запросу.
    - Реализована функция остановки индексации.
    - Имеется возможность индексации конкретной страницы.
- Пользователь присылает запрос через API движка. Запрос — это набор слов, по которым нужно найти страницы сайта.
- Запрос определённым образом трансформируется в список слов, переведённых в базовую форму. Например, для существительных — именительный падеж, единственное число.
- В индексе ищутся страницы, на которых встречаются все эти слова.
  Результаты поиска ранжируются, сортируются и отдаются пользователю.

- ## Настройка

Все настройки находятся в файле конфигураций application.yaml

Для запуска нужно указать 
 - параметры БД в секции datasource: url, username, password.
 - список сайтов в секции indexing-settings, по которым будет проведена индексация.
 - percentLemma в секции params-query для указания процента лемм, которые встречаются на слишком большом количестве страниц (при указании 100 нет фильтрации)

## Работа с web-интерфейсом: при запуске страницы проекта (http://localhost:8081/) на экране открывается меню с тремя пунктами:
- в пункте "DASHBOARD" показано количество проиндексированных сайтов и страниц, и найденных лемм, а также для каждого сайта показан текущий статус индексации;
- в пункте "MANAGEMENT" два управляющих элемента: "START INDEXING" (после нажатия текст меняется на "STOP INDEXING") для запуска/остановки индексирования и форма добавления отдельной страницы;
- в пункте "SEARCH" - поиск текста по сохранённым в БД данным.

При работе движка весь функционал логируется (информация, ошибки) через Spring logger.

## Инструкция по запуску проекта:
- скачать исходный текст со страницы проекта, разрешить зависимости maven командой вида `mvn dependency:resolve`, прописать в файле настроек параметры доступа к базе данных MySQL параметры индексируемых сайтов, запустить проект на выполнение.