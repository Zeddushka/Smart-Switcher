package ru.kartashovaa.idledisabler.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ru.kartashovaa.idledisabler.util.Log
import ru.kartashovaa.toolkit.Repositories

/**
 * Created by Kartashov A.A. on 2/20/18.
 * Receiver, that enables our enabled services at device startup
 */
class BootCompleteReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.i("Boot received", TAG)
            Repositories.device.startEnabledServices(TAG)
        }
    }

    companion object {
        private const val TAG = "BootCompleteReceiver"
    }

}