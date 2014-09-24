package com.vaadin.demo.dashboard.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.google.common.eventbus.Subscribe;
import com.vaadin.demo.dashboard.component.MovieDetailsWindow;
import com.vaadin.demo.dashboard.data.dummy.DummyDataProvider;
import com.vaadin.demo.dashboard.domain.Movie;
import com.vaadin.demo.dashboard.event.DashboardEventBus;
import com.vaadin.demo.dashboard.event.QuickTicketsEvent.BrowserResizeEvent;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Page;
import com.vaadin.server.WebBrowser;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Calendar;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table.TableTransferable;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.components.calendar.CalendarComponentEvents.BackwardEvent;
import com.vaadin.ui.components.calendar.CalendarComponentEvents.BackwardHandler;
import com.vaadin.ui.components.calendar.CalendarComponentEvents.EventClick;
import com.vaadin.ui.components.calendar.CalendarComponentEvents.EventClickHandler;
import com.vaadin.ui.components.calendar.CalendarComponentEvents.EventResize;
import com.vaadin.ui.components.calendar.CalendarComponentEvents.ForwardEvent;
import com.vaadin.ui.components.calendar.CalendarComponentEvents.ForwardHandler;
import com.vaadin.ui.components.calendar.CalendarComponentEvents.MoveEvent;
import com.vaadin.ui.components.calendar.CalendarTargetDetails;
import com.vaadin.ui.components.calendar.event.CalendarEvent;
import com.vaadin.ui.components.calendar.event.CalendarEventProvider;
import com.vaadin.ui.components.calendar.handler.BasicEventMoveHandler;
import com.vaadin.ui.components.calendar.handler.BasicEventResizeHandler;

public class ScheduleView extends CssLayout implements View {

    private final CssLayout catalog;

    private Window popup;

    // private CSSInject css;

    public ScheduleView() {
        setSizeFull();
        addStyleName("schedule");
        DashboardEventBus.register(this);

        // css = new CSSInject(UI.getCurrent());

        TabSheet tabs = new TabSheet();
        tabs.setSizeFull();
        tabs.addStyleName("borderless");
        addComponent(tabs);
        tabs.addComponent(buildCalendarView());

        catalog = new CssLayout();
        catalog.setCaption("Catalog");
        catalog.addStyleName("catalog");
        tabs.addComponent(catalog);

        for (final Movie movie : DummyDataProvider.getMovies()) {
            Image poster = new Image(movie.getTitle(), new ExternalResource(
                    movie.getThumbUrl()));
            CssLayout frame = new CssLayout();
            frame.addComponent(poster);
            frame.addLayoutClickListener(new LayoutClickListener() {
                @Override
                public void layoutClick(LayoutClickEvent event) {
                    if (event.getButton() == MouseButton.LEFT) {
                        Window w = new MovieDetailsWindow(null);
                        UI.getCurrent().addWindow(w);
                        w.focus();
                    }
                }
            });
            catalog.addComponent(frame);
        }

    }

    private Calendar cal;

    private final MovieEventProvider provider = new MovieEventProvider();

