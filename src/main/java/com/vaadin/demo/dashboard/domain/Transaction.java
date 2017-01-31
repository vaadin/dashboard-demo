package com.vaadin.demo.dashboard.domain;

import java.util.Date;

public final class Transaction {
    private Date time;
    private String country;
    private String city;
    private String theater;
    private String room;
    private int seats;
    private double price;
    private long movieId;
    private String title;

    public long getMovieId() {
        return movieId;
    }

    public void setMovieId(final long movieId) {
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(final Date time) {
        this.time = time;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(final String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(final String city) {
        this.city = city;
    }

    public String getTheater() {
        return theater;
    }

    public void setTheater(final String theater) {
        this.theater = theater;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(final String room) {
        this.room = room;
    }

    public int getSeats() {
        return seats;
    }

    public void setSeats(final int seats) {
        this.seats = seats;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(final double price) {
        this.price = price;
    }

}
