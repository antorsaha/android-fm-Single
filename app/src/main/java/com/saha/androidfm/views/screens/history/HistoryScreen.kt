package com.saha.androidfm.views.screens.history

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.saha.androidfm.R
import com.saha.androidfm.data.enums.MenuAction
import com.saha.androidfm.utils.ext.showLongToast
import com.saha.androidfm.views.components.CustomTabLayout
import com.saha.androidfm.views.components.HeightGap
import com.saha.androidfm.views.components.MyTopBar
import com.saha.androidfm.views.components.StickerItem

@Composable
fun HistoryScreen(navController: NavController) {
    val context: Context = LocalContext.current
    val viewModel: HistoryViewModel = hiltViewModel()

    val tebItems = listOf(stringResource(R.string.all), stringResource(R.string.favorite))


    LaunchedEffect(Unit) {

        viewModel.loadDownloadedStickers()

    }

    Column(modifier = Modifier.fillMaxSize()) {

        MyTopBar(
            text = stringResource(R.string.history),
            //showBackButton = false,
            onProButtonClick = {
                context.showLongToast("Pro Button Clicked")
            })

        CustomTabLayout(
            tabItems = tebItems,
            selectedIndex = viewModel.selectedTabIndex,
        ) {
            viewModel.selectedTabIndex = it
        }

        HeightGap(8.dp)

        if (viewModel.selectedTabIndex == 1) {
            //favorite stickers

            if (viewModel.likedImageUris.isEmpty()) {
                EmptyHistoryView(isFavorite = true)
            } else {
                FavoriteHistoryList(viewModel)
            }


        } else {
            //all history

            if (viewModel.downloadedImageUris.isEmpty()) {
                EmptyHistoryView(isFavorite = false)
            } else {
                AllHistoryList(viewModel)
            }
        }
    }
}

@Composable
fun FavoriteHistoryList(viewModel: HistoryViewModel) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // 2 columns
        modifier = Modifier.fillMaxSize()
    ) {
        items(viewModel.likedImageUris.size) { index ->

            if (index % 2 == 0) {
                StickerItem(
                    Modifier.padding(end = 4.dp, top = 8.dp),
                    imageUri = viewModel.likedImageUris[index],
                    isFavorites = true,
                    isHistory = true,
                    onMenuActionClick = { menuAction ->
                        when(menuAction){
                            MenuAction.DELETE -> {

                            }
                            MenuAction.MARK_AS_FAVORITE ->{
                                // Toggle favorite to remove from favorites
                                viewModel.toggleFavoriteStatus(viewModel.likedImageUris[index])
                            }
                            else -> {

                            }
                        }
                    }
                )
            } else {
                StickerItem(
                    Modifier.padding(start = 4.dp, top = 8.dp),
                    imageUri = viewModel.likedImageUris[index],
                    isFavorites = true,
                    isHistory = true,
                    onMenuActionClick = { menuAction ->
                        when(menuAction){
                            MenuAction.DELETE -> {

                            }
                            MenuAction.MARK_AS_FAVORITE ->{
                                // Toggle favorite to remove from favorites
                                viewModel.toggleFavoriteStatus(viewModel.likedImageUris[index])
                            }
                            else -> {

                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AllHistoryList(viewModel: HistoryViewModel) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // 2 columns
        modifier = Modifier.fillMaxSize()
    ) {
        items(viewModel.downloadedImageUris.size) { index ->

            if (index % 2 == 0) {
                StickerItem(
                    Modifier.padding(end = 4.dp, top = 8.dp),
                    imageUri = viewModel.downloadedImageUris[index],
                    isHistory = true,
                    onMenuActionClick = { menuAction ->
                        when(menuAction){
                            MenuAction.DELETE -> {

                            }
                            MenuAction.MARK_AS_FAVORITE ->{
                                viewModel.markAsFavorite(viewModel.downloadedImageUris[index])
                            }
                            else -> {

                            }
                        }

                    }
                )
            } else {
                StickerItem(
                    Modifier.padding(start = 4.dp, top = 8.dp),
                    imageUri = viewModel.downloadedImageUris[index],
                    isHistory = true,
                    onMenuActionClick = { menuAction ->
                        when(menuAction){
                            MenuAction.DELETE -> {

                            }
                            MenuAction.MARK_AS_FAVORITE ->{
                                viewModel.markAsFavorite(viewModel.downloadedImageUris[index])
                            }
                            else -> {

                            }
                        }

                    }
                )
            }

        }

    }
}

@Composable
fun EmptyHistoryView(isFavorite: Boolean = false) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(R.drawable.img_empty),
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
            )

            HeightGap(8.dp)

            if (isFavorite) {
                Text(
                    text = stringResource(R.string.no_favorite_found),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text(
                    text = stringResource(R.string.no_history_found),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

        }
    }
}