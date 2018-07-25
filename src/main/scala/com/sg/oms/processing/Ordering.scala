package com.sg.oms.processing


import scala.concurrent.{Await, Future}
import scala.util.{Failure, Random, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import scala.concurrent.duration._

object Ordering {

  def placeOrder(i: Int) = {

    println("Processing the order...")
    val orderId = createOrder(i)

    val shipmentId = for {
      isAvailable <- checkInStock(orderId)
      paymentStatus <- paymentGatewayStatus(orderId, isAvailable)
      shipId <- shipOrder(orderId, paymentStatus)
    } yield shipId

    Await.result(shipmentId, 2 seconds)
    shipmentId.onComplete {
      case Success(id) => println(s"Order processing completed. (shipment created - $id)")
      case Failure(e) => println("Failed to Complete the Order")
    }
    //println(s"Order processing completed.")
  }


  private def createOrder(i: Int) = {
    val orderId = Random.nextInt(100000)
    println(s"    New order created - ${orderId}")
    orderId
  }


  private def checkInStock(orderId: Int) = Future {
    println("Check product availability...")
    orderId match {
      case oId if oId%2 == 0 => {
        println("    Product is In-Stock")
        true
      }
      case _ => {
        println("    Product is Out-of-Stock")
        false
      }
    }
  }

  private def paymentGatewayStatus(orderId: Int, isAvailable: Boolean) = Future {
    println("processing by payment gateway...")

    if (isAvailable) {
      orderId match {
        case oId if oId%4 == 0 => {
          println("    Payment is successful")
          true
        }
        case _ => {
          println("    Payment failed!")
          false
        }
      }
    } else {
      println("    Re-order later")
      false
    }
  }

  private def shipOrder(orderId: Int, status: Boolean): Future[Option[Int]] = Future {

    println("Shipment under process...")

    val shipmentId = Random.nextInt(10000)

    (status, orderId) match {
      case (true, oId) if oId%4 == 0 => {
        println(s"    Shipment generated - $shipmentId")
        Some(shipmentId)
      }
      case (false, oId) => {
        println("    Payment confirmation awaited")
        None
      }
    }
  }

}
