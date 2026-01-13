package com.saha.androidfm.utils.helpers

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Helper object for securely downloading images from URLs and storing them in private internal storage.
 * 
 * Security Features:
 * - Files are stored in app's internal storage (context.filesDir) which is:
 *   - Private to the app (not accessible by file managers)
 *   - Not accessible by other apps
 *   - Automatically deleted when app is uninstalled
 * - Files are saved with random UUID-based names to prevent easy discovery
 * - Original URLs and file mappings are stored in a separate metadata file
 * 
 * Storage Structure:
 * filesDir/
 *   ├── secure_images/
 *   │   ├── {uuid1}.webp (downloaded image)
 *   │   ├── {uuid2}.webp (downloaded image)
 *   │   └── ...
 *   └── secure_images_metadata.json (maps UUIDs to original URLs and metadata)
 * 
 * Usage:
 * ```
 * // Download image
 * val uri = SecureImageDownloader.downloadImage(context, imageUrl)
 * 
 * // Display in Coil
 * AsyncImage(
 *     model = uri,
 *     contentDescription = "Downloaded image"
 * )
 * 
 * // Use for sticker pack
 * val sticker = Sticker(
 *     imageFileName = "sticker.webp",
 *     sourceUri = uri
 * )
 * ```
 */
object SecureImageDownloader {
    private const val TAG = "SecureImageDownloader"
    private const val SECURE_IMAGES_DIR = "secure_images"
    private const val METADATA_FILE = "secure_images_metadata.json"
    
    private val okHttpClient = OkHttpClient()
    private val gson = Gson()
    private val metadataType = object : TypeToken<Map<String, String>>() {}.type
    
    /**
     * Downloads an image from a URL and saves it securely to internal storage.
     * 
     * @param context Application context
     * @param imageUrl The URL of the image to download
     * @param fileExtension Optional file extension (default: "webp"). If URL doesn't provide one, this is used.
     * @return Uri pointing to the downloaded file in internal storage, or null if download failed
     * 
     * The file is saved with a random UUID filename to prevent easy discovery:
     * - Original URL is stored in metadata for reference
     * - File is accessible only through this app's context
     * - File cannot be accessed by file managers or other apps
     */
    suspend fun downloadImage(
        context: Context,
        imageUrl: String,
        fileExtension: String = "webp"
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            // Validate URL
            if (imageUrl.isBlank()) {
                Log.e(TAG, "Invalid URL: blank")
                return@withContext null
            }
            
            // Create secure images directory if it doesn't exist
            val secureDir = File(context.filesDir, SECURE_IMAGES_DIR)
            if (!secureDir.exists()) {
                secureDir.mkdirs()
            }
            
            // Generate secure random filename using UUID
            val fileName = "${UUID.randomUUID()}.$fileExtension"
            val targetFile = File(secureDir, fileName)
            
            // Download image using OkHttp
            val request = Request.Builder()
                .url(imageUrl)
                .build()
            
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Download failed: HTTP ${response.code} for URL: $imageUrl")
                    return@withContext null
                }
                
                val body = response.body
                
                // Save to internal storage
                body.byteStream().use { inputStream ->
                    FileOutputStream(targetFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                
                Log.d(TAG, "Image downloaded successfully: ${targetFile.absolutePath}")
                
                // Create Uri for the file
                // For internal storage, file:// URI works with Coil on all Android versions
                // Internal storage files are private and not accessible by file managers or other apps
                // This is the "secret" location requested - files are completely hidden from file managers
                val uri = Uri.fromFile(targetFile)
                
                // Save metadata for reference (optional - can be used for cleanup/debugging)
                saveMetadata(context, fileName, imageUrl)
                
                Log.d(TAG, "Image saved securely: ${targetFile.absolutePath} (not accessible by file managers)")
                
                return@withContext uri
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading image from URL: $imageUrl", e)
            return@withContext null
        }
    }
    
    /**
     * Downloads multiple images from URLs.
     * 
     * @param context Application context
     * @param imageUrls List of image URLs to download
     * @param fileExtension Optional file extension (default: "webp")
     * @return List of Uris for successfully downloaded images (in same order as input, null for failures)
     */
    suspend fun downloadImages(
        context: Context,
        imageUrls: List<String>,
        fileExtension: String = "webp"
    ): List<Uri?> = withContext(Dispatchers.IO) {
        imageUrls.map { url ->
            downloadImage(context, url, fileExtension)
        }
    }
    
