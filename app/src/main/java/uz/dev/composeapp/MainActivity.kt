package uz.dev.composeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.graphics.Color.Companion.Cyan
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.graphics.Color.Companion.Magenta
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.Color.Companion.Yellow
import androidx.compose.ui.layout.RelocationRequester
import androidx.compose.ui.layout.relocationRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import uz.dev.composeapp.ui.theme.ComposeAppTheme

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      ComposeAppTheme {
        BringIntoViewDemo()
      }
    }
  }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BringIntoViewDemo() {
  val greenRequester = remember { RelocationRequester() }
  val redRequester = remember { RelocationRequester() }
  val coroutineScope = rememberCoroutineScope()
  Column {
    Column(Modifier.requiredHeight(100.dp).verticalScroll(rememberScrollState())) {
      Row(Modifier.width(300.dp).horizontalScroll(rememberScrollState())) {
        Box(Modifier.background(Blue).size(100.dp))
        Box(Modifier.background(Green).size(100.dp).relocationRequester(greenRequester))
        Box(Modifier.background(Yellow).size(100.dp))
        Box(Modifier.background(Magenta).size(100.dp))
        Box(Modifier.background(Gray).size(100.dp))
        Box(Modifier.background(Black).size(100.dp))
      }
      Row(Modifier.width(300.dp).horizontalScroll(rememberScrollState())) {
        Box(Modifier.background(Black).size(100.dp))
        Box(Modifier.background(Cyan).size(100.dp))
        Box(Modifier.background(DarkGray).size(100.dp))
        Box(Modifier.background(White).size(100.dp))
        Box(Modifier.background(Red).size(100.dp).relocationRequester(redRequester))
        Box(Modifier.background(LightGray).size(100.dp))
      }
    }
    Button(onClick = { coroutineScope.launch { greenRequester.bringIntoView() } }) {
      Text("Bring Green box into view")
    }
    Button(onClick = { coroutineScope.launch { redRequester.bringIntoView() } }) {
      Text("Bring Red box into view")
    }
  }
}


@ExperimentalComposeUiApi
@Composable
fun BringPartOfComposableIntoViewSample() {
  with(LocalDensity.current) {
    val relocationRequester = remember { RelocationRequester() }
    val coroutineScope = rememberCoroutineScope()
    Column {
      Box(
        Modifier
          .border(2.dp, Black)
          .size(500f.toDp())
          .horizontalScroll(rememberScrollState())
      ) {
        Canvas(
          Modifier
            .size(1500f.toDp(), 500f.toDp())
            // This associates the RelocationRequester with a Composable that wants
            // to be brought into view.
            .relocationRequester(relocationRequester)
        ) {
          drawCircle(color = Red, radius = 250f, center = Offset(750f, 250f))
        }
      }
      Button(
        onClick = {
          val circleCoordinates = Rect(500f, 0f, 1000f, 500f)
          coroutineScope.launch {
            // This sends a request to all parents that asks them to scroll so that
            // the circle is brought into view.
            relocationRequester.bringIntoView(circleCoordinates)
          }
        }
      ) {
        Text("Bring circle into View")
      }
    }
  }
}
