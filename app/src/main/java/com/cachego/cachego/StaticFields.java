package com.cachego.cachego;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class StaticFields {
    private static String author;
    private static double lat;
    private static double lon;
    private static String SelectedId;
    private static ArrayList<Cache> cacheArrayList;
    private static ArrayList<String> alFavorites;
    private static Fragment PreviousFragment;
    private static String metricSystem;
    private static String easy, normal, hard;
    private static boolean PreferenceUpdate;
    private static String UserName;

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public boolean isPreferenceUpdate() {
        return PreferenceUpdate;
    }

    public void setPreferenceUpdate(boolean preferenceUpdate) {
        PreferenceUpdate = preferenceUpdate;
    }

    public String getEasy() {
        return easy;
    }

    public void setEasy(String easy) {
        this.easy = easy;
    }

    public String getNormal() {
        return normal;
    }

    public void setNormal(String normal) {
        this.normal = normal;
    }

    public String getHard() {
        return hard;
    }

    public void setHard(String hard) {
        this.hard = hard;
    }

    public String getMetricSystem() {
        return metricSystem;
    }

    public void setMetricSystem(String metricSystem) {
        this.metricSystem = metricSystem;
    }

    public Fragment getPreviousFragment() {
        return PreviousFragment;
    }

    public void setPreviousFragment(Fragment previousFragment) {
        PreviousFragment = previousFragment;
    }

    public ArrayList<String> getAlFavorites() {
        return alFavorites;
    }

    public void setAlFavorites(ArrayList<String> alFavorites) {
        this.alFavorites = alFavorites;
    }

    public ArrayList<Cache> getCacheArrayList() {
        return cacheArrayList;
    }

    public void setCacheArrayList(ArrayList<Cache> cacheArrayList) {
        this.cacheArrayList = cacheArrayList;
    }

    public String getSelectedId() {
        return SelectedId;
    }

    public void setSelectedId(String selectedId) {
        SelectedId = selectedId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        StaticFields.author = author;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        StaticFields.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        StaticFields.lon = lon;
    }
}
