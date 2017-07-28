package controllers

import javax.inject.Inject

import models.ItemData
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.Files
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.play.json.collection.JSONCollection
import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection._
import reactivemongo.bson.BSONDocument

import scala.concurrent.Future

class ItemManagementController @Inject()(val messagesApi: MessagesApi, environment: play.api.Environment, val reactiveMongoApi: ReactiveMongoApi) extends Controller
  with I18nSupport with MongoController with ReactiveMongoComponents {

  def collection: Future[JSONCollection] = database.map(_.collection[JSONCollection]("items"))
  var i = 0;
  collection.foreach(_ => i = i +1)

  def listItems: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.items(ItemData.items, ItemData.createItemForm))
  }

  def editItem(index: Int):  Action[AnyContent] = Action { implicit request =>
    val item = ItemData.items(index)
    val itemData = ItemData(Some(index),
                            item.name,
                            item.description,
                            item.manufacturer,
                            item.warranty,
                            item.price,
                            item.discount,
                            item.seller,
                            item.picture)
    Ok(views.html.items(ItemData.items, ItemData.createItemForm.fill(itemData)))
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

        val selector = BSONDocument("index" -> itemData.index.getOrElse(0))
        val futureResult = postAction match {
          case "save" if itemData.index.get != -1 => collection.flatMap(_.update(selector ,itemData))
          case "delete" if itemData.index.get != -1 => collection.flatMap(_.remove(itemData))
          case _ => collection.flatMap(_.insert(itemData))
        }
        futureResult.map(_ => Redirect(routes.ItemManagementController.listItems))

//        if(itemData.index.get != -1 && postAction == "save") {
//          ItemData.items.update(itemData.index.get, itemData)
//          val futureResult = collection.flatMap(_.update(itemData.index, itemData))
//          futureResult.map(_ => Redirect(routes.ItemManagementController.listItems))
//        } else if (itemData.index.get != -1 && postAction == "delete") {
//          val futureResult = collection.flatMap(_.remove(itemData.index))
//          futureResult.map(_ => Redirect(routes.ItemManagementController.listItems))
//          ItemData.items.remove(itemData.index.get)
//        } else {
//          val futureResult = collection.flatMap(_.insert(itemData))
//          futureResult.map(_ => Redirect(routes.ItemManagementController.listItems))
//          ItemData.items.append(itemData)
//        }
      }.getOrElse {
        Redirect(routes.Application.index).flashing(
          "error" -> "Missing file")
      }
      Redirect(routes.ItemManagementController.listItems)
    })
  }


}