    private Component buildCalendarView() {
        VerticalLayout calendarLayout = new VerticalLayout();
        calendarLayout.setCaption("Calendar");
        calendarLayout.addStyleName("dummy");
        calendarLayout.setMargin(true);

        cal = new Calendar(provider);
        cal.setWidth("100%");
        cal.setHeight("1000px");

        // cal.setStartDate(new Date());
        // cal.setEndDate(new Date());

        cal.setHandler(new EventClickHandler() {
            @Override
            public void eventClick(EventClick event) {
                hideTray();
                getUI().removeWindow(popup);
                buildPopup((MovieEvent) event.getCalendarEvent());
                getUI().addWindow(popup);
                popup.focus();
                // if (!helpShown) {
                // ((QuickTicketsDashboardUI) getUI())
                // .getHelpManager()
                // .addOverlay(
                // "Change the movie",
                // "Try to drag the movie posters from the tray onto the poster in the window",
                // "poster").center();
                // helpShown = true;
                // }
            }
        });
        calendarLayout.addComponent(cal);

        cal.setFirstVisibleHourOfDay(11);
        cal.setLastVisibleHourOfDay(23);

        cal.setHandler(new BackwardHandler() {
            @Override
            public void backward(BackwardEvent event) {
                createEvents();
            }
        });

        cal.setHandler(new ForwardHandler() {
            @Override
            public void forward(ForwardEvent event) {
                createEvents();
            }
        });

        cal.setDropHandler(new DropHandler() {
            private static final long serialVersionUID = -8939822725278862037L;

            @Override
            public void drop(DragAndDropEvent event) {
                CalendarTargetDetails details = (CalendarTargetDetails) event
                        .getTargetDetails();
                TableTransferable transferable = (TableTransferable) event
                        .getTransferable();

                createEvent(details, transferable);
            }

            @Override
            public AcceptCriterion getAcceptCriterion() {
                return AcceptAll.get();
            }

        });

        cal.setHandler(new BasicEventMoveHandler() {
            @Override
            public void eventMove(MoveEvent event) {
                CalendarEvent calendarEvent = event.getCalendarEvent();
                if (calendarEvent instanceof MovieEvent) {
                    MovieEvent editableEvent = (MovieEvent) calendarEvent;

                    Date newFromTime = event.getNewStart();

                    // Update event dates
                    long length = editableEvent.getEnd().getTime()
                            - editableEvent.getStart().getTime();
                    setDates(editableEvent, newFromTime,
                            new Date(newFromTime.getTime() + length));
                    showTray();
                }
            }

            protected void setDates(MovieEvent event, Date start, Date end) {
                event.start = start;
                event.end = end;
            }
        });
        cal.setHandler(new BasicEventResizeHandler() {
            @Override
            public void eventResize(EventResize event) {
                Notification
                        .show("You're not allowed to change the movie duration");
            }
        });

        createEvents();

        return calendarLayout;
    }

    private void createEvent(CalendarTargetDetails details,
            TableTransferable transferable) {
        Date start = details.getDropTime();
        Date end = details.getDropTime();
        int endHour = (int) (1 + Math.round(Math.random()));
        int endMinutes = (int) (45 + Math.random() * 30);
        end.setHours(end.getHours() + endHour);
        end.setMinutes(endMinutes);
        MovieEvent newEvent = new MovieEvent(details.getDropTime(), end,
                DummyDataProvider.getMovies().iterator().next());
        provider.addEvent(newEvent);
    }

    boolean[] created = new boolean[366];

    void createEvents() {
        Date startDate = cal.getStartDate();

        int k = (startDate.getMonth()) * 30 + startDate.getDate();
        if (!created[k]) {

            for (int i = 0; i < 7; i++) {
                createEventsForDay(startDate);
                startDate.setDate(startDate.getDate() + 1);
            }

            created[k] = true;
        }

        // Add all movie cover images as classes to CSSInject
        String styles = "";
        for (Movie m : DummyDataProvider.getMovies()) {
            WebBrowser webBrowser = Page.getCurrent().getWebBrowser();

            String bg = "url(VAADIN/themes/" + UI.getCurrent().getTheme()
                    + "/img/event-title-bg.png), url(" + m.getPosterUrl() + ")";

            // IE8 doesn't support multiple background images
            if (webBrowser.isIE() && webBrowser.getBrowserMajorVersion() == 8) {
                bg = "url(" + m.getPosterUrl() + ")";
            }

            styles += ".v-calendar-event-" + m.titleSlug().replaceAll("&", "_")
                    + " .v-calendar-event-content {background-image:" + bg
                    + ";}";
        }

        Page.getCurrent().getStyles().add(styles);
    }

