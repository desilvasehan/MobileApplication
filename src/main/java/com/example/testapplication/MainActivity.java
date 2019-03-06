package com.example.testapplication;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    Button btn1,btn2;
    TextView txt1, txt2;
    SmsManager smsManager = SmsManager.getDefault();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt1 = (EditText) findViewById(R.id.txtphnumber);
        txt2 = (EditText) findViewById(R.id.txtbody);
        btn1 = (Button) findViewById(R.id.btnsend);
        btn2 = (Button)findViewById(R.id.btnsearch);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                 String number = (String)txt1.getText().toString();
                 String body = (String) txt2.getText().toString();
                 sendSMS();
                 smsManager.sendTextMessage(number, null, body, null, null);
                }
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, 3);
                onActivityResult(3,4,intent);
            }
        });


    }

    public void EnableRuntimePermission(){
        String[] permissions = {
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.SEND_SMS
        };
        if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.SEND_SMS)){
            Toast.makeText(MainActivity.this,"CONTACTS permission allows us to Access Contacts app",Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.SEND_SMS}, 2);
        }

    }


    @Override
    protected void onActivityResult(int reqCode,int resultCode,Intent data){
        super.onActivityResult(reqCode,resultCode,data);
        if(reqCode ==3){
            if(resultCode == Activity.RESULT_OK){
                Cursor cursor1,cursor2;
                String TempNameHolder ,TempNumberHolder,TempContactID,IDresult="";
                int IDresultHolder;
                Uri contactData = data.getData();
                cursor1 = getContentResolver().query(contactData,null,null,null,null);

                if(cursor1.moveToFirst()){
                    TempNameHolder = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    TempContactID = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts._ID));
                    IDresult = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                    IDresultHolder = Integer.valueOf(IDresult);
                    //Toast.makeText(getApplicationContext(), TempNameHolder, Toast.LENGTH_SHORT).show();

                    if(IDresultHolder == 1) {
                        cursor2 = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + TempContactID,
                                null,
                                null);
                        if (cursor2.getCount() > 1) {
                            final String[] numbers = new String[cursor2.getCount()];
                            int count = 0;
                            while (cursor2.moveToNext()) {
                                TempNumberHolder = cursor2.getString(cursor2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                numbers[count] = TempNumberHolder;
                                count++;
                            }

                            AlertDialog.Builder Builder = new AlertDialog.Builder(this);
                            Builder.setTitle("Choose Number");
                            Builder.setItems(numbers, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    txt1.setText(numbers[which]);
                                }
                            });
                            Builder.create();
                            Builder.show();
                        }
                    }
                }
            }
        }
    }

    private void sendSMS(){
        String textSMS = "Hello";

        SubscriptionManager localSubscriptionManager = SubscriptionManager.from(getApplicationContext());

        if(ActivityCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.READ_PHONE_STATE)==PackageManager.PERMISSION_GRANTED)
            if (localSubscriptionManager.getActiveSubscriptionInfoCount()>1){
                List localList = localSubscriptionManager.getActiveSubscriptionInfoList();
                SubscriptionInfo simInfo1 = (SubscriptionInfo) localList.get(0);
                SubscriptionInfo simInfo2 = (SubscriptionInfo) localList.get(1);

                SmsManager.getSmsManagerForSubscriptionId(simInfo2.getSubscriptionId()).sendTextMessage(txt1.getText().toString(),null,txt2.getText().toString(),null,null);
            }
    }


}

