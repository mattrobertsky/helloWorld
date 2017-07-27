package controllers

import javax.inject.Inject

import models.{Item, ItemData}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.Files
import play.api.mvc._

class ItemManagementController @Inject()(val messagesApi: MessagesApi, environment: play.api.Environment) extends Controller with I18nSupport {

  def listItems: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.items(ItemData.items, ItemData.createItemForm))
  }

  def editItem(index: Int):  Action[AnyContent] = Action { implicit request =>
    val item = ItemData.items(index)
    val itemData = ItemData(Some(index), item.name, item.description, item.manufacturer, item.warranty, item.price, item.discount, item.seller)
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
        val item = Item(itemData.name, itemData.description, itemData.manufacturer,
          itemData.warranty, itemData.price, itemData.discount, itemData.seller,
          routes.Assets.at(s"images/$filename").url)


        if(itemData.index.get != -1 && postAction == "save") {
          ItemData.items.update(itemData.index.get, item)
        } else if (itemData.index.get != -1 && postAction == "delete") {
          ItemData.items.remove(itemData.index.get)
        } else {
          ItemData.items.append(item)
        }
      }.getOrElse {
        Redirect(routes.Application.index).flashing(
          "error" -> "Missing file")
      }
      Redirect(routes.ItemManagementController.listItems)
    })
  }


}
