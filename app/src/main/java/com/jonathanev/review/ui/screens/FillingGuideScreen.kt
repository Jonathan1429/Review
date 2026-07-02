package com.jonathanev.review.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FillingGuideScreen() {
    Scaffold(
        //containerColor = BackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // 1. Selector de Pregunta (Navegación entre items)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* Anterior */ }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Prev", tint = Color.White)
                }
                Text(
                    text = "Question 3 of 15",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                IconButton(onClick = { /* Siguiente */ }) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Next", tint = Color.White)
                }
            }

            // 2. Tabs "Pregunta" y "Respuesta" con degradado inferior
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Pestaña Pregunta (Seleccionada)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        //.background(CardBackground, RoundedCornerShape(8.dp))
                        .border(
                            BorderStroke(
                                2.dp,
                                Brush.verticalGradient(listOf(Color.Transparent, Color.Red)) // AccentBorder
                            ),
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Pregunta", color = Color.White, fontSize = 18.sp)
                }

                // Pestaña Respuesta
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                        //.background(CardBackground, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Respuesta", color = Color.Red, fontSize = 18.sp) // TextSecondary
                }
            }

            // 3. Filtros/Chips (Text, Images, Audio, etc.)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChipItem(text = "Text", iconRes = null, isSelected = false)
                FilterChipItem(text = "Images", iconRes = null, isSelected = true) // Activo
                FilterChipItem(text = "Audio", iconRes = null, isSelected = false)

                // Icono final tipo claqueta/video sin texto
                Box(
                    modifier = Modifier
                        .size(40.dp),
                        //.background(CardBackground, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Reemplazar con el icono adecuado o un Vector
                    Icon(Icons.Default.Menu, contentDescription = "Video", tint = Color.Red, modifier = Modifier.size(18.dp)) // TextSecondary
                }
            }

            // 4. Grid de contenido (Add Image + Imágenes existentes)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Item: Añadir Imagen (Borde punteado/discontinuo simulado con guiones)
                item {
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .border(
                                BorderStroke(1.5.dp, Color.Gray), // Nota: Compose nativo requiere Canvas para líneas punteadas exactas
                                RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.Gray, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Add Image", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                }

                // Items de ejemplo con imágenes mockeadas
                item { MediaImageCard(imageRes = null /* Colocar R.drawable.tu_imagen */) }
                item { MediaImageCard(imageRes = null) }
                item { MediaImageCard(imageRes = null) }
            }
        }
    }
}

@Composable
fun FilterChipItem(text: String, iconRes: Int?, isSelected: Boolean) {
    val backgroundColor = if (isSelected) Color(0xF22153B1) else Color.Red //CardBackground
    val borderStroke = if (isSelected) BorderStroke(1.5.dp, Color.Red) else BorderStroke(1.dp, Color.Transparent) // AccentBorder
    val textColor = if (isSelected) Color.White else Color.Red //TextSecondary

    Box(
        modifier = Modifier
            .height(40.dp)
            .background(backgroundColor, RoundedCornerShape(20.dp))
            .border(borderStroke, RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Aquí puedes integrar tus iconos asignados según el tipo
            Text(text = text, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun MediaImageCard(imageRes: Int?) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            //.background(CardBackground)
    ) {
        // Renderizado de Imagen Placeholder (En producción usa AsyncImage o painterResource)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xF1118271), Color(0xF1F29371))
                    )
                )
        )

        // Botón flotante para eliminar (Tacho de basura)
        IconButton(
            onClick = { /* Eliminar acción */ },
            modifier = Modifier
                .padding(8.dp)
                .size(36.dp)
                //.background(Color(0密F111827).copy(alpha = 0.6f), CircleShape)
                .align(Alignment.TopStart)
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}