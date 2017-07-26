package controllers

import javax.inject.Inject

import models.{Discount, Item, ItemData}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.Files
import com.google.common.io.Files._
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc._

class ItemManagementController @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport{

  def listItems: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.items(ItemData.items, ItemData.createItemForm))
  }



//  def createItem: Action[AnyContent] = Action { implicit request =>
  def createItem: Action[MultipartFormData[Files.TemporaryFile]] = Action(parse.multipartFormData) { implicit request =>
    val boundForm = ItemData.createItemForm.bindFromRequest
    boundForm.fold({ formWithErrors =>
      formWithErrors.errors.seq.foreach(x => println(x.message))
      BadRequest(views.html.items(ItemData.items, formWithErrors))
    }, { itemData =>


      request.body.file("picture").map { picture =>
        val item = Item(itemData.name, itemData.description, itemData.manufacturer,
          itemData.warranty, itemData.price, itemData.discount, itemData.seller,
          toByteArray(picture.ref.file))
        ItemData.items.append(item)


      }.getOrElse {
        Redirect(routes.Application.index).flashing(
          "error" -> "Missing file")
      }





      Redirect(routes.ItemManagementController.listItems)
    })
  }



}
