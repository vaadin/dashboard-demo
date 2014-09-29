package com.vaadin.demo.dashboard.event;

import java.util.Collection;

import com.vaadin.demo.dashboard.domain.Transaction;
import com.vaadin.demo.dashboard.view.QuickTicketsView;

public class QuickTicketsEvent {

    public static class UserLoginRequestedEvent {
        private final String userName, password;

        public UserLoginRequestedEvent(String userName, String password) {
            this.userName = userName;
            this.password = password;
        }

        public String getUserName() {
            return userName;
        }

        public String getPassword() {
            return password;
        }
    }

    public static class BrowserResizeEvent {

    }

    public static class UserLoggedOutEvent {

    }

    public static class NotificationsCountUpdatedEvent {
    }

    public static class ReportsCountUpdatedEvent {
        private final int count;

        public ReportsCountUpdatedEvent(int count) {
            this.count = count;
        }

        public int getCount() {
            return count;
        }

    }

    public static class TransactionReportEvent {
        private final Collection<Transaction> transactions;

        public TransactionReportEvent(Collection<Transaction> transactions) {
            this.transactions = transactions;
        }

        public Collection<Transaction> getTransactions() {
            return transactions;
        }
    }

    public static class DashboardEditEvent {
        private final String name;

        public DashboardEditEvent(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

    }

    public static class PostViewChangeEvent {
        private final QuickTicketsView view;

        public PostViewChangeEvent(QuickTicketsView view) {
            this.view = view;
        }

        public QuickTicketsView getView() {
            return view;
        }
    }

    public static class CloseOpenWindowsEvent {
    }

}
