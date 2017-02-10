package com.snapswap.db.helpers.futurity

import com.snapswap.db.errors.DataError

/**
  * Allows to use conditions with DataError in for comprehension with Future
  *
  * @example
  * for {
  * item <- dao.get(id)
  * if payer.isMarkedForDelete thenFailWith SomeError(s"item is marked for deletion")
  * } yield order   // returns Future.successful(item) or Future.failed(SomeError(...))
  */
trait ToDataErrorHelper {

  case class DataCondition(condition: Boolean) {
    def thenFailWith(error: => DataError): Boolean =
      if (condition) throw error
      else true
  }

  implicit def toDataCondition(cond: Boolean): DataCondition = DataCondition(cond)
}

object ToDataErrorHelper extends ToDataErrorHelper