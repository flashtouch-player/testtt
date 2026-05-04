# MiApp Player — Capacitor + ExoPlayer (Media3)

APK Android con reproductor nativo ExoPlayer/Media3, soporte HLS/DASH/DRM Widevine, Picture-in-Picture y firma automática vía GitHub Actions.

---

## 🚀 Compilación en GitHub (sin instalar nada)

### 1. Personaliza tu app
Edita las primeras líneas de `.github/workflows/build-apk.yml`:
```yaml
env:
  APP_NAME:     "MiApp Player"      # Nombre visible en el teléfono
  APP_ID:       "com.miapp.player"  # Package ID único
  VERSION_NAME: "1.0.0"             # Versión legible
  VERSION_CODE: "1"                 # Entero incremental (sube 1 en cada release)
```

### 2. Agrega tu icono (opcional)
Pon un archivo `icon.png` (1024×1024 px) en la raíz del proyecto.
Si no hay `icon.png` se genera un placeholder automáticamente.

### 3. Pon tu HTML
Reemplaza `www/index.html` con tu propio HTML. Mantén `www/exoplayer-bridge.js`.

### 4. Crea el repositorio en GitHub
```
https://github.com/new
```
Sube todos los archivos de esta carpeta al repo.

### 5. El APK se compila solo
La pestaña **Actions** → workflow **Build APK** → cuando termine → sección **Artifacts** → descarga `MiApp-v1.0.0-signed`.

---

## 🔐 Firma con tu propio keystore (recomendado para producción)

**Primera vez** (sin keystore propio):
El workflow genera un keystore automáticamente y lo sube como artifact `release-keystore-GUARDAR-ESTE-ARCHIVO`.
**Descárgalo y guárdalo en un lugar seguro** — lo necesitarás para todas las actualizaciones futuras.

**Para usar tu propio keystore** (o el generado):

1. Genera el base64 del keystore:
   ```bash
   cat release.keystore | base64 -w 0
   ```

2. Ve a tu repo en GitHub → **Settings** → **Secrets and variables** → **Actions** → **New repository secret**

   | Secret | Valor |
   |--------|-------|
   | `KEYSTORE_BASE64` | El base64 del paso anterior |
   | `KEYSTORE_PASSWORD` | Contraseña del keystore |
   | `KEY_ALIAS` | Alias (ej: `miapp`) |
   | `KEY_PASSWORD` | Contraseña de la llave |

3. El siguiente push usará tu keystore automáticamente.

> ⚠️ **Importante**: si publicas en Play Store, siempre debes firmar con el mismo keystore. Si lo pierdes, no podrás actualizar tu app.

---

## 🎬 Usar el reproductor desde tu HTML

```html
<script type="module">
  import { ExoPlayer } from './exoplayer-bridge.js';

  // Reproducir stream HLS
  ExoPlayer.play({
    url:   'https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8',
    title: 'Mi canal',
    type:  'hls',          // 'hls' | 'dash' | 'progressive' | 'auto'
  });

  // Con DRM Widevine
  ExoPlayer.play({
    url:           'https://ejemplo.com/stream.mpd',
    type:          'dash',
    drmLicenseUrl: 'https://license.server.com/widevine',
    drmScheme:     'widevine',
  });
</script>

<button onclick="ExoPlayer.enterPip()">PiP</button>
<button onclick="ExoPlayer.pause()">Pausa</button>
<button onclick="ExoPlayer.resume()">Reanudar</button>
<button onclick="ExoPlayer.stop()">Detener</button>
```

En el navegador hace fallback a `<video>` para pruebas. En el APK abre el reproductor nativo.

---

## 📱 Instalar el APK en tu teléfono

1. En Android: **Ajustes → Seguridad → Fuentes desconocidas** → Activar
2. Transfiere el `.apk` al teléfono y ábrelo

---

## 🔢 Subir versión en cada release

Opción A — edita `VERSION_NAME` y `VERSION_CODE` en el workflow y haz push.

Opción B — ejecuta el workflow manualmente:
GitHub → Actions → Build APK → **Run workflow** → ingresa la versión.

---

## 🛠️ Compilación local (opcional)

```bash
npm install
npx cap add android
# Replica manualmente los pasos del workflow para integrar ExoPlayer
npx cap sync android
cd android && ./gradlew assembleDebug
# APK en: android/app/build/outputs/apk/debug/app-debug.apk
```
