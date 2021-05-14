package seng440.vaccinepassport.room

import android.app.Application

class VPassLiveRoomApplication : Application() {
    val database by lazy { VPassDatabase.getDatabase(this) }
    val repository by lazy { VPassRepository(database.vPassDao()) }
}