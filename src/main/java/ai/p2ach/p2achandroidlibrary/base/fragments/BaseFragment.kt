package ai.p2ach.p2achandroidlibrary.base.fragments

import ai.p2ach.p2achandroidlibrary.utils.Log
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    private var _binding: VB by autoCleared()
    protected val binding: VB
        get() = _binding

    protected open val enableUsbEvents: Boolean = false
    protected open val usbPermissionAction: String = "ai.p2ach.USB_PERMISSION"
    private var usbReceiver: BroadcastReceiver? = null
    // ----------------- Fragment 기본 -----------------

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val vbClass = (javaClass.genericSuperclass as ParameterizedType)
            .actualTypeArguments[0] as Class<VB>
        val inflate = vbClass.getMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.java
        )
        _binding = inflate.invoke(null, inflater, container, false) as VB
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewInit(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        registerUsbReceiverIfNeeded()

    }

    override fun onResume() {
        super.onResume()

        checkAlreadyConnectUsbCamera()


    }

    override fun onStop() {
        super.onStop()
        unregisterUsbReceiver()
    }

    open fun viewInit(savedInstanceState: Bundle?) {}

    protected inline fun autoBinding(block: VB.() -> Unit) {
        binding.block()
    }

    // ----------------- Navigation helper -----------------

    fun navigate(
        @IdRes destinationId: Int,
        args: Bundle? = null,
        navOptions: NavOptions? = null
    ) {
        findNavController().navigate(
            resId = destinationId,
            args = args,
            navOptions = navOptions
        )
    }

    fun navigate(deepLink: Uri) {
        findNavController().navigate(deepLink)
    }

    fun popBack() {
        findNavController().popBackStack()
    }

    fun popBackTo(
        @IdRes destinationId: Int,
        inclusive: Boolean = false
    ) {
        findNavController().popBackStack(destinationId, inclusive)
    }

    // ----------------- USB 이벤트 & 권한 -----------------

    private fun registerUsbReceiverIfNeeded() {
        if (!enableUsbEvents) return
        if (usbReceiver != null) return

        val context = requireContext()
        val filter = IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }

        usbReceiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                val device =
                    intent?.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE) ?: return

                when (intent.action) {
                    UsbManager.ACTION_USB_DEVICE_ATTACHED ->  if(device.isCameraDevice())onUsbCameraAttached(device)
                    UsbManager.ACTION_USB_DEVICE_DETACHED ->  if(device.isCameraDevice())onUsbCameraDetached(device)

                }
            }
        }

        ContextCompat.registerReceiver(
            context,
            usbReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private fun unregisterUsbReceiver() {
        val context = context ?: return
        usbReceiver?.let {
            try {
                context.unregisterReceiver(it)
            } catch (_: Exception) {
            }
        }
        usbReceiver = null
    }

     private fun onUsbCameraAttached(device: UsbDevice) {
         Log.d("onUsbCameraAttached ${device.deviceName}")
         requestUsbPermission(device,"onUsbCameraAttached"){
             g->
             if(g)onReadyUsbCamera(device)
             else Log.d("${device.deviceName} permission denined.")
         }

     }


    private fun onUsbCameraDetached(device: UsbDevice) {
        Log.d("onUsbCameraDetached ${device.deviceName}")
    }



    open fun onReadyUsbCamera(device: UsbDevice){
        Log.d("onReadyUsbCamera ${device.deviceName}")
    }


    fun checkAlreadyConnectUsbCamera(){

        val context = requireContext()
        val usbManager = context.getSystemService(android.content.Context.USB_SERVICE) as android.hardware.usb.UsbManager


         usbManager.deviceList.values.filter { it.isCameraDevice() }.forEach {
                device ->

            if(usbManager.hasPermission(device)){
                Log.d("usbManager has permission true")
                onReadyUsbCamera(device)
                return
            }

            requestUsbPermission(device,"checkAlreadyConnectUsbCamera"){
                    granted->
                if(granted)onReadyUsbCamera(device)
                else Log.d("${device.deviceName} permission denined.")
            }
        }
    }


    protected fun requestUsbPermission(
        device: UsbDevice,
        where:String,
        onResult: (Boolean) -> Unit
    ) {
        val context = requireContext()
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager

        val intent = Intent(usbPermissionAction)
        val permissionIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val filter = IntentFilter(usbPermissionAction)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, received: Intent?) {

                val usbManager = context.getSystemService(android.content.Context.USB_SERVICE) as android.hardware.usb.UsbManager
                val granted = usbManager.hasPermission(device)

                Log.d("granted where $where $granted")
                try {
                    context.unregisterReceiver(this)
                } catch (_: Exception) {
                }
                onResult(granted)
            }
        }

        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )

        usbManager.requestPermission(device, permissionIntent)
    }





    fun  UsbDevice.isCameraDevice(): Boolean {
        return this.deviceClass == UsbConstants.USB_CLASS_VIDEO ||
                this.interfaceCount > 0 && this.getInterface(0).interfaceClass == UsbConstants.USB_CLASS_VIDEO
    }


}

fun <T : Any> Fragment.autoCleared() = AutoClearedValue<T>(this)

class AutoClearedValue<T : Any>(fragment: Fragment) : ReadWriteProperty<Fragment, T> {
    private var _value: T? = null

    init {
        fragment.viewLifecycleOwnerLiveData.observe(fragment) { owner ->
            owner?.lifecycle?.addObserver(
                object : androidx.lifecycle.DefaultLifecycleObserver {
                    override fun onDestroy(owner: androidx.lifecycle.LifecycleOwner) {
                        _value = null
                    }
                }
            )
        }
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        return _value ?: error("Binding accessed outside of view lifecycle.")
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T) {
        _value = value
    }
}

