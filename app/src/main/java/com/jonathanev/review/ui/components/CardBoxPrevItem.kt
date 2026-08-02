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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.jonathanev.review.presentation.model.ColorType
import com.jonathanev.review.presentation.model.FileFormMode
import com.jonathanev.review.presentation.model.IconType
import com.jonathanev.review.ui.preview.ComponentsPreviews
import com.jonathanev.review.ui.preview.providers.CardBoxItemFolderProv
import com.jonathanev.review.ui.preview.providers.CardBoxItemFolderProviders
import com.jonathanev.review.ui.theme.getCardContainerColor
import com.jonathanev.review.ui.theme.getColorSubtitle

@ComponentsPreviews
@Composable
fun PreviewCardBoxPrevItem(
    @PreviewParameter(CardBoxItemFolderProviders::class) data: CardBoxItemFolderProv
) {
    CardBoxPrevItem(
        name = "",
        icon = data.icon,
        color = data.color,
        fileFormMode = data.fileFormMode
    )
}

@Composable
fun CardBoxPrevItem(
    name: String,
    icon: IconType,
    color: ColorType,
    fileFormMode: FileFormMode
) {
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
                iconRes = icon,
                iconColor = color,
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = name.ifEmpty { "Nuevo Proyecto" },
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