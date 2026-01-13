package com.saha.androidfm.utils.helpers

import android.content.Context
import android.content.Intent
import com.saha.androidfm.BuildConfig

/**
 * Utility object for exporting sticker packs to WhatsApp.
 * 
 * This object handles:
 * 1. Creating Intents to add sticker packs to WhatsApp
 * 2. Detecting which WhatsApp apps are installed (Consumer vs Business)
 * 3. Determining which app to target based on whitelist status
 * 4. Checking if packs are already added to WhatsApp
 * 
 * WhatsApp Integration Flow:
 * 1. Create Intent with pack identifier and authority
 * 2. Launch Intent to open WhatsApp
 * 3. WhatsApp queries our ContentProvider using the identifier
 * 4. ContentProvider returns pack metadata and images
 * 5. WhatsApp validates and adds the pack
 * 6. WhatsApp returns result to our app
 */
object StickerPackExporter {
    private const val TAG = "StickerPackExporter"

    // Intent extras that WhatsApp expects (DO NOT CHANGE - these are WhatsApp's constants)
    const val EXTRA_STICKER_PACK_ID = "sticker_pack_id"
    const val EXTRA_STICKER_PACK_AUTHORITY = "sticker_pack_authority"
    const val EXTRA_STICKER_PACK_NAME = "sticker_pack_name"
    const val ADD_PACK_REQUEST_CODE = 200

    /**
     * Creates an Intent to add a sticker pack to WhatsApp.
     * 
     * This Intent will launch WhatsApp and trigger the pack addition flow.
     * WhatsApp will then query our ContentProvider using the provided identifier
     * to get pack metadata and images.
     * 
     * @param identifier Unique identifier of the sticker pack (must match ContentProvider data)
     * @param stickerPackName Display name for the pack
     * @return Intent configured for WhatsApp sticker pack addition
     * 
     * How it works:
     * - Action: "com.whatsapp.intent.action.ENABLE_STICKER_PACK" (WhatsApp's action)
     * - Extra: Pack identifier (used by WhatsApp to query ContentProvider)
     * - Extra: Content provider authority (tells WhatsApp which provider to query)
     * - Extra: Pack name (shown to user in WhatsApp UI)
     */
    fun createAddStickerPackIntent(
        identifier: String,
        stickerPackName: String
    ): Intent {
        return Intent().apply {
            action = "com.whatsapp.intent.action.ENABLE_STICKER_PACK"
            putExtra(EXTRA_STICKER_PACK_ID, identifier)
            putExtra(EXTRA_STICKER_PACK_AUTHORITY, BuildConfig.CONTENT_PROVIDER_AUTHORITY)
            putExtra(EXTRA_STICKER_PACK_NAME, stickerPackName)
        }
    }

    /**
     * Determines which WhatsApp app to target based on installation and whitelist status.
     * 
     * WhatsApp has two versions:
     * - Consumer WhatsApp (com.whatsapp) - Regular WhatsApp
     * - Business WhatsApp (com.whatsapp.w4b) - WhatsApp Business
     * 
     * A pack can be whitelisted (already added) in one or both apps.
     * 
     * Logic:
     * - If pack is NOT whitelisted in Consumer: target Consumer
     * - If pack is NOT whitelisted in Business: target Business
     * - If pack is whitelisted in both: return null (already added)
     * - If neither app installed: return null (can't export)
     * - If both apps installed and pack not whitelisted: return null (show chooser)
     * 
     * @param context Application context
     * @param identifier Sticker pack identifier
     * @return Package name to target (com.whatsapp or com.whatsapp.w4b),
     *         or null to show chooser or if already added
     */
    fun getTargetWhatsAppPackage(
        context: Context,
        identifier: String
    ): String? {
        // Check which WhatsApp apps are installed
        val isConsumerInstalled =
            WhitelistCheck.isWhatsAppConsumerAppInstalled(context.packageManager)
        val isSmbInstalled = WhitelistCheck.isWhatsAppSmbAppInstalled(context.packageManager)

        // No WhatsApp installed - can't export
        if (!isConsumerInstalled && !isSmbInstalled) {
            return null
        }

        // Check whitelist status (whether pack is already added)
        val isWhitelistedInConsumer =
            WhitelistCheck.isStickerPackWhitelistedInWhatsAppConsumer(context, identifier)
        val isWhitelistedInSmb =
            WhitelistCheck.isStickerPackWhitelistedInWhatsAppSmb(context, identifier)

        return when {
            // Pack not whitelisted in either app - show chooser if both installed
            !isWhitelistedInConsumer && !isWhitelistedInSmb -> {
                null // Will show chooser dialog to let user pick which WhatsApp
            }

            // Pack not whitelisted in Consumer - target Consumer
            !isWhitelistedInConsumer -> WhitelistCheck.CONSUMER_WHATSAPP_PACKAGE_NAME
            
            // Pack not whitelisted in Business - target Business
            !isWhitelistedInSmb -> WhitelistCheck.SMB_WHATSAPP_PACKAGE_NAME
            
            // Pack already whitelisted in both apps - nothing to do
            else -> null
        }
    }

    /**
     * Checks if WhatsApp (Consumer or Business) is installed on the device.
     * 
     * @param context Application context
     * @return true if at least one WhatsApp app is installed
     */
    fun isWhatsAppInstalled(context: Context): Boolean {
        return WhitelistCheck.isWhatsAppConsumerAppInstalled(context.packageManager) ||
                WhitelistCheck.isWhatsAppSmbAppInstalled(context.packageManager)
    }

    /**
     * Checks if a sticker pack is already added (whitelisted) in WhatsApp.
     * 
     * When a pack is added to WhatsApp, it's "whitelisted" which means
     * WhatsApp has registered it and allows it to be used.
     * 
     * @param context Application context
     * @param identifier Sticker pack identifier
     * @return true if pack is whitelisted in any installed WhatsApp app
     */
    fun isStickerPackAdded(context: Context, identifier: String): Boolean {
        return WhitelistCheck.isWhitelisted(context, identifier)
    }
}