    void createEventsForDay(Date day) {
        Collection<Movie> movies = DummyDataProvider.getMovies();
        boolean[] used = new boolean[movies.size()];

        Date date = new Date(day.getTime());
        // Start from noon
        date.setHours(11);
        date.setMinutes(0);
        date.setSeconds(0);

        Iterator<Movie> iterator = movies.iterator();
        while (date.getHours() < 23) {
            // Get "random" movie

            int i = -1;
            int reallyStupidStuffForCodeThatIDontReallyUnderStandWTFItIsDoing = 0;
            do {
                i = (int) (Math.random() * movies.size());
                if (!used[i]) {
                    used[i] = true;
                    break;
                }
                if (reallyStupidStuffForCodeThatIDontReallyUnderStandWTFItIsDoing++ > used.length) {
                    break;
                }
            } while (true);

            Movie m = iterator.next();

            Date start = new Date(date.getTime());
            Date end = new Date(start.getTime());
            // int endHour = (int) (1 + Math.round(Math.random()));
            // int endMinutes = (int) (45 + Math.random() * 30);
            // end.setHours(end.getHours() + endHour);
            end.setMinutes(end.getMinutes() + m.getDuration());

            MovieEvent e = new MovieEvent(start, end, m);
            provider.addEvent(e);

            date.setDate(end.getDate());
            date.setHours(end.getHours());
            date.setMinutes((int) (end.getMinutes() + 15 + (Math.random() * 60)));

            if (date.getDate() > day.getDate())
                break;
        }

    }

    class MovieEventProvider implements CalendarEventProvider {
        private final List<CalendarEvent> events = new ArrayList<CalendarEvent>();

        @Override
        public List<CalendarEvent> getEvents(Date startDate, Date endDate) {
            return events;
        }

        public void addEvent(CalendarEvent MovieEvent) {
            events.add(MovieEvent);
        }

    }

    public class MovieEvent implements CalendarEvent {

        Date start;
        Date end;
        String caption;
        Movie movie;

        public MovieEvent(Date start, Date end, Movie movie) {
            this.start = start;
            this.end = end;
            this.caption = movie.getTitle();
            this.movie = movie;
        }

        @Override
        public Date getStart() {
            return start;
        }

        @Override
        public Date getEnd() {
            return end;
        }

        @Override
        public String getCaption() {
            return caption;
        }

        @Override
        public String getDescription() {
            return "";
        }

        @Override
        public String getStyleName() {
            return movie.titleSlug().replaceAll("&", "_");
        }

        @Override
        public boolean isAllDay() {
            return false;
        }

        public Movie getMovie() {
            return movie;
        }

        public void setMovie(Movie movie) {
            this.movie = movie;
        }

        public void setStart(Date start) {
            this.start = start;
        }

        public void setEnd(Date end) {
            this.end = end;
        }

        public void setCaption(String caption) {
            this.caption = caption;
        }

    }

    void buildPopup(final MovieEvent event) {
        popup = new MovieDetailsWindow(event);
    }

    HorizontalLayout tray;

    void buildTray() {
        if (tray != null)
            return;

        tray = new HorizontalLayout();
        tray.setWidth("100%");
        tray.addStyleName("tray");
        tray.setSpacing(true);
        tray.setMargin(true);

        Label warning = new Label(
                "You have unsaved changes made to the schedule");
        warning.addStyleName("warning");
        warning.addStyleName("icon-attention");
        tray.addComponent(warning);
        tray.setComponentAlignment(warning, Alignment.MIDDLE_LEFT);
        tray.setExpandRatio(warning, 1);

        ClickListener close = new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                tray.removeStyleName("v-animate-reveal");
                tray.addStyleName("v-animate-hide");
            }
        };

        Button confirm = new Button("Confirm");
        confirm.addStyleName("wide");
        confirm.addStyleName("default");
        confirm.addClickListener(close);
        tray.addComponent(confirm);
        tray.setComponentAlignment(confirm, Alignment.MIDDLE_LEFT);

        Button discard = new Button("Discard");
        discard.addStyleName("wide");
        discard.addClickListener(close);
        tray.addComponent(discard);
        tray.setComponentAlignment(discard, Alignment.MIDDLE_LEFT);
    }

    // boolean helpShown = false;

    void showTray() {
        buildTray();
        tray.removeStyleName("v-animate-hide");
        tray.addStyleName("v-animate-reveal");
        addComponent(tray);
    }

    void hideTray() {
        if (tray != null)
            removeComponent(tray);
    }

    @Subscribe
    public void browserWindowResized(BrowserResizeEvent event) {
        if (Page.getCurrent().getBrowserWindowWidth() < 800
                && cal.getLastVisibleDayOfWeek() > cal
                        .getFirstVisibleDayOfWeek()) {
            cal.setEndDate(cal.getStartDate());
        }
    }

    @Override
    public void enter(ViewChangeEvent event) {
    }

}
