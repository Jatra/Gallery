package uk.co.jatra.gallery

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.recyclerview.widget.GridLayoutManager
import uk.co.jatra.gallery.databinding.ActivityMainBinding
import java.io.*
import java.util.*


private val TAG = MainActivity::class.java.simpleName
const val CAMERA = 0
const val GALLERY_IMAGE_VIEW_TYPE = 1
const val BLANK_IMAGE_VIEW_TYPE = 2

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<MainViewModel>()
    private lateinit var binding: ActivityMainBinding
    private lateinit var photosAdapter: PhotosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        photosAdapter = PhotosAdapter(this, viewModel)
        binding.recyclerView.adapter = photosAdapter


        val currentPermissions = checkSelfPermission(this, READ_EXTERNAL_STORAGE)
        if (currentPermissions == PackageManager.PERMISSION_GRANTED) {
            viewModel.loadPhotos()
        } else {
            requestPermissionForLoadPhotosLauncher.launch(READ_EXTERNAL_STORAGE)
        }

        viewModel.uris.observe(this, ::updateView)
    }

    fun updateView(uris: List<Uri>) {
        photosAdapter.update(uris)
    }


    private val requestPermissionForLoadPhotosLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(
            RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                viewModel.loadPhotos()
            } else {
                Toast.makeText(this, "NEED PERMS", Toast.LENGTH_LONG).show()
            }
        }

    private val requestPermissionForSavePhotosLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(
            RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                takePhoto()
            } else {
                Toast.makeText(this, "NEED PERMS", Toast.LENGTH_LONG).show()
            }
        }

    private val takePictureLauncher: ActivityResultLauncher<Uri> =
        registerForActivityResult(TakePicture(), fun(captured: Boolean) {
            if (captured) {
                Log.d(TAG, "image captured")
            } else {
                Toast.makeText(this, "camera failed", Toast.LENGTH_LONG).show()
            }
        })


    private fun takePhoto() {
        //How do we find the URI to use?
        getStorageUri(this, "myimage-${Date()}")?.let {
            takePictureLauncher.launch(it)
            viewModel.loadPhotos()
        } ?: Toast.makeText(this, "Camera failed", Toast.LENGTH_LONG).show()
    }


    fun getPermissionsAndTakePhoto() {
        val currentPermissions = checkSelfPermission(this, WRITE_EXTERNAL_STORAGE)
        if (currentPermissions == PackageManager.PERMISSION_GRANTED) {
            takePhoto()
        } else {
            requestPermissionForSavePhotosLauncher.launch(WRITE_EXTERNAL_STORAGE)
        }

    }

    //Pass the new file to the MediaScanner so it gets immediately added to the content provider and shows up in the gallery
    //Without this our new image won't show up in the gallery until the OS runs the MediaScanner at some point in the future
    fun addImageToContentProvider(file: File) {
        MediaScannerConnection.scanFile(this, arrayOf(file.absolutePath), null, null)
    }

    private fun galleryAddPic(uri: Uri) {
        Intent(ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            mediaScanIntent.data = uri
            sendBroadcast(mediaScanIntent)
        }
    }


    //From https://stackoverflow.com/questions/56904485/how-to-save-an-image-in-android-q-using-mediastore/56990305
    @Throws(IOException::class)
    fun getStorageUri(
        context: Context,
        displayName: String
    ): Uri? {

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
//            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
        }

        val resolver = context.contentResolver
        var uri: Uri? = null

        return try {
            uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: throw IOException("Failed to create new MediaStore record.")
            uri

        } catch (e: IOException) {

            uri?.let { orphanUri ->
                // Don't leave an orphan entry in the MediaStore
                resolver.delete(orphanUri, null, null)
            }
            return null
        }
    }
}