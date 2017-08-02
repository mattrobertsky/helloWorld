package controllers

import actions.LoggingAction
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

class CompositionController extends Controller {

  // using a custom action
  def something = LoggingAction.async {

    Future(Ok("foo with action logging thru action composition"))
  }

}
