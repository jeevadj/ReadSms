package com.example.readsmssample.elements

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension

@Composable
fun TopBar(title : String, subtitle : String? = null, content :(@Composable () -> Unit)? = null ){

    val backPressDispatcher = LocalOnBackPressedDispatcherOwner.current
    ConstraintLayout(modifier = Modifier.height(56.dp).fillMaxWidth()) {
        val (back, titleText, otherOptions) = createRefs()

        IconButton(modifier = Modifier.constrainAs(back){
            start.linkTo(parent.start)
            centerVerticallyTo(parent)
        }, onClick = {
            backPressDispatcher?.onBackPressedDispatcher?.onBackPressed()
        }) {
            Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "")
        }

        Column(
            modifier = Modifier
            .constrainAs(titleText){
                start.linkTo(back.end)
                end.linkTo(otherOptions.start)
                width = Dimension.fillToConstraints
                centerVerticallyTo(parent)
            }
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Start
            )

            subtitle?.let {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = it,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Thin,
                    textAlign = TextAlign.Start,
                    color = Color.LightGray
                )
            }

        }



        Column(modifier = Modifier.constrainAs(otherOptions){
            end.linkTo(parent.end)
        }) {
            content?.let { it() }
        }
    }
}