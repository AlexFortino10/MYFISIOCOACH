package com.example.myfisiocoach;

import android.content.Context;
import android.media.MediaPlayer;

public class AudioManager {
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;

    // Interfaccia per il callback
    public interface OnSoundCompletionListener {
        void onSoundCompletion();
    }

    private OnSoundCompletionListener completionListener;

    public void setOnSoundCompletionListener(OnSoundCompletionListener listener) {
        this.completionListener = listener;
    }

    public void playSound(Context context, int soundResourceId) {
        if (mediaPlayer != null) {
            mediaPlayer.release(); // Rilascia eventuali risorse precedenti
        }

        mediaPlayer = MediaPlayer.create(context, soundResourceId);
        if (mediaPlayer != null) {
            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false; // Imposta a false quando il suono è completato
                mp.release();  // Rilascia le risorse quando la riproduzione è completata
            });
            mediaPlayer.start();
            isPlaying = true; // Imposta a true quando inizia la riproduzione
        }
    }


    public void stopSound() {
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.stop();
            mediaPlayer.release(); // Rilascia le risorse
            mediaPlayer = null; // Imposta a null dopo aver rilasciato
            isPlaying = false; // Imposta a false
        }
    }

    public boolean isPlaying() {
        return isPlaying;
    }
}