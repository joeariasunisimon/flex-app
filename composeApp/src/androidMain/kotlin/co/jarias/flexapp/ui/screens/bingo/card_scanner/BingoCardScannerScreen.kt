package co.jarias.flexapp.ui.screens.bingo.card_scanner

import android.app.Activity
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview as ComposePreview
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import co.jarias.flexapp.R
import co.jarias.flexapp.ui.navigation.NavigationEvent
import co.jarias.flexapp.ui.theme.FlexAppTheme
import co.jarias.flexapp.ui.theme.PrimaryTeal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingoCardScannerScreen(
    onNavigate: (NavigationEvent) -> Unit,
    onEvent: (BingoCardScannerScreenEvents) -> Unit,
    state: BingoCardScannerScreenState,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val parser = remember { BingoCardParser() }
    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }

    val scannerOptions = remember {
        GmsDocumentScannerOptions.Builder()
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .setGalleryImportAllowed(false)
            .setPageLimit(1)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
            .build()
    }
    val scanner = remember { GmsDocumentScanning.getClient(scannerOptions) }

    val scanLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            val imageUri = scanResult?.pages?.firstOrNull()?.imageUri

            if (imageUri != null) {
                scope.launch {
                    try {
                        val bitmap = withContext(Dispatchers.IO) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                val source = ImageDecoder.createSource(context.contentResolver, imageUri)
                                ImageDecoder.decodeBitmap(source)
                            } else {
                                @Suppress("DEPRECATION")
                                MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
                            }
                        }

                        val inputImage = InputImage.fromBitmap(bitmap, 0)
                        val visionText = suspendCancellableCoroutine { continuation ->
                            recognizer.process(inputImage)
                                .addOnSuccessListener { result -> continuation.resume(result) }
                                .addOnFailureListener { e -> continuation.resumeWithException(e) }
                        }
                        val parseResult = parser.parse(visionText)

                        when (parseResult) {
                            is ParseResult.Success ->
                                onEvent(BingoCardScannerScreenEvents.OnNumbersDetected(parseResult.grid))
                            is ParseResult.InvalidRange ->
                                onEvent(BingoCardScannerScreenEvents.OnScanFailed(
                                    "Number ${parseResult.number} is in the wrong column (${parseResult.column}). Should be: B(1-15), I(16-30), N(31-45), G(46-60), O(61-75)."
                                ))
                            is ParseResult.Incomplete ->
                                onEvent(BingoCardScannerScreenEvents.OnScanFailed(
                                    "Could not detect a complete 5x5 Bingo card. Make sure all 24 numbers are clearly visible."
                                ))
                        }
                    } catch (e: Exception) {
                        onEvent(BingoCardScannerScreenEvents.OnScanFailed(
                            "Scan failed: ${e.message ?: "Unknown error"}"
                        ))
                    }
                }
            } else {
                onEvent(BingoCardScannerScreenEvents.OnScanFailed("No image captured. Please try again."))
            }
        }
        // else: user cancelled — do nothing
    }

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
                    Text(text = "Try Again", style = MaterialTheme.typography.titleMedium)
                }
            }
        )
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        if (state.detectedGrid != null) {
            // Result display
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Card Detected!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                BingoGridDisplay(grid = state.detectedGrid)

                Spacer(modifier = Modifier.weight(1f))

                if (state.errorMessage != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = state.errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
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
                        enabled = !state.isProcessing,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                    ) {
                        if (state.isProcessing) {
                            ScannerProgressIndicator(size = 24.dp)
                        } else {
                            Text(text = "Confirm & Next", style = MaterialTheme.typography.titleSmall)
                        }
                    }
                }
            }
        } else {
            // Scan prompt
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_photo_camera_24),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = PrimaryTeal
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Scan Your Bingo Card",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Position the camera over a 5×5 Bingo card.\nThe scanner will automatically crop and enhance the image.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        val activity = context as Activity
                        scanner.getStartScanIntent(activity)
                            .addOnSuccessListener { intentSender ->
                                scanLauncher.launch(
                                    IntentSenderRequest.Builder(intentSender).build()
                                )
                            }
                            .addOnFailureListener { e ->
                                onEvent(BingoCardScannerScreenEvents.OnScanFailed(
                                    "Could not start scanner: ${e.message}"
                                ))
                            }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 32.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryTeal,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_photo_camera_24),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Scan Card",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                if (state.isProcessing) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(color = PrimaryTeal)
                }
            }
        }
    }
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

@ComposePreview(showBackground = true, name = "Scan Prompt", showSystemUi = true)
@Composable
fun BingoCardScannerScreenPromptPreview() {
    FlexAppTheme {
        BingoCardScannerScreen(
            onNavigate = {},
            onEvent = {},
            state = BingoCardScannerScreenState(gameId = 1L)
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
                scanErrorMessage = "Could not detect a complete 5x5 Bingo card."
            )
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
            )
        )
    }
}
