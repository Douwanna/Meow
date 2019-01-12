package top.rechinx.meow.ui.base

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.jaredrummler.cyanea.app.CyaneaAppCompatActivity
import toothpick.Toothpick
import toothpick.config.Module
import top.rechinx.meow.di.AppScope

abstract class BaseActivity: CyaneaAppCompatActivity() {

    @Suppress("LeakingThis")
    val scope = AppScope.subscope(this).also { scope ->
        getModule()?.let { scope.installModules(it) }
    }

    abstract fun getModule(): Module?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Toothpick.inject(this, scope)
        setContentView(getLayoutRes())
        setUpViews(savedInstanceState)
    }

    open fun setUpViews(savedInstanceState: Bundle?) {

    }

    abstract fun getLayoutRes() : Int

    override fun onDestroy() {
        super.onDestroy()
        Toothpick.closeScope(this)
    }
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T : ViewModel> BaseActivity.viewModel() = lazy {
    val factory = object : ViewModelProvider.Factory {
        override fun <R : ViewModel?> create(modelClass: Class<R>): R {
            return scope.getInstance(T::class.java) as R
        }
    }
    ViewModelProviders.of(this, factory).get(T::class.java)
}