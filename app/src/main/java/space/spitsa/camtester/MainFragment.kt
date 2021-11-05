package space.spitsa.camtester

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmRecyclerViewAdapter
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

interface MyInterface {

    fun onClick(item: PhotoUri)
}

class MainFragment : MyInterface, Fragment() {

    /**
     * Тут можно не сохранять ссылку на вью фрагмента,
     * а просто взять у фрагмента свойство view
     */
//    lateinit var fragmentView: View

    /**
     * Не сохраняем контекст, по дефолту доступен
     *
     * Вообще не желательно сохранять контекст в статику(object или companion object)
     *
     * Но если все таки сохраняем контекст/activity/view/fragment в статику,
     * то либо юзаем WeakReference(потом расскажу или сам почитаешь про типы ссылки)
     * либо контролируем ссылку по жизненному циклу контекста(зависит от activity/view/fragment)
     * mContext = null
     */
//    lateinit var mContext: Context

    lateinit var uiThreadRealm: Realm
    private var photosCount: Long = 0
    private var REQUEST_IMAGE_CAPTURE = 1


    /**
     * нет
     */
    // TODO:непонятно, есть ли тут косяк с проебанной памятью
    private lateinit var lastPhotoFileUri: Uri

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val realmName: String = "Photos"
        val config = RealmConfiguration.Builder().allowWritesOnUiThread(true)
            .name(realmName)
            .build()
        uiThreadRealm = Realm.getInstance(config)

        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val photoButton = view.findViewById<Button>(R.id.cam_button)
        val recyclerView: RecyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

        /**
         * В котлине можно именовать проперти, которые передаешь в методы/конструкторы
         *
         * Таким образом когда читаешь код как обычный текст в блокноте сразу становится
         * понятно какие свойства/поля передаем и для чего
         */
        val adapter = ExampleRecyclerViewAdapter(
            data = uiThreadRealm.where(PhotoUri::class.java).findAll(),
            listener = this
        )
        recyclerView.layoutManager = LinearLayoutManager(requireActivity().applicationContext)
        recyclerView.adapter = adapter

        photoButton.setOnClickListener {

            val photoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val manager = requireContext().packageManager

            /**
             * Если открыли скобки в if, то открываем и в else
             *
             * + отступы, чтобы код не сливался в одну кашу, так проще читать
             */
            if (photoIntent.resolveActivity(manager) != null) {

                val lastPhotoFile = getPhotoFile()

                /**
                 * FileProvider - это круто, лайк
                 */
                lastPhotoFileUri = FileProvider.getUriForFile(
                    requireContext(),
                    "space.spitsa.camtester.fileprovider",
                    lastPhotoFile
                )
                photoIntent.putExtra(MediaStore.EXTRA_OUTPUT, lastPhotoFileUri)
                //startActivity(photoIntent)
                startActivityForResult(photoIntent, REQUEST_IMAGE_CAPTURE)
            } else {

                /**
                 * Тут забыл вызвать show() у Toast
                 */
                Toast.makeText(requireContext(), "Активити для камеры не найдено", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getPhotoFile(): File {
        //т.к. getExternalFilesDir - сохраняет в папку, которую видит только приложение, галерею не заабузить
        val directoryStorage = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val timeStamp: String = SimpleDateFormat("MM_dd_HHmmss").format(Date())
        Log.d("filecreate", timeStamp)
        return File.createTempFile(timeStamp, ".jpg", directoryStorage)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            val photoUri: PhotoUri = PhotoUri()
            photoUri.uri = lastPhotoFileUri.toString()
            uiThreadRealm.executeTransaction { transactionRealm ->
                transactionRealm.insert(photoUri)
            }
            MediaStore.Images.Media.getBitmap(requireContext().contentResolver, lastPhotoFileUri)
        }
    }

    override fun onDetach() {
        //TODO:как удалять ссылки на неизменяемые файлы
        super.onDetach()

        uiThreadRealm.close()
    }

    /**
     * Выпилили ссылку на контекст
     */
//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        mContext = context
//    }

    override fun onClick(item: PhotoUri) {

        Log.d("interface", "onClick interface method")

        val bundle = Bundle()
        bundle.putString("BUNDLE KEY", item.uri)

        /**
         * Можно так :)
         */
//        val bundle = Bundle().apply { putString("BUNDLE KEY", item.uri) }

        Log.d("onClick", item.uri)

        val photoFragment = PhotoFragment()
        photoFragment.arguments = bundle

        val manager = (requireContext() as FragmentActivity).supportFragmentManager
        val transaction = manager.beginTransaction()
        transaction.replace(R.id.main_fragment_view, photoFragment)
        transaction.addToBackStack(photoFragment::class.java.name);
        transaction.commit()
    }

    /**
     * Как уже говорил, надо вынести в отдельный файл
     *
     * И прикручиваем стиль кода
     */
    internal class ExampleRecyclerViewAdapter(

        data: OrderedRealmCollection<PhotoUri?>?,

        private val listener: MyInterface

    ) : RealmRecyclerViewAdapter<PhotoUri?, ExampleRecyclerViewAdapter.MyViewHolder?>(data, true) {

        var TAG = "REALM_RECYCLER_ADAPTER"

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_view, parent, false)
            return MyViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

            val obj = getItem(position)
            Log.i(TAG, "Binding view holder: ${obj!!.uri}")

            holder.data = obj

            val name = obj.uri
            val index = name.lastIndexOf('/')

            holder.itemTextView.text = name.substring(index + 1)
            holder.itemPhoto.setImageURI(Uri.parse(obj.uri))
            holder.itemView.setOnClickListener { listener.onClick(obj) }
        }


        class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            /**
             * Никаких _ (snake case) в названиях, придерживаемся camel case
             */
            var itemTextView: TextView = itemView.findViewById(R.id.item_name)
            var itemPhoto: ImageView = itemView.findViewById(R.id.item_bitmap)
            var data: PhotoUri? = null
        }

        init {

            Log.i(
                TAG,
                "Created RealmRecyclerViewAdapter for ${getData()!!.size} items."
            )
        }
    }
}