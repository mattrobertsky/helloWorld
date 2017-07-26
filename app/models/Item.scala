package models

import play.api.data.Form
import play.api.data.Forms._

import scala.collection.mutable.ArrayBuffer
import scala.reflect.io.File

case class Item(name: String,
                description: String,
                manufacturer: String,
                warranty: Int,
                price: BigDecimal,
                discount: Discount,
                seller: String,
                var picture: Array[Byte]
               )

case class ItemData(name: String,
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

  val items: ArrayBuffer[Item] = ArrayBuffer[Item](
//    Item("foo1", "bar", "foobar", 10, 99.00, Discount(2, 50), "jo", "somefilepath"),
//    Item("foo2", "bar", "foobar", 10, 99.00, Discount(2, 50), "jo", "somefilepath"),
//    Item("foo3", "bar", "foobar", 10, 99.00, Discount(2, 50), "jo", "somefilepath"),
//    Item("foo4", "bar", "foobar", 10, 99.00, Discount(2, 50), "jo", "somefilepath")
//    Item("foo4", "bar", "foobar", 10, 99.00, Discount(2, 50), "jo")
  )

}
