package com.snapswap.db.errors

case class OverrideNotAllowed(details: String = "The same entity is already registered",
                              cause: Option[Throwable] = None,
                              override val dbDetails: Option[String] = None) extends DataError