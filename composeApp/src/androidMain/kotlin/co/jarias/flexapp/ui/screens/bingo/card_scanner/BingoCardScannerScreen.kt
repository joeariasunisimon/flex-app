package co.jarias.flexapp.ui.screens.bingo.card_scanner

import android.Manifest
import android.util.Log
import androidx.camera.core.*
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview as ComposePreview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import co.jarias.flexapp.R
import co.jarias.flexapp.providers.CameraManager
import co.jarias.flexapp.ui.navigation.NavigationEvent
import co.jarias.flexapp.ui.theme.FlexAppTheme
import co.jarias.flexapp.ui.theme.AccentAmber
import co.jarias.flexapp.ui.theme.PrimaryTeal
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BingoCardScannerScreen(
    onNavigate: (NavigationEvent) -> Unit,
    onEvent: (BingoCardScannerScreenEvents) -> Unit,
    state: BingoCardScannerScreenState,
    cameraManager: CameraManager?,
    isPermissionGrantedOverride: Boolean? = null,
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val isPermissionGranted = isPermissionGrantedOverride ?: cameraPermissionState.status.isGranted

    LaunchedEffect(state.cardSaved) {
        if (state.cardSaved) {
            onNavigate(NavigationEvent.NavigateToBingoFigureSelection(state.gameId))
        }
    }

    LaunchedEffect(state.navigateBack) {
        if (state.navigateBack) {
            onNavigate(NavigationEvent.OnNavigateUp)
        }
    }

    if (state.scanErrorMessage != null) {
        AlertDialog(
            onDismissRequest = { onEvent(BingoCardScannerScreenEvents.OnErrorModalDismissed) },
            title = { Text("Invalid Bingo Card") },
            text = { Text(state.scanErrorMessage) },
            confirmButton = {
                TextButton(onClick = { onEvent(BingoCardScannerScreenEvents.OnErrorModalDismissed) }) {
                    Text(text = "Go Back", style = MaterialTheme.typography.titleSmall)
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (isPermissionGranted && (state.detectedGrid == null) && (state.scanErrorMessage == null)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Black
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(bottom = 16.dp)
                    ) {
                        // Action buttons (Flash, Capture, Flip)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp, vertical = 24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CameraControlIcon(
                                iconRes = if (state.isFlashOn) R.drawable.outline_flash_on else R.drawable.outline_flash_off,
                                label = "FLASH"
                            ) { onEvent(BingoCardScannerScreenEvents.OnFlashToggle) }

                            // Capture Button (Central Circle)
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .border(4.dp, Color.White, CircleShape)
                                    .padding(6.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .clickable { onEvent(BingoCardScannerScreenEvents.OnCaptureClicked) }
                            )

                            CameraControlIcon(
                                iconRes = R.drawable.outline_change_circle_24,
                                label = "FLIP"
                            ) { onEvent(BingoCardScannerScreenEvents.OnFlipCamera) }
                        }

                        // Bottom Capture Card Button
                        Button(
                            onClick = { onEvent(BingoCardScannerScreenEvents.OnCaptureClicked) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryTeal,
                                contentColor = Color.White
                            )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.outline_photo_camera_24),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "CAPTURE CARD",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (isPermissionGranted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                if (state.detectedGrid == null) {
                    CameraPreview(
                        cameraManager = cameraManager,
                        isFlashOn = state.isFlashOn,
                        isBackCamera = state.isBackCamera,
                        onResult = { result ->
                            when (result) {
                                is ParseResult.Success -> onEvent(
                                    BingoCardScannerScreenEvents.OnNumbersDetected(
                                        result.grid
                                    )
                                )

                                is ParseResult.InvalidRange -> onEvent(
                                    BingoCardScannerScreenEvents.OnScanFailed(
                                        "Number ${result.number} is in the wrong position for column ${result.column}. Standard Bingo rules: B(1-15), I(16-30), N(31-45), G(46-60), O(61-75)."
                                    )
                                )

                                is ParseResult.Incomplete -> { /* Keep scanning */
                                }
                            }
                        }
                    )

                    // Overlay with cutout and guides
                    ScannerOverlay(
                        modifier = Modifier.fillMaxSize(),
                        text = "Align the Bingo card within the square"
                    )

                    // Top Back Button
                    IconButton(
                        onClick = { onNavigate(NavigationEvent.OnNavigateUp) },
                        modifier = Modifier
                            .padding(16.dp)
                            .statusBarsPadding()
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.outline_arrow_back),
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                } else {
                    // Result display
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Card Detected!",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        BingoGridDisplay(grid = state.detectedGrid)

                        Spacer(modifier = Modifier.weight(1f))

                        if (state.errorMessage != null) {
                            Text(text = state.errorMessage, color = MaterialTheme.colorScheme.error)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onEvent(BingoCardScannerScreenEvents.OnRetry) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = BorderStroke(1.dp, Color.White)
                            ) {
                                Text(text = "Retry", style = MaterialTheme.typography.titleSmall)
                            }

                            Button(
                                onClick = { onEvent(BingoCardScannerScreenEvents.OnConfirmSave) },
                                modifier = Modifier.weight(1f),
                                enabled = !state.isProcessing,
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                            ) {
                                if (state.isProcessing) {
                                    ScannerProgressIndicator(size = 24.dp)
                                } else {
                                    Text(
                                        text = "Confirm & Next",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Permission denied UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp)
                    .background(MaterialTheme.colorScheme.background),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Camera permission is required to scan the card.")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                    Text(text = "Grant Permission", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
fun CameraControlIcon(iconRes: Int, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = label,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun ScannerOverlay(modifier: Modifier = Modifier, text: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserPosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser"
    )

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val boxSize = canvasWidth * 0.8f
            val left = (canvasWidth - boxSize) / 2
            val top = (canvasHeight - boxSize) / 2.5f

            // Dark semi-transparent overlay
            drawRect(
                color = Color.Black.copy(alpha = 0.5f),
                size = size
            )

            // Cutout
            drawRect(
                color = Color.Transparent,
                topLeft = Offset(left, top),
                size = Size(boxSize, boxSize),
                blendMode = BlendMode.Clear
            )

            // Corners (Amber)
            val cornerLength = 40.dp.toPx()
            val strokeWidth = 4.dp.toPx()

            // Top-Left
            drawLine(AccentAmber, Offset(left, top), Offset(left + cornerLength, top), strokeWidth)
            drawLine(AccentAmber, Offset(left, top), Offset(left, top + cornerLength), strokeWidth)

            // Top-Right
            drawLine(
                AccentAmber,
                Offset(left + boxSize, top),
                Offset(left + boxSize - cornerLength, top),
                strokeWidth
            )
            drawLine(
                AccentAmber,
                Offset(left + boxSize, top),
                Offset(left + boxSize, top + cornerLength),
                strokeWidth
            )

            // Bottom-Left
            drawLine(
                AccentAmber,
                Offset(left, top + boxSize),
                Offset(left + cornerLength, top + boxSize),
                strokeWidth
            )
            drawLine(
                AccentAmber,
                Offset(left, top + boxSize),
                Offset(left, top + boxSize - cornerLength),
                strokeWidth
            )

            // Bottom-Right
            drawLine(
                AccentAmber,
                Offset(left + boxSize, top + boxSize),
                Offset(left + boxSize - cornerLength, top + boxSize),
                strokeWidth
            )
            drawLine(
                AccentAmber,
                Offset(left + boxSize, top + boxSize),
                Offset(left + boxSize, top + boxSize - cornerLength),
                strokeWidth
            )

            // Laser Line
            val laserY = top + (boxSize * laserPosition)
            drawLine(
                color = AccentAmber.copy(alpha = 0.6f),
                start = Offset(left + 10.dp.toPx(), laserY),
                end = Offset(left + boxSize - 10.dp.toPx(), laserY),
                strokeWidth = 2.dp.toPx()
            )
        }

        Text(
            text = text,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 180.dp), // Adjust based on square position
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun CameraPreview(
    cameraManager: CameraManager?,
    isFlashOn: Boolean,
    isBackCamera: Boolean,
    onResult: (ParseResult) -> Unit
) {
    if (cameraManager == null || LocalInspectionMode.current) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            Text("Camera Preview Placeholder", color = Color.White)
        }
        return
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val parser = remember { BingoCardParser() }
    val scope = rememberCoroutineScope()

    var camera by remember { mutableStateOf<Camera?>(null) }

    LaunchedEffect(isFlashOn) {
        camera?.cameraControl?.enableTorch(isFlashOn)
    }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx)
        },
        modifier = Modifier.fillMaxSize(),
        update = { previewView ->
            scope.launch {
                val cameraProvider = cameraManager.getCameraProvider()
                val preview = androidx.camera.core.Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraManager.getAnalysisExecutor()) { imageProxy ->
                            val mediaImage = imageProxy.image
                            if (mediaImage != null) {
                                val image = InputImage.fromMediaImage(
                                    mediaImage,
                                    imageProxy.imageInfo.rotationDegrees
                                )
                                val recognizer =
                                    TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                                recognizer.process(image)
                                    .addOnSuccessListener { visionText ->
                                        val result = parser.parse(visionText)
                                        onResult(result)
                                    }
                                    .addOnCompleteListener {
                                        imageProxy.close()
                                    }
                            } else {
                                imageProxy.close()
                            }
                        }
                    }

                val cameraSelector =
                    if (isBackCamera) CameraSelector.DEFAULT_BACK_CAMERA else CameraSelector.DEFAULT_FRONT_CAMERA

                try {
                    camera = cameraManager.bindCamera(
                        lifecycleOwner,
                        cameraProvider,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                    camera?.cameraControl?.enableTorch(isFlashOn)
                } catch (exc: Exception) {
                    Log.e("BingoScanner", "Use case binding failed", exc)
                }
            }
        }
    )
}

@Composable
fun BingoGridDisplay(grid: List<List<String>>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        grid.forEachIndexed { r, row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEachIndexed { c, value ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .background(if (r == 2 && c == 2) Color.LightGray else Color.White)
                            .border(1.dp, Color.Gray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (r == 2 && c == 2) "FREE" else value,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScannerProgressIndicator(size: androidx.compose.ui.unit.Dp) {
    CircularProgressIndicator(
        modifier = Modifier.size(size),
        color = Color.White
    )
}

@ComposePreview(showBackground = true, name = "Scanning State", showSystemUi = true)
@Composable
fun BingoCardScannerScreenScanningPreview() {
    FlexAppTheme {
        BingoCardScannerScreen(
            onNavigate = {},
            onEvent = {},
            state = BingoCardScannerScreenState(
                gameId = 1L,
                detectedGrid = null
            ),
            cameraManager = null,
            isPermissionGrantedOverride = true
        )
    }
}

@ComposePreview(showBackground = true, name = "Error Modal", showSystemUi = true)
@Composable
fun BingoCardScannerScreenErrorPreview() {
    FlexAppTheme {
        BingoCardScannerScreen(
            onNavigate = {},
            onEvent = {},
            state = BingoCardScannerScreenState(
                gameId = 1L,
                scanErrorMessage = "Number 25 is in the wrong position for column B. Standard Bingo rules: B(1-15), I(16-30), N(31-45), G(46-60), O(61-75)."
            ),
            cameraManager = null,
            isPermissionGrantedOverride = true
        )
    }
}

@ComposePreview(showBackground = true, name = "Detected Grid", showSystemUi = true)
@Composable
fun BingoCardScannerScreenDetectedPreview() {
    val dummyGrid = listOf(
        listOf("5", "20", "35", "50", "65"),
        listOf("12", "25", "40", "55", "70"),
        listOf("3", "18", "FREE", "48", "63"),
        listOf("15", "30", "45", "60", "75"),
        listOf("1", "16", "31", "46", "61")
    )
    FlexAppTheme {
        BingoCardScannerScreen(
            onNavigate = {},
            onEvent = {},
            state = BingoCardScannerScreenState(
                gameId = 1L,
                detectedGrid = dummyGrid
            ),
            cameraManager = null,
            isPermissionGrantedOverride = true
        )
    }
}

@ComposePreview(showBackground = true, name = "Scanning State", showSystemUi = true)
@Composable
fun BingoCardScannerScreenNoPermissionPreview() {
    FlexAppTheme {
        BingoCardScannerScreen(
            onNavigate = {},
            onEvent = {},
            state = BingoCardScannerScreenState(
                gameId = 1L,
                detectedGrid = null
            ),
            cameraManager = null,
            isPermissionGrantedOverride = false,
        )
    }
}
