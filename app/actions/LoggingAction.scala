package actions

import play.Logger
import play.api.mvc._

import scala.concurrent.Future

object LoggingAction extends ActionBuilder[Request] {
  def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {
    Logger.info("Calling action " + request.path)
    block(request)
  }
}