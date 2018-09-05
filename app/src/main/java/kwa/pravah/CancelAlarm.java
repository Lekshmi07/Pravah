package kwa.pravah;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import kwa.pravah.database.DbManager;

public class CancelAlarm extends AppCompatActivity {


    Button cancel;
    EditText Ph;
    DbManager db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancel_alarm);

        db=new DbManager(CancelAlarm.this);


        cancel=findViewById(R.id.Cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Ph=findViewById(R.id.Number);
                String phone=Ph.getText().toString();
                if (db.getnumber(phone))
                {
                    Cursor cursor=db.getPendingIntent(phone);
                    if(cursor.getCount()!=0) {

                        cursor.moveToFirst();
                        String Pending_intent_to_on = cursor.getString(cursor.getColumnIndex(db.PENDING_INTENT_ON));
                        String Pending_intent_to_off = cursor.getString(cursor.getColumnIndex(db.PENDING_INTENT_OFF));

                        cancelAlarm(Pending_intent_to_on);
                        cancelAlarm(Pending_intent_to_off);

                        Ph.setText("");
                        Toast.makeText(CancelAlarm.this, "Alarm cleared", Toast.LENGTH_SHORT).show();
                        db.deleteRow(phone);
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), "No alarms to clear...!!", Toast.LENGTH_SHORT).show();
                }

            }
        });
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
