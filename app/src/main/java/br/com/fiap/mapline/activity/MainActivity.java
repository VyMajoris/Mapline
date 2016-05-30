package br.com.fiap.mapline.activity;


import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.collect.Lists;
import com.squareup.picasso.Picasso;
import com.yqritc.scalablevideoview.ScalableVideoView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import br.com.fiap.mapline.R;
import br.com.fiap.mapline.fragment.ItemFragment;
import br.com.fiap.mapline.fragment.LoginFragment;
import br.com.fiap.mapline.fragment.MyMapFragment;
import br.com.fiap.mapline.service.MyFirebaseListenerService;
import br.com.fiap.mapline.util.MyFirebaseMapUtil;
import br.com.fiap.mapline.util.OnActivityResultEvent;
import br.com.fiap.mapline.util.OnLoginChange;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, MyMapFragment.OnMyMapReady, MyFirebaseMapUtil.OnMyFirebaseReady {

    boolean isLogged = false;
    String nome;
    String email;
    Uri avatar;
    TextView nomeView;
    ImageView avatarView;
    TextView emailView;
    SharedPreferences mPrefs;
    LatLng latLng = null;
    Bundle savedInstanceState;
    MyMapFragment myMapFragment;
    LoginFragment loginFragment;
    ScalableVideoView mVideoView;
    private boolean isMapReady = false;
    NavigationView navigationView;
    boolean isAllHidden = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        setContentView(R.layout.activity_main);
        this.myMapFragment = new MyMapFragment();
        this.loginFragment = new LoginFragment();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //Pega o SharedPreferences
        mPrefs = getSharedPreferences("Google_firebase", MODE_PRIVATE);

        //Seta o context para o firebase
        Firebase.setAndroidContext(this);
        MyFirebaseMapUtil.init(this, mPrefs);

        //Para mandar mensagens entre classes ativas
        EventBus.getDefault().register(this);




        //Configs do video
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
        } catch (Exception e) {
            e.printStackTrace();

        }

        //Configs do Navigation Drawer
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        findViewById(R.id.main_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            drawer.openDrawer(Gravity.LEFT);
            }
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Como eu sei que o único resultado que pode aparecer aqui é o de login, eu passo direto os parametros para o bus que irá disparar um método que esteja subscribed
        //para um parametro com nome de "OnActivityResultEvent". Fica no Login Fragment
        //O correto seria fazer o tratamento da mensagem antes de enviar pelo EventBus
        EventBus.getDefault().post(new OnActivityResultEvent(data, requestCode, resultCode));
    }


    //Botão de retorno
    @Override
    public void onBackPressed() {
        getSupportActionBar().setTitle(R.string.title_activity_main);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);


        if (drawer.isDrawerOpen(GravityCompat.START) || !isAllHidden) {
            drawer.closeDrawer(GravityCompat.START);
            try {
                handleFragments(null, Lists.newArrayList("map", "login","list"), null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            isAllHidden = true;
        } else {
            super.onBackPressed();
        }


    }


    //Subscribe do event bus de mudança de status de login
    @Subscribe
    public void onMessageEvent(OnLoginChange event) {
        this.avatar = event.avatar;
        this.email = event.email;
        this.nome = event.name;
        this.isLogged = event.isLogged;

        updateUserInfoFromPrefs();

        //isLogged vem do SharedPreferences
        if (isLogged) {
            if (avatar != null) {
                Picasso.with(this).load(this.avatar).error(R.drawable.fiap).into(avatarView);
            }
            nomeView.setText(this.nome);
            emailView.setText(this.email);

        } else {
            Picasso.with(this).load(R.drawable.fiap).into(avatarView);
            nomeView.setText(R.string.name_placeholder);
            emailView.setText(R.string.email_placeholder);
        }

    }

    //Atualiza as infos do SharedPrefences com as do login
    public void updateUserInfoFromPrefs() {
        mPrefs.edit().putString("email", email).putString("nome", nome).putString("avatar", avatar == null ? null : avatar.toString()).putBoolean("isLogged", isLogged).apply();
    }

    //Recupera as infos para as variáves de classe
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
            Picasso.with(this).load(R.drawable.fiap).into(avatarView);
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
        FragmentManager fragmentManager = getSupportFragmentManager();
        try {


            if (id == R.id.nav_map) {
                //Mapa selecionado
                handleFragments("map", Lists.newArrayList("login", "list"), MyMapFragment.class);
                //Loigin elecionado
            } else if (id == R.id.nav_account) {
                handleFragments("login", Lists.newArrayList("map", "list"), LoginFragment.class);
                //Lista selecionada
            } else if (id == R.id.nav_list) {
                handleFragments("list", Lists.newArrayList("map", "login"), ItemFragment.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    //da show nos fragments que já estão adicionados
    //da add nos fragemnts que ainda não estão adicionados
    //da hide nos outros fragments que estão adicionados para mostrar o fragment em questão
    public <T extends Fragment> void  handleFragments(String toShow, List<String> toHide, Class<T> fragment) throws Exception {

        FragmentManager fragmentManager = getSupportFragmentManager();




        if (toShow != null){
            //Some com o botão e o texto da activity
            findViewById(R.id.main_button).setVisibility(View.GONE);
            findViewById(R.id.main_info).setVisibility(View.GONE);
            if (fragmentManager.findFragmentByTag(toShow) != null) {
                //if the fragment exists, show it.

                fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag(toShow)).commit();
            } else {
                //if the fragment does not exist, add it to fragment manager.
                fragmentManager.beginTransaction().add(R.id.content, fragment.getConstructor().newInstance(), toShow).commit();
            }
        }else{

            findViewById(R.id.main_button).setVisibility(View.VISIBLE);
            findViewById(R.id.main_info).setVisibility(View.VISIBLE);
        }
        for (String toHideString : toHide) {
            if (fragmentManager.findFragmentByTag(toHideString) != null) {

                //if the other fragment is visible, hide it.
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(toHideString)).commit();
            }
        }

    }

    //Callback do OnMapReady do MyMapFragment (custom)
    @Override
    public void onFragmentInteraction() {
        isMapReady = true;
        if (latLng != null && myMapFragment.isAdded()) {
            myMapFragment.updateMapCamera(latLng);
        }
    }

    //Mostra a posição no MyMapFragment conforme o click na lista de linhas
    // (Método chamado pelo ItemFragment via contexto)
    public void showUserLineOnMap(LatLng latLng) {
        this.latLng = latLng;
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag("map") != null) {
            fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("map")).commit();
            myMapFragment = (MyMapFragment) fragmentManager.findFragmentByTag("map");
        } else {
            myMapFragment = new MyMapFragment();
            fragmentManager.beginTransaction().add(R.id.content, myMapFragment, "map").commit();
        }
        if (fragmentManager.findFragmentByTag("login") != null) {
            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("login")).commit();
        }
        if (fragmentManager.findFragmentByTag("list") != null) {
            //if the other fragment is visible, hide it.
            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("list")).commit();
        }

        if (latLng != null && myMapFragment.isAdded() && isMapReady) {
            myMapFragment.updateMapCamera(latLng);
        }

        navigationView.getMenu().getItem(0).setChecked(true);
    }

    //Callback do MyFirebaseUtil
    @Override
    public void onMyFireabseReady() {
        startMyFirebaseListenerService();
    }


    @Override
    public void onPause() {
        super.onPause();
        mVideoView.pause();
        //para o serviço  não comer bateria e rede, pois os listeners do firebase usam socket
        stopService(new Intent(this, MyFirebaseListenerService.class));
    }

    @Override
    public void onResume() {
        super.onResume();
        mVideoView.start();
        startMyFirebaseListenerService();

    }


    private void startMyFirebaseListenerService() {
        Thread t = new Thread() {
            public void run() {
                startService(new Intent(getApplicationContext(), MyFirebaseListenerService.class));
            }
        };
        t.start();
    }
}
