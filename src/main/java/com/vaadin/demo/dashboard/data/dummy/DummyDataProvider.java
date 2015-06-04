package com.vaadin.demo.dashboard.data.dummy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vaadin.demo.dashboard.data.DataProvider;
import com.vaadin.demo.dashboard.domain.DashboardNotification;
import com.vaadin.demo.dashboard.domain.Movie;
import com.vaadin.demo.dashboard.domain.MovieRevenue;
import com.vaadin.demo.dashboard.domain.Transaction;
import com.vaadin.demo.dashboard.domain.User;
import com.vaadin.server.VaadinRequest;
import com.vaadin.util.CurrentInstance;

/**
 * A dummy implementation for the backend API.
 */
public class DummyDataProvider implements DataProvider {

    // TODO: Get API key from http://developer.rottentomatoes.com
    private static final String ROTTEN_TOMATOES_API_KEY = null;

    /* List of countries and cities for them */
    private static Multimap<String, String> countryToCities;
    private static Date lastDataUpdate;
    private static Collection<Movie> movies;
    private static Multimap<Long, Transaction> transactions;
    private static Multimap<Long, MovieRevenue> revenue;

    private static Random rand = new Random();

    private final Collection<DashboardNotification> notifications = DummyDataGenerator
            .randomNotifications();

