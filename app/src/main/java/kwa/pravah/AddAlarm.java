package kwa.pravah;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;


import kwa.pravah.database.DbManager;

public class AddAlarm extends AppCompatActivity {
    private String POWERON="ON";
    private String PUMPOFF="OFF";
    private String intent_off="000";
    private String time_off="000";
    private static final int CONTACT_PICK = 1;

    String PhoneNo,Name ;
    ImageButton cntct;
    TimePicker setTime;
    Button bt_ON,bt_OFF;
    EditText Phone;
    private DbManager db;
    boolean mFlag;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);

        final Context context = getApplicationContext();
        db=new DbManager(context);
        final int mValue = db.numOfRows();
        if (mValue == 0) {
            mFlag = true;
        } else {
            mFlag = false;
        }


        bt_ON=findViewById(R.id.ON);
        bt_OFF=findViewById(R.id.OFF);
        cntct = findViewById(R.id.Contact);
        Phone =findViewById(R.id.Phone);

        final Calendar now=Calendar.getInstance();
        setTime=findViewById(R.id.PickTime);
        setTime.setCurrentHour(now.get(Calendar.HOUR_OF_DAY));
        setTime.setCurrentMinute(now.get(Calendar.MINUTE));



        try {
            bt_ON.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {



                    Calendar cal = Calendar.getInstance();

                    cal.set(Calendar.HOUR_OF_DAY, setTime.getCurrentHour());
                    cal.set(Calendar.MINUTE, setTime.getCurrentMinute());
                    cal.set(Calendar.SECOND,00);


                    if (cal.compareTo(now) <= 0) {
                        //Today Set time passed, count to tomorrow
                        cal.add(Calendar.DATE, 1);
                    }
                    String time=cal.getTime().toString();
                    time=time.substring(11,19) ;
                    Toast.makeText(context, "Alarm is set @" + time, Toast.LENGTH_SHORT).show();

                    AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);


                    Intent myIntent = new Intent(context, AlarmReceiver.class);

                    String num = Phone.getText().toString();
                    String PhNo = num+",1";
                    myIntent.putExtra("Number", PhNo);

                    int alarmID = (int) cal.getTimeInMillis();
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarmID, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);


                    assert manager != null;
                    //manager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);

                    manager.setRepeating(AlarmManager.RTC_WAKEUP,cal.getTimeInMillis(),AlarmManager.INTERVAL_DAY,pendingIntent);

                    Toast.makeText(context, "Shift set", Toast.LENGTH_SHORT).show();
                    String alarmID_to_on= String.valueOf(alarmID);


                    if(mFlag) {


                        db.insertUserDetails(num,Name,POWERON , PUMPOFF, alarmID_to_on,"", "", "", "", "", intent_off,time,time_off);
                    }
                    else
                    {
                        if (db.getnumber(num) == true) {

                            Cursor cursor=db.getPendingIntent(num);
                            if(cursor.getCount()!=0) {

                                cursor.moveToFirst();
                                String Pending_intent_to_on = cursor.getString(cursor.getColumnIndex(db.PENDING_INTENT_ON));

                                cancelAlarm(Pending_intent_to_on);



                            }

                            db.addPendingIntent_ON(num, alarmID_to_on);
                            db.addTime_ON(num,time);
                        } else {
                            db.insertUserDetails(num,Name, POWERON, PUMPOFF, alarmID_to_on,"", "", "", "", "", intent_off,time,time_off);
                        }

                    }
                }
            });

            bt_OFF.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {



                    String num=Phone.getText().toString();
                    Calendar cal = Calendar.getInstance();

                    if (db.getnumber(num))
                    {
                        cal.set(Calendar.HOUR_OF_DAY, setTime.getCurrentHour());
                        cal.set(Calendar.MINUTE, setTime.getCurrentMinute());
                        cal.set(Calendar.SECOND,00);

                        if (cal.compareTo(now) <= 0) {
                            //Today Set time passed, count to tomorrow
                            cal.add(Calendar.DATE, 1);
                        }

                        String time=cal.getTime().toString();
                        time=time.substring(11,19) ;
                        Toast.makeText(context, "Alarm is set @" + time, Toast.LENGTH_SHORT).show();


                        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);


                        Intent myIntent = new Intent(context, AlarmReceiver.class);


                        String PhNo = num+",2";
                        myIntent.putExtra("Number", PhNo);

                        int alarmID = (int) cal.getTimeInMillis();
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarmID, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);


                        assert manager != null;
                        manager.setRepeating(AlarmManager.RTC_WAKEUP,cal.getTimeInMillis(),AlarmManager.INTERVAL_DAY,pendingIntent);


                        Toast.makeText(context, "Shift set", Toast.LENGTH_SHORT).show();

                        String alarmID_to_off= String.valueOf(alarmID);


                        Cursor cursor=db.getPendingIntent(num);
                        if(cursor.getCount()!=0) {

                            cursor.moveToFirst();
                            String Pending_intent_to_off = cursor.getString(cursor.getColumnIndex(db.PENDING_INTENT_OFF));

                            cancelAlarm(Pending_intent_to_off);


                        }

                        db.addPendingIntent_OFF(num,alarmID_to_off);
                        db.addTime_OFF(num,time);
                    }
                    else {
                        Toast.makeText(context, "First set time to switch on the pump", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }
        catch (NullPointerException e) {
            Toast.makeText(this, "Null value", Toast.LENGTH_SHORT).show();
        }


    }
    public void contactPickerOnClick(View view) {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(contactPickerIntent, CONTACT_PICK);

    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (resultCode == RESULT_OK) {
            // Check for the request code, we might be usign multiple startActivityForReslut
            switch (requestCode) {
                case CONTACT_PICK:
                    contactPicked(data);
                    break;
            }
        } else {
            Log.e("MainActivity", "Failed to pick contact");
        }
    }


    private void contactPicked(Intent data) {
        Cursor cursor = null;
        try {
            String name = null;
            // getData() method will have the Content Uri of the selected contact
            Uri uri = data.getData();
            //Query the content uri
            cursor = getContentResolver().query(uri, null, null, null, null);
            cursor.moveToFirst();
            // column index of the phone number
            int  phoneIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            // column index of the contact name
            int  nameIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            PhoneNo = cursor.getString(phoneIndex);
            Name = cursor.getString(nameIndex);
            // Set the value to the textviews
            //textView1.setText(name);
            if(PhoneNo.length()==13)
                PhoneNo=PhoneNo.substring(3,13);
            Phone.setText(PhoneNo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cancelAlarm(String pndIntent)
    {
        AlarmManager aManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        PendingIntent pIntent = PendingIntent.getBroadcast(getApplicationContext(),
                Integer.parseInt(pndIntent),intent,0);
        aManager.cancel(pIntent);
    }

}
