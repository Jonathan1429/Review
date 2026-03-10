package com.jonathanev.review.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(showBackground = true)
@Composable
fun PreviewCreateFiles() {
    CreateFiles(name = "", onValueChangeName = {})
}

@Composable
fun CreateFiles(name: String, onValueChangeName: (String) -> Unit) {
    Scaffold(
        //topBar = { TopAppBar(title = { Text("Carpetas") }) },
        /*floatingActionButton = {
            FloatingActionButton(
                onClick = { onCreateFolderClick() },
                containerColor = ColorBotones
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Boton crear carpeta",
                    tint = Color.White
                )
            }
        }*/
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .padding(padding)
                .padding(8.dp)
        ) {
            OutlinedTextField(name, { onValueChangeName(it) })
            TextField(value = name, onValueChange = { onValueChangeName(it) })
        }
    }
}

