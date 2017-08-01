package controllers

import javax.inject.Inject

import scala.concurrent.Future
import play.api.mvc.{Action, AnyContent, Controller, MultipartFormData}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import models._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.play.json._
import collection._
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.Files
import play.api.libs.json.{JsObject, Json}
import reactivemongo.api.Cursor
import reactivemongo.bson.BSONDocument

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.{Failure, Success}

class ItemManagementController @Inject()(val messagesApi: MessagesApi, environment: play.api.Environment, val reactiveMongoApi: ReactiveMongoApi) extends Controller
  with I18nSupport with MongoController with ReactiveMongoComponents {

  def collection: Future[JSONCollection] = database.map(_.collection[JSONCollection]("items"))

  def findByIndex(index: Int) = Action.async { implicit request =>

    val futures = for {
      all <- getResults(Json.obj())
      one <- getResults(Json.obj("index" -> index))
    } yield (all, one)

    futures.map {
      case (all, one) => Ok(views.html.items(all, ItemData.createItemForm.fill(one.head)))
    }
  }

  private def getResults(selector: JsObject): Future[List[ItemData]] = {
    val cursor: Future[Cursor[ItemData]] = collection.map {
      _.find(selector).
        cursor[ItemData]
    }
    cursor.flatMap(_.collect[List]())
  }

  def listItems: Action[AnyContent] = Action.async { implicit request =>

    getResults(Json.obj()).map { items =>
      Ok(views.html.items(items, ItemData.createItemForm))
    }
  }

  def createItem: Action[MultipartFormData[Files.TemporaryFile]] = Action.async(parse.multipartFormData) { implicit request =>
    val postAction = request.body.asFormUrlEncoded.get("action").get(0)
    val boundForm = ItemData.createItemForm.bindFromRequest
    boundForm.fold({ formWithErrors =>
      getResults(Json.obj()).map { items =>
        BadRequest(views.html.items(items, formWithErrors))
      }
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
      Future(Redirect(routes.ItemManagementController.listItems))
    })
  }


}
