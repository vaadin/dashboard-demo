Vaadin QuickTickets Dashboard Demo
==================================

Responsive application demo/template built using only server-side Java with [Vaadin Framework](https://vaadin.com/framework). Showcasing big data, data visualization, drag 'n' drop and other Vaadin features.

[![View the application](https://vaadin.com/documents/10187/2487938/Dashboard+Demo+2014/a37b2c4d-c941-48fe-97c3-ad5a60586882?t=1412769929183)](http://demo.vaadin.com/dashboard)

Running the App
==
Run 'mvn -Pproduction-mode jetty:run' to run in a local jetty. Open in localhost:8080

Run the Maven 'install' target and deploy the resulting WAR file to your Java application server.

You need a license for Vaadin Charts to compile the widgetset. You can get a free 30 day trial license by going to https://vaadin.com/directory#addon/vaadin-charts and clicking the orange "Free trial key" button. It gives you a trial key. [See the help section](https://vaadin.com/directory/help/installing-cval-license) which shows you how to install the key.

Basically you need to create a file name ".vaadin.charts.developer.license" in your HOME directory, and place the key there.

Run 'mvn -Pproduction-mode verify' to run the Vaadin TestBench tests. 

*Note*: You need a valid [Vaadin TestBench license](https://vaadin.com/add-ons/testbench) and [Firefox browser](https://www.mozilla.org/firefox/) installed to run the tests.

# Running Scalability Tests

In order to run the scalability tests locally:

1. Make sure you are using Java 8 (Gatling Maven plugin does not yet work with Java 9+)

1. Run the app (described [above](#running-the-app))

1. Open terminal in the project root

1. Start a test from the command line:

    ```bash
    mvn -Pscalability gatling:execute
    ```

1. Test results are stored into target folder (e.g. to ```target/gatling/BaristaFlow-1487784042461/index.html```)

1. By default the scalability test starts 2 user session at a 2 s interval for one repeat, all of which connect to a locally running the app.
These defaults can be overridden with the `gatling.sessionCount`, `gatling.sessionStartInterval` `gatling.sessionRepeats`, and `gatling.baseUrl` system properties.
See an example execution for 300 users started within 50 s:

    ```bash
    mvn -Pscalability gatling:execute -Dgatling.sessionCount=300 -Dgatling.sessionStartInterval=50000
    ```

Licenses
==
The source code is released under Apache 2.0.

The application uses the [Vaadin Charts 2](https://vaadin.com/charts) add-on, which is released under the Commercial Vaadin Addon License: https://vaadin.com/license/cval-3
