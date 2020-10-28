package controllers

import javax.inject.{Inject, Singleton}
import models.{TaskItem, TaskListDatabaseModel, UserData}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.json._
import play.api.libs.mailer.{Email, MailerClient}
import play.api.mvc._
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TaskList5 @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, cc: ControllerComponents, mailerClient: MailerClient)(implicit ec: ExecutionContext)
  extends AbstractController(cc) with play.api.i18n.I18nSupport with HasDatabaseConfigProvider[JdbcProfile] {

  private val model = new TaskListDatabaseModel(db)

  def load = Action { implicit request =>
    if (request.session.get("username").isEmpty) {
      Ok(views.html.version4Main())
    }else {
      Redirect(routes.WebSocketChat.index())
    }
  }

  implicit val UserDataReads: Reads[UserData] = Json.reads[UserData]
  implicit val itemDataWrites: Writes[TaskItem] = Json.writes[TaskItem]

  def withJsonBodyString[String](f: String => Future[Result])(implicit request: Request[AnyContent], reads: Reads[String]): Future[Result] = {
    request.body.asJson.map { body =>
      Json.fromJson[String](body) match {
        case JsSuccess(a, path) => {
          f(a)
        }
        case e@JsError(_) => Future.successful(Redirect(routes.TaskList5.load()))
      }
    }.getOrElse(Future.successful(Ok(Json.toJson(false))))
  }

  def withJsonBody[A](f: A => Future[Result])(implicit request: Request[AnyContent], reads: Reads[A]): Future[Result] = {
    request.body.asJson.map { body =>
      Json.fromJson[A](body) match {
        case JsSuccess(a, path) => f(a)
        case e@JsError(_) => Future.successful(Redirect(routes.TaskList5.load()))
      }
    }.getOrElse(Future.successful(Redirect(routes.TaskList5.load())))
  }

  def withSessionUsername(f: String => Future[Result])(implicit request: Request[AnyContent]): Future[Result] = {
    request.session.get("username").map(f).getOrElse(Future.successful(Ok(Json.toJson(Seq.empty[String]))))
  }

  def withSessionUserid(f: Int => Future[Result])(implicit request: Request[AnyContent]): Future[Result] = {
    request.session.get("userid").map(userid => f(userid.toInt)).getOrElse(Future.successful(Ok(Json.toJson(Seq.empty[String]))))
  }

  def validate = Action.async { implicit request =>
    withJsonBody[UserData] { ud =>
      model.validateUser(ud.username, ud.password, true).map {
        case Some(userid) =>
          Ok(Json.toJson(true))
            .withSession("username" -> ud.username, "userid" -> userid.toString, "csrfToken" -> play.filters.csrf.CSRF.getToken.map(_.value).getOrElse(""))
        case None =>
          Ok(Json.toJson(false))
      }
    }
  }

  def newPass = Action.async { implicit request =>
    val code:Int = 1234
    withJsonBodyString[String] { pass =>
      model.updatePass(pass, code.toString).map { s =>
        if (s.eq("None")) {
          Ok(Json.toJson(false))
        } else {
          Ok(Json.toJson(true))
            .withNewSession
        }
      }
    }
  }

  def createUser = Action.async { implicit request =>
    withJsonBody[UserData] { ud =>
      model.createUser(ud.username, ud.password, ud.status, ud.email).map {
        case Some(userid) => {
        //  sendEmail(ud.email, "Thank you for using our Chat", 1)
          Ok(Json.toJson(true))
            .withSession("username" -> ud.username, "userid" -> userid.toString, "csrfToken" -> play.filters.csrf.CSRF.getToken.map(_.value).getOrElse(""))
        }
        case None =>
          Ok(Json.toJson(false))
      }
    }
  }

  def restorePassword = Action.async { implicit request =>
    val code:Int = 1234
    withJsonBodyString[String] { email =>
      println(email)
      model.restorePassword(email, code).map { s =>
        if (s.eq("None")) {
          Ok(Json.toJson(false))
        } else {
          //sendEmail(email, s, code)
          println("email sended")
          Ok(Json.toJson(true))
            .withNewSession
        }
      }
    }
  }

  def taskList = Action.async { implicit request =>
    withSessionUsername { username =>
      model.getTasks(username).map(tasks => Ok(Json.toJson(tasks)))
    }
  }

  def addTask = Action.async { implicit request =>
    withSessionUserid { userid =>
      withJsonBody[String] { task =>
        model.addTask(userid, task).map(count => Ok(Json.toJson(count > 0)))
      }
    }
  }

  def delete = Action.async { implicit request =>
    withSessionUsername { username =>
      withJsonBody[Int] { itemId =>
        model.removeTask(itemId).map(removed => Ok(Json.toJson(removed)))
      }
    }
  }
//
//  def logout = Action { implicit request =>
//    Ok(Json.toJson(true)).withSession(request.session - "username")
//  }

  def logout = Action.async { implicit request =>
    withSessionUsername {username =>
      model.logoutUpdateStatus(username, false).map {
        case Some(_) =>
          Redirect(routes.TaskList5.load()).withNewSession
        case None =>
          Redirect(routes.TaskList5.load()).withNewSession
      }
    }
  }

  def sendEmail(emailadress:String, username: String, code:Int): String = {
    val email = Email(
      "Simple email",
      "Simple chat FROM <timofeevld@mail.com>",
      Seq(s"Registration confirmation <$emailadress>"),
      // adds attachment
      attachments = Seq.empty,
      // sends text, HTML or both...
      bodyText = Some("Thank you for using our service"),
      bodyHtml = if(code==1)  Some("Your username: "+ username)
      else Some("Your username: "+ username+ " "+code)

    )
    println(s"sendinf email to $emailadress")
    mailerClient.send(email)
  }


}
