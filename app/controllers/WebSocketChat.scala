package controllers

import actors.ChatManager.{Chatter, GiveMeList, LogoutMessage}
import actors.{ChatActor, ChatManager, ServiceActor}
import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.Timeout
import javax.inject.{Inject, Singleton}
import models.UserListItem
import play.api.libs.json.{Json, Writes}
import play.api.libs.mailer.{Email, MailerClient}
import play.api.libs.streams.ActorFlow
import play.api.mvc.{AbstractController, ControllerComponents, WebSocket}
import tasks.KeepAliveTask

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

@Singleton
class WebSocketChat @Inject()(cc: ControllerComponents)(implicit system:ActorSystem, mat: Materializer) extends AbstractController(cc) with play.api.i18n.I18nSupport {

  import system.dispatcher
  implicit val itemDataWrites: Writes[UserListItem] = Json.writes[UserListItem]

  val manager = system.actorOf(Props[ChatManager], "Manager2")
  val serviceActor = system.actorOf(ServiceActor.props(manager), "ServiceActor")
  val keepAlive = new KeepAliveTask(system, manager)




  def index() = Action { implicit request =>
      Ok(views.html.chatPage()).withSession("username" -> request.session.data.head._2)
  }

  def logoutMessage() = Action { implicit request =>
    manager ! LogoutMessage(request.session.data.head._2)
    Redirect(routes.TaskList5.logout())
  }

  def socket() = WebSocket.accept[String,String] { implicit request =>
      ActorFlow.actorRef { out =>
        ChatActor.props(out, manager, request.session.data.head._2)
      }
    }

  def getListValue():Future[Seq[String]] = {
    var list = List.empty[Chatter]
    var uI = Seq.empty[UserListItem]
    var listString = Seq.empty[String]
    implicit val timeout = Timeout(5.seconds)
    val f = Source.single(GiveMeList)
      .runWith(Sink.foreachAsync(1) { msg =>
        (manager ask msg).map(ch => {
            list = ch.asInstanceOf[List[Chatter]]
            listString = list.map(c => c.username)
            uI = listString.map(item => UserListItem(item))
          })
        })
    Await.result(f, 2.seconds)
    Future.successful(listString)
    }

  def activeUsersList = Action.async { implicit request =>
    getListValue().map(uI =>
      Ok(Json.toJson(uI))
    )
     }


}




