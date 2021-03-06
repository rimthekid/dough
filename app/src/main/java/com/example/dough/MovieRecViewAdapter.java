package com.example.dough;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

public class MovieRecViewAdapter extends RecyclerView.Adapter<MovieRecViewAdapter.ViewHolder> {

    private ArrayList<Movie> movie = new ArrayList<>();
    private Context context;

    public MovieRecViewAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_list_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.txtName.setText(movie.get(position).getName());
        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                moviePlayerStuff(view, position);
                /*else {
                    Intent intent = new Intent(view.getContext(), SeriesActivity.class);
                    intent.putExtra("imgurl", movie.get(position).getImgURL());
                    intent.putExtra("seriesName", movie.get(position).getName());
                    view.getContext().startActivity(intent);
                }*/
            }
        });

        Glide.with(context)
                .asBitmap()
                .load(movie.get(position).getImgURL())
                .into(holder.image);
    }

    private void moviePlayerStuff(View view, final int position) {
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.activity_popup, null);
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;

        //Create a window with our parameters
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        TextView test2 = popupView.findViewById(R.id.textView);
        test2.setText(movie.get(position).getName());
        ImageView imageView = popupView.findViewById(R.id.imageView);
        Glide.with(context)
                .asBitmap()
                .load(movie.get(position).getImgURL())
                .into(imageView);
        ImageButton playButton = popupView.findViewById(R.id.imageButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, movie.get(position).getName(), Toast.LENGTH_SHORT).show();
                Bundle b = new Bundle();
                b.putStringArrayList("links",movie.get(position).getUrls());
                Intent intent = new Intent(view.getContext(), LinkListActivity.class);
                intent.putExtras(b);
                view.getContext().startActivity(intent);
            }
        });
        ImageButton closeButton = popupView.findViewById(R.id.imageButton2);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });

        ImageButton dlButton = popupView.findViewById(R.id.imageButton3);
        dlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (isDownloadManagerAvailable(context)) {
                            String url = movie.get(position).getVidURL();
                            DownloadManager.Request request = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {
                                request = new DownloadManager.Request(Uri.parse(url));

                            request.setDescription("در حال دانلود...");
                            request.setTitle(movie.get(position).getName());
// in order for this if to run, you must use the android 3.2 to compile your app
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                request.allowScanningByMediaScanner();
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                            }
                            File dir = new File(Environment.DIRECTORY_DOWNLOADS , "Dough");

                            request.setDestinationInExternalPublicDir(dir.getAbsolutePath(), movie.get(position).getName() + ".mkv");

// get download service and enqueue file
                            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                            manager.enqueue(request);
                        }
                    }}
                }).start();
                Toast.makeText(context, "دانلود شما آغاز شد و به لیست دانلود ها در ابتدای صفحه اصافه میشود.", Toast.LENGTH_LONG).show();


            }
        });
    }

    @Override
    public int getItemCount() {
        return movie.size();
    }

    public void setMovie(ArrayList<Movie> movie) {
        this.movie = movie;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView txtName;

        private CardView parent;
        private ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            parent = itemView.findViewById(R.id.parent);
            image = itemView.findViewById(R.id.image);

        }
    }

    public static boolean isDownloadManagerAvailable(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return true;
        }
        return false;
    }
}
