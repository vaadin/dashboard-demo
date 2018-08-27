import java.util.concurrent.atomic.AtomicInteger

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._

class V7Memory extends Simulation {
  val scenarioName = "V7Memory"

  def toInt(s: String): Option[Int] = {
    try {
      Some(s.toInt)
    } catch {
      case _: NumberFormatException => None
    }
  }

  // The URL of the system under test
  val baseUrl: String = System.getProperty("gatling.baseUrl", "http://localhost:8080")

  // The total number of simulated user sessions
  // NOTE: the number of concurrent sessions is lower because sessions start one by one
  // with a given interval and some may finish before the last session starts.
  val sessionCount: Int = toInt(System.getProperty("gatling.sessionCount", "")) match {
    case Some(n) => n
    case None => 2
  }

  // The interval (in milliseconds) between starting new user sessions
  val sessionStartInterval: Int = toInt(System.getProperty("gatling.sessionStartInterval", "")) match {
    case Some(n) => n
    case None => 2000
  }

  // The repeat count of the scenario, by default executed only once
  val sessionRepeats: Int = toInt(System.getProperty("gatling.sessionRepeats", "")) match {
    case Some(n) => n
    case None => 1
  }

  // The timeout (in milliseconds) between requests done. Is not applicable to GC stats colleciton requests.
  val requestTimeout: Int = toInt(System.getProperty("gatling.requestTimeout", "")) match {
    case Some(n) => n
    case None => 100
  }

  val httpProtocol = http
    .baseURL(baseUrl)
    .acceptHeader("*/*")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .doNotTrackHeader("1")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.13; rv:61.0) Gecko/20100101 Firefox/61.0")

  val headers_initial_request = Map(
    "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
    "Upgrade-Insecure-Requests" -> "1")

  val headers_security_data_request = Map(
    "Cache-Control" -> "max-age=0",
    "Content-type" -> "application/x-www-form-urlencoded",
    "Origin" -> "http://localhost:8080")

  val uidlRequestHeaders = Map("Content-Type" -> "application/json; charset=UTF-8")

  val numberOfConcurrentUsers = new AtomicInteger()
  val memoryMeasurements = mutable.HashMap[String, mutable.ListBuffer[(Int, Long)]]()

  val url = "/"
  val uidlUrl = url + "UIDL/?v-uiId=${uiId}"

  val uidlExtract = regex(""""v-uiId":\s?(\d+)""").saveAs("uiId")
  val securityKeyExtract = regex("""Vaadin-Security-Key\\?":\s?\\?"([^"\\]*)""").saveAs("seckey")
  val syncIdExtract = regex("""syncId\\?":\s?([0-9]*)""").saveAs("syncId")
  val clientIdExtract = regex("""clientId\\?":\s?([0-9]*)""").saveAs("clientId")

  val initSyncAndClientIds = exec(session => {
    numberOfConcurrentUsers.incrementAndGet()
    session.setAll(
      "syncId" -> 0,
      "clientId" -> 0
    )
  })

  def gatherMeasurements(requestSetNumber: Int, description: String) =
    exec(http("gc_button_click")
      .post(uidlUrl)
      .headers(uidlRequestHeaders)
      .body(ElFileBody(s"${scenarioName}_gc_click_request.txt")))
      .pause(3)
      .exec(http("measure_memory_button_click")
        .post(uidlUrl)
        .headers(uidlRequestHeaders)
        .body(ElFileBody(s"${scenarioName}_memory_click_request.txt"))
        .check(
          regex("""Total memory \(B\): ([0-9]+)""")
            .transform((size: String) => memoryMeasurements.getOrElseUpdate(s"${requestSetNumber}_$description", ListBuffer()).append((numberOfConcurrentUsers.get(), size.toLong)))
        )
      )

  def measureAndExec(lastRequestNumber: Int, requestSetNumber: Int) = exec(gatherMeasurements(requestSetNumber, "before"))
    .foreach(0 to lastRequestNumber, "requestNumber") {
      exec(http(s"${scenarioName}_${requestSetNumber}_$${requestNumber}_request")
        .post(uidlUrl)
        .headers(uidlRequestHeaders)
        .body(ElFileBody(s"${scenarioName}_${requestSetNumber}_$${requestNumber}_request.txt"))
        .check(syncIdExtract).check(clientIdExtract)
      ).pause(requestTimeout millis)
    }
    .pause(1.5 seconds)
    .exec(gatherMeasurements(requestSetNumber, "after"))

  val scn = scenario(scenarioName)
    .repeat(sessionRepeats) {
      exec(http("initial_request")
        .get(url)
        .headers(headers_initial_request)
        .resources(
          http("styles_request")
            .get("/VAADIN/themes/dashboard/styles.css?v=7.6.5"),
          http("widgetset_request")
            .get("/VAADIN/widgetsets/com.vaadin.demo.dashboard.DashboardWidgetSet/com.vaadin.demo.dashboard.DashboardWidgetSet.nocache.js?1535092631102")
        ))
        .exec(initSyncAndClientIds)
        .pause(requestTimeout millis)
        .exec(http("security_data_request")
          .post("/?v-1535092631104")
          .headers(headers_security_data_request)
          .body(ElFileBody(s"${scenarioName}_0_0_security_data_request.txt"))
          .check(uidlExtract)
          .check(securityKeyExtract)
          .check(regex("""\\"hierarchy\\":\{\\"([0-9]+)\\"""").saveAs("topLevelElementId"))
          .check(syncIdExtract).check(clientIdExtract)
        )
        .pause(requestTimeout millis)
        .exec(http("ui_resize")
          .post(uidlUrl)
          .headers(uidlRequestHeaders)
          .body(ElFileBody(s"${scenarioName}_0_1_ui_resize_request.txt"))
          .check(syncIdExtract).check(clientIdExtract)
        )
        .pause(requestTimeout millis)
        .exec(http("login_click_request")
          .post(uidlUrl)
          .headers(uidlRequestHeaders)
          .body(ElFileBody(s"${scenarioName}_0_2_login_click_request.txt"))
          .check(regex("""\{"id":"([0-9]+)","selectmode":""").saveAs("gridId"))
          .check(syncIdExtract).check(clientIdExtract)
        )
        .pause(1.5 seconds)
        .exec(measureAndExec(10, 1))
        // TODO kb re-enable (save responses, determine node ids and use them in request)
        //        .exec(measureAndExec(108, 2))
        .exec(measureAndExec(3, 3))
        //        .exec(measureAndExec(5, 4))
        //        .exec(measureAndExec(7, 5))
        .exec { session =>
        numberOfConcurrentUsers.decrementAndGet()
        session
      }
    }

  setUp(scn.inject(rampUsers(sessionCount) over (sessionStartInterval millis)))
    .protocols(httpProtocol)
    .assertions(forAll.failedRequests.percent.is(0))

  after {
    println("=======================")
    println(memoryMeasurements)
    println("=======================")
  }
}
