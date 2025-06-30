package test.test.bkoclient_tsd_v02.Manager;

import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import test.test.bkoclient_tsd_v02.R;

public class MessageAlert {

    private static MediaPlayer succesSound, errorSound;

    public static void SuccesMsg(Context _context, String _succesMsg, boolean _showMessage){

        if(succesSound == null)
            succesSound = MediaPlayer.create(_context, R.raw.soundsucces);

        succesSound.start();

        if(_showMessage) {
            AlertDialog.Builder builder = new AlertDialog.Builder(_context);
            builder.setTitle("Данные отправлены!")
                    .setMessage(_succesMsg)
                    .setIcon(R.drawable.icon_succes)
                    .setCancelable(false)
                    .setNegativeButton("ОК",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    public static void InfoMsg(Context _context, String _infoMsg){

        if(succesSound == null)
            succesSound = MediaPlayer.create(_context, R.raw.soundsucces);

        succesSound.start();

        AlertDialog.Builder builder = new AlertDialog.Builder(_context);
        builder.setTitle("Info!")
                .setMessage(_infoMsg)
                .setIcon(R.drawable.icon_info)
                .setCancelable(false)
                .setNegativeButton("ОК",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void ErrorMessage (Context _context, String _errorMsg){

        if(errorSound == null)
            errorSound = MediaPlayer.create(_context, R.raw.sounderror);

        errorSound.start();

        AlertDialog.Builder builder = new AlertDialog.Builder(_context);

        builder.setTitle("Данные не отправлены!")
                .setMessage(_errorMsg)
                .setIcon(R.drawable.icon_error)
                .setCancelable(false)
                .setNegativeButton("ОК",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void ShowToast(Context _context, String _text){
        Toast.makeText(_context,_text, Toast.LENGTH_SHORT).show();
    }

    public static void SoundOK(Context _context){
        if(succesSound == null)
            succesSound = MediaPlayer.create(_context, R.raw.soundsucces);

        succesSound.start();
    }

    public static void SoundError(Context _context){
        if(errorSound == null)
            errorSound = MediaPlayer.create(_context, R.raw.sounderror);

        errorSound.start();
    }
}

