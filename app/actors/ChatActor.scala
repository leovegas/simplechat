package actors

import actors.ChatActor.{LogoutMessageForActor, SendMessage}
import actors.ChatManager.{Chatter}
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import tasks.KeepAliveTask

class ChatActor(out: ActorRef, manager: ActorRef, username: String) extends Actor{

  manager ! ChatManager.NewChatter(Chatter(self, username))

  override def receive: Receive = {
    case s: String => {
      manager ! ChatManager.Message(s, username)
      println(s"sending message to manager with username $username")
    }
    case SendMessage(msg) => {
      out ! msg
      println(s"sending message to socket...")
    }
    case LogoutMessageForActor => {
        println(s"Actor $self got message and kill itself")
        context.stop(self)
    }
  }

  override def postStop(): Unit = {
    println(s"actor $self stopping")
    manager ! ChatManager.ChatterGone(Chatter(self, username))
    super.postStop()
  }
}

object ChatActor {
  def props(out: ActorRef, manager: ActorRef, username: String) = Props(new ChatActor(out, manager, username))
  case class SendMessage(msg: String)
  case object LogoutMessageForActor

}