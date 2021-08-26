package uz.dev.composeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.animateScrollBy
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
import androidx.compose.ui.layout.onRelocationRequest
import androidx.compose.ui.layout.relocationRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.launch
import uz.dev.composeapp.ui.theme.ComposeAppTheme
import kotlin.math.abs

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      ComposeAppTheme {
        BringPartOfComposableIntoViewSample()
      }
    }
  }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BringRectangleIntoViewDemo() {
  with(LocalDensity.current) {
    val relocationRequester = remember { RelocationRequester() }
    val coroutineScope = rememberCoroutineScope()
    Column {
      Text(
        "This is a scrollable Box. Drag to scroll the Circle into view or click the " +
              "button to bring the circle into view."
      )
      Box(
        Modifier
          .border(2.dp, Black)
          .size(500f.toDp())
          .horizontalScrollWithRelocation(rememberScrollState())
      ) {
        Canvas(
          Modifier
            .size(1500f.toDp(), 500f.toDp())
            .relocationRequester(relocationRequester)
        ) {
          drawCircle(color = Red, radius = 250f, center = Offset(750f, 250f))
        }
      }
      Button(
        onClick = {
          val circleCoordinates = Rect(500f, 0f, 1000f, 500f)
          coroutineScope.launch {
            relocationRequester.bringIntoView(circleCoordinates)
          }
        }
      ) {
        Text("Bring circle into View")
      }
    }
  }
}


@OptIn(ExperimentalComposeUiApi::class)
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

// This is a helper function that users will have to use since experimental "ui" API cannot be used
// inside Scrollable, which is ihe "foundation" package. After onRelocationRequest is added
// to Scrollable, users can use Modifier.horizontalScroll directly.
@OptIn(ExperimentalComposeUiApi::class)
private fun Modifier.horizontalScrollWithRelocation(
  state: ScrollState,
  enabled: Boolean = true,
  flingBehavior: FlingBehavior? = null,
  reverseScrolling: Boolean = false
): Modifier {
  return this
    .onRelocationRequest(
      onProvideDestination = { rect, layoutCoordinates ->
        val size = layoutCoordinates.size.toSize()
        rect.translate(relocationDistance(rect.left, rect.right, size.width), 0f)
      },
      onPerformRelocation = { source, destination ->
        val offset = destination.left - source.left
        state.animateScrollBy(if (reverseScrolling) -offset else offset)
      }
    )
    .horizontalScroll(state, enabled, flingBehavior, reverseScrolling)
}

// Calculate the offset needed to bring one of the edges into view. The leadingEdge is the side
// closest to the origin (For the x-axis this is 'left', for the y-axis this is 'top').
// The trailing edge is the other side (For the x-axis this is 'right', for the y-axis this is
// 'bottom').
private fun relocationDistance(leadingEdge: Float, trailingEdge: Float, parentSize: Float) = when {
  // If the item is already visible, no need to scroll.
  leadingEdge >= 0 && trailingEdge <= parentSize -> 0f

  // If the item is visible but larger than the parent, we don't scroll.
  leadingEdge < 0 && trailingEdge > parentSize -> 0f

  // Find the minimum scroll needed to make one of the edges coincide with the parent's edge.
  abs(leadingEdge) < abs(trailingEdge - parentSize) -> leadingEdge
  else -> trailingEdge - parentSize
}
