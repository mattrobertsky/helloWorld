package controllers

import actions.{LoggingAction, LoggingAuthenticatedAction}
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

class CompositionController extends Controller {

  // using a custom action
  def something = LoggingAction.async {

    Future(Ok("foo with action logging thru action composition"))
  }

  def authenticated: Action[AnyContent] = LoggingAuthenticatedAction.instance { request =>
    // if the name isn't on the session you get an unauth header but just in case this
    // changes, and to make codacy happy we use a getOr..
    Ok("hello " + request.session.get("name").getOrElse("Anon") + " you are authentic")
  }


}
