package com.codepath.apps.restclienttemplate;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class TimelineActivity extends AppCompatActivity {

    private TwitterClient client;
    private RecyclerView rvTweets;
    private TweetsAdapter tweetsAdapter;
    private List<Tweet> tweets;

    private SwipeRefreshLayout swipeContainer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        client = TwitterApp.getRestClient(this);

        swipeContainer = findViewById(R.id.swipeContainer);

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        // Find the recyclerView
        rvTweets = findViewById(R.id.rvTweets);
        // initialize the list of tweets and adapter from the data source
        tweets = new ArrayList<>();
        tweetsAdapter = new TweetsAdapter(this, tweets);
        // Recycler View setup: layout manager and setting the adapter
        rvTweets.setLayoutManager(new LinearLayoutManager(this));
        rvTweets.setAdapter(tweetsAdapter);
        populateHomeTimeline();

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d("TwitterClient", "content is being refreshed");
                populateHomeTimeline();
            }
        });
    }

    private void populateHomeTimeline() {
        client.getHomeTimeLine(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);

                //Log.d("TwitterClient", response.toString());

                // Iterate through the list of tweets
                List<Tweet> tweetsToAdd = new ArrayList<>();
                for(int i = 0; i < response.length(); i++)
                {
                    try {
                        // Convert each JSON object into a Tweet object
                        JSONObject jsonTweetObject = response.getJSONObject(i);
                        Tweet tweet = Tweet.fromJson(jsonTweetObject);
                        // Add the tweet into our data source
                        tweetsToAdd.add(tweet);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                // Clearing the existing data
                tweetsAdapter.clear();
                //Show the data we just received
                tweetsAdapter.addTweets(tweetsToAdd);
                // Now we call setRefreshing(false) to signal refresh has finished
                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Log.e("TwitterClient", responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.e("TwitterClient", errorResponse.toString());
            }
        });
    }
}
