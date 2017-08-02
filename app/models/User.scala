package models

case class User(name: String)

object User {

  def findByName(name: String): Option[User] = {
    users.find(u => u.name == name)
  }

  val users = List(User("matt"), User("tadas"))
}

