package org.binaryeye.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.binaryeye.popularmovies.Models.Result;
import org.binaryeye.popularmovies.Models.TMDBJsonResponse;
import org.binaryeye.popularmovies.Utilities.NetworkUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;

public class MainActivity extends AppCompatActivity implements MoviesAdapter.MoviesAdapterOnClickHandler {

    int popularOrRated = 0;
    int pageNumber = 1;

    ProgressBar mLoadingIndicator;
    TextView pageNumberTextView;
    ImageButton nextPage;
    ImageButton previousPage;
    int totalNumberOfPages;
    private RecyclerView mRecyclerView;
    private MoviesAdapter moviesAdapter;
    private TextView mErrorMessageDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_movies);
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        GridLayoutManager layoutManager = new GridLayoutManager(this,5);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.hasFixedSize();
        moviesAdapter = new MoviesAdapter(this);
        mRecyclerView.setAdapter(moviesAdapter);
        pageNumberTextView = (TextView) findViewById(R.id.page_number);
        pageNumberTextView.setText("1");
        nextPage = (ImageButton) findViewById(R.id.next_btn);
        previousPage = (ImageButton) findViewById(R.id.previous_btn);
        previousPage.setEnabled(false);
        loadMoviesData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.sort_pop) {
            popularOrRated = 0;
            pageNumber = 1;
            previousPage.setEnabled(false);
            pageNumberTextView.setText("1");
            nextPage.setEnabled(true);
            loadMoviesData();
            return true;
        }
        if (id == R.id.sort_rated) {
            popularOrRated = 1;
            pageNumber = 1;
            previousPage.setEnabled(false);
            pageNumberTextView.setText("1");
            nextPage.setEnabled(true);
            loadMoviesData();
            return true;
        }
        if (id == R.id.sort_fav) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadMoviesData() {
        showMoviesDataView();
        new FetchMoviesTask().execute();
    }

    private void showErrorMessage() {
        /* First, hide the currently visible data */
        mRecyclerView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    private void showMoviesDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the weather data is visible */
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(Result currentMovie) {
        Context context = this;
        Class destinationClass = MovieActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        intentToStartDetailActivity.putExtra("Details",  currentMovie.toString());
        startActivity(intentToStartDetailActivity);
    }

    public void previous_page_onClick(View v) {
        pageNumber--;
        if(pageNumber==1){
            previousPage.setEnabled(false);
        }
        nextPage.setEnabled(true);
        pageNumberTextView.setText(Integer.toString(pageNumber));
        loadMoviesData();
    }

    public void next_page_onClick(View v) {
        pageNumber++;
        if(pageNumber == totalNumberOfPages){
            nextPage.setEnabled(false);
        }
        previousPage.setEnabled(true);
        pageNumberTextView.setText(Integer.toString(pageNumber));
        loadMoviesData();
    }

    public class FetchMoviesTask extends AsyncTask<Void, Void, TMDBJsonResponse>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.INVISIBLE);
        }

        @Override
        protected TMDBJsonResponse doInBackground(Void... params) {
            URL moviesRequestUrl = NetworkUtils.buildUrl(popularOrRated, pageNumber, 0L);

            try {
                String jsonMoviesResponse = NetworkUtils
                        .getResponseFromHttpUrl(moviesRequestUrl);

                JSONObject movieJson = new JSONObject(jsonMoviesResponse);

                TMDBJsonResponse response = new TMDBJsonResponse();

                response.setPage(Integer.parseInt(movieJson.get("page").toString()));
                Result[] results = new Result[20];
                JSONArray movieJSONResults = movieJson.getJSONArray("results");
                for(int i = 0; i < movieJSONResults.length(); i++ ){
                    results[i] = new Result();
                    results[i].setPoster_path(movieJSONResults.getJSONObject(i).getString("poster_path"));
                    results[i].setAdult(movieJSONResults.getJSONObject(i).get("adult").toString());
                    results[i].setOverview(movieJSONResults.getJSONObject(i).get("overview").toString());
                    results[i].setRelease_date(movieJSONResults.getJSONObject(i).get("release_date").toString());
                    results[i].setGenre_ids(movieJSONResults.getJSONObject(i).get("genre_ids").toString());
                    results[i].setId(movieJSONResults.getJSONObject(i).get("id").toString());
                    results[i].setOriginal_title(movieJSONResults.getJSONObject(i).get("original_title").toString());
                    results[i].setOriginal_language(movieJSONResults.getJSONObject(i).get("original_language").toString());
                    results[i].setTitle(movieJSONResults.getJSONObject(i).get("title").toString());
                    results[i].setBackdrop_path(movieJSONResults.getJSONObject(i).get("backdrop_path").toString());
                    results[i].setPopularity(movieJSONResults.getJSONObject(i).get("popularity").toString());
                    results[i].setVote_count(movieJSONResults.getJSONObject(i).get("vote_count").toString());
                    results[i].setVideo(movieJSONResults.getJSONObject(i).get("video").toString());
                    results[i].setVote_average(movieJSONResults.getJSONObject(i).get("vote_average").toString());
                }
                response.setResults(results);
                response.setTotal_results(movieJson.get("total_results").toString());
                response.setTotal_pages(movieJson.get("total_pages").toString());
                totalNumberOfPages = Integer.parseInt(response.getTotal_pages());

                return response;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(TMDBJsonResponse moviesData) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            mRecyclerView.setVisibility(View.VISIBLE);
            if (moviesData != null) {
                showMoviesDataView();
                moviesAdapter.setMoviesData(moviesData);
            } else {
                showErrorMessage();
            }
        }
    }
}
