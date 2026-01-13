package com.saha.androidfm.utils.helpers

import android.util.Log
import com.saha.androidfm.viewmodels.M3UStream
import java.io.File

object M3UParser {
    private const val TAG = "M3UParser"
    
    /**
     * Parses an M3U file and returns a list of streams
     * Supports both M3U and M3U8 formats
     */
    fun parseM3UFile(file: File): List<M3UStream> {
        val streams = mutableListOf<M3UStream>()
        
        try {
            if (!file.exists()) {
                Log.e(TAG, "File does not exist: ${file.absolutePath}")
                return streams
            }
            
            val lines = file.readLines()
            var currentName: String? = null
            var currentDuration: Int? = null
            
            for (line in lines) {
                val trimmed = line.trim()
                
                // Skip empty lines
                if (trimmed.isEmpty()) continue
                
                // Check for extended M3U format (#EXTM3U)
                if (trimmed.startsWith("#EXTM3U")) {
                    continue
                }
                
                // Check for extended info line (#EXTINF)
                if (trimmed.startsWith("#EXTINF:")) {
                    // Format: #EXTINF:duration,Name
                    val info = trimmed.substring(8) // Remove "#EXTINF:"
                    val commaIndex = info.indexOf(',')
                    
                    if (commaIndex > 0) {
                        val durationStr = info.substring(0, commaIndex)
                        currentDuration = durationStr.toIntOrNull()
                        currentName = info.substring(commaIndex + 1).trim()
                    } else {
                        // No comma, try to parse duration only
                        currentDuration = info.toIntOrNull()
                        currentName = null
                    }
                    continue
                }
                
                // If line doesn't start with #, it's a URL
                if (!trimmed.startsWith("#")) {
                    // This is a URL
                    streams.add(
                        M3UStream(
                            url = trimmed,
                            name = currentName,
                            duration = currentDuration
                        )
                    )
                    // Reset for next stream
                    currentName = null
                    currentDuration = null
                }
            }
            
            Log.d(TAG, "Parsed ${streams.size} streams from M3U file")
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing M3U file: ${e.message}", e)
        }
        
        return streams
    }
    
    /**
     * Parses M3U content from a string (useful for network streams)
     */
    fun parseM3UContent(content: String): List<M3UStream> {
        val streams = mutableListOf<M3UStream>()
        
        try {
            val lines = content.lines()
            var currentName: String? = null
            var currentDuration: Int? = null
            
            for (line in lines) {
                val trimmed = line.trim()
                
                if (trimmed.isEmpty()) continue
                
                if (trimmed.startsWith("#EXTM3U")) {
                    continue
                }
                
                if (trimmed.startsWith("#EXTINF:")) {
                    val info = trimmed.substring(8)
                    val commaIndex = info.indexOf(',')
                    
                    if (commaIndex > 0) {
                        val durationStr = info.substring(0, commaIndex)
                        currentDuration = durationStr.toIntOrNull()
                        currentName = info.substring(commaIndex + 1).trim()
                    } else {
                        currentDuration = info.toIntOrNull()
                        currentName = null
                    }
                    continue
                }
                
                if (!trimmed.startsWith("#")) {
                    streams.add(
                        M3UStream(
                            url = trimmed,
                            name = currentName,
                            duration = currentDuration
                        )
                    )
                    currentName = null
                    currentDuration = null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing M3U content: ${e.message}", e)
        }
        
        return streams
    }
}
