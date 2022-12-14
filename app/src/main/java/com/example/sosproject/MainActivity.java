package com.example.sosproject;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Context;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.response.model.User;
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//1. ???????????? ?????? ??? ?????? ????????? ????????? ??????..?
public class MainActivity extends AppCompatActivity {//extends Calender{
    NfcAdapter nfcAdapter;
    private int ride_or_quit;  // 0?????? nfc ??????: ?????? / 1?????? nfc ??????: ??????
    private int is_tag_mode;  // 0: nfc ?????? ?????? / 1: nfc ?????? ??????

    Station station = new Station();
    String rideStation;
    String quitStation;

    public static String image_profile;
    private ImageButton card;
    private RelativeLayout cardLayout;
    private Animation cardAnim;
    private Animation nfcAnim;
    private Animation handAnim;
    LinearLayout main_activity;
    LinearLayout nfcReader;
    ImageView hand;
    private int cardcounter = 0;
    private int handcounter = 0;
    private DrawerLayout drawerLayout;
    private View drawerView;
    private DrawerLayout drawerLayout2;
    private View drawerView2;

    //DB ?????????
    protected PeopleDBHelper dbHelper;
    private TextView test;
    TextView personal_name;
    static String NAME = KakaoLogin2Activity.strNick;
    static String CHARGE;
    static final String DB_NAME = "personal.db";


    // Retrofit (Spring server ?????????)
    static String p_id;
    static UserInfo p_userInfo;
    static String phoneNumber;
    static String birthday;

    DBHelper mDBHelper; // ??? ???????????? ?????? ????????? ???????????? sqlite DB helper
    ArrayList<BoardingInfo> arrayList;


    long mNow;
    Date mDate;
    SimpleDateFormat mFormat = new SimpleDateFormat("yyyyMMddhhmmss");

