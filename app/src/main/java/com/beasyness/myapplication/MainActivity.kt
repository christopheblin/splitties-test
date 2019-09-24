package com.beasyness.myapplication

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.Barrier
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.postDelayed
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import splitties.dimensions.dip
import splitties.resources.txt
import splitties.views.appcompat.configActionBar
import splitties.views.appcompat.showHomeAsUp
import splitties.views.dsl.appcompat.toolbar
import splitties.views.dsl.constraintlayout.*
import splitties.views.dsl.coordinatorlayout.appBarLParams
import splitties.views.dsl.coordinatorlayout.coordinatorLayout
import splitties.views.dsl.coordinatorlayout.defaultLParams
import splitties.views.dsl.core.*
import splitties.views.dsl.material.appBarLayout
import splitties.views.dsl.material.contentScrollingWithAppBarLParams
import splitties.views.gravityCenter
import splitties.views.onClick
import splitties.views.textAppearance
import splitties.views.textResource


class MainActivity : AppCompatActivity() {
    private lateinit var ui: MainActivityUi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ui = MainActivityUi(this)

        setContentView(ui)

        //simulate a call to business logic on a bg thread
        Handler().postDelayed(delayInMillis = 2000L) {
            ui.setData(Data(libName = getString(R.string.app_name), libAuthor = "CBN", libLicense = "Commercial"))
        }
    }

    fun doAction() {
        ui.setOtherData(OtherData(expiration = "Tomorrow"))
    }
}

data class Data(val libName: String, val libAuthor: String, val libLicense: String)

class DataUi(override val ctx: Context) : Ui {
    fun setData(d: Data) {
        this.libNameTv.text = d.libName
        this.authorTv.text = d.libAuthor
        this.licenseTv.text = d.libLicense
    }

    private val licenseTv = textView { text = "..."  }
    private val authorTv = textView { text = "..." }
    private val libNameTv = textView { text = "..." }

    override val root = constraintLayout {
        val libNameLabel = label(R.string.library_name)
        val authorLabel = label(R.string.author)
        val licenseLabel = label(R.string.license)
        val labelsBarrier = endBarrier(libNameLabel, authorLabel, licenseLabel)
        addLabelAndTv(labelsBarrier, libNameLabel, libNameTv) { topOfParent() }
        addLabelAndTv(labelsBarrier, authorLabel, authorTv) { topToBottomOf(libNameTv) }
        addLabelAndTv(labelsBarrier, licenseLabel, licenseTv) { topToBottomOf(authorTv) }
    }

    private fun label(@StringRes txtResId: Int) = textView {
        textAppearance = R.style.TextAppearance_MaterialComponents_Body2
        text = buildSpannedString { bold { append(txt(txtResId)) } }
    }

    private inline fun ConstraintLayout.addLabelAndTv(
        labelBarrier: Barrier,
        label: View,
        tv: View,
        addLabelConstraints: ConstraintLayout.LayoutParams.() -> Unit
    ) {
        add(label, lParams(wrapContent, wrapContent) {
            startOfParent(); addLabelConstraints()
        })
        add(tv, lParams(wrapContent, wrapContent) {
            startToEndOf(labelBarrier, margin = dip(8)); alignVerticallyOn(label)
        })
    }
}

data class OtherData(val expiration: String)

interface OtherDataListener {
    fun doAction()
}

class OtherDataUi(override val ctx: Context, private val listener: OtherDataListener?) : Ui {
    fun setOtherData(d: OtherData) {
        expirationTv.text = d.expiration
    }

    private val expirationTv = textView {
        textAppearance = R.style.TextAppearance_MaterialComponents_Body2
        text = "..."
    }

    private val btn = button {
        textResource = R.string.actionBtn
        onClick { listener?.doAction() }
    }

    override val root = verticalLayout {
        add(btn, lParams(wrapContent, wrapContent) { gravity = gravityCenter })
        add(expirationTv, lParams(wrapContent, wrapContent))
    }
}

open class MainActivityUi(final override val ctx: Context) : Ui {
    fun setData(d: Data) {
        this.dataUi.setData(d)
    }

    fun setOtherData(d: OtherData) {
        this.otherDataUi.setOtherData(d)
    }

    private val dataUi = DataUi(ctx)

    private val otherDataUi = OtherDataUi(ctx, object : OtherDataListener {
        override fun doAction() {
            (ctx as? MainActivity)?.doAction()
        }
    })

    private val mainContent = constraintLayout {
        add(dataUi.root, lParams(matchParent, wrapContent) { topOfParent() })

        add(otherDataUi.root, lParams(matchParent, wrapContent) {
            topToBottomOf(dataUi.root, dip(8))
        })
    }

    override val root = coordinatorLayout {
        fitsSystemWindows = true
        addDefaultAppBar(ctx)
        add(mainContent, contentScrollingWithAppBarLParams {
            margin = dip(16)
        })
    }
}

class PreviewMainActivityUi(ctx: Context) : MainActivityUi(ctx) {
    init {
        when (1) {
            1 -> setData(Data(libName = "My app", libAuthor = "CBN", libLicense = "Commercial"))
            2 -> setData(Data(libName = "My app 2", libAuthor = "PGD", libLicense = "Open source"))
        }
    }
}

fun CoordinatorLayout.addDefaultAppBar(ctx: Context) {
    add(appBarLayout(theme = R.style.AppTheme_AppBarOverlay) {
        add(toolbar {
            popupTheme = R.style.AppTheme_PopupOverlay
            val activity = ctx as? AppCompatActivity ?: return@toolbar
            activity.setSupportActionBar(this)
            activity.configActionBar { showHomeAsUp = true }
        }, defaultLParams())
    }, appBarLParams())
}