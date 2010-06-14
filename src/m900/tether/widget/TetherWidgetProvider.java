package m900.tether.widget;

import java.io.IOException;

import m900.tether.R;
import m900.tether.system.CoreTask;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class TetherWidgetProvider extends AppWidgetProvider
{

    /**
     * Action to pass in for the intent
     */
    private static final String ACTION_WIDGET_RECEIVER = "ActionRecieverWidget";  

    /**
     * Key to get the tethering status
     */
    public static final String TETHER_STATUS = "tether.status";
    
    /**
     * Value of tether.status that means the phone is currently tethering
     */
    public static final String TETHER_ENABLED = "running";
    
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
                startTethering();
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
    private void startTethering() 
    {
        String cmd[] = new String[3];
        cmd[0] = "su";
        cmd[1] = "-c";
        cmd[2]= "/data/data/m900.tether/bin/tether start 1";

        // get the runtime object
        Runtime r = Runtime.getRuntime();
        try
        {
            Process p = r.exec(cmd);
            p.waitFor();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }     
        
    }

    /**
     * stop tethering using stop 1
     */
    private void stopTethering() 
    {
        String cmd[] = new String[3];
        cmd[0] = "su";
        cmd[1] = "-c";
        cmd[2]= "/data/data/m900.tether/bin/tether stop 1";

        // get the runtime object
        Runtime r = Runtime.getRuntime();
        try
        {
            Process p = r.exec(cmd);
            p.waitFor();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
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
            tethering = coretask.runShellCommand("sh","stdout",TETHER_STATUS).equals(TETHER_ENABLED);
        }
        catch (Exception exc)
        {}
        
        return tethering;
    }
}
