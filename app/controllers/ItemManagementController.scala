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
import reactivemongo.bson.BSONDocument

class ItemManagementController @Inject()(val messagesApi: MessagesApi, environment: play.api.Environment, val reactiveMongoApi: ReactiveMongoApi) extends Controller
  with I18nSupport with MongoController with ReactiveMongoComponents {

  def collection: Future[JSONCollection] = database.map(_.collection[JSONCollection]("items"))
//  def collectionB: JSONCollection = db.collection[JSONCollection]("items")

//  def findByName(name: String) = Action.async {
//    // let's do our query
//    val cursor: Future[Cursor[ItemData]] = collection.map {
//      // find all people with name `name`
//      _.find(Json.obj("name" -> name)).
//        // sort them by creation date
////        sort(Json.obj("created" -> -1)).
//        // perform the query and get a cursor of JsObject
//        cursor[ItemData]
//    }
//
//    // gather all the JsObjects in a list
//    val futureUsersList: Future[List[ItemData]] = cursor.flatMap(_.collect[List]())
//
//    // everything's ok! Let's reply with the array
//    futureUsersList.map { items =>
//      Ok(items.toString)
//    }
//  }

  def listItems: Action[AnyContent] = Action { implicit request =>
//  def listItems: Action[AnyContent] = Action.async { implicit request =>

//    println("collection " + collection.collect()

//    def collection: JSONCollection = db.collection[JSONCollection]("items")

//    val cursor: Cursor[ItemData] = collectionB.find("name" -> "asd").cursor[ItemData]
//
//
//    val futureItemsList: Future[List[ItemData]] = cursor.collect[List]()
//    futureItemsList.map(items => Ok(views.html.items(items, ItemData.createItemForm)))


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
//        val selector = BSONDocument("index" -> 0)
        val futureResult = postAction match {
          case "save" if itemData.index.get != -1 => collection.map(_.findAndUpdate(selector ,itemData))
          case "delete" if itemData.index.get != -1 => collection.flatMap(_.remove(itemData))
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
