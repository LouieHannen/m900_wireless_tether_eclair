package m900.tether.widget;

import java.io.File;

import m900.tether.R;
import m900.tether.system.CoreTask;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class TetherWidgetProvider extends AppWidgetProvider
{

    /**
     * Action to pass in for the intent
     */
    private static final String ACTION_WIDGET_RECEIVER = "ActionRecieverWidget";  

    /**
     * Value of tether.status that means the phone is currently tethering
     */
    public static final String TETHER_ENABLED = "running";
    
    /**
     * Value of data path.
     */
    public static final String DATA_FILE_PATH = "/data/data/m900.tether";
    
    /**
     * Value of log message tags.
     */
    public static final String MSG_TAG = "Tether Widget";
    
    
    /**
     * Remind user to set up everything if the app isn't properly set up.
     */
    public void onEnabled (Context context)
    {
    	tetherCheck(context);
    }
    
    
    @Override  
    public void onUpdate(
        Context context, 
        AppWidgetManager appWidgetManager, 
        int[] appWidgetIds)    
    {
    	
    	// create an intent
        Intent intent = new Intent(context, TetherWidgetProvider.class);
        intent.setAction(ACTION_WIDGET_RECEIVER);
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, 0);
        RemoteViews views = new RemoteViews(
            context.getPackageName(), R.layout.widgetview);
        
        views.setOnClickPendingIntent(R.id.buttonWidget, pendingIntent);

        // set the widget button accordingly
        if (isTetheringEnabled())
        {
            views.setImageViewResource(R.id.buttonWidget, R.drawable.icon_on); 
        }
        else
        {
            views.setImageViewResource(R.id.buttonWidget, R.drawable.icon_off); 
        }

        for (int i = 0; i < appWidgetIds.length; i++) 
        {  
            int appWidgetId = appWidgetIds[i];  
            appWidgetManager.updateAppWidget(appWidgetId, views);  
        }  
    }

    
    @Override  
    public void onReceive(Context context, Intent intent) 
    {

        if (intent.getAction().equals(ACTION_WIDGET_RECEIVER)) 
        {  
            // get the views
            RemoteViews views = new RemoteViews(
                context.getPackageName(),  
                R.layout.widgetview);  
            ComponentName cn = new ComponentName(
                context, TetherWidgetProvider.class);  

            // are we tethering now?
            if (isTetheringEnabled())
            {
                // stop tethering
                stopTethering();
            }
            else
            {
                // set the loading/starting image
                views.setImageViewResource(
                    R.id.buttonWidget, 
                    R.drawable.icon_starting); 
                AppWidgetManager.getInstance(context).updateAppWidget(
                    cn, 
                    views);  
                
                // start tethering
                startTethering(context);
            }
            
            // check the status - to change the button icon
            if (isTetheringEnabled())
            {
                views.setImageViewResource(R.id.buttonWidget, R.drawable.icon_on); 
            }
            else
            {
                views.setImageViewResource(R.id.buttonWidget, R.drawable.icon_off); 
            }
            
            // get the component and call update on it
            AppWidgetManager.getInstance(context).updateAppWidget(cn, views);  
        }

        super.onReceive(context, intent);  
    }

    
    /**
     * Start tethering using the command tether start 1
     */
    private void startTethering(Context context) 
    {
    	if (tetherCheck(context))
    	{
        	CoreTask coretask = new CoreTask();
            coretask.setPath(DATA_FILE_PATH + "/bin");
            coretask.runShellCommand("su","stdout","/data/data/m900.tether/bin/tether start 1");
            /*
             *  TODO
             *  Hacky underclock pref detection. Redo.
             */
    		File cfg = new File("/data/data/m900.tether/conf/underclock");
    		if (cfg.exists() == true)
    		{
    			boolean OverClockResult = coretask.underClock(); 
    			if (OverClockResult)
    			{
    		   		Log.d(MSG_TAG, "Underclock succeeded!");
    			} 
    			else
    			{
    		   		Log.d(MSG_TAG, "Underclock failed!");
    			}
    		}
        }
    }


    /**
     * stop tethering using stop 1
     */
    private void stopTethering() 
    {
        CoreTask coretask = new CoreTask();
        coretask.setPath(DATA_FILE_PATH + "/bin");
        coretask.runShellCommand("su","stdout","/data/data/m900.tether/bin/tether stop 1");
        /*
         *  TODO
         *  Hacky underclock pref detection. Redo.
         */
		File cfg = new File("/data/data/m900.tether/conf/underclock");
		if (cfg.exists() == true)
		{
			boolean OverClockResult = coretask.overClock(); 
			if (OverClockResult)
			{
		   		Log.d(MSG_TAG, "Overclock succeeded!");
			} 
			else
			{
		   		Log.d(MSG_TAG, "Overclock failed!");
			}
		}
    }
    
    
    /**
     * Return whether or not the phone is currently tethering
     * 
     * @return
     */
    public boolean isTetheringEnabled()
    {
        boolean tethering = false;
        
        try 
        {
            CoreTask coretask = new CoreTask();
            coretask.setPath("/data/data/m900.tether");
            tethering = coretask.runShellCommand("sh","stdout","getprop tether.status").equals(TETHER_ENABLED);
        }
        catch (Exception exc)
        {}
        
        return tethering;
    }

    /**
     * Check if tether binary exists. If it doesn't, remind the user to run main app.
     * 
     * @param context
     * @return
     */
    private boolean tetherCheck(Context context)
    {
    	File file = new File("/data/data/m900.tether/bin/tether");
    	if (!file.exists())
    	{
    		Toast.makeText(context, "Please run the m900 Wifi Tether app to set up the files for this widget.", Toast.LENGTH_SHORT).show();
    		return false;
    	}
    	return true;
    }
}

