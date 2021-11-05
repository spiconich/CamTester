package space.spitsa.camtester

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

class PhotoFragment : Fragment() {
    lateinit var mContext: Context
    lateinit var photoView:View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val TAG="PhotoFragment"

        Log.d(TAG,"onCreateView started")
        // Inflate the layout for this fragment
        photoView = inflater.inflate(R.layout.fragment_photo, container, false)
        val bundle = this.arguments
        val uriString = bundle?.getString("BUNDLE KEY")
        if (uriString!=null){
            Log.d("PhotoFragment","uriString not empty")
            val image=photoView.findViewById<ImageView>(R.id.photo_image)
            val name=photoView.findViewById<TextView>(R.id.photo_name)
            val index = uriString.lastIndexOf('/')
            name.setText(uriString.substring(index + 1))
            image.setImageURI(Uri.parse(uriString))
        }
        else
            Log.d("PhotoFragment","uriString is empty")
        return photoView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext=context
    }
    override fun onDetach() {
        //TODO: че делать с сылками, которые могу привести к утечкам памяти
        super.onDetach()
    }
}