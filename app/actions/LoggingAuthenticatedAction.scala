package actions

import play.api.mvc.ActionBuilder

class LoggingAuthenticatedAction {

  val instance: ActionBuilder[AuthenticatedRequest] = LoggingAction andThen AuthenticatedAction

}
