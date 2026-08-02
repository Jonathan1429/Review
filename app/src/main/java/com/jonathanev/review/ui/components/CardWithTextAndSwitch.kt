package com.jonathanev.review.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jonathanev.review.R
import com.jonathanev.review.ui.preview.ComponentsPreviews
import com.jonathanev.review.ui.preview.providers.CardWithTextAndSwitchProvider
import com.jonathanev.review.ui.theme.HardColorButton
import com.jonathanev.review.ui.theme.ReviewTheme
import com.jonathanev.review.ui.theme.TextGray
import com.jonathanev.review.ui.theme.cardStepBackground
import com.jonathanev.review.ui.theme.getAlerDialogContainerColor
import com.jonathanev.review.ui.theme.getColorSubtitle
import com.jonathanev.review.ui.theme.lighten

@ComponentsPreviews
@Composable
fun PreviewCardWithTextAndSwitch(
    @PreviewParameter(CardWithTextAndSwitchProvider::class) data: Boolean
) {
    ReviewTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "PREVIEW: Checked: $data",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }

                CardWithTextAndSwitch(
                    isChecked = data,
                    onCheckedChange = {}
                )
            }
        }
    }
}

@Composable
fun CardWithTextAndSwitch(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = getAlerDialogContainerColor(), shape = RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.lblNoVolverPreguntar),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(id = R.string.lblRecordarEleccion),
                    color = getColorSubtitle(),
                    fontSize = 12.sp
                )
            }

            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = HardColorButton.lighten(0.4f),
                    uncheckedThumbColor = TextGray,
                    uncheckedTrackColor = cardStepBackground
                )
            )
        }
    }
}