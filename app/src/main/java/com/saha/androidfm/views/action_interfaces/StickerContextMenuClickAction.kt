package com.saha.androidfm.views.action_interfaces

interface StickerContextMenuClickAction {
    fun onDismiss()
    fun onDelete ()
    fun onMarkAsFavorite()
    fun onExportWhatsApp()
    fun onSaveToLibrary()
    fun onCopyPrompt()
}