package dam2.jetpack.actividad1_tema4

import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

@Composable
fun SoundPool(){
    val context = LocalContext.current

    val soundPool = remember {
        SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
    }

    var soundId by remember { mutableStateOf(0) }
    var soundLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(soundPool) {
        soundId = soundPool.load(context, R.raw.sonidocorto, 1)
        soundPool.setOnLoadCompleteListener {
                _, sampleId, status ->
            if (status == 0 && sampleId == soundId) {
                soundLoaded = true
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            soundPool.release()
        }
    }

    Button(
        onClick = {
            if (soundLoaded) {
                soundPool.play(
                    soundId,
                    1f, // leftVolume
                    1f, // rightVolume
                    1,  // priority
                    0,  // loop (0 = no loop)
                    1f  // rate (1.0 = normal)
                )
            }
        },
        enabled = soundLoaded
    ) {
        Text(text = if (soundLoaded) "Reproducir sonido" else "Cargando sonido...")
    }
}