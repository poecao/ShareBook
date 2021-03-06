package com.angcyo.sharebook.iview.book

import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.angcyo.sharebook.R
import com.angcyo.sharebook.http.Action
import com.angcyo.sharebook.http.BSub
import com.angcyo.sharebook.http.P
import com.angcyo.sharebook.http.RxBook
import com.angcyo.sharebook.http.bean.BookDetailBean
import com.angcyo.sharebook.http.bean.HomeBean
import com.angcyo.sharebook.http.service.Api
import com.angcyo.sharebook.http.service.Book
import com.angcyo.sharebook.iview.base.BaseRecyclerUIView
import com.angcyo.uiview.kotlin.v
import com.angcyo.uiview.model.TitleBarPattern
import com.angcyo.uiview.net.RRetrofit
import com.angcyo.uiview.recycler.RBaseViewHolder
import com.angcyo.uiview.recycler.adapter.RExBaseAdapter
import com.angcyo.uiview.utils.T_
import com.angcyo.uiview.widget.TagsTextView
import com.lzy.imagepicker.GlideImageLoader

/**
 * 书本详情
 * Created by angcyo on 2017-06-03.
 */
class BookDetailUIView(var isbn: String) : BaseRecyclerUIView<String, BookDetailBean, String>() {

    lateinit var bookDetailBean: BookDetailBean

    override fun getTitleBar(): TitleBarPattern {
        return super.getTitleBar().setTitleString("")
    }

    override fun createAdapter(): RExBaseAdapter<String, BookDetailBean, String> {
        return object : RExBaseAdapter<String, BookDetailBean, String>(mActivity) {
            override fun getItemLayoutId(viewType: Int): Int {
                return R.layout.item_book_detail
            }

            override fun onBindDataView(holder: RBaseViewHolder, posInData: Int, dataBean: BookDetailBean) {
                if (!dataBean.pic.startsWith("http")) {
                    dataBean.pic = HomeBean.BASE_IMG_PATH + dataBean.pic
                }

                GlideImageLoader.displayImage(holder.imgV(R.id.image_view),
                        dataBean.pic,
                        R.drawable.default_image)

                holder.tv(R.id.title_view).text = dataBean.title
                holder.tv(R.id.author_view).text = dataBean.author
                holder.tv(R.id.publisher_view).text = dataBean.publisher
                holder.tv(R.id.pubdate_view).text = dataBean.pubdate

                holder.tv(R.id.author_intro_view).text =
                        if (dataBean.author_intro.isEmpty())
                            "暂无作者信息"
                        else
                            dataBean.author_intro

                holder.tv(R.id.summary_view).text = dataBean.summary

                val tagsTextView: TagsTextView = holder.v(R.id.tags_view)
                tagsTextView.setTags(dataBean.tags)
            }
        }
    }

    override fun initOnShowContentLayout() {
        super.initOnShowContentLayout()
        mViewHolder.click(R.id.borrow_book_view) {

        }
        mViewHolder.click(R.id.join_book_view) {
            showLoadView()
            add(RRetrofit.create(Api::class.java)
                    .api(P.b(Action.ADD_CART, "isbn:" + isbn))
                    .compose(RxBook.transformer(String::class.java))
                    .subscribe(object : BSub<String>() {
                        override fun onSucceed(bean: String) {
                            super.onSucceed(bean)
                            T_.ok(bean)
                        }

                        override fun onEnd(isError: Boolean, isNoNetwork: Boolean, e: Throwable?) {
                            super.onEnd(isError, isNoNetwork, e)
                            hideLoadView()
                        }
                    }))
        }
    }

    override fun createRecyclerRootView(baseContentLayout: RelativeLayout, inflater: LayoutInflater) {
        inflate(R.layout.view_book_detail)
        mRecyclerView = baseContentLayout.v(R.id.recycler_view)
        mRefreshLayout = baseContentLayout.v(R.id.refresh_view)

        initRecyclerView(mRecyclerView, null)
        initRefreshLayout(mRefreshLayout, null)
    }

    override fun onUILoadData() {
        super.onUILoadData()
        add(RRetrofit.create(Book::class.java)
                .searchBook(P.b(Action.SEARCH_BOOK, "isbn:" + isbn))
                .compose(RxBook.transformerList(BookDetailBean::class.java))
                .subscribe(object : BSub<List<BookDetailBean>>() {
                    override fun onSucceed(bean: List<BookDetailBean>) {
                        super.onSucceed(bean)
                        if (bean.isEmpty()) {
                            onUILoadFinish(true)
                        } else {
                            onUILoadFinish()
                            bookDetailBean = bean.first()
                            titleString = bookDetailBean.title
                            mExBaseAdapter.resetAllData(bean)

                            initFavView()
                        }
                    }

                    override fun onEnd(isError: Boolean, isNoNetwork: Boolean, e: Throwable?) {
                        super.onEnd(isError, isNoNetwork, e)
                        if (isError) {
                            showNonetLayout { loadData() }
                        }
                    }
                }))
    }

    private fun initFavView() {
        if (bookDetailBean.favorite == 1) {
            mViewHolder.tv(R.id.fav_book_view).text = "取消收藏"
            mViewHolder.click(R.id.fav_book_view) {
                showLoadView()
                add(RRetrofit.create(Book::class.java)
                        .favBook(P.b(Action.UNFAV_BOOK, "isbn:" + isbn))
                        .compose(RxBook.transformer(String::class.java))
                        .subscribe(object : BSub<String>() {
                            override fun onSucceed(bean: String) {
                                super.onSucceed(bean)
                                T_.ok(bean)
                                bookDetailBean.favorite = 0
                                initFavView()
                            }

                            override fun onEnd(isError: Boolean, isNoNetwork: Boolean, e: Throwable?) {
                                super.onEnd(isError, isNoNetwork, e)
                                hideLoadView()
                            }
                        }))
            }
        } else {
            mViewHolder.tv(R.id.fav_book_view).text = "收藏"
            mViewHolder.click(R.id.fav_book_view) {
                showLoadView()
                add(RRetrofit.create(Book::class.java)
                        .favBook(P.b(Action.FAV_BOOK, "isbn:" + isbn))
                        .compose(RxBook.transformer(String::class.java))
                        .subscribe(object : BSub<String>() {
                            override fun onSucceed(bean: String) {
                                super.onSucceed(bean)
                                T_.ok(bean)
                                bookDetailBean.favorite = 1
                                initFavView()
                            }

                            override fun onEnd(isError: Boolean, isNoNetwork: Boolean, e: Throwable?) {
                                super.onEnd(isError, isNoNetwork, e)
                                hideLoadView()
                            }
                        }))
            }
        }
    }
}
