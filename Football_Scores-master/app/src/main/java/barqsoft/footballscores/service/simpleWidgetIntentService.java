package barqsoft.footballscores.service;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;
import barqsoft.footballscores.widget.SimpleWidgetProvider;

/**
 * Created by silen_000 on 12/16/2015.
 */
public class simpleWidgetIntentService extends IntentService {

    private static final String[] GAME_COLUMNS = {
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
    };

    private static final int INDEX_HOME_COL = 0;
    private static final int INDEX_AWAY_COL = 1;
    private static final int INDEX_HOME_GOALS_COL = 2;
    private static final int INDEX_AWAY_GOALS_COL = 3;

    public static final String LOG_TAG = "myFetchService";

    public simpleWidgetIntentService() {
        super("simpleWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Retrieve all of the Today widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                SimpleWidgetProvider.class));

        Date today = new Date(System.currentTimeMillis());
        SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd");
        String queryDate = newFormat.format(today);


        Uri gameData = DatabaseContract.scores_table.buildScoreWithDate();
        Cursor data = getContentResolver().query(gameData, GAME_COLUMNS, null, new String[]{queryDate}, null);

        if (data == null) {
            int layoutId = R.layout.widget_simple_empty;
            RemoteViews views = new RemoteViews(getPackageName(), layoutId);
            views.setTextViewText(R.id.empty_widget_textview, getString(R.string.widget_simple_empty_no_data));
            for (int appWidgetId : appWidgetIds) {
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
            return;
        }
        if (!data.moveToFirst()) {
            data.close();
            int layoutId = R.layout.widget_simple_empty;
            RemoteViews views = new RemoteViews(getPackageName(), layoutId);
            for (int appWidgetId : appWidgetIds) {
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
            return;
        }

        String homeTeam = data.getString(INDEX_HOME_COL);
        String awayTeam = data.getString(INDEX_AWAY_COL);
        int homeGoals = data.getInt(INDEX_HOME_GOALS_COL);
        int awayGoals = data.getInt(INDEX_AWAY_GOALS_COL);

        for (int appWidgetId : appWidgetIds) {
            String description = "Open Football Scores App";
            int layoutId = R.layout.widget_simple;
            RemoteViews views = new RemoteViews(getPackageName(), layoutId);

            views.setImageViewResource(R.id.widget_icon_home, Utilies.getTeamCrestByTeamName(homeTeam));
            views.setImageViewResource(R.id.widget_icon_away, Utilies.getTeamCrestByTeamName(awayTeam));
            views.setTextViewText(R.id.widget_team_home, homeTeam);
            views.setTextViewText(R.id.widget_team_away, awayTeam);

            if (homeGoals < 0)
                views.setTextViewText(R.id.widget_score_home, "0");
            else
                views.setTextViewText(R.id.widget_score_home, String.valueOf(homeGoals));

            if (awayGoals < 0)
                views.setTextViewText(R.id.widget_score_away, "0");
            else
                views.setTextViewText(R.id.widget_score_away, String.valueOf(awayGoals));

            // Content Descriptions for RemoteViews were only added in ICS MR1
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                setRemoteContentDescription(views, description);
            }
            //views.setTextViewText(R.id.widget_high_temperature, formattedMaxTemperature);

            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, String description) {
        views.setContentDescription(R.id.widget, description);
    }

}
