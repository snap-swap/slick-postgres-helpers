package com.snapswap.db.errors

import scala.util.control.NoStackTrace

trait DataError extends NoStackTrace {
  def details: String

  def dbDetails: Option[String] = None

  override def getMessage: String = dbDetails match {
    case None =>
      details
    case Some(d) =>
      s"$details\n$d"
  }
}