package practice.application.graphviewtutorial1;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

//graphview package from https://github.com/jjoe64/GraphView used.
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    //startseries used to record graph
    LineGraphSeries<DataPoint> xseries, yseries, zseries, timeseries;
    List<Float> xlist, ylist, zlist;
    List<Long> timelist;
    Thread graphThread;
    //notStarted used to identify if thread is running
    //stopped used to identify if graph has been cleared and series needs to be added
    Boolean notStarted, stopped;
    GraphView graphx;

    public static String  TABLE_NAME = "AppData";

    EditText name;
    EditText id;
    EditText age;
    RadioGroup rg;

    //setting up accelerometer sensor
    Sensor accelerometer;
    SensorManager sm;

    //accelerometer values
    float x,y,z;
    long time = System.currentTimeMillis();

    private SQLiteDatabase db;
    DatabaseHelper  dbHelp;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notStarted = true;
        stopped = false;

        //set to run in landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        Button run = (Button)findViewById(R.id.button);
        Button stop = (Button)findViewById(R.id.button1);
        final Button download = (Button)findViewById(R.id.download);
        final Button upload = (Button)findViewById(R.id.upload);
        download.setEnabled(false);
        upload.setEnabled(false);

        graphx = (GraphView) findViewById(R.id.graph1);
        graphx.setTitle("X is Red; Y is Yellow, Z is Blue");

        sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer=sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);


        name = (EditText) findViewById(R.id.editText3);
        id = (EditText) findViewById(R.id.editText);
        age = (EditText) findViewById(R.id.editText2);
        rg = (RadioGroup)findViewById(R.id.radioG);

        xseries = new LineGraphSeries();
        yseries = new LineGraphSeries();
        zseries = new LineGraphSeries();
        timeseries = new LineGraphSeries();

        xseries.setColor(Color.RED);
        yseries.setColor(Color.YELLOW);

        graphx.addSeries(xseries);
        graphx.addSeries(yseries);
        graphx.addSeries(zseries);

        //setting axis of graph and scrollability
        Viewport viewport = graphx.getViewport();
        viewport.setXAxisBoundsManual(true);
        viewport.setYAxisBoundsManual(true);
        viewport.setMinX(0);
        viewport.setMaxX(10);
        viewport.setMinY(-15);
        viewport.setMaxY(15);
        viewport.setScrollable(true);


        xlist = new ArrayList<Float>();
        ylist = new ArrayList<Float>();
        zlist = new ArrayList<Float>();
        timelist = new ArrayList<Long>();





        graphThread = new Thread()
        {
            @Override
            public void run() {
                while(true) {

                    if(!notStarted) {
                        //updates to the graph need to be done with the ui thread
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(stopped){
                                    //restore graph if it's been erased
                                    graphx.addSeries(xseries);
                                    graphx.addSeries(yseries);
                                    graphx.addSeries(zseries);
                                    stopped = false;
                                }
                                //add new datapoint: a float take from sensor

                                time = System.currentTimeMillis();
                                xseries.appendData(new DataPoint(xseries.getHighestValueX() + 1, x), true, 10);
                                xlist.add(x);
                                yseries.appendData(new DataPoint(yseries.getHighestValueX() + 1, y), true, 10);
                                ylist.add(y);
                                zseries.appendData(new DataPoint(zseries.getHighestValueX() + 1, z), true, 10);
                                zlist.add(z);
                                timeseries.appendData(new DataPoint(timeseries.getHighestValueX() + 1, time), true, 10);
                                timelist.add(time);

                            }
                        });
                        try {
                            //updates at pace of 1/sec
                            Thread.sleep(1000);

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        graphThread.start();


        run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(notStarted){
                    notStarted = false;
                    download.setEnabled(false);
                    upload.setEnabled(false);
                }
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!notStarted) {
                    notStarted = true;
                    //updates to the graph need to be done with the ui thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //removes the series from graphview, making it empty
                            graphx.removeAllSeries();

                        }
                    });
                    stopped = true;
                    download.setEnabled(true);
                    upload.setEnabled(true);
                }
            }
        });

        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TABLE_NAME = name.getText().toString() + "_" + id.getText().toString() + "_" + age.getText().toString() + "_" + ((RadioButton)findViewById(rg.getCheckedRadioButtonId())).getText().toString();
                new DownloadFromURL(MainActivity.this).execute("https://192.168.1.102/Android/Data/CSE535_ASSIGNMENT2_DOWN");
                DatabaseHelper db = new DatabaseHelper(MainActivity.super.getBaseContext());

                //reset stored values for x, y, and z.
                xseries = new LineGraphSeries<>();
                yseries = new LineGraphSeries<>();
                zseries = new LineGraphSeries<>();
                xseries.setColor(Color.RED);
                yseries.setColor(Color.YELLOW);
                xlist.clear();
                ylist.clear();
                zlist.clear();

                Cursor r;


                try {
                    r = db.getAllData();

                    do {
                        x = r.getFloat(r.getColumnIndex("COL_2"));//second column
                        y = r.getFloat(r.getColumnIndex("COL_3"));//third column
                        z = r.getFloat(r.getColumnIndex("COL_4"));//forth column

                        //replace datapoints for x, y, and z with database's values
                        xseries.appendData(new DataPoint(xseries.getHighestValueX() + 1, x), true, 10);
                        xlist.add(x);
                        yseries.appendData(new DataPoint(yseries.getHighestValueX() + 1, y), true, 10);
                        ylist.add(y);
                        zseries.appendData(new DataPoint(zseries.getHighestValueX() + 1, z), true, 10);
                        zlist.add(z);

                    } while (r.moveToNext());

                } catch (CursorIndexOutOfBoundsException e){

                } catch (SQLiteException e) {

                }

            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //table name pulled from fields
                TABLE_NAME = name.getText().toString() + "_" + id.getText().toString() + "_" + age.getText().toString() + "_" + ((RadioButton)findViewById(rg.getCheckedRadioButtonId())).getText().toString();
                String create_table = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + time + " TIMESTAMP," + xlist + " REAL," + ylist + " REAL," + zlist + " REAL)";
                System.out.println(create_table);


                dbHelp = new DatabaseHelper (MainActivity.super.getBaseContext());

                for (int i=0; i<xlist.size(); i++) {

                    System.out.println(dbHelp.insertData(i, xlist.get(i),ylist.get(i),zlist.get(i)));
                }

                new UploadtoURL(MainActivity.this).execute(MainActivity.super.getBaseContext().getFilesDir().getPath(), "ABaptiste.db", "https://192.168.1.102/UploadToServer_Assignment2.php");


            }
        });

    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //updates x, y, and z values as phone moves.
        x = sensorEvent.values[0];
        y = sensorEvent.values[1];
        z = sensorEvent.values[2];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    //DatabaseHelper class made to manage database with methods specific to the table
    public class DatabaseHelper extends SQLiteOpenHelper {
        public static final String DATABASE_NAME = "ABaptiste.db";
        public String tname = TABLE_NAME;
        public static final String COL_1 = "Time";
        public static final String COL_2 = "X";
        public static final String COL_3 = "Y";
        public static final String COL_4 = "Z";

        public SQLiteDatabase db;

        public DatabaseHelper(Context context) {

            super(context, DATABASE_NAME, null, 1);
            String path = MainActivity.super.getBaseContext().getFilesDir().getPath()+"/"+DATABASE_NAME;
            db = SQLiteDatabase.openOrCreateDatabase(path, null);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            //initialize the table, defining it's columns
            db.execSQL("create table " + tname +" (Time INTEGER PRIMARY KEY AUTOINCREMENT,X REAL,Y REAL,Z REAL)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS "+tname);
            onCreate(db);
        }

        public boolean insertData(int time,Float x,Float y,Float z) {
            //use of autoincriment in time column when making table sets time column to incrimenting count.
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            //contentValues.put(COL_1,time);
            contentValues.put(COL_2,x);
            contentValues.put(COL_3,y);
            contentValues.put(COL_4,z);
            long result = db.insert(tname,null ,contentValues);
            if(result == -1)
                return false;
            else
                return true;
        }

        public Cursor getAllData() {
            SQLiteDatabase db = this.getWritableDatabase();

            //orders table buy time column and retrieves last 10.
            Cursor results = db.rawQuery("select * from "+tname+" order by "+COL_1+" desc limit 10",null);
            return results;
        }

        public boolean updateData(int time,Float x,Float y,Float z) {
            //would be used to update table if time was set differently.
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            //contentValues.put(COL_1,time);
            contentValues.put(COL_2,x);
            contentValues.put(COL_3,y);
            contentValues.put(COL_4,z);
            db.update(tname, contentValues, "Time = ?",new String[] { Integer.toString(time) });
            return true;
        }

        public Integer deleteData (String id) {
            SQLiteDatabase db = this.getWritableDatabase();
            return db.delete(tname, "ID = ?",new String[] {id});
        }
    }
}
