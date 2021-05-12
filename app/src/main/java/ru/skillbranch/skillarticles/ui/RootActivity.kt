package ru.skillbranch.skillarticles.ui

import android.os.Bundle
import android.os.PersistableBundle
import android.text.Selection
import android.text.Spannable
import android.text.SpannableString
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.text.getSpans
import androidx.core.view.children
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
//import kotlinx.android.synthetic.main.activity_root.*
//import kotlinx.android.synthetic.main.layout_bottombar.*
//import kotlinx.android.synthetic.main.layout_submenu.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.databinding.ActivityRootBinding
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.extensions.setMarginOptionally
import ru.skillbranch.skillarticles.ui.custom.SearchSpan
import ru.skillbranch.skillarticles.ui.custom.SearchFocusSpan
import ru.skillbranch.skillarticles.ui.delegates.AttrValue
import ru.skillbranch.skillarticles.ui.delegates.viewBinding
import ru.skillbranch.skillarticles.viewmodels.*

class RootActivity : AppCompatActivity(), IArticleView {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var viewModelFactory: ViewModelProvider.Factory = ViewModelFactory(this, "0")
    private val viewModel: ArticleViewModel by viewModels { ViewModelFactory(this,"0") }

    private val vb: ActivityRootBinding by viewBinding(ActivityRootBinding::inflate)

    private val vbBottombar
        get() = vb.bottombar.binding
    private val vbSubmenu
        get() = vb.submenu.binding
    private lateinit var searchView: SearchView

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val bgColor by AttrValue(R.attr.colorSecondary)

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val fgColor by AttrValue(R.attr.colorOnSecondary)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //setContentView(vb.root)
        setupToolbar()
        setupBottombar()
        setupSubmenu()

        viewModel.observeState(this, ::renderUi)
        viewModel.observeSubState(this, ArticleState::toBottombarData, ::renderBotombar)
        viewModel.observeSubState(this, ArticleState::toSubmenuData, ::renderSubmenu)

