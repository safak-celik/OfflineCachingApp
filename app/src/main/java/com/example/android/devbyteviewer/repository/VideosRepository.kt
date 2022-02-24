/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.example.android.devbyteviewer.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.android.devbyteviewer.database.VideosDatabase
import com.example.android.devbyteviewer.database.asDomainModel
import com.example.android.devbyteviewer.domain.Video
import com.example.android.devbyteviewer.network.Network
import com.example.android.devbyteviewer.network.asDatabaseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for fetching debyte Videos from network andStoring them on disk
 * A Repository is just a regular class that has one (or more) methods that load data without
 * specifying the data source as part of the main API. Because it's just a regular class,
 * there's no need for an annotation to define a repository.
 * The repository hides the complexity of managing the interactions between the database and
 * the networking code.
 */

class VideosRepository(private val database: VideosDatabase) {

    /**
     * LiveData with List of Videos
     * convert the list of DatabaseVideo to a list of Video with asDomainModel()
     * Transformations.map is perfect for mapping the output of one LiveData to another type
     * convert your LiveData list of DatabaseVideo objects to domain Video with Transformations.map
     */

    val videos: LiveData<List<Video>> =
        Transformations.map(database.videoDao.getVideos()) {
            it.asDomainModel()
        }// from the DB

    // Coroutine Function -> Daher Suspend Function
    suspend fun refreshVideos() {
        withContext(Dispatchers.IO) {
            // Get Playlist with  Network Call
            /** await : function to tell the coroutine to suspend until the data is available.
             *"*" is the spread operator. It allows you to pass in an array to a function that expects varargs.
             */
            val playlist = Network.devbytes.getPlaylist().await()
            /**
             * Database Call by using insertAll() auf videoDao
             * map Networks Result To Database Object with asDatabaseModel
             * insertAll() to insert the playlist into the database.
             */
            database.videoDao.insertAll(*playlist.asDatabaseModel())
        }
    }
}
