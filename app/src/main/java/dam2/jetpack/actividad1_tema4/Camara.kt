import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter

fun createImageUri(context: Context): Uri? {
    val contentResolver = context.contentResolver

    val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    } else {
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }

    val contentValues = ContentValues().apply {
        put(
            MediaStore.Images.Media.DISPLAY_NAME,
            "foto_${System.currentTimeMillis()}.jpg"
        )
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MisFotosApp")
        }
    }

    return contentResolver.insert(collection, contentValues)
}

@Composable
fun CameraCaptureCard() {
    val context = LocalContext.current

    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher para hacer la foto
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            Toast.makeText(context, "Imagen guardada en la galería", Toast.LENGTH_SHORT).show()
        } else {
            imageUri = null
        }
    }

    // Launcher para pedir PERMISO de cámara
    var pendingUri by remember { mutableStateOf<Uri?>(null) }

    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // Si el usuario acepta, lanzamos la cámara con el uri que habíamos preparado
            pendingUri?.let { uri ->
                imageUri = uri
                takePictureLauncher.launch(uri)
            }
        } else {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
        pendingUri = null
    }

    fun openCamera() {
        val uri = createImageUri(context)
        if (uri == null) {
            Toast.makeText(
                context,
                "No se pudo crear el archivo de imagen",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Comprobar permiso de cámara en tiempo de ejecución
        val permissionStatus = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        )

        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            imageUri = uri
            takePictureLauncher.launch(uri)
        } else {
            // Guardamos el uri mientras pedimos el permiso
            pendingUri = uri
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Cámara (guardar en galería)")

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = { openCamera() }
        ) {
            Text("Abrir cámara")
        }

        Spacer(Modifier.height(16.dp))

        imageUri?.let { uri ->
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = "Última foto tomada",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}
