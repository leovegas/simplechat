package actors

import actors.ChatActor.LogoutMessageForActor
import actors.ChatManager.{Chatter, ChatterGone, GiveMeList, LogoutMessage, Message, NewChatter}
import actors.ServiceActor.UserList
import akka.actor.{Actor, ActorRef, PoisonPill}

class ChatManager extends Actor {
  private var chatters = List.empty[Chatter]

  def getUsersnamesList = {
    chatters.map(_.username).toList
  }

  override def receive: Receive = {
    case "tick009" => for (c <- chatters) c.actor ! s"$getActiveUsers tick009"
    case NewChatter(chatter) => {
      chatters ::= chatter
      println(s"Added new chatter. Active users now $getActiveUsers")
    }
    case ChatterGone(chatter) => {
      println(s" Manager got ChatterGone with username ${chatter.username}")
      chatters = chatters.filter(ch => ch.username != chatter.username)
      println(s" Removed chatter. Active users now $getActiveUsers")
    }
    case Message(msg, username) => {
      println(s"Sending message for all actors")
      for (c <- chatters) c.actor ! ChatActor.SendMessage(username + ": " + msg)
    }
    case LogoutMessage(username) => {
      for (c <- chatters) {
        if (c.username == username) {
          c.actor ! LogoutMessageForActor
          println(s"sending Logout message for actor to ${c.actor} with username ${c.username}")
        } else{
          c.actor ! ChatActor.SendMessage(username + " logged out")
          println(s"sending message to ${c.actor} with username ${c.username}")
        }
      }
    }
    case GiveMeList => {sender() ! chatters
       //sender() ! UserList(chatters)
    }
  }

  def getActiveUsers: Int = {
    chatters.size
  }
}

object ChatManager {

  case class LogoutMessage(username: String)

  case class NewChatter(chatter: Chatter)

  case class ChatterGone(chatter: Chatter)

  case class Message(msg: String, username: String)

  case class Chatter(actor: ActorRef, username: String)

  object GiveMeList


}
