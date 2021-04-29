package uk.co.jatra.gallery

import android.graphics.Bitmap
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import uk.co.jatra.gallery.databinding.ImageItemBinding

class PhotoViewHolder(
    private val binding: ViewBinding,
    private val activity: MainActivity
) :
    RecyclerView.ViewHolder(binding.root) {
    fun bindBitmap(bitmap: Bitmap) {
        (binding as ImageItemBinding).imageView.setImageBitmap(bitmap)
    }

    fun setCameraClickListener() {
        itemView.setOnClickListener { activity.getPermissionsAndTakePhoto() }
    }
}