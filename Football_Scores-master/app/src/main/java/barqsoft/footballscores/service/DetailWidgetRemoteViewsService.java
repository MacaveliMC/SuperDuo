package barqsoft.footballscores.service;

import android.widget.RemoteViewsService;
import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;


import java.lang.annotation.Target;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;

/**
 * Created by silen_000 on 12/28/2015.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService {
    public final String LOG_TAG = DetailWidgetRemoteViewsService.class.getSimpleName();
    private static final String[] GAME_COLUMNS = {
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.TIME_COL,
            DatabaseContract.scores_table.MATCH_ID,
            DatabaseContract.scores_table._ID
    };
    // these indices must match the projection
    private static final int INDEX_HOME_COL = 0;
    private static final int INDEX_AWAY_COL = 1;
    private static final int INDEX_HOME_GOALS_COL = 2;
    private static final int INDEX_AWAY_GOALS_COL = 3;
    private static final int INDEX_TIME_COL = 4;
    private static final int INDEX_MATCH_ID = 5;
    private static final int INDEX_ID = 6;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                //final long identityToken = Binder.clearCallingIdentity();
                //String location = Utility.getPreferredLocation(DetailWidgetRemoteViewsService.this);
                //Uri weatherForLocationUri = WeatherContract.WeatherEntry
                //        .buildWeatherLocationWithStartDate(location, System.currentTimeMillis());
                //data = getContentResolver().query(weatherForLocationUri,
                //        FORECAST_COLUMNS,
                //        null,
                //        null,
                //        WeatherContract.WeatherEntry.COLUMN_DATE + " ASC");
                //Binder.restoreCallingIdentity(identityToken);

                Date today = new Date(System.currentTimeMillis());
                SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd");
                String queryDate = newFormat.format(today);

                Uri gameData = DatabaseContract.scores_table.buildScoreWithDate();
                data = getContentResolver().query(gameData, GAME_COLUMNS, null, new String[]{queryDate}, null);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }

                String homeTeam = data.getString(INDEX_HOME_COL);
                String awayTeam = data.getString(INDEX_AWAY_COL);
                int homeGoals = data.getInt(INDEX_HOME_GOALS_COL);
                int awayGoals = data.getInt(INDEX_AWAY_GOALS_COL);
                String match_date = data.getString(INDEX_TIME_COL);

                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_detail_item);


                views.setTextViewText(R.id.widget_detail_home_name, homeTeam);
                views.setTextViewText(R.id.widget_detail_away_name, awayTeam);
                views.setImageViewResource(R.id.widget_detail_home_crest, Utilies.getTeamCrestByTeamName(homeTeam));
                views.setImageViewResource(R.id.widget_detail_away_crest, Utilies.getTeamCrestByTeamName(awayTeam));
                views.setTextViewText(R.id.widget_detail_score_textview, Utilies.getScores(homeGoals, awayGoals));
                views.setTextViewText(R.id.widget_detail_data_textview, match_date);




                String description = homeTeam + " versus " + awayTeam + ". The score is " + homeGoals + " to " + awayGoals;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    setRemoteContentDescription(views, description);
                }

                final Intent fillInIntent = new Intent();
                fillInIntent.putExtra(MainActivity.DETAIL_WIDGET_KEY, data.getInt(INDEX_MATCH_ID));
                views.setOnClickFillInIntent(R.id.list_item_layout, fillInIntent);
                return views;
            }

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views, String description) {
                views.setContentDescription(R.id.list_item_layout, description);
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.scores_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(INDEX_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}