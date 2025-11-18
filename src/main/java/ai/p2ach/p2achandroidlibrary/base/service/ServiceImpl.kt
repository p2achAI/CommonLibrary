package ai.p2ach.p2achandroidlibrary.base.service

import android.app.Service


/*Service쪽 LocalBinder*/
interface ServiceGetter<S : Service> {
   fun getService(): S
}


/*Fragment 구현*/
interface ServiceBoundListener<S : Service> {
   fun onServiceBound(service: S)
   fun onServiceUnbound()
}