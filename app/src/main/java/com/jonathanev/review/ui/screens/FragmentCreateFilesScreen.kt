package com.jonathanev.review.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(showBackground = true)
@Composable
fun PreviewCreateFiles(){
    CreateFiles(name = "", onValueChangeName = {})
}

@Composable
fun CreateFiles(name: String, onValueChangeName: (String) -> Unit){
    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        OutlinedTextField(name, { onValueChangeName(it) })
    }
}