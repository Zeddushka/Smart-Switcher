package ru.kartashovaa.idledisabler.feature.logon

import android.content.pm.PackageManager
import ru.kartashovaa.idledisabler.util.Log
import ru.kartashovaa.toolkit.BasePresenter

/**
 * Created by Kartashov A.A. on 2/27/18.
 */
class PresenterLogon : BasePresenter<ContractLogon.View, ContractLogon.Interactor>(), ContractLogon.Presenter {
    override val interactorClass = InteractorLogon::class.java

    override fun onViewAttached() {
        view?.run {
            if (!interactor.hasPermission) {
                setLogoType(ContractLogon.LogoType.RED)
                askUserPermissions()
            } else {
                setLogoType(ContractLogon.LogoType.GREEN)
            }
        }
    }

    override fun onLogoClick() {
        view?.run {
            if (interactor.hasPermission) {
                startSettingsActivity()
            } else {
                showPermissionNotGrantedDialog()
            }
        }
    }

    override fun onDialogExitClick() {
        view?.exitApplication()
    }

    override fun onDialogGrantClick() {
        view?.openAndroidApplicationSettings()
    }

    override fun onPermissionResult(permissions: Array<out String>, grantResults: IntArray) {
        view?.run {
            val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            Log.i("Permission ${if (granted) "granted" else "denied"}")
            if (granted) {
                setLogoType(ContractLogon.LogoType.GREEN)
            } else {
                showPermissionNotGrantedDialog()
            }
        }
    }
}