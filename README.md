Vaadin QuickTickets Dashboard Demo
==================================

Sources for the official Vaadin desktop browser demo application: http://demo.vaadin.com/dashboard

![QuickTickets Dashboard](https://vaadin.com/image/image_gallery?uuid=0333a002-1e66-43f4-b127-b7da911a3cb3&groupId=10187&t=1359053559577)

To run
==
Run the Maven install target and deploy the resulting WAR-file to your server.

*Note*: You need a [Rotten Tomatoes API key](http://developer.rottentomatoes.com) and a connection to the internet to run the app. Insert your own API key here: [DataProvider.java#L156](https://github.com/vaadin/dashboard-demo/blob/master/src/main/java/com/vaadin/demo/dashboard/data/DataProvider.java#L156)


Licenses
==
The source code is released under Apache 2.0.

The application uses the Vaadin Charts add-on, which is released under the Commercial Vaadin Addon License: https://vaadin.com/license/cval-2.0
