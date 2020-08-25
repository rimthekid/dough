package com.example.dough;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2;
    static final Integer READ_EXST = 0x4;
    private RecyclerView movierecview;
    private RecyclerView downloadedFilmRecylclerView;
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(MainActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }


        };
        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();
        setContentView(R.layout.activity_main);
        movierecview = findViewById(R.id.movierecview);
        downloadedFilmRecylclerView = findViewById(R.id.downloadedFilms);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        /**
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */
        mSwipeRefreshLayout.post(new Runnable() {

            @Override
            public void run() {

                mSwipeRefreshLayout.setRefreshing(true);
                downloadJSON("https://raw.githubusercontent.com/cppox/Dough/master/movies.json");
                // Fetching data from server
            }
        });
    }

    private void downloadJSON(final String urlWebService) {

        class DownloadJSON extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }


            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                try {
                    loadIntoListView(s);
                    //Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    System.out.println(s);
                }
            }

            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL(urlWebService);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String json;
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }
                    return sb.toString().trim();
                } catch (Exception e) {
                    return null;
                }
            }
        }
        DownloadJSON getJSON = new DownloadJSON();
        getJSON.execute();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void loadIntoListView(String json) throws JSONException {
        JSONArray jsonArray = new JSONArray(json);
        ArrayList<movie> movies = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            movies.add(new movie(obj.getString("name"), obj.getString("image"), obj.getString("vidurl")));
            //Toast.makeText(getApplicationContext(),"kk", Toast.LENGTH_SHORT).show();
        }
        /*File file = new File(Environment.DIRECTORY_DOWNLOADS + "/folderName");
        if (!file.mkdirs()) {
            file.mkdirs();
        }*/
        System.out.println(movies.get(0).getVidurl());
        askForPermission(Manifest.permission.READ_EXTERNAL_STORAGE, READ_EXST);
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);


        Log.v("Files", directory.exists() + "");
        Log.v("Files", directory.isDirectory() + "");
        Log.v("Files", directory.listFiles() + "");
        File[] files = directory.listFiles();
        ArrayList<movie> downloadedMovies = new ArrayList<>();
        for (File file : files) {
            System.out.println(file.getName());
            String[] name = file.getName().split("\\.");
            String movieName = filmName(name);
            downloadedMovies.add(findMovieByName(movieName, movies));
        }
        MovieRecViewAdapter adapter = new MovieRecViewAdapter(this);
        adapter.setMovie(movies);
        movierecview.setAdapter(adapter);
        movierecview.setLayoutManager(new GridLayoutManager(this, 3));
        DownloadedMoviesAdapter downloadedMoviesAdapter = new DownloadedMoviesAdapter(downloadedMovies, this);
        downloadedFilmRecylclerView.setAdapter(downloadedMoviesAdapter);
        downloadedFilmRecylclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mSwipeRefreshLayout.setRefreshing(false);
    }

    public String filmName(String[] name) {
        String movieName = "";
        for (int i = 0; i < name.length; i++) {
            if (name[i].equals("mkv"))
                return movieName;
            else {
                if (name[i + 1].equals("mkv"))
                    movieName += name[i] ;
                else movieName += name[i] + " ";
            }
        }
        return null;
    }

    public movie findMovieByName(String name, ArrayList<movie> movies) {
        for (movie movie : movies) {
            if (movie.getName().equals(name))
                return movie;
        }
        return null;
    }


    @Override
    public void onRefresh() {
        downloadJSON("https://raw.githubusercontent.com/cppox/Dough/master/movies.json");
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        Log.e("TAg", "permission dary");

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED) {

            Intent imageIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(imageIntent, 11);

            Log.e("TAg", "permission dary");
        } else {
            Log.e("TAg", "permission nadary");
        }
    }


    private void askForPermission(String permission, Integer requestCode) {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
    }


}