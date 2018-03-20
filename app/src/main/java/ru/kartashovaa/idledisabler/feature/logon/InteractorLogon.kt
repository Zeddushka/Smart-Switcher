package ru.kartashovaa.idledisabler.feature.logon

import ru.kartashovaa.toolkit.BaseInteractor
import ru.kartashovaa.toolkit.Repositories

/**
 * Created by Kartashov A.A. on 2/27/18.
 */
class InteractorLogon: BaseInteractor<ContractLogon.Presenter>(), ContractLogon.Interactor {
    override val presenterClass = PresenterLogon::class.java

    override val hasPermission: Boolean
        get() = Repositories.device.hasLocationPermission
}