package com.snapswap.db.errors

case class EntityNotFound(details: String = "Failed to lookup a requested entity",
                          override val dbDetails: Option[String] = None) extends DataError