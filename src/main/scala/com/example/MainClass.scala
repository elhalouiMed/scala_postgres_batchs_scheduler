//#full-example
package com.example

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import akka.actor.{ ActorSystem, Props, OneForOneStrategy, SupervisorStrategy }
import akka.pattern.{ Backoff, BackoffSupervisor }
import akka.testkit.TestActors.EchoActor
import scala.io.Source

object StartBatch
object StartBatchs
class RestartMeException extends Exception("RESTART")
case class EndJob(batchName : String)

object MainClass extends App {

  import scala.concurrent.duration._
  // Create the 'innovlab' actor system
  val system: ActorSystem = ActorSystem("innovlab")
  val supervisor1 = Props(classOf[Supervisor])
  val sup = system.actorOf(supervisor1, name = "Supervisor")

  sup ! StartBatchs


}