    /**
     * Gets a Uri for a file that was previously downloaded.
     * Note: This only works if you know the UUID filename. Use getUriByOriginalUrl() for lookup by URL.
     * 
     * @param context Application context
     * @param fileName The UUID filename (e.g., "550e8400-e29b-41d4-a716-446655440000.webp")
     * @return Uri if file exists, null otherwise
     */
    fun getUriForFile(context: Context, fileName: String): Uri? {
        val file = File(context.filesDir, "$SECURE_IMAGES_DIR/$fileName")
        return if (file.exists()) {
            Uri.fromFile(file)
        } else {
            null
        }
    }
    
    /**
     * Checks if a file exists in secure storage.
     * 
     * @param context Application context
     * @param fileName The UUID filename
     * @return true if file exists
     */
    fun fileExists(context: Context, fileName: String): Boolean {
        val file = File(context.filesDir, "$SECURE_IMAGES_DIR/$fileName")
        return file.exists()
    }
    
    /**
     * Deletes a downloaded image from secure storage.
     * 
     * @param context Application context
     * @param uri The Uri of the file to delete
     * @return true if file was deleted successfully
     */
    fun deleteImage(context: Context, uri: Uri): Boolean {
        return try {
            val file = File(uri.path ?: return false)
            if (file.exists() && file.parentFile?.name == SECURE_IMAGES_DIR) {
                val deleted = file.delete()
                if (deleted) {
                    removeMetadata(context, file.name)
                }
                deleted
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting image", e)
            false
        }
    }
    
    /**
     * Gets all downloaded image Uris from secure storage.
     * 
     * @param context Application context
     * @return List of Uris for all downloaded images
     */
    fun getAllDownloadedImages(context: Context): List<Uri> {
        val secureDir = File(context.filesDir, SECURE_IMAGES_DIR)
        return if (secureDir.exists() && secureDir.isDirectory) {
            secureDir.listFiles()?.filter { it.isFile }?.map { Uri.fromFile(it) } ?: emptyList()
        } else {
            emptyList()
        }
    }
    
    /**
     * Clears all downloaded images from secure storage.
     * 
     * @param context Application context
     * @return Number of files deleted
     */
    fun clearAllImages(context: Context): Int {
        val secureDir = File(context.filesDir, SECURE_IMAGES_DIR)
        return if (secureDir.exists() && secureDir.isDirectory) {
            val files = secureDir.listFiles()
            var deletedCount = 0
            files?.forEach { file ->
                if (file.isFile && file.delete()) {
                    deletedCount++
                }
            }
            // Delete metadata file
            val metadataFile = File(context.filesDir, METADATA_FILE)
            if (metadataFile.exists()) {
                metadataFile.delete()
            }
            deletedCount
        } else {
            0
        }
    }
    
    /**
     * Gets the file path for use with ContentProvider or other systems that need absolute paths.
     * 
     * @param context Application context
     * @param uri The Uri of the downloaded file
     * @return Absolute file path, or null if invalid
     */
    fun getFilePath(context: Context, uri: Uri): String? {
        return try {
            val file = File(uri.path ?: return null)
            if (file.exists() && file.parentFile?.name == SECURE_IMAGES_DIR) {
                file.absolutePath
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting file path", e)
            null
        }
    }
    
    // Private helper functions for metadata management
    
    /**
     * Saves metadata about downloaded image (URL mapping for reference/debugging).
     * This is optional and stored as JSON using Gson.
     * 
     * Metadata format: { "filename": "original_url", ... }
     */
    private fun saveMetadata(context: Context, fileName: String, originalUrl: String) {
        try {
            val metadataFile = File(context.filesDir, METADATA_FILE)
            val metadata = if (metadataFile.exists()) {
                try {
                    val json = metadataFile.readText()
                    gson.fromJson<Map<String, String>>(json, metadataType).toMutableMap()
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing metadata, creating new", e)
                    mutableMapOf()
                }
            } else {
                mutableMapOf()
            }
            
            metadata[fileName] = originalUrl
            val jsonString = gson.toJson(metadata)
            metadataFile.writeText(jsonString)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving metadata", e)
        }
    }
    
    /**
     * Removes metadata entry for a deleted file.
     */
    private fun removeMetadata(context: Context, fileName: String) {
        try {
            val metadataFile = File(context.filesDir, METADATA_FILE)
            if (metadataFile.exists()) {
                val metadata = try {
                    val json = metadataFile.readText()
                    gson.fromJson<Map<String, String>>(json, metadataType).toMutableMap()
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing metadata for removal", e)
                    return
                }
                
                metadata.remove(fileName)
                
                if (metadata.isEmpty()) {
                    metadataFile.delete()
                } else {
                    val jsonString = gson.toJson(metadata)
                    metadataFile.writeText(jsonString)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error removing metadata", e)
        }
    }
}

