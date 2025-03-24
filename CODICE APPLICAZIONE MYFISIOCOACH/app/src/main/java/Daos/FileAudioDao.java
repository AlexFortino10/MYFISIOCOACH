package Daos;

import com.example.myfisiocoach.R;

public class FileAudioDao implements FileAudioInterface{
    @Override
    public int getFileAudioInizio(int numfileaudio) {

       if (numfileaudio == 1){
        return R.raw.frase_iniziamo_1;
       } else if (numfileaudio == 2) {
           return R.raw.frase_iniziamo_2;

       } else if (numfileaudio == 3) {
           return R.raw.frase_iniziamo_3;

       } else if (numfileaudio == 4) {
           return R.raw.frase_iniziamo_4;

       } else if (numfileaudio == 5) {
           return R.raw.frase_iniziamo_5;

       }
         return 0;
    }

    @Override
    public int getFileAudioContinua(int numfileaudio) {
        if (numfileaudio == 1){
            return R.raw.frase_continua_1;
        } else if (numfileaudio == 2) {
            return R.raw.frase_continua_2;

        } else if (numfileaudio == 3) {
            return R.raw.frase_continua_3;

        } else if (numfileaudio == 4) {
            return R.raw.frase_continua_4;

        } else if (numfileaudio == 5) {
            return R.raw.frase_continua_5;

        }
        return 0;
    }

    @Override
    public int getFileAudioFine(int numfileaudio) {
        if (numfileaudio == 1){
            return R.raw.frase_fine_1;
        } else if (numfileaudio == 2) {
            return R.raw.frase_fine_2;

        } else if (numfileaudio == 3) {
            return R.raw.frase_fine_3;

        } else if (numfileaudio == 4) {
            return R.raw.frase_fine_4;

        } else if (numfileaudio == 5) {
            return R.raw.frase_fine_5;

        }
        return 0;
    }
}
