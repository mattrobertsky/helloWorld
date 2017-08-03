package actions

import play.api.mvc.ActionBuilder

object LoggingAuthenticatedAction {

  val instance: ActionBuilder[AuthenticatedRequest] = LoggingAction andThen AuthenticatedAction

}
