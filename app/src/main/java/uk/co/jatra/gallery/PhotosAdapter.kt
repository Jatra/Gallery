package uk.co.jatra.gallery

import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import uk.co.jatra.gallery.databinding.GalleryCameraButtonBinding
import uk.co.jatra.gallery.databinding.ImageItemBinding
import java.io.InputStream

class PhotosAdapter(
    private val activity: MainActivity,
    val viewModel: MainViewModel,
) : RecyclerView.Adapter<PhotoViewHolder>() {

    var data: List<Uri> = emptyList()

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) CAMERA
        else GALLERY_IMAGE_VIEW_TYPE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = when (viewType) {
            CAMERA -> {
                GalleryCameraButtonBinding.inflate(LayoutInflater.from(activity), parent, false)
            }
            else -> {
                ImageItemBinding.inflate(LayoutInflater.from(activity), parent, false)
            }
        }
        return PhotoViewHolder(binding, activity)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        if (position > 0) {
            val stream: InputStream? = activity.contentResolver.openInputStream(data[position - 1])
            val bitmap = BitmapFactory.decodeStream(stream)
            bitmap?.let {
                holder.bindBitmap(it)
            }
        } else {
            holder.setCameraClickListener()
        }
    }

    override fun getItemCount(): Int {
        return data.size + 1
    }

    fun update(newUris: List<Uri>) {
        data = newUris
        notifyDataSetChanged()
    }
}