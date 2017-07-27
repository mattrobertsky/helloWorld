package models

import play.api.data.Form
import play.api.data.Forms._

import scala.collection.mutable.ArrayBuffer

case class Item(name: String,
                description: String,
                manufacturer: String,
                warranty: Int,
                price: BigDecimal,
                discount: Discount,
                seller: String,
                picture: String
               )

case class ItemData(index: Option[Int],
                    name: String,
                    description: String,
                    manufacturer: String,
                    warranty: Int,
                    price: BigDecimal,
                    discount: Discount,
                    seller: String
                   )


object ItemData {

  val createItemForm: Form[ItemData] = Form(
    mapping(
      "index" -> optional(number),
      "name" -> nonEmptyText,
      "description" -> nonEmptyText,
      "manufacturer" -> nonEmptyText,
      "warranty" -> number,
      "price" -> bigDecimal,
      "discount" -> mapping(
        "howManyItems" -> number,
        "percentageOff" -> number
      )(Discount.apply)(Discount.unapply),
      "seller" -> nonEmptyText
    )(ItemData.apply)(ItemData.unapply)
  )

  val items: ArrayBuffer[Item] = ArrayBuffer[Item]()

}
