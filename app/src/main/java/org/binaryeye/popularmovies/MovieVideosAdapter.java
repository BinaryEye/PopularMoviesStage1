package org.binaryeye.popularmovies;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.binaryeye.popularmovies.Models.MovieVideos;
import org.binaryeye.popularmovies.Models.MoviesVideosList;
import org.binaryeye.popularmovies.Models.Result;
import org.binaryeye.popularmovies.Models.TMDBJsonResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

/**
 * Created by ammar on 0019 19 Mar 17.
 */

public class MovieVideosAdapter extends RecyclerView.Adapter<MovieVideosAdapter.MoviesAdapterViewHolder> {

    private MoviesVideosList moviesData;

    private final MoviesAdapterOnClickHandler mClickHandler;

    public MovieVideosAdapter(MoviesAdapterOnClickHandler clickHandler){
        mClickHandler = clickHandler;
    }

    public interface MoviesAdapterOnClickHandler{
        void onClick(MovieVideos currentMovie);
    }
    @Override
    public MoviesAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.movie_videos_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new MoviesAdapterViewHolder(view);
    }

    public class LoadImage extends AsyncTask<String , Void, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected Bitmap doInBackground(String... params) {

            try {
                URL url = new URL(params[0]);
                URI uri = new URI(url.getProtocol(), url.getHost(),
                        url.getPath(), url.getQuery(), null);
                HttpURLConnection connection = (HttpURLConnection) uri
                        .toURL().openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

                int bufferSize = 1024;
                byte[] buffer = new byte[bufferSize];
                int len = 0;
                while ((len = input.read(buffer)) != -1) {
                    byteBuffer.write(buffer, 0, len);
                }
                byte[] img = byteBuffer.toByteArray();
                byteBuffer.flush();
                byteBuffer.close();
                input.close();
                Bitmap bitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
                return bitmap;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    @Override
    public void onBindViewHolder(MoviesAdapterViewHolder holder, int position) {
        MovieVideos currentMovie = moviesData.getResults()[position];
        LoadImage loadImage = new LoadImage();
        holder.movieVideoTitle.setText(currentMovie.getName());
        holder.movieVideoTitle.setTag(currentMovie.getKey());
    }

    @Override
    public int getItemCount() {
        if (null == moviesData) return 0;
        return moviesData.getResults().length;
    }

    public class MoviesAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView movieVideoTitle;

        public MoviesAdapterViewHolder(View view){
            super(view);
            movieVideoTitle = (TextView) view.findViewById(R.id.trailer_title);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            MovieVideos currentMovie = moviesData.getResults()[adapterPosition];
            mClickHandler.onClick(currentMovie);
        }
    }

    public void setMoviesData(MoviesVideosList movieData) {
        moviesData = movieData;
        notifyDataSetChanged();
    }
}
