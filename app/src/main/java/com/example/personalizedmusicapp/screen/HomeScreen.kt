
package com.example.personalizedmusicapp.screen

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.personalizedmusicapp.BuildConfig
import com.example.personalizedmusicapp.YoutubePlayer
import com.example.personalizedmusicapp.data.Item
import com.example.personalizedmusicapp.data.PlayListItemsResponse
import com.example.personalizedmusicapp.model.VideoEvent
import com.example.personalizedmusicapp.model.VideoState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("playlistItems")
    suspend fun getPlaylistItems(
        @Query("part") part: String,
        @Query("maxResults") maxResults: String,
        @Query("playlistId") playlistId: String,
        @Query("key") key: String
    ): Response<PlayListItemsResponse>
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: VideoState,
    onEvent: (VideoEvent) -> Unit){

    var playListItems by remember { mutableStateOf(emptyList<Item>()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(key1 = Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://www.googleapis.com/youtube/v3/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val apiService = retrofit.create(ApiService::class.java)

            val part = "snippet"
            val maxResults = "50"
            val playlistId = "PL9JwhzITbbGZGA5qjHDbVfNQnK5Sc_XWG"
            val key = BuildConfig.API_KEY

            val response = apiService.getPlaylistItems(part, maxResults, playlistId, key)

            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    playListItems = responseBody.items
                }
                Log.d("MyApp", "Success")
            } else {
                // Handle API error here
                Log.d("MyApp", "Failed to retrieve!")
            }
        }
    }
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        )
        {
            items(playListItems) { item ->
                ItemCard(item, state, onEvent = onEvent)
            }
            item { Row(modifier = Modifier.height(120.dp)){} }
        }
    }

fun showToastMessage(context: Context, message: String){
    Toast.makeText(context,message, Toast.LENGTH_SHORT).show()
}

@Composable
fun ItemCard(item: Item, state: VideoState, onEvent: (VideoEvent) -> Unit) {

    var isFound = false
    state.videos.forEach{
        if (it.youtubeId == item.snippet.resourceId.videoId)
            isFound = true
    }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth().padding(5.dp)){
        Column(modifier = Modifier.padding(5.dp)){
            Row (modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically){
                Text(item.snippet.title)
                Row (modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End){
                    IconButton(onClick = {
                        onEvent(VideoEvent.SetYoutubeId(item.snippet.resourceId.videoId))
                        if (isFound)
                            onEvent(VideoEvent.DeleteVideoByYoutubeId(item.snippet.resourceId.videoId))
                        else
                            onEvent(VideoEvent.SaveVideo)
                    }) {
                        if (isFound)
                            Icon(Icons.Filled.Favorite, contentDescription = null, tint = Color.Red)
                        else
                            Icon(Icons.Outlined.FavoriteBorder, contentDescription = null, tint = Color.Red)
                    }
                }
            }
            Text(item.snippet.position)
            Text(item.snippet.resourceId.videoId)
            YoutubePlayer(youtubeVideoId = item.snippet.resourceId.videoId)
        }
    }
}