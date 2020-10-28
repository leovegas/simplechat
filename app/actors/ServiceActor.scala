package actors

import actors.ChatManager.{Chatter, GiveMeList}
import actors.ServiceActor.UserList
import akka.actor.{Actor, ActorRef, Props}

class ServiceActor(manager: ActorRef) extends Actor {
 var userList: List[Chatter] = List.empty[Chatter]
  manager ! GiveMeList

  def GetUserList = {
    userList
  }

  override def receive: Receive = {
    case UserList(userlist) => {
      userList = userlist
      context.stop(self)
    }
  }

  override def postStop(): Unit = {
    GetUserList
    super.postStop()
  }
}

object ServiceActor {
  def props(manager: ActorRef) = Props(new ServiceActor(manager))

  case class UserList(userlist: List[Chatter])
}


