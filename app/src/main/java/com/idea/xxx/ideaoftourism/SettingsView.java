package com.idea.xxx.ideaoftourism;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.widget.CheckBox;

import com.github.angads25.toggle.widget.LabeledSwitch;

import static android.content.Context.MODE_PRIVATE;

public class SettingsView {
    private Activity m_activity;
    SharedPreferences sharedPreferences;
    String sharedPrefFile = "com.idea.xxx.ideaoftourism";


    public SettingsView(SettingsActivity settingsActivity) {
        m_activity = settingsActivity;
        initSettings();

    }

    private void initSettings() {
        sharedPreferences = m_activity.getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        SharedPreferences.Editor preferencesEditor = sharedPreferences.edit();

        LabeledSwitch labeledSwitch = m_activity.findViewById(R.id.switch_tourist);
        labeledSwitch.setOnToggledListener((toggleableView, isOn) -> {
            if(isOn){
                preferencesEditor.putString("Tourist mode", "ON");
                preferencesEditor.apply();

            }
            if(!isOn){
                preferencesEditor.putString("Tourist mode", "OFF");
                preferencesEditor.apply();
            }

        });

        LabeledSwitch labeledSwitch1 = m_activity.findViewById(R.id.switch_hologram);
        labeledSwitch1.setOnToggledListener((toggleableView, isOn) -> {
            if(isOn){
                preferencesEditor.putString("Hologram mode", "ON");
                preferencesEditor.apply();
            }
            if(!isOn){
                preferencesEditor.putString("Hologram mode", "OFF");
                preferencesEditor.apply();
            }

        });

        CheckBox checkBox_most = m_activity.findViewById(R.id.checkBox_most);
        CheckBox checkBox_three = m_activity.findViewById(R.id.checkBox_three);
        CheckBox checkBox_one = m_activity.findViewById(R.id.checkBox_one);
        CheckBox checkBox_light = m_activity.findViewById(R.id.checkBox_light);
        CheckBox checkBox_dark = m_activity.findViewById(R.id.checkBox_dark);

        if(checkBox_three.isChecked()){
            checkBox_one.setChecked(false);
            checkBox_most.setChecked(false);
            checkBox_three.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#3b9855")));
            preferencesEditor.putString("Route", "three");
            preferencesEditor.apply();
        }else if(checkBox_one.isChecked()){
            checkBox_one.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#3b9855")));
            checkBox_most.setChecked(false);
            checkBox_three.setChecked(false);
            preferencesEditor.putString("Route", "one");
            preferencesEditor.apply();
        }else if(checkBox_most.isChecked()){
            checkBox_most.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#3b9855")));
            checkBox_one.setChecked(false);
            checkBox_three.setChecked(false);
            preferencesEditor.putString("Route", "most");
            preferencesEditor.apply();
        }

        if(checkBox_dark.isChecked()){
            checkBox_dark.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#3b9855")));
            checkBox_light.setChecked(false);
            preferencesEditor.putString("View", "dark");
            preferencesEditor.apply();
        }else if(checkBox_light.isChecked()){
            checkBox_light.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#3b9855")));
            checkBox_dark.setChecked(false);
            preferencesEditor.putString("View", "light");
            preferencesEditor.apply();
        }
    }
}
