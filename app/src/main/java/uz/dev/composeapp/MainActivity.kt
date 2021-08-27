package uz.dev.composeapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Red
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
        Column {
          TestFocusWithDynamicContent()
        }
      }
    }
  }
}

@Composable
fun TestFocusWithDynamicContent() {
  val list = remember { mutableStateListOf("") }
  val focusRequesters = (list.indices + 1).map { remember(it) { FocusRequester() } }
  Column {
    list.forEachIndexed { index, item ->
      ComposeItem(
        item = item,
        isLastItem = index == list.lastIndex,
        onValueChange = { list[index] = it },
        addNew = list::add,
        focusRequester = focusRequesters[index]
      )
    }
  }
}

@Composable
private fun ComposeItem(
  item: String,
  isLastItem: Boolean,
  onValueChange: (String) -> Unit,
  addNew: (String) -> Unit,
  focusRequester: FocusRequester
) {
  if (!isLastItem) {
    OutlinedTextField(
      value = item,
      onValueChange = {
        onValueChange(it)
      },
      modifier = Modifier
        .fillMaxWidth()
        .focusRequester(focusRequester),
      placeholder = { Text(text = "type anything") }
    )
  } else {
    OutlinedTextField(
      value = "",
      onValueChange = {
        onValueChange(it)
        if (it.isNotEmpty()) addNew("")
      },
      placeholder = { Text(text = "type new item") }
    )
  }
}


@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.relocate(key: Any, rootRect: Rect? = null): Modifier =
  composed {
    val scope = rememberCoroutineScope()
    val relocationRequester = remember(key) { RelocationRequester() }
    this
      .relocationRequester(relocationRequester)
      .onFocusEvent { state ->
        if (state.isFocused) {
          scope.launch {
            relocationRequester.bringIntoView(rootRect)

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
          .horizontalScrollWithRelocation(rememberScrollState())
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
private fun Modifier.horizontalScrollWithRelocation2(
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
    .relocationScrollable(
      scrollableState = state,
      orientation = Orientation.Horizontal,
      reverseDirection = reverseScrolling
    )
    .horizontalScroll(state, enabled, flingBehavior, reverseScrolling)
}

// This is a helper function that users will have to use since experimental "ui" API cannot be used
// inside Scrollable, which is ihe "foundation" package. After onRelocationRequest is added
// to Scrollable, users can use Modifier.horizontalScroll directly.
@OptIn(ExperimentalComposeUiApi::class)
private fun Modifier.verticalScrollWithRelocation(
  state: ScrollState,
  enabled: Boolean = true,
  flingBehavior: FlingBehavior? = null,
  reverseScrolling: Boolean = false
): Modifier {
  return this
    .relocationScrollable(
      scrollableState = state,
      orientation = Orientation.Vertical,
      reverseDirection = reverseScrolling
    )
    .verticalScroll(state, enabled, flingBehavior, reverseScrolling)
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

@OptIn(ExperimentalComposeUiApi::class)
private fun Modifier.relocationScrollable(
  scrollableState: ScrollableState,
  orientation: Orientation,
  reverseDirection: Boolean = false
): Modifier {
  fun Float.reverseIfNeeded(): Float = if (reverseDirection) this * -1 else this
  return this.onRelocationRequest(
    onProvideDestination = { rect, layoutCoordinates ->
      val size = layoutCoordinates.size.toSize()
      when (orientation) {
        Orientation.Vertical ->
          rect.translate(0f, relocationDistance(rect.top, rect.bottom, size.height))
        Orientation.Horizontal ->
          rect.translate(relocationDistance(rect.left, rect.right, size.width), 0f)
      }
    },
    onPerformRelocation = { source, destination ->
      val offset = when (orientation) {
        Orientation.Vertical -> -(source.top - destination.top)
        Orientation.Horizontal -> -(source.left - destination.left)
      }
      scrollableState.animateScrollBy(offset.reverseIfNeeded())
    }
  )
}
