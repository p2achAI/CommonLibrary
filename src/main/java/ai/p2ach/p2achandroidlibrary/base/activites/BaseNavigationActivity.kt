package ai.p2ach.p2achandroidlibrary.base.activites



import androidx.viewbinding.ViewBinding
/*
*Navigation으로 Fragment 이동할 수 있는 Activity
* */

abstract class BaseNavigationActivity<VB : ViewBinding> : BaseActivity<VB>() {

    protected open var navHostViewId : Int =-1
}