package com.saha.androidfm.views.screens

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.saha.androidfm.R
import com.saha.androidfm.ui.theme.cardBackgroundColor
import com.saha.androidfm.ui.theme.secondaryButtonColor
import com.saha.androidfm.ui.theme.secondaryTextColor
import com.saha.androidfm.utils.ext.showLongToast
import com.saha.androidfm.utils.ext.showShortToast
import com.saha.androidfm.views.components.HeightGap
import com.saha.androidfm.views.components.MyTopBar
import com.saha.androidfm.views.components.SettingItem

@Composable
fun SettingScreen(navController: NavController) {
    val context: Context = LocalContext.current

    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(bottom = 8.dp)
    ) {

        MyTopBar(text = stringResource(R.string.settings),
            //showBackButton = false,
            onProButtonClick = {
                context.showLongToast("Pro Button Clicked")
            })

        //Preference
        Text(
            text = stringResource(R.string.preference),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = secondaryTextColor
            ),
            modifier = Modifier
                .padding(start = 8.dp)
        )

        HeightGap(8.dp)

        Card(
            modifier = Modifier
                .fillMaxWidth(),
            /*.shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color.Gray, // Shadow color
                spotColor = Color.Gray
            )*/
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = cardBackgroundColor
            ),
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                SettingItem(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.haptic_feedback),
                    icon = { Icon(Icons.Default.Vibration, contentDescription = "Haptic", tint = MaterialTheme.colorScheme.primary) }
                ) {
                    context.showShortToast("Haptic Feedback Clicked")
                }
            }
        }


        HeightGap(24.dp)

        //in app purchase
        Text(
            text = stringResource(R.string.in_app_purchase),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = secondaryTextColor
            ),
            modifier = Modifier
                .padding(start = 8.dp)
        )

        HeightGap(8.dp)

        Card(
            modifier = Modifier
                .fillMaxWidth(),
            /*.shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color.Gray, // Shadow color
                spotColor = Color.Gray
            )*/
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = cardBackgroundColor
            ),
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                SettingItem(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.upgrade_pro),
                    icon = { Icon(Icons.Default.WorkspacePremium, contentDescription = "Upgrade Pro", tint = MaterialTheme.colorScheme.primary) },
                ) {
                    context.showShortToast("Haptic Feedback Clicked")
                }

                HeightGap(8.dp)
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    color = secondaryButtonColor
                )

                HeightGap(8.dp)

                SettingItem(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.restore_purchases),
                    icon = { Icon(Icons.Default.Restore, contentDescription = "Restore", tint = MaterialTheme.colorScheme.primary) },
                ) {
                    context.showShortToast("Restore purchases")
                }

                HeightGap(8.dp)
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    color = secondaryButtonColor
                )

                HeightGap(8.dp)

                SettingItem(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.buy_more_coins),
                    icon = { Icon(Icons.Default.MonetizationOn, contentDescription = "Coins", tint = MaterialTheme.colorScheme.primary) },
                ) {
                    context.showShortToast("Haptic Feedback Clicked")
                }
            }
        }


        HeightGap(24.dp)

        //feedback
        Text(
            text = stringResource(R.string.feedback),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = secondaryTextColor
            ),
            modifier = Modifier
                .padding(start = 8.dp)
        )

        HeightGap(8.dp)

        Card(
            modifier = Modifier
                .fillMaxWidth(),
            /*.shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color.Gray, // Shadow color
                spotColor = Color.Gray
            )*/
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = cardBackgroundColor
            ),
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                SettingItem(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.invite_a_friend),
                    icon = { Icon(Icons.Default.PersonAdd, contentDescription = "Invite Friend", tint = MaterialTheme.colorScheme.primary) },
                ) {
                    context.showShortToast("Haptic Feedback Clicked")
                }

                HeightGap(8.dp)
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    color = secondaryButtonColor
                )

                HeightGap(8.dp)

                SettingItem(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.rate_the_app),
                    icon = { Icon(Icons.Default.StarRate, contentDescription = "Rate App", tint = MaterialTheme.colorScheme.primary) },
                ) {
                    context.showShortToast("Haptic Feedback Clicked")
                }

                HeightGap(8.dp)
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    color = secondaryButtonColor
                )

                HeightGap(8.dp)

                SettingItem(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.request_a_feature),
                    icon = { Icon(Icons.Default.Lightbulb, contentDescription = "Request Feature", tint = MaterialTheme.colorScheme.primary) },
                ) {
                    context.showShortToast("Haptic Feedback Clicked")
                }

                HeightGap(8.dp)
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    color = secondaryButtonColor
                )

                HeightGap(8.dp)

                SettingItem(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.contact),
                    icon = { Icon(Icons.Default.Email, contentDescription = "Contact", tint = MaterialTheme.colorScheme.primary) },
                ) {
                    context.showShortToast("Haptic Feedback Clicked")
                }
            }
        }


        HeightGap(24.dp)

        //feedback
        Text(
            text = stringResource(R.string.info),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = secondaryTextColor
            ),
            modifier = Modifier
                .padding(start = 8.dp)
        )

        HeightGap(8.dp)

        Card(
            modifier = Modifier
                .fillMaxWidth(),
            /*.shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color.Gray, // Shadow color
                spotColor = Color.Gray
            )*/
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = cardBackgroundColor
            ),
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                SettingItem(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.terms_of_service),
                    icon = { Icon(Icons.Default.Description, contentDescription = "Terms", tint = MaterialTheme.colorScheme.primary) },
                ) {
                    context.showShortToast("Haptic Feedback Clicked")
                }

                HeightGap(8.dp)
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    color = secondaryButtonColor
                )

                HeightGap(8.dp)

                SettingItem(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.privacy_policy),
                    icon = { Icon(Icons.Default.Shield, contentDescription = "Privacy",tint = MaterialTheme.colorScheme.primary) },
                ) {
                    context.showShortToast("Haptic Feedback Clicked")
                }
            }
        }

    }
}