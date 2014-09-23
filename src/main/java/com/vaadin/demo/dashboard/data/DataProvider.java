package com.vaadin.demo.dashboard.data;

import java.util.Collection;

import com.vaadin.demo.dashboard.domain.DashboardNotification;
import com.vaadin.demo.dashboard.domain.MovieRevenue;
import com.vaadin.demo.dashboard.domain.Transaction;
import com.vaadin.demo.dashboard.domain.User;

public interface DataProvider {
    public Collection<Transaction> getTransactions();

    public Collection<MovieRevenue> getRevenueByMovie(long id);

    public User authenticate(String userName, String password);

    public Collection<MovieRevenue> getMovieRevenues();

    public int getUnreadNotificationsCount();

    public Collection<DashboardNotification> getNotifications();

    public double getTotalSum();
}
