package com.gigaStorm.twinRinks;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.CalendarContract;

/**
 * <code>Data_CalendarManager</code> handles the addition of events to a user's
 * calendar.
 * 
 * @author Boris Dubinsky
 * @author Andrew Mass
 */
@SuppressLint("NewApi")
public class Data_CalendarManager {

  private Context context;

  private Data_MemoryManager memoryManager;

  private Util util;

  public Data_CalendarManager(Context context) {
    this.context = context;
    memoryManager = new Data_MemoryManager(context);
    util = new Util(context);
  }

  public void saveGamesToCalendar() {
    showCalendarPopup();
  }

  private void showCalendarPopup() {
    final ContentResolver cr;
    final Cursor result;
    final Uri uri;
    List<String> listCals = new ArrayList<String>();
    final String[] projection = new String[] {BaseColumns._ID,
        CalendarContract.Calendars.ACCOUNT_NAME,
        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
        CalendarContract.Calendars.NAME,};

    uri = CalendarContract.Calendars.CONTENT_URI;

    cr = context.getContentResolver();
    result = cr.query(uri, projection, null, null, null);

    if(result.getCount() > 0 && result.moveToFirst()) {
      do {
    	  String	tmp = result.getString(result
    	     .getColumnIndex(CalendarContract.Calendars.NAME));
    	  if (tmp == null)
    		  tmp = result.getString(result
    				  .getColumnIndex(
    						  CalendarContract.Calendars.CALENDAR_DISPLAY_NAME));
    	  listCals.add(tmp);
      }
      while(result.moveToNext());
    }

    CharSequence[] calendars = listCals.toArray(new CharSequence[listCals
        .size()]);

    AlertDialog.Builder builder = new AlertDialog.Builder(context);
    builder.setTitle("Calendar to use:");
    builder.setItems(calendars, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int itemCal) {
        loopThroughGames(itemCal);
      };
    });

    AlertDialog alert = builder.create();
    alert.show();
  }

  private void loopThroughGames(int whichCalendar) {
    ArrayList<Model_Game> games = memoryManager.getGames();

    for(Model_Game g: games) {
      for(Model_Team t: memoryManager.getUserTeams()) {
        if((g.getTeamA().equalsIgnoreCase(t.getTeamName()) || g.getTeamH()
            .equalsIgnoreCase(t.getTeamName()))
            && g.getLeague().equalsIgnoreCase(t.getLeague()) && !g.hasPassed()) {
          addGameToCalendar(g, whichCalendar + 1);
        }
      }
    }
  }

  private void addGameToCalendar(Model_Game game, int whichCalendar) {
    ContentResolver cr = context.getContentResolver();
    ContentValues values = new ContentValues();

    try {   
      String[] query = new String[] {
    	  CalendarContract.Events.CALENDAR_ID,
    	  CalendarContract.Events.TITLE,
    	  CalendarContract.Events.EVENT_LOCATION,
    	  CalendarContract.Events.DTSTART,
    	  CalendarContract.Events.DTEND,
    	  CalendarContract.Events.EVENT_TIMEZONE
      };
      
      final int PROJECTION_CALENDAR_ID_INDEX = 0;
      final int PROJECTION_TITLE_INDEX = 1;
      final int PROJECTION_EVENT_LOCATION_INDEX = 2;
      final int PROJECTION_DTSTART_INDEX = 3;
      
      Cursor cursor = context.getContentResolver().query(
    		  CalendarContract.Events.CONTENT_URI,
    		  query, null, null, null);
      Uri uri = CalendarContract.Calendars.CONTENT_URI;

      cr = context.getContentResolver();   
      String selection = "((" + Calendars.ACCOUNT_NAME + " = ?) AND (" 
                              + Calendars.ACCOUNT_TYPE + " = ?) AND ("
                              + Calendars.OWNER_ACCOUNT + " = ?))";
      String[] selectionArgs = new String[] {"sampleuser@gmail.com", "com.google",
              "sampleuser@gmail.com"}; 
      // Submit the query and get a Cursor object back. 
      cursor = cr.query(uri, query, selection, selectionArgs, null);
      
      boolean done = cursor.moveToFirst();
      boolean found = false;
      while (!done) {
    	  // Check if this record is the one we are trying to add
    	  Integer id = cursor.getInt(PROJECTION_CALENDAR_ID_INDEX);
    	  if (id == whichCalendar) { 
    		  String title = cursor.getString(PROJECTION_TITLE_INDEX);
    		  if (title.startsWith("Hockey- ")) {
    			  String event = cursor.getString(PROJECTION_EVENT_LOCATION_INDEX);
    			  if (event.startsWith("Twin Rinks Ice Arena - ")) {
    				  Long start = cursor.getLong(PROJECTION_DTSTART_INDEX);
    				  if ( start == game.getCal().getTimeInMillis() ) {
    					  found = true;
    		    	  }
    			  }
    		  }
    		  if (found || !cursor.moveToNext())
    			  done = true; 
    	  }
      }
      if (!found) {
	      // Check if the event already exists. If it does not add it.
	      values.put(CalendarContract.Events.CALENDAR_ID, whichCalendar);
	      values.put(CalendarContract.Events.TITLE, "Hockey- " + game.getLeague()
	          + ": " + game.getTeamH() + " vs " + game.getTeamA());
	      values.put(CalendarContract.Events.EVENT_LOCATION,
	          "Twin Rinks Ice Arena - " + game.getRink() + " Rink");
	      values.put(CalendarContract.Events.DTSTART, game.getCal()
	          .getTimeInMillis());
	      values.put(CalendarContract.Events.DTEND,
	          game.getCal().getTimeInMillis() + 5400000);
	      values.put(CalendarContract.Events.EVENT_TIMEZONE, 
	    		  TimeZone.getTimeZone("America/Chicago")
	          .getID());
	      cr.insert(CalendarContract.Events.CONTENT_URI, values);
      }
    }
    catch(Exception e) {
      util.err(e.getMessage());
    }
  }
}
