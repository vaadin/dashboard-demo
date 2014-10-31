package com.vaadin.demo.dashboard.data;

import java.util.Collection;
import java.util.Date;

import com.vaadin.demo.dashboard.domain.DashboardNotification;
import com.vaadin.demo.dashboard.domain.Movie;
import com.vaadin.demo.dashboard.domain.MovieRevenue;
import com.vaadin.demo.dashboard.domain.Transaction;
import com.vaadin.demo.dashboard.domain.User;

/**
 * QuickTickets Dashboard backend API.
 */
public interface DataProvider {
    /**
     * @param count
     *            Number of transactions to fetch.
     * @return A Collection of most recent transactions.
     */
    Collection<Transaction> getRecentTransactions(int count);

    /**
     * @param id
     *            Movie identifier.
     * @return A Collection of daily revenues for the movie.
     */
    Collection<MovieRevenue> getDailyRevenuesByMovie(long id);

    /**
     * @return Total revenues for each listed movie.
     */
    Collection<MovieRevenue> getTotalMovieRevenues();

    /**
     * @param userName
     * @param password
     * @return Authenticated used.
     */
    User authenticate(String userName, String password);

    /**
     * @return The number of unread notifications for the current user.
     */
    int getUnreadNotificationsCount();

    /**
     * @return Notifications for the current user.
     */
    Collection<DashboardNotification> getNotifications();

    /**
     * @return The total summed up revenue of sold movie tickets
     */
    double getTotalSum();

    /**
     * @return A Collection of movies.
     */
    Collection<Movie> getMovies();

    /**
     * @param movieId
     *            Movie's identifier
     * @return A Movie instance for the given id.
     */
    Movie getMovie(long movieId);

    /**
     * @param startDate
     * @param endDate
     * @return A Collection of Transactions between the given start and end
     *         dates.
     */
    Collection<Transaction> getTransactionsBetween(Date startDate, Date endDate);
}
