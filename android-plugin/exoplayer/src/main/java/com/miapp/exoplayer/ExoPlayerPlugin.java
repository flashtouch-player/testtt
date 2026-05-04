package com.miapp.exoplayer;

import android.content.Intent;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "ExoPlayer")
public class ExoPlayerPlugin extends Plugin {

    @PluginMethod
    public void play(PluginCall call) {
        String url = call.getString("url");
        if (url == null || url.isEmpty()) {
            call.reject("Falta 'url'");
            return;
        }
        Intent i = new Intent(getContext(), PlayerActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra("url", url);
        i.putExtra("title", call.getString("title", ""));
        i.putExtra("type", call.getString("type", "auto"));            // hls|dash|progressive|auto
        i.putExtra("drmLicenseUrl", call.getString("drmLicenseUrl", ""));
        i.putExtra("drmScheme", call.getString("drmScheme", ""));      // widevine|playready
        getContext().startActivity(i);

        JSObject ret = new JSObject();
        ret.put("started", true);
        call.resolve(ret);
    }

    @PluginMethod
    public void stop(PluginCall call) {
        Intent i = new Intent("com.miapp.exoplayer.STOP");
        getContext().sendBroadcast(i);
        call.resolve();
    }

    @PluginMethod public void pause(PluginCall call)    { broadcast("PAUSE");  call.resolve(); }
    @PluginMethod public void resume(PluginCall call)   { broadcast("RESUME"); call.resolve(); }
    @PluginMethod public void enterPip(PluginCall call) { broadcast("PIP");    call.resolve(); }

    private void broadcast(String action){
        Intent i = new Intent("com.miapp.exoplayer." + action);
        getContext().sendBroadcast(i);
    }
}
