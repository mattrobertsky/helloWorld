package controllers

import actions.AuthenticatedAction
import play.api.mvc._

class Application extends Controller {

  def index: Action[AnyContent] = Action {
    Ok(views.html.index("Hello World."))
  }

  def hello(name: String): Action[AnyContent] = Action { request =>
    Ok("Hello " + name).withSession("name" -> name)
//    Redirect(routes.Application.authenticate(name)).withSession("name" -> name)
  }

  def authenticated: Action[AnyContent] = AuthenticatedAction { request =>
    // if the name isn't on the session you get an unauth header but just in case this
    // changes, and to make codacy happy we use a getOr..
    Ok("hello " + request.session.get("name").getOrElse("Anon") + " you are authentic")
  }

  def somethingStatic(): Action[AnyContent] = Action {
    Ok(views.html.static("something static"))
  }

  def images(query: String): Action[AnyContent] = Action {
    Redirect("http://images.google.com/search?tbm=isch&q=" + java.net.URLEncoder.encode(query, "utf-8"), MOVED_PERMANENTLY)
  }

  def somethingSet: Action[AnyContent] = TODO

  def loseHouse(): Action[AnyContent] = Action {
    Redirect(routes.Application.somethingStatic())
  }

  def optional(opt: Option[String]): Action[AnyContent] = Action {
    val name = opt.getOrElse("Tadas")
    Redirect(routes.Application.hello(name))
  }

  def sessCookie(cooked: Option[String]): Action[AnyContent] = Action {
    cooked match {
      case Some(c) =>
        val cookie = Some(Cookie("sessCookie", c))
        Ok(views.html.cooked("cookin")(cookie)).withCookies(cookie.get)

      case None =>
        Ok(views.html.cooked("uncookin")(None)).discardingCookies(DiscardingCookie("sessCookie"))
    }
  }

  def flashed: Action[AnyContent] = Action { implicit request =>
    Ok {
      request.flash.get("success").getOrElse("Welcome!")
    }
  }

  def flashy: Action[AnyContent] = Action {
    Redirect("/flashed").flashing(
      "success" -> "this is from flash")
  }

  def sessionName: Action[AnyContent] = Action { implicit request =>
    request.session.get("name").map { name =>
      Ok(views.html.session(name))
    }.getOrElse(
      Unauthorized("you are not on the session, please say hello!")
    )
  }

  def contactForm: Action[AnyContent] = Action {
    Ok(views.html.bscontactform())
  }

}


