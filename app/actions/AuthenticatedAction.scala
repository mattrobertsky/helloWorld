package actions

import models.User
import models.User.findByName
import play.api.mvc._

import scala.concurrent.Future

class AuthenticatedRequest[A] (val user: User, val request: Request[A]) extends WrappedRequest[A] (request)

object AuthenticatedAction extends ActionBuilder[AuthenticatedRequest]{
  def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) =>
    Future[Result]): Future[Result] = {
      request.session.get("name")
        .flatMap(findByName(_))
        .map(user => block(new AuthenticatedRequest[A](user, request)))
        .getOrElse(Future.successful(Results.Forbidden))
  }
}
