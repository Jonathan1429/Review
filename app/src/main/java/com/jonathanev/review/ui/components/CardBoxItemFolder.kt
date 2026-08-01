package com.jonathanev.review.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jonathanev.review.presentation.model.ColorType
import com.jonathanev.review.presentation.model.FileFormMode
import com.jonathanev.review.presentation.model.IconType
import com.jonathanev.review.presentation.state.PropertiesFilesState
import com.jonathanev.review.ui.theme.getCardContainerColor
import com.jonathanev.review.ui.theme.getColorSubtitle

@Preview
@Composable
fun A() {
    CardBoxItemFolder(
        state = PropertiesFilesState(
            name = "",
            description = "",
            oldName = "",
            oldDescription = "",
            icon = IconType.BACTERIA_SOLID_FULL,
            color = ColorType.Default,
            selectedIndex = 0,
            icons = listOf(IconType.BACTERIA_SOLID_FULL),
            showOverwriteDialogFile = false,
            showOverwriteDialogFolder = false
        ),
        fileFormMode = FileFormMode.CreatingFile
    )
}

@Preview
@Composable
fun B() {
    CardBoxItemFolder(
        state = PropertiesFilesState(
            name = "",
            description = "",
            oldName = "",
            oldDescription = "",
            icon = IconType.BACTERIA_SOLID_FULL,
            color = ColorType.Default,
            selectedIndex = 0,
            icons = listOf(IconType.BACTERIA_SOLID_FULL),
            showOverwriteDialogFile = false,
            showOverwriteDialogFolder = false
        ),
        fileFormMode = FileFormMode.CreatingFile
    )
}

@Composable
fun CardBoxItemFolder(state: PropertiesFilesState, fileFormMode: FileFormMode) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = getCardContainerColor()),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BoxItemFolder(
                iconRes = state.icon,
                iconColor = state.color,
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = state.name.ifEmpty { "Nuevo Proyecto" },
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (fileFormMode is FileFormMode.CreatingFolder) "Previsualización de Carpeta" else "Previsualización de Archivo",
                style = MaterialTheme.typography.bodySmall,
                color = getColorSubtitle()
            )
        }
    }
}