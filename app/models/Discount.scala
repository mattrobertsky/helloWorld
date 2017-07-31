package models


case class Discount(howManyItems: Int, percentageOff: Int)

object Discount {

  import play.api.libs.json.OFormat
  import play.api.libs.json.Json


  // Generates Writes and Reads for Feed and User thanks to Json
  implicit val discountFormat: OFormat[Discount] = Json.format[Discount]
}