package com.saha.androidfm.views.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.saha.androidfm.ui.theme.primaryButtonColor
import com.saha.androidfm.ui.theme.primaryButtonColor20
import com.saha.androidfm.ui.theme.primaryButtonColor50

@Composable
fun CustomTabLayout(
    tabItems: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(primaryButtonColor20, shape = RoundedCornerShape(8.dp)) // Background color with rounded corners
            .padding(2.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        tabItems.forEachIndexed { index, title ->
            val isSelected = selectedIndex == index
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) primaryButtonColor50 else Color.Transparent) // Selected & unselected colors
                    .clickable { onTabSelected(index) }
                    .padding(vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = primaryButtonColor
                )
            }
        }
    }
}
