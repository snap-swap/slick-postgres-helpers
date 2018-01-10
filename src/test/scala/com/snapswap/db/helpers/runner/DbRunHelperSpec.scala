package com.snapswap.db.helpers.runner

import java.time.{Clock, ZonedDateTime}

import com.opentable.db.postgres.embedded.EmbeddedPostgres
import com.snapswap.db.driver.ExtendedPostgresProfile.api._
import com.snapswap.db.errors.{EntityNotFound, InternalDataError}
import org.scalatest.{AsyncWordSpec, Matchers, OneInstancePerTest}
import slick.lifted.TableQuery

class DbRunHelperSpec
  extends AsyncWordSpec
    with Matchers
    with OneInstancePerTest {

  import runSafe._

  val epg: EmbeddedPostgres = EmbeddedPostgres.start
  val db: Database = Database.forDataSource(epg.getPostgresDatabase, None)
  val tbl: TableQuery[TestTable] = TableQuery[TestTable]

  "SlickRunWrapper" should {
    "replace known errors by appropriate DataError" in {
      val action = for {
        _ <- db.runSafe(tbl.schema.create)
        result <- db.runSafe(sql"""select "IntColumn" from "TestTable" """.as[Int].head)
      } yield result

      action.failed.map { r =>
        r shouldBe a[EntityNotFound]
      }
    }
    "replace unknown errors by InternalDataError with original error description" in {
      val action = for {
        _ <- db.runSafe(tbl.schema.create)
        result <- db.runSafe(DBIO.seq(sql"""select IntColumn from NotExistingTable""".as[Int]).map(_ => ()))
      } yield result

      action.failed map { result =>
        result shouldBe a[InternalDataError]
        result.getMessage should startWith("ERROR: relation \"notexistingtable\" does not exist\n  Position: 23")
      }
    }
    "when success - return Future result" in {
      val now: ZonedDateTime = ZonedDateTime.now(Clock.systemUTC())

      val action = for {
        _ <- db.runSafe(tbl.schema.create)
        result <- db.runSafe((tbl += ((1, now))).andThen(tbl.map(_.IntColumn).result))
      } yield result

      action.map { result =>
        result should equal(Seq(1))
      }
    }

    "Correct work with ZonedDateTime" in {
      val now: ZonedDateTime = ZonedDateTime.now(Clock.systemUTC())

      val action = for {
        _ <- db.runSafe(tbl.schema.create)
        _ <- db.runSafe(tbl += ((1, now)))
        result <- db.runSafe(tbl.map(_.ZonedDateTimeColumn).result)
      } yield result

      action.map { result: Seq[ZonedDateTime] =>
        result should contain(now)
      }
    }
  }

  class TestTable(tag: Tag) extends Table[(Int, ZonedDateTime)](tag, "TestTable") {
    def IntColumn = column[Int]("IntColumn")

    def ZonedDateTimeColumn = column[ZonedDateTime]("ZonedDateTimeColumn")

    def * = (IntColumn, ZonedDateTimeColumn)
  }

}