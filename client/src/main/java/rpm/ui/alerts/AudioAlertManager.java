package rpm.ui.alerts;

import javafx.scene.media.AudioClip;
import rpm.ui.app.AlertPreference;
import rpm.ui.app.UISettings;

public final class AudioAlertManager {

    private final UISettings settings;
    private final AudioClip clip;
    private long stopAtMs = 0;

    public AudioAlertManager(UISettings settings) {
        this.settings = settings;

        // Load the alert sound from resources
        java.net.URL url = AudioAlertManager.class.getResource("/rpm/ui/assets/alert2.wav");
        if (url == null) {
            System.err.println("[WARN] alert.wav not found at /rpm/ui/assets/alert2.wav. Audio disabled.");
            this.clip = null;
            return;
        }

        this.clip = new AudioClip(url.toExternalForm());

        // Loop the sound continuously until stopped
        this.clip.setCycleCount(AudioClip.INDEFINITE);
    }

    /*
    - Starts playing the alert sound based on the user's settings.
    - Audio only plays when audio + visual alerts are enabled.
    - Playback duration depends on the configured alert duration.
    */
    public void startFor(long nowMs) {
        if (clip == null) return;
        if (settings.getAlertPreference() != AlertPreference.AUDIO_AND_VISUAL) return;

        java.time.Duration d = settings.getAlertDuration().toDurationOrNull();

        // Start playing if not already active
        if (!clip.isPlaying()) clip.play();

        // Calculate when the sound should stop
        stopAtMs = (d == null) ? Long.MAX_VALUE : (nowMs + d.toMillis());
    }

    /*
    - Called periodically to check whether the audio should stop.
    - Stops playback once the timeout is reached.
    */
    public void tick(long nowMs) {
        if (clip == null) return;
        if (clip.isPlaying() && nowMs >= stopAtMs) clip.stop();
    }

    // Immediately stop the alert sound
    public void stop() {
        if (clip == null) return;
        clip.stop();
        stopAtMs = 0;
    }
}
