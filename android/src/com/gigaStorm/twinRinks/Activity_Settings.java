package com.gigaStorm.twinRinks;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.text.InputType;

/**
 * <code>Activity_Settings</code> allows the user to change application settings
 * and choose preferences.
 * 
 * @author Andrew Mass
 * @see PreferenceActivity
 */
public class Activity_Settings extends PreferenceActivity {

  private PreferenceCategory addTeamCategory;

  private ArrayList<Model_Team> userTeams;

  private Data_MemoryManager memoryManager;

  private Preference addNewTeamPref;

  private ActionBar actionBar;

  private Util util;

  @SuppressWarnings("deprecation")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Adds the settings XML to the activity
    addPreferencesFromResource(R.xml.settings_main);

    getWindow().setBackgroundDrawableResource(android.R.color.black);

    actionBar = getActionBar();
    actionBar.setHomeButtonEnabled(true);
    actionBar.setDisplayHomeAsUpEnabled(true);
    actionBar.setTitle("Settings");

    addTeamCategory = (PreferenceCategory) findPreference("addTeamCategory");
    addTeamCategory.setOrderingAsAdded(false);

    addNewTeamPref = findPreference("addNewTeamPref");
    addNewTeamPref.setOrder(999);
    addNewTeamPref
        .setOnPreferenceClickListener(new OnPreferenceClickListener() {
          @Override
          public boolean onPreferenceClick(Preference preference) {
            showAddTeamPopup();
            return true;
          }
        });

    util = new Util(this);

    memoryManager = new Data_MemoryManager(this);
    userTeams = memoryManager.getUserTeams();
    updatePreferencesFromTeams();

    final EditTextPreference autoLogInUsername = (EditTextPreference) findPreference("autoLogInUsername");
    final EditTextPreference autoLogInPassword = (EditTextPreference) findPreference("autoLogInPassword");

    autoLogInUsername.getEditText()
        .setInputType(
            InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
    autoLogInPassword.getEditText().setInputType(
        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

    final CheckBoxPreference autoLogInCheckBox = (CheckBoxPreference) findPreference("autoLogInCheckbox");
    autoLogInCheckBox
        .setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
          @Override
          public boolean onPreferenceChange(Preference preference,
              Object newValue) {
            if(autoLogInCheckBox.isChecked()) {
              autoLogInPassword.setEnabled(false);
              autoLogInUsername.setEnabled(false);
            }
            else {
              autoLogInPassword.setEnabled(true);
              autoLogInUsername.setEnabled(true);
            }
            return true;
          }
        });

    if(autoLogInCheckBox.isChecked() == false) {
      autoLogInPassword.setEnabled(false);
      autoLogInUsername.setEnabled(false);
    }
    else {
      autoLogInPassword.setEnabled(true);
      autoLogInUsername.setEnabled(true);
    }

    final Preference clearDatabasePreference = findPreference("clearDatabasePreference");
    final Context context = this;
    clearDatabasePreference
        .setOnPreferenceClickListener(new OnPreferenceClickListener() {
          @Override
          public boolean onPreferenceClick(Preference preference) {
            Data_DbHelper dbHelper = new Data_DbHelper(context);
            dbHelper.deleteAllGameRecords();
            dbHelper.deleteAllTeamRecords();
            dbHelper.deleteAllUserTeamRecords();
            dbHelper.close();

            userTeams = memoryManager.getUserTeams();
            updatePreferencesFromTeams();
            util.toast("Cleared all loaded data. Refresh the app.");
            return true;
          }
        });
  }

  @Override
  protected void onPause() {
    memoryManager.saveUserTeams(userTeams);
    super.onPause();
  }

  private void updatePreferencesFromTeams() {
    addTeamCategory.removeAll();
    for(int i = 0; i < userTeams.size(); i++) {
      addTeamCategory.addPreference(getNewPreference(userTeams.get(i)));
    }

    addTeamCategory.addPreference(addNewTeamPref);
  }

  private void addNewTeam(Model_Team team) {
    if(getIndexOfTeam(team) == -1) {
      userTeams.add(team);
      addTeamCategory.addPreference(getNewPreference(team));
    }
    else {
      util.toast("Team already entered");
    }
  }

  private int getIndexOfTeam(Model_Team team) {
    for(int i = 0; i < userTeams.size(); i++) {
      if(userTeams.get(i).getLeague().equals(team.getLeague())
          && userTeams.get(i).getTeamName().equals(team.getTeamName())) {
        return i;
      }
    }
    return -1;
  }

  private Preference getNewPreference(Model_Team teamP) {
    final Context context = this;
    final Model_Team team = teamP;

    Preference newPref = new Preference(this);
    newPref.setTitle(team.getLeague() + "-" + team.getTeamName());
    newPref.setOrder(addTeamCategory.getPreferenceCount() - 1);

    newPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(final Preference preference) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Remove this team?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int id) {
            userTeams.remove(getIndexOfTeam(team));
            addTeamCategory.removePreference(preference);
          }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int id) {
            dialog.cancel();
          }
        });

        AlertDialog alert = builder.create();
        alert.show();
        return true;
      }
    });
    return newPref;
  }

  private void showAddTeamPopup() {
    final Context context = this;
    final ArrayList<Model_Team> teams = memoryManager.getTeams();

    final CharSequence[] teamStrings = new CharSequence[teams.size()];
    for(int i = 0; i < teams.size(); i++) {
      teamStrings[i] = teams.get(i).toString();
    }

    AlertDialog.Builder builder = new AlertDialog.Builder(context);
    builder.setTitle("Select A Team:");
    builder.setItems(teamStrings, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, final int item) {
        addNewTeam(teams.get(item));
      }
    });
    AlertDialog alert = builder.create();
    alert.show();
  }
}
