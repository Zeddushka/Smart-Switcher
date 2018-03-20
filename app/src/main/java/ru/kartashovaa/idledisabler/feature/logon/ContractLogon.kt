package ru.kartashovaa.idledisabler.feature.logon

import ru.kartashovaa.idledisabler.R

/**
 * Created by Kartashov A.A. on 2/27/18.
 * Logon contract definition.
 * Logon feature holds permission granting
 */
interface ContractLogon {

    enum class LogoType(val drawableResource: Int) {
        RED(R.drawable.ic_battery_red),
        GREEN(R.drawable.ic_battery)
    }

    companion object {
        const val PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1
    }

    interface View: ru.kartashovaa.toolkit.View {
        fun startSettingsActivity()
        fun askUserPermissions()
        fun showPermissionNotGrantedDialog()
        fun setLogoType(logoType: LogoType)
        fun exitApplication()
        fun openAndroidApplicationSettings()
    }

    interface Presenter: ru.kartashovaa.toolkit.Presenter<View> {
        fun onLogoClick()
        fun onPermissionResult(permissions: Array<out String>, grantResults: IntArray)
        fun onDialogExitClick()
        fun onDialogGrantClick()
    }

    interface Interactor: ru.kartashovaa.toolkit.Interactor<Presenter> {
        val hasPermission: Boolean
    }
}