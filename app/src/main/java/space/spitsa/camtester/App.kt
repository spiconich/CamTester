package space.spitsa.camtester

import android.app.Application
import io.realm.Realm
import io.realm.RealmObject

/**
 * Ниже ебану стиль кода, которого бы желательно придерживаться
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        Realm.init(this)
    }
}

/**
 * Тут надо вынести класс в отдельный файл и ебануть PrimaryKey - первичный ключ
 */
open class PhotoUri(): RealmObject() {

    var uri: String = ""
}