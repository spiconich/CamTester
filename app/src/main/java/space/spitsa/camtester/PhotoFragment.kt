package space.spitsa.camtester

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment

class PhotoFragment : Fragment() {

    /**
     * Контекст не надо сохранять, он доступен по дефолту у фрагмента/активити/вью
     */
//    lateinit var mContext: Context
    /**
     * Тут тоже не надо сохранять view фрагмента,
     * оно доступно после метода onViewCreated(включая этот метод)
     */
//    lateinit var photoView:View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val TAG = "PhotoFragment"
        Log.d(TAG,"onCreateView started")

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bundle = this.arguments
        val uriString = bundle?.getString("BUNDLE KEY")

        if (uriString != null){

            Log.d("PhotoFragment","uriString not empty")

            val image = view.findViewById<ImageView>(R.id.photo_image)
            val name = view.findViewById<TextView>(R.id.photo_name)
            val index = uriString.lastIndexOf('/')
            name.text = uriString.substring(index + 1)
            image.setImageURI(Uri.parse(uriString))
        }
        else {

            Log.d("PhotoFragment","uriString is empty")
        }
    }

    /**
     * Нет необходимости
     */
//    override fun onDetach() {
//        //TODO: че делать с сылками, которые могу привести к утечкам памяти
//        super.onDetach()
//    }
}