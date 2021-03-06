package top.rechinx.meow.ui.filter

import android.os.Bundle
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.flexibleadapter.items.ISectionable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import top.rechinx.meow.core.source.Source
import top.rechinx.meow.core.source.SourceManager
import top.rechinx.meow.core.source.model.Filter
import top.rechinx.meow.core.source.model.FilterList
import top.rechinx.meow.data.database.model.Manga
import top.rechinx.meow.data.repository.CataloguePager
import top.rechinx.meow.ui.filter.items.*
import top.rechinx.rikka.mvp.BasePresenter

class FilterPresenter(sourceId: Long): BasePresenter<FilterActivity>(), KoinComponent {

    val sourceManager by inject<SourceManager>()

    val source = sourceManager.get(sourceId) as Source

    private lateinit var pager: CataloguePager

    private var pagerDisposable: Disposable? = null

    private var pageDisposable: Disposable? = null

    var sourceFilters = FilterList()
        set(value) {
            field = value
            filterItems = value.toItems()
        }

    var filterItems: List<IFlexible<*>> = emptyList()

    var appliedFilters = FilterList()

    var query = ""
        private set

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        sourceFilters = source.getFilterList()
        restartPager()
    }

    fun restartPager(query: String = this.query, filters: FilterList = this.appliedFilters) {
        pager = CataloguePager(source, query, filters)

        pagerDisposable?.let { remove(it) }
        pagerDisposable = pager.results
                .observeOn(Schedulers.io())
                .map {
                    it.first to it.second.map {
                    val manga = Manga()
                    manga.copyFrom(it)
                    manga.sourceId = source.id
                    manga
                } }
                .map { it.first to it.second.map { CatalogueItem(it) } }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeReplay({ view, (page, mangas) ->
                    view.onAddPage(page, mangas)
                }, { _, error ->
                    error.printStackTrace()
                })

        // request first page
        requestNext()
    }

   fun requestNext() {
        if(!hasNextPage()) return
        pageDisposable?.let { remove(it) }
        pageDisposable = Observable.defer { pager.requestNext() }
                .subscribeFirst({ _, _ -> }, FilterActivity::onAddPageError)
    }

    fun hasNextPage(): Boolean {
        return pager.hasNextPage
    }


    private fun FilterList.toItems(): List<IFlexible<*>> {
        return mapNotNull {
            when (it) {
                is Filter.Header -> HeaderItem(it)
                is Filter.Separator -> SeparatorItem(it)
                is Filter.CheckBox -> CheckboxItem(it)
                is Filter.TriState -> TriStateItem(it)
                is Filter.Text -> TextItem(it)
                is Filter.Select<*> -> SelectItem(it)
                is Filter.Group<*> -> {
                    val group = GroupItem(it)
                    val subItems = it.state.mapNotNull {
                        when (it) {
                            is Filter.CheckBox -> CheckboxSectionItem(it)
                            is Filter.TriState -> TriStateSectionItem(it)
                            is Filter.Text -> TextSectionItem(it)
                            is Filter.Select<*> -> SelectSectionItem(it)
                            else -> null
                        } as? ISectionable<*, *>
                    }
                    subItems.forEach { it.header = group }
                    group.subItems = subItems
                    group
                }
                is Filter.Sort -> {
                    val group = SortGroup(it)
                    val subItems = it.values.map {
                        SortItem(it, group)
                    }
                    group.subItems = subItems
                    group
                }
            }
        }
    }
}