package com.vaadin.demo.dashboard.data;

import java.util.Collection;
import java.util.Date;

import com.vaadin.demo.dashboard.domain.DashboardNotification;
import com.vaadin.demo.dashboard.domain.Movie;
import com.vaadin.demo.dashboard.domain.MovieRevenue;
import com.vaadin.demo.dashboard.domain.Transaction;
import com.vaadin.demo.dashboard.domain.User;

public interface DataProvider {
    public Collection<Transaction> getRecentTransactions(int count);

    public Collection<MovieRevenue> getDailyRevenuesByMovie(long id);

    public Collection<MovieRevenue> getTotalMovieRevenues();

    public User authenticate(String userName, String password);

    public int getUnreadNotificationsCount();

    public Collection<DashboardNotification> getNotifications();

    public double getTotalSum();

    public Collection<Movie> getMovies();

    public Movie getMovie(long movieId);

    public Collection<Transaction> getTransactionsBetween(Date startDate,
            Date endDate);
}
