package com.snapswap.db.helpers.runner

import java.sql.BatchUpdateException

import com.snapswap.db.errors.{DataError, EntityNotFound, InternalDataError, OverrideNotAllowed}
import org.postgresql.util.PSQLException
import slick.basic.BasicBackend
import slick.dbio.{DBIOAction, NoStream}
import slick.util.TreePrinter

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}

trait DbRunSafe {

  private[this] val pSQLExceptionErrCodes = Set("23505", "23503")

  implicit class SilentDbImporter[D <: BasicBackend#DatabaseDef](db: D) {
    def runSafe[R](a: DBIOAction[R, NoStream, Nothing])(implicit ec: ExecutionContext): Future[R] = {
      def additionalInfo = TreePrinter.default.get(a)

      db.run(a).recoverWith {
        case ex: UnsupportedOperationException if ex.getMessage == "empty.head" => // When query returns no results then UnsupportedOperationException("empty.head") is thrown instead of NoSuchElementException
          Future.failed[R](EntityNotFound(dbDetails = Some(additionalInfo)))
        case ex: NoSuchElementException =>
          Future.failed[R](EntityNotFound(details = ex.getMessage, dbDetails = Some(additionalInfo)))
        case ex: PSQLException =>
          // wrap with Option to avoid NullPointerException
          Option(ex.getServerErrorMessage).flatMap(msg => Option(msg.getSQLState)) match {
            case Some(sqlState) if pSQLExceptionErrCodes.contains(sqlState) =>
              Future.failed[R](OverrideNotAllowed(dbDetails = Some(additionalInfo)))
            case _ =>
              Future.failed[R](InternalDataError(ex.getMessage, dbDetails = Some(additionalInfo)))
          }
        case ex: DataError =>
          Future.failed[R](ex)
        case ex: BatchUpdateException =>
          Future.failed[R](InternalDataError(ex.getMessage, dbDetails = Some(getAllExceptions(ex))))
        case ex =>
          Future.failed[R](InternalDataError(ex.getMessage, dbDetails = Some(additionalInfo)))
      }
    }
  }

  @tailrec
  private[this] def getAllExceptions(exception: BatchUpdateException, msg: List[String] = List.empty): String = exception match {
    case ex: BatchUpdateException =>
      ex.getNextException match {
        case next: BatchUpdateException =>
          getAllExceptions(ex, s"${next.getClass.getSimpleName}: ${next.getMessage}" :: msg)
        case ex: PSQLException =>
          (s"${ex.getClass.getSimpleName}: ${ex.getMessage}" :: msg).reverse.mkString(",")
      }
    case _ =>
      msg.reverse.mkString(",")
  }
}

object DbRunHelper extends DbRunSafe