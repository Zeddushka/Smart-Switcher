package ru.kartashovaa.idledisabler.feature.logon

import android.Manifest
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_logon.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.startActivity
import ru.kartashovaa.idledisabler.R
import ru.kartashovaa.idledisabler.feature.logon.ContractLogon.Companion.PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION
import ru.kartashovaa.idledisabler.feature.settings.ViewSettings
import ru.kartashovaa.idledisabler.util.Log
import ru.kartashovaa.toolkit.BaseActivity
import android.content.Intent
import android.net.Uri
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator
import ru.kartashovaa.idledisabler.BuildConfig
import kotlin.math.pow


/**
 * Created by Kartashov A.A. on 2/27/18.
 */
class ViewLogon: BaseActivity<ContractLogon.View, ContractLogon.Presenter>(), ContractLogon.View {

    override val presenterClass = PresenterLogon::class.java

    private var dialogNotGranted: AlertDialog? = null
    private val androidSettingsIntent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        addCategory(Intent.CATEGORY_DEFAULT)
        data = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logon)
        imageLogo.onClick { presenter.onLogoClick() }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION) {
            presenter.onPermissionResult(permissions, grantResults)
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onStop() {
        dialogNotGranted?.dismiss()
        stopLogoAnimation()
        super.onStop()
    }

    override fun startSettingsActivity() = startActivity<ViewSettings>()

    override fun askUserPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION)
        }
    }

    override fun showPermissionNotGrantedDialog() {
        dialogNotGranted = AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_permission_needed_title))
                .setMessage(getString(R.string.dialog_permission_needed_message, getString(R.string.app_name)))
                .setCancelable(false)
                .setNegativeButton(R.string.dialog_permission_exit) {
                    dialog, _ ->
                    dialog.dismiss()
                    presenter.onDialogExitClick()
                }
                .setPositiveButton(R.string.dialog_permission_grant) {
                    dialog, _ ->
                    dialog.dismiss()
                    presenter.onDialogGrantClick()
                }
                .setOnDismissListener { dialogNotGranted = null }
                .create()
                .apply { show() }
    }

    override fun exitApplication() = finish()

    override fun openAndroidApplicationSettings() = startActivity(androidSettingsIntent)

    override fun setLogoType(logoType: ContractLogon.LogoType) {
        Log.d("Logo type is set", TAG)
        imageLogo.setImageResource(logoType.drawableResource)
        when (logoType) {
            ContractLogon.LogoType.RED -> stopLogoAnimation()
            ContractLogon.LogoType.GREEN -> startLogoAnimation()
        }
    }

    private fun startLogoAnimation() {
        val animation = AnimationUtils.loadAnimation(this, R.anim.heartbeat)
        animation.interpolator = Interpolator { t -> t.pow(4) }
        imageLogo.startAnimation(animation)
    }

    private fun stopLogoAnimation() {
        imageLogo.clearAnimation()
    }

    companion object {
        private const val TAG = "ViewLogon"
    }
}