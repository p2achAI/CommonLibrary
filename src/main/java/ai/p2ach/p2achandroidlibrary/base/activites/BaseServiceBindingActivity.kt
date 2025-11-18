import ai.p2ach.p2achandroidlibrary.base.activites.BaseNavigationActivity
import ai.p2ach.p2achandroidlibrary.base.service.ServiceBoundListener
import ai.p2ach.p2achandroidlibrary.base.service.ServiceGetter
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.viewbinding.ViewBinding

abstract class BaseServiceBindingActivity<VB : ViewBinding, S : Service> :
    BaseNavigationActivity<VB>() {

    protected var boundService: S? = null
        private set

    private var bound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder) {
            @Suppress("UNCHECKED_CAST")
            val getter = binder as ServiceGetter<S>
            val service = getter.getService()
            boundService = service
            bound = true
            dispatchServiceBound(service)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bound = false
            boundService = null
            dispatchServiceUnbound()
        }
    }

    override fun onStart() {
        super.onStart()
        bindServiceInternal()
    }

    override fun onStop() {
        super.onStop()
        if (bound) {
            unbindService(connection)
            bound = false
        }
    }

    protected abstract fun createBindIntent(): Intent

    private fun bindServiceInternal() {
        val intent = createBindIntent()
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun dispatchServiceBound(service: S) {
        val current = findCurrentNavFragment()
        if (current is ServiceBoundListener<*>) {
            @Suppress("UNCHECKED_CAST")
            (current as ServiceBoundListener<S>).onServiceBound(service)
        }
    }

    private fun dispatchServiceUnbound() {
        val current = findCurrentNavFragment()
        if (current is ServiceBoundListener<*>) {
            @Suppress("UNCHECKED_CAST")
            (current as ServiceBoundListener<S>).onServiceUnbound()
        }
    }

    private fun findCurrentNavFragment(): Fragment? {
        val primary = supportFragmentManager.primaryNavigationFragment
        val navHost = primary ?: supportFragmentManager.fragments
            .firstOrNull { it is NavHostFragment }

        val childManager = (navHost as? NavHostFragment)?.childFragmentManager ?: return null
        val childPrimary = childManager.primaryNavigationFragment
        if (childPrimary != null) return childPrimary

        return childManager.fragments.firstOrNull { it.isVisible }
    }
}