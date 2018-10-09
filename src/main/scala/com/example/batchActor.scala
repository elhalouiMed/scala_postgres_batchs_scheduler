package com.example

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props ,PoisonPill}
import akka.actor.{ ActorSystem, Props, OneForOneStrategy, SupervisorStrategy }
import akka.pattern.{ Backoff, BackoffSupervisor }
import akka.testkit.TestActors.EchoActor
import scala.io.Source
import scala.concurrent.ExecutionContext

object BatchActor {
  final val batchName = "readFromFile"
  def apply(manager : ActorRef): Props = Props(new BatchActor(manager))
}

class BatchActor ( manager : ActorRef) extends Actor with ActorLogging {

  val batchName = "readFromFile"
  override def preRestart(reason: Throwable, message: Option[Any]) = {
    println("Yo, I am restarting...")
    super.preRestart(reason, message)
  }

  override def postRestart(reason: Throwable) = {
    println("...restart completed!")
    super.postRestart(reason)
  }

  override def preStart() = self ! StartBatch
  override def postStop() = log.info("Stopping actor ..")

import com.example.BatchActor._


  def receive = {
    case StartBatch =>
      val fileName = "/home/elhaloui/Work/testDatas/fileopen.txt"
      try{
        for(line <- Source.fromFile(fileName).getLines) {println(line) }
        println(s"sending to ${manager}")
        manager ! EndJob(batchName)
        self ! PoisonPill
      } catch {
        case e : Exception => {
          throw new RestartMeException
        }
      }

  }
}