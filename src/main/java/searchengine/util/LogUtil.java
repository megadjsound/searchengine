package searchengine.util;

public interface LogUtil {
    //Errors ApiController
    String ERROR_INDEXING_ALREADY_STARTED = "Индексация уже запущена";
    String ERROR_INDEXING_NOT_STARTED = "Индексация не запущена";

    //Logs IndexingServiceImpl
    String LOG_STOP_INDEXING = " Запускаем остановку индексации";
    String LOG_START_INDEXING_PAGE = " Запущена индексация страницы: ";
    String LOG_DONE_INDEXING_PAGE = " Завершена индексация страницы: ";
    String LOG_NOT_AVAILABLE_PAGE = "Данная страница находится за пределами сайтов, " +
            "указанных в конфигурационном файле";
    String LOG_NOT_SITE_INDEXING = "Сайты не проиндексированы";

    //Erorrs and Logs SearchServiceImpl
    String LOG_FINISH_SITES_SEARCH = " Поиск по сайтам завершен.";
    String ERROR_EMPTY_QUERY = "Задан пустой поисковый запрос";
    String ERROR_NOT_FOUND = "По данному запросу ни чего не найдено.";

    //Logs IndexingAllPages, IndexingOnePage
    String LOG_ADD_SITE = " добавлен сайт: ";
    String LOG_UPD_SITE = " добавлено инфо по сайту: ";
    String LOG_ADD_PAGES = " добавляем страницы сайта: ";
    String LOG_ADD_PAGES_DONE = " добавлены страницы сайта: ";
    String LOG_ADD_LEMMAS = " добавляем леммы сайта: ";
    String LOG_ADD_LEMMAS_DONE = " добавлены леммы сайта: ";
    String LOG_ADD_INDEX = " добавлеяем индексы для сайта: ";
    String LOG_ADD_INDEX_DONE = " добавлены индексы для сайта: ";
    String LOG_DELETE_DATA = " начинаем удалять прежние данные по сайту:  ";
    String LOG_DELETE_DATA_DONE = " удалены прежние данные по сайту:  ";
    String LOG_START_INDEXING = " Запущена индексация сайта: ";
    String LOG_INDEXING_COMPLETED = " Индексация завершена ";
    String LOG_INDEXING_STOPPED = " Остановка индексации завершена.";
    String LOG_INDEXING_BREAK = " Индексация остановлена пользователем.";

    String LOG_DELETE_PAGE = " удаляем данные для страницы сайта: ";
    String LOG_ADD_LEMMAS_AND_INDEX = " добавлены леммы и индексы страницы: ";

    //Logs UrlUtil
    String LOG_MALFORMED_EXCEPTION = "! Некорректный URL адрес: ";
    String LOG_EXCEPTION = "! Ошибка: ";
}