        viewModel.observeNotifications(this) {
            renderNotification(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        val menuItem = menu.findItem(R.id.action_search)
        searchView = (menuItem.actionView as SearchView)
        searchView.queryHint = getString(R.string.article_search_placeholder)

        //restore SearchView onCreateOptionsMenu
        if (viewModel.currentState.isSearch) {
            menuItem.expandActionView()
            searchView.setQuery(viewModel.currentState.searchQuery, false)
            searchView.requestFocus()
        }else{
            searchView.clearFocus()
        }

        menuItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                viewModel.handleSearchMode(true)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                viewModel.handleSearchMode(false)
                return true
            }

        })
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.handleSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.handleSearch(newText)
                return true
            }

        })

        return super.onCreateOptionsMenu(menu)
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        viewModel.saveState()
        super.onSaveInstanceState(outState)
    }

    private fun renderNotification(notify: Notify) {
        val snackbar = Snackbar.make(vb.coordinatorContainer, notify.message, Snackbar.LENGTH_LONG)
            .setAnchorView(vb.bottombar)

        when (notify) {
            is Notify.ActionMessage -> {
                val (_, label, handler) = notify

                with(snackbar) {
                    setActionTextColor(getColor(R.color.color_accent_dark))
                    setAction(label) { handler.invoke() }
                }
            }

            is Notify.ErrorMessage -> {
                val (_, label, handler) = notify

                with(snackbar) {
                    setBackgroundTint(getColor(R.color.design_default_color_error))
                    setTextColor(getColor(android.R.color.white))
                    setActionTextColor(getColor(android.R.color.white))
                    handler ?: return@with
                    setAction(label) { handler.invoke() }
                }
            }
            else -> { /* nothing */ }
        }

        snackbar.show()
    }

    override fun setupSubmenu() {
        with (vbSubmenu) {
            btnTextUp.setOnClickListener { viewModel.handleUpText() }
            btnTextDown.setOnClickListener { viewModel.handleDownText() }
            switchMode.setOnClickListener { viewModel.handleNightMode() }
        }
    }

    override fun setupBottombar() {
        with (vbBottombar) {
            btnLike.setOnClickListener { viewModel.handleLike() }
            btnShare.setOnClickListener { viewModel.handleShare() }
            btnSettings.setOnClickListener { viewModel.handleToggleMenu() }
            btnBookmark.setOnClickListener { viewModel.handleBookmark() }

            btnResultUp.setOnClickListener {
                searchView.clearFocus()
                viewModel.handleUpResult()
            }

            btnResultDown.setOnClickListener {
                searchView.clearFocus()
                viewModel.handleDownResult()
            }

            btnSearchClose.setOnClickListener {
                Log.e("RootActivity", "btnSearchClose.setOnClickListener")
                viewModel.handleSearchMode(false)
                invalidateOptionsMenu()
            }
        }
    }

    override fun renderSubmenu(data: SubmenuData) {
        with (vbSubmenu) {
            switchMode.isChecked = data.isDarkMode
            btnTextUp.isChecked = !data.isBigText
            btnTextDown.isChecked = data.isBigText
        }

        if (data.isShowMenu) vb.submenu.open()
        else vb.submenu.close()
    }

    override fun renderBotombar(data: BottombarData) {
//        Log.e("RootActivity", "renderBottombar $data")
        with (vbBottombar) {
            btnSettings.isChecked = data.isShowMenu
            btnLike.isChecked = data.isLike
            btnBookmark.isChecked = data.isBookmark
        }
        if (data.isSearch) showSearchBar(data.resultsCount, data.searchPosition)
        else hideSearchBar()
    }

    override fun renderUi(data: ArticleState) {
//        Log.e("RootActivity", "renderUi $data")
        delegate.localNightMode =
            if (data.isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO

        with (vb.tvTextContent) {
            textSize = if (data.isBigText) 18f else 14f
            setText(
                if (data.isLoadingContent) "loading..." else data.content.first(),
                TextView.BufferType.SPANNABLE
            )

            movementMethod = ScrollingMovementMethod()
        }

        //bind toolbar
        with (vb.toolbar) {
            title = data.title ?: "loading"
            subtitle = data.category ?: "loading"
            if (data.categoryIcon != null) logo = getDrawable(data.categoryIcon as Int)
        }

        if (data.isLoadingContent) return

        if (data.isSearch) {
            renderSearchResult(data.searchResults)
            renderSearchPosition(data.searchPosition)
        }
        else
            clearSearchResult()
    }

    override fun setupToolbar() {
        setSupportActionBar(vb.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val logo = vb.toolbar.children.find { it is AppCompatImageView } as? ImageView
        logo ?: return
        logo.scaleType = ImageView.ScaleType.CENTER_CROP
        //check toolbar imports
        (logo.layoutParams as? Toolbar.LayoutParams)?.let {
            it.width = dpToIntPx(40)
            it.height = dpToIntPx(40)
            it.marginEnd = dpToIntPx(16)
            logo.layoutParams = it
        }
    }

    override fun renderSearchResult(searchResult: List<Pair<Int, Int>>) {
        val content = vb.tvTextContent.text as Spannable

        clearSearchResult()

        searchResult.forEach { (start, end) ->
            content.setSpan(
                SearchSpan(bgColor, fgColor),
                start,
                end,
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    override fun renderSearchPosition(searchPosition: Int) {
        val content = vb.tvTextContent.text as Spannable

        val spans = content.getSpans<SearchSpan>()

        // remove old search focus span
        content.getSpans<SearchFocusSpan>()
            .forEach { content.removeSpan(it) }

        if (spans.isNotEmpty()) {
            // find position span
            val result = spans[searchPosition]
            // move to selection
            Selection.setSelection(content, content.getSpanStart(result))
            // set new focus span
            content.setSpan(
                SearchFocusSpan(bgColor, fgColor),
                content.getSpanStart(result),
                content.getSpanEnd(result),
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    override fun clearSearchResult() {
        val content = vb.tvTextContent.text as Spannable
        content.getSpans<SearchSpan>()
            .forEach { content.removeSpan(it) }
    }

    override fun showSearchBar(resultsCount: Int, searchPosition: Int) {
        with (vb.bottombar) {
            Log.e("RootActivity", "showSearchBar")
            setSearchState(true)
            setSearchInfo(resultsCount, searchPosition)
        }
        vb.scroll.setMarginOptionally(bottom = dpToIntPx(56))
    }

    override fun hideSearchBar() {
        with (vb.bottombar) {
            Log.e("RootActvity", "hideSearchBar")
            setSearchState(false)
        }
        vb.scroll.setMarginOptionally(bottom = dpToIntPx(0))
    }
}