    private String getTime() {
        mNow = System.currentTimeMillis();
        mDate = new Date(mNow);
        return mFormat.format(mDate);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        Intent intent2 = getIntent();
        NAME = intent2.getStringExtra("name");
        phoneNumber = intent2.getStringExtra("phone");
        birthday = intent2.getStringExtra("birth");
        image_profile = intent2.getStringExtra("profileImg");
        p_id = phoneNumber + birthday;

//        // NFC??? ???????????? ?????? ?????? ??????
//        if (nfcAdapter == null) {
//            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
//            finish();
//            return;
//        } // NFC ????????? ???????????? ????????? NFC ?????? ?????? ??????
//        else if (!nfcAdapter.isEnabled()){
//            Toast.makeText(this, "NFC ????????? ????????????.", Toast.LENGTH_LONG).show();
//            startActivity(new Intent(this, noNfcWarning.class));
//        }

        //
        mDBHelper = new DBHelper(this, p_id, 1);
        //????????????
        Intent intent = new Intent(this, LoadingActivity.class);
        startActivity(intent);

        test = findViewById(R.id.txt_json);
        personal_name = findViewById(R.id.personal_name);

        dbHelper = new PeopleDBHelper(this, DB_NAME, 1);

        //dbHelper.insertRecord("?????????", 1998);
        int isit = printTabletest("??????");
        Log.d("TAG", "isit : " + isit);

//        //??? ?????? ????????? ??????
//        if(isit != 1) {
//
//            intent = new Intent(this,BeforeLogin_explane_Activity.class);
//            startActivity(intent);
//        }

        main_activity = (LinearLayout) findViewById(R.id.main_activity);
        main_activity.setVisibility(View.VISIBLE);

        //menu ?????? ??????
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");


        // server ?????? ?????????
        // retrofitAPI interface ??????

        selectDB();

        setSupportActionBar(toolbar);

        new SlidingRootNavBuilder(this)
                .withToolbarMenuToggle(toolbar)
                .withMenuOpened(false)
                .withMenuLayout(R.layout.menu)
                .inject();

        // ????????? ??????, ?????? ???????????? ??????
        // ????????? NAME?????? ??????(print????????? ???????????? thename?????????????????? NAME???)
        // ????????? CHARGE??? ??????(?????? ???????????? ??????)
        //nameChanger(name); ???????????? ?????? ?????????

        // Retrofit ????????? ?????????????????? ???????????? Retrofit callback ?????? ????????? NAME?????? CHARGE??? ???????????? ?????????
        // NewRunnable ???????????? ????????? ????????? ???????????? ??? ?????????
        // personal_name, personal_charge??? ?????? ???????????? ????????? Retrofit callback ?????? ?????? ???????????????

        LinearLayout btn_cardMenu = (LinearLayout) findViewById(R.id.btn_cardMenu);
        btn_cardMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, CardMenuActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout btn_history = (LinearLayout) findViewById(R.id.btn_history);
        btn_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout btn_myinfo = (LinearLayout) findViewById(R.id.btn_myinfo);
        btn_myinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MyinfoMenuActivity.class);
                intent.putExtra("profile", image_profile);
                startActivity(intent);
            }
        });

        // ????????? ?????? ??????????????????.
        LinearLayout btn_out = (LinearLayout) findViewById(R.id.btn_menulogout);
        btn_out.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                showMessage();
            }
        });

        LinearLayout btn_gps = (LinearLayout) findViewById(R.id.btn_gps);
        btn_gps.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "????????? ????????? ??????", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, SQLiteTestActivity.class);
                startActivity(intent);
            }
        });


        //setting ?????? ??????
        ImageButton btn_setting = (ImageButton) findViewById(R.id.btn_setting);
        btn_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fadein_right, R.anim.stay);

            }
        });


        //main NFC?????? ?????? ??????
        hand = (ImageView) findViewById(R.id.hand);
        nfcReader = (LinearLayout) findViewById(R.id.nfcReader);
        card = (ImageButton) findViewById(R.id.personal_Card);

        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // personal_card??? ?????? ??? cardcounter??? 0??????
                if (cardcounter == 0) {

                    // nfcReader LinearLayout??? ?????????
                    nfcReader.setVisibility(View.VISIBLE);

                    cardAnim = AnimationUtils.loadAnimation(getApplicationContext(),
                            R.anim.card_anim); //???????????????????????????
                    card.startAnimation(cardAnim);

                    nfcAnim = AnimationUtils.loadAnimation(getApplicationContext(),
                            R.anim.nfc_anim); //???????????????????????????
                    nfcReader.startAnimation(nfcAnim);

                    card.setClickable(false);
                    handcounter = 1;

                    is_tag_mode = 1;  // nfc ????????? ?????? ???????????? ??????

                    if (handcounter == 1) {

                        handAnim = AnimationUtils.loadAnimation(getApplicationContext(),
                                R.anim.hand_anim); //???????????????????????????
                        hand.startAnimation(handAnim);
                    }

                    cardcounter = 1;
                }
            }
        });

        cardLayout = (RelativeLayout) findViewById(R.id.cardLayout);
        cardLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cardcounter == 1) {

                    cardAnim = AnimationUtils.loadAnimation(getApplicationContext(),
                            R.anim.card2_anim); //???????????????????????????
                    card.startAnimation(cardAnim);

                    nfcAnim = AnimationUtils.loadAnimation(getApplicationContext(),
                            R.anim.nfc2_anim); //???????????????????????????
                    nfcReader.startAnimation(nfcAnim);


                    nfcReader.setVisibility(View.INVISIBLE);
                    card.setClickable(true);
                    cardcounter = 0;
                    handcounter = 0;
                    is_tag_mode = 0; //nfc ?????? ???????????? ???????????? ?????????
                }
            }
        });
    }

    //DB ?????????
    private void dbDataDelete(String target) {

        String sql = "delete * from mycontacts where name=" + "'" + target + "'";
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL(sql);

    }

    protected int printTabletest(String thename) {


        int isit = 0;
        Cursor cursor = dbHelper.readRecordOrderByAge();
        String result = "";
        String person_name = "";
        String name = "";

        while (cursor.moveToNext()) {
            int itemId = cursor.getInt(cursor.getColumnIndexOrThrow(PersonalDB.PeopleEntry._ID));
            name = cursor.getString(cursor.getColumnIndexOrThrow(PersonalDB.PeopleEntry.COLUMN_NAME));
            //person_name = name;
            if (name.equals(thename)) {
                person_name = name;
                NAME = name;
                isit = 1;
            }
            int age = cursor.getInt(cursor.getColumnIndexOrThrow(PersonalDB.PeopleEntry.COLUMN_ID));

            result += itemId + " " + name + " " + age + "\n";
        }

        test.setText(result);
        //personal_name.setText(person_name);
        cursor.close();
        return isit;
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }

    //???????????? ?????? ?????? ?????? ??????
    private final long finishtimeed = 1000;
    private long presstime = 0;

    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - presstime;

        if (0 <= intervalTime && finishtimeed >= intervalTime) {
            finish();
        } else {
            presstime = tempTime;
            Toast.makeText(getApplicationContext(), "????????? ???????????? ?????? ???????????????", Toast.LENGTH_SHORT).show();
        }
    }

    public void chargeChanger(int charge) {
        String temp = Integer.toString(charge);
        for (int i = 3; temp.length() - i > 0; i += 4) {
            temp = new StringBuilder(temp).insert(temp.length() - i, ",").toString();
        }

        CHARGE = temp;

    }

    // NFC ????????? ????????? ??? DB??? ???????????? ???????????? ??????, total ????????? ??????
    public void sendToDB(int start, int end) {
        // ?????? ????????? ???????????? ????????? ??????
        UserInfo n_userInfo = new UserInfo(p_id, p_userInfo.getAge(), p_userInfo.getIncome_grade(), p_userInfo.getTotal_fare());
        Log.d("debug1", n_userInfo.getTotal_fare());
        int fare = station.getFareFromNum(start, end, Integer.parseInt(p_userInfo.getAge()));
        int total_fare = Integer.parseInt(p_userInfo.getTotal_fare())+ fare;

        n_userInfo.setTotal_fare(Integer.toString(total_fare));
        Log.d("debug2", n_userInfo.getTotal_fare());

        String time = getTime();
        String time2 = time.substring(0, 8); //??????
        time = time.substring(8);    //??????

        int TODAY = Integer.parseInt(time2);
        int TIME = Integer.parseInt(time);

        mDBHelper.InsertBoarding(TODAY, TIME, start, end, fare, total_fare);
        updateDB(n_userInfo);
    }


    // Retrofit ????????????
    private void selectDB() {
        RetrofitAPI retrofitApi = RetrofitClientInstance.getRetrofitInstance().create(RetrofitAPI.class);
        Call<UserInfo> call = retrofitApi.getMember(p_id);

        call.enqueue(new Callback<UserInfo>() {

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(Call<UserInfo> call, Response<UserInfo> response) {
                // ????????? ??? ????????????
                if (response.isSuccessful()) {
                    UserInfo s_userInfo = response.body(); // DB??? ???????????? List<UserInfo> ????????? ????????????
                    Log.d("debug4", s_userInfo.getTotal_fare());
                    // list?????? p_id??? id??? ?????? UserInfo ????????? ????????? p_userInfo??? ??????
                    // p_userInfo = list.stream().filter(h -> h.getId().equals(p_id)).findFirst().orElseThrow(() -> new IllegalArgumentException());
                    p_userInfo = s_userInfo;
                    Log.d("debug5", p_userInfo.getTotal_fare());
                    // NAME??? p_userInfo.getId() ??????
                    String name = NAME;
                    // CHARGE??? p_userInfo.getTotal_fare() ??????
                    chargeChanger(Integer.parseInt(s_userInfo.getTotal_fare())); // -> 10,000??? ?????? ??? ?????? ,??? ?????? ??????

                    TextView personal_name = (TextView) findViewById(R.id.personal_name);
                    TextView personal_charge = (TextView) findViewById(R.id.menu_charge);

                    personal_name.setText(name);
                    personal_charge.setText(CHARGE);
                    Log.d("debug6", p_userInfo.getTotal_fare());

                } else {
                    Log.e("SelectDB", "response but fail");
                }
            }

            @Override
            public void onFailure(Call<UserInfo> call, Throwable t) {
                Log.e("SelectDB", "fail");
                t.printStackTrace();
            }
        });
    }

    private void syncDB(UserInfo s_userInfo) {
        Log.d("debug4", s_userInfo.getTotal_fare());
        // list?????? p_id??? id??? ?????? UserInfo ????????? ????????? p_userInfo??? ??????
        // p_userInfo = list.stream().filter(h -> h.getId().equals(p_id)).findFirst().orElseThrow(() -> new IllegalArgumentException());
        p_userInfo = s_userInfo;
        Log.d("debug5", p_userInfo.getTotal_fare());
        // NAME??? p_userInfo.getId() ??????
        String name = NAME;
        // CHARGE??? p_userInfo.getTotal_fare() ??????
        chargeChanger(Integer.parseInt(s_userInfo.getTotal_fare())); // -> 10,000??? ?????? ??? ?????? ,??? ?????? ??????

        TextView personal_name = (TextView) findViewById(R.id.personal_name);
        TextView personal_charge = (TextView) findViewById(R.id.menu_charge);

        personal_name.setText(name);
        personal_charge.setText(CHARGE);
        Log.d("debug6", p_userInfo.getTotal_fare());
    }

    private void updateDB(UserInfo u_userInfo) {
        RetrofitAPI retrofitApi = RetrofitClientInstance.getRetrofitInstance().create(RetrofitAPI.class);
        Call<UserInfo> call = retrofitApi.updateMember(u_userInfo.getId(), u_userInfo.getAge(), u_userInfo.getIncome_grade(), u_userInfo.getTotal_fare());
        call.enqueue(new Callback<UserInfo>() {
            @Override
            public void onResponse(Call<UserInfo> call, Response<UserInfo> response) {
                Log.e("updateDB", u_userInfo.getTotal_fare());
            }

            @Override
            public void onFailure(Call<UserInfo> call, Throwable t) {
                Log.e("SelectDB", "fail");
                t.printStackTrace();
            }
        });
        Log.d("debug3", u_userInfo.getTotal_fare());
        syncDB(u_userInfo);
    }

    public void showMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("??????");
        builder.setMessage("??????????????? ??? ?????? ???????????????. \n???????????? ???????????????????");
        builder.setIcon(android.R.drawable.ic_dialog_alert);

        //???????????? "???"
        builder.setPositiveButton("???", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "??????????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                UserManagement.getInstance().requestLogout(new LogoutResponseCallback() {
                    @Override
                    public void onCompleteLogout() {
                        finish(); // ?????? ???????????? ??????
                    }
                });
            }
        });

        //???????????? "?????????"
        builder.setNegativeButton("?????????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                builder.setNegativeButton("?????????", null);
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        enableForegroundDispatchSystem();
    }

    @Override
    protected void onPause() {
        super.onPause();

        disableForegroundDispatchSystem();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.hasExtra(NfcAdapter.EXTRA_TAG)) {
            if (is_tag_mode == 1) {
                Parcelable[] parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
                // nfc tag ????????? ?????? ??????
                if (ride_or_quit == 0) {
                    ride(parcelables);
                    ride_or_quit = 1;
                } else {
                    quit(parcelables);
                    ride_or_quit = 0;
                    sendToDB(station.name2num(rideStation), station.name2num(quitStation));
                }
            } else {
                Toast.makeText(this, "open NFC tag mode", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void ride(Parcelable[] parcelables) {
        boolean is_empty = true;
        if (parcelables != null && parcelables.length > 0) {
            rideStation = readTextFromMessage((NdefMessage) parcelables[0]);
            if (!rideStation.equals("None")) {
                is_empty = false;
                Toast.makeText(this, "?????????????????????.", Toast.LENGTH_SHORT).show();
            }
        }

        if (is_empty) {// ??? nfc tag ?????? No NDEF messages found ????????? ??????
            Toast.makeText(this, "No NDEF messages found!", Toast.LENGTH_SHORT).show();
        }
    }

    private void quit(Parcelable[] parcelables) {
        boolean is_empty = true;
        if (parcelables != null && parcelables.length > 0) {
            quitStation = readTextFromMessage((NdefMessage) parcelables[0]);
            if (!quitStation.equals("None")) {
                is_empty = false;
                Toast.makeText(this, "?????????????????????.\n?????? ??????:" + Integer.toString(station.getFareFromName(rideStation, quitStation, Integer.parseInt(p_userInfo.getAge()))), Toast.LENGTH_SHORT).show();
            }
        }

        if (is_empty) {// ??? nfc tag ?????? No NDEF messages found ????????? ??????
            Toast.makeText(this, "No NDEF messages found!", Toast.LENGTH_SHORT).show();
        }
    }

    private String readTextFromMessage(NdefMessage ndefMessage) {
        NdefRecord[] ndefRecords = ndefMessage.getRecords();

        if (ndefRecords != null && ndefRecords.length > 0) {
            NdefRecord ndefRecord = ndefRecords[0];
            return getTextFromNdefRecord(ndefRecord);
            // Toast.makeText(this, tagContent, Toast.LENGTH_SHORT).show();
        } else {
            return "None";
        }
    }

    public String getTextFromNdefRecord(NdefRecord ndefRecord) {
        String tagContent = null;
        try {
            byte[] payload = ndefRecord.getPayload();
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
            int languageSize = payload[0] & 0063;
            tagContent = new String(payload, languageSize + 1,
                    payload.length - languageSize - 1, textEncoding);
        } catch (UnsupportedEncodingException e) {
            Log.e("getTextFromNdefRecord", e.getMessage(), e);
        }
        return tagContent;
    }

    private void enableForegroundDispatchSystem() {

        Intent intent = new Intent(this, MainActivity.class).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        IntentFilter[] intentFilters = new IntentFilter[]{};
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters, null);
    }

    private void disableForegroundDispatchSystem() {
        nfcAdapter.disableForegroundDispatch(this);
    }
}