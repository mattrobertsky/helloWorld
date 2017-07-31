package controllers

import javax.inject.Inject

import scala.concurrent.Future
import play.api.mvc.{Action, AnyContent, Controller, MultipartFormData}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import models._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.play.json._
import collection._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.Files
import play.api.libs.json.Json
import reactivemongo.api.Cursor
import reactivemongo.bson.BSONDocument

class ItemManagementController @Inject()(val messagesApi: MessagesApi, environment: play.api.Environment, val reactiveMongoApi: ReactiveMongoApi) extends Controller
  with I18nSupport with MongoController with ReactiveMongoComponents {

  def collection: Future[JSONCollection] = database.map(_.collection[JSONCollection]("items"))

  def findByIndex(index: Int) = Action.async { implicit request =>
    val cursor: Future[Cursor[ItemData]] = collection.map {
      _.find(Json.obj("index" -> index)).
        cursor[ItemData]
    }

    // gather all the JsObjects in a list
    val futureUsersList: Future[List[ItemData]] = cursor.flatMap(_.collect[List]())

    futureUsersList.map { items =>
      Ok(views.html.items(ItemData.items, ItemData.createItemForm.fill(items.head)))
    }
  }

  def listItems: Action[AnyContent] = Action.async { implicit request =>

    val cursor: Future[Cursor[ItemData]] = collection.map {
      _.find(Json.obj()).
        cursor[ItemData]
    }
    val futureUsersList: Future[List[ItemData]] = cursor.flatMap(_.collect[List]())

    futureUsersList.map { items =>
      Ok(views.html.items(items, ItemData.createItemForm))
    }
  }

  def createItem: Action[MultipartFormData[Files.TemporaryFile]] = Action(parse.multipartFormData) { implicit request =>
    val postAction = request.body.asFormUrlEncoded.get("action").get(0)
    val boundForm = ItemData.createItemForm.bindFromRequest
    boundForm.fold({ formWithErrors =>
      BadRequest(views.html.items(ItemData.items, formWithErrors))
    }, { itemData =>
      request.body.file("picture").map { picture =>
        import java.io.File
        val filename = picture.filename.replaceAll(" ", "_")
        val p = environment.rootPath.getAbsolutePath
        val path = s"$p/public/images/$filename"
        picture.ref.moveTo(new File(path))
        itemData.picture = Some(routes.Assets.at(s"images/$filename").url)

        println("index is " + itemData.index)

        val selector = BSONDocument("index" -> itemData.index.getOrElse(0))
        val futureResult = postAction match {
          case "save" if itemData.index.get != -1 => collection.map(_.update(selector ,itemData, upsert = true))
          case "delete" if itemData.index.get != -1 => collection.flatMap(_.remove(selector))
          case _ => collection.flatMap(_.insert(itemData))
        }
        futureResult.map(_ => Redirect(routes.ItemManagementController.listItems))

      }.getOrElse {
        Redirect(routes.Application.index).flashing(
          "error" -> "Missing file")
      }
      Redirect(routes.ItemManagementController.listItems)
    })
  }


}
