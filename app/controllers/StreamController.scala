package controllers

import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString
import play.api.http.HttpEntity
import play.api.mvc._



class StreamController extends Controller {

  def servePdf = Action {
    val file = new java.io.File("/home/matt/Downloads/Crime_and_Punishment_T.pdf")
//    val path: java.nio.file.Path = file.toPath
//    val source: Source[ByteString, _] = FileIO.fromPath(path)
//
//    val contentLength = Some(file.length())

//    Result(
//      header = ResponseHeader(200, Map.empty),
//      body = HttpEntity.Streamed(source, contentLength, Some("application/pdf"))
//    )
    Ok.sendFile(content = file, fileName = _ => "CrimeAndPunishment.pdf")
  }

//  def chunks

}
