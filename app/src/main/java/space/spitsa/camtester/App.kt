package space.spitsa.camtester

import android.app.Application
import android.net.Uri
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

class App:Application() {
    override fun onCreate() {
        Realm.init(this)
        super.onCreate()
    }
}

open class PhotoUri(): RealmObject() {
    var uri: String = ""
}