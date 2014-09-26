package com.vaadin.demo.dashboard.domain;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.JsonObject;

public class Movie {
    private final long id;
    private final String title;
    private final String synopsis;
    private final String thumbUrl;
    private final String posterUrl;
    private final int duration;
    private Date releaseDate;

    private int score;

    public Movie(long id, String title, int runtimeMinutes, String synopsis,
            String thumbUrl, String posterUrl, JsonObject releaseDates,
            JsonObject critics) {
        this.id = id;
        this.title = title;
        this.synopsis = synopsis;
        this.thumbUrl = thumbUrl.replace("_tmb", "_320");
        this.posterUrl = posterUrl.replace("_tmb", "_640");
        this.duration = runtimeMinutes;
        try {
            String datestr = releaseDates.get("theater").getAsString();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            releaseDate = df.parse(datestr);
            score = critics.get("critics_score").getAsInt();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String titleSlug() {
        return title.toLowerCase().replace(' ', '-').replace(":", "")
                .replace("'", "").replace(",", "").replace(".", "")
                .replaceAll("/", "_").replaceAll("\\?", "");
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getTitle() {
        return title;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public int getDuration() {
        return duration;
    }

    public long getId() {
        return id;
    }

}
