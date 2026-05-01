package co.jarias.flexapp.ui.screens.bingo.card_scanner

import android.Manifest
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview as ComposePreview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import co.jarias.flexapp.R
import co.jarias.flexapp.ui.navigation.NavigationEvent
import co.jarias.flexapp.ui.theme.FlexAppTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BingoCardScannerScreen(
    onNavigate: (NavigationEvent) -> Unit,
    onEvent: (BingoCardScannerScreenEvents) -> Unit,
    state: BingoCardScannerScreenState
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(state.cardSaved) {
        if (state.cardSaved) {
            onNavigate(NavigationEvent.NavigateToBingoFigureSelection(state.gameId))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Scan Bingo Card",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigate(NavigationEvent.OnNavigateUp) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.outline_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (cameraPermissionState.status.isGranted) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                if (state.detectedGrid == null) {
                    CameraPreview(onGridDetected = { grid ->
                        onEvent(BingoCardScannerScreenEvents.OnNumbersDetected(grid))
                    })

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Overlay guide for the card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .background(Color.Transparent)
                                .padding(16.dp)
                                .background(Color.White.copy(alpha = 0.1f))
                        )
                    }

                    Text(
                        text = "Align the Bingo card within the square",
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 64.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Card Detected!",
                            style = MaterialTheme.typography.headlineSmall,
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
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "Retry", style = MaterialTheme.typography.titleSmall)
                            }

                            Button(
                                onClick = { onEvent(BingoCardScannerScreenEvents.OnConfirmSave) },
                                modifier = Modifier.weight(1f),
                                enabled = !state.isProcessing
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
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Camera permission is required to scan the card.")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                    Text("Grant Permission")
                }
            }
        }
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun CameraPreview(onGridDetected: (List<List<String>>) -> Unit) {
    if (LocalInspectionMode.current) {
        // Show a placeholder in Previews to avoid CameraX initialization crash
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

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val parser = remember { BingoCardParser() }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(executor) { imageProxy ->
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
                                        val grid = parser.parse(visionText)
                                        if (grid != null) {
                                            onGridDetected(grid)
                                        }
                                    }
                                    .addOnCompleteListener {
                                        imageProxy.close()
                                    }
                            } else {
                                imageProxy.close()
                            }
                        }
                    }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (exc: Exception) {
                    Log.e("BingoScanner", "Use case binding failed", exc)
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun BingoGridDisplay(grid: List<List<String>>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color.White)
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
                            fontWeight = FontWeight.Bold
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
        modifier = Modifier.size(size)
    )
}

@ComposePreview(showBackground = true)
@Composable
fun BingoCardScannerScreenScanningPreview() {
    FlexAppTheme {
        BingoCardScannerScreen(
            onNavigate = {},
            onEvent = {},
            state = BingoCardScannerScreenState(
                gameId = 1L,
                detectedGrid = null
            )
        )
    }
}

@ComposePreview(showBackground = true)
@Composable
fun BingoCardScannerScreenDetectedPreview() {
    val dummyGrid = listOf(
        listOf("1", "16", "31", "46", "61"),
        listOf("2", "17", "32", "47", "62"),
        listOf("3", "18", "FREE", "48", "63"),
        listOf("4", "19", "34", "49", "64"),
        listOf("5", "20", "35", "50", "65")
    )
    FlexAppTheme {
        BingoCardScannerScreen(
            onNavigate = {},
            onEvent = {},
            state = BingoCardScannerScreenState(
                gameId = 1L,
                detectedGrid = dummyGrid
            )
        )
    }
}
