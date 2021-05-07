package ru.skillbranch.skillarticles.ui

import ru.skillbranch.skillarticles.viewmodels.ArticleState
import ru.skillbranch.skillarticles.viewmodels.BottombarData
import ru.skillbranch.skillarticles.viewmodels.SubmenuData

interface IArticleView {
    // отрисовка вхождений поискового запрос в контент
    fun renderSearchResult(searchResult: List<Pair<Int, Int>>)
    // отрисовка и перевод фокуса на текущюю позицию поиска
    fun renderSearchPosition(searchPosition: Int)
    // очистка результатов поиска
    fun clearSearchResult()
    // отображение панели поиска
    fun showSearchBar(resultsCount: Int, searchPosition: Int)
    // скрытие панели поиска
    fun hideSearchBar()
    // установка listeners для submenu и настройка внешнего вида представления
    fun setupSubmenu()
    // установка listeners для bottombar и настройка внешнего вида представления
    fun setupBottombar()
    // установка listeners для toolbar и настройка внешнего вида представления
    fun setupToolbar()
    // отрисовка данных для Bottombar на слое представления
    fun renderBotombar(data: BottombarData)
    // отрисовка данных для Submenu на слое представления
    fun renderSubmenu(data: SubmenuData)
    // отрисовка данных для UI на слое представления
    fun renderUi(data: ArticleState)
}
