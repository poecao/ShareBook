package com.angcyo.sharebook.control

import com.angcyo.sharebook.App
import com.angcyo.sharebook.http.BSub
import com.angcyo.sharebook.http.RxBook
import com.angcyo.sharebook.http.bean.VersionBean
import com.angcyo.sharebook.http.service.Version
import com.angcyo.uiview.container.ILayout
import com.angcyo.uiview.dialog.UIDialog
import com.angcyo.uiview.dialog.UILoading
import com.angcyo.uiview.github.utilcode.utils.AppUtils
import com.angcyo.uiview.net.RRetrofit

/**
 * Created by angcyo on 2017-05-29.
 */
object VersionControl {
    var isChecking = false
    fun check(layout: ILayout<*>, inBack: Boolean = false) {
        if (isChecking) {
            return
        }
        isChecking = true
        RRetrofit.create(Version::class.java)
                .version(Version.url)
                .compose(RxBook.transformer(VersionBean::class.java))
                .subscribe(object : BSub<VersionBean>() {
                    override fun onSucceed(bean: VersionBean?) {
                        super.onSucceed(bean)
                        bean?.haveUpdate({
                            showUpdateDialog(layout, bean)
                        }) {
                            if (!inBack) {
                                showNoUpdateDialog(layout)
                            }
                        }
                    }

                    override fun onEnd(isError: Boolean, isNoNetwork: Boolean, e: Throwable?) {
                        super.onEnd(isError, isNoNetwork, e)
                        isChecking = false
                        UILoading.hide()
                    }
                })
    }

    fun showUpdateDialog(layout: ILayout<*>, version: VersionBean) {
        UpdateHelper.showUpdateDialog(layout, version.version_name, version.des, version.url, version.force ?: false)
    }

    fun showNoUpdateDialog(layout: ILayout<*>) {
        UIDialog.build()
                .setDialogTitle("暂无更新")
                .setDialogContent("当前${AppUtils.getAppVersionName(App.getApp())}已是最新版.")
                .showDialog(layout)
    }
}
