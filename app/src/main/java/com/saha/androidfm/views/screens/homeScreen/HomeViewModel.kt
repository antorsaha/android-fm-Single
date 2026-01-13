package com.saha.androidfm.views.screens.homeScreen

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntRect
import androidx.lifecycle.AndroidViewModel
import com.saha.androidfm.data.repo.Repo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

private const val TAG = "HomeViewModel"

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val apiRepo: Repo,
) : AndroidViewModel(application) {
    //var text by mutableStateOf("")
    var queryText by mutableStateOf("")
    var showContextMenu by mutableStateOf(false)
    var contextMenuBounds by mutableStateOf(IntRect.Zero)

    var errorMessage: String = ""
    var showErrorDialog by mutableStateOf(false)

    // State for downloaded images from API (stored securely in internal storage)
    var downloadedImageUris by mutableStateOf<List<Uri>>(emptyList())


    //UI action management
    /*val onContextMenuClick = object : StickerContextMenuClickAction {
        override fun onDismiss() {
            showContextMenu = false
        }

        override fun onDelete() {
            showContextMenu = false
        }

        override fun onMarkAsFavorite() {
            showContextMenu = false
        }

        override fun onExportWhatsApp() {
            showContextMenu = false


        }

        override fun onSaveToLibrary() {
            showContextMenu = false
        }

        override fun onCopyPrompt() {
            showContextMenu = false
        }
    }*/


}