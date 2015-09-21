Vaadin QuickTickets Dashboard Demo
==================================

Sources for the official [Vaadin](https://vaadin.com) demo application: http://demo.vaadin.com/dashboard

![QuickTickets Dashboard](https://vaadin.com/documents/10187/2487938/Dashboard+Demo+2014/a37b2c4d-c941-48fe-97c3-ad5a60586882?t=1412769929183)

Running the App
==
Run 'mvn -Pproduction-mode jetty:run' to run in a local jettty. Open in localhost:8080

Run the Maven 'install' target and deploy the resulting WAR file to your Java application server.

You need a license for Vaadin Charts to compile the widgetset. You can get a free 30 day trial license by going to https://vaadin.com/directory#addon/vaadin-charts and clicking the orange "Free trial key" button. It gives you a trial key. [See the help section](https://vaadin.com/directory/help/installing-cval-license) which shows you how to install the key.

Basically you need to create a file name ".vaadin.charts.developer.license" in your HOME directory, and place the key there.

Run 'mvn -Pproduction-mode verify' to run the Vaadin TestBench tests. 

*Note*: You need a valid [Vaadin TestBench license](https://vaadin.com/add-ons/testbench) and [Firefox browser](https://www.mozilla.org/firefox/) installed to run the tests.

Licenses
==
The source code is released under Apache 2.0.

The application uses the [Vaadin Charts 2](https://vaadin.com/charts) add-on, which is released under the Commercial Vaadin Addon License: https://vaadin.com/license/cval-3
