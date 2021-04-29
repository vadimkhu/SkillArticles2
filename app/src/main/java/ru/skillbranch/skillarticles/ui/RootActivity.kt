package ru.skillbranch.skillarticles.ui

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import com.google.android.material.snackbar.Snackbar
//import kotlinx.android.synthetic.main.activity_root.*
//import kotlinx.android.synthetic.main.layout_bottombar.*
//import kotlinx.android.synthetic.main.layout_submenu.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.databinding.ActivityRootBinding
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.ui.delegates.viewBinding
import ru.skillbranch.skillarticles.viewmodels.*

class RootActivity : AppCompatActivity(), IArticleView {

    private val viewModel: ArticleViewModel by viewModels { ViewModelFactory("0") }
    private val vb: ActivityRootBinding by viewBinding(ActivityRootBinding::inflate)
    private val vbBottombar
        get() = vb.bottombar.binding
    private val vbSubmenu
        get() = vb.submenu.binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(vb.root)
        setupToolbar()
        setupBottombar()
        setupSubmenu()

        viewModel.observeState(this, ::renderUi)
        viewModel.observeSubState(this, ArticleState::toBottombarData, ::renderBottombar)
        viewModel.observeSubState(this, ArticleState::toSubmenuData, ::renderSubmenu)

        viewModel.observeNotifications(this) {
            renderNotification(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        val menuItem = menu.findItem(R.id.action_search)
        val searchView = (menuItem.actionView as SearchView)
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
        }
    }

    override fun renderSubmenu(data: SubmenuData) {
        Log.e("RootActivity", "renderSubmenu $data")
        with (vbSubmenu) {
            switchMode.isChecked = data.isDarkMode
            btnTextUp.isChecked = !data.isBigText
            btnTextDown.isChecked = data.isBigText
        }

        if (data.isShowMenu) vb.submenu.open() else vb.submenu.close()
    }

    override fun renderBottombar(data: BottombarData) {
//        Log.e("RootActivity", "renderBottombar $data")
        with (vbBottombar) {
            btnSettings.isChecked = data.isShowMenu
            btnLike.isChecked = data.isLike
            btnBookmark.isChecked = data.isBookmark
        }
    }

    override fun renderUi(data: ArticleState) {
//        Log.e("RootActivity", "renderUi $data")
        delegate.localNightMode =
            if (data.isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO

        with (vb.tvTextContent) {
            textSize = if (data.isBigText) 18f else 14f
            text = if (data.isLoadingContent) "loading" else data.content.first()
            // TODO set content as spannable
        }

        //bind toolbar
        vb.toolbar.title = data.title ?: "loading"
        vb.toolbar.subtitle = data.category ?: "loading"
        if (data.categoryIcon != null) vb.toolbar.logo = getDrawable(data.categoryIcon as Int)
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
        TODO("Not yet implemented")
    }

    override fun renderSearchPosition(searchPosition: Int) {
        TODO("Not yet implemented")
    }

    override fun clearSearchResult() {
        TODO("Not yet implemented")
    }

    override fun showSearchBar() {
        TODO("Not yet implemented")
    }

    override fun hideSearchBar() {
        TODO("Not yet implemented")
    }
}
