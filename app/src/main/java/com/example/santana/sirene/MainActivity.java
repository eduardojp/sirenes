package com.example.santana.sirene;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.AsyncTask;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;

public class MainActivity extends Activity {

    private MainActivity context;
    private TextView returnedText;
    private ToggleButton toggleButton;
    private ProgressBar progressBar;
    private Intent recognizerIntent;
    private String LOG_TAG = "MainActivity";

    private static final String TAG = MainActivity.class.getSimpleName();



    //Properties (AsyncTask)
    protected TextView _percentField;
    protected InitTask _initTask;

    //Properties (MIC)
    public AudioRecord audioRecord;
    public int mSamplesRead; //how many samples read
    public int recordingState;
    public int buffersizebytes;
    public int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    public int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    public static short[] buffer; //+-32767
    public static double[] bufferDouble;
    public static final int SAMPPERSEC = 44100; //samp per sec 8000, 11025, 22050 44100 or 48000
    public static final float TIMEINTERVAL = 0.47f;
    public static final float TIMESPAN = 10.0e-3f;
    public static final float LAGSPAN = 0.25f*TIMESPAN;
    private static String PERMISSIONTAG = "PermissionDemo";
    private static final int RECORD_REQUEST_CODE = 101;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        context = this;

        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(PERMISSIONTAG, "Permission to record denied");
            makeRequest();
        }

        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        toggleButton = (ToggleButton) findViewById(R.id.toggleButton1);

        progressBar.setVisibility(View.INVISIBLE);

        toggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setIndeterminate(true);
                    //speech.startListening(recognizerIntent);
                } else {
                    progressBar.setIndeterminate(false);
                    progressBar.setVisibility(View.INVISIBLE);
                    //speech.stopListening();
                }
            }
        });


        _percentField = ( TextView ) findViewById(R.id.textView1);



        buffersizebytes = AudioRecord.getMinBufferSize(SAMPPERSEC, channelConfiguration, audioEncoding); //4096 on ion
        buffer = new short[Config.FRAMESPERDECISION*Config.SAMPLESPERFRAME];
        //bufferDouble = new double[buffersizebytes];

        audioRecord = new AudioRecord(
            android.media.MediaRecorder.AudioSource.MIC,SAMPPERSEC,channelConfiguration,audioEncoding,2*Config.FRAMESPERDECISION*441
        ); //constructor



        _initTask = new InitTask();
        _initTask.execute( this );

    }

    protected void makeRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                RECORD_REQUEST_CODE);
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        /*if (speech != null) {
            speech.destroy();
            Log.i(LOG_TAG, "destroy");
        }*/
    }

    /*@Override
    public void onDestroy() {
        super.onDestroy();
        if (speech != null) {
            speech.destroy();
            Log.i(LOG_TAG, "destroy");
        }
    }*/

    /**
     * sub-class of AsyncTask
     */
    protected class InitTask extends AsyncTask<Context, Integer, String> {
        // -- run intensive processes here
        // -- notice that the datatype of the first param in the class definition matches the param passed to this method
        // -- and that the datatype of the last param in the class definition matches the return type of this method
        @Override
        protected String doInBackground( Context... params ) {
            //-- on every iteration
            //-- runs a while loop that causes the thread to sleep for 50 milliseconds
            //-- publishes the progress - calls the onProgressUpdate handler defined below
            //-- and increments the counter variable i by one
            //int i = 0;

            audioRecord.startRecording();

            Neural neuralNetwork = null;
            try {
                double[][] inputWeights = Utils.readMatrixFromXML(getResources().getStringArray(R.array.inputWeights), Config.HIDDENLAYERSIZE, Config.NCEPSTRALCOEFFS);
                double[][] layerWeights = Utils.readMatrixFromXML(getResources().getStringArray(R.array.layerWeights), 2, Config.HIDDENLAYERSIZE);
                double[][] bias1 = Utils.readMatrixFromXML(getResources().getStringArray(R.array.bias1), Config.HIDDENLAYERSIZE, 1);
                double[][] bias2 = Utils.readMatrixFromXML(getResources().getStringArray(R.array.bias2), 2, 1);
                double[][] xmin = Utils.readMatrixFromXML(getResources().getStringArray(R.array.xmin), Config.NCEPSTRALCOEFFS, 1);
                double[][] xmax = Utils.readMatrixFromXML(getResources().getStringArray(R.array.xmax), Config.NCEPSTRALCOEFFS, 1);
                neuralNetwork = new Neural(inputWeights, layerWeights, bias1, bias2, xmin, xmax);
            } catch (Exception e) {
                e.printStackTrace();
            }


            AudioProcessor audioProcessor = null;
            try {
                audioProcessor = new AudioProcessor(TIMESPAN, Config.FRAMESPERDECISION, SAMPPERSEC, neuralNetwork);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //double[] inputBuffer = new double[Config.FRAMESPERDECISION*441];
            short[] inputBuffer = new short[Config.FRAMESPERDECISION*Config.SAMPLESPERFRAME];
            /*String[] inputList = getResources().getStringArray(R.array.input);
            for(int k = 0; k < 50*441; k ++)
                inputBuffer[k] = Double.parseDouble(inputList[k]);

            int i = 0;*/

            while( true )
            {
                try{

                    /*for(int totalSamples = 0; totalSamples < Config.FRAMESPERDECISION*441; totalSamples += mSamplesRead) {
                        mSamplesRead = audioRecord.read(buffer, 0, buffersizebytes, AudioRecord.READ_BLOCKING);
                        for (int j = 0; j < mSamplesRead && (j + totalSamples) < Config.FRAMESPERDECISION*441; j++) {
                            inputBuffer[j + totalSamples] = buffer[j];
                        }
                    }*/
                    mSamplesRead = audioRecord.read(buffer, 0, Config.FRAMESPERDECISION*Config.SAMPLESPERFRAME, AudioRecord.READ_BLOCKING);
                    Log.d("mSamplesRead", "" + mSamplesRead);

                    /*for(int k = 0; k < buffer.length; k ++)
                        bufferDouble[k] = buffer[k]/32767.0;*/

                    Boolean result = audioProcessor.processBuffer(buffer, mSamplesRead);

                    if(result != null){
                        int finalResult = result ? 1 : 0;
                        publishProgress(finalResult);
                    }
                    //publishProgress(1);

                    /*for(int aux = 0; aux < mSamplesRead; aux++){
                        amp = (int)buffer[aux];
                        publishProgress( amp );
                    }*/

                } catch( Exception e ){
                    String errorMsg = e.getMessage() + "\n";
                    for(StackTraceElement ste : e.getStackTrace()) {
                        errorMsg += ste.toString() + "\n";
                    }
                    //Log.e(TAG, e.getMessage());
                    Log.e(TAG, errorMsg);
                    System.exit(0);
                }
            }
        }

        // -- gets called just before thread begins
        @Override
        protected void onPreExecute()
        {
            //Log.i( "makemachine", "onPreExecute()" );
            super.onPreExecute();

        }

        // -- called from the publish progress
        // -- notice that the datatype of the second param gets passed to this method
        @Override
        protected void onProgressUpdate(Integer... values)
        {
            super.onProgressUpdate(values);
            //Log.i( "makemachine", "onProgressUpdate(): " +  String.valueOf( values[0] ) );
            _percentField.setText( String.valueOf(values[0]) );
        }

        // -- called as soon as doInBackground method completes
        // -- notice that the third param gets passed to this method
        @Override
        protected void onPostExecute( String result )
        {
            super.onPostExecute(result);
            //Log.i( "makemachine", "onPostExecute(): " + result );
        }


    }
}