package br.com.fiap.pizza;


import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.view.SurfaceHolder;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.squareup.picasso.Picasso;
import com.yqritc.scalablevideoview.ScalableVideoView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LoginFragment.OnFragmentInteractionListener, SurfaceHolder.Callback {


    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "SignInActivity";
    private MediaPlayer mp = null;
    boolean isLogged = false;
    String nome;
    String email;
    Uri avatar;
    TextView nomeView;
    ImageView avatarView;
    TextView emailView;

    SharedPreferences mPrefs;

    Bundle savedInstanceState;

    MyMapFragment myMapFragment;
    LoginFragment loginFragment;


    ScalableVideoView mVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        setContentView(R.layout.activity_main);
        this.myMapFragment = new MyMapFragment();
        this.loginFragment = new LoginFragment();

        Firebase.setAndroidContext(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        EventBus.getDefault().register(this);
        mPrefs = getSharedPreferences("Google_firebase", MODE_PRIVATE);
        mVideoView = (ScalableVideoView) findViewById(R.id.main_background_videoview);

        try {
            mVideoView.setRawData(R.raw.main_background);
            mVideoView.setLooping(true);

            mVideoView.prepare(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mVideoView.start();
                }
            });
            System.out.println("BACK GROUND OK");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("ERROR BACKGROUND");
        }

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_CALENDAR);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("onActivityResult" + requestCode);

        EventBus.getDefault().post(new OnActivityResultEvent(data, requestCode, resultCode));


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Subscribe
    public void onMessageEvent(OnLoginChange event) {
        System.out.println("OnLoginChange");

        this.avatar = event.avatar;
        this.email = event.email;
        this.nome = event.name;
        this.isLogged = event.isLogged;

        updateUserInfoFromPrefs();

        if (isLogged) {

            Picasso.with(this).load(this.avatar).into(avatarView);
            nomeView.setText(this.nome);
            emailView.setText(this.email);

        } else {

            Picasso.with(this).load("https://www2.fiap.com.br/updown/fiapx/podcast/logo_fiap.jpg").into(avatarView);
            nomeView.setText("Faça o Login!");
            emailView.setText("Google-Firebase-Fiap");
        }


    }

    public void updateUserInfoFromPrefs() {
        mPrefs.edit()
                .putString("email", email)
                .putString("nome", nome)
                // .putString("avatar", avatar.toString())
                .putBoolean("isLogged", isLogged)
                .apply();
    }

    public void getUserInfoFromPrefs() {
        this.email = mPrefs.getString("email", "");
        this.nome = mPrefs.getString("nome", "");
        this.avatar = Uri.parse(mPrefs.getString("avatar", ""));
        this.isLogged = mPrefs.getBoolean("isLogged", false);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        nomeView = (TextView) findViewById(R.id.nome);
        avatarView = (ImageView) findViewById(R.id.avatar);
        emailView = (TextView) findViewById(R.id.email);
        getUserInfoFromPrefs();
        if (isLogged) {

            Picasso.with(this).load(this.avatar).into(avatarView);
            nomeView.setText(this.nome);
            emailView.setText(this.email);

        } else {
            Picasso.with(this).load("https://www2.fiap.com.br/updown/fiapx/podcast/logo_fiap.jpg").into(avatarView);
            nomeView.setText("Faça o Login!");
            emailView.setText("Google-Firebase-Fiap");
        }


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;
        FragmentManager fragmentManager = getSupportFragmentManager();

        Fragment.SavedState mapState = null;
        if (id == R.id.nav_map) {

            findViewById(R.id.main_button).setVisibility(View.GONE);

            if(fragmentManager.findFragmentByTag("map") != null) {
                //if the fragment exists, show it.
                fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("map")).commit();
            } else {
                //if the fragment does not exist, add it to fragment manager.
                fragmentManager.beginTransaction().add(R.id.content, new MyMapFragment(), "map").commit();
            }
            if(fragmentManager.findFragmentByTag("login") != null){
                //if the other fragment is visible, hide it.
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("login")).commit();
            }




        } else if (id == R.id.nav_account) {

            findViewById(R.id.main_button).setVisibility(View.GONE);

            if(fragmentManager.findFragmentByTag("login") != null) {
                //if the fragment exists, show it.
                fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("login")).commit();
            } else {
                //if the fragment does not exist, add it to fragment manager.
                fragmentManager.beginTransaction().add(R.id.content, new LoginFragment(), "login").commit();
            }
            if(fragmentManager.findFragmentByTag("map") != null){
                //if the other fragment is visible, hide it.
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("map")).commit();
            }
        }



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
