package controllers

import javax.inject.{Inject, Singleton}
import models.{TaskListInMemoryModel, UserData}
import play.api.libs.json.{JsError, JsSuccess, Json, Reads}
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request, Result}

@Singleton
class TaskList3 @Inject()(cc: ControllerComponents) extends AbstractController(cc) with play.api.i18n.I18nSupport {

  implicit val UserDataReads: Reads[UserData] = Json.reads[UserData]

  def load = Action { implicit request =>
    Ok(views.html.version4Main())
  }

  def withJsonBody[A](f: A => Result)(implicit request: Request[AnyContent], reads: Reads[A]) = {
    request.body.asJson.map { body =>
      Json.fromJson[A](body) match {
        case JsSuccess(a, path) => f(a)
        case e @ JsError(_) => Redirect(routes.TaskList3.load())
      }
    }.getOrElse(Redirect(routes.TaskList3.load()))
  }

  def withSessionUsername(f: String => Result)(implicit request: Request[AnyContent]) = {
    request.session.get("username").map(f).getOrElse(Ok(Json.toJson(Seq.empty[String])))
  }


  def validate = Action { implicit request =>
    request.body.asJson.map { body=>
      Json.fromJson[UserData](body) match {
        case JsSuccess(ud, path) =>
          val username = ud.username
          val password = ud.password
          if (TaskListInMemoryModel.validateUser(username, password)) {
            Ok(Json.toJson(true))
              .withSession("username" -> username, "csrfToken" -> play.filters.csrf.CSRF.getToken.get.value)
          }else {
            Ok(Json.toJson(false))
          }
        case e @ JsError(_) => Redirect(routes.TaskList3.load())
      }
    }.getOrElse(Redirect(routes.TaskList3.load()))
  }

  def createUser = Action { implicit request =>
    request.body.asJson.map { body=>
      Json.fromJson[UserData](body) match {
        case JsSuccess(ud, path) =>
          val username = ud.username
          val password = ud.password
          if (TaskListInMemoryModel.createUser(username, password)) {
            Ok(Json.toJson(true))
              .withSession("username" -> username, "csrfToken" -> play.filters.csrf.CSRF.getToken.get.value)
          }else {
            Ok(Json.toJson(false))
          }
        case e @ JsError(_) => Redirect(routes.TaskList3.load())
      }
    }.getOrElse(Redirect(routes.TaskList3.load()))
  }

  def taskList = Action {implicit request =>
    val usernameOption = request.session.get("username")
    usernameOption.map { username =>
      Ok(Json.toJson(TaskListInMemoryModel.getTasks(username)))
    }.getOrElse(Ok(Json.toJson(Seq.empty[String])))
  }

  def addTask = Action { implicit request =>
    val usernameOption = request.session.get("username")
    usernameOption.map { username =>

      request.body.asJson.map { body =>
        Json.fromJson[String](body) match {
          case JsSuccess(task, path) => {
            TaskListInMemoryModel.addTask(username, task);
            Ok(Json.toJson(true))
        }
          case e @ JsError(_) => Redirect(routes.TaskList3.load())
        }
      }.getOrElse(Ok(Json.toJson(false)))
    }.getOrElse(Ok(Json.toJson(false)))
  }


  def delete = Action { implicit request =>
    val usernameOption = request.session.get("username")
    usernameOption.map { username =>

      request.body.asJson.map { body =>
        Json.fromJson[Int](body) match {
          case JsSuccess(taskNumber, path) => {
            TaskListInMemoryModel.removeTask(username, taskNumber);
            Ok(Json.toJson(true))
          }
          case e @ JsError(_) => Redirect(routes.TaskList3.load())
        }
      }.getOrElse(Ok(Json.toJson(false)))
    }.getOrElse(Ok(Json.toJson(false)))
  }

  def logout = Action {
    Redirect(routes.TaskList3.load()).withNewSession
  }


}
