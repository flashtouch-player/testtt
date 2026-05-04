// Puente JS → plugin nativo Capacitor ExoPlayer
// Funciona en APK; en navegador hace fallback a <video> para poder probar.
const isNative = !!(window.Capacitor && window.Capacitor.isNativePlatform && window.Capacitor.isNativePlatform());

function nativeCall(method, options = {}) {
  if (!isNative) {
    console.warn(`[ExoPlayer] ${method} llamado fuera de APK — fallback web`);
    if (method === 'play' && options.url) {
      let v = document.getElementById('__fallbackVideo');
      if (!v) {
        v = document.createElement('video');
        v.id = '__fallbackVideo';
        v.controls = true;
        v.style.cssText = 'position:fixed;inset:0;width:100%;height:100%;background:#000;z-index:9999';
        document.body.appendChild(v);
      }
      v.src = options.url;
      v.play();
    }
    if (method === 'stop') {
      const v = document.getElementById('__fallbackVideo');
      if (v) { v.pause(); v.remove(); }
    }
    return Promise.resolve();
  }
  return window.Capacitor.Plugins.ExoPlayer[method](options);
}

export const ExoPlayer = {
  play:   (opts) => nativeCall('play', opts),
  stop:   ()     => nativeCall('stop'),
  pause:  ()     => nativeCall('pause'),
  resume: ()     => nativeCall('resume'),
  enterPip: ()   => nativeCall('enterPip'),
};
