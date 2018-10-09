package com.example

import akka.actor._
import akka.pattern._
import akka.testkit.TestActors.EchoActor
import akka.actor.SupervisorStrategy._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import akka.persistence._
import akka.persistence.query._
import org.joda.time.DateTime

class Supervisor extends PersistentActor with ActorLogging {

  case class LastBatchTime(time : Long)

  override def persistenceId: String = "test-persistent-actor-counter"
  var batchActors : Map[String, ActorRef] = Map.empty[String, ActorRef] // a map containing the ActorRef of the actor that executing the batchName

  override def preStart(): Unit = log.info(s"Supervisor is Ready")
  override def preRestart(reason: Throwable, message: Option[Any]): Unit = log.info(s"Supervisor restarting...   --> ${reason}")
  override def postStop() = log.info(s"Supervisor Stopped")
  

  context.system.scheduler.schedule(
    30 milliseconds,
    5 seconds){
    //self  ! StartBatchs
    println(s"\n scheduler \n")
  }


  override def receiveRecover: Receive = {
    case time : Long =>   println(s"\n time ==> ${time}\n")
    case _ =>   println

  }

  override def receiveCommand = {
    case EndJob(batchName) =>  {
      println(s"\n batchName ==> ${batchName}\n")
      batchActors -= batchName
      println(batchActors)
    }
    case StartBatchs => {
       persist(new DateTime().getMillis() / 1000 ){ lbt =>
         println(s"\nlbt ==> ${lbt}\n")
         val batch1Props = BatchActor(self)
         startBatch(BatchActor.batchName , batch1Props).map(batchActors += BatchActor.batchName -> _)
         println(batchActors)
       }

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