    /**
     * Initialize the data for this application.
     */
    public DummyDataProvider() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        if (lastDataUpdate == null || lastDataUpdate.before(cal.getTime())) {
            refreshStaticData();
            lastDataUpdate = new Date();
        }
    }

    private void refreshStaticData() {
        countryToCities = loadTheaterData();
        movies = loadMoviesData();
        transactions = generateTransactionsData();
        revenue = countRevenues();
    }

    /**
     * Get a list of movies currently playing in theaters.
     *
     * @return a list of Movie objects
     */
    @Override
    public Collection<Movie> getMovies() {
        return Collections.unmodifiableCollection(movies);
    }

    /**
     * Initialize the list of movies playing in theaters currently. Uses the
     * Rotten Tomatoes API to get the list. The result is cached to a local file
     * for 24h (daily limit of API calls is 10,000).
     *
     * @return
     */
    private static Collection<Movie> loadMoviesData() {

        JsonObject json = null;
        File cache;
        VaadinRequest vaadinRequest = CurrentInstance.get(VaadinRequest.class);

        File baseDirectory = vaadinRequest.getService().getBaseDirectory();
        cache = new File(baseDirectory + "/movies.txt");

        try {
            if (cache.exists()
                    && System.currentTimeMillis() < cache.lastModified()
                            + (1000 * 60 * 60 * 24)) {
                // Use cache if it's under 24h old
                json = readJsonFromFile(cache);
            } else {
                if (ROTTEN_TOMATOES_API_KEY != null) {
                    try {
                        json = readJsonFromUrl("http://api.rottentomatoes.com/api/public/v1.0/lists/movies/in_theaters.json?page_limit=30&apikey="
                                + ROTTEN_TOMATOES_API_KEY);
                        // Store in cache
                        FileWriter fileWriter = new FileWriter(cache);
                        fileWriter.write(json.toString());
                        fileWriter.close();
                    } catch (Exception e) {
                        json = readJsonFromFile(new File(baseDirectory
                                + "/movies-fallback.txt"));
                    }
                } else {
                    json = readJsonFromFile(new File(baseDirectory
                            + "/movies-fallback.txt"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Collection<Movie> result = new ArrayList<Movie>();
        if (json != null) {
            JsonArray moviesJson;

            moviesJson = json.getAsJsonArray("movies");
            for (int i = 0; i < moviesJson.size(); i++) {
                JsonObject movieJson = moviesJson.get(i).getAsJsonObject();
                JsonObject posters = movieJson.get("posters").getAsJsonObject();
                if (!posters.get("profile").getAsString()
                        .contains("poster_default")) {
                    Movie movie = new Movie();
                    movie.setId(i);
                    movie.setTitle(movieJson.get("title").getAsString());
                    try {
                        movie.setDuration(movieJson.get("runtime").getAsInt());
                    } catch (Exception e) {
                        // No need to handle this exception
                    }
                    movie.setSynopsis(movieJson.get("synopsis").getAsString());
                    movie.setThumbUrl(posters.get("profile").getAsString()
                            .replace("_tmb", "_320"));
                    movie.setPosterUrl(posters.get("detailed").getAsString()
                            .replace("_tmb", "_640"));

                    try {
                        JsonObject releaseDates = movieJson
                                .get("release_dates").getAsJsonObject();
                        String datestr = releaseDates.get("theater")
                                .getAsString();
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                        movie.setReleaseDate(df.parse(datestr));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        movie.setScore(movieJson.get("ratings")
                                .getAsJsonObject().get("critics_score")
                                .getAsInt());
                    } catch (Exception e) {
                        // No need to handle this exception
                    }

                    result.add(movie);

                }
            }
        }
        return result;
    }

    /* JSON utility method */
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    /* JSON utility method */
    private static JsonObject readJsonFromUrl(String url) throws IOException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is,
                    Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JsonElement jelement = new JsonParser().parse(jsonText);
            JsonObject jobject = jelement.getAsJsonObject();
            return jobject;
        } finally {
            is.close();
        }
    }

    /* JSON utility method */
    private static JsonObject readJsonFromFile(File path) throws IOException {
        BufferedReader rd = new BufferedReader(new FileReader(path));
        String jsonText = readAll(rd);
        JsonElement jelement = new JsonParser().parse(jsonText);
        JsonObject jobject = jelement.getAsJsonObject();
        return jobject;
    }

    /**
     * =========================================================================
     * Countries, cities, theaters and rooms
     * =========================================================================
     */

    static List<String> theaters = new ArrayList<String>() {
        private static final long serialVersionUID = 1L;
        {
            add("Threater 1");
            add("Threater 2");
            add("Threater 3");
            add("Threater 4");
            add("Threater 5");
            add("Threater 6");
        }
    };

    static List<String> rooms = new ArrayList<String>() {
        private static final long serialVersionUID = 1L;
        {
            add("Room 1");
            add("Room 2");
            add("Room 3");
            add("Room 4");
            add("Room 5");
            add("Room 6");
        }
    };

    /**
     * Parse the list of countries and cities
     */
    private static Multimap<String, String> loadTheaterData() {

        /* First, read the text file into a string */
        StringBuffer fileData = new StringBuffer(2000);
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                DummyDataProvider.class.getResourceAsStream("cities.txt")));

        char[] buf = new char[1024];
        int numRead = 0;
        try {
            while ((numRead = reader.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
                buf = new char[1024];
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String list = fileData.toString();

        /*
         * The list has rows with tab delimited values. We want the second (city
         * name) and last (country name) values, and build a Map from that.
         */
        Multimap<String, String> countryToCities = MultimapBuilder.hashKeys()
                .arrayListValues().build();
        for (String line : list.split("\n")) {
            String[] tabs = line.split("\t");
            String city = tabs[1];
            String country = tabs[tabs.length - 2];

            if (!countryToCities.containsKey(country)) {
                countryToCities.putAll(country, new ArrayList<String>());
            }
            countryToCities.get(country).add(city);
        }

        return countryToCities;

    }

    /**
     * Create a list of dummy transactions
     *
     * @return
     */
    private Multimap<Long, Transaction> generateTransactionsData() {
        Multimap<Long, Transaction> result = MultimapBuilder.hashKeys()
                .arrayListValues().build();

        for (Movie movie : movies) {
            result.putAll(movie.getId(), new ArrayList<Transaction>());

            Calendar cal = Calendar.getInstance();
            int daysSubtractor = rand.nextInt(150) + 30;
            cal.add(Calendar.DAY_OF_YEAR, -daysSubtractor);

            Calendar lastDayOfWeek = Calendar.getInstance();
            lastDayOfWeek.add(Calendar.DAY_OF_YEAR,
                    Calendar.SATURDAY - cal.get(Calendar.DAY_OF_WEEK));

            while (cal.before(lastDayOfWeek)) {

                int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
                if (hourOfDay > 10 && hourOfDay < 22) {

                    Transaction transaction = new Transaction();
                    transaction.setMovieId(movie.getId());
                    transaction.setTitle(movie.getTitle());

                    // Country
                    Object[] array = countryToCities.keySet().toArray();
                    int i = (int) (Math.random() * (array.length - 1));
                    String country = array[i].toString();
                    transaction.setCountry(country);

                    transaction.setTime(cal.getTime());

                    // City
                    Collection<String> cities = countryToCities.get(country);
                    transaction.setCity(cities.iterator().next());

                    // Theater
                    String theater = theaters
                            .get((int) (rand.nextDouble() * (theaters.size() - 1)));
                    transaction.setTheater(theater);

                    // Room
                    String room = rooms.get((int) (rand.nextDouble() * (rooms
                            .size() - 1)));
                    transaction.setRoom(room);

                    // Title
                    int randomIndex = (int) (Math.abs(rand.nextGaussian()) * (movies
                            .size() / 2.0 - 1));
                    while (randomIndex >= movies.size()) {
                        randomIndex = (int) (Math.abs(rand.nextGaussian()) * (movies
                                .size() / 2.0 - 1));
                    }

                    // Seats
                    int seats = (int) (1 + rand.nextDouble() * 3);
                    transaction.setSeats(seats);

                    // Price (approx. USD)
                    double price = seats * (2 + (rand.nextDouble() * 8));
                    transaction.setPrice(price);

                    result.get(movie.getId()).add(transaction);
                }

                cal.add(Calendar.SECOND, rand.nextInt(500000) + 5000);
            }
        }

        return result;

    }

    public static Movie getMovieForTitle(String title) {
        for (Movie movie : movies) {
            if (movie.getTitle().equals(title)) {
                return movie;
            }
        }
        return null;
    }

    @Override
    public User authenticate(String userName, String password) {
        User user = new User();
        user.setFirstName(DummyDataGenerator.randomFirstName());
        user.setLastName(DummyDataGenerator.randomLastName());
        user.setRole("admin");
        String email = user.getFirstName().toLowerCase() + "."
                + user.getLastName().toLowerCase() + "@"
                + DummyDataGenerator.randomCompanyName().toLowerCase() + ".com";
        user.setEmail(email.replaceAll(" ", ""));
        user.setLocation(DummyDataGenerator.randomWord(5, true));
        user.setBio("Quis aute iure reprehenderit in voluptate velit esse."
                + "Cras mattis iudicium purus sit amet fermentum.");
        return user;
    }

    @Override
    public Collection<Transaction> getRecentTransactions(int count) {
        List<Transaction> orderedTransactions = Lists.newArrayList(transactions
                .values());
        Collections.sort(orderedTransactions, new Comparator<Transaction>() {
            @Override
            public int compare(Transaction o1, Transaction o2) {
                return o2.getTime().compareTo(o1.getTime());
            }
        });
        return orderedTransactions.subList(0,
                Math.min(count, transactions.values().size() - 1));
    }

    private Multimap<Long, MovieRevenue> countRevenues() {
        Multimap<Long, MovieRevenue> result = MultimapBuilder.hashKeys()
                .arrayListValues().build();
        for (Movie movie : movies) {
            result.putAll(movie.getId(), countMovieRevenue(movie));
        }
        return result;
    }

    private Collection<MovieRevenue> countMovieRevenue(Movie movie) {
        Map<Date, Double> dailyIncome = new HashMap<Date, Double>();
        for (Transaction transaction : transactions.get(movie.getId())) {
            Date day = getDay(transaction.getTime());

            Double currentValue = dailyIncome.get(day);
            if (currentValue == null) {
                currentValue = 0.0;
            }
            dailyIncome.put(day, currentValue + transaction.getPrice());
        }

        Collection<MovieRevenue> result = new ArrayList<MovieRevenue>();

        List<Date> dates = new ArrayList<Date>(dailyIncome.keySet());
        Collections.sort(dates);

        double revenueSoFar = 0.0;
        for (Date date : dates) {
            MovieRevenue movieRevenue = new MovieRevenue();
            movieRevenue.setTimestamp(date);
            revenueSoFar += dailyIncome.get(date);
            movieRevenue.setRevenue(revenueSoFar);
            movieRevenue.setTitle(movie.getTitle());
            result.add(movieRevenue);
        }

        return result;
    }

    @Override
    public Collection<MovieRevenue> getDailyRevenuesByMovie(long id) {
        return Collections.unmodifiableCollection(revenue.get(id));
    }

    private Date getDay(Date time) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(time);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        return cal.getTime();
    }

    @Override
    public Collection<MovieRevenue> getTotalMovieRevenues() {
        return Collections2.transform(movies,
                new Function<Movie, MovieRevenue>() {
                    @Override
                    public MovieRevenue apply(Movie input) {
                        return Iterables.getLast(getDailyRevenuesByMovie(input
                                .getId()));
                    }
                });
    }

    @Override
    public int getUnreadNotificationsCount() {
        Predicate<DashboardNotification> unreadPredicate = new Predicate<DashboardNotification>() {
            @Override
            public boolean apply(DashboardNotification input) {
                return !input.isRead();
            }
        };
        return Collections2.filter(notifications, unreadPredicate).size();
    }

    @Override
    public Collection<DashboardNotification> getNotifications() {
        for (DashboardNotification notification : notifications) {
            notification.setRead(true);
        }
        return Collections.unmodifiableCollection(notifications);
    }

    @Override
    public double getTotalSum() {
        double result = 0;
        for (Transaction transaction : transactions.values()) {
            result += transaction.getPrice();
        }
        return result;
    }

    @Override
    public Movie getMovie(final long movieId) {
        return Iterables.find(movies, new Predicate<Movie>() {
            @Override
            public boolean apply(Movie input) {
                return input.getId() == movieId;
            }
        });
    }

    @Override
    public Collection<Transaction> getTransactionsBetween(final Date startDate,
            final Date endDate) {
        return Collections2.filter(transactions.values(),
                new Predicate<Transaction>() {
                    @Override
                    public boolean apply(Transaction input) {
                        return !input.getTime().before(startDate)
                                && !input.getTime().after(endDate);
                    }
                });
    }

}
