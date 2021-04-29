package uk.co.jatra.gallery

import android.app.Application
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media
import android.provider.MediaStore.Images.Media.*
import android.provider.MediaStore.MediaColumns.DATE_ADDED
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import kotlinx.coroutines.launch

private val TAG = MainViewModel::class.java.simpleName

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _uris: MutableLiveData<List<Uri>> = MutableLiveData()
    val uris: LiveData<List<Uri>> = _uris


    fun loadPhotos() {
        viewModelScope.launch {
            _loadPhotos()
        }
    }

    private fun _loadPhotos() {
        val photoUris = mutableListOf<Uri>()

        val projection = arrayOf(
            _ID,
            DISPLAY_NAME,
            SIZE,
            ORIENTATION
        )
        val selection = ""
        val selectionArgs = emptyArray<String>()
        val sortOrder = "$DATE_ADDED DESC"

        getApplication<Application>().contentResolver.query(
            EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(_ID)
            val nameColumn =
                cursor.getColumnIndexOrThrow(DISPLAY_NAME)
            val sizeColumn = cursor.getColumnIndexOrThrow(SIZE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val contentUri: Uri = ContentUris.withAppendedId(EXTERNAL_CONTENT_URI, id)
                val photoDescription = "$id ${cursor.getString(nameColumn)}  ${cursor.getInt(sizeColumn)} $contentUri"
                Log.d(TAG, photoDescription)
                photoUris.add(contentUri)
            }
            _uris.postValue(photoUris)
        }
    }


}


