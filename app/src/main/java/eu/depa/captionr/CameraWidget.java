package eu.depa.captionr;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class CameraWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        for (int appWidgetId : appWidgetIds) {
            Intent intent = new Intent(context, DescribeActivity.class);
            intent.putExtra("from_widget", true);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.camera_widget_initial);

            views.setOnClickPendingIntent(R.id.wid_mom, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

    }
}
