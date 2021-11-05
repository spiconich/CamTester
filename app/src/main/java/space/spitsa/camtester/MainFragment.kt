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
import android.widget.*
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import io.realm.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import androidx.recyclerview.widget.RecyclerView
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter
import io.realm.kotlin.where
import androidx.cardview.widget.CardView

import android.widget.TextView
import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import io.realm.RealmResults

interface MyInterface {
    fun onClick(item: PhotoUri)

}

class MainFragment : MyInterface, Fragment() {
    lateinit var fragmentView: View
    lateinit var mContext: Context
    lateinit var uiThreadRealm: Realm
    private var photosCount: Long = 0
    private var REQUEST_IMAGE_CAPTURE =
        1 // TODO:непонятно, есть ли тут косяк с проебанной памятью
    private lateinit var lastPhotoFileUri: Uri

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentView = inflater.inflate(R.layout.fragment_main, container, false)
        val realmName: String = "Photos"
        val config = RealmConfiguration.Builder().allowWritesOnUiThread(true)
            .name(realmName)
            .build()
        uiThreadRealm = Realm.getInstance(config)
        val photoButton = fragmentView.findViewById<Button>(R.id.cam_button)
        val recyclerView: RecyclerView = fragmentView.findViewById<RecyclerView>(R.id.recyclerView)

        val adapter =
            ExampleRecyclerViewAdapter(uiThreadRealm.where(PhotoUri::class.java).findAll(), this)
        recyclerView.layoutManager = LinearLayoutManager(requireActivity()!!.applicationContext)
        recyclerView.adapter = adapter

        photoButton.setOnClickListener {
            val photoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val manager = mContext!!.packageManager
            if (photoIntent.resolveActivity(manager) != null) {
                val lastPhotoFile = getPhotoFile()
                lastPhotoFileUri = FileProvider.getUriForFile(
                    mContext,
                    "space.spitsa.camtester.fileprovider",
                    lastPhotoFile
                )
                photoIntent.putExtra(MediaStore.EXTRA_OUTPUT, lastPhotoFileUri)
                //startActivity(photoIntent)
                startActivityForResult(photoIntent, REQUEST_IMAGE_CAPTURE)
            } else
                Toast.makeText(mContext, "Активити для камеры не найдено", Toast.LENGTH_SHORT)
        }
        return fragmentView
    }

    private fun getPhotoFile(): File {
        //т.к. getExternalFilesDir - сохраняет в папку, которую видит только приложение, галерею не заабузить
        val directoryStorage = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
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
            MediaStore.Images.Media.getBitmap(mContext.contentResolver, lastPhotoFileUri)

        }
    }

    override fun onDetach() {
        //TODO:как удалять ссылки на неизменяемые файлы
        super.onDetach()
        uiThreadRealm.close()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onClick(item: PhotoUri) {
        Log.d("interface", "onClick interface method")
        val bundle = Bundle()
        bundle.putString("BUNDLE KEY", item.uri)
        Log.d("onClick", item.uri)
        val manager = (mContext as FragmentActivity).supportFragmentManager
        val transaction = manager.beginTransaction()
        val photoFragment = PhotoFragment()
        photoFragment.arguments = bundle
        transaction.replace(R.id.main_fragment_view, photoFragment)
        transaction.addToBackStack(photoFragment::class.java.name);
        transaction.commit()

    }

    internal class ExampleRecyclerViewAdapter(
        data: OrderedRealmCollection<PhotoUri?>?,
        private val listener: MyInterface
    ) :
        RealmRecyclerViewAdapter<PhotoUri?,
                ExampleRecyclerViewAdapter.MyViewHolder?>(data, true) {
        var TAG = "REALM_RECYCLER_ADAPTER"
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val itemView =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_view, parent, false)
            return MyViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val obj = getItem(position)
            Log.i(TAG, "Binding view holder: ${obj!!.uri}")
            holder.data = obj
            val name = obj.uri
            val index = name.lastIndexOf('/')
            holder.item_TextView?.text = name.substring(index + 1)
            holder.item_Photo?.setImageURI(Uri.parse(obj.uri))
            holder.itemView.setOnClickListener {

                listener.onClick(obj)

            }

        }


        class MyViewHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
            var item_TextView: TextView? = null
            var item_Photo: ImageView? = null
            var data: PhotoUri? = null

            init {
                item_TextView = itemView.findViewById(R.id.item_name)
                item_Photo = itemView.findViewById(R.id.item_bitmap)
            }
        }

        init {
            Log.i(
                TAG,
                "Created RealmRecyclerViewAdapter for ${getData()!!.size} items."
            )
        }
    }
}