package models

import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json._
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONObjectID}
import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection._

import scala.collection.mutable.ArrayBuffer

case class ItemData(id: BSONObjectID,
                    index: Option[Int],
                    name: String,
                    description: String,
                    manufacturer: String,
                    warranty: Int,
                    price: BigDecimal,
                    discount: Discount,
                    seller: String,
                    var picture: Option[String]
                   )

object ItemData {

//  implicit object ItemDataReader extends BSONDocumentReader[ItemData] {
//    override def read(doc: BSONDocument): ItemData = {
//      val id = doc.getAs[BSONObjectID]("_id").get
//      val index = doc.getAs[Int]("index").get
//      val name = doc.getAs[String]("name").get
//      val description = doc.getAs[String]("description").get
//      val manufacturer = doc.getAs[String]("manfacturer").get
//      val
//    }
//  }


  import play.api.libs.json.Json


  // Generates Writes and Reads for Feed and User thanks to Json
  //implicit val discountFormat = Json.format[Discount]
//  implicit val itemFormat = Json.format[ItemData]
  implicit val itemDataFormat: OFormat[ItemData] = Json.format[ItemData]

  val createItemForm: Form[ItemData] = Form(
    mapping(
      "id" -> number,
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
      "seller" -> nonEmptyText,
      "picture" -> optional(text)
    )(ItemData.apply)(ItemData.unapply)
  )

  val items: ArrayBuffer[ItemData] = ArrayBuffer[ItemData]()

}
