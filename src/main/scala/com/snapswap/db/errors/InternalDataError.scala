package com.snapswap.db.errors

case class InternalDataError(details: String,
                             override val dbDetails: Option[String] = None) extends DataError