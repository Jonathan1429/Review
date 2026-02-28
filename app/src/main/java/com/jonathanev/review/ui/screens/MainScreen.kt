package com.jonathanev.review.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jonathanev.review.R
import com.jonathanev.review.ui.theme.BorderPasos
import com.jonathanev.review.ui.theme.CircleContentSVG
import com.jonathanev.review.ui.theme.ColorBotones
import com.jonathanev.review.ui.theme.Inter
import com.jonathanev.review.ui.theme.TextGray
import com.jonathanev.review.ui.theme.cardStepBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onCreateFolderClick: () -> Unit
) {
    Scaffold(
        //topBar = { TopAppBar(title = { Text("Carpetas") }) },
        floatingActionButton = {
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SinFolders()
            Spacer(Modifier.size(8.dp))
            BasePasos(
                R.drawable.ic_plus_circle_outline,
                R.string.lblTitleStepOne,
                R.string.lblDescStepOne
            )
            Spacer(Modifier.size(8.dp))
            BasePasos(
                R.drawable.ic_palette,
                R.string.lblTitleStepTwo,
                R.string.lblDescStepTwo
            )
            Spacer(Modifier.size(8.dp))
            BasePasos(
                R.drawable.ic_folder_move_outline,
                R.string.lblTitleStepThree,
                R.string.lblDescStepThree
            )
        }
    }
}

@Composable
fun BasePasos(image: Int, title: Int, description: Int) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                color = cardStepBackground, //Contents
                shape = RoundedCornerShape(10.dp)
            ) //Contents
            .border(2.dp, BorderPasos, RoundedCornerShape(10.dp))
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(CircleContentSVG)
                .padding(8.dp),
            painter = painterResource(image),
            contentDescription = "añadir carpeta"
        )
        Spacer(Modifier.size(10.dp))
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = stringResource(title),
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = Inter,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stringResource(description),
                color = TextGray,
                fontSize = 15.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SinFolders() {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(cardStepBackground) //Contents
            .padding(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(CircleContentSVG)
                .padding(8.dp),
            painter = painterResource(R.drawable.ic_folder_off),
            contentDescription = "imagen folders"
        )

        Text(
            modifier = Modifier.padding(top = 10.dp),
            text = stringResource(R.string.lblWithoutFolders),
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = Inter,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            modifier = Modifier.padding(top = 10.dp),
            text = stringResource(R.string.lblWithoutFoldersDes),
            color = TextGray,
            textAlign = TextAlign.Center
        )
    }
}