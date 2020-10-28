package tasks

import actors.ServiceActor
import akka.actor.{ActorRef, ActorSystem, Cancellable}
import javax.inject.{Inject, Named}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class KeepAliveTask @Inject() (actorSystem: ActorSystem, @Named("some-actor") someActor: ActorRef)(
  implicit executionContext: ExecutionContext
) {

  val cancellable = actorSystem.scheduler.scheduleAtFixedRate(
  initialDelay = 0.microseconds,
  interval = 20.seconds,
  receiver = someActor,
  message = "tick009"
)

  def StopMessagesWithInterval: Unit = {
    cancellable.cancel()
  }


}