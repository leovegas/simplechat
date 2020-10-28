package models

import models.Tables._
import org.mindrot.jbcrypt.BCrypt
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}


class TaskListDatabaseModel(db: Database)(implicit ec: ExecutionContext) {

  def validateUser(username: String, password: String, status: Boolean): Future[Option[Int]] = {
    db.run(
      (for {
        user <- Users if user.username === username.toLowerCase
      } yield {
        user
      }).map(_.online).update(status))
    val matches = db.run(Users.filter(userRow => userRow.username === username.toLowerCase).result)
    matches.map(userRows => userRows.headOption.flatMap {
      userRow => if (BCrypt.checkpw(password, userRow.password)) Some(userRow.id) else None
    })
  }

  def updatePass(pass: String, code:String): Future[String] = {
        db.run(
          (for {
            user <- Users if user.password === code
          } yield {
            user
          }).map(_.password).update(BCrypt.hashpw(pass, BCrypt.gensalt()))
        ).flatMap { addCount =>
          if (addCount > 0)
            Future.successful("ok")
          else Future.successful("None")
        }
  }

  def isEmailNotExists(email: String):Boolean = {
    val matchByEmail = db.run(Users.filter(userRow => userRow.email === email.toLowerCase).result)
    val result = matchByEmail.map(i => i.isEmpty)
    Await.result(result, 5.seconds);
  }

  def createUser(username: String, password: String, status: Boolean, email: String): Future[Option[Int]] = {
    if (isEmailNotExists(email)) {
      val matches = db.run(Users.filter(userRow => userRow.username === username).result)
      matches.flatMap { userRows =>
        if (userRows.isEmpty) {
          db.run(Users += UsersRow(-1, username.toLowerCase, BCrypt.hashpw(password, BCrypt.gensalt()), status, email.toLowerCase))
            .flatMap { addCount =>
              if (addCount > 0) {
                db.run(Users.filter(userRow => userRow.username === username).result)
                  .map(_.headOption.map(_.id))
              }
              else Future.successful(None)
            }
        } else Future.successful(None)
      }
    } else Future.successful(None)
  }

  def restorePassword(email: String, code:Int): Future[String] = {
    if (isEmailNotExists(email)) {
      Future.successful("None")
    } else {
      db.run(
        (for {
          user <- Users if user.email === email
        } yield {
          user
        }).map(_.password).update(code.toString))

      val userRow = db.run(
        (for {
          user <- Users if user.email === email
        } yield {
          user
        }).result)

      userRow.map(_.map(itemRow => itemRow.username)).map({item => if(item.nonEmpty) item.head else "None" })
    }
  }

  def getTasks(username: String): Future[Seq[TaskItem]] = {
    db.run(
      (for {
        user <- Users if user.username === username
        item <- Items if item.userId === user.id
      } yield {
        // TaskItem(item.itemId, item.text)
        item
      }).result
    ).map(_.map(itemRow => TaskItem(itemRow.itemId, itemRow.text)))
  }

  def addTask(userid: Int, task: String): Future[Int] = {
    db.run(Items += ItemsRow(-1, userid, task))
  }

  def removeTask(itemId: Int): Future[Boolean] = {
    db.run(Items.filter(_.itemId === itemId).delete).map(count => count > 0)
  }

  def logoutUpdateStatus(username: String, status: Boolean): Future[Option[Int]] = {
    db.run(
      (for {
        user <- Users if user.username === username.toLowerCase
      } yield {
        user
      }).map(_.online).update(status)
    ).flatMap { addCount =>
      if (addCount > 0) db.run(Users.filter(userRow => userRow.username === username.toLowerCase).result)
        .map(_.headOption.map(_.id))
      else Future.successful(None)
    }
  }





}

