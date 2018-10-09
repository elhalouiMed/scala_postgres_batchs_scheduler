package com.example

import akka.actor._
import akka.pattern._
import akka.testkit.TestActors.EchoActor
import scala.io.Source
import akka.actor.SupervisorStrategy._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global


class Supervisor extends Actor with ActorLogging {

  var batchActors : Map[String, ActorRef] = Map.empty[String, ActorRef] // a map containing the ActorRef of the actor that executing the batchName

  override def preStart(): Unit = log.info(s"Supervisor is Ready")
  override def preRestart(reason: Throwable, message: Option[Any]): Unit = log.info(s"Supervisor restarting...   --> ${reason}")
  override def postStop() = log.info(s"Supervisor Stopped")



  def receive = {
    case EndJob(batchName) =>  {
      println(s"\n\n\n\n batchName ==> ${batchName}\n\n\n\n")
      batchActors -= batchName
      println(batchActors)
    }
    case StartBatchs => {
       val batch1Props = BatchActor.apply(self)
       startBatch(BatchActor.batchName , batch1Props).map(batchActors += BatchActor.batchName -> _)
       println(batchActors)
     }
    case _ => println

    }



  def startBatch (batchName : String, actorProps : Props): Option[ActorRef] = {
    if( batchActors.contains(BatchActor.batchName)){
      log.warning(s" Actor ${BatchActor.batchName} already running !")
      None
    }
    else
    Some(context.actorOf(BackoffSupervisor.props(
      Backoff.onFailure(
        actorProps,
        childName = batchName,
        minBackoff = 3.seconds,
        maxBackoff = 30.seconds,
        randomFactor = 0.2
      ).withSupervisorStrategy(
        OneForOneStrategy() {
          case _:RestartMeException =>
            SupervisorStrategy.Restart
        })
    )))
  }